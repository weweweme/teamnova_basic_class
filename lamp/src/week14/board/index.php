<?php
// ============================================================
// board/index.php — 종목 토론방  [GET 요청]
//   ?ticker= 종목 / ?sort= 정렬 / ?sentiment= 심리필터 / ?page= 페이지
//   → 파라미터 4개가 한 주소에 겹치는 'GET 복합'의 완성형.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/posts.php';   // 글 데이터·필터·정렬·페이징 모듈

// 한 페이지에 보여줄 글 수. (매직값 금지 — 이름 붙인 상수로)
const POSTS_PER_PAGE = 3;

// ── 1) 파라미터 받기 ─────────────────────────────────────────
$ticker    = $_GET['ticker'] ?? '';
$sort      = $_GET['sort'] ?? 'new';       // new | hot | views | comments
$sentiment = $_GET['sentiment'] ?? '';     // '' = 전체 | 매수 | 매도 | 중립
$page      = (int)($_GET['page'] ?? 1);    // 1부터 시작

// 심리 값 검증: 허용된 값만 인정, 이상한 값이면 '전체'로.
if (!in_array($sentiment, ['매수', '매도', '중립'], true)) {
    $sentiment = '';
}
// 페이지 최소값 보정 (?page=0, ?page=-5 같은 장난 방어)
if ($page < 1) {
    $page = 1;
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

// ── 4) 목록 만들기: 가져오기 → 걸러내기 → 정렬 → 페이지 자르기 ──
//   ★ 순서 중요! 거르고 정렬한 '전체 결과'가 나와야 총 페이지 수를 셀 수 있다.
$posts = get_posts();
$posts = filter_posts_by_sentiment($posts, $sentiment);
$posts = sort_posts($posts, $sort);

$totalCount = count($posts);                                        // 조건에 맞는 전체 개수
$totalPages = max(1, (int)ceil($totalCount / POSTS_PER_PAGE));      // 총 페이지 수(올림)
if ($page > $totalPages) {                                          // 범위 넘으면 마지막으로
    $page = $totalPages;
}
$pagePosts = paginate_posts($posts, $page, POSTS_PER_PAGE);         // 이 페이지 분량만

// ── 5) 탭 목록 (키 = URL에 들어갈 값, 값 = 화면에 보일 이름) ──
$sortTabs   = ['new' => '최신', 'hot' => '인기', 'views' => '조회', 'comments' => '댓글'];
$sentiments = ['' => '전체', '매수' => '매수', '매도' => '매도', '중립' => '중립'];

$pageTitle = $name . ' 토론방';
require __DIR__ . '/../includes/header.php';
?>

  <h1><?= e($name) ?> <small>(<?= e($ticker) ?>)</small></h1>
  <div class="widget-placeholder">📈 현재가 · 차트 위젯 자리 (나중 연결)</div>

  <!-- 정렬 탭: ?sort= 만 바꾼다.
       ★ 'page' => '' 를 같이 넘겨 페이지를 1로 리셋한다.
          (정렬이 바뀌면 목록 순서가 달라지므로, 3페이지에 머물러 있으면 엉뚱함) -->
  <div class="sort-tabs">
    <?php foreach ($sortTabs as $key => $label): ?>
      <a class="<?= $sort === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/board/', ['sort' => $key, 'page' => ''])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <!-- 심리 필터: ?sentiment= 만 바꾼다. 여기도 페이지 1로 리셋.
       ('전체'는 값이 ''라 주소에서 아예 빠짐) -->
  <div class="filter-tabs">
    <?php foreach ($sentiments as $key => $label): ?>
      <a class="<?= $sentiment === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/board/', ['sentiment' => $key, 'page' => ''])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <p class="muted">총 <?= $totalCount ?>개 · <?= $page ?>/<?= $totalPages ?> 페이지</p>

  <?php if (!$pagePosts): ?>
    <p class="muted">해당 조건의 글이 없습니다.</p>
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

  <!-- 페이지 이동: ?page= 만 바꾸고 종목·정렬·필터는 query_url이 그대로 유지.
       nav = '이동 링크 묶음'을 뜻하는 의미(시맨틱) 태그 -->
  <?php if ($totalPages > 1): ?>
    <nav class="pagination">
      <!-- ① 이전 버튼 (첫 페이지면 링크가 아닌 span = 못 누름) -->
      <?php if ($page > 1): ?>
        <a class="page-nav" href="<?= e(query_url('/board/', ['page' => $page - 1])) ?>">← 이전</a>
      <?php else: ?>
        <span class="page-nav disabled">← 이전</span>
      <?php endif; ?>

      <!-- ② 페이지 번호들 (가운데). 현재 페이지는 active로 꽉 찬 배지 -->
      <div class="page-numbers">
        <?php for ($n = 1; $n <= $totalPages; $n++): ?>
          <a class="page-num <?= $n === $page ? 'active' : '' ?>"
             href="<?= e(query_url('/board/', ['page' => $n])) ?>"><?= $n ?></a>
        <?php endfor; ?>
      </div>

      <!-- ③ 다음 버튼 (마지막 페이지면 못 누름) -->
      <?php if ($page < $totalPages): ?>
        <a class="page-nav" href="<?= e(query_url('/board/', ['page' => $page + 1])) ?>">다음 →</a>
      <?php else: ?>
        <span class="page-nav disabled">다음 →</span>
      <?php endif; ?>
    </nav>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
