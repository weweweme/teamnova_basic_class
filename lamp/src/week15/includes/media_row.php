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

// ── 지연 로딩용 '빈 가로줄' (스켈레톤) ─────────────────────
//   서버는 이 껍데기만 즉시 보내고, 실제 포스터는 JS(main.js)가 api/row.php에서
//   받아와 .row-scroll 안을 채운다. data-kind로 '어떤 줄'인지 JS에 알려준다.
//   ★ 무거운 TMDB 호출을 페이지 로딩에서 떼어내 홈이 즉시 뜨게 하는 장치.
function render_lazy_row(string $title, string $kind): void {
    ?>
    <section class="media-row media-row-sm lazy-row" data-kind="<?= e($kind) ?>">
      <h2><?= e($title) ?></h2>
      <div class="row-scroll">
        <?php // 로딩 중 자리표시(회색 네모) — 채워지면 JS가 이 자리를 실제 카드로 갈아끼운다 ?>
        <?php for ($i = 0; $i < 8; $i++): ?><span class="row-skeleton"></span><?php endfor; ?>
      </div>
    </section>
    <?php
}
