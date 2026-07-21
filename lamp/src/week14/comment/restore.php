<?php
// ============================================================
// comment/restore.php — 삭제한 댓글 되돌리기  [POST 요청 → PRG]
//   글 되돌리기(post/restore.php)와 완전히 같은 구조.
//   '지운 댓글 번호' 목록에서 그 번호만 빼면 댓글이 돌아온다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/comments.php';

require_login();

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 값 받기 (comment_id = 되돌릴 댓글 / post_id = 돌아갈 글) ──
$commentId = post_int('comment_id', 0);
$postId    = post_int('post_id', 0);

if ($postId <= 0) {
    header('Location: /');
    exit;
}

// ── 2) 검증 + 소유권 ─────────────────────────────────────────
$comment = get_deleted_comment($commentId);
if ($comment === null) {
    header("Location: /post/view.php?id=$postId");
    exit;
}
if (!is_owner($comment['author'])) {
    set_flash('본인이 쓴 댓글만 되돌릴 수 있습니다.', 'error');
    header("Location: /post/view.php?id=$postId");
    exit;
}

// ── 3) 복구 ──────────────────────────────────────────────────
restore_comment($commentId);

// ── 4) PRG ───────────────────────────────────────────────────
set_flash('↩️ 댓글을 되돌렸습니다.');
header("Location: /post/view.php?id=$postId");
exit;
