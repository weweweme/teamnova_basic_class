<?php
// ============================================================
// board/index.php — 작품 게시판  [GET 요청]
//   ?work= 작품 / ?q= 글검색 / ?sort= 정렬 / ?sentiment= 감상필터 / ?page= 페이지
//   → 파라미터 5개가 한 주소에 겹치는 'GET 복합'의 완성형.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/posts.php';   // 글 데이터·필터·정렬·페이징 모듈
require_once __DIR__ . '/../includes/works.php';   // 작품 데이터 모듈

// 한 페이지에 보여줄 글 수. (매직값 금지 — 이름 붙인 상수로)
const POSTS_PER_PAGE = 3;

// ── 1) 파라미터 받기 ─────────────────────────────────────────
$work      = get_str('work', '');
$q         = mb_substr(trim(get_str('q')), 0, SEARCH_QUERY_MAX);       // 이 게시판 '안에서' 글 검색어
$sort      = get_str('sort', 'new');       // new | hot | views | comments
$sentiment = get_str('sentiment', '');     // '' = 전체 | 호평 | 보통 | 혹평
$page      = get_int('page', 1);    // 1부터 시작

// 감상 값 검증: 허용된 값만 인정하고, 이상한 값이 오면 '전체'로 되돌린다.
if (!in_array($sentiment, ['호평', '보통', '혹평'], true)) {
    $sentiment = '';
}
// 페이지 최소값 보정 (?page=0, ?page=-5 같은 장난 방어)
if ($page < 1) {
    $page = 1;
}

// ── 2) 작품 검증 (없으면 안내 후 종료) ───────────────────────
if ($work === '') {
    $pageTitle = '작품 게시판';
    require __DIR__ . '/../includes/header.php';
    echo '<p>작품을 선택해 주세요. <a href="/">홈으로</a></p>';
    require __DIR__ . '/../includes/footer.php';
    exit;
}

// ── 3) 작품 정보 (works 모듈에서 조회) ───────────────────────
$workInfo = get_work($work);                    // 없으면 null
$title    = $workInfo['title'] ?? '알 수 없는 작품';

// 추천/비추천 투표 집계 → 퍼센트 계산
//   round() = 반올림. 총 투표가 0이면 나눗셈을 못 하니 먼저 확인(Tester-Doer).
$upVotes    = $workInfo['upVotes']   ?? 0;
$downVotes  = $workInfo['downVotes'] ?? 0;
$totalVotes = $upVotes + $downVotes;
$upPct      = $totalVotes > 0 ? (int)round($upVotes / $totalVotes * 100) : 0;
$downPct    = 100 - $upPct;   // 나머지가 비추천 (합이 항상 100이 되도록)

// ── 4) 목록 만들기: 작품으로 추리기 → 검색 → 감상 필터 → 정렬 → 페이지 자르기 ──
//   ★ 순서 중요! 다 거르고 정렬한 '전체 결과'가 나와야 총 페이지 수를 셀 수 있다.
$posts = get_posts();
$posts = filter_posts_by_work($posts, $work);           // ① 이 작품 글만
$posts = search_posts($posts, $q);                      // ② 그 안에서 검색어로 추리기
$posts = filter_posts_by_sentiment($posts, $sentiment); // ③ 호평/혹평으로 추리기
$posts = sort_posts($posts, $sort);                     // ④ 정렬

$totalCount = count($posts);                                        // 조건에 맞는 전체 개수
$totalPages = max(1, (int)ceil($totalCount / POSTS_PER_PAGE));      // 총 페이지 수(올림)
if ($page > $totalPages) {                                          // 범위 넘으면 마지막으로
    $page = $totalPages;
}
$pagePosts = paginate_posts($posts, $page, POSTS_PER_PAGE);         // 이 페이지 분량만

// ── 5) 탭 목록 (키 = URL에 들어갈 값, 값 = 화면에 보일 이름) ──
$sortTabs   = ['new' => '최신', 'hot' => '인기', 'views' => '조회', 'comments' => '댓글'];
$sentiments = ['' => '전체', '호평' => '호평', '보통' => '보통', '혹평' => '혹평'];

