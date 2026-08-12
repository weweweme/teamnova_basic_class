<?php
// ============================================================
// works.php — 작품 둘러보기  [GET 요청]
//   ?genre=SF / 액션 ...  (메인 축: 장르)
//   ?media=all / movie / tv  (보조 필터)
//   → TMDB에서 그 장르·타입의 인기작을 포스터 그리드로 보여준다.
//   ★ 무한 스크롤: 스크롤 끝에 닿으면 JS가 다음 페이지를 api/browse.php에서
//     받아와 이어붙인다. (여긴 첫 페이지만 서버가 그려준다)
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/tmdb.php';

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

// ── 2) 껍데기만 즉시 — 포스터는 무한스크롤 JS가 '첫 페이지부터' 채운다 ──
//   ★ 무거운 TMDB 호출을 페이지 로딩에서 떼어냈다(홈과 같은 지연 로딩).
//     탭·필터는 TMDB가 필요 없어 즉시 뜨고, 포스터만 스르륵 채워진다.
$mediaTabs = ['all' => '전체', 'movie' => '영화', 'tv' => '드라마', 'anime' => '애니'];

$pageTitle = '작품 둘러보기';
require __DIR__ . '/../includes/header.php';
?>

  <h1 class="wide-title">작품 둘러보기</h1>

  <!-- 메인 축: 장르 탭 -->
  <div class="genre-tabs">
    <a class="<?= $genre === '' ? 'active' : '' ?>"
       href="<?= e(query_url('/works/', ['genre' => ''])) ?>">전체</a>
    <?php foreach ($genres as $name => $codes): ?>
      <a class="<?= $genre === $name ? 'active' : '' ?>"
         href="<?= e(query_url('/works/', ['genre' => $name])) ?>"><?= e($name) ?></a>
    <?php endforeach; ?>
  </div>

  <!-- 보조 필터: 영화 / 드라마 / 전체 -->
  <div class="media-filter">
    <?php foreach ($mediaTabs as $key => $label): ?>
      <a class="<?= $media === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/works/', ['media' => $key])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <!-- 포스터 그리드 — 비어서 시작하고 JS가 첫 페이지부터 채운다.
       data-start-page="0" → JS가 (0+1)=1페이지부터 요청한다. -->
  <div class="poster-grid" id="browse-grid"
       data-genre="<?= e($genre) ?>" data-media="<?= e($media) ?>" data-start-page="0">
    <?php // 로딩 자리표시(스켈레톤) — 첫 페이지가 오면 JS가 걷어낸다 ?>
    <?php for ($i = 0; $i < 12; $i++): ?><span class="row-skeleton"></span><?php endfor; ?>
  </div>
  <!-- 무한 스크롤 감지용 — 이게 화면에 보이면 JS가 다음 페이지를 부른다 -->
  <div id="browse-sentinel" class="browse-sentinel">불러오는 중…</div>

<?php require __DIR__ . '/../includes/footer.php'; ?>
