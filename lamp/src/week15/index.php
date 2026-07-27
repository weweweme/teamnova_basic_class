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
require_once __DIR__ . '/includes/media_row.php';  // 가로 줄 렌더링 조각

// 각 줄의 데이터 (TMDB 3줄 + 우리 DB 1줄)
$community = get_community_works();   // 우리 DB — 글 달린 작품 + 지표
$trending  = tmdb_trending();        // TMDB — 이번 주 인기작
$movies    = tmdb_popular('movie');  // TMDB — 인기 영화
$tv        = tmdb_popular('tv');     // TMDB — 인기 드라마

$pageTitle = '홈 · 리뷰 커뮤니티';
require __DIR__ . '/includes/header.php';
?>

  <?php // 로그인·글등록 완료 알림은 header.php가 세션에서 꺼내 그린다 (set_flash) ?>

  <?php
    // ── 넷플릭스식 줄들 (조각 재사용) ──────────────────────────
    //   ★ 우리 커뮤니티 줄을 '맨 위'에 — TMDB 포스터에 우리 게 묻히지 않도록.
    //     (글이 하나도 없으면 render_media_row가 알아서 안 그린다)
    render_media_row('💬 우리 커뮤니티에서 이야기 중', $community);
    render_media_row('🔥 이번 주 인기작',              $trending);
    render_media_row('🎬 인기 영화',                   $movies);
    render_media_row('📺 인기 드라마',                 $tv);
  ?>

<?php require __DIR__ . '/includes/footer.php'; ?>
