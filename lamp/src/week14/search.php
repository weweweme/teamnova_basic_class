<?php
// ============================================================
// search.php — 글 검색  [GET 요청]
//   ?q=검색어 & sort= & page=
//   ★ 검색을 GET으로 하는 이유: 결과 주소를 그대로 '공유·북마크'할 수 있어야 하니까.
//     (구글·네이버 검색도 주소에 ?q=... 가 붙는 이유가 바로 이것)
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/posts.php';

const SEARCH_PER_PAGE = 3;      // 한 페이지에 보여줄 검색 결과 수

// ── 1) 파라미터 받기 ─────────────────────────────────────────
$q    = trim($_GET['q'] ?? '');         // 검색어 (앞뒤 공백 제거)
$sort = $_GET['sort'] ?? 'new';
$page = (int)($_GET['page'] ?? 1);
if ($page < 1) {
    $page = 1;
}

// ── 2) 검색 → 정렬 → 페이지 자르기 ──────────────────────────
//   ★ posts 모듈에 만들어둔 함수들을 그대로 재사용! (board와 같은 부품)
$posts = search_posts(get_posts(), $q);
$posts = sort_posts($posts, $sort);

$totalCount = count($posts);
$totalPages = max(1, (int)ceil($totalCount / SEARCH_PER_PAGE));
if ($page > $totalPages) {
    $page = $totalPages;
}
$pagePosts = paginate_posts($posts, $page, SEARCH_PER_PAGE);

$sortTabs = ['new' => '최신', 'hot' => '인기', 'views' => '조회', 'comments' => '댓글'];

$pageTitle = $q === '' ? '검색' : "'{$q}' 검색결과";
require __DIR__ . '/includes/header.php';
?>

  <h1>검색</h1>

  <!-- ★ 검색 폼은 method="get" ! (글쓰기·댓글 폼이 POST였던 것과 대조)
       GET 폼은 제출하면 입력값이 '주소'에 자동으로 붙는다:
         입력 '배당' + 전송  →  /search.php?q=배당
       그래서 검색 결과 주소를 복사해 남에게 줄 수 있다.
       value="..." 로 방금 친 검색어를 칸에 그대로 남겨둔다(사용자 편의). -->
  <form class="search-form" method="get" action="/search.php">
    <input type="text" name="q" value="<?= e($q) ?>" placeholder="글 제목·내용 검색">
    <button type="submit">검색</button>
  </form>

  <?php if ($q === ''): ?>
    <p class="muted">검색어를 입력해 주세요.</p>
  <?php else: ?>

    <!-- 정렬 탭: 검색 결과에도 같은 정렬을 적용 (posts 모듈 재사용의 이점)
         page를 ''로 비워 1페이지부터 다시 보게 한다 -->
    <div class="sort-tabs">
      <?php foreach ($sortTabs as $key => $label): ?>
        <a class="<?= $sort === $key ? 'active' : '' ?>"
           href="<?= e(query_url('/search.php', ['sort' => $key, 'page' => ''])) ?>"><?= e($label) ?></a>
      <?php endforeach; ?>
    </div>

    <p class="muted">'<?= e($q) ?>' 검색결과 총 <?= $totalCount ?>개 · <?= $page ?>/<?= $totalPages ?> 페이지</p>

    <?php if (!$pagePosts): ?>
      <p class="muted">검색 결과가 없습니다.</p>
    <?php else: ?>
      <ul class="post-list">
        <?php foreach ($pagePosts as $p): ?>
          <li>
            <a href="/post/view.php?id=<?= e((string)$p['id']) ?>"><?= e($p['title']) ?></a>
            <span class="tag"><?= e($p['sentiment']) ?></span>
            <span class="post-stat">조회 <?= e((string)$p['views']) ?> · 댓글 <?= e((string)$p['comments']) ?></span>
          </li>
        <?php endforeach; ?>
      </ul>
    <?php endif; ?>

    <!-- 페이징 (board와 같은 구조. 목적지 경로만 /search.php) -->
    <?php if ($totalPages > 1): ?>
      <nav class="pagination">
        <?php if ($page > 1): ?>
          <a class="page-nav" href="<?= e(query_url('/search.php', ['page' => $page - 1])) ?>">← 이전</a>
        <?php else: ?>
          <span class="page-nav disabled">← 이전</span>
        <?php endif; ?>

        <div class="page-numbers">
          <?php for ($n = 1; $n <= $totalPages; $n++): ?>
            <a class="page-num <?= $n === $page ? 'active' : '' ?>"
               href="<?= e(query_url('/search.php', ['page' => $n])) ?>"><?= $n ?></a>
          <?php endfor; ?>
        </div>

        <?php if ($page < $totalPages): ?>
          <a class="page-nav" href="<?= e(query_url('/search.php', ['page' => $page + 1])) ?>">다음 →</a>
        <?php else: ?>
          <span class="page-nav disabled">다음 →</span>
        <?php endif; ?>
      </nav>
    <?php endif; ?>

  <?php endif; ?>

<?php require __DIR__ . '/includes/footer.php'; ?>
