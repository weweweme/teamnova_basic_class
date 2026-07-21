<?php
// ============================================================
// auth/register.php — 회원가입 처리  [POST 요청 → PRG]
//   지금은 저장할 DB가 없어 '가입된 셈 치고' 로그인 페이지로 보낸다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /auth/signup.php');
    exit;
}

// ── 1) 값 받기 ───────────────────────────────────────────────
$username = trim(post_str('username'));
$password = post_str('password', '');

// ── 2) 검증 ──────────────────────────────────────────────────
//   브라우저의 required·minlength는 1차 검사일 뿐, 서버에서 다시 확인한다.
//   mb_strlen = 글자 수 세기 (mb_ = 한글도 1글자로 정확히 셈).
if ($username === '' || mb_strlen($password) < 4) {
    set_flash('❌ 아이디를 입력하고, 비밀번호는 4자 이상으로 정해 주세요.', 'error');
    header('Location: /auth/signup.php');
    exit;
}
// 이미 있는 아이디면 거절 (중복 가입 방지)
if (find_user($username) !== null) {
    set_flash('❌ 이미 있는 아이디입니다.', 'error');
    header('Location: /auth/signup.php');
    exit;
}

// ── 3) 저장 (지금은 stub) ────────────────────────────────────
//   나중엔 users 테이블에 INSERT 한다. 이때 비밀번호는 반드시
//   password_hash($password, PASSWORD_DEFAULT) 로 '해시'해서 저장한다.
//   (평문 저장 절대 금지 — DB가 유출되면 비밀번호가 그대로 새어나간다)

// ── 4) PRG: 로그인 페이지로 (+가입 완료 표시) ────────────────
set_flash('🎉 회원가입이 완료되었습니다. 로그인해 주세요.');
header('Location: /auth/login.php');
exit;
