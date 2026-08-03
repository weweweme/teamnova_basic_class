<?php
// ============================================================
// search/index.php — 통합검색  [GET 요청]
//   ?q=기생충 → 작품(TMDB) · 글(DB) · 유저(DB)를 한 번에 찾아
//   카테고리마다 몇 개씩만 보여주고, 더 있으면 '더보기'로 전용 페이지로 보낸다.
//   (포털 검색과 같은 구조 — 통합은 맛보기, 전용 페이지가 전체)
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/search_ui.php';   // 검색창·탭·결과 줄 조각
require_once __DIR__ . '/../includes/tmdb.php';        // 작품 검색(TMDB 실시간)
require_once __DIR__ . '/../includes/posts.php';       // 글 검색(DB)
require_once __DIR__ . '/../includes/users.php';       // 유저 검색(DB)
require_once __DIR__ . '/../includes/prefs.php';       // 최근 검색어 쿠키 (week16)

// ── 카테고리마다 몇 개씩 미리 보여줄까 (매직값 금지 — 이름 붙인 상수로) ──
//   글을 가장 많이 보여준다. 검색해서 읽을거리를 찾는 사람이 제일 많기 때문이다.
const WORK_PREVIEW = 3;
const POST_PREVIEW = 5;
const USER_PREVIEW = 3;

// ── 1) 검색어 받기 ───────────────────────────────────────────
$q        = create_search_query();
$hasQuery = $q !== '';

// ── 최근 검색어 (week16 쿠키) ────────────────────────────────
//   ★ 쿠키를 굽는 일이라 화면 출력이 시작되기 전에 해야 한다 → 여기(파일 위쪽)에 둔다.
//   ★ 읽기는 '지금 검색한 말을 넣기 전'에 해도 되고 후에 해도 되지만, 넣은 뒤에 읽어야
//     방금 친 말이 목록 맨 앞에 보인다. 그래서 넣고 → 읽는 순서다.
if ($hasQuery) {
    remember_search($q);
}
$recentSearches = get_recent_searches();

// ── 2) 세 곳에서 찾는다 (검색어가 있을 때만) ─────────────────
//   ★ '전체 개수'와 '보여줄 몇 개'를 따로 구한다.
//     개수를 알아야 "더보기를 띄울지"와 "몇 개 중 몇 개인지"를 말할 수 있고,
//     그렇다고 전부 가져오면 3개 보여주려고 수백 건을 실어 나르게 된다.
$works      = [];
$posts      = [];
$users      = [];
$workTotal  = 0;
$postTotal  = 0;
$userTotal  = 0;

if ($hasQuery) {
    // 작품: TMDB는 '개수만 세기'가 따로 없어서 1페이지(20개)를 받아 앞에서 자른다.
    //   여기서 3개만 쓸 건데 3페이지(60개)까지 받으면 남의 서버를 세 번 두드려 화면이 느려진다.
    //   (30분 캐시가 걸려 있어 같은 검색어를 다시 쳐도 다시 부르지는 않는다)
    $allWorks  = search_tmdb($q);
    $workTotal = count($allWorks);   // '전체'가 아니라 '1페이지에서 찾은 수' → 화면엔 안 적는다
    $works     = array_slice($allWorks, 0, WORK_PREVIEW);

    // 글·유저: DB가 세고(COUNT), 보여줄 만큼만 가져온다(LIMIT).
    $postTotal = count_search_posts($q);
    $posts     = search_posts_db($q, POST_PREVIEW);
    $userTotal = count_search_users($q);
    $users     = search_users($q, USER_PREVIEW);
}

