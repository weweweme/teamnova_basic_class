<?php
// ============================================================
// report/create.php — 신고 처리  [POST 요청 → PRG]
//   글 보기 화면의 신고 폼이 보낸 '글 번호 + 신고 사유'를 받아
//   (지금은 접수된 셈 치고) 그 글로 다시 리다이렉트한다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/reports.php';   // add_report(), ALLOWED_REASONS

//   ★ 화이트리스트(ALLOWED_REASONS)는 reports 모듈에 있다.
//     폼의 <select>는 화면상 4개뿐이지만, 개발자도구로 값을 바꾸거나 직접 요청을
//     만들면 '아무 값'이나 보낼 수 있으므로, 서버에서 '허용된 것만' 인정한다.

// ★ 로그인 필수 — 화면에서 버튼을 숨겨도 요청은 조작할 수 있으므로
//   '처리하는 쪽'에서 반드시 다시 확인한다. (안 했으면 로그인 페이지로 보내고 중단)
require_login();

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 값 받기 ───────────────────────────────────────────────
$postId = post_int('post_id', 0);
$reason = post_str('reason', '');

// ── 2) 검증 ──────────────────────────────────────────────────
if ($postId <= 0) {
    header('Location: /');
    exit;
}
// 사유가 허용 목록에 없으면 신고 처리하지 않고 그 글로 돌려보낸다.
if (!in_array($reason, ALLOWED_REASONS, true)) {
    header("Location: /post/view.php?id=$postId");
    exit;
}

// ── 3) 저장 ──────────────────────────────────────────────────
//   reports 표에 INSERT. 신고자는 폼이 아니라 세션의 '지금 로그인한 사람'을 쓴다(위조 방지).
//   add_report는 이미 신고한 글이면 false를 준다 → 중복 안내.
$ok = add_report($postId, current_user_id(), $reason);

// ── 4) PRG: 그 글로 다시 리다이렉트 (+결과 표시) ─────────────
if ($ok) {
    set_flash('🚩 신고가 접수되었습니다.');
} else {
    set_flash('이미 신고한 글입니다.', 'error');
}
header("Location: /post/view.php?id=$postId");
exit;
