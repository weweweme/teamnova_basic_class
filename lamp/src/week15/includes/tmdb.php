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

// ── TMDB에 GET 요청을 보내고 결과(배열)를 돌려주는 공통 함수 ──
//   $path  : '/search/movie' 같은 엔드포인트
//   $params: ['query' => '기생충', ...] 쿼리 파라미터
//   ★ v4 토큰을 'Authorization: Bearer ...' 헤더로 보낸다 (URL에 키를 노출하지 않음).
function tmdb_get(string $path, array $params = []): ?array {
    // 파라미터를 URL 쿼리문자열로 (한글·특수문자 자동 인코딩)
    $params['language'] = 'ko-KR';                       // 항상 한국어로
    $url = TMDB_API_BASE . $path . '?' . http_build_query($params);

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
    $poster = !empty($item['poster_path']) ? TMDB_IMAGE_BASE . $item['poster_path'] : '';

    return [
        'tmdb_id'    => $item['id'] ?? 0,
        'title'      => $title,
        'genre'      => $isMovie ? '영화' : '드라마',
        'year'       => $year,
        'overview'   => $item['overview'] ?? '',      // 줄거리
        'poster_url' => $poster,
    ];
}
