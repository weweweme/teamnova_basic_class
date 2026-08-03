<?php
// ============================================================
// comment/update.php — 댓글 수정 처리  [POST 요청 → PRG]
//   view.php의 수정 폼(?edit=<번호>로 열린 것)이 보낸 값을 받아 저장하고,
//   '그 글'로 다시 돌아간다. post/update.php와 같은 흐름의 댓글판.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/comments.php';   // get_comment(), update_comment(), COMMENT_MAX

// ★ 로그인 필수 — 화면에서 버튼을 숨겨도 요청은 조작할 수 있으므로
//   '처리하는 쪽'에서 반드시 다시 확인한다.
require_login();

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
//   수정은 '서버의 상태를 바꾸는' 동작이라 GET으로는 받지 않는다.
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/');
}

// ── 1) 값 받기 ───────────────────────────────────────────────
//   comment_id = 고칠 댓글 / post_id = 돌아갈 글 (둘 다 hidden으로 옴)
$commentId = post_int('comment_id', 0);
$postId    = post_int('post_id', 0);
$content   = trim(post_str('content'));

// ── 2) 검증 ──────────────────────────────────────────────────
if ($postId <= 0) {
    redirect('/');
}
if ($commentId <= 0) {
    redirect('/post/view.php', ['id' => $postId]);
}

// ★ 소유권 확인: 남의 댓글은 고칠 수 없다.
//   get_comment()는 '지워진 댓글'을 주지 않으므로, 삭제된 댓글 수정도 여기서 함께 막힌다.
$comment = get_comment($commentId);
if ($comment === null || !is_owner($comment['author'])) {
    set_flash('본인이 쓴 댓글만 수정할 수 있습니다.', 'error');
    redirect('/post/view.php', ['id' => $postId]);
}

// 내용이 비었거나 너무 길면 '수정 폼을 다시 열어둔 채로' 돌려보낸다.
//   ★ ['edit' => $commentId] 를 붙이는 게 핵심 — 안 붙이면 폼이 닫혀서
//     사용자가 [수정]을 다시 눌러야 한다. 고치던 자리에 그대로 남겨주는 게 자연스럽다.
//   cpage도 함께 실어야 한다 — 그 댓글이 2페이지에 있으면 1페이지로 보내봐야 폼이 안 보인다.
if ($content === '' || mb_strlen($content) > COMMENT_MAX) {
    set_flash('댓글 내용을 확인해 주세요. (1~' . COMMENT_MAX . '자)', 'error');
    redirect('/post/view.php', [
        'id'    => $postId,
        'edit'  => $commentId,
        'cpage' => comment_page_param(find_comment_page($commentId)),
    ]);
}

// ── 3) 저장 ──────────────────────────────────────────────────
//   UPDATE comments SET content = ?, edited_at = NOW() WHERE id = ?
update_comment($commentId, $content);

// ── 4) PRG: 그 글로 돌아가기 (+수정 완료 표시) ───────────────
set_flash('✏️ 댓글이 수정되었습니다.');
redirect('/post/view.php', [
    'id'    => $postId,
    'cpage' => comment_page_param(find_comment_page($commentId)),
]);
