<?php
// ============================================================
// notifications.php — 알림 도메인 모듈
//   두 종류를 한 표에 담는다:
//     · comment — 내 글에 남이 댓글  → 받는 사람은 '글 주인'
//     · reply   — 내 댓글에 남이 답글 → 받는 사람은 '댓글 주인'
//   받는사람·행위자·종류·글·댓글을 기록하고, 안읽음 개수(상단바 뱃지)와 목록을 조회한다.
// ============================================================
require_once __DIR__ . '/db.php';

// ── 알림 하나 생성 (댓글·답글 저장 직후 호출) ───────────────
//   $recipientId: 받는 사람, $actorId: 댓글·답글을 단 사람
//   $type: 'comment'(글에 댓글) 또는 'reply'(댓글에 답글)
//   ★ 내가 나에게 보내는 알림은 만들지 않는다(부르는 쪽에서도 걸러지지만, 여기서도 방어).
//     내 글에 내가 댓글 달거나, 내 댓글에 내가 답글 다는 경우가 이에 해당한다.
function create_notification(
    int $recipientId,
    int $actorId,
    int $postId,
    int $commentId,
    string $type = 'comment'
): void {
    if ($recipientId === $actorId || $recipientId === 0) {
        return;   // 자기 자신 알림·잘못된 대상은 무시
    }
    $stmt = db()->prepare(
        'INSERT INTO notifications (user_id, actor_id, type, post_id, comment_id) VALUES (?, ?, ?, ?, ?)'
    );
    $stmt->execute([$recipientId, $actorId, $type, $postId, $commentId]);
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
               n.type,                      -- 'comment'(글에 댓글) / 'reply'(내 댓글에 답글)
               n.comment_id,                -- 그 댓글로 바로 가는 앵커(#c12)에 쓴다
               UNIX_TIMESTAMP(n.created_at) AS created,
               a.nickname AS actorNick,     -- 댓글·답글 단 사람(표시 이름)
               p.title    AS postTitle,     -- 어느 글
               -- 그 댓글이 이 글의 몇 번째 '묶음'인가 → 댓글 몇 페이지인지 계산하는 재료.
               --   댓글이 20개를 넘으면 페이지가 나뉘므로, 이게 없으면 알림을 눌러도
               --   1페이지만 열려서 정작 그 댓글(#c12)이 화면에 없다.
               --   ★ 목록을 다 읽고 알림마다 따로 물어보면 알림 30개에 쿼리가 30번 더 나간다(N+1).
               --     그래서 목록을 뽑는 이 한 번의 쿼리 안에서 함께 센다.
               (SELECT COUNT(*) FROM comments r
                 WHERE r.post_id = n.post_id AND r.parent_id IS NULL
                   AND r.id <= COALESCE(c.parent_id, c.id)) AS commentPosition
        FROM notifications n
        JOIN users a ON n.actor_id = a.id
        JOIN posts p ON n.post_id  = p.id AND p.deleted_at IS NULL
        -- 알림이 가리키는 댓글. 답글이면 부모(원댓글)를 기준으로 페이지를 세야 하므로 필요하다.
        LEFT JOIN comments c ON n.comment_id = c.id
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
