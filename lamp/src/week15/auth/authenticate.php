<?php
// ============================================================
// auth/authenticate.php — 로그인 처리  [POST 요청 → PRG]
//   login.php 폼이 보낸 아이디·비밀번호를 확인하고,
//   맞으면 신원(?as=아이디)을 붙인 주소로 홈에 보낸다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/auth/login.php');
}

// ── 1) 값 받기 ───────────────────────────────────────────────
//   비밀번호는 trim하지 않는다 — 앞뒤 공백도 비밀번호의 일부일 수 있으므로.
$username = trim(post_str('username'));
$password = post_str('password', '');

// ── 2) 검증 ──────────────────────────────────────────────────
$user = verify_login($username, $password);

if ($user === null) {
    // ★ 실패 이유를 "아이디가 없음 / 비번이 틀림"으로 나눠 알려주지 않는다.
    //   나누면 공격자가 "이 아이디는 존재하는구나"를 알아낼 수 있기 때문(계정 열거).
    //   그래서 항상 뭉뚱그려 하나의 메시지로 돌려보낸다.
    set_flash('❌ 아이디 또는 비밀번호가 올바르지 않습니다.', 'error');
    redirect('/auth/login.php');
}

// ── 3) PRG: 신원을 주소에 심어 홈으로 리다이렉트 ─────────────
//   ★ 세션이 없으므로 '기록'이 아니라 '전달'이다.
//     홈 주소에 ?as=영화광 이 붙어서 나가고, 그 뒤 링크·폼·리다이렉트가 물고 다닌다.
set_flash('👋 ' . $user['username'] . '님, 환영합니다!');
login_and_redirect($user['username']);
