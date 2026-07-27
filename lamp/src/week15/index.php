<?php
// ============================================================
// index.php — 홈 화면  [GET 요청]  (넷플릭스식 가로 스크롤 줄)
//   · TMDB에서 인기작을 가져와 포스터 줄로 보여준다 (둘러보기).
//   · 맨 위엔 '우리 커뮤니티에서 이야기 중'인 작품 줄 (우리 DB = 우리 정체성).
//   ★ media 표는 화면 목록이 아니라 '글이 저장될 때 참조하는 내부 저장소'로만 쓴다.
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/tmdb.php';       // 인기작·인기영화·인기드라마
require_once __DIR__ . '/includes/works.php';      // 우리 커뮤니티 작품
require_once __DIR__ . '/includes/posts.php';      // 최근 글 (게시판)
require_once __DIR__ . '/includes/media_row.php';  // 가로 줄 렌더링 조각

// 각 줄의 데이터 (우리 DB + TMDB)
$community = get_community_works();               // 우리 DB — 글 달린 작품 + 지표
$trending  = tmdb_trending();                     // TMDB — 이번 주 인기작
$movies    = tmdb_popular('movie');               // TMDB — 인기 영화
$tv        = tmdb_popular('tv');                  // TMDB — 인기 드라마
$recent    = paginate_posts(get_posts(), 1, 8);   // 최근 글 8개 (get_posts는 최신순)

// ── 히어로 배너: 우리 커뮤니티 1위(글 최다). 없으면 이번 주 인기작 1위. ──
//   backdrop(가로 큰 이미지)이 DB엔 없으므로, tmdb_id로 TMDB 상세를 가져와 채운다.
$hero = null;
if ($community) {
    $topSlug = $community[0]['slug'];                        // 글 제일 많은 작품
    $hero = tmdb_find_by_id((int) substr($topSlug, 5));      // 'tmdb-496243' → 496243
    if ($hero) { $hero['slug'] = $topSlug; }
} elseif ($trending) {
    $hero = $trending[0];
    $hero['slug'] = 'tmdb-' . $hero['tmdb_id'];
}

$pageTitle = '홈 · 리뷰 커뮤니티';
require __DIR__ . '/includes/header.php';
?>

  <?php // 로그인·글등록 완료 알림은 header.php가 세션에서 꺼내 그린다 (set_flash) ?>

  <?php // ── 히어로 배너 (넷플릭스 첫 화면식 큰 배경) ─────────────── ?>
  <?php if ($hero && !empty($hero['backdrop_url'])): ?>
    <a class="hero" href="/board/?work=<?= e($hero['slug']) ?>"
       style="background-image: url('<?= e($hero['backdrop_url']) ?>')">
      <div class="hero-inner">
        <h1 class="hero-title"><?= e($hero['title']) ?></h1>
        <p class="hero-desc"><?= e(mb_substr($hero['overview'] ?? '', 0, 110)) ?>…</p>
        <span class="hero-cta">리뷰 보러 가기 →</span>
      </div>
    </a>
  <?php endif; ?>

  <?php
    // ── 우리 커뮤니티 (대형 카드로 강조) ──────────────────────
    //   ★ 우리 것을 '맨 위·크게' — TMDB 포스터에 정체성이 묻히지 않도록.
    render_media_row('🔥 우리 커뮤니티에서 이야기 중', $community, 'lg');

    // ── TMDB 둘러보기 (소형 카드, 리사이클러뷰처럼 여러 줄) ──
    render_media_row('이번 주 인기작', $trending, 'sm');
    render_media_row('인기 영화',      $movies,   'sm');
    render_media_row('인기 드라마',    $tv,       'sm');
  ?>

  <!-- ── 게시판: 최근 올라온 글 ──────────────────────────────── -->
  <?php if ($recent): ?>
    <section class="home-board">
      <h2>📋 최근 올라온 글</h2>
      <ul class="post-list">
        <?php foreach ($recent as $p): ?>
          <li>
            <a href="/post/view.php?id=<?= e((string)$p['id']) ?>"><?= e($p['title']) ?></a>
            <span class="tag"><?= e($p['sentiment']) ?></span>
            <a class="post-stat" href="/board/?work=<?= e($p['work']) ?>"><?= e($p['workTitle']) ?></a>
            <span class="post-stat">· <?= e($p['author']) ?> · 💬 <?= (int)$p['comments'] ?></span>
          </li>
        <?php endforeach; ?>
      </ul>
    </section>
  <?php endif; ?>

<?php require __DIR__ . '/includes/footer.php'; ?>
