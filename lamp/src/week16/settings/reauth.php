<?php
// ============================================================
// settings/reauth.php — 비밀번호 재확인 처리  [POST → PRG]
//   맞으면 sudo 창을 열고(세션에 확인 시각을 적고), 원래 하려던 화면으로 돌려보낸다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

require_login();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/settings/');
}

require_csrf();

// ── 비밀번호 확인 ────────────────────────────────────────────
//   ★ 아이디는 폼에서 받지 않는다. 지금 로그인한 사람의 것을 쓴다 —
//     폼으로 받으면 남의 아이디+비번으로도 통과시켜 버릴 수 있다.
$username = (string) current_user();

if (verify_login($username, post_str('password')) === null) {
    set_flash('비밀번호가 일치하지 않습니다.', 'error');
    redirect('/settings/confirm.php');       // 다시 확인 화면으로 (목적지는 세션에 그대로 있다)
}

// ── 통과: sudo 창을 연다 ─────────────────────────────────────
//   ★ 이 시각 하나로 '최근에 본인 확인을 했다'가 증명된다. SUDO_WINDOW 동안 유효.
$_SESSION[SESSION_AUTH_AT] = time();

set_flash('✅ 확인되었습니다. 이어서 진행해 주세요.');

// 원래 하려던 화면으로. 없으면 설정으로.
//   ★ '로그인 후 가려던 곳'과 똑같은 장치를 쓴다 — 하는 일이 같으므로 새로 만들지 않는다.
redirect(take_intended('/settings/'));
