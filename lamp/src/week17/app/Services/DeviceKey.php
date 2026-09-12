<?php

namespace App\Services;

use App\Models\Device;
use Illuminate\Http\Request;

// ============================================================
// DeviceKey — 기기 '도장'(공개키 서명)으로 세션 연장을 통제한다
//   지금 includes/device_key.php 를 옮긴 것이다.
//
//   [한 줄 요약]
//     번호표를 내밀면 통과하던 것을, 도장을 찍어야 연장되게 바꾼다.
//
//   [왜 필요한가]
//     쿠키만으로 로그인을 유지하면 '훔치면 그대로 통하는' 성질을 없앨 수 없다.
//     브라우저 안에 꺼낼 수 없는 도장(WebCrypto, extractable=false)을 만들면
//     · 도장은 브라우저 밖으로 안 나온다      → 훔쳐갈 것 자체가 없다
//     · 서버가 매번 다른 숫자를 주고 찍게 한다 → 자국을 베껴도 다음번엔 안 통한다
//
//   [왜 매 요청이 아니라 가끔인가]
//     서명 한 번에 수백 ms가 든다. 그래서 평소 요청은 쿠키로 통과시키고,
//     '마지막으로 도장을 확인한 시각'이 만료될 때만 다시 찍게 한다.
//     ※ 확인이 만료되면 세션이 살아 있어도 그 세션으로 화면을 열 수 없다.
//
//   [남은 한계 — 발표에서 먼저 말할 것]
//     ① 소프트웨어 도장이다. 브라우저 프로필을 통째로 복사당하면 도장도 같이 간다
//     ② JS가 필요하다. 서버 폼으로는 대신할 수 없다
//     ③ 등록 직전 몇 초는 여전히 쿠키만으로 통하는 구간이다
// ============================================================
class DeviceKey
{
    // 도장 확인이 유효한 시간(초). 지나면 다시 찍어야 한다.
    //   ★ 설정으로 뺀 이유 — 시험할 때 짧게 줄여 '만료되면 정말 막히는가'를 확인해야 한다.
    public static function proofTtl(): int
    {
        return (int) config('auth.device_key_proof_ttl', 60);
    }

    // 서버가 던진 숫자의 수명(초).
    public const CHALLENGE_TTL = 120;

    // 비밀번호를 확인한 뒤 도장을 등록할 수 있는 시간(초).
    public const ENROLL_WINDOW = 180;

    // 세션에 담는 값들
    public const S_PROOF_AT  = 'key_proof_at';
    public const S_CHALLENGE = 'key_challenge';
    public const S_ENROLL_OK = 'key_enroll_ok';

    // P-256 공개키(SPKI)의 고정 머리말과 전체 길이.
    //   ★ 길이·머리말을 확인하는 이유 — 아무 문자열이나 공개키로 저장되면
    //     나중에 openssl 에 그대로 넘어간다. 모양부터 못 박는다.
    private const SPKI_PREFIX = '3059301306072a8648ce3d020106082a8648ce3d030107034200';
    private const SPKI_LEN    = 91;

    // ── 공개키 모양 확인 ────────────────────────────────────
    public function isValidPublicKey(string $spkiBase64): bool
    {
        $der = base64_decode($spkiBase64, true);

        return $der !== false
            && strlen($der) === self::SPKI_LEN
            && str_starts_with(bin2hex($der), self::SPKI_PREFIX);
    }

    // ── 숫자 발급 (한 번 쓰면 버린다) ───────────────────────
    public function issueChallenge(Request $request): string
    {
        $challenge = bin2hex(random_bytes(32));

        $request->session()->put(self::S_CHALLENGE, ['value' => $challenge, 'at' => time()]);

        return $challenge;
    }

    // 꺼내면서 지운다 — 같은 숫자를 두 번 쓸 수 없게.
    public function takeChallenge(Request $request): ?string
    {
        $saved = $request->session()->pull(self::S_CHALLENGE);

        if (! is_array($saved) || time() - (int) ($saved['at'] ?? 0) > self::CHALLENGE_TTL) {
            return null;
        }

        return $saved['value'];
    }

    // ── 서명 검증 ───────────────────────────────────────────
    //   브라우저(WebCrypto)는 r‖s 64바이트 원시 서명을 준다.
    //   openssl 은 DER 형식을 원하므로 바꿔서 넘긴다.
    public function verifySignature(string $spkiBase64, string $challenge, string $signatureBase64): bool
    {
        if (! $this->isValidPublicKey($spkiBase64)) {
            return false;
        }

        $raw = base64_decode($signatureBase64, true);
        if ($raw === false) {
            return false;
        }

        $der = $this->rawSignatureToDer($raw);
        if ($der === null) {
            return false;
        }

        $pem = "-----BEGIN PUBLIC KEY-----\n"
             . chunk_split($spkiBase64, 64, "\n")
             . "-----END PUBLIC KEY-----\n";

        $publicKey = openssl_pkey_get_public($pem);
        if ($publicKey === false) {
            return false;
        }

        return openssl_verify($challenge, $der, $publicKey, OPENSSL_ALGO_SHA256) === 1;
    }

    private function rawSignatureToDer(string $raw): ?string
    {
        if (strlen($raw) !== 64) {
            return null;
        }

        $toInteger = static function (string $bytes): string {
            $bytes = ltrim($bytes, "\x00");
            if ($bytes === '') {
                $bytes = "\x00";
            }
            // 최상위 비트가 켜져 있으면 음수로 읽히므로 0을 앞에 붙인다
            if (ord($bytes[0]) & 0x80) {
                $bytes = "\x00" . $bytes;
            }

            return "\x02" . chr(strlen($bytes)) . $bytes;
        };

        $body = $toInteger(substr($raw, 0, 32)) . $toInteger(substr($raw, 32, 32));

        return "\x30" . chr(strlen($body)) . $body;   // P-256이라 길이는 항상 127 이하
    }

    // ── 이 기기에 등록된 공개키 ─────────────────────────────
    public function publicKeyFor(int $userId, string $deviceId): ?string
    {
        return Device::where('user_id', $userId)->where('device_id', $deviceId)->value('public_key');
    }

    public function savePublicKey(int $userId, string $deviceId, string $spkiBase64): bool
    {
        if (! $this->isValidPublicKey($spkiBase64)) {
            return false;
        }

        return Device::where('user_id', $userId)
            ->where('device_id', $deviceId)
            ->update(['public_key' => $spkiBase64, 'key_added_at' => now()]) > 0;
    }

    // ── 도장 확인 시각 ──────────────────────────────────────
    public function markProved(Request $request, int $userId, string $deviceId): void
    {
        $request->session()->put(self::S_PROOF_AT, time());

        Device::where('user_id', $userId)->where('device_id', $deviceId)
            ->update(['key_proved_at' => now()]);
    }

    public function proofIsFresh(Request $request): bool
    {
        $at = (int) $request->session()->get(self::S_PROOF_AT, 0);

        return $at > 0 && time() - $at <= self::proofTtl();
    }

    // ── 등록 창 (비밀번호를 막 확인한 직후에만 열린다) ──────
    public function openEnrollWindow(Request $request): void
    {
        $request->session()->put(self::S_ENROLL_OK, time());
    }

    public function canEnroll(Request $request): bool
    {
        $at = (int) $request->session()->get(self::S_ENROLL_OK, 0);

        return $at > 0 && time() - $at <= self::ENROLL_WINDOW;
    }

    public function closeEnrollWindow(Request $request): void
    {
        $request->session()->forget(self::S_ENROLL_OK);
    }
}
