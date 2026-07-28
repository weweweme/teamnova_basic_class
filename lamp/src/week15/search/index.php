<?php
// ============================================================
// search.php — 작품 검색  [GET 요청]
//   ?q=기생  → TMDB(영화·드라마 API)에서 실시간 검색 → 목록 표시.
//   클릭하면 그 작품 게시판으로 이동 (/board/?work=tmdb-<id>).
//
//   ★ week14와 달라진 점: 더미 데이터(works.php)가 아니라 진짜 TMDB에서 가져온다.
//     검색 결과는 아직 우리 DB에 저장되지 않는다 — 누군가 글을 쓰는 순간에만
//     media 표에 저장된다(ensure_media). 즉 검색은 'TMDB 실시간 조회'일 뿐.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/tmdb.php';   // TMDB 검색 모듈

// ── 1) 검색어 받기 ───────────────────────────────────────────
$q = mb_substr(trim(get_str('q')), 0, 50);

// ── 2) 검색어가 있을 때만 TMDB에 물어본다 ────────────────────
//   검색 전 / 결과 없음 / 결과 있음 — 세 상태를 아래에서 구분해 안내한다.
$hasQuery = $q !== '';
$works    = $hasQuery ? search_tmdb($q) : [];   // TMDB 실시간 검색

$pageTitle = $q === '' ? '작품 검색' : "'{$q}' 검색결과";
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>작품 검색</h1>

  <!-- 검색 폼은 method="get" — 검색어가 주소에 붙어 공유·북마크 가능 (/search/?q=기생) -->
  <form class="search-form" method="get" action="/search/">
    <input type="text" name="q" value="<?= e($q) ?>" placeholder="영화·드라마 제목 (예: 기생충, 인셉션)">
    <button type="submit">검색</button>
  </form>

  <?php // 상태가 셋이라 3갈래로 안내: ① 검색 전 ② 결과 없음 ③ 결과 있음 ?>
  <?php if (!$hasQuery): ?>
    <p class="muted">영화·드라마 제목으로 검색해 보세요. (예: 기생충, 인셉션)</p>

  <?php elseif (!$works): ?>
    <p class="muted">'<?= e($q) ?>'와 일치하는 작품이 없습니다.</p>

  <?php else: ?>
    <p class="muted">'<?= e($q) ?>' 검색결과 <?= count($works) ?>개</p>
    <ul class="media-list">
      <?php foreach ($works as $w): ?>
        <?php // TMDB엔 우리 slug가 없으므로 'tmdb-<번호>'로 링크. 글 쓰는 순간 이 slug로 저장된다. ?>
        <li>
          <a href="/board/?work=tmdb-<?= e((string)$w['tmdb_id']) ?>">
            <?php if ($w['poster_url'] !== ''): ?>
              <!-- 포스터. loading=lazy = 화면에 보일 때만 이미지를 받아 초기 로딩을 가볍게 -->
              <img class="poster" src="<?= e($w['poster_url']) ?>" alt="" loading="lazy">
            <?php else: ?>
              <span class="poster poster-empty">No Image</span>
            <?php endif; ?>
            <span class="media-info">
              <strong><?= create_highlighted($w['title'], $q) ?></strong>
              <span class="post-stat"><?= e($w['genre']) ?> · <?= e((string)($w['year'] ?? '')) ?></span>
            </span>
          </a>
        </li>
      <?php endforeach; ?>
    </ul>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
