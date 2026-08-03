<?php
// ============================================================
// post/purge.php — 휴지통 글 '지금 영구삭제'  [POST → PRG]
//   소프트삭제(deleted_at)된 내 글을 DB에서 진짜로 DELETE 한다.
//   ★ 되돌릴 수 없는 동작 → 반드시 POST + 소유권 확인.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/posts.php';

require_login();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/trash/');
}

// ── 0-1) 우리 화면에서 온 요청이 맞나? (CSRF) ────────────────
//   남의 사이트가 우리 폼을 흉내 내 쏜 POST를 걸러낸다.
//   ★ POST를 처리하는 파일은 예외 없이 이 줄을 갖는다 — 한 곳이라도 빠지면 그 파일이 통로가 된다.
require_csrf();

$id   = post_int('id', 0);
$post = get_deleted_post($id);   // '지워진 상태'인 글만 (id·author 반환)

// 실제로 휴지통에 있는(지워진) 글이 아니면 되돌린다
if ($id <= 0 || $post === null) {
    redirect('/trash/');
}

// 소유권 확인 — 남의 휴지통 글은 못 지운다 (hard_delete_post도 author_id로 한 번 더 막음)
if (!is_owner($post['author'])) {
    set_flash('본인이 쓴 글만 삭제할 수 있습니다.', 'error');
    redirect('/trash/');
}

hard_delete_post($id, current_user_id());

set_flash('🔥 글을 영구 삭제했습니다.');
redirect('/trash/');
