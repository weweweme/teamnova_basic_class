<?php
// ============================================================
// settings/password.php — 비밀번호 변경  [POST → PRG]
//   ★ 반드시 '현재 비밀번호'를 확인한 뒤에 바꾼다.
//     (로그인 중이어도, 자리를 비운 틈의 무단 변경을 막는 실무 관행)
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/session_db.php'; // destroy_other_sessions

require_login();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/settings/');
}

// ── 0-1) 우리 화면에서 온 요청이 맞나? (CSRF) ────────────────
//   남의 사이트가 우리 폼을 흉내 내 쏜 POST를 걸러낸다.
//   ★ POST를 처리하는 파일은 예외 없이 이 줄을 갖는다 — 한 곳이라도 빠지면 그 파일이 통로가 된다.
require_csrf();

$current = post_str('current');
$new     = post_str('new');
$username = (string) current_user();

// ── ① 현재 비밀번호 확인 (틀리면 변경 거부) ──────────────────
//   verify_login이 '아이디+비번'이 맞으면 회원 배열, 틀리면 null.
if (verify_login($username, $current) === null) {
    set_flash('현재 비밀번호가 일치하지 않습니다.', 'error');
    redirect('/settings/');
}

// ── ② 새 비밀번호 길이 검증 (회원가입과 같은 최소 4자) ───────
if (mb_strlen($new) < 4) {
    set_flash('새 비밀번호는 4자 이상이어야 합니다.', 'error');
    redirect('/settings/');
}

// ── ③ 해시로 바꿔 저장 ──────────────────────────────────────
set_password(current_user_id(), $new);

// ★★ 비밀번호를 바꿨으면 **다른 곳의 로그인을 전부 끊는다.**
//   [왜 이게 없으면 안 되나]
//     "털린 것 같은데?" 싶을 때 사람이 제일 먼저 하는 행동이 비밀번호 변경이다.
//     그런데 세션과 자동 로그인 표를 그대로 두면 **공격자는 계속 들어와 있다.**
//     비밀번호를 바꿨는데도 쫓아내지 못하면, 그 기능은 사용자를 안심시키기만 하는 셈이다.
//   ★ 세션이 DB에 있어서 가능한 일이다 — WHERE user_id = ? 한 줄이면 된다.
//     파일 세션이었으면 남의 기기 세션 파일을 찾을 방법이 아예 없다.
$userId = current_user_id();
$cut = destroy_other_sessions($userId, session_id());  // 이 기기만 빼고 세션 전부

set_flash($cut > 0
    ? '🔑 비밀번호를 변경하고 다른 기기 ' . $cut . '곳에서 로그아웃했습니다.'
    : '🔑 비밀번호가 변경되었습니다.');
redirect('/settings/');
