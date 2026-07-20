<?php
// ============================================================
// board/index.php — 종목 토론방  [GET 요청]
//   ?ticker= 로 종목을 받고, ?sort= 로 글 '정렬 방식'을 받는다.
//   (심리 필터 ?sentiment=, 페이징 ?page= 는 다음 micro-step에서 추가)
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/posts.php';   // 글 데이터·정렬을 모아둔 모듈

// ── 1) 파라미터 받기 ─────────────────────────────────────────
$ticker = $_GET['ticker'] ?? '';
$sort   = $_GET['sort'] ?? 'new';   // new(최신) | hot(인기) | views(조회) | comments(댓글)

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

// ── 4) 글 목록 가져와서 정렬 (둘 다 posts 모듈에 맡김) ───────
//   get_posts()  : 더미 글 전체 가져오기
//   sort_posts() : 정렬 기준(sort)대로 정렬
//   ★ 정렬 로직을 이 파일에 두지 않고 모듈로 뺀 이유: search·profile도 같은 정렬을
//     쓸 거라, 한 곳(posts.php)에 두면 중복이 없고 나중 DB 교체도 그 안만 고치면 됨.
$posts = sort_posts(get_posts(), $sort);

// ── 6) 정렬 탭 목록 (키=?sort 값, 값=표시 이름) ──────────────
$sortTabs = ['new' => '최신', 'hot' => '인기', 'views' => '조회', 'comments' => '댓글'];

$pageTitle = $name . ' 토론방';
require __DIR__ . '/../includes/header.php';
?>

  <h1><?= e($name) ?> <small>(<?= e($ticker) ?>)</small></h1>
  <div class="widget-placeholder">📈 현재가 · 차트 위젯 자리 (나중 연결)</div>

  <!-- 정렬 탭: 클릭하면 ?sort= 만 바꿔서 다시 요청 (ticker는 유지) -->
  <div class="sort-tabs">
    <?php foreach ($sortTabs as $key => $label): ?>
      <!-- 지금 선택된 정렬이면 'active' 클래스를 붙여 강조 (삼항 연산자) -->
      <a class="<?= $sort === $key ? 'active' : '' ?>"
         href="/board/?ticker=<?= e($ticker) ?>&sort=<?= e($key) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <ul class="post-list">
    <?php foreach ($posts as $p): ?>
      <li>
        <a href="/post/view.php?id=<?= e((string)$p['id']) ?>"><?= e($p['title']) ?></a>
        <span class="tag"><?= e($p['sentiment']) ?></span>
        <span class="post-stat">조회 <?= e((string)$p['views']) ?> · 댓글 <?= e((string)$p['comments']) ?></span>
      </li>
    <?php endforeach; ?>
  </ul>

<?php require __DIR__ . '/../includes/footer.php'; ?>
