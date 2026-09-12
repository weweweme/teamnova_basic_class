<?php

namespace App\Services;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cookie;

// ============================================================
// Prefs — 화면 취향과 최근 기록을 쿠키에 담는다
//   지금 includes/prefs.php 를 옮긴 것이다.
//
//   ★ 여기만 '어느 쿠키가 어느 동의 항목에 속하는지'를 안다.
//     Consent 는 항목 이름만 알고 쿠키 이름은 모른다.
// ============================================================
class Prefs
{
    private const DAYS = 30;

    // 쿠키 이름 → 동의 항목
    public const OWNED_BY = [
        'recent_posts'   => 'view',
        'recent_works'   => 'view',
        'recent_search'  => 'search',
        'pref_sort'      => 'ui',
        'pref_sentiment' => 'ui',
        'per_page'       => 'ui',
    ];

    private const RECENT_MAX   = 5;
    private const PER_PAGE_SET = [15, 30, 50];

    public function __construct(private Consent $consent) {}

    // ── 한 페이지에 보여줄 글 수 ────────────────────────────
    public function perPage(Request $request, int $default = 15): int
    {
        $value = (int) $request->cookie('per_page');

        // ★ '숫자니까 안전'이 아니라 '우리가 정한 값인가'로 검사한다.
        //   숫자라는 이유로 그대로 쓰면 ?per_page=100000 으로 서버를 갈아버릴 수 있다.
        return in_array($value, self::PER_PAGE_SET, true) ? $value : $default;
    }

    public function rememberPerPage(Request $request, int $value): void
    {
        if (in_array($value, self::PER_PAGE_SET, true)) {
            $this->put($request, 'per_page', (string) $value);
        }
    }

    // ── 게시판 정렬 ─────────────────────────────────────────
    //   ★ 쿠키 값을 그대로 믿지 않는다. 우리가 정한 목록에 있는 값만 쓴다.
    public function sort(Request $request, array $allowed, string $default): string
    {
        $value = (string) $request->cookie('pref_sort');

        return in_array($value, $allowed, true) ? $value : $default;
    }

    public function rememberSort(Request $request, string $value, array $allowed): void
    {
        if (in_array($value, $allowed, true)) {
            $this->put($request, 'pref_sort', $value);
        }
    }

    // ── 게시판 감상 필터 ────────────────────────────────────
    public function sentiment(Request $request, array $allowed): string
    {
        $value = (string) $request->cookie('pref_sentiment');

        return in_array($value, $allowed, true) ? $value : '';
    }

    public function rememberSentiment(Request $request, string $value, array $allowed): void
    {
        if ($value === '' || in_array($value, $allowed, true)) {
            $this->put($request, 'pref_sentiment', $value);
        }
    }

    // ── 최근 본 작품 ────────────────────────────────────────
    //   ★ 글은 숫자라 ctype_digit 이면 끝인데, 작품 slug 는 글자다.
    //     그래서 '모양을 정해 두고 그 모양만' 통과시킨다 (영문 소문자·숫자·하이픈).
    public function rememberRecentWork(Request $request, string $slug): void
    {
        if (! preg_match('/^[a-z0-9-]{1,50}$/', $slug)) {
            return;
        }

        $list = array_values(array_unique(array_merge([$slug], $this->recentWorkSlugs($request))));

        $this->put($request, 'recent_works', implode(',', array_slice($list, 0, self::RECENT_MAX)));
    }

    public function recentWorkSlugs(Request $request): array
    {
        return collect(explode(',', (string) $request->cookie('recent_works')))
            ->filter(fn ($v) => (bool) preg_match('/^[a-z0-9-]{1,50}$/', $v))
            ->take(self::RECENT_MAX)
            ->values()->all();
    }

    // ── 최근 본 글 ──────────────────────────────────────────
    public function rememberRecentPost(Request $request, int $postId): void
    {
        $ids = array_values(array_unique(array_merge([$postId], $this->recentPostIds($request))));

        $this->put($request, 'recent_posts', implode(',', array_slice($ids, 0, self::RECENT_MAX)));
    }

    public function recentPostIds(Request $request): array
    {
        // ★ 쿠키에서 온 값은 남이 고칠 수 있다. 숫자만 통과시킨다.
        return collect(explode(',', (string) $request->cookie('recent_posts')))
            ->filter(fn ($v) => ctype_digit($v) && (int) $v > 0)
            ->map(fn ($v) => (int) $v)
            ->take(self::RECENT_MAX)
            ->values()->all();
    }

    // ── 최근 검색어 ─────────────────────────────────────────
    public function rememberSearch(Request $request, string $query): void
    {
        $query = trim($query);
        if ($query === '' || mb_strlen($query) > 50) {
            return;
        }

        $list = array_values(array_unique(array_merge([$query], $this->recentSearches($request))));

        $this->put($request, 'recent_search', json_encode(array_slice($list, 0, self::RECENT_MAX)));
    }

    public function recentSearches(Request $request): array
    {
        $raw = json_decode((string) $request->cookie('recent_search'), true);

        return collect(is_array($raw) ? $raw : [])
            ->filter(fn ($v) => is_string($v) && $v !== '' && mb_strlen($v) <= 50)
            ->take(self::RECENT_MAX)
            ->values()->all();
    }

    // ── 동의하지 않은 항목의 쿠키를 치운다 ──────────────────
    //   ★ 동의는 "앞으로 안 심겠다"가 아니라 "지금 것도 치우겠다"까지다.
    //     이게 없으면 [거절]을 눌러도 이미 쌓인 기록이 브라우저에 남는다.
    public function forgetUnconsented(Request $request): void
    {
        foreach (self::OWNED_BY as $cookie => $item) {
            if (! $this->consent->has($request, $item) && $request->cookie($cookie) !== null) {
                Cookie::queue(Cookie::forget($cookie));
            }
        }
    }

    // 동의가 있을 때만 담는다
    private function put(Request $request, string $cookie, string $value): void
    {
        if ($this->consent->has($request, self::OWNED_BY[$cookie])) {
            Cookie::queue($cookie, $value, self::DAYS * 24 * 60);
        }
    }
}
