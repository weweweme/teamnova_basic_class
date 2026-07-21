<?php
// ============================================================
// vote/sentiment.php — 종목 투자심리 투표 처리  [POST 요청 → PRG]
//   토론방의 '매수 / 매도' 버튼이 보낸 값을 받아 (지금은 집계된 셈 치고)
//   그 종목 토론방으로 다시 리다이렉트한다.
//
//   ★ 지금까지의 POST는 '글'에 대한 것이었지만(추천·신고·댓글),
//     이건 '종목'에 대한 투표라 post_id가 아니라 ticker를 받는다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';

// 허용된 선택지 (화이트리스트 — 폼을 조작해 아무 값이나 보낼 수 있으므로)
const ALLOWED_CHOICES = ['매수', '매도'];

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 값 받기 ───────────────────────────────────────────────
//   choice = 어느 버튼을 눌렀는지. (버튼에 name/value를 달아두면 그 값이 전송된다)
$ticker = $_POST['ticker'] ?? '';
$choice = $_POST['choice'] ?? '';

// ── 2) 검증 ──────────────────────────────────────────────────
if ($ticker === '') {
    header('Location: /');
    exit;
}
if (!in_array($choice, ALLOWED_CHOICES, true)) {
    // 이상한 선택지면 투표하지 않고 그 토론방으로 되돌린다.
    //   urlencode() = 값을 주소에 안전하게 넣기 위해 특수문자·한글을 변환.
    header('Location: /board/?ticker=' . urlencode($ticker));
    exit;
}

// ── 3) 저장 (지금은 stub) ────────────────────────────────────
//   나중엔 votes 테이블에 (ticker, user_id, choice)를 INSERT 하고,
//   이미 투표했으면 바꿔치기(UPDATE) 한다. 지금은 로그인이 없어 집계만 흉내낸다.

// ── 4) PRG: 그 종목 토론방으로 리다이렉트 (+투표 완료 표시) ──
header('Location: /board/?ticker=' . urlencode($ticker) . '&voted=1');
exit;
