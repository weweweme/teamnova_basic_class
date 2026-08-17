<?php
// ============================================================
// session/challenge.php — "아무 숫자나 하나 줄게"  [GET → JSON]
//
//   [이 파일이 하는 일]
//     브라우저가 도장을 찍을 대상(무작위 숫자)을 하나 만들어 준다.
//
//   [★ 왜 매번 새 숫자인가 — 이게 bearer와 갈리는 지점이다]
//     숫자가 고정이면 그 자국 하나만 훔쳐서 계속 쓰면 된다. 쿠키와 똑같아진다.
//     매번 다른 숫자를 주니 **자국도 매번 다르고, 지난 자국은 아무 쓸모가 없다.**
//
//   [★ 왜 로그인한 사람에게만 주나]
//     안 그러면 아무나 숫자를 받아 갈 수 있다. 지금 당장 피해는 없지만,
//     **쓸 이유가 없는 사람에게 주는 것 자체가 불필요한 문**이다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/device_key.php';

header('Content-Type: application/json; charset=utf-8');

// ★ 이 응답은 절대 저장되면 안 된다. 캐시에 남은 숫자를 다시 받아 쓰면 '매번 새 숫자'가 깨진다.
header('Cache-Control: no-store');

if (!is_logged_in()) {
    http_response_code(401);
    echo json_encode(['error' => 'login required']);
    return;
}

echo json_encode(['challenge' => issue_key_challenge()]);
