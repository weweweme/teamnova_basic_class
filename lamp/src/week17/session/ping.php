<?php
// ============================================================
// session/ping.php — "아직 사람이 있다" 신호  [POST → JSON]
//
//   [★ 왜 필요한가 — '활동'을 요청으로만 재고 있었다]
//     유휴 자동 로그아웃은 **마지막 요청 시각**을 보고 판정한다.
//     그런데 글을 쓰는 동안에는 **요청이 한 번도 안 나간다.**
//     → 20분 넘게 공들여 쓰고 [등록]을 누르면, 그 요청이 도착하는 순간
//       "20분간 아무 활동이 없었다"로 판정되어 **본문이 통째로 버려진다.**
//       (실측: 댓글 4개 → 4개, drafts 0행, old_input 없음 — 어디에도 안 남았다)
//
//   ★ 사람은 활동 중인데 서버가 유휴로 본 것이다. **타이핑도 활동이다.**
//
//   [★★ 이건 자동 저장이 아니다]
//     **내용을 한 글자도 안 보낸다.** 보내는 것은 "살아있다"는 사실 하나뿐이고,
//     이 파일은 **아무 일도 하지 않는다** — auth.php가 요청을 처리하면서
//     last_seen을 갱신하는 것이 전부다. 그래서 본문이 없다.
//     · 초안을 남기는 일은 '💾 임시저장' 버튼이 따로 맡는다(drafts 표).
//
//   [★ 도장 갱신과 정반대로 취급한다]
//     `/session/refresh.php`는 **브라우저가 스스로** 보내는 요청이라 시계를 밀면 안 된다
//     (기계가 밀어주는 시계는 시계가 아니다 → device_key.php의 KEY_PROOF_PATHS).
//     이건 **사람이 자판을 두드렸을 때만** 나가므로 밀어야 한다. **누가 시작했는지가 기준이다.**
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store');

$fail = static function (int $code): never {
    http_response_code($code);
    echo json_encode(['ok' => false]);
    exit;
};

// ★ POST로 못 박는다. GET이면 주소만 열어도 시계가 밀린다 —
//   링크 하나로 남의 세션을 연장시킬 수 있게 된다.
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    $fail(405);
}
require_csrf();

// 여기 도달했다는 건 auth.php의 유휴 판정을 이미 통과했다는 뜻이다.
//   (만료됐으면 auth.php가 로그아웃시키고 '/'로 튕겼다 → fetch는 실패로 받는다)
if (!is_logged_in()) {
    $fail(401);
}

// 남은 시간을 돌려준다. 화면 카운트다운이 이 값으로 다시 시작한다.
echo json_encode(['ok' => true, 'left' => idle_seconds_left()]);
