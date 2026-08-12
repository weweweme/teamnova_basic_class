<?php
// ============================================================
// auth/register.php — 회원가입 처리  [POST 요청 → PRG]
//   users 표에 새 회원을 저장한다 (비밀번호는 해시로).
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/auth/signup.php');
}

// ── 0-1) 우리 화면에서 온 요청이 맞나? (CSRF) ────────────────
//   남의 사이트가 우리 폼을 흉내 내 쏜 POST를 걸러낸다.
//   ★ POST를 처리하는 파일은 예외 없이 이 줄을 갖는다 — 한 곳이라도 빠지면 그 파일이 통로가 된다.
require_csrf();

// ── 1) 값 받기 ───────────────────────────────────────────────
$username = trim(post_str('username'));
$password = post_str('password', '');

// 검증에 걸려 폼으로 되돌아갈 때 아이디는 다시 안 치게 맡겨 둔다.
//   ★ 비밀번호는 절대 넣지 않는다. 세션은 DB의 payload에 평문으로 저장되므로,
//     담는 순간 비밀번호가 표에 그대로 찍힌다. 다시 치는 불편보다 이쪽이 훨씬 위험하다.
keep_old_input(['username' => $username]);

// ── 2) 검증 ──────────────────────────────────────────────────
//   브라우저의 required·minlength는 1차 검사일 뿐, 서버에서 다시 확인한다.
//   mb_strlen = 글자 수 세기 (mb_ = 한글도 1글자로 정확히 셈).
if ($username === '' || mb_strlen($password) < 4) {
    set_flash('❌ 아이디를 입력하고, 비밀번호는 4자 이상으로 정해 주세요.', 'error');
    redirect('/auth/signup.php');
}
// 이미 있는 아이디면 거절 (중복 가입 방지)
if (find_user($username) !== null) {
    set_flash('❌ 이미 있는 아이디입니다.', 'error');
    redirect('/auth/signup.php');
}

// ── 3) 저장 ──────────────────────────────────────────────────
//   create_user가 비밀번호를 password_hash로 해시해 users 표에 INSERT 한다.
//   (평문 저장 절대 금지 — DB가 유출돼도 원래 비번을 알 수 없게)
$newId = create_user($username, $password);
if ($newId === 0) {
    // 위에서 중복 검사를 했지만, 그 찰나에 같은 아이디가 들어올 수도 있어 한 번 더 방어
    set_flash('❌ 이미 있는 아이디입니다.', 'error');
    redirect('/auth/signup.php');
}

// ── 4) PRG: 로그인 페이지로 (+가입 완료 표시) ────────────────
// 성공했으니 맡겨둔 아이디를 버린다 (안 버리면 다음 가입 화면에 되살아난다).
forget_old_input();

set_flash('🎉 회원가입이 완료되었습니다. 로그인해 주세요.');
redirect('/auth/login.php');
