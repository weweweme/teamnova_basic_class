<?php
// ============================================================
// settings/revoke_device.php — 기기 하나 끊기  [POST → PRG]
//
//   ★ 끊을 것이 **두 곳**이다 — 기기 기록과 그 기기의 세션.
//     하나만 지우면 아무 효과가 없다:
//       · 기록만 지우면 → 이미 열려 있는 창이 멀쩡히 살아 있다
//       · 세션만 지우면 → 목록에 그대로 남아 다시 로그인하면 조용히 부활한다
//     (로그아웃에서 밟았던 것과 같은 함정이다)
//
//   ★ 민감한 작업이므로 비밀번호를 다시 묻는다(sudo).
//     자리를 비운 사이 지나가던 사람이 **주인의 기기를 끊어버리는 것**도 공격이다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/devices.php';

require_login();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/settings/');
}
require_csrf();
require_recent_auth('/settings/');

$target = post_str('device_id');

// ★ '지금 이 기기'는 못 끊게 한다. 끊으면 스스로 로그아웃되어 사용자가 당황한다.
//   (로그아웃 버튼이 따로 있으니 그쪽을 쓰면 된다)
if ($target === device_id()) {
    set_flash('지금 쓰고 있는 기기는 여기서 끊을 수 없어요. 로그아웃을 눌러주세요.', 'error');
    redirect('/settings/');
}

if (!revoke_device(current_user_id(), $target)) {
    set_flash('이미 없는 기기입니다.', 'error');
    redirect('/settings/');
}

set_flash('🔒 해당 기기의 로그인을 끊었습니다.');
redirect('/settings/');