$pageTitle = $hasQuery ? "'{$q}' 통합검색" : '통합검색';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>🔍 통합검색</h1>

  <?php render_search_bar($q, 'all'); ?>

  <?php // ── 최근 검색어 (쿠키) ─────────────────────────────
        //   ★ 한 번도 검색한 적 없으면 줄 자체를 그리지 않는다 — 빈 '최근 검색어'는
        //     알려주는 게 없으면서 자리만 차지한다.
        //   ★ e()로 감싸는 걸 잊으면 안 된다. 쿠키 값은 사용자가 넣은 글자이고,
        //     쿠키는 손으로 고칠 수 있으니 <script>를 심어둘 수도 있다 (XSS). ?>
  <?php if ($recentSearches): ?>
    <div class="recent-searches">
      <span class="muted">최근 검색어</span>
      <?php foreach ($recentSearches as $past): ?>
        <a href="/search/?q=<?= urlencode($past) ?>"><?= e($past) ?></a>
      <?php endforeach; ?>
    </div>
  <?php endif; ?>

  <?php if (!$hasQuery): ?>
    <p class="muted">작품 · 글 · 유저를 한 번에 검색합니다. (예: 기생충, 인생 영화, 영화광)</p>

  <?php else: ?>

    <?php // ★ 세 칸은 결과가 없어도 자리를 지킨다.
          //   빈 칸을 지워버리면 화면이 검색할 때마다 달라져서, 없는 게 아니라
          //   '아직 안 나온 것'처럼 보인다 — 고장으로 오해하기 쉽다.
          //   자리에 "없습니다"가 적혀 있으면 그 자체가 답이 된다. ?>

    <?php // ── 🎬 작품 ────────────────────────────────────── ?>
    <section class="search-section">
      <div class="search-section-head">
        <?php // ★ 작품만 개수를 안 적는다.
              //   TMDB에서 1페이지(20개)만 받아온 상태라 여기 있는 20은 '전체'가 아니다.
              //   그런데도 20이라 적으면, 더보기를 눌러 59개가 나오는 순간 화면이 거짓말을 한 셈이 된다.
              //   모르는 숫자는 말하지 않는다 — 글·유저는 DB가 정확히 세주므로 그대로 적는다. ?>
        <h2>🎬 작품</h2>
        <?php // 전체가 몇 개인지 모르니, 하나라도 있으면 더보기를 연다 (실제 전체는 저 화면이 보여준다) ?>
        <?php if ($workTotal > 0): ?>
          <a class="search-more" href="<?= e(query_url('/search/works.php', ['page' => ''])) ?>">더보기 ›</a>
        <?php endif; ?>
      </div>
      <?php if ($workTotal > 0): ?>
        <?php render_work_results($works, $q); ?>
      <?php else: ?>
        <p class="muted search-empty">일치하는 작품이 없습니다.</p>
      <?php endif; ?>
    </section>

    <?php // ── 📝 글 ──────────────────────────────────────── ?>
    <section class="search-section">
      <div class="search-section-head">
        <h2>📝 글 <span class="count"><?= $postTotal ?></span></h2>
        <?php if ($postTotal > POST_PREVIEW): ?>
          <a class="search-more" href="<?= e(query_url('/search/posts.php', ['page' => ''])) ?>">더보기 ›</a>
        <?php endif; ?>
      </div>
      <?php if ($postTotal > 0): ?>
        <?php render_post_results($posts, $q); ?>
      <?php else: ?>
        <p class="muted search-empty">제목·내용에 '<?= e($q) ?>'가 들어간 글이 없습니다.</p>
      <?php endif; ?>
    </section>

    <?php // ── 👤 유저 ────────────────────────────────────── ?>
    <section class="search-section">
      <div class="search-section-head">
        <h2>👤 유저 <span class="count"><?= $userTotal ?></span></h2>
        <?php if ($userTotal > USER_PREVIEW): ?>
          <a class="search-more" href="<?= e(query_url('/search/users.php', ['page' => ''])) ?>">더보기 ›</a>
        <?php endif; ?>
      </div>
      <?php if ($userTotal > 0): ?>
        <?php render_user_results($users, $q); ?>
      <?php else: ?>
        <p class="muted search-empty">아이디·닉네임이 일치하는 회원이 없습니다.</p>
      <?php endif; ?>
    </section>

  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
