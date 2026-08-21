<?php
// ============================================================
// settings/logout_all.php — 모든 기기에서 로그아웃  [POST → PRG]
//
//   [★ 왜 비밀번호 변경과 따로 두나]
//     비밀번호를 바꾸면 다른 세션이 함께 끊긴다(password.php). 그런데 그것만으로는 부족하다:
//       · 비번 변경은 **새 비번을 정하고 기억**해야 한다 — 급할 때 부담이 크다
//       · 기기가 다섯이면 하나씩 다섯 번 눌러야 한다
//       · **비번은 안 샜다고 확신**할 때도 있다 (세션 번호표만 훔쳐간 경우)
//     → "지금 당장 끊고 싶다"와 "비밀번호를 바꾸고 싶다"는 **다른 일**이다.
//
//   [무엇을 끊나 — 두 곳이다]
//     · sessions   — 지금 열려 있는 로그인. 안 지우면 이미 열린 창이 그대로 산다
//     · user_devices — 기기 기록. 안 지우면 목록에 남아 '아직 로그인된 것처럼' 보인다
//     ★ 다만 **이 기기는 남긴다.** 스스로 로그아웃되면 사용자가 당황한다 —
//       "다른 기기를 끊었는데 왜 내가 나가지지?" (로그아웃 버튼은 따로 있다)
//
//   ★ 민감한 작업이므로 비밀번호를 다시 묻는다(sudo).
//     자리를 비운 사이 지나가던 사람이 **주인의 모든 기기를 끊어버리는 것**도 공격이다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/devices.php';
require_once __DIR__ . '/../includes/session_db.php';

require_login();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/settings/');
}
require_csrf();
require_recent_auth('/settings/');

$userId = current_user_id();

// ① 지금 이 세션만 빼고 전부 끊는다.
$cut = destroy_other_sessions($userId, session_id());

// ② 기기 기록도 이 기기만 남기고 지운다.
//   ★ 세션만 끊고 기록을 두면, 목록에 그대로 남아 **아직 살아 있는 것처럼 보인다.**
//     "끊었다"는 말이 화면에서 증명되지 않으면 사용자는 또 믿어달라는 소리로 듣는다.
db()->prepare('DELETE FROM user_devices WHERE user_id = ? AND device_id <> ?')
    ->execute([$userId, device_id()]);

set_flash($cut > 0
    ? '🔒 다른 기기 ' . $cut . '곳에서 로그아웃했습니다. 이 기기는 그대로 유지됩니다.'
    : '🔒 다른 기기에 로그인된 곳이 없었습니다.');
redirect('/settings/');
