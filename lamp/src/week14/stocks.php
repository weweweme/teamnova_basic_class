<?php
// ============================================================
// stocks.php — 전체 종목 목록  [GET 요청]
//   ?market=KOSPI / NASDAQ 로 시장을 걸러 본다.
//   각 종목의 '글 수'와 '매수 비율'까지 함께 보여준다.
//
//   ※ 파일 이름이 includes/stocks.php(모듈)와 같지만 폴더가 달라 서로 다른 파일이다.
//     이 파일 = 화면(페이지) / includes/stocks.php = 데이터·기능 모듈.
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/stocks.php';   // 종목 데이터 모듈
require_once __DIR__ . '/includes/posts.php';    // 글 수를 세려고 함께 사용

// ── 1) 시장 필터 받기 + 화이트리스트 검증 ────────────────────
$market = $_GET['market'] ?? '';
if (!in_array($market, ['KOSPI', 'NASDAQ'], true)) {
    $market = '';   // 이상한 값이면 '전체'로
}

// ── 2) 종목 목록 걸러내기 + 글 목록 미리 가져오기 ────────────
$stocks   = filter_stocks_by_market(get_stocks(), $market);
$allPosts = get_posts();   // 반복문 안에서 매번 부르지 않도록 '한 번만' 가져와 재사용

$markets = ['' => '전체', 'KOSPI' => '코스피', 'NASDAQ' => '나스닥'];

$pageTitle = '전체 종목';
require __DIR__ . '/includes/header.php';
?>

  <h1>전체 종목</h1>

  <!-- 시장 필터: ?market= 만 바꾼다 -->
  <div class="filter-tabs">
    <?php foreach ($markets as $key => $label): ?>
      <a class="<?= $market === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/stocks.php', ['market' => $key])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <p class="muted">총 <?= count($stocks) ?>개 종목</p>

  <?php if (!$stocks): ?>
    <p class="muted">해당 시장의 종목이 없습니다.</p>
  <?php else: ?>
    <ul class="stock-list">
      <?php foreach ($stocks as $s): ?>
        <?php
          // 이 종목의 글 수 (posts 모듈 재사용)
          $postCount = count(filter_posts_by_ticker($allPosts, $s['ticker']));

          // 매수 비율 (0으로 나누기 방지 — 먼저 확인)
          $totalVotes = $s['buyVotes'] + $s['sellVotes'];
          $buyPct     = $totalVotes > 0 ? (int)round($s['buyVotes'] / $totalVotes * 100) : 0;
        ?>
        <li>
          <!-- 종목명을 누르면 그 종목 토론방으로 -->
          <a href="/board/?ticker=<?= e($s['ticker']) ?>"><?= e($s['name']) ?></a>
          <span class="tag">매수 <?= $buyPct ?>%</span>
          <span class="post-stat"><?= e($s['ticker']) ?> · <?= e($s['market']) ?> · 글 <?= $postCount ?>개</span>
        </li>
      <?php endforeach; ?>
    </ul>
  <?php endif; ?>

<?php require __DIR__ . '/includes/footer.php'; ?>
