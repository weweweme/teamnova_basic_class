<?php
// ============================================================
// board/index.php — 작품 게시판  [GET 요청]
//   ?work= 작품 / ?q= 글검색 / ?sort= 정렬 / ?sentiment= 감상필터 / ?page= 페이지
//   → 파라미터 5개가 한 주소에 겹치는 'GET 복합'의 완성형.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/posts.php';   // 글 데이터·필터·정렬·페이징 모듈
require_once __DIR__ . '/../includes/works.php';   // 작품 데이터 모듈
require_once __DIR__ . '/../includes/tmdb.php';    // 작품 상세(감독·출연·예고편)
require_once __DIR__ . '/../includes/prefs.php';   // 정렬 취향 쿠키 (week16)

// 한 페이지에 보여줄 글 수. (매직값 금지 — 이름 붙인 상수로)
const POSTS_PER_PAGE = 15;                       // 기본값
const PER_PAGE_CHOICES = [15, 30, 50];           // 고를 수 있는 값 = 곧 허용 목록

// 정렬 탭 목록 (키 = URL에 들어갈 값, 값 = 화면에 보일 이름)
//   ★ 화면보다 위에 둔 이유: 이 목록이 곧 '허용된 정렬값'이라, 아래 검증에서 먼저 필요하다.
//     목록과 검증이 따로 놀면 탭을 추가했는데 그 탭만 동작하지 않는 일이 생긴다.
const SORT_TABS    = ['new' => '최신', 'hot' => '인기', 'views' => '조회', 'comments' => '댓글'];
const SORT_DEFAULT = 'new';

// ── 1) 파라미터 받기 ─────────────────────────────────────────
$work      = get_str('work', '');
$q         = mb_substr(trim(get_str('q')), 0, SEARCH_QUERY_MAX);       // 이 게시판 '안에서' 글 검색어
$page      = get_int('page', 1);    // 1부터 시작

// ── 정렬: 주소에 있으면 그걸 쓰고 기억한다, 없으면 지난번 취향을 꺼낸다 ──
//   [왜 쿠키인가]
//     "나는 조회순으로 보는 걸 좋아한다"는 취향이다. 틀려도 탭 한 번 더 누르면 그만이고,
//     브라우저를 닫아도 남아야 하며, 로그인 안 한 사람도 쓴다 → 쿠키 자리다.
//   ★ 주소가 항상 이긴다. 링크를 눌러 온 사람이 보게 될 화면이 내 쿠키 때문에
//     달라지면, 그 링크를 공유한 사람과 다른 걸 보게 된다.
$sortKeys = array_keys(SORT_TABS);
$sortFromUrl = get_str('sort');
if ($sortFromUrl !== '') {
    // 주소로 온 값도 그대로 믿지 않는다 — 허용 목록에 있을 때만 인정하고 기억한다.
    $sort = in_array($sortFromUrl, $sortKeys, true) ? $sortFromUrl : SORT_DEFAULT;
    remember_sort($sort);
} else {
    // ★ preferred_sort()가 허용 목록으로 다시 검증한다. 쿠키는 사용자가 고칠 수 있으므로
    //   "우리가 저장한 값이니 안전하겠지"라고 믿으면 안 된다. (prefs.php 주석 참고)
    $sort = preferred_sort($sortKeys, SORT_DEFAULT);
}

// ── 감상 필터: 정렬과 완전히 같은 규칙 ──────────────────────
//   주소에 있으면 그걸 쓰고 기억한다 / 없으면 지난번 취향을 꺼낸다.
//   ★ 여기도 주소가 항상 이긴다. 공유된 링크를 눌러 온 사람이 내 쿠키 때문에
//     다른 화면을 보면 안 되기 때문이다.
//   ★ '전체'를 빈 문자열이 아니라 'all'로 둔다 — 이유가 있다.
//     query_url()은 값이 빈 파라미터를 주소에서 빼버린다(주소를 깨끗하게 유지하려고).
//     그래서 '전체'를 ''로 두면 링크가 그냥 /board/?work=… 가 되고,
//     **주소에 sentiment가 없으니 쿠키가 이겨서 '혹평'이 그대로 남는 버그**가 났다.
//     → '전체'도 하나의 선택이므로 주소에 남을 수 있는 이름을 준다.
$sentimentKeys = ['all', '호평', '보통', '혹평'];   // 이 목록이 곧 허용 목록이다.
if (isset($_GET['sentiment'])) {
    // 주소로 온 값도 그대로 믿지 않는다 — 허용 목록에 있을 때만 인정하고 기억한다.
    $fromUrl   = get_str('sentiment');
    $sentiment = in_array($fromUrl, $sentimentKeys, true) ? $fromUrl : 'all';
    remember_sentiment($sentiment);
} else {
    // ★ 쿠키 값도 허용 목록으로 다시 검증한다 (preferred_sentiment 안에서).
    $sentiment = preferred_sentiment($sentimentKeys, 'all');
}

