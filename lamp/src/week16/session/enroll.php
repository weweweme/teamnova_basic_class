<?php
// ============================================================
// session/enroll.php — 이 기기의 도장을 등록한다  [POST → JSON]
//
//   [받는 것 둘]
//     ① 도장의 **공개된 짝**(공개키)
//     ② 방금 받은 숫자에 찍은 **자국**
//
//   [★★ 왜 ②까지 받나 — 공개키만 받으면 안 되는 이유]
//     공개키는 이름 그대로 공개돼도 되는 값이라, **아무나 남의 공개키를 주워다 낼 수 있다.**
//     그것만 믿고 등록하면 "도장은 없는데 공개키만 등록된 기기"가 생긴다.
//     → 그 도장을 **실제로 갖고 있다는 것**까지 그 자리에서 보여야 한다. 그게 ②다.
//     ※ 이걸 "소유 증명"이라고 부른다. 공개키를 다루는 모든 프로토콜이 같은 짝으로 움직인다.
//
//   [★ 그리고 한 번 등록되면 못 바꾼다]
//     save_device_public_key()가 `public_key IS NULL`일 때만 쓴다.
//     안 그러면 **세션을 훔친 쪽이 자기 도장으로 갈아 끼우면 그만**이라 검사가 무의미해진다.
//     도장을 바꾸려면 설정에서 기기를 끊고 **비밀번호로 다시 로그인**해야 한다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/device_key.php';

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');

// 실패를 한 곳에서 내보낸다. 이유는 안 나눠 알려준다 —
//   ★ "공개키가 이상함 / 자국이 안 맞음"을 구분해 주면 어디를 고쳐 다시 시도할지 알려주는 셈이다.
$fail = static function (int $code = 400): never {
    http_response_code($code);
    echo json_encode(['ok' => false]);
    exit;
};

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    $fail(405);
}
require_csrf();

if (!is_logged_in()) {
    $fail(401);
}
$userId = current_user_id();

// 이미 도장이 있으면 등록이 아니라 연장으로 가야 한다.
if (device_public_key($userId) !== null) {
    $fail(409);
}

$publicKey = post_str('public_key');
$signature = post_str('signature');
$challenge = take_key_challenge();       // ★ 꺼내면서 지운다 (한 번만 쓰인다)

if ($challenge === null || $publicKey === '' || $signature === '') {
    $fail();
}

// ★ 대조부터 하고, 통과했을 때만 저장한다. 순서가 반대면 '아무 공개키나 저장되는' 문이 열린다.
if (!verify_key_signature($publicKey, $challenge, $signature)) {
    $fail();
}

if (!save_device_public_key($userId, $publicKey)) {
    $fail(409);
}

// 방금 자국까지 확인했으므로 도장 시계도 지금 시작한다.
//   ★ 여기서 안 켜면 등록 직후 곧바로 '확인 필요' 상태가 되어 한 바퀴 더 돈다.
mark_key_proved($userId);

echo json_encode(['ok' => true]);
