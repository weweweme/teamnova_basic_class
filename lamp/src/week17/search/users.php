<?php
// ============================================================
// search/users.php — 유저 검색 결과 전체  [GET 요청]
//   ?q=영화&page=2 → 아이디·닉네임에 검색어가 든 회원을 페이지로 나눠 보여준다.
//   통합검색의 '👤 유저 더보기'가 여기로 온다. 누르면 그 사람 프로필로 간다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/search_ui.php';
require_once __DIR__ . '/../includes/users.php';

// ── 1) 검색어·페이지 받기 ────────────────────────────────────
$q        = create_search_query();
$page     = create_search_page();
$hasQuery = $q !== '';

// ── 2) 개수를 먼저 세고, 그 페이지 분량만 가져온다 (글 검색과 같은 흐름) ──
$total      = $hasQuery ? count_search_users($q) : 0;
$totalPages = max(1, (int) ceil($total / RESULTS_PER_PAGE));
if ($page > $totalPages) {
    $page = $totalPages;
}
$users = $hasQuery
    ? search_users($q, RESULTS_PER_PAGE, ($page - 1) * RESULTS_PER_PAGE)
    : [];

$pageTitle = $hasQuery ? "'{$q}' 유저 검색" : '유저 검색';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>🔍 통합검색</h1>

  <?php render_search_bar($q, 'users'); ?>

  <?php if (!$hasQuery): ?>
    <p class="muted">아이디나 닉네임으로 찾습니다. (예: 영화광)</p>

  <?php elseif ($total === 0): ?>
    <p class="muted">'<?= e($q) ?>'와 일치하는 회원이 없습니다.</p>

  <?php else: ?>
    <p class="muted">👤 유저 <?= $total ?>명 · <?= $page ?>/<?= $totalPages ?> 페이지</p>
    <?php render_user_results($users, $q); ?>
    <?php render_search_pagination('/search/users.php', $page, $totalPages); ?>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
