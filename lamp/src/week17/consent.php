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
//   choice = all(모두 동의) / selected(체크한 것만) / none(거절·철회) / reset(처음부터 다시)
$choice = post_str('choice');
$back   = post_str('back');

// ── reset — 기록을 지워 '안 물어본 상태'로 되돌린다 ──────────
//   ★ 철회(none)와 다르다. 철회는 **'싫다'는 선택**이라 기록으로 남고 배너가 다시 안 뜬다.
//     reset은 **아무것도 안 고른 상태**라 배너가 다시 뜬다.
//     둘을 한 버튼에 묶으면 눌렀을 때 어느 쪽인지 알 수 없다.
//   ★ 로그인 상태면 회원 번호도 함께 남긴다. 비로그인이면 NULL이 정상이다.
$userId = is_logged_in() ? current_user_id() : null;

if ($choice === 'reset') {
    forget_consent($userId);
    forget_unconsented_cookies();       // 안 고른 상태 = 아무것도 동의 안 함 → 쌓인 것도 치운다
    set_flash('🍪 선택을 지웠습니다. 동의 창이 다시 나타납니다.');
    redirect(is_internal_path($back) ? $back : '/');
}

//   ★ 체크박스는 **체크했을 때만 값이 온다.** 안 왔으면 안 켠 것이다.
$items = [];
foreach (array_keys(CONSENT_ITEMS) as $key) {
    $items[$key] = match ($choice) {
        'all'      => true,
        'none'     => false,
        default    => post_str('item_' . $key) !== '',   // selected
    };
}

// ★ 어느 화면에서 골랐는지도 함께 남긴다 — '어떻게 받았는가'는 증빙에서 반드시 묻는 것이다.
//   배너는 back이 지금 보던 화면, 쿠키 설정 화면은 /cookies.php 로 고정돼 있다.
$source = $back === '/cookies.php' ? 'settings' : 'banner';

save_consent($items, $source, $userId);

// ★ 거절한 항목의 쿠키는 여기서 바로 치운다.
//   "앞으로 안 심겠다"만으로는 부족하다 — 이미 쌓인 것도 없애야 거절이 거절이다.
forget_unconsented_cookies();

$agreed = count(array_filter($items));
set_flash($agreed > 0
    ? '🍪 선택하신 대로 저장했습니다. 쿠키 설정에서 언제든 바꿀 수 있어요.'
    : '🍪 필수 쿠키만 사용합니다. 기록해 둔 것은 지웠습니다.');

// ── 보던 화면으로 돌려보낸다 ─────────────────────────────────
//   ★ 배너는 모든 페이지에 뜨므로 '어디서 눌렀는지'를 폼이 들고 온다.
//     그 값은 사용자가 보낸 것이라 그대로 믿으면 안 된다 —
//     //evil.com 을 넣으면 동의 버튼이 남의 사이트로 보내는 버튼이 된다(오픈 리다이렉트).
//     → intended 쿠키를 검사할 때 쓰는 그 함수를 그대로 재사용한다.
redirect(is_internal_path($back) ? $back : '/');
