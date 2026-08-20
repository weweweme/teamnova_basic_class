<?php
// ============================================================
// device_key.php — 기기 도장(공개키 서명)으로 세션 연장을 통제한다
//
//   [★ 한 줄 요약]
//     번호표를 **내밀면 통과**하던 것을, **도장을 찍어야 연장**되게 바꿨다.
//
//   [무엇을 고치려는 것인가]
//     쿠키만으로 로그인을 유지하면 **훔치면 그대로 통하는 문제**를 없앨 수 없다.
//     내미는 것만으로 통과하는 값(bearer)이라는 성질 자체가 원인이기 때문이다.
//     그래서 우리는 지금까지 **줄이기**만 했다 — 짧게(회전) · 얕게(재인증) · 보이게(기기 목록).
//
//   [★★ 도장이 무엇을 바꾸나 — 훔칠 것이 없어진다]
//     브라우저 안에 **꺼낼 수 없는 도장**을 만든다(WebCrypto, extractable=false).
//       서버   "아무 숫자나 줄게. 그 도장으로 찍어서 보내봐"
//       기기   도장은 안 내주고, 찍은 자국만 보낸다
//     · 도장은 브라우저 밖으로 안 나온다     → **훔쳐갈 것 자체가 없다**
//     · 숫자가 매번 바뀌니 자국도 매번 다르다 → **자국을 베껴놔도 다음번엔 안 통한다**
//     ※ 업계 용어로는 이 도장 찍기를 **서명**, 도장의 공개된 짝을 **공개키**라고 부른다.
//
//   [★ 그런데 왜 쿠키를 그대로 두나 — 도장이 느리다]
//     DBSC 설명서(W3C)의 실측: 도장 한 번에 **P50 200ms · P95 600ms.**
//     매 요청 찍으면 클릭할 때마다 그만큼 멈춘다. 그래서 DBSC도 이렇게 한다:
//       평소 요청     → 짧은 쿠키로 통과   (빠르다)
//       쿠키 만료 때  → 도장 찍고 연장     (느려도 가끔이라 괜찮다)
//     ★ 우리 구조와 **모양이 같다.** 바뀐 건 조건 하나뿐이다:
//       전:  요청만 오면        계속 쓸 수 있다   ← 훔친 쪽도 요청만 보내면 무기한
//       후:  도장을 확인받아야  계속 쓸 수 있다   ← 훔친 쪽은 1분 뒤 막힌다
//     ※ '세션을 연장한다'가 아니다 — 갱신되는 건 **'마지막으로 도장을 확인한 시각'** 뿐이고,
//       확인이 만료되면 세션은 살아 있어도 **그 세션으로 아무 화면도 못 연다.**
//
//   [★ 이것이 '절대 상한 없음' 문제도 절반 메운다]
//     유휴 20분·TTL 30분은 둘 다 *마지막 사용* 기준이라 요청할 때마다 뒤로 밀렸다(sliding).
//     이제는 밀리는 조건에 **도장**이 붙는다. 도장 없는 쪽은 아무리 요청해도 못 민다.
//
//   [남은 한계 — 발표에서 먼저 말할 것]
//     ① **소프트웨어 도장이다.** TPM이 아니라 브라우저 저장소(IndexedDB)에 있다.
//        기기에 악성코드가 들어와 브라우저 프로필을 통째로 복사하면 **도장도 같이 간다.**
//        → DBSC가 노리는 바로 그 위협에는 **부분 방어**다.
//        (구글도 이걸 알아서 "보안 하드웨어 없는 기기용 소프트웨어 키"를 로드맵에 따로 뒀다)
//     ② **JS가 필요하다.** 우리 쿠키 동의는 'JS 0줄'이 자랑이었는데, 여기서는 못 그런다.
//        도장은 브라우저만 찍을 수 있어서 서버 폼으로는 대신할 수 없다.
//     ③ **등록 직전 몇 초는 여전히 bearer다.** 비밀번호를 맞힌 뒤 도장을 만들기 전 구간.
// ============================================================

require_once __DIR__ . '/db.php';
require_once __DIR__ . '/devices.php';

// ★ 도장을 '있으면 좋은 것'이 아니라 **필수**로 둔다.
//   [왜 필수인가]
//     선택으로 두면 공격자는 그냥 **도장 없는 척**하면 된다. 방어가 통째로 무의미해진다.
//     (보안 장치를 껐다 켤 수 있게 두면, 끄는 쪽은 언제나 공격자다)
//   ※ 운영 스위치로 남겨둔다 — false로 두면 도장이 없어도 예전처럼 동작한다.
//     시연 중 사고가 나면 이 한 줄만 바꿔 되돌릴 수 있다.
const DEVICE_KEY_REQUIRED = true;

