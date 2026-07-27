<?php
// ============================================================
// media_row.php — '작품 가로 스크롤 줄' 렌더링 조각 (넷플릭스식)
//   홈의 여러 줄이 같은 모양이라, 한 조각으로 만들어 재사용한다.
//   render_media_row(제목, 작품배열) 을 부르면 한 줄이 그려진다.
// ============================================================

// $title  : 줄 제목 (예: "🎬 인기 영화")
// $items  : 작품 배열. 링크용 slug가 있으면 그걸, 없으면(TMDB 결과) tmdb_id로 slug를 만든다.
//           선택적으로 postCount·upPct(우리 커뮤니티 지표).
// $size   : 'lg'(대형 — 우리 커뮤니티) | 'sm'(소형 — TMDB 둘러보기). CSS 클래스로 크기 결정.
function render_media_row(string $title, array $items, string $size = 'sm'): void {
    if (!$items) {
        return;                          // 빈 줄은 아예 안 그린다
    }
    ?>
    <section class="media-row media-row-<?= e($size) ?>">
      <h2><?= e($title) ?></h2>
      <div class="row-scroll">
        <?php foreach ($items as $m): ?>
          <?php // slug가 있으면(우리 DB) 그걸, 없으면(TMDB) tmdb_id로 slug를 만든다 ?>
          <?php $slug = $m['slug'] ?? ('tmdb-' . ($m['tmdb_id'] ?? '')); ?>
          <a class="row-card" href="/board/?work=<?= e($slug) ?>">
            <img class="row-poster" src="<?= e($m['poster_url']) ?>" alt="" loading="lazy">
            <span class="row-title"><?= e($m['title']) ?></span>
            <?php // 우리 커뮤니티 지표 (있을 때만 — TMDB 줄엔 없음) ?>
            <?php if (isset($m['postCount'])): ?>
              <span class="row-meta">
                💬 <?= (int) $m['postCount'] ?>
                <?php if ($m['upPct'] !== null): ?> · 👍 <?= $m['upPct'] ?>%<?php endif; ?>
              </span>
            <?php endif; ?>
          </a>
        <?php endforeach; ?>
      </div>
    </section>
    <?php
}
