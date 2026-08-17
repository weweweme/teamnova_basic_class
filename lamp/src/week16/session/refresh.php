<?php
// ============================================================
// session/refresh.php — 도장을 찍어 세션을 연장한다  [POST → JSON]
//
//   [★ 이 파일이 이번 변경의 전부다]
//     전:  요청이 오면        연장   ← 훔친 쪽도 요청만 보내면 무기한 연장된다
//     후:  도장을 찍어야      연장   ← 훔친 쪽은 도장이 없어 10분 뒤 끊긴다
//
//   [★★ 그래서 훔친 쿠키의 수명이 '10분'으로 못 박힌다]
//     예전에는 세션 TTL 30분·유휴 20분이 **요청할 때마다 뒤로 밀렸다**(sliding).
//     즉 공격자가 20분마다 한 번씩만 요청해도 무기한 유지됐다.
//     이제 미는 조건에 도장이 붙었으므로 **도장 없는 쪽은 아무리 요청해도 못 민다.**
//
//   [왜 매 요청이 아니라 여기서만 찍나]
//     도장 한 번이 P50 200ms · P95 600ms다(DBSC 설명서 실측).
//     매 요청 찍으면 클릭할 때마다 그만큼 멈춘다 → **평소엔 쿠키로, 만료 때만 도장.**
//     DBSC가 하는 것과 같은 타협이다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/device_key.php';

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');

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

// 이 기기에 등록된 공개키. 없으면 아직 등록 전이다 → 등록 화면으로 보내야 한다.
$publicKey = device_public_key($userId);
if ($publicKey === null) {
    $fail(409);
}

$challenge = take_key_challenge();
$signature = post_str('signature');

if ($challenge === null || $signature === '') {
    $fail();
}

if (!verify_key_signature($publicKey, $challenge, $signature)) {
    // ★ 여기 실패는 그냥 오류가 아니다 — **등록된 도장과 다른 도장으로 찍었다**는 뜻이다.
    //   지금은 거부만 한다. 실무라면 이 자리가 '기기 도난 의심' 신호를 남길 자리다.
    $fail(403);
}

mark_key_proved($userId);

echo json_encode(['ok' => true, 'left' => key_proof_seconds_left()]);