// 도장 한 번으로 얼마나 버티나. **이것이 곧 '훔친 쿠키의 최대 수명'이다.**
//   ★ 짧을수록 안전하지만 도장을 자주 찍어야 한다(한 번에 0.2~0.6초).
//   ※ 실무는 10분 안팎을 쓴다. 여기서는 **훔친 쿠키가 죽는 것을 눈으로 보려고** 1분으로 뒀다.
const KEY_PROOF_TTL = 60;

// 만료 **몇 초 전에** 미리 찍을까.
//   ★ 고정 60초로 두면 수명이 60초일 때 '항상 지금 당장'이 되어 **매 요청 도장을 찍게 된다.**
//     그러면 "평소엔 쿠키로 빠르게, 만료 때만 도장"이라는 설계가 통째로 무너진다.
//     그래서 고정값이 아니라 **수명에 대한 비율**로 잡는다.
function key_proof_margin(): int {
    return (int) min(60, KEY_PROOF_TTL / 3);
}

// 서버가 던진 숫자의 유효 시간. 짧아야 한다 — 오래 살면 미리 받아둔 자국을 쓸 여지가 생긴다.
const KEY_CHALLENGE_TTL = 120;

const SESSION_KEY_PROOF_AT  = 'key_proof_at';    // 이 세션이 마지막으로 도장을 확인한 시각
const SESSION_KEY_CHALLENGE = 'key_challenge';   // 방금 던진 숫자 (한 번 쓰면 버린다)

// ── 도장을 '다시' 등록할 수 있는 짧은 창 ─────────────────────
//   [★ 왜 필요한가 — 안 두면 사용자가 스스로 잠긴다]
//     사용자가 브라우저 저장소를 지우면 도장이 사라진다(흔한 일이다).
//     그런데 서버엔 옛 도장이 남아 있어 대조가 영영 실패한다 →
//     **그 브라우저로는 다시 로그인해도 못 들어온다.** 잠금이 풀릴 길이 없다.
//
//   [★★ 그럼 언제 허용해야 안전한가 — 비밀번호를 맞힌 직후]
//     쿠키를 훔친 쪽에는 **비밀번호가 없다.** 그래서 '비밀번호를 방금 확인함'을
//     조건으로 걸면, 주인은 풀 수 있고 훔친 쪽은 못 푼다.
//     → start_session_for()에서만 이 표시를 남긴다(거기 오는 길은 비밀번호뿐이다).
//
//   ★ 그리고 창을 **짧게** 둔다. 로그인 직후 세션을 훔쳐가면 이 표시까지 같이 가므로,
//     열려 있는 시간이 곧 위험 구간이다. 등록에 성공하면 **그 자리에서 닫는다.**
const SESSION_KEY_ENROLL_OK = 'key_enroll_ok';
const KEY_ENROLL_WINDOW     = 180;   // 3분

// P-256 공개키(SPKI/DER)의 고정 머리말 26바이트.
//   ★ 브라우저가 export('spki')로 뽑아준 값을 **그대로** 받는다. 서버가 손으로 조립하지 않는다.
//     대신 "우리가 기대한 그 형식이 맞나"만 확인한다 —
//     엉뚱한 걸 받아 저장하면 나중에 대조가 조용히 실패한다.
const P256_SPKI_PREFIX = '3059301306072a8648ce3d020106082a8648ce3d030107034200';
const P256_SPKI_LEN    = 91;      // 머리말 26 + 04 표시 1 + X 32 + Y 32

// ── 도장 등록 ────────────────────────────────────────────────

// 브라우저가 보낸 공개키가 우리가 기대한 형식인가?
//   ★ 길이·머리말만 본다. 이건 '검증'이 아니라 **형식 확인**이다.
//     진짜 검증은 아래 verify_key_signature()가 한다 — 자국이 맞아떨어지느냐로.
function is_valid_public_key(string $spkiBase64): bool {
    $der = base64_decode($spkiBase64, true);
    if ($der === false || strlen($der) !== P256_SPKI_LEN) {
        return false;
    }
    return str_starts_with(bin2hex($der), P256_SPKI_PREFIX . '04');
}

// 이 회원 + 이 기기의 공개키. 없으면 null.
function device_public_key(int $userId): ?string {
    $stmt = db()->prepare('SELECT public_key FROM user_devices WHERE user_id = ? AND device_id = ?');
    $stmt->execute([$userId, device_id()]);
    $key = $stmt->fetchColumn();

    return ($key === false || $key === null || $key === '') ? null : (string) $key;
}

