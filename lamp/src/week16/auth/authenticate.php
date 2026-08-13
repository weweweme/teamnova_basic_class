<?php
// ============================================================
// auth/authenticate.php — 로그인 처리  [POST 요청 → PRG]
//   login.php 폼이 보낸 아이디·비밀번호를 확인하고,
//   맞으면 신원(?as=아이디)을 붙인 주소로 홈에 보낸다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/consent.php';   // 비로그인 때 받아둔 동의를 회원과 잇는다

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/auth/login.php');
}

// ── 0-1) 우리 화면에서 온 요청이 맞나? (CSRF) ────────────────
//   남의 사이트가 우리 폼을 흉내 내 쏜 POST를 걸러낸다.
//   ★ POST를 처리하는 파일은 예외 없이 이 줄을 갖는다 — 한 곳이라도 빠지면 그 파일이 통로가 된다.
require_csrf();

// ── 1) 값 받기 ───────────────────────────────────────────────
//   비밀번호는 trim하지 않는다 — 앞뒤 공백도 비밀번호의 일부일 수 있으므로.
$username = trim(post_str('username'));
$password = post_str('password', '');

// 실패해서 폼으로 되돌아갈 때 아이디는 다시 안 치게 맡겨 둔다.
//   ★ 비밀번호는 넣지 않는다 — 이 값은 쿠키에 평문으로 담겨 브라우저에 남는다.
keep_old_input(['username' => $username]);

// ── 2) 검증 ──────────────────────────────────────────────────
$user = verify_login($username, $password);

if ($user === null) {
    // ★ 실패 이유를 "아이디가 없음 / 비번이 틀림"으로 나눠 알려주지 않는다.
    //   나누면 공격자가 "이 아이디는 존재하는구나"를 알아낼 수 있기 때문(계정 열거).
    //   그래서 항상 뭉뚱그려 하나의 메시지로 돌려보낸다.
    set_flash('❌ 아이디 또는 비밀번호가 올바르지 않습니다.', 'error');
    redirect('/auth/login.php');
}

// ── 3) PRG: 세션에 신원을 기록하고 홈으로 리다이렉트 ─────────
//   ★ week15와 결정적으로 다른 지점이다. 주소에 '전달'하는 게 아니라 서버가 '기록'한다.
//     주소는 그냥 `/` 로 깨끗하게 나가고, 그 뒤 요청들은 번호표만 들고 온다.
//   ★ 아이디가 아니라 번호(id)를 넘긴다 — 세션에 담는 것이 번호이기 때문.
//
//   '로그인 유지'는 체크박스라 **체크했을 때만 값이 온다.** 안 왔으면 안 누른 것.
//   → 체크했으면 세션과 별도로 30일짜리 쿠키를 하나 더 받는다 (includes/remember.php).
$remember = post_str('remember') !== '';

// 성공했으니 맡겨둔 아이디를 버린다 (안 버리면 다음 로그인 화면에 되살아난다).
forget_old_input();

// ★ 이 브라우저에서 받아둔 동의를 회원과 잇는다 (consent_log에 'link' 줄 한 줄).
//   비로그인일 때 동의했다면 그 기록의 user_id가 NULL이었다 —
//   이제 누구였는지 알게 됐으므로 그 사실을 남긴다.
link_consent_to_user((int) $user['id']);

set_flash('👋 ' . $user['username'] . '님, 환영합니다!');
login_and_redirect((int) $user['id'], $remember);
