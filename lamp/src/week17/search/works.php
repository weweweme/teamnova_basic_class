<?php
// ============================================================
// search/works.php — 작품 검색 결과 전체  [GET 요청]
//   ?q=기생충&page=2 → TMDB 실시간 검색 결과를 페이지로 나눠 보여준다.
//   통합검색의 '🎬 작품 더보기'가 여기로 온다.
//
//   ★ 다른 두 화면(글·유저)과 다른 점: 여기만 자르는 일을 PHP가 한다.
//     TMDB는 우리 DB가 아니라 남의 서버라 LIMIT/OFFSET을 시킬 수 없다.
//     받아온 목록을 array_slice로 자른다. (한 번 받은 결과는 30분 캐시)
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/search_ui.php';
require_once __DIR__ . '/../includes/tmdb.php';

// ── 1) 검색어·페이지 받기 ────────────────────────────────────
$q        = create_search_query();
$page     = create_search_page();
$hasQuery = $q !== '';

// ── 2) 검색 → 페이지 계산 → 그 페이지 분량만 자르기 ──────────
//   ★ 여기서만 TMDB_PAGES(3페이지 ≒ 60개)까지 받아온다. 통합검색은 3개만 쓰므로 1페이지면 충분하지만,
//     '작품 전체' 화면은 다 보여주는 곳이라 넉넉히 받아야 페이지를 넘길 거리가 생긴다.
$works      = $hasQuery ? search_tmdb($q, TMDB_PAGES) : [];
$total      = count($works);
$totalPages = max(1, (int) ceil($total / RESULTS_PER_PAGE));
if ($page > $totalPages) {          // 범위를 넘으면 마지막 페이지로
    $page = $totalPages;
}
$pageWorks = array_slice($works, ($page - 1) * RESULTS_PER_PAGE, RESULTS_PER_PAGE);

$pageTitle = $hasQuery ? "'{$q}' 작품 검색" : '작품 검색';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>🔍 통합검색</h1>

  <?php render_search_bar($q, 'works'); ?>

  <?php if (!$hasQuery): ?>
    <p class="muted">영화·드라마 제목으로 검색해 보세요. (예: 기생충, 인셉션)</p>

  <?php elseif ($total === 0): ?>
    <p class="muted">'<?= e($q) ?>'와 일치하는 작품이 없습니다.</p>

  <?php else: ?>
    <p class="muted">🎬 작품 <?= $total ?>개 · <?= $page ?>/<?= $totalPages ?> 페이지</p>
    <?php render_work_results($pageWorks, $q); ?>
    <?php render_search_pagination('/search/works.php', $page, $totalPages); ?>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