// 공개키를 이 기기 줄에 적는다.
//   ★★ **이미 있으면 덮어쓰지 않는다.** 이게 이 함수에서 가장 중요한 줄이다.
//     덮어쓰게 두면, 세션을 훔친 쪽이 **자기 도장을 새로 등록**해 버리면 그만이다.
//     그러면 도장 검사 전체가 무의미해진다 — 훔친 쿠키로 자기 열쇠를 만들어 끼우는 셈이니까.
//     → 도장을 갈아 끼우려면 **기기를 끊고 비밀번호로 다시 로그인**해야 한다.
function save_device_public_key(int $userId, string $spkiBase64, bool $replace = false): bool {
    if (!is_valid_public_key($spkiBase64)) {
        return false;
    }

    // ★ 기본은 '빈 자리에만' 쓴다. 덮어쓰기는 비밀번호를 방금 확인했을 때만 열린다.
    $onlyIfEmpty = $replace ? '' : ' AND public_key IS NULL';

    $stmt = db()->prepare(
        'UPDATE user_devices
            SET public_key = ?, key_added_at = NOW()
          WHERE user_id = ? AND device_id = ?' . $onlyIfEmpty
    );
    $stmt->execute([$spkiBase64, $userId, device_id()]);

    // ★ rowCount()는 '값이 실제로 바뀐 줄'만 센다 —
    //   같은 도장을 다시 등록하면 0이 나오므로, 그 경우도 성공으로 본다.
    return $stmt->rowCount() >= 1 || ($replace && device_public_key($userId) === $spkiBase64);
}

// 지금 도장을 새로(다시) 등록해도 되나? = 비밀번호를 방금 확인했나.
function can_enroll_key(): bool {
    $at = (int) ($_SESSION[SESSION_KEY_ENROLL_OK] ?? 0);

    return $at !== 0 && time() - $at <= KEY_ENROLL_WINDOW;
}

// 등록 창을 연다 — **비밀번호를 확인한 자리에서만** 부른다.
function open_key_enroll_window(): void {
    $_SESSION[SESSION_KEY_ENROLL_OK] = time();
}

// 등록 창을 닫는다. 성공하면 곧바로 닫아 위험 구간을 최소로 줄인다.
function close_key_enroll_window(): void {
    unset($_SESSION[SESSION_KEY_ENROLL_OK]);
}

// ── 숫자 던지고 자국 받기 ────────────────────────────────────

// 아무 숫자나 하나 만들어 세션에 적어두고 돌려준다.
//   ★ 세션에 적어두는 이유: **던진 쪽과 받는 쪽이 같은 세션이어야** 한다.
//     남의 화면에서 받은 숫자로 내 세션을 연장하는 길을 막는다.
function issue_key_challenge(): string {
    $challenge = bin2hex(random_bytes(32));
    $_SESSION[SESSION_KEY_CHALLENGE] = ['value' => $challenge, 'at' => time()];

    return $challenge;
}

// 적어둔 숫자를 꺼내면서 **지운다**(한 번 쓰면 끝).
//   ★ 안 지우면 같은 자국을 두 번 쓸 수 있다(재사용 공격).
//     "한 번만 쓰이는 값"은 반드시 꺼내는 자리에서 지워야 한다 — 플래시와 같은 원칙이다.
function take_key_challenge(): ?string {
    $saved = $_SESSION[SESSION_KEY_CHALLENGE] ?? null;
    unset($_SESSION[SESSION_KEY_CHALLENGE]);

    if (!is_array($saved) || time() - (int) ($saved['at'] ?? 0) > KEY_CHALLENGE_TTL) {
        return null;
    }
    return (string) $saved['value'];
}

// ── 자국 대조 ────────────────────────────────────────────────

// 브라우저가 만든 자국은 r·s 두 숫자를 그냥 이어붙인 64바이트인데,
// PHP(OpenSSL)는 **DER이라는 포장**을 기대한다. 그 포장을 씌워준다.
//   ★ 알고리즘이 다른 게 아니라 **같은 값의 표기법이 다를 뿐**이다.
//     (같은 날짜를 2026-08-17로도, 17/08/2026으로도 쓰는 것과 같다)
function raw_signature_to_der(string $raw): ?string {
    if (strlen($raw) !== 64) {
        return null;
    }

    // 숫자 하나를 DER 정수로 포장한다.
    //   · 앞의 0은 뺀다 (DER은 군더더기 0을 금지한다)
    //   · 맨 앞 비트가 1이면 0x00을 덧댄다 — 안 그러면 **음수로 읽힌다**
    $toInteger = static function (string $bytes): string {
        $bytes = ltrim($bytes, "\x00");
        if ($bytes === '') {
            $bytes = "\x00";
        }
        if (ord($bytes[0]) & 0x80) {
            $bytes = "\x00" . $bytes;
        }
        return "\x02" . chr(strlen($bytes)) . $bytes;
    };

    $body = $toInteger(substr($raw, 0, 32)) . $toInteger(substr($raw, 32, 32));

    return "\x30" . chr(strlen($body)) . $body;   // P-256이라 길이는 항상 127 이하다
}

