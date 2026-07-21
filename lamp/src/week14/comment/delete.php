<?php
// ============================================================
// comment/delete.php — 댓글 삭제 처리  [POST 요청 → PRG]
//   글 삭제와 같은 이유로 POST. 처리 후엔 '그 글'로 돌아간다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/comments.php';   // get_comment(), delete_comment()

// ★ 로그인 필수 — 화면에서 버튼을 숨겨도 요청은 조작할 수 있으므로
//   '처리하는 쪽'에서 반드시 다시 확인한다. (안 했으면 로그인 페이지로 보내고 중단)
require_login();

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 값 받기 ───────────────────────────────────────────────
//   comment_id = 지울 댓글 / post_id = 돌아갈 글 (둘 다 hidden으로 옴)
$commentId = post_int('comment_id', 0);
$postId    = post_int('post_id', 0);

// ── 2) 검증 ──────────────────────────────────────────────────
if ($postId <= 0) {
    header('Location: /');
    exit;
}
if ($commentId <= 0) {
    header("Location: /post/view.php?id=$postId");
    exit;
}

// ★ 소유권 확인: 남의 댓글은 지울 수 없다 (화면에서 버튼을 숨겨도 요청은 조작 가능)
$comment = get_comment($commentId);
if ($comment === null || !is_owner($comment['author'])) {
    set_flash('본인이 쓴 댓글만 삭제할 수 있습니다.', 'error');
    header("Location: /post/view.php?id=$postId");
    exit;
}

// ── 3) 삭제 ──────────────────────────────────────────────────
//   임시 보관함(세션)에 '지운 댓글 번호'를 기록해 목록에서 빠지게 한다.
//   나중엔 이 한 줄이 DELETE FROM comments WHERE id = ? 로 바뀐다.
delete_comment($commentId);

// ── 4) PRG: 그 글로 돌아가기 (+삭제 완료 표시) ───────────────
//   알림에 '되돌리기' 버튼을 함께 띄운다 → comment/restore.php 로 POST.
set_flash('🗑 댓글이 삭제되었습니다.', 'ok', [
    'label'  => '되돌리기',
    'url'    => '/comment/restore.php',
    'fields' => ['comment_id' => $commentId, 'post_id' => $postId],
]);
header("Location: /post/view.php?id=$postId");
exit;
