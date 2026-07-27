<?php
// ============================================================
// works.php — 작품 둘러보기  [GET 요청]
//   ?genre=SF / 액션 ...  (메인 축: 장르)
//   ?media=all / movie / tv  (보조 필터)
//   → TMDB에서 그 장르·타입의 인기작을 포스터 그리드로 보여준다.
//   ★ 무한 스크롤: 스크롤 끝에 닿으면 JS가 다음 페이지를 api/browse.php에서
//     받아와 이어붙인다. (여긴 첫 페이지만 서버가 그려준다)
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/tmdb.php';

// ── 1) 장르·타입 받기 + 검증 ─────────────────────────────────
$genres = tmdb_genres();                        // ['액션'=>..., 'SF'=>..., ...]
$genre  = get_str('genre', '');                 // '' = 전체(인기작)
if ($genre !== '' && !isset($genres[$genre])) {
    $genre = '';                                // 이상한 값이면 전체로
}
$media  = get_str('media', 'all');              // all | movie | tv | anime
if (!in_array($media, ['all', 'movie', 'tv', 'anime'], true)) {
    $media = 'all';
}

// ── 2) 첫 페이지 작품 (나머지는 무한 스크롤로) ──────────────
$works = discover_by_genre($genre, $media, 1);

$mediaTabs = ['all' => '전체', 'movie' => '영화', 'tv' => '드라마', 'anime' => '애니'];

$pageTitle = '작품 둘러보기';
require __DIR__ . '/includes/header.php';
?>

  <h1 class="wide-title">작품 둘러보기</h1>

  <!-- 메인 축: 장르 탭 -->
  <div class="genre-tabs">
    <a class="<?= $genre === '' ? 'active' : '' ?>"
       href="<?= e(query_url('/works.php', ['genre' => ''])) ?>">전체</a>
    <?php foreach ($genres as $name => $codes): ?>
      <a class="<?= $genre === $name ? 'active' : '' ?>"
         href="<?= e(query_url('/works.php', ['genre' => $name])) ?>"><?= e($name) ?></a>
    <?php endforeach; ?>
  </div>

  <!-- 보조 필터: 영화 / 드라마 / 전체 -->
  <div class="media-filter">
    <?php foreach ($mediaTabs as $key => $label): ?>
      <a class="<?= $media === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/works.php', ['media' => $key])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <!-- 포스터 그리드 — JS가 이 아래에 다음 페이지를 이어붙인다 -->
  <?php if (!$works): ?>
    <p class="muted">작품을 불러오지 못했습니다.</p>
  <?php else: ?>
    <div class="poster-grid" id="browse-grid"
         data-genre="<?= e($genre) ?>" data-media="<?= e($media) ?>">
      <?php foreach ($works as $w): ?>
        <a class="row-card" href="/board/?work=tmdb-<?= e((string)$w['tmdb_id']) ?>">
          <img class="row-poster" src="<?= e($w['poster_url']) ?>" alt="" loading="lazy">
          <span class="row-title"><?= e($w['title']) ?></span>
          <span class="post-stat"><?= e((string)($w['year'] ?? '')) ?></span>
        </a>
      <?php endforeach; ?>
    </div>
    <!-- 무한 스크롤 감지용 — 이게 화면에 보이면 JS가 다음 페이지를 부른다 -->
    <div id="browse-sentinel" class="browse-sentinel">불러오는 중…</div>
  <?php endif; ?>

<?php require __DIR__ . '/includes/footer.php'; ?>
