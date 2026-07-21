<?php
// ============================================================
// post/restore.php — 삭제한 글 되돌리기  [POST 요청 → PRG]
//   삭제 알림에 함께 뜨는 '되돌리기' 버튼이 여기로 POST를 보낸다.
//
//   ★ 왜 되돌릴 수 있나?
//     delete_post()는 글을 진짜로 없애지 않고, '지운 글 번호' 목록에 번호만 적어둔다.
//     그래서 그 번호만 빼면 원본이 그대로 돌아온다.
//     (실무 DB도 DELETE 대신 deleted_at 칼럼을 채우는 '소프트 삭제'를 자주 쓴다)
//
//   ★ 되돌리기도 '상태를 바꾸는' 동작이므로 링크(GET)가 아니라 POST다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/posts.php';

require_login();

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 값 받기 + 대상 찾기 ───────────────────────────────────
$id   = post_int('id', 0);
$post = get_deleted_post($id);   // '지워진 상태'인 글만 돌려준다

// ── 2) 검증: 정말 지워진 글인가 ──────────────────────────────
//   (이미 살아있는 글이거나 없는 번호면 되돌릴 것이 없다)
if ($id <= 0 || $post === null) {
    header('Location: /');
    exit;
}

// ★ 소유권 확인: 남의 글을 마음대로 되살릴 수 없다.
//   삭제와 똑같이, 되돌리기도 서버에서 주인을 다시 확인한다.
if (!is_owner($post['author'])) {
    set_flash('본인이 쓴 글만 되돌릴 수 있습니다.', 'error');
    header('Location: /');
    exit;
}

// ── 3) 복구 ──────────────────────────────────────────────────
restore_post($id);

// ── 4) PRG: 되살아난 글로 바로 이동 ──────────────────────────
set_flash('↩️ 글을 되돌렸습니다.');
header('Location: /post/view.php?id=' . $id);
exit;
