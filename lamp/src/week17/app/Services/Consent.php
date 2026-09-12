<?php

namespace App\Services;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cookie;
use Illuminate\Support\Facades\DB;

// ============================================================
// Consent — 쿠키 동의
//   지금 includes/consent.php 를 옮긴 것이다.
//
//   ★ 이 클래스는 '무엇에 동의했나'만 안다. 쿠키 이름은 하나도 모른다.
//     어느 쿠키가 어느 항목에 속하는지는 쿠키를 만드는 쪽(Prefs)이 안다.
//     서로를 덜 알수록 안 엉킨다.
//
//   ★ JS를 쓰지 않는다. 배너는 그냥 폼이고, 누르면 POST 가 간다.
// ============================================================
class Consent
{
    private const COOKIE = 'consent';
    private const DAYS   = 365;

    // 선택 항목들. 필수 쿠키(로그인·CSRF)는 동의 대상이 아니다.
    public const ITEMS = ['view', 'search', 'ui'];

    public const LABELS = [
        'view'   => '최근 본 글·작품 기억하기',
        'search' => '최근 검색어 기억하기',
        'ui'     => '정렬·목록 개수 기억하기',
    ];

    // 정책이 바뀌면 올린다. 값이 다르면 다시 물어본다.
    private const POLICY_VERSION = 1;

    public function decided(Request $request): bool
    {
        $raw = $this->raw($request);

        return $raw !== null && (int) ($raw['v'] ?? 0) === self::POLICY_VERSION;
    }

    public function has(Request $request, string $item): bool
    {
        $raw = $this->raw($request);

        return $raw !== null
            && (int) ($raw['v'] ?? 0) === self::POLICY_VERSION
            && in_array($item, (array) ($raw['items'] ?? []), true);
    }

    // ── 결정 기록 ───────────────────────────────────────────
    //   ★ 쿠키에만 남기지 않고 서버에도 남긴다.
    //     "언제 무엇에 동의했는가"를 나중에 보여줄 수 있어야 하기 때문이다.
    //     ⚠ 다만 누구인지 특정하지 않는다 — IP는 앞부분만, 임의의 동의 번호로만 잇는다.
    public function record(Request $request, array $items, string $source): void
    {
        $items = array_values(array_intersect($items, self::ITEMS));

        Cookie::queue(
            self::COOKIE,
            json_encode(['v' => self::POLICY_VERSION, 'items' => $items]),
            self::DAYS * 24 * 60
        );

        DB::table('consent_log')->insert([
            'consent_id'     => bin2hex(random_bytes(16)),
            'user_id'        => $request->user()?->id,
            'action'         => $items === [] ? 'reject' : 'accept',
            'source'         => $source,
            'policy_version' => self::POLICY_VERSION,
            'items'          => implode(',', $items),
            'user_agent'     => substr((string) $request->userAgent(), 0, 255),
            'ip_prefix'      => $this->ipPrefix((string) $request->ip()),
            'created_at'     => now(),
        ]);
    }

    private function raw(Request $request): ?array
    {
        $decoded = json_decode((string) $request->cookie(self::COOKIE), true);

        return is_array($decoded) ? $decoded : null;
    }

    // ⚠ IP를 통째로 남기지 않는다. 기록의 목적은 '동의가 있었다'를 보이는 것이지
    //   누가 했는지를 특정하는 것이 아니다.
    private function ipPrefix(string $ip): string
    {
        if (str_contains($ip, ':')) {                       // IPv6
            return implode(':', array_slice(explode(':', $ip), 0, 3)) . '::';
        }

        return implode('.', array_slice(explode('.', $ip), 0, 3)) . '.0';
    }
}