// 이 자국이 그 도장으로 찍힌 게 맞나?
//   ★ 여기가 이 모듈의 심장이다. 나머지는 전부 이 한 줄을 부르기 위한 준비다.
function verify_key_signature(string $spkiBase64, string $challenge, string $signatureBase64): bool {
    if (!is_valid_public_key($spkiBase64)) {
        return false;
    }

    $raw = base64_decode($signatureBase64, true);
    if ($raw === false) {
        return false;
    }
    $der = raw_signature_to_der($raw);
    if ($der === null) {
        return false;
    }

    // 저장해둔 DER 공개키를 PEM 봉투에 넣는다 (OpenSSL이 읽는 형식).
    $pem = "-----BEGIN PUBLIC KEY-----\n"
         . chunk_split($spkiBase64, 64, "\n")
         . "-----END PUBLIC KEY-----\n";

    $publicKey = openssl_pkey_get_public($pem);
    if ($publicKey === false) {
        return false;
    }

    // 1 = 맞음 / 0 = 틀림 / -1 = 오류. **1일 때만** 통과시킨다.
    //   ★ `!== 0` 같은 식으로 쓰면 오류(-1)까지 통과한다. 실무에서 자주 나오는 실수다.
    return openssl_verify($challenge, $der, $publicKey, OPENSSL_ALGO_SHA256) === 1;
}

// ── 판정 ─────────────────────────────────────────────────────

// 도장을 확인했다고 적는다. 세션과 기기 줄 **양쪽에** 남긴다.
//   ★ 세션에만 적으면 훔친 쪽이 그 값을 함께 가져간다.
//   ★ 기기 줄에만 적으면 같은 기기의 다른 세션까지 덩달아 통과한다.
//     → 둘 다 적고, 판정도 세션 것으로 한다(기기 줄은 화면 표시·감사용).
function mark_key_proved(int $userId): void {
    $_SESSION[SESSION_KEY_PROOF_AT] = time();

    db()->prepare('UPDATE user_devices SET key_proved_at = NOW() WHERE user_id = ? AND device_id = ?')
        ->execute([$userId, device_id()]);
}

// 도장 확인이 아직 유효한가?
function has_key_proof(): bool {
    if (!DEVICE_KEY_REQUIRED) {
        return true;
    }
    $at = (int) ($_SESSION[SESSION_KEY_PROOF_AT] ?? 0);

    return $at !== 0 && time() - $at <= KEY_PROOF_TTL;
}

// 다음 도장까지 남은 시간(초). 화면이 미리 갱신을 걸 수 있게 알려준다.
//   ★ 만료된 **뒤에** 갱신하면 사용자가 화면 전환을 겪는다.
//     남은 시간을 알려주면 브라우저가 **만료 전에 조용히** 찍어둘 수 있다.
function key_proof_seconds_left(): int {
    $at = (int) ($_SESSION[SESSION_KEY_PROOF_AT] ?? 0);
    if ($at === 0) {
        return 0;
    }
    return max(0, KEY_PROOF_TTL - (time() - $at));
}

// 이 요청이 도장 검사에서 빠지는 자리인가?
//   ★ 도장을 받으러 가는 길까지 막으면 **아무도 도장을 못 찍는다**(무한 루프).
//     검사를 넣을 때 가장 먼저 확인해야 하는 지점이다.
// 이 요청이 **브라우저가 스스로 보낸 도장 갱신 요청**인가?
//   [왜 구분해야 하나 — 실제로 크게 밟았다]
//     device-key.js가 페이지를 열고 40초 뒤 이 두 곳을 자동으로 부른다.
//     그런데 auth.php의 유휴 시계가 **모든 요청을 '사람이 움직였다'로 세는 바람에**,
//     자리를 비워도 기계가 시계를 대신 밀어주어 **자동 로그아웃이 영영 안 걸렸다.**
//   ★ 유휴 시계가 재려는 건 **"사람이 자리에 있나"** 다.
//     기계가 밀어주는 시계는 시계가 아니다. (사용자가 들고 있는 시계가 시계가 아닌 것과 같다)
const KEY_PROOF_PATHS = ['/session/challenge.php', '/session/refresh.php'];

function is_key_proof_request(): bool {
    return in_array((string) ($_SERVER['SCRIPT_NAME'] ?? ''), KEY_PROOF_PATHS, true);
}

function is_key_exempt_path(): bool {
    $path = (string) ($_SERVER['SCRIPT_NAME'] ?? '');

    return str_starts_with($path, '/session/')       // 도장 등록·확인 화면
        || str_starts_with($path, '/auth/');         // 로그인·로그아웃 자체
}
