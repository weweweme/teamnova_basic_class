<?php
// ============================================================
// consent.php — 쿠키 동의 처리  [POST 요청 → PRG]
//   배너의 세 버튼([모두 동의] / [선택한 것만] / [거절])이 전부 여기로 온다.
//
//   ★ 예전에는 이 파일이 없었다. 브라우저(JS)가 document.cookie 한 줄로 직접 심었기 때문이다.
//     '확인'일 때는 그래도 됐다. **동의로 바뀌면서 서버를 거쳐야 하게 됐다** —
//     서버가 모르는 동의는 나중에 증명할 수 없기 때문이다.
//     (곧 이 자리에서 DB에도 남길 것이다)
//
//   ★★ JS가 한 줄도 없다. 폼 하나로 끝난다 —
//     JS를 꺼도 동의를 고를 수 있어야 한다. 동의는 '되면 좋은 기능'이 아니다.
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/auth.php';    // is_internal_path — 돌아갈 주소 검증에 재사용
require_once __DIR__ . '/includes/prefs.php';   // forget_unconsented_cookies

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/');
}

// ★ 동의도 POST다 → CSRF 검사를 한다.
//   남의 사이트가 "전부 동의" POST를 대신 쏴서 동의를 만들어내면 그건 동의가 아니다.
require_csrf();

// ── 무엇을 골랐나 ────────────────────────────────────────────
//   choice = all(모두 동의) / selected(체크한 것만) / none(거절)
//   ★ 체크박스는 **체크했을 때만 값이 온다.** 안 왔으면 안 켠 것이다.
$choice = post_str('choice');
$items  = [];

foreach (array_keys(CONSENT_ITEMS) as $key) {
    $items[$key] = match ($choice) {
        'all'      => true,
        'none'     => false,
        default    => post_str('item_' . $key) !== '',   // selected
    };
}

save_consent($items);

// ★ 거절한 항목의 쿠키는 여기서 바로 치운다.
//   "앞으로 안 심겠다"만으로는 부족하다 — 이미 쌓인 것도 없애야 거절이 거절이다.
forget_unconsented_cookies();

$agreed = count(array_filter($items));
set_flash($agreed > 0
    ? '🍪 선택하신 대로 저장했습니다. 설정에서 언제든 바꿀 수 있어요.'
    : '🍪 필수 쿠키만 사용합니다. 기록해 둔 것은 지웠습니다.');

// ── 보던 화면으로 돌려보낸다 ─────────────────────────────────
//   ★ 배너는 모든 페이지에 뜨므로 '어디서 눌렀는지'를 폼이 들고 온다.
//     그 값은 사용자가 보낸 것이라 그대로 믿으면 안 된다 —
//     //evil.com 을 넣으면 동의 버튼이 남의 사이트로 보내는 버튼이 된다(오픈 리다이렉트).
//     → intended 쿠키를 검사할 때 쓰는 그 함수를 그대로 재사용한다.
$back = post_str('back');
redirect(is_internal_path($back) ? $back : '/');
