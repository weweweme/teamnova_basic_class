<?php
// ============================================================
// tmdb.php — TMDB(영화·드라마 API) 호출 모듈
//   외부 API에서 작품 데이터를 가져온다. (검색·상세)
//   ★ 우리 DB(media 표)와는 별개 — 여기선 'TMDB에서 읽어오기'만 담당.
//     가져온 걸 우리 DB에 저장하는 건 media 모듈(다음 단계)에서 한다.
// ============================================================

require_once __DIR__ . '/config.php';   // TMDB_TOKEN

// TMDB 기본 주소들 (매직값 금지 — 이름 붙인 상수로)
const TMDB_API_BASE    = 'https://api.themoviedb.org/3';
//   포스터 이미지 주소. TMDB는 poster_path(예: /abc.jpg)만 주므로 앞에 이 주소를 붙여야 실제 이미지가 됨.
//   w342 = 가로 342px 크기 (w92·w185·w500 등 선택 가능. 목록엔 342면 충분)
const TMDB_IMAGE_BASE  = 'https://image.tmdb.org/t/p/w342';
//   배경(backdrop) = 가로형 큰 이미지. 히어로 배너용. w1280 = 넓게.
const TMDB_BACKDROP_BASE = 'https://image.tmdb.org/t/p/w1280';

// ── TMDB에 GET 요청을 보내고 결과(배열)를 돌려주는 공통 함수 ──
//   $path  : '/search/movie' 같은 엔드포인트
//   $params: ['query' => '기생충', ...] 쿼리 파라미터
//   ★ v4 토큰을 'Authorization: Bearer ...' 헤더로 보낸다 (URL에 키를 노출하지 않음).
const TMDB_CACHE_DIR = __DIR__ . '/../cache/tmdb';
const TMDB_CACHE_TTL = 1800;   // 캐시 유효시간(초) = 30분

function tmdb_get(string $path, array $params = []): ?array {
    // 파라미터를 URL 쿼리문자열로 (한글·특수문자 자동 인코딩)
    $params['language'] = 'ko-KR';                       // 항상 한국어로
    $url = TMDB_API_BASE . $path . '?' . http_build_query($params);

    // ── 캐시 확인 ────────────────────────────────────────────
    //   [문제] 인기작 목록은 자주 안 바뀌는데, 홈을 열 때마다 TMDB를 여러 번 부르면 느리다.
    //   [해결] 같은 요청 결과를 파일에 잠깐(30분) 저장해두고 재사용한다.
    //     URL을 md5로 짧게 줄여 파일명으로. (같은 요청 = 같은 파일명)
    $cacheFile = TMDB_CACHE_DIR . '/' . md5($url) . '.json';
    //   파일이 있고 30분이 안 지났으면 → TMDB 안 부르고 그걸 그대로 쓴다.
    if (is_file($cacheFile) && (time() - filemtime($cacheFile)) < TMDB_CACHE_TTL) {
        $cached = file_get_contents($cacheFile);
        $data   = json_decode($cached, true);
        if (is_array($data)) {
            return $data;
        }
    }

    // 요청에 헤더를 실어 보내기 위한 '설정 봉투'(context)
    //   file_get_contents는 기본이 GET. 헤더만 얹으면 된다.
    $context = stream_context_create([
        'http' => [
            'method'  => 'GET',
            'header'  => "Authorization: Bearer " . TMDB_TOKEN . "\r\n"
                       . "Accept: application/json\r\n",
            'timeout' => 10,   // 10초 안에 응답 없으면 포기 (API가 느릴 때 페이지가 멈추지 않게)
        ],
    ]);

    // 실제 호출. 실패하면 false가 오므로 확인 후 처리 (Tester-Doer)
    $body = @file_get_contents($url, false, $context);
    if ($body === false) {
        return null;                                    // 네트워크 실패 등 → null
    }

    // 다음을 위해 캐시에 저장 (성공한 응답만)
    @file_put_contents($cacheFile, $body);

    // JSON 문자열 → PHP 배열
    $data = json_decode($body, true);
    return is_array($data) ? $data : null;
}

// ── 작품 검색: 제목으로 찾기 ────────────────────────────────
//   화면에 뿌리기 좋게 '우리가 필요한 필드만' 골라 새 배열로 만들어 돌려준다.
//   (이름을 search_…로 시작 — 매번 새 배열을 만들어 반환하므로)
function search_tmdb(string $query): array {
    $query = trim($query);
    if ($query === '') {
        return [];                                       // 검색어 없으면 빈 목록
    }

    $data = tmdb_get('/search/multi', ['query' => $query]);   // multi = 영화+드라마 함께
    if ($data === null || empty($data['results'])) {
        return [];
    }

    // TMDB 원본은 필드가 아주 많다 → 우리가 쓸 것만 추린다.
    $result = [];
    foreach ($data['results'] as $item) {
        // multi 검색은 사람(person)도 섞여 오므로, 영화·드라마만 남긴다
        $type = $item['media_type'] ?? '';
        if ($type !== 'movie' && $type !== 'tv') {
            continue;
        }
        $result[] = build_media_from_tmdb($item);
    }
    return $result;
}

