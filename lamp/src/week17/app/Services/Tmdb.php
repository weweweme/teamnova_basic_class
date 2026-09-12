<?php

namespace App\Services;

use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\Http;

// ============================================================
// Tmdb — 영화·드라마 정보 API 호출
//   지금 includes/tmdb.php(403줄) 가 하던 일 중 '검색'만 먼저 옮긴다.
//
//   ★ 우리 환경에는 cURL 확장이 없지만 호출이 된다.
//     HTTP 클라이언트가 cURL 이 없으면 PHP 스트림으로 대신 보내기 때문이다.
//     (직접 확인: /3/movie/496243 → HTTP 200 "Parasite")
// ============================================================
class Tmdb
{
    // 같은 검색어를 30분 동안은 다시 물어보지 않는다.
    //   ★ 지금은 cache/tmdb 폴더에 파일로 직접 저장하고 filemtime 으로 나이를 쟀다.
    //     Cache::remember 는 '없으면 만들어 넣고, 있으면 그대로 준다'를 한 줄로 한다.
    private const TTL_SECONDS = 1800;

    // ── 인기작 (홈 화면의 포스터 줄) ────────────────────────
    //   ★ 사람마다 다르지 않은 값이라 한 번 받아 두면 모두가 같이 쓴다.
    //     그래서 캐시 키에 사용자 정보가 들어가지 않는다.
    public function trending(int $limit = 12): array
    {
        return Cache::remember('tmdb:trending', self::TTL_SECONDS, function () use ($limit) {
            $res = Http::withToken(config('services.tmdb.token'))
                ->timeout(5)
                ->get('https://api.themoviedb.org/3/trending/movie/week', ['language' => 'ko-KR']);

            if ($res->failed()) {
                return [];   // 외부 서비스가 죽어도 홈 화면은 떠야 한다
            }

            return $this->shape($res->json('results', []), $limit);
        });
    }

    public function searchMovies(string $query, int $limit = 10): array
    {
        $query = trim($query);
        if ($query === '') {
            return [];
        }

        return Cache::remember("tmdb:search:{$query}", self::TTL_SECONDS, function () use ($query, $limit) {
            $res = Http::withToken(config('services.tmdb.token'))
                ->timeout(5)
                ->get('https://api.themoviedb.org/3/search/movie', [
                    'query'    => $query,
                    'language' => 'ko-KR',
                ]);

            // 외부 서비스가 죽어도 우리 화면은 떠야 한다 → 실패하면 빈 목록
            if ($res->failed()) {
                return [];
            }

            return $this->shape($res->json('results', []), $limit);
        });
    }

    // TMDB 응답을 우리가 쓰는 모양으로 바꾼다 (검색·인기작이 함께 쓴다)
    private function shape(array $results, int $limit): array
    {
        return collect($results)
            ->take($limit)
            ->map(fn ($m) => [
                'tmdb_id'    => $m['id'],
                'slug'       => 'tmdb-' . $m['id'],
                'title'      => $m['title'] ?? '(제목 없음)',
                'year'       => isset($m['release_date']) ? (int) substr($m['release_date'], 0, 4) : null,
                'poster_url' => isset($m['poster_path']) && $m['poster_path']
                    ? 'https://image.tmdb.org/t/p/w185' . $m['poster_path']
                    : null,
            ])
            ->all();
    }
}
