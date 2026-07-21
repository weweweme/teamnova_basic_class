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
//   ★ '1인 1표' 토글 — 누른 걸 또 누르면 취소, 반대쪽을 누르면 갈아타기.
//   나중 DB에선: votes 테이블의 (work, user_id) 행을 DELETE / INSERT / UPDATE.
toggle_vote($work, $choice);

// ── 4) PRG: 그 작품 게시판으로 리다이렉트 (+결과 알림) ───────
//   저장한 '뒤'에 다시 물어봐야 투표인지 취소인지 정확히 알 수 있다.
set_flash(my_vote($work) === null
    ? '투표를 취소했습니다.'
    : '🗳️ ' . $choice . '에 투표했습니다. (임시 저장 — 브라우저를 닫으면 초기화됩니다)');
header('Location: /board/?work=' . urlencode($work));
exit;
