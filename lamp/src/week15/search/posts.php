<?php
// ============================================================
// search/posts.php — 글 검색 결과 전체  [GET 요청]
//   ?q=인생&page=2 → 제목·내용에 검색어가 든 글을 페이지로 나눠 보여준다.
//   통합검색의 '📝 글 더보기'가 여기로 온다.
//
//   ★ 자르는 일은 DB가 한다(LIMIT/OFFSET). 글이 아무리 많아도
//     서버가 한 번에 들고 있는 건 이 페이지에 그릴 20개뿐이다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/search_ui.php';
require_once __DIR__ . '/../includes/posts.php';

// ── 1) 검색어·페이지 받기 ────────────────────────────────────
$q        = create_search_query();
$page     = create_search_page();
$hasQuery = $q !== '';

// ── 2) 개수를 먼저 세고, 그 페이지 분량만 가져온다 ───────────
//   ★ 순서가 중요하다. 총 개수를 알아야 마지막 페이지 번호가 나오고,
//     그게 정해져야 "몇 번째부터(OFFSET) 가져올지"를 정할 수 있다.
$total      = $hasQuery ? count_search_posts($q) : 0;
$totalPages = max(1, (int) ceil($total / RESULTS_PER_PAGE));
if ($page > $totalPages) {
    $page = $totalPages;
}
$posts = $hasQuery
    ? search_posts_db($q, RESULTS_PER_PAGE, ($page - 1) * RESULTS_PER_PAGE)
    : [];

$pageTitle = $hasQuery ? "'{$q}' 글 검색" : '글 검색';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>🔍 통합검색</h1>

  <?php render_search_bar($q, 'posts'); ?>

  <?php if (!$hasQuery): ?>
    <p class="muted">글 제목과 내용에서 찾습니다. (예: 인생 영화, 결말)</p>

  <?php elseif ($total === 0): ?>
    <p class="muted">'<?= e($q) ?>'가 들어간 글이 없습니다.</p>

  <?php else: ?>
    <p class="muted">📝 글 <?= $total ?>개 · <?= $page ?>/<?= $totalPages ?> 페이지</p>
    <?php render_post_results($posts, $q); ?>
    <?php render_search_pagination('/search/posts.php', $page, $totalPages); ?>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
