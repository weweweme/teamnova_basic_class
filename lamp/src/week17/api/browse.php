<?php
// ============================================================
// api/browse.php — 작품 둘러보기 '다음 페이지' 데이터  [GET → JSON]
//   ★ 지금까지 페이지들은 화면(HTML)을 돌려줬지만, 이 파일은 '데이터(JSON)'만 준다.
//     무한 스크롤에서 JS가 fetch로 불러 화면에 이어붙이는 용도.
//     (실무의 AJAX/API 방식 — 페이지 이동 없이 데이터만 주고받는다)
//
//   요청 예:  /api/browse.php?genre=SF&media=movie&page=2
//   응답:     { "items": [ {tmdb_id, title, poster_url, year}, ... ] }
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/tmdb.php';

// 응답이 'JSON 데이터'임을 브라우저에 알린다 (HTML이 아니라).
header('Content-Type: application/json; charset=utf-8');

// ── 파라미터 받기 (works.php와 같은 규칙) ────────────────────
$genre = get_str('genre', '');
$media = get_str('media', 'all');
if (!in_array($media, ['all', 'movie', 'tv', 'anime'], true)) {
    $media = 'all';
}
$page = get_int('page', 1);
if ($page < 1)   { $page = 1; }
if ($page > 20)  { $page = 20; }   // TMDB·부하 보호 (너무 깊이 안 감)

// ── 그 페이지 작품 가져와서, 화면에 필요한 필드만 골라 JSON으로 ──
$works = discover_by_genre($genre, $media, $page);

$items = [];
foreach ($works as $w) {
    $items[] = [
        'tmdb_id'    => $w['tmdb_id'],
        'title'      => $w['title'],
        'year'       => $w['year'],
        'poster_url' => $w['poster_url'],
    ];
}

// 배열 → JSON 문자열. JSON_UNESCAPED_UNICODE = 한글을 \uXXXX 안 하고 그대로.
echo json_encode(['items' => $items], JSON_UNESCAPED_UNICODE);
