<?php
// ============================================================
// search.php — 작품 검색  [GET 요청]
//   ?q=기생  → 작품 제목·감독으로 찾아서 목록 표시 → 클릭하면 그 작품 게시판으로.
//
//   ★ 왜 '글 검색'이 아니라 '작품 검색'인가?
//     리뷰 커뮤니티에서 검색의 주인공은 '작품'이다.
//     "기생충" 검색 → 작품 찾기 → 그 작품 게시판으로 이동.
//     글 검색은 각 게시판 '안에서' 한다 (board/?work=..&q=..).
//     모든 작품의 글을 통째로 뒤지면 엉뚱한 작품 글이 섞여 쓸모가 없다.
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/works.php';   // 작품 데이터·검색 모듈

// ── 1) 검색어 받기 ───────────────────────────────────────────
$q = trim($_GET['q'] ?? '');

// ── 2) 작품 검색 (검색어 없으면 전체 작품) ───────────────────
$works = search_works(get_works(), $q);

$pageTitle = $q === '' ? '작품 검색' : "'{$q}' 검색결과";
require __DIR__ . '/includes/header.php';
?>

  <h1>작품 검색</h1>

  <!-- ★ 검색 폼은 method="get" (글쓰기·댓글 폼이 POST였던 것과 대조)
       GET 폼은 제출하면 입력값이 '주소'에 자동으로 붙는다:
         '기생' 입력 + 전송  →  /search.php?q=기생
       그래서 검색 결과 주소를 그대로 공유·북마크할 수 있다. -->
  <form class="search-form" method="get" action="/search.php">
    <input type="text" name="q" value="<?= e($q) ?>" placeholder="작품 제목 또는 감독 (예: 기생충, 놀란)">
    <button type="submit">검색</button>
  </form>

  <?php if ($q === ''): ?>
    <p class="muted">작품 제목이나 감독 이름으로 검색해 보세요. 아래는 전체 작품입니다.</p>
  <?php else: ?>
    <p class="muted">'<?= e($q) ?>' 검색결과 <?= count($works) ?>개</p>
  <?php endif; ?>

  <?php if (!$works): ?>
    <p class="muted">일치하는 작품이 없습니다.</p>
  <?php else: ?>
    <ul class="work-list">
      <?php foreach ($works as $w): ?>
        <li>
          <!-- 작품을 클릭하면 그 작품 게시판으로 (GET으로 work 전달) -->
          <a href="/board/?work=<?= e($w['slug']) ?>"><?= e($w['title']) ?></a>
          <span class="post-stat"><?= e($w['genre']) ?> · <?= e((string)$w['year']) ?> · <?= e($w['director']) ?></span>
        </li>
      <?php endforeach; ?>
    </ul>
  <?php endif; ?>

<?php require __DIR__ . '/includes/footer.php'; ?>
