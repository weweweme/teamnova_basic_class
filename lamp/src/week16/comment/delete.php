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
    redirect('/');
}

// ── 1) 값 받기 ───────────────────────────────────────────────
//   comment_id = 지울 댓글 / post_id = 돌아갈 글 (둘 다 hidden으로 옴)
$commentId = post_int('comment_id', 0);
$postId    = post_int('post_id', 0);

// ── 2) 검증 ──────────────────────────────────────────────────
if ($postId <= 0) {
    redirect('/');
}
if ($commentId <= 0) {
    redirect('/post/view.php', ['id' => $postId]);
}

// ★ 소유권 확인: 남의 댓글은 지울 수 없다 (화면에서 버튼을 숨겨도 요청은 조작 가능)
$comment = get_comment($commentId);
if ($comment === null || !is_owner($comment['author'])) {
    set_flash('본인이 쓴 댓글만 삭제할 수 있습니다.', 'error');
    redirect('/post/view.php', ['id' => $postId]);
}

// ── 3) 삭제 ──────────────────────────────────────────────────
//   소프트삭제 — UPDATE comments SET deleted_at = NOW() WHERE id = ?
//   글과 마찬가지로 행은 남기고 '지운 시각'만 찍는다 → 되돌리기가 가능하다.
delete_comment($commentId);

// ── 4) PRG: 그 글로 돌아가기 (+삭제 완료 표시) ───────────────
//   ★ 글과 달리 댓글에는 '되돌리기'를 두지 않는다.
//     지운 댓글은 "삭제된 댓글입니다"로 자리가 남아 대화 흐름이 깨지지 않고,
//     짧은 한 줄짜리 글이라 되돌릴 일이 드물어서다. (글은 휴지통에서 30일간 되돌릴 수 있다)
set_flash('🗑 댓글이 삭제되었습니다.');

// ★ 여기만 header()를 직접 쓰고 있었다 → redirect()로 통일한다.
//   직접 쓰면 신원(?as=)도 알림도 안 붙어서, 댓글을 지우는 순간 로그아웃됐다.
//   지운 댓글도 "삭제된 댓글입니다"로 자리가 남으므로 페이지 위치는 그대로다 → 그 자리로 돌아간다.
redirect('/post/view.php', [
    'id'    => $postId,
    'cpage' => comment_page_param(find_comment_page($commentId)),
]);
