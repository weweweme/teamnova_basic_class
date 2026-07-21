<?php
// ============================================================
// board/index.php — 종목 토론방  [GET 요청]
//   ?ticker= 종목 / ?sort= 정렬 / ?sentiment= 심리필터 / ?page= 페이지
//   → 파라미터 4개가 한 주소에 겹치는 'GET 복합'의 완성형.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/posts.php';    // 글 데이터·필터·정렬·페이징 모듈
require_once __DIR__ . '/../includes/stocks.php';   // 종목 데이터 모듈

// 한 페이지에 보여줄 글 수. (매직값 금지 — 이름 붙인 상수로)
const POSTS_PER_PAGE = 3;

// ── 1) 파라미터 받기 ─────────────────────────────────────────
$ticker    = $_GET['ticker'] ?? '';
$q         = trim($_GET['q'] ?? '');       // 이 토론방 '안에서' 글 검색어
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

// ── 3) 종목 정보 (stocks 모듈에서 조회) ──────────────────────
$stock = get_stock($ticker);                  // 없으면 null
$name  = $stock['name'] ?? '알 수 없는 종목';

// 투자심리 투표 집계 → 퍼센트 계산
//   round() = 반올림. 총 투표가 0이면 나눗셈을 못 하니 먼저 확인(Tester-Doer).
$buyVotes   = $stock['buyVotes']  ?? 0;
$sellVotes  = $stock['sellVotes'] ?? 0;
$totalVotes = $buyVotes + $sellVotes;
$buyPct     = $totalVotes > 0 ? (int)round($buyVotes / $totalVotes * 100) : 0;
$sellPct    = 100 - $buyPct;   // 나머지가 매도 (합이 항상 100이 되도록)

// ── 4) 목록 만들기: 종목으로 추리기 → 심리 필터 → 정렬 → 페이지 자르기 ──
//   ★ 종목 토론방이므로 '그 종목 글만' 추리는 게 첫 단계.
//   ★ 순서 중요! 다 거르고 정렬한 '전체 결과'가 나와야 총 페이지 수를 셀 수 있다.
$posts = get_posts();
$posts = filter_posts_by_ticker($posts, $ticker);       // ① 이 종목 글만
$posts = search_posts($posts, $q);                      // ② 그 안에서 검색어로 추리기
$posts = filter_posts_by_sentiment($posts, $sentiment); // ③ 매수/매도 심리로 추리기
$posts = sort_posts($posts, $sort);                     // ④ 정렬

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

  <?php // vote/sentiment.php가 ?voted=1 로 리다이렉트해오면 완료 알림 ?>
  <?php if (isset($_GET['voted'])): ?>
    <div class="flash">🗳️ 투표했습니다. <small>(지금은 숫자가 실제로 반영되진 않는 껍데기)</small></div>
  <?php endif; ?>

  <!-- 종목 투자심리 투표 — '글'이 아니라 '종목'에 대한 POST -->
  <section class="vote-box">
    <h2>투자 심리</h2>

    <?php if ($totalVotes > 0): ?>
      <!-- 막대그래프: 두 칸의 너비(%)를 style로 직접 지정해 비율을 표현 -->
      <div class="vote-bar">
        <div class="vote-buy"  style="width: <?= $buyPct ?>%">매수 <?= $buyPct ?>%</div>
        <div class="vote-sell" style="width: <?= $sellPct ?>%">매도 <?= $sellPct ?>%</div>
      </div>
      <p class="muted">총 <?= $totalVotes ?>표</p>
    <?php else: ?>
      <p class="muted">아직 투표가 없습니다.</p>
    <?php endif; ?>

    <!-- 투표 = 서버 상태를 바꾸는 동작 → POST.
         ★ 새로운 기법: 제출 버튼에 name과 value를 달면 '어느 버튼을 눌렀는지'가 전송된다.
           (앞에서 '버튼은 name이 없어서 전송 안 된다'고 했던 그 반대 경우!)
           덕분에 폼 하나로 버튼 두 개를 구분해서 처리할 수 있다 → $_POST['choice'] -->
    <form class="vote-form" method="post" action="/vote/sentiment.php">
      <input type="hidden" name="ticker" value="<?= e($ticker) ?>">
      <button type="submit" name="choice" value="매수" class="btn-buy">📈 매수</button>
      <button type="submit" name="choice" value="매도" class="btn-sell">📉 매도</button>
    </form>
  </section>

  <!-- 이 토론방 '안에서만' 글 검색 (종목 검색은 상단 메뉴의 '검색')
       ★ GET 폼의 함정: 폼을 제출하면 주소의 기존 파라미터가 전부 사라지고
         '폼 안의 입력칸들'만 새 주소가 된다.
         → 종목(ticker)을 hidden으로 같이 실어 보내야 "그 종목 토론방"이 유지된다.
         (정렬·심리필터·페이지는 새로 검색하는 것이니 초기화되는 게 자연스러워 일부러 안 넣음) -->
  <form class="search-form" method="get" action="/board/">
    <input type="hidden" name="ticker" value="<?= e($ticker) ?>">
    <input type="text" name="q" value="<?= e($q) ?>" placeholder="이 종목 글 검색">
    <button type="submit">검색</button>
  </form>

  <?php if ($q !== ''): ?>
    <p class="muted">
      '<?= e($q) ?>' 검색 중 —
      <a href="<?= e(query_url('/board/', ['q' => '', 'page' => ''])) ?>">검색 해제</a>
    </p>
  <?php endif; ?>

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
