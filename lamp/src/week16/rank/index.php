<?php
// ============================================================
// rank.php — 랭킹(리더보드)  [GET 요청]
//   ?tab=works(작품) | users(유저) | posts(글)
//   → 여러 표를 집계해 "제일 활발한 것"을 순위로 보여준다.
//     매 요청 DB를 새로 세니 방문할 때마다 자동 갱신된다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/works.php';    // get_community_works (작품 랭킹 재사용)
require_once __DIR__ . '/../includes/posts.php';    // get_posts·sort_posts (글 랭킹 재사용)
require_once __DIR__ . '/../includes/ranking.php';  // rank_users (유저 랭킹)

// ── 탭 받기 + 검증 (화이트리스트) ────────────────────────────
$tab = get_str('tab', 'works');
if (!in_array($tab, ['works', 'users', 'posts'], true)) {
    $tab = 'works';
}

$rankLimit = 10;   // 각 랭킹 상위 10개

// 1~3위는 메달, 나머지는 숫자. (매직값 대신 이름 붙인 표로)
$medals = [1 => '🥇', 2 => '🥈', 3 => '🥉'];

$tabs = ['works' => '🎬 인기 작품', 'users' => '👑 명예의 전당', 'posts' => '🔥 화제의 글'];

$pageTitle = '랭킹';
$containerClass = 'narrow';   // 세로 순위 목록이라 좁은 중앙 컬럼이 읽기 좋다
require __DIR__ . '/../includes/header.php';
?>

  <h1>🏆 랭킹</h1>

  <!-- 탭: ?tab= 만 바꾼다 -->
  <div class="rank-tabs">
    <?php foreach ($tabs as $key => $label): ?>
      <a class="<?= $tab === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/rank/', ['tab' => $key])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <?php // ── 작품 랭킹: 글 많은(핫한) 순 — get_community_works 재사용 ── ?>
  <?php if ($tab === 'works'): ?>
    <?php $rows = get_community_works($rankLimit); ?>
    <?php if (!$rows): ?>
      <p class="muted">아직 글이 달린 작품이 없습니다.</p>
    <?php else: ?>
      <ol class="rank-list">
        <?php foreach ($rows as $i => $w): $rank = $i + 1; ?>
          <li class="rank-item">
            <span class="rank-num rank-<?= $rank ?>"><?= $medals[$rank] ?? $rank ?></span>
            <a class="rank-body" href="/board/?work=<?= e($w['slug']) ?>">
              <img class="rank-poster" src="<?= e($w['poster_url']) ?>" alt="" loading="lazy">
              <span class="rank-info">
                <strong><?= e($w['title']) ?></strong>
                <span class="rank-meta">💬 글 <?= (int)$w['postCount'] ?>개<?php
                  if ($w['upPct'] !== null): ?> · 👍 추천 <?= (int)$w['upPct'] ?>%<?php endif; ?></span>
              </span>
            </a>
          </li>
        <?php endforeach; ?>
      </ol>
    <?php endif; ?>

  <?php // ── 유저 랭킹: 받은 추천 순 — rank_users (users↔posts↔likes JOIN) ── ?>
  <?php elseif ($tab === 'users'): ?>
    <?php $rows = rank_users($rankLimit); ?>
    <?php if (!$rows): ?>
      <p class="muted">아직 글을 쓴 유저가 없습니다.</p>
    <?php else: ?>
      <ol class="rank-list">
        <?php foreach ($rows as $i => $u): $rank = $i + 1; ?>
          <li class="rank-item">
            <span class="rank-num rank-<?= $rank ?>"><?= $medals[$rank] ?? $rank ?></span>
            <a class="rank-body" href="/profile/?user=<?= urlencode($u['username']) ?>">
              <?php if (!empty($u['avatar'])): ?>
                <img class="rank-avatar" src="<?= e($u['avatar']) ?>" alt="">
              <?php else: ?>
                <span class="rank-avatar rank-avatar-empty"><?= e(mb_substr($u['nickname'], 0, 1)) ?></span>
              <?php endif; ?>
              <span class="rank-info">
                <strong><?= level_badge_html((int)$u['postCount']) ?> <?= e($u['nickname']) ?></strong>
                <span class="rank-meta">👍 받은 추천 <?= (int)$u['likesReceived'] ?> · ✍️ 글 <?= (int)$u['postCount'] ?></span>
              </span>
            </a>
          </li>
        <?php endforeach; ?>
      </ol>
    <?php endif; ?>

  <?php // ── 글 랭킹: 조회+댓글 가중(hot) 순 — sort_posts 재사용 ── ?>
  <?php else: ?>
    <?php $rows = array_slice(sort_posts(get_posts(), 'hot'), 0, $rankLimit); ?>
    <?php if (!$rows): ?>
      <p class="muted">아직 글이 없습니다.</p>
    <?php else: ?>
      <ol class="rank-list">
        <?php foreach ($rows as $i => $p): $rank = $i + 1; ?>
          <li class="rank-item">
            <span class="rank-num rank-<?= $rank ?>"><?= $medals[$rank] ?? $rank ?></span>
            <a class="rank-body rank-body-post" href="/post/view.php?id=<?= e((string)$p['id']) ?>">
              <span class="rank-info">
                <strong><?= e($p['title']) ?> <span class="tag"><?= e($p['sentiment']) ?></span></strong>
                <span class="rank-meta"><?= e($p['workTitle']) ?> · <?= level_badge_html((int)$p['authorPostCount']) ?> <?= e($p['authorNick']) ?>
                  · 👁 <?= (int)$p['views'] ?> · 💬 <?= (int)$p['comments'] ?> · 👍 <?= (int)$p['likes'] ?></span>
              </span>
            </a>
          </li>
        <?php endforeach; ?>
      </ol>
    <?php endif; ?>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
