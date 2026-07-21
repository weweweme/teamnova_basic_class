<?php
// ============================================================
// like/toggle.php — 좋아요(추천) 처리  [POST 요청 → PRG]
//   글 보기 화면의 추천 버튼이 POST로 보낸 값을 받아
//   (지금은 눌린 셈 치고) 그 글로 다시 리다이렉트한다.
//
//   ★ 왜 링크(GET)가 아니라 POST인가?
//     추천은 '서버의 상태를 바꾸는' 동작이다. GET은 '그냥 조회'라는 약속이라,
//     GET 링크로 만들면 브라우저·크롤러가 미리 훑기만 해도 추천이 눌려버릴 수 있다.
//     상태를 바꾸는 건 POST — 우리가 정한 규칙(조회=GET / 변경=POST) 그대로.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/posts.php';   // toggle_like()

// ★ 로그인 필수 — 화면에서 버튼을 숨겨도 요청은 조작할 수 있으므로
//   '처리하는 쪽'에서 반드시 다시 확인한다. (안 했으면 로그인 페이지로 보내고 중단)
require_login();

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 어느 글에 대한 추천인지 받기 (폼의 hidden으로 옴) ─────
$postId = post_int('post_id', 0);

// ── 2) 검증 ──────────────────────────────────────────────────
if ($postId <= 0) {
    header('Location: /');
    exit;
}

// ── 3) 저장 ──────────────────────────────────────────────────
//   ★ '토글' — 이미 추천했으면 취소, 아니면 추천. (1인 1회)
//   나중 DB에선: likes 테이블에 (user_id, post_id)가 있으면 DELETE, 없으면 INSERT.
toggle_like($postId);

// ── 4) PRG: 그 글로 다시 리다이렉트 (+추천 완료 표시) ────────
header("Location: /post/view.php?id=$postId&liked=1");
exit;
