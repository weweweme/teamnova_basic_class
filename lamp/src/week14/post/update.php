<?php
// ============================================================
// post/update.php — 글 수정 처리  [POST 요청 → PRG]
//   edit.php 폼이 보낸 값을 받아 (지금은 저장한 셈 치고)
//   그 글 보기 화면으로 리다이렉트한다.
//   ★ 새 글 쓰기(create.php)와 거의 같지만, '어느 글인지(id)'가 함께 온다는 점이 다르다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/posts.php';

// ★ 로그인 필수 — 화면에서 버튼을 숨겨도 요청은 조작할 수 있으므로
//   '처리하는 쪽'에서 반드시 다시 확인한다. (안 했으면 로그인 페이지로 보내고 중단)
require_login();

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 값 받기 ───────────────────────────────────────────────
$id        = post_int('id', 0);
$title     = trim(post_str('title'));
$content   = trim(post_str('content'));
$sentiment = post_str('sentiment', '보통');

// 감상 값은 허용된 것만 (화이트리스트)
if (!in_array($sentiment, ['호평', '보통', '혹평'], true)) {
    $sentiment = '보통';
}

// ── 2) 검증 ──────────────────────────────────────────────────
//   ★ '그 글이 실제로 있는지'까지 확인한다.
//     없는 id를 보내 엉뚱한 걸 수정하려는 시도를 막기 위함.
$target = get_post($id);
if ($id <= 0 || $target === null) {
    header('Location: /');
    exit;
}
// ★ 소유권 확인: 남의 글은 수정할 수 없다 (요청 조작 방어)
if (!is_owner($target['author'])) {
    set_flash('본인이 쓴 글만 수정·삭제할 수 있습니다.', 'error');
    header('Location: /post/view.php?id=' . $id);
    exit;
}
// 제목·내용이 비었으면 수정 폼으로 되돌린다.
if ($title === '' || $content === '') {
    header("Location: /post/edit.php?id=$id");
    exit;
}
// 길이 제한도 서버에서 다시 확인 (브라우저 maxlength는 우회 가능)
if (mb_strlen($title) > POST_TITLE_MAX || mb_strlen($content) > POST_CONTENT_MAX) {
    set_flash('제목 또는 내용이 너무 깁니다. 줄여서 다시 시도해 주세요.', 'error');
    header("Location: /post/edit.php?id=$id");
    exit;
}

// ── 3) 저장 ──────────────────────────────────────────────────
//   임시 보관함(세션)에 '수정 내용'을 기록한다.
//   나중엔 이 한 줄이 UPDATE posts SET … WHERE id = ? 로 바뀐다.
update_post($id, $title, $content, $sentiment);

// ── 4) PRG: 수정한 글 보기로 리다이렉트 (+완료 표시) ─────────
set_flash('✏️ 글이 수정되었습니다. (임시 저장 — 브라우저를 닫으면 초기화됩니다)');
header("Location: /post/view.php?id=$id");
exit;
