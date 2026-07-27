<?php
// ============================================================
// works.php — 작품 둘러보기  [GET 요청]
//   ?genre=영화 / 드라마 로 TMDB 인기작을 포스터 그리드로 보여준다.
//   ★ 데이터는 TMDB(전체 카탈로그). 우리 media 표는 '글 저장 시 참조'로만 쓴다.
//     각 작품을 누르면 그 작품 게시판(/board/?work=tmdb-<id>)으로 간다.
//
//   ※ 파일 이름이 includes/works.php(모듈)와 같지만 폴더가 달라 서로 다른 파일이다.
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/tmdb.php';   // 인기 영화·드라마

// ── 1) 장르 받기 (기본: 영화) + 화이트리스트 ────────────────
$genre = get_str('genre', '영화');
if (!in_array($genre, ['영화', '드라마'], true)) {
    $genre = '영화';
}

// ── 2) TMDB에서 그 장르 인기작 가져오기 ─────────────────────
//   영화면 'movie', 드라마면 'tv' 엔드포인트.
$works = tmdb_popular($genre === '영화' ? 'movie' : 'tv');

$genres = ['영화' => '영화', '드라마' => '드라마'];

$pageTitle = '작품 둘러보기';
require __DIR__ . '/includes/header.php';
?>

  <h1>작품 둘러보기</h1>

  <!-- 장르 탭: ?genre= 만 바꾼다 -->
  <div class="filter-tabs">
    <?php foreach ($genres as $key => $label): ?>
      <a class="<?= $genre === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/works.php', ['genre' => $key])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <?php if (!$works): ?>
    <p class="muted">작품을 불러오지 못했습니다.</p>
  <?php else: ?>
    <!-- 포스터 그리드 (넷플릭스식 카드) -->
    <div class="poster-grid">
      <?php foreach ($works as $w): ?>
        <a class="row-card" href="/board/?work=tmdb-<?= e((string)$w['tmdb_id']) ?>">
          <img class="row-poster" src="<?= e($w['poster_url']) ?>" alt="" loading="lazy">
          <span class="row-title"><?= e($w['title']) ?></span>
          <span class="post-stat"><?= e((string)($w['year'] ?? '')) ?></span>
        </a>
      <?php endforeach; ?>
    </div>
  <?php endif; ?>

<?php require __DIR__ . '/includes/footer.php'; ?>
