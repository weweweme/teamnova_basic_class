<?php
// ============================================================
// notifications.php — 알림 도메인 모듈
//   지금은 '내 글에 남이 댓글' 한 종류. 받는사람·행위자·글·댓글을 기록하고,
//   안읽음 개수(상단바 뱃지)와 목록을 조회한다.
// ============================================================
require_once __DIR__ . '/db.php';

// ── 알림 하나 생성 (댓글 저장 직후 호출) ────────────────────
//   $recipientId: 받는 사람(글 작성자), $actorId: 댓글 단 사람
//   ★ 내 글에 내가 댓글 단 경우는 알림을 만들지 않는다(호출 전에 확인하지만, 여기서도 방어).
function create_notification(int $recipientId, int $actorId, int $postId, int $commentId): void {
    if ($recipientId === $actorId || $recipientId === 0) {
        return;   // 자기 자신 알림·잘못된 대상은 무시
    }
    $stmt = db()->prepare(
        'INSERT INTO notifications (user_id, actor_id, post_id, comment_id) VALUES (?, ?, ?, ?)'
    );
    $stmt->execute([$recipientId, $actorId, $postId, $commentId]);
}

// ── 안 읽은 알림 개수 (상단바 🔔 뱃지용) ────────────────────
function count_unread_notifications(int $userId): int {
    if ($userId === 0) {
        return 0;
    }
    return (int) db_scalar(
        'SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0',
        [$userId]
    );
}

// ── 알림 목록 (누가·어느 글에 댓글 달았는지 이름·제목까지) ──
//   지워진(소프트삭제) 글의 알림은 뺀다 → 클릭해도 없는 글로 안 가게.
function get_notifications(int $userId, int $limit = 30): array {
    $sql = "
        SELECT n.id, n.post_id, n.is_read,
               UNIX_TIMESTAMP(n.created_at) AS created,
               a.nickname AS actorNick,     -- 댓글 단 사람(표시 이름)
               p.title    AS postTitle      -- 어느 글
        FROM notifications n
        JOIN users a ON n.actor_id = a.id
        JOIN posts p ON n.post_id  = p.id AND p.deleted_at IS NULL
        WHERE n.user_id = ?
        ORDER BY n.id DESC
        LIMIT " . (int) $limit . "
    ";
    $stmt = db()->prepare($sql);
    $stmt->execute([$userId]);
    return $stmt->fetchAll();
}

// ── 이 사람의 알림을 전부 '읽음'으로 (목록을 여는 순간 호출) ──
function mark_all_notifications_read(int $userId): void {
    if ($userId === 0) {
        return;
    }
    db()->prepare('UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0')
        ->execute([$userId]);
}
