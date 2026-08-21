<?php
// ============================================================
// handoff.php — '딱 다음 한 요청'에만 건네는 값 (알림 · 가려던 곳 · 폼 입력값)
//
//   [무엇을 나르나 — PRG 때문에 생기는 틈]
//     처리(POST)와 화면(GET)이 응답 두 개로 나뉜다.
//       ① POST 응답 = "저기로 가"뿐. 화면이 없다.
//       ② GET  응답 = 진짜 화면.
//     ①에서 정한 것(알림 문구 · 돌아갈 곳 · 방금 친 값)을 ②까지 들고 갈 통로가 필요하다.
//     그 통로가 이 파일이다.
//
//   [★ 왜 쿠키인가 — 세션에서 옮겨온 이유]
//     우리 기준은 "틀리면 손해 보는 값만 세션"이다. 이 셋은 그 기준에 걸리지 않는다.
//       · 알림 문구를 고치면 → 자기 화면에 자기가 쓴 글자가 뜰 뿐
//       · 가려던 곳을 고치면 → 자기가 엉뚱한 데로 갈 뿐
//       · 폼 입력값을 고치면 → 자기 입력칸에 자기가 쓴 글자가 채워질 뿐
//     남는 것은 없고 자기만 손해다 → 쿠키 자리다.
//
//     ★ week15의 '주소로 나르기'와는 다르다. 주소는 **남에게 링크로 뿌릴 수 있어서**
//       위험했다 (피해자 화면에 가짜 되돌리기 버튼을 만들 수 있었다).
//       쿠키는 남이 심을 수 없다 — 내 브라우저의 쿠키를 고칠 수 있는 건 나뿐이고,
//       내가 나를 속이는 건 공격이 아니다.
//
//   [★★ 쿠키로 옮기면서 새로 생긴 숙제 둘]
//     ① 지우는 것도 응답 헤더(Set-Cookie)다.
//        세션은 unset() 한 줄이면 언제든 지워졌다. 쿠키는 "지워라"를 헤더로 보내야 하는데,
//        헤더는 화면이 한 글자라도 나가면 못 보낸다.
//        그런데 알림을 그리는 자리는 header.php 한복판 — 이미 출력이 시작된 뒤다.
//        → 출력 전인 요청 맨 앞에서 미리 만료시킨다 (handoff_boot).
//
//     ② 값의 주인이 서버에서 사용자로 바뀌었다.
//        세션에 있을 땐 '적을 때'만 검사하면 됐다. 담아둔 뒤엔 아무도 못 건드렸으니까.
//        쿠키는 담아둔 뒤에도 사용자가 고칠 수 있다 → **읽을 때도 검사**해야 한다.
//        (take_flash · take_intended · old 가 각자 한다)
//
//   [수명]
//     expires를 안 주면 '세션 쿠키'가 된다 — 브라우저를 닫으면 사라진다.
//     어차피 다음 한 요청만 살면 되는 값이라 이게 맞다.
//     ※ 이름이 헷갈리지만 서버 세션과는 아무 상관 없다. '창을 닫으면 끝'이라는 뜻뿐이다.
// ============================================================

// 쿠키 이름. F12 → Application → Cookies 에서 이 이름 그대로 보인다.
const HANDOFF_FLASH     = 'flash';
const HANDOFF_INTENDED  = 'intended';
const HANDOFF_OLD_INPUT = 'old_input';

// ★★ 요청이 시작될 때 자동으로 걷어갈 목록 — intended는 **일부러 뺐다.**
//   [왜 갈리는가 — 몇 요청을 살아야 하는가가 다르다]
//     · 알림·폼 입력값 : **바로 다음 요청 하나**에서 소비된다.
//         POST(실패) → 리다이렉트 → GET(폼 화면에서 그린다). 끝.
//         화면을 그리는 중간에 지울 수 없으니, 요청 맨 앞에서 자동으로 걷어간다.
//
//     · 가려던 곳(intended) : **누가 가져갈 때까지 기다려야 한다.**
//         GET /post/write.php  → 기록하고 로그인 화면으로 (① 요청)
//         GET /auth/login.php  → 사용자가 아이디·비번을 친다 (② 요청 — 아직 안 쓴다!)
//         POST /authenticate   → 그제야 꺼내 쓴다            (③ 요청)
//       여기 넣어두면 ②에서 지워져서, 로그인해도 홈으로 튕긴다.
//       (세션일 땐 unset을 take_intended 안에서만 했으니 이런 일이 없었다.
//        '언제 지우는가'를 쿠키가 바꿔놓은 것 — 옮기면서 실제로 밟은 함정이다)
//       → 그래서 intended는 handoff_claim()으로 **쓰는 쪽이 직접** 지운다.
const HANDOFF_NAMES = [HANDOFF_FLASH, HANDOFF_OLD_INPUT];

// 쿠키 하나에 담을 수 있는 한도(글자).
//   브라우저 규격상 쿠키 하나는 4KB인데, 한글은 URL 인코딩되며 한 글자가 9바이트까지 커진다.
//   넘치면 브라우저가 **조용히 버린다** — 그래서 넘기기 전에 우리가 먼저 잘라낸다.
const HANDOFF_MAX = 1500;

