<?php
// ============================================================
// post/update.php — 글 수정 처리  [POST 요청 → PRG]
//   edit.php 폼이 보낸 값을 받아 (지금은 저장한 셈 치고)
//   그 글 보기 화면으로 리다이렉트한다.
//   ★ 새 글 쓰기(create.php)와 거의 같지만, '어느 글인지(id)'가 함께 온다는 점이 다르다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/posts.php';

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 값 받기 ───────────────────────────────────────────────
$id        = (int)($_POST['id'] ?? 0);
$title     = trim($_POST['title']   ?? '');
$content   = trim($_POST['content'] ?? '');
$sentiment = $_POST['sentiment']    ?? '보통';

// 감상 값은 허용된 것만 (화이트리스트)
if (!in_array($sentiment, ['호평', '보통', '혹평'], true)) {
    $sentiment = '보통';
}

// ── 2) 검증 ──────────────────────────────────────────────────
//   ★ '그 글이 실제로 있는지'까지 확인한다.
//     없는 id를 보내 엉뚱한 걸 수정하려는 시도를 막기 위함.
if ($id <= 0 || get_post($id) === null) {
    header('Location: /');
    exit;
}
// 제목·내용이 비었으면 수정 폼으로 되돌린다.
if ($title === '' || $content === '') {
    header("Location: /post/edit.php?id=$id");
    exit;
}

// ── 3) 저장 (지금은 stub) ────────────────────────────────────
//   나중엔 posts 테이블을 UPDATE 한다 (WHERE id = $id).

// ── 4) PRG: 수정한 글 보기로 리다이렉트 (+완료 표시) ─────────
header("Location: /post/view.php?id=$id&updated=1");
exit;