$pageTitle = $title . ' 게시판';
require __DIR__ . '/../includes/header.php';
?>

  <h1><?= e($title) ?>
    <?php if ($workInfo !== null): ?>
      <small>(<?= e($workInfo['genre']) ?> · <?= e((string)$workInfo['year']) ?>)</small>
    <?php endif; ?>
  </h1>

  <!-- 작품 정보 -->
  <?php if ($workInfo === null): ?>
    <div class="widget-placeholder">존재하지 않는 작품입니다</div>
  <?php else: ?>
    <div class="work-info">
      <p class="work-director">감독 · <?= e($workInfo['director']) ?></p>
      <p class="work-summary"><?= e($workInfo['summary']) ?></p>
    </div>
  <?php endif; ?>

  <?php // 투표·글등록·삭제 완료 알림은 header.php가 세션에서 꺼내 그린다 (set_flash) ?>

  <!-- 작품 추천/비추천 투표 — '글'이 아니라 '작품'에 대한 POST -->
  <section class="vote-box">
    <h2>이 작품, 추천하시나요?</h2>

    <?php if ($totalVotes > 0): ?>
      <!-- 막대그래프: 두 칸의 너비(%)를 style로 직접 지정해 비율을 표현 -->
      <div class="vote-bar">
        <div class="vote-buy"  style="width: <?= $upPct ?>%">추천 <?= $upPct ?>%</div>
        <div class="vote-sell" style="width: <?= $downPct ?>%">비추천 <?= $downPct ?>%</div>
      </div>
      <p class="muted">총 <?= $totalVotes ?>표</p>
    <?php else: ?>
      <p class="muted">아직 투표가 없습니다.</p>
    <?php endif; ?>

    <!-- 투표 = 서버 상태를 바꾸는 동작 → POST.
         ★ 제출 버튼에 name과 value를 달면 '어느 버튼을 눌렀는지'가 전송된다.
           덕분에 폼 하나로 버튼 두 개를 구분해서 처리할 수 있다 → $_POST['choice'] -->
    <?php // 내가 고른 쪽 버튼을 채워서 표시한다 (버튼 색이 곧 '내 선택' 표시라 따로 안내문은 두지 않음) ?>
    <?php $myVote = my_vote($work); ?>
    <form class="vote-form" method="post" action="/vote/sentiment.php">
      <input type="hidden" name="work" value="<?= e($work) ?>">
      <button type="submit" name="choice" value="추천"
              class="btn-buy <?= $myVote === '추천' ? 'voted-up' : '' ?>">👍 추천</button>
      <button type="submit" name="choice" value="비추천"
              class="btn-sell <?= $myVote === '비추천' ? 'voted-down' : '' ?>">👎 비추천</button>
    </form>
  </section>

  <!-- 이 게시판 '안에서만' 글 검색 (작품 검색은 상단 메뉴의 '검색')
       ★ GET 폼의 함정: 폼을 제출하면 주소의 기존 파라미터가 전부 사라지고
         '폼 안의 입력칸들'만 새 주소가 된다.
         → 작품(work)을 hidden으로 같이 실어 보내야 "그 작품 게시판"이 유지된다. -->
  <form class="search-form" method="get" action="/board/">
    <input type="hidden" name="work" value="<?= e($work) ?>">
    <input type="text" name="q" maxlength="50" value="<?= e($q) ?>" placeholder="이 작품 글 검색">
    <button type="submit">검색</button>
  </form>

  <?php if ($q !== ''): ?>
    <p class="muted">
      '<?= e($q) ?>' 검색 중 —
      <a href="<?= e(query_url('/board/', ['q' => '', 'page' => ''])) ?>">검색 해제</a>
    </p>
  <?php endif; ?>

  <!-- 정렬 탭: ?sort= 만 바꾼다. 'page' => '' 로 페이지를 1로 리셋 -->
  <div class="sort-tabs">
    <?php foreach ($sortTabs as $key => $label): ?>
      <a class="<?= $sort === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/board/', ['sort' => $key, 'page' => ''])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <!-- 감상 필터: ?sentiment= 만 바꾼다. ('전체'는 값이 ''라 주소에서 아예 빠짐) -->
  <div class="filter-tabs">
    <?php foreach ($sentiments as $key => $label): ?>
      <a class="<?= $sentiment === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/board/', ['sentiment' => $key, 'page' => ''])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <div class="board-toolbar">
    <p class="muted">총 <?= $totalCount ?>개 · <?= $page ?>/<?= $totalPages ?> 페이지</p>
    <!-- 이 작품으로 글쓰기 — GET으로 work를 넘기면 글쓰기 폼에서 그 작품이 미리 선택된다. -->
    <a class="btn-write" href="/post/write.php?work=<?= e($work) ?>">✏️ 글쓰기</a>
  </div>

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

  <!-- 페이지 이동: ?page= 만 바꾸고 작품·정렬·필터·검색어는 query_url이 그대로 유지 -->
  <?php if ($totalPages > 1): ?>
    <nav class="pagination">
      <?php if ($page > 1): ?>
        <a class="page-nav" href="<?= e(query_url('/board/', ['page' => $page - 1])) ?>">← 이전</a>
      <?php else: ?>
        <span class="page-nav disabled">← 이전</span>
      <?php endif; ?>

      <div class="page-numbers">
        <?php for ($n = 1; $n <= $totalPages; $n++): ?>
          <a class="page-num <?= $n === $page ? 'active' : '' ?>"
             href="<?= e(query_url('/board/', ['page' => $n])) ?>"><?= $n ?></a>
        <?php endfor; ?>
      </div>

      <?php if ($page < $totalPages): ?>
        <a class="page-nav" href="<?= e(query_url('/board/', ['page' => $page + 1])) ?>">다음 →</a>
      <?php else: ?>
        <span class="page-nav disabled">다음 →</span>
      <?php endif; ?>
    </nav>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
