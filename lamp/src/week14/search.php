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
$q = mb_substr(trim(get_str('q')), 0, 50);

// ── 2) 검색어가 있을 때만 찾는다 ─────────────────────────────
//   ★ 검색어가 없으면 아무것도 안 보여준다.
//     전체 목록은 '작품' 메뉴(works.php)의 몫 — 두 페이지가 같은 걸 보여주면
//     메뉴가 나뉘어 있을 이유가 없다. (각 화면은 한 가지 일만)
//   '검색 전'과 '검색했는데 결과 없음'은 서로 다른 상태라서, 아래 화면에서도 구분해 안내한다.
$hasQuery = $q !== '';
$works    = $hasQuery ? search_works(get_works(), $q) : [];

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

  <?php // 상태가 셋이라 3갈래로 안내한다: ① 검색 전 ② 결과 없음 ③ 결과 있음 ?>
  <?php if (!$hasQuery): ?>
    <p class="muted">작품 제목이나 감독 이름으로 검색해 보세요. (예: 기생충, 놀란)</p>
    <p class="muted">전체 작품은 <a href="/works.php">작품 목록</a>에서 볼 수 있어요.</p>

  <?php elseif (!$works): ?>
    <p class="muted">'<?= e($q) ?>'와 일치하는 작품이 없습니다.</p>
    <p class="muted"><a href="/works.php">전체 작품 보기</a></p>

  <?php else: ?>
    <p class="muted">'<?= e($q) ?>' 검색결과 <?= count($works) ?>개</p>
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
