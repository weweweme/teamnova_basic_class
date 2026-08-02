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

// ── 카테고리마다 몇 개씩 미리 보여줄까 (매직값 금지 — 이름 붙인 상수로) ──
//   글을 가장 많이 보여준다. 검색해서 읽을거리를 찾는 사람이 제일 많기 때문이다.
const WORK_PREVIEW = 3;
const POST_PREVIEW = 5;
const USER_PREVIEW = 3;

// ── 1) 검색어 받기 ───────────────────────────────────────────
$q        = create_search_query();
$hasQuery = $q !== '';

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

// 셋 다 0이면 "결과 없음" 한 줄만 보여준다 (빈 섹션 세 개를 늘어놓지 않는다)
$foundAny = ($workTotal + $postTotal + $userTotal) > 0;

$pageTitle = $hasQuery ? "'{$q}' 통합검색" : '통합검색';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>🔍 통합검색</h1>

  <?php render_search_bar($q, 'all'); ?>

  <?php // 상태가 셋이라 3갈래로 안내: ① 검색 전 ② 결과 없음 ③ 결과 있음 ?>
  <?php if (!$hasQuery): ?>
    <p class="muted">작품 · 글 · 유저를 한 번에 검색합니다. (예: 기생충, 인생 영화, 영화광)</p>

  <?php elseif (!$foundAny): ?>
    <p class="muted">'<?= e($q) ?>'와 일치하는 결과가 없습니다.</p>

  <?php else: ?>

    <?php // ── 🎬 작품 ──────────────────────────────────────
          //   결과가 0개인 카테고리는 섹션째로 건너뛴다.
          //   "작품 0개"를 세 번 늘어놓으면 진짜 결과가 아래로 밀려난다. ?>
    <?php if ($workTotal > 0): ?>
      <section class="search-section">
        <div class="search-section-head">
          <?php // ★ 작품만 개수를 안 적는다.
                //   TMDB에서 1페이지(20개)만 받아온 상태라 여기 있는 20은 '전체'가 아니다.
                //   그런데도 20이라 적으면, 더보기를 눌러 59개가 나오는 순간 화면이 거짓말을 한 셈이 된다.
                //   모르는 숫자는 말하지 않는다 — 글·유저는 DB가 정확히 세주므로 그대로 적는다. ?>
          <h2>🎬 작품</h2>
          <?php // 전체가 몇 개인지 모르니 더보기는 항상 연다 (실제 전체는 저 화면이 보여준다) ?>
          <a class="search-more" href="<?= e(query_url('/search/works.php', ['page' => ''])) ?>">더보기 ›</a>
        </div>
        <?php render_work_results($works, $q); ?>
      </section>
    <?php endif; ?>

    <?php // ── 📝 글 ────────────────────────────────────── ?>
    <?php if ($postTotal > 0): ?>
      <section class="search-section">
        <div class="search-section-head">
          <h2>📝 글 <span class="count"><?= $postTotal ?></span></h2>
          <?php if ($postTotal > POST_PREVIEW): ?>
            <a class="search-more" href="<?= e(query_url('/search/posts.php', ['page' => ''])) ?>">더보기 ›</a>
          <?php endif; ?>
        </div>
        <?php render_post_results($posts, $q); ?>
      </section>
    <?php endif; ?>

    <?php // ── 👤 유저 ──────────────────────────────────── ?>
    <?php if ($userTotal > 0): ?>
      <section class="search-section">
        <div class="search-section-head">
          <h2>👤 유저 <span class="count"><?= $userTotal ?></span></h2>
          <?php if ($userTotal > USER_PREVIEW): ?>
            <a class="search-more" href="<?= e(query_url('/search/users.php', ['page' => ''])) ?>">더보기 ›</a>
          <?php endif; ?>
        </div>
        <?php render_user_results($users, $q); ?>
      </section>
    <?php endif; ?>

  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
