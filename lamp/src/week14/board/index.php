<?php
// ============================================================
// board/index.php — 종목 토론방  [GET 요청]
//   ?ticker= 종목 / ?sort= 정렬 / ?sentiment= 심리 필터
//   → 파라미터 3개가 한 주소에 겹치는 'GET 복합' 예시.
//   (페이징 ?page= 는 다음 micro-step)
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/posts.php';   // 글 데이터·필터·정렬 모듈

// ── 1) 파라미터 받기 ─────────────────────────────────────────
$ticker    = $_GET['ticker'] ?? '';
$sort      = $_GET['sort'] ?? 'new';       // new | hot | views | comments
$sentiment = $_GET['sentiment'] ?? '';     // '' = 전체 | 매수 | 매도 | 중립

// 심리 값 검증: 허용된 값만 인정하고, 이상한 값이 오면 '전체'로 되돌린다.
//   in_array(찾을값, 배열, true) — 세 번째 true는 '타입까지 엄격 비교'.
if (!in_array($sentiment, ['매수', '매도', '중립'], true)) {
    $sentiment = '';
}

// ── 2) ticker 검증 (없으면 안내 후 종료) ─────────────────────
if ($ticker === '') {
    $pageTitle = '종목 토론방';
    require __DIR__ . '/../includes/header.php';
    echo '<p>종목을 선택해 주세요. <a href="/">홈으로</a></p>';
    require __DIR__ . '/../includes/footer.php';
    exit;
}

// ── 3) ticker → 종목명 (더미, 나중 stocks 테이블 조회) ───────
$stockNames = ['005930' => '삼성전자', '000660' => 'SK하이닉스', 'AAPL' => '애플'];
$name = $stockNames[$ticker] ?? '알 수 없는 종목';

// ── 4) 목록 만들기: 가져오기 → 걸러내기 → 정렬 ───────────────
//   ★ 순서가 중요! '거른 뒤 정렬'해야 필요한 것만 정렬한다.
$posts = get_posts();
$posts = filter_posts_by_sentiment($posts, $sentiment);
$posts = sort_posts($posts, $sort);

// ── 5) 탭 목록 (키 = URL에 들어갈 값, 값 = 화면에 보일 이름) ──
$sortTabs   = ['new' => '최신', 'hot' => '인기', 'views' => '조회', 'comments' => '댓글'];
$sentiments = ['' => '전체', '매수' => '매수', '매도' => '매도', '중립' => '중립'];

$pageTitle = $name . ' 토론방';
require __DIR__ . '/../includes/header.php';
?>

  <h1><?= e($name) ?> <small>(<?= e($ticker) ?>)</small></h1>
  <div class="widget-placeholder">📈 현재가 · 차트 위젯 자리 (나중 연결)</div>

  <!-- 정렬 탭: ?sort= 만 바꾼다. 나머지(종목·필터)는 query_url이 유지해줌 -->
  <div class="sort-tabs">
    <?php foreach ($sortTabs as $key => $label): ?>
      <a class="<?= $sort === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/board/', ['sort' => $key])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <!-- 심리 필터: ?sentiment= 만 바꾼다. '전체'는 값이 ''라 주소에서 아예 빠짐 -->
  <div class="filter-tabs">
    <?php foreach ($sentiments as $key => $label): ?>
      <a class="<?= $sentiment === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/board/', ['sentiment' => $key])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <p class="muted">총 <?= count($posts) ?>개</p>

  <?php if (!$posts): ?>
    <!-- 필터 결과가 0개일 때 -->
    <p class="muted">해당 조건의 글이 없습니다.</p>
  <?php else: ?>
    <ul class="post-list">
      <?php foreach ($posts as $p): ?>
        <li>
          <a href="/post/view.php?id=<?= e((string)$p['id']) ?>"><?= e($p['title']) ?></a>
          <span class="tag"><?= e($p['sentiment']) ?></span>
          <span class="post-stat">조회 <?= e((string)$p['views']) ?> · 댓글 <?= e((string)$p['comments']) ?></span>
        </li>
      <?php endforeach; ?>
    </ul>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
