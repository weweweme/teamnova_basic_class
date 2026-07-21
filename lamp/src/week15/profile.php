<?php
// ============================================================
// profile.php — 유저 프로필  [GET 요청]
//   ?user=영화광  →  그 사람이 쓴 글 목록 + 간단한 활동 통계
//   ★ 누구나 볼 수 있는 '공개 프로필'이지만,
//     지금 로그인한 사람 본인이면 '내 프로필'로 표시해준다.
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/auth.php';   // 내 프로필인지 판별하려고
require_once __DIR__ . '/includes/posts.php';

// ── 1) 누구의 프로필인지 받기 ────────────────────────────────
$user = trim(get_str('user'));

if ($user === '') {
    $pageTitle = '프로필';
    require __DIR__ . '/includes/header.php';
    echo '<p>사용자를 지정해 주세요. <a href="/">홈으로</a></p>';
    require __DIR__ . '/includes/footer.php';
    exit;
}

// ── 2) 그 사람 글만 추려서 최신순 정렬 (모듈 재사용) ─────────
$posts = sort_posts(filter_posts_by_author(get_posts(), $user), 'new');

// ── 3) 활동 통계 ─────────────────────────────────────────────
//   array_column($posts, 'views') = 각 글에서 'views' 값만 뽑아 [320, 150, 300] 처럼 만든다.
//     (표에서 '한 열(column)'만 세로로 뽑아내는 느낌이라 이런 이름)
//   array_sum([...])              = 그 배열의 합계.
//   Java로 치면 posts.stream().mapToInt(p -> p.views).sum() 과 같은 일.
$postCount  = count($posts);
$totalViews = array_sum(array_column($posts, 'views'));
$totalLikes = array_sum(array_column($posts, 'likes'));

// 지금 보고 있는 프로필이 '내 것'인가? (로그인한 사람과 이름이 같으면)
$isMe = is_owner($user);

$pageTitle = $isMe ? '내 프로필' : $user . ' 님의 프로필';
require __DIR__ . '/includes/header.php';
?>

  <h1>
    <?= e($user) ?>
    <small><?= $isMe ? '— 내 프로필' : '님의 프로필' ?></small>
  </h1>

  <?php // 내 프로필일 때만 보이는 바로가기 ?>
  <?php if ($isMe): ?>
    <p class="muted">
      내가 쓴 글을 모아 봅니다. <a href="/post/write.php">✏️ 새 글 쓰기</a>
    </p>
  <?php endif; ?>

  <!-- 활동 통계 카드 3개 -->
  <div class="profile-stats">
    <div><strong><?= $postCount ?></strong><span>작성 글</span></div>
    <div><strong><?= $totalViews ?></strong><span>총 조회</span></div>
    <div><strong><?= $totalLikes ?></strong><span>받은 추천</span></div>
  </div>

  <h2>작성한 글</h2>

  <?php if (!$posts): ?>
    <p class="muted">작성한 글이 없습니다.</p>
  <?php else: ?>
    <ul class="post-list">
      <?php foreach ($posts as $p): ?>
        <li>
          <a href="/post/view.php?id=<?= e((string)$p['id']) ?>"><?= e($p['title']) ?></a>
          <span class="tag"><?= e($p['sentiment']) ?></span>
          <!-- 어느 작품 글인지 표시 + 그 게시판으로 바로 갈 수 있게 링크 -->
          <a class="post-stat" href="/board/?work=<?= e($p['work']) ?>"><?= e($p['workTitle']) ?></a>
        </li>
      <?php endforeach; ?>
    </ul>
  <?php endif; ?>

<?php require __DIR__ . '/includes/footer.php'; ?>