// ── 한 페이지 글 수: 정렬·감상과 같은 규칙 (주소가 이기고, 쿠키도 허용 목록 대조) ──
$perPageFromUrl = get_int('per', 0);
if ($perPageFromUrl !== 0) {
    $perPage = in_array($perPageFromUrl, PER_PAGE_CHOICES, true) ? $perPageFromUrl : POSTS_PER_PAGE;
    remember_per_page($perPage);
} else {
    $perPage = preferred_per_page(PER_PAGE_CHOICES, POSTS_PER_PAGE);
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

// 작품 상세(감독·출연·예고편·분량) — 'tmdb-번호' 슬러그일 때만 TMDB에서 가져온다.
//   (우리 DB에만 있는 작품이면 상세가 없으니 $detail은 null → 화면에서 알아서 건너뜀)
$detail = null;
if (str_starts_with($work, 'tmdb-')) {
    $detail = build_tmdb_detail((int) substr($work, 5));
}

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
//   filter_posts_by_sentiment는 ''를 '전체'로 알아듣는다 → 'all'일 때만 ''로 바꿔 넘긴다.
//   ★ 화면·주소에서 쓰는 이름('all')과 필터 함수가 쓰는 값('')을 여기 한 줄에서만 잇는다.
$posts = filter_posts_by_sentiment($posts, $sentiment === 'all' ? '' : $sentiment); // ③ 호평/혹평으로 추리기
$posts = sort_posts($posts, $sort);                     // ④ 정렬

$totalCount = count($posts);                                        // 조건에 맞는 전체 개수
$totalPages = max(1, (int)ceil($totalCount / $perPage));            // 총 페이지 수(올림)
if ($page > $totalPages) {                                          // 범위 넘으면 마지막으로
    $page = $totalPages;
}
$pagePosts = paginate_posts($posts, $page, $perPage);               // 이 페이지 분량만

// ── 5) 탭 목록 ───────────────────────────────────────────────
//   정렬 탭(SORT_TABS)은 검증에도 쓰이므로 파일 맨 위에 있다.
$sortTabs   = SORT_TABS;
$sentiments = ['all' => '전체', '호평' => '호평', '보통' => '보통', '혹평' => '혹평'];

// ── 🆕 배지: 지난 방문 이후 올라온 글 표시 ──────────────────
//   ★ 순서가 중요하다 — **그리기 전에 '지난 방문 시각'을 먼저 읽어둔다.**
//     갱신을 먼저 해버리면 방금 온 나 자신이 기준이 되어 배지가 하나도 안 뜬다.
$lastVisit = last_visit_at();

//   ★ 그리고 곧바로 갱신한다. setcookie()는 **화면에 한 글자라도 출력되기 전에** 불러야 한다
//     (쿠키는 HTTP 헤더로 나가므로). 그래서 '다 그리고 나서 갱신'은 아예 불가능하다.
//   ★ 30분 안에 다시 오면 갱신하지 않는다 → 둘러보는 동안 배지가 유지된다. (prefs.php)
touch_visit();

// 이 작품을 '최근 본 작품'으로 기록한다. (작품 목록 화면에서 다시 보여준다)
//   ★ setcookie는 출력 전이어야 하므로 여기(화면 그리기 직전)에 둔다.
remember_recent_work($work);

$pageTitle = $title . ' 게시판';
require __DIR__ . '/../includes/header.php';
?>

  <h1 class="narrow-title"><?= e($title) ?>
    <?php if ($workInfo !== null): ?>
      <small>(<?= e($workInfo['genre']) ?> · <?= e((string)$workInfo['year']) ?><?php
        if ($detail !== null && $detail['runtimeText'] !== ''): ?> · <?= e($detail['runtimeText']) ?><?php endif; ?>)</small>
    <?php endif; ?>
  </h1>

  <!-- 작품 정보 (포스터 + 감독·출연·분량 + 줄거리 + 예고편) -->
  <?php if ($workInfo === null): ?>
    <div class="widget-placeholder">존재하지 않는 작품입니다</div>
  <?php else: ?>
    <div class="work-info">
      <?php if (!empty($workInfo['poster_url'])): ?>
        <img class="poster" src="<?= e($workInfo['poster_url']) ?>" alt="" loading="lazy">
      <?php endif; ?>
      <div class="work-detail">
        <?php // 감독(영화)/제작(드라마) — 이름이 있을 때만 ?>
        <?php if ($detail !== null && $detail['creditName'] !== ''): ?>
          <p class="work-credit"><span class="k"><?= e($detail['creditLabel']) ?></span><?= e($detail['creditName']) ?></p>
        <?php endif; ?>
        <?php // 출연진 — 있을 때만, 가운뎃점으로 이어 붙여서 ?>
        <?php if ($detail !== null && $detail['cast']): ?>
          <p class="work-credit"><span class="k">출연</span><?= e(implode(' · ', $detail['cast'])) ?></p>
        <?php endif; ?>
        <p class="work-summary"><?= e($workInfo['summary']) ?></p>
        <?php // 예고편 버튼 — 유튜브 영상이 있을 때만. data-trailer에 영상 키를 실어 JS가 읽는다. ?>
        <?php if ($detail !== null && $detail['trailerKey'] !== ''): ?>
          <button type="button" class="btn-trailer" data-trailer="<?= e($detail['trailerKey']) ?>">▶ 예고편 보기</button>
        <?php endif; ?>
      </div>
    </div>
  <?php endif; ?>

  <?php // 투표·글등록·삭제 완료 알림은 header.php가 주소(?flash=)에서 읽어 그린다 (set_flash) ?>

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
      <?= csrf_field() ?>
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
    <?php // 한 페이지에 몇 개씩 볼지 — 고르면 쿠키에 기억된다(취향). ?>
    <span class="per-page">
      <?php foreach (PER_PAGE_CHOICES as $n): ?>
        <a class="<?= $perPage === $n ? 'active' : '' ?>"
           href="<?= e(query_url('/board/', ['per' => (string) $n, 'page' => ''])) ?>"><?= $n ?></a>
      <?php endforeach; ?>
    </span>
    <a class="btn-write" href="/post/write.php?work=<?= e($work) ?>">✏️ 글쓰기</a>
  </div>

  <?php if (!$pagePosts): ?>
    <p class="muted board-empty">해당 조건의 글이 없습니다.</p>
  <?php else: ?>
    <?php // board-list = 게시판 전용 배치(제목 왼쪽 / 정보 오른쪽). 홈의 목록과 구분하려고 붙인다. ?>
    <ul class="post-list board-list">
      <?php foreach ($pagePosts as $p): ?>
        <?php
        // 시각 표기 — 고친 글은 '최종 수정 시각'을 보여준다.
        //   목록에서 보고 싶은 건 "이 글이 마지막으로 언제 달라졌나"이기 때문이다.
        //   edited는 안 고친 글이면 NULL이라, 그때는 작성 시각을 쓴다.
        $isEdited = $p['edited'] !== null;
        $shownAt  = (int) ($isEdited ? $p['edited'] : $p['created']);
        // 마우스를 올리면 정확한 시각을 보여준다. 고친 글은 작성·수정 둘 다.
        $timeHint = format_time_full((int)$p['created']) . ' 작성'
                  . ($isEdited ? ' · ' . format_time_full((int)$p['edited']) . ' 수정' : '');
        ?>
        <li>
          <?php // ── 왼쪽: 제목 · 감상 · 댓글 수 (글을 고르는 데 필요한 것들) ── ?>
          <span class="post-left">
            <?php // 검색 중이면 제목에서 찾은 글자를 형광펜으로 강조한다.
                  // (create_highlighted가 e() 처리까지 끝내주므로 여기선 그대로 출력) ?>
            <?php // title 속성 = 길어서 '…'으로 잘렸을 때 마우스를 올리면 전체 제목이 뜬다. ?>
            <a href="/post/view.php?id=<?= e((string)$p['id']) ?>" title="<?= e($p['title']) ?>"><?= create_highlighted($p['title'], $q) ?></a>
            <?php // 지난 방문 이후에 올라온 글이면 🆕. ($lastVisit이 0이면 = 첫 방문이라 안 붙인다) ?>
            <?php if ($lastVisit > 0 && (int) $p['created'] > $lastVisit): ?>
              <span class="badge-new" title="지난 방문 이후 올라온 글">NEW</span>
            <?php endif; ?>
            <span class="tag"><?= e($p['sentiment']) ?></span>
            <?php // 댓글 수는 '이 글에 이야기가 오갔나'를 알려주므로 제목 옆에 둔다.
                  //   0이면 아예 안 보여준다 — 없는 정보로 줄을 채우지 않는다. ?>
            <?php if ((int)$p['comments'] > 0): ?>
              <span class="post-comments">💬 <?= e((string)$p['comments']) ?></span>
            <?php endif; ?>
          </span>

          <?php // ── 오른쪽: 작성자 · 조회 · 시각 (부가 정보) ── ?>
          <span class="post-right">
            <?= level_badge_html((int)$p['authorPostCount']) ?> <?= e($p['authorNick']) ?>
            · 조회 <?= e((string)$p['views']) ?>
            · <time datetime="<?= e(format_time_machine($shownAt)) ?>" title="<?= e($timeHint) ?>"><?= e(format_time_short($shownAt)) ?></time><?= $isEdited ? ' 수정' : '' ?>
          </span>
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

  <?php // ── 예고편 모달: 버튼을 누르면 이 <dialog>가 페이지 위에 뜬다 (신고 모달과 같은 방식) ──
        //   ★ iframe src를 비워둔다 → 페이지 로드 때 유튜브를 미리 안 불러 빠르다.
        //     버튼을 누르는 '그 순간' JS가 src를 채워 재생하고, 닫으면 다시 비워 정지시킨다. ?>
  <?php if ($detail !== null && $detail['trailerKey'] !== ''): ?>
    <dialog id="trailer-modal" class="trailer-modal">
      <div class="trailer-frame">
        <iframe id="trailer-iframe" src="" title="예고편"
                allow="autoplay; encrypted-media; picture-in-picture" allowfullscreen></iframe>
      </div>
      <form method="dialog"><button class="trailer-close">닫기 ✕</button></form>
    </dialog>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