// 쿠키 옵션 한 벌.
//   httponly : JS가 못 읽게. 이 셋은 서버만 쓰므로 굳이 JS에 열어줄 이유가 없다.
//   samesite=Lax : 남의 사이트에서 넘어온 요청에는 안 딸려간다.
//   secure   : HTTPS일 때만 보낸다. (지금은 http라 자동으로 꺼진다)
function handoff_options(bool $expire = false): array {
    return [
        'expires'  => $expire ? time() - 3600 : 0,   // 0 = 브라우저를 닫으면 사라짐
        'path'     => '/',
        'httponly' => true,
        'samesite' => 'Lax',
        'secure'   => !empty($_SERVER['HTTPS']),
    ];
}

// ★ 요청 맨 앞에서 한 번 부른다 (util.php가 부른다). 화면이 나가기 전이어야 한다.
//   하는 일: 지금 들어온 handoff 쿠키들에 '만료' 헤더를 미리 붙여둔다.
//     → 이 응답을 받은 브라우저는 그 쿠키를 지운다 = read-once가 성립한다.
//   ★ 값 자체는 $_COOKIE에 그대로 남아 있으므로, 이 요청 안에서는 계속 읽을 수 있다.
//     (만료는 '다음 요청부터'의 이야기다. 지금 읽는 데는 지장이 없다)
//   ★ 이 요청이 새 값을 다시 넣으면(set_flash 등) Set-Cookie가 한 번 더 나가고,
//     브라우저는 나중에 온 것을 따른다 → 덮어쓰기가 된다.
function handoff_boot(): void {
    foreach (HANDOFF_NAMES as $name) {
        if (isset($_COOKIE[$name])) {
            setcookie($name, '', handoff_options(true));
        }
    }
}

// 값을 다음 요청으로 넘긴다. (액션 파일이 redirect 하기 '직전'에 부른다)
//   ★ 배열을 JSON 한 줄로 만들어 담는다. 쿠키는 문자열 한 줄만 담을 수 있기 때문이다.
//     (세션은 배열을 배열 그대로 담을 수 있었다 — 이것도 쿠키로 옮긴 대가다)
function handoff_put(string $name, array $value): void {
    // JSON_UNESCAPED_UNICODE = 한글을 \uXXXX 로 바꾸지 않는다 (그래야 길이가 안 부푼다).
    $json = json_encode($value, JSON_UNESCAPED_UNICODE);

    // 인코딩이 실패했거나(깨진 바이트) 한도를 넘으면 아예 안 보낸다.
    //   ★ 잘라서 보내면 JSON이 깨져 다음 요청에서 못 읽는다. 없느니만 못하다.
    if ($json === false || strlen(rawurlencode($json)) > HANDOFF_MAX) {
        handoff_drop($name);
        return;
    }
    setcookie($name, $json, handoff_options());
}

// 값을 꺼낸다. 없거나 모양이 이상하면 null.
//   ★★ 이름이 take_(가져가다)인 이유 — 꺼내면서 이 요청에서도 지운다.
//     $_COOKIE에서 지우지 않으면, 같은 요청에서 또 부를 때 두 번 읽힌다.
//     (브라우저 쪽 삭제는 handoff_boot이 이미 예약해 뒀다)
function handoff_take(string $name): ?array {
    if (!isset($_COOKIE[$name]) || !is_string($_COOKIE[$name])) {
        return null;
    }
    $raw = $_COOKIE[$name];
    unset($_COOKIE[$name]);              // ★ 읽었으니 뗀다 (read-once)

    // ★ 여기가 '읽을 때 검사'의 첫 관문이다. 쿠키 값은 사용자가 고칠 수 있으므로
    //   JSON이 아닐 수도, 배열이 아닐 수도, 터무니없이 길 수도 있다.
    if (strlen($raw) > HANDOFF_MAX) {
        return null;
    }
    $value = json_decode($raw, true);

    return is_array($value) ? $value : null;
}

// 값을 꺼내면서 **브라우저에서도 지운다.** (자동으로 안 걷어가는 값 = intended 전용)
//   ★ 부르는 자리가 redirect() 직전이어서 아직 출력이 없다 → 여기서 헤더를 보내도 된다.
//     화면을 그리는 중간이라면 이 함수를 쓸 수 없다. 그게 handoff_boot이 따로 있는 이유다.
function handoff_claim(string $name): ?array {
    $hadCookie = isset($_COOKIE[$name]);
    $value     = handoff_take($name);

    if ($hadCookie) {
        setcookie($name, '', handoff_options(true));
    }
    return $value;
}

// 값을 버린다. (성공해서 다음 화면에 넘길 게 없어졌을 때)
function handoff_drop(string $name): void {
    unset($_COOKIE[$name]);
    setcookie($name, '', handoff_options(true));
}

// 쿠키에서 꺼낸 조각을 '믿을 수 있는 문자열'로 만든다.
//   [왜 이 함수가 필요한가]
//     사용자가 쿠키를 이렇게 고칠 수 있다:  {"message": ["배열이다"]}
//     그 상태로 (string) 을 씌우면 "Array"라는 글자가 화면에 찍히고 경고가 뜬다.
//     숫자·배열·null이 섞여 들어올 수 있다고 보고, 문자열이 아니면 버린다.
//   ★ mb_convert_encoding('UTF-8','UTF-8') = 깨진 바이트를 버리고 성한 글자만 남긴다.
//     쿠키는 아무 바이트나 실어 보낼 수 있으므로, 화면에 그리기 전에 걸러야 한다.
function handoff_str(mixed $value, int $maxLen): string {
    if (!is_string($value)) {
        return '';
    }
    return mb_substr(mb_convert_encoding($value, 'UTF-8', 'UTF-8'), 0, $maxLen);
}
