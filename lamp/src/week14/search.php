<?php
// ============================================================
// search.php — 종목 검색  [GET 요청]
//   ?q=삼성  → 종목명·코드로 찾아서 목록 표시 → 클릭하면 그 종목 토론방으로.
//
//   ★ 왜 '글 검색'이 아니라 '종목 검색'인가?
//     종목토론방(토스·네이버)에서 검색의 주인공은 '종목'이다.
//     "삼성전자" 검색 → 종목 찾기 → 그 종목 토론방으로 이동.
//     글 검색은 각 토론방 '안에서' 한다 (board/?ticker=..&q=..).
//     모든 종목의 글을 통째로 뒤지면 엉뚱한 종목 글이 섞여 쓸모가 없다.
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/stocks.php';   // 종목 데이터·검색 모듈

// ── 1) 검색어 받기 ───────────────────────────────────────────
$q = trim($_GET['q'] ?? '');

// ── 2) 종목 검색 (검색어 없으면 전체 종목) ───────────────────
$stocks = search_stocks(get_stocks(), $q);

$pageTitle = $q === '' ? '종목 검색' : "'{$q}' 검색결과";
require __DIR__ . '/includes/header.php';
?>

  <h1>종목 검색</h1>

  <!-- ★ 검색 폼은 method="get" (글쓰기·댓글 폼이 POST였던 것과 대조)
       GET 폼은 제출하면 입력값이 '주소'에 자동으로 붙는다:
         '삼성' 입력 + 전송  →  /search.php?q=삼성
       그래서 검색 결과 주소를 그대로 공유·북마크할 수 있다.
       value="..." 로 방금 친 검색어를 칸에 남겨둔다(사용자 편의). -->
  <form class="search-form" method="get" action="/search.php">
    <input type="text" name="q" value="<?= e($q) ?>" placeholder="종목명 또는 코드 (예: 삼성, 005930)">
    <button type="submit">검색</button>
  </form>

  <?php if ($q === ''): ?>
    <p class="muted">종목명이나 코드로 검색해 보세요. 아래는 전체 종목입니다.</p>
  <?php else: ?>
    <p class="muted">'<?= e($q) ?>' 검색결과 <?= count($stocks) ?>개</p>
  <?php endif; ?>

  <?php if (!$stocks): ?>
    <p class="muted">일치하는 종목이 없습니다.</p>
  <?php else: ?>
    <ul class="stock-list">
      <?php foreach ($stocks as $s): ?>
        <li>
          <!-- 종목을 클릭하면 그 종목의 토론방으로 (GET으로 ticker 전달) -->
          <a href="/board/?ticker=<?= e($s['ticker']) ?>"><?= e($s['name']) ?></a>
          <span class="post-stat"><?= e($s['ticker']) ?> · <?= e($s['market']) ?></span>
        </li>
      <?php endforeach; ?>
    </ul>
  <?php endif; ?>

<?php require __DIR__ . '/includes/footer.php'; ?>
