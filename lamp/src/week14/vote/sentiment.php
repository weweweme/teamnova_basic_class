<?php
// ============================================================
// vote/sentiment.php — 작품 추천/비추천 투표 처리  [POST 요청 → PRG]
//   게시판의 '추천 / 비추천' 버튼이 보낸 값을 받아 (지금은 집계된 셈 치고)
//   그 작품 게시판으로 다시 리다이렉트한다.
//
//   ★ 지금까지의 POST는 '글'에 대한 것이었지만(추천·신고·댓글),
//     이건 '작품'에 대한 투표라 post_id가 아니라 work를 받는다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/works.php';   // set_vote()

// 허용된 선택지 (화이트리스트 — 폼을 조작해 아무 값이나 보낼 수 있으므로)
const ALLOWED_CHOICES = ['추천', '비추천'];

// ★ 로그인 필수 — 화면에서 버튼을 숨겨도 요청은 조작할 수 있으므로
//   '처리하는 쪽'에서 반드시 다시 확인한다. (안 했으면 로그인 페이지로 보내고 중단)
require_login();

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 값 받기 ───────────────────────────────────────────────
//   choice = 어느 버튼을 눌렀는지. (버튼에 name/value를 달아두면 그 값이 전송된다)
$work   = post_str('work', '');
$choice = post_str('choice', '');

// ── 2) 검증 ──────────────────────────────────────────────────
if ($work === '') {
    header('Location: /');
    exit;
}
if (!in_array($choice, ALLOWED_CHOICES, true)) {
    // 이상한 선택지면 투표하지 않고 그 게시판으로 되돌린다.
    //   urlencode() = 값을 주소에 안전하게 넣기 위해 특수문자·한글을 변환.
    header('Location: /board/?work=' . urlencode($work));
    exit;
}

// ── 3) 저장 ──────────────────────────────────────────────────
//   ★ '1인 1표' — 이미 투표했으면 그 표를 옮긴다(추천 ↔ 비추천 갈아타기).
//   나중 DB에선: votes 테이블에 (work, user_id)가 있으면 UPDATE, 없으면 INSERT.
set_vote($work, $choice);

// ── 4) PRG: 그 작품 게시판으로 리다이렉트 (+투표 완료 표시) ──
header('Location: /board/?work=' . urlencode($work) . '&voted=1');
exit;