// 한 줄에 몇 페이지까지 이어붙일지 (TMDB 1페이지 = 20개). 3이면 약 60개.
const TMDB_PAGES = 3;

// ── 이번 주 인기작 (영화+드라마 섞여서) ────────────────────
//   TMDB가 전 세계 데이터로 집계한 '요즘 뜨는' 작품들. 매주 바뀐다.
function tmdb_trending(): array {
    return tmdb_list('/trending/all/week');
}

// ── 인기 영화 / 인기 드라마 ─────────────────────────────────
//   $type: 'movie'(영화) | 'tv'(드라마). TMDB의 누적 인기도(popularity) 순.
function tmdb_popular(string $type): array {
    return tmdb_list("/$type/popular", $type);
}

// ── 목록 엔드포인트 공통 처리 → 우리 형식 배열로 ────────────
//   여러 페이지(TMDB_PAGES)를 이어붙여 '길게' 만든다 → 오래 스크롤 가능.
//   $forceType: 결과에 media_type이 없을 때(popular은 안 줌) 강제로 지정.
function tmdb_list(string $path, string $forceType = ''): array {
    $result = [];
    $seen   = [];   // 같은 작품이 여러 페이지에 겹쳐 나올 때 중복 제거용

    for ($page = 1; $page <= TMDB_PAGES; $page++) {
        $data = tmdb_get($path, ['page' => $page]);
        if ($data === null || empty($data['results'])) {
            break;                        // 더 없으면 멈춘다
        }
        foreach ($data['results'] as $item) {
            // trending은 media_type을 주지만 person(인물)도 섞임 → 영화·드라마만.
            // popular은 media_type이 없으므로 $forceType으로 채운다.
            $type = $item['media_type'] ?? $forceType;
            if ($type !== 'movie' && $type !== 'tv') {
                continue;
            }
            $id = $item['id'] ?? 0;
            if (isset($seen[$id])) {
                continue;                 // 이미 담은 작품이면 건너뜀
            }
            $seen[$id] = true;

            $item['media_type'] = $type;
            $m = build_media_from_tmdb($item);
            if ($m['poster_url'] !== '') {  // 포스터 없는 건 뺀다 (화면 빈칸 방지)
                $result[] = $m;
            }
        }
    }
    return $result;
}

// ── 장르 매핑 (우리 이름 → TMDB 코드) ──────────────────────
//   ★ 영화와 드라마는 같은 장르라도 코드가 다르다 (SF: 영화878 / 드라마10765).
//     그래서 [영화코드, 드라마코드]로 짝지어 둔다. null = 그쪽엔 해당 장르 없음.
//   화면 탭에 쓸 '흔한 장르'만 골랐다.
function tmdb_genres(): array {
    return [
        // 우리이름     => [movie, tv]
        '액션'    => [28,   10759],   // 드라마는 'Action & Adventure'
        'SF'      => [878,  10765],   // 드라마는 'Sci-Fi & Fantasy'
        '코미디'  => [35,   35],
        '드라마'  => [18,   18],
        '로맨스'  => [10749, null],   // 드라마엔 별도 로맨스 코드 없음 → 영화만
        '스릴러'  => [53,   null],
        '미스터리'=> [9648, 9648],
        '공포'    => [27,   null],
        '범죄'    => [80,   80],
        '다큐'    => [99,   99],
    ];
}

// ── 장르로 작품 둘러보기 (discover) ─────────────────────────
//   $genre: 우리 장르 이름 (tmdb_genres의 키). ''이면 인기작 전체.
//   $media: 'movie' | 'tv' | 'all'(둘 다 합침)
//   $page : 페이지 (무한 스크롤에서 1,2,3… 늘려가며 부름)
const TMDB_ANIME_GENRE = 16;   // 애니메이션 장르 코드 (영화·드라마 공통)

