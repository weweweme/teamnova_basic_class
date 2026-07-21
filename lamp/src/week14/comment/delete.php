<?php
// ============================================================
// comment/delete.php — 댓글 삭제 처리  [POST 요청 → PRG]
//   글 삭제와 같은 이유로 POST. 처리 후엔 '그 글'로 돌아간다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

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
$commentId = (int)($_POST['comment_id'] ?? 0);
$postId    = (int)($_POST['post_id']    ?? 0);

// ── 2) 검증 ──────────────────────────────────────────────────
if ($postId <= 0) {
    header('Location: /');
    exit;
}
if ($commentId <= 0) {
    header("Location: /post/view.php?id=$postId");
    exit;
}

// ── 3) 삭제 (지금은 stub) ────────────────────────────────────
//   나중엔 comments 테이블에서 DELETE (WHERE id = $commentId).

// ── 4) PRG: 그 글로 돌아가기 (+삭제 완료 표시) ───────────────
header("Location: /post/view.php?id=$postId&cdeleted=1");
exit;
