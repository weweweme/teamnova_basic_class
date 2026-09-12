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

    // ── 작품 하나의 배경 이미지 (홈 히어로용) ───────────────
    //   포스터(세로)와 달리 backdrop 은 가로 이미지라 큰 배너에 쓴다.
    public function backdropFor(int $tmdbId): ?string
    {
        return Cache::remember("tmdb:backdrop:{$tmdbId}", self::TTL_SECONDS, function () use ($tmdbId) {
            $res = Http::withToken(config('services.tmdb.token'))
                ->timeout(5)
                ->get("https://api.themoviedb.org/3/movie/{$tmdbId}", ['language' => 'ko-KR']);

            $path = $res->successful() ? $res->json('backdrop_path') : null;

            return $path ? 'https://image.tmdb.org/t/p/w1280' . $path : null;
        });
    }

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

    // ── 장르 이름 → TMDB 장르 코드 [영화, 드라마] ──────────
    //   드라마 쪽에 같은 장르가 없으면 null 이다 (예: 로맨스·스릴러·공포).
    public const GENRES = [
        '액션'     => [28,    10759],   // 드라마는 'Action & Adventure'
        'SF'       => [878,   10765],   // 드라마는 'Sci-Fi & Fantasy'
        '코미디'   => [35,    35],
        '드라마'   => [18,    18],
        '로맨스'   => [10749, null],
        '스릴러'   => [53,    null],
        '미스터리' => [9648,  9648],
        '공포'     => [27,    null],
        '범죄'     => [80,    80],
        '다큐'     => [99,    99],
    ];

    // 애니메이션 장르 코드 (영화·드라마 공통)
    private const ANIME_GENRE = 16;

    // 가로줄 하나를 만들 때 이어붙이는 TMDB 페이지 수 → 길게 스크롤할 수 있다
    private const ROW_PAGES = 3;

    // ── 홈 가로줄: 이번 주 인기작 / 인기 영화 / 인기 드라마 ──
    //   $kind: trending | movie | tv
    public function row(string $kind): array
    {
        return Cache::remember("tmdb:row:{$kind}", self::TTL_SECONDS, function () use ($kind) {
            [$path, $forceType] = match ($kind) {
                'movie' => ['/movie/popular', 'movie'],
                'tv'    => ['/tv/popular',    'tv'],
                default => ['/trending/all/week', ''],
            };

            $items = [];
            $seen  = [];   // 같은 작품이 여러 페이지에 겹쳐 나올 때 중복 제거용

            for ($page = 1; $page <= self::ROW_PAGES; $page++) {
                $results = $this->get($path, ['page' => $page]);
                if (! $results) {
                    break;                 // 더 없으면 멈춘다
                }
                foreach ($this->shape($results, 100, $forceType) as $item) {
                    if (isset($seen[$item['tmdb_id']])) {
                        continue;
                    }
                    $seen[$item['tmdb_id']] = true;
                    $items[] = $item;
                }
            }

            return $items;
        });
    }

    // ── 작품 둘러보기: 장르·매체로 추려서 한 페이지씩 ───────
    //   $media: all | movie | tv | anime
    public function discover(string $genre, string $media, int $page): array
    {
        $key = "tmdb:discover:{$genre}:{$media}:{$page}";

        return Cache::remember($key, self::TTL_SECONDS, function () use ($genre, $media, $page) {
            // 애니메이션은 '매체'가 아니라 장르(16)라서 따로 다룬다.
            //   고른 장르가 또 있으면 두 코드를 함께 건다 (애니 + 액션 처럼).
            if ($media === 'anime') {
                [$movieExtra, $tvExtra] = self::GENRES[$genre] ?? [null, null];

                return array_merge(
                    $this->page('/discover/movie', 'movie', $page, ['with_genres' => self::ANIME_GENRE . ($movieExtra ? ',' . $movieExtra : '')]),
                    $this->page('/discover/tv',    'tv',    $page, ['with_genres' => self::ANIME_GENRE . ($tvExtra    ? ',' . $tvExtra    : '')]),
                );
            }

            // 장르를 안 골랐으면 그냥 인기순
            if (! isset(self::GENRES[$genre])) {
                return match ($media) {
                    'tv'    => $this->page('/tv/popular',    'tv',    $page),
                    'movie' => $this->page('/movie/popular', 'movie', $page),
                    default => array_merge(
                        $this->page('/movie/popular', 'movie', $page),
                        $this->page('/tv/popular',    'tv',    $page),
                    ),
                };
            }

            [$movieCode, $tvCode] = self::GENRES[$genre];
            $items = [];

            if ($media !== 'tv' && $movieCode !== null) {
                $items = array_merge($items, $this->page('/discover/movie', 'movie', $page, ['with_genres' => $movieCode]));
            }
            if ($media !== 'movie' && $tvCode !== null) {
                $items = array_merge($items, $this->page('/discover/tv', 'tv', $page, ['with_genres' => $tvCode]));
            }

            return $items;
        });
    }

    // ── 우리 표에 없는 작품 한 편 (tmdb-123 → 작품 정보) ────
    //   ★ 아직 아무도 글을 쓰지 않은 작품은 media 표에 없다.
    //     그래도 게시판은 열려야 하므로 TMDB 에서 직접 가져온다.
    //     영화로 먼저 물어보고 없으면 드라마로 물어본다.
    public function findById(int $tmdbId): ?array
    {
        return Cache::remember("tmdb:find:{$tmdbId}", self::TTL_SECONDS, function () use ($tmdbId) {
            foreach (['movie' => true, 'tv' => false] as $type => $isMovie) {
                $item = $this->raw("/{$type}/{$tmdbId}");
                if (! $item || empty($item['id'])) {
                    continue;
                }

                $date = $isMovie ? ($item['release_date'] ?? '') : ($item['first_air_date'] ?? '');

                return [
                    'tmdb_id'    => $item['id'],
                    'slug'       => 'tmdb-' . $item['id'],
                    'title'      => ($isMovie ? $item['title'] : $item['name']) ?? '(제목 없음)',
                    'genre'      => $isMovie ? '영화' : '드라마',
                    'year'       => $date ? (int) substr($date, 0, 4) : null,
                    'overview'   => $item['overview'] ?? '',
                    'poster_url' => ! empty($item['poster_path'])
                        ? 'https://image.tmdb.org/t/p/w342' . $item['poster_path']
                        : null,
                ];
            }

            return null;                   // 둘 다 없으면 진짜 없는 작품
        });
    }

    // ── 작품 한 편의 상세 (감독·출연·예고편·분량) ───────────
    //   ★ 작품 게시판 위쪽에 쓴다. 영화로 먼저 물어보고 없으면 드라마로 물어본다.
    public function detail(int $tmdbId): ?array
    {
        return Cache::remember("tmdb:detail:{$tmdbId}", self::TTL_SECONDS, function () use ($tmdbId) {
            $params = ['append_to_response' => 'credits,videos'];

            foreach (['movie', 'tv'] as $type) {
                $item = $this->raw("/{$type}/{$tmdbId}", $params);
                if ($item && ! empty($item['id'])) {
                    return $this->shapeDetail($item, $type);
                }
            }

            return null;                   // 둘 다 없으면 진짜 없는 것
        });
    }

    // 상세 응답 → 화면이 쓰는 모양으로.
    //   ★ 영화와 드라마는 '감독'을 담는 자리가 다르다:
    //     영화는 credits.crew 중 job 이 'Director' 인 사람,
    //     드라마는 created_by(기획자) — 감독이 회차마다 달라 crew 에 안 들어간다.
    private function shapeDetail(array $item, string $type): array
    {
        $creditName = $type === 'movie'
            ? (collect($item['credits']['crew'] ?? [])->firstWhere('job', 'Director')['name'] ?? '')
            : ($item['created_by'][0]['name'] ?? '');

        // 출연진 상위 5명 이름만 (credits.cast 는 비중 순으로 정렬돼 온다)
        $cast = collect($item['credits']['cast'] ?? [])->take(5)->pluck('name')->filter()->values()->all();

        // 예고편: 유튜브 영상 중 'Trailer' 를 우선, 없으면 아무 유튜브 영상
        $videos  = collect($item['videos']['results'] ?? [])->where('site', 'YouTube');
        $trailer = $videos->firstWhere('type', 'Trailer') ?? $videos->first();

        // 분량: 영화는 분, 드라마는 시즌 수
        $runtime = $type === 'movie'
            ? (($item['runtime'] ?? 0) > 0 ? $item['runtime'] . '분' : '')
            : (($item['number_of_seasons'] ?? 0) > 0 ? '시즌 ' . $item['number_of_seasons'] : '');

        return [
            'creditLabel' => $type === 'movie' ? '감독' : '제작',
            'creditName'  => $creditName,
            'cast'        => $cast,
            'trailerKey'  => $trailer['key'] ?? '',
            'runtimeText' => $runtime,
        ];
    }

    // discover/popular 한 페이지를 우리 형식으로
    private function page(string $path, string $forceType, int $page, array $params = []): array
    {
        $params += ['sort_by' => 'popularity.desc'];
        $params['page'] = $page;

        return $this->shape($this->get($path, $params), 100, $forceType);
    }

    // TMDB 를 한 번 불러 results 만 꺼낸다 (실패하면 빈 배열)
    private function get(string $path, array $params = []): array
    {
        return $this->raw($path, $params)['results'] ?? [];
    }

    // TMDB 를 한 번 불러 응답 전체를 준다 (실패하면 null)
    //   ★ 외부 서비스가 죽어도 우리 화면은 떠야 한다 → 예외 대신 null 을 돌려준다.
    private function raw(string $path, array $params = []): ?array
    {
        $res = Http::withToken(config('services.tmdb.token'))
            ->timeout(5)
            ->get('https://api.themoviedb.org/3' . $path, $params + ['language' => 'ko-KR']);

        return $res->successful() ? $res->json() : null;
    }

    // TMDB 응답을 우리가 쓰는 모양으로 바꾼다 (검색·인기작·둘러보기가 함께 쓴다)
    //   ★ 영화는 title/release_date, 드라마는 name/first_air_date 로 칸 이름이 다르다.
    //     $forceType — popular 응답에는 media_type 이 없어서 밖에서 알려 준다.
    //     trending 응답에는 인물(person)도 섞여 오므로 영화·드라마만 남긴다.
    private function shape(array $results, int $limit, string $forceType = 'movie'): array
    {
        return collect($results)
            ->map(function ($m) use ($forceType) {
                $type = $m['media_type'] ?? $forceType;
                if ($type !== 'movie' && $type !== 'tv') {
                    return null;
                }

                $date = $type === 'movie' ? ($m['release_date'] ?? '') : ($m['first_air_date'] ?? '');

                return [
                    'tmdb_id'    => $m['id'],
                    'slug'       => 'tmdb-' . $m['id'],
                    'title'      => $m['title'] ?? $m['name'] ?? '(제목 없음)',
                    'year'       => $date ? (int) substr($date, 0, 4) : null,
                    // 포스터 없는 건 뺀다 (화면에 빈칸이 생긴다)
                    'poster_url' => ! empty($m['poster_path'])
                        ? 'https://image.tmdb.org/t/p/w185' . $m['poster_path']
                        : null,
                ];
            })
            ->filter(fn ($m) => $m !== null && $m['poster_url'] !== null)
            ->take($limit)
            ->values()
            ->all();
    }
}
