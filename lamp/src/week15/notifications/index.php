<?php
// ============================================================
// notifications.php — 내 알림 목록  [GET]
//   ★ 이 목록을 여는 순간 전체를 '읽음'으로 처리한다(사용자가 선택한 방식).
//     단, 화면엔 '방금 전까지 안 읽었던 것'을 강조 표시하려고, 읽음 처리 '전에' 먼저 조회한다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/notifications.php';

require_login();
$userId = current_user_id();

// ① 먼저 목록을 가져온다 (is_read 상태 포함 → 안읽음 강조용)
$notifs = get_notifications($userId);
// ② 그다음 전체 읽음 처리 (다음 방문 땐 뱃지 0)
mark_all_notifications_read($userId);

// 상대 시간 표시용 (이 페이지에서만 쓰는 작은 도우미)
function notif_time_ago(int $ts): string {
    $diff = time() - $ts;
    if ($diff < 60)    return '방금 전';
    if ($diff < 3600)  return (int) floor($diff / 60) . '분 전';
    if ($diff < 86400) return (int) floor($diff / 3600) . '시간 전';
    return (int) floor($diff / 86400) . '일 전';
}

$pageTitle = '알림';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>🔔 알림</h1>

  <?php if (!$notifs): ?>
    <p class="muted">아직 알림이 없습니다. 내 글에 댓글이 달리거나 내 댓글에 답글이 달리면 여기에 표시돼요.</p>
  <?php else: ?>
    <ul class="notif-list">
      <?php foreach ($notifs as $n): ?>
        <?php
        // 알림 종류에 따라 문구가 달라진다. 한 표에 두 종류가 섞여 있으므로
        // type을 보고 고르지 않으면 답글 알림에도 "글에 댓글을 남겼어요"가 나온다.
        $isReply = $n['type'] === 'reply';
        // 그 댓글 자리로 바로 이동한다 (#c12) — 댓글이 많은 글에서 찾아 헤매지 않게.
        $link = '/post/view.php?id=' . (int)$n['post_id'] . '#c' . (int)$n['comment_id'];
        ?>
        <?php // 방금 전까지 안 읽었던 알림은 파란 점으로 강조 ?>
        <li class="notif-item <?= $n['is_read'] ? '' : 'notif-unread' ?>">
          <a href="<?= e($link) ?>">
            <span class="notif-text">
              <?php if ($isReply): ?>
                ↳ <strong><?= e($n['actorNick']) ?></strong>님이
                <strong><?= e($n['postTitle']) ?></strong> 글에서 내 댓글에 답글을 남겼어요
              <?php else: ?>
                💬 <strong><?= e($n['actorNick']) ?></strong>님이
                <strong><?= e($n['postTitle']) ?></strong> 글에 댓글을 남겼어요
              <?php endif; ?>
            </span>
            <span class="notif-time"><?= notif_time_ago((int)$n['created']) ?></span>
          </a>
        </li>
      <?php endforeach; ?>
    </ul>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
