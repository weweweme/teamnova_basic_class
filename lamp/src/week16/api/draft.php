<?php
// ============================================================
// api/draft.php — 글쓰기 초안 자동 저장  [POST → JSON]
//   글쓰기 폼의 JS가 입력이 멈출 때마다 여기로 보낸다. 세션에 담아두고,
//   폼을 다시 열면 그 값으로 채워진다. (includes/drafts.php)
//
//   요청:  work=tmdb-496243 & title=… & content=… & sentiment=호평 & _token=…
//   응답:  { "ok": true, "at": "02:41" }
//
//   ★ 화면을 새로 그리지 않는 요청이라 JSON으로 답한다 — api/row.php와 같은 규칙.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/drafts.php';

header('Content-Type: application/json; charset=utf-8');

// ── 0) 로그인한 사람만 ───────────────────────────────────────
//   ★ require_login()을 쓰지 않는다. 그건 로그인 화면으로 '리다이렉트'하는데,
//     JS가 부르는 창구에서는 HTML 로그인 페이지가 돌아와 봐야 쓸 데가 없다.
//     화면 이동이 없는 요청에는 상태 코드로 답하는 게 맞다.
if (!is_logged_in()) {
    http_response_code(401);                 // 401 = 로그인 안 됨
    echo json_encode(['ok' => false]);
    exit;
}

// ── 1) POST만 ───────────────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);                 // 405 = 이 주소에 허용되지 않은 방식
    echo json_encode(['ok' => false]);
    exit;
}

// ── 2) 우리 화면에서 온 요청이 맞나 (CSRF) ───────────────────
//   ★ POST면 예외 없이 검사한다. 다만 require_csrf()는 실패 시 리다이렉트하므로
//     여기서는 직접 대조하고 상태 코드로 답한다.
//   ★ 남의 사이트가 이 창구로 쏴 봐야 남의 세션 초안을 덮어쓸 수 없게 막는 것.
if (!hash_equals($_SESSION[SESSION_CSRF] ?? '', post_str(CSRF_FIELD))) {
    http_response_code(403);                 // 403 = 확인 안 된 요청
    echo json_encode(['ok' => false]);
    exit;
}

// ── 3) 저장 ─────────────────────────────────────────────────
save_draft(post_str('work'), [
    'title'     => post_str('title'),
    'content'   => post_str('content'),
    'sentiment' => post_str('sentiment'),
]);

// 언제 저장됐는지 화면에 표시해 주려고 시각을 함께 돌려준다.
//   ★ 서버 시각을 쓰는 이유: 사용자 PC 시계가 틀어져 있어도 '서버가 받은 시각'이 사실이다.
echo json_encode(['ok' => true, 'at' => date('H:i:s')]);