function discover_by_genre(string $genre, string $media, int $page = 1): array {
    $map = tmdb_genres();

    // ── '애니' 타입: 애니메이션 장르(16)를 강제로 걸고 영화+드라마를 합친다 ──
    //   타입 필터지만 실제로는 '장르16' 필터. 추가 장르가 선택됐으면 함께 건다(AND).
    //   (예: 애니 + SF → 장르 16 AND 878)
    if ($media === 'anime') {
        $extra = ($genre !== '' && isset($map[$genre])) ? $map[$genre] : [null, null];
        $result = [];
        // 애니 영화 (장르16 [+ 추가장르])
        $movieGenres = TMDB_ANIME_GENRE . ($extra[0] !== null ? ',' . $extra[0] : '');
        $result = array_merge($result,
            tmdb_list_page('/discover/movie', 'movie', $page, ['with_genres' => $movieGenres]));
        // 애니 드라마
        $tvGenres = TMDB_ANIME_GENRE . ($extra[1] !== null ? ',' . $extra[1] : '');
        $result = array_merge($result,
            tmdb_list_page('/discover/tv', 'tv', $page, ['with_genres' => $tvGenres]));
        return $result;
    }

    // '전체 장르'면 그냥 인기작 (장르 필터 없음)
    if ($genre === '' || !isset($map[$genre])) {
        if ($media === 'tv')  return tmdb_list_page('/tv/popular',    'tv',    $page);
        if ($media === 'movie') return tmdb_list_page('/movie/popular', 'movie', $page);
        // all: 영화+드라마 인기작 합침
        return array_merge(
            tmdb_list_page('/movie/popular', 'movie', $page),
            tmdb_list_page('/tv/popular',    'tv',    $page)
        );
    }

    [$movieCode, $tvCode] = $map[$genre];
    $result = [];
    // 영화 쪽
    if ($media !== 'tv' && $movieCode !== null) {
        $result = array_merge($result,
            tmdb_list_page('/discover/movie', 'movie', $page, ['with_genres' => $movieCode]));
    }
    // 드라마 쪽
    if ($media !== 'movie' && $tvCode !== null) {
        $result = array_merge($result,
            tmdb_list_page('/discover/tv', 'tv', $page, ['with_genres' => $tvCode]));
    }
    return $result;
}

// ── 한 페이지만 가져오기 (tmdb_list는 여러 페이지, 이건 딱 한 페이지) ──
//   무한 스크롤에서 '다음 한 페이지'만 받아올 때 쓴다.
function tmdb_list_page(string $path, string $forceType, int $page, array $params = []): array {
    $params['page'] = $page;
    $params['sort_by'] = $params['sort_by'] ?? 'popularity.desc';   // discover는 정렬 지정
    $data = tmdb_get($path, $params);
    if ($data === null || empty($data['results'])) {
        return [];
    }
    $result = [];
    foreach ($data['results'] as $item) {
        $type = $item['media_type'] ?? $forceType;
        if ($type !== 'movie' && $type !== 'tv') {
            continue;
        }
        $item['media_type'] = $type;
        $m = build_media_from_tmdb($item);
        if ($m['poster_url'] !== '') {
            $result[] = $m;
        }
    }
    return $result;
}

// ── tmdb_id로 작품 하나 상세 조회 ──────────────────────────
//   슬러그(tmdb-496243)만 있고 우리 DB에 아직 없을 때, TMDB에서 정보를 가져온다.
//   ★ 슬러그엔 영화/드라마 구분이 없다 → /movie/{id} 먼저 시도, 없으면 /tv/{id}.
//     (TMDB는 영화와 드라마가 번호 체계가 달라서 각각 조회해야 한다)
function tmdb_find_by_id(int $tmdbId): ?array {
    // ① 영화로 시도
    $movie = tmdb_get("/movie/$tmdbId");
    if ($movie !== null && !empty($movie['id'])) {
        $movie['media_type'] = 'movie';
        return build_media_from_tmdb($movie);
    }
    // ② 없으면 드라마로 시도
    $tv = tmdb_get("/tv/$tmdbId");
    if ($tv !== null && !empty($tv['id'])) {
        $tv['media_type'] = 'tv';
        return build_media_from_tmdb($tv);
    }
    return null;                          // 둘 다 없으면 진짜 없는 것
}

// ── TMDB 원본 한 건 → 우리 형식으로 변환 ───────────────────
//   영화(movie)와 드라마(tv)는 필드 이름이 다르다:
//     영화: title, release_date  /  드라마: name, first_air_date
//   그 차이를 여기서 흡수해 '우리 형식'으로 통일한다.
function build_media_from_tmdb(array $item): array {
    $isMovie = ($item['media_type'] ?? 'movie') === 'movie';

    $title = $isMovie ? ($item['title'] ?? '') : ($item['name'] ?? '');
    $date  = $isMovie ? ($item['release_date'] ?? '') : ($item['first_air_date'] ?? '');
    $year  = $date !== '' ? (int) substr($date, 0, 4) : null;   // '2019-05-30' → 2019

    // 포스터: TMDB는 '/abc.jpg' 조각만 주므로 앞에 이미지 주소를 붙인다. 없으면 빈 문자열.
    $poster   = !empty($item['poster_path'])   ? TMDB_IMAGE_BASE . $item['poster_path']     : '';
    // 배경(가로 큰 이미지) — 히어로 배너용. 없으면 빈 문자열.
    $backdrop = !empty($item['backdrop_path']) ? TMDB_BACKDROP_BASE . $item['backdrop_path'] : '';

    return [
        'tmdb_id'      => $item['id'] ?? 0,
        'title'        => $title,
        'genre'        => $isMovie ? '영화' : '드라마',
        'year'         => $year,
        'overview'     => $item['overview'] ?? '',      // 줄거리
        'poster_url'   => $poster,
        'backdrop_url' => $backdrop,
    ];
}
