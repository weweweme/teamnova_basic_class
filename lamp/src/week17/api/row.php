<?php
// ============================================================
// api/row.php — 홈의 TMDB 가로줄 데이터  [GET → JSON]
//   홈을 열 때 이 무거운 TMDB 목록을 '나중에' JS로 받아오기 위한 창구.
//   덕분에 홈 HTML은 우리 DB 것만 담아 즉시 뜨고, 포스터 줄은 뒤따라 채워진다.
//
//   요청 예:  /api/row.php?kind=movie
//   kind:     trending(인기작) | movie(인기 영화) | tv(인기 드라마)
//   응답:     { "items": [ {tmdb_id, title, poster_url}, ... ] }
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/tmdb.php';

header('Content-Type: application/json; charset=utf-8');

// 어떤 줄을 달라는지 (화이트리스트 — 이상한 값이면 인기작으로)
$kind = get_str('kind', 'trending');
switch ($kind) {
    case 'movie':
        $rows = tmdb_popular('movie');
        break;
    case 'tv':
        $rows = tmdb_popular('tv');
        break;
    case 'trending':
    default:
        $rows = tmdb_trending();
        break;
}

// 화면에 필요한 필드만 골라서 (원본은 필드가 많다)
$items = [];
foreach ($rows as $m) {
    $items[] = [
        'tmdb_id'    => $m['tmdb_id'],
        'title'      => $m['title'],
        'poster_url' => $m['poster_url'],
    ];
}

echo json_encode(['items' => $items], JSON_UNESCAPED_UNICODE);
