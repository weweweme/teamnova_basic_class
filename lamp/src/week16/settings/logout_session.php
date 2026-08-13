<?php
// ============================================================
// settings/logout_session.php — 그 기기 하나만 로그아웃  [POST → PRG]
//   설정 화면의 '로그인 기기' 목록에서 [로그아웃]을 누르면 여기로 온다.
//
//   ★ '다른 기기에서 모두 로그아웃'(logout_others.php)과 다른 점:
//     이건 목록에서 **고른 하나만** 끊는다. "노트북은 두고 PC방 것만 끊고 싶다"에 쓴다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/remember.php';

require_login();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/settings/');
}

require_csrf();

// 남의 기기를 끊는 일이므로 '모두 로그아웃'과 같은 문턱을 세운다.
require_recent_auth('/settings/');

$userId = current_user_id();
$target = post_str('id_hash');

// ── 지금 쓰는 기기를 고른 경우 ───────────────────────────────
//   ★ 그냥 지우면 자기 세션이 사라져 화면이 로그아웃 상태로 튕긴다.
//     의도한 것일 수도 있지만, '기기 목록'에서 일어나면 사고처럼 보인다.
//     그래서 그건 정식 로그아웃으로 안내한다.
if ($target === session_fingerprint(session_id())) {
    set_flash('지금 쓰는 기기입니다. 로그아웃 메뉴를 이용해 주세요.', 'error');
    redirect('/settings/');
}

// ── 끊기 ─────────────────────────────────────────────────────
//   ★ user_id 조건이 함수 안에 들어 있다 — 남의 세션은 못 끊는다.
if (!destroy_session_of($userId, $target)) {
    set_flash('이미 로그아웃된 기기입니다.', 'error');
    redirect('/settings/');
}

// ★ 세션만 끊으면 그 기기의 '로그인 유지' 쿠키가 다음 접속에 되살린다.
//   그런데 어느 표가 그 기기 것인지 알 방법이 없다 — remember 토큰과 세션은 서로 모른다.
//   그래서 여기서는 **표를 전부 버리고 이 기기 것만 다시 발급**한다.
//   (한 기기만 끊는데 다른 기기의 자동 로그인까지 풀리는 건 그 대가다. 안전한 쪽을 택했다)
$hadRemember = isset($_COOKIE[REMEMBER_COOKIE]);
remember_forget_all($userId);
if ($hadRemember) {
    remember_issue($userId);
}

set_flash('🔒 해당 기기에서 로그아웃했습니다.');
redirect('/settings/');
