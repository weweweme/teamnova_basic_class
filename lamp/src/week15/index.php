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

// ── 사이드바 데이터 ──────────────────────────────────────────
//   B) 지금 뜨는 글 = 조회수 순 상위 4개 (우리 커뮤니티 = 우리 정체성)
$hotPosts = paginate_posts(sort_posts(get_posts(), 'views'), 1, 4);
//   D) 오늘의 발견 = 인기작 중 하나 (매 요청 무작위면 산만하니, 인기작 앞쪽에서 하나)
$pickPool = $trending ?: $movies;
$pick     = $pickPool[array_rand($pickPool)] ?? null;   // 새로고침마다 바뀜(발견의 재미)

$pageTitle = '홈 · 리뷰 커뮤니티';
require __DIR__ . '/includes/header.php';
?>

  <?php // 로그인·글등록 완료 알림은 header.php가 세션에서 꺼내 그린다 (set_flash) ?>

  <?php // ── 히어로 영역: 넓으면 [히어로 | 사이드바] 2단, 좁으면 세로 1단 ── ?>
  <?php if ($hero && !empty($hero['backdrop_url'])): ?>
    <div class="hero-area">
      <!-- 왼쪽: 큰 히어로 배너 -->
      <a class="hero" href="/board/?work=<?= e($hero['slug']) ?>"
         style="background-image: url('<?= e($hero['backdrop_url']) ?>')">
        <div class="hero-inner">
          <h1 class="hero-title"><?= e($hero['title']) ?></h1>
          <p class="hero-desc"><?= e(mb_substr($hero['overview'] ?? '', 0, 110)) ?>…</p>
          <span class="hero-cta">리뷰 보러 가기 →</span>
        </div>
      </a>

      <!-- 오른쪽: 사이드바 (넓은 화면에서만 옆에, 좁으면 아래로) -->
      <aside class="hero-side">
        <?php // B) 지금 뜨는 글 (조회순) — 우리 커뮤니티 ?>
        <?php if ($hotPosts): ?>
          <section class="side-box">
            <h3>🔥 지금 뜨는 글</h3>
            <ol class="side-hot">
              <?php foreach ($hotPosts as $p): ?>
                <li>
                  <a href="/post/view.php?id=<?= e((string)$p['id']) ?>"><?= e($p['title']) ?></a>
                  <span class="side-meta"><?= e($p['workTitle']) ?> · 조회 <?= (int)$p['views'] ?></span>
                </li>
              <?php endforeach; ?>
            </ol>
          </section>
        <?php endif; ?>

        <?php // D) 오늘의 발견 — 랜덤 작품 하나 ?>
        <?php if ($pick): ?>
          <section class="side-box">
            <h3>🎲 오늘의 발견</h3>
            <a class="side-pick" href="/board/?work=tmdb-<?= e((string)$pick['tmdb_id']) ?>">
              <img src="<?= e($pick['poster_url']) ?>" alt="" loading="lazy">
              <span class="side-pick-title"><?= e($pick['title']) ?></span>
            </a>
          </section>
        <?php endif; ?>
      </aside>
    </div>
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
