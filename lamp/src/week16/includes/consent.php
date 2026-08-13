<?php
// ============================================================
// consent.php — 쿠키 사용 '동의'
//
//   [무엇이 바뀌었나 — 확인 → 동의]
//     원래는 `cookie_notice=1` 하나였다. "안내를 읽었다"는 표시였고 버튼도 [확인]뿐이었다.
//     그런데 우리가 심는 쿠키를 목록으로 적어놓고 보니 두 무리가 섞여 있었다:
//
//       · **사용자가 시킨 것** — 정렬 탭을 눌렀으니 그 정렬을 기억한다 (pref_sort …)
//         '로그인 유지' 체크박스를 눌렀으니 로그인을 유지한다 (remember)
//         → 시킨 일을 하는 것이므로 **알려주면 된다.**
//
//       · **우리가 알아서 쌓는 것** — 검색하면 검색어가 남고, 글을 열면 그 글 번호가 남는다
//         (recent_search · recent_posts · recent_works · last_visit)
//         → **사용자는 이게 쌓이는 줄 모른다.** 이건 물어봐야 한다.
//
//   [★ 확인과 동의의 결정적 차이 — 되돌릴 수 있는가]
//     확인은 '읽었다'는 표시라 되돌릴 게 없다. 눌렀으면 끝이다.
//     동의는 허락이므로 **언제든 거둘 수 있어야 한다.**
//     그래서 버튼이 [확인] 하나면 애초에 동의가 아니다 —
//     **거절할 수 없으면 물어본 게 아니다.**
//
//   [★★ JS가 아니라 서버가 심는다 — 바뀐 이유]
//     예전엔 이 쿠키만 브라우저(JS)가 직접 심었다. 서버 왕복이 없어 빨랐기 때문이다.
//     동의로 바뀌면서 그게 문제가 됐다 — **서버가 모르는 동의는 나중에 증명할 수 없다.**
//     지금은 쿠키에 적지만, 곧 DB에도 남길 것이다(증빙용). 그때 호출부를 안 고치려고
//     저장 위치를 이 파일 안에만 두었다. (초안을 세션→DB로 옮길 때 쓴 것과 같은 방법)
//
//   [아직 남은 숙제]
//     · 설정 화면에서 **철회**하기
//     · **동의 전에는 아예 안 심기** — 지금은 배너를 처음 보는 화면에서도 recent_works가 심어진다
//       (동의는 '사전' 동의여야 한다. 다 심어놓고 나중에 받으면 순서가 틀린 것)
//     · 받은 동의를 **DB에 남기기** — 쿠키는 사용자가 지울 수 있어 증거가 못 된다
// ============================================================

const CONSENT_COOKIE = 'consent';
const CONSENT_DAYS   = 90;

// ★ 방침 버전. 안내 문구나 쿠키 목록이 바뀌면 이 숫자를 올린다.
//   그러면 옛 버전에 동의한 사람에게 배너가 다시 뜬다.
//   [왜 필요한가]
//     `cookie_notice=1` 에는 **무엇에 동의했는지가 없었다.** 목록을 바꿔도 값이 그대로 1이라
//     다시 묻지 않았다. "저는 옛날 안내에만 동의했는데요"라고 하면 반박할 수가 없다.
const CONSENT_VERSION = 1;

// 물어보는 항목. 여기 없는 쿠키는 '필수'라 묻지 않는다.
//   ★ 검색 기록을 따로 뺀 이유: **검색어는 사람이 무엇을 궁금해했는지 그 자체**라
//     열람 기록보다 민감하다. 하나로 묶으면 "글 기록은 되는데 검색은 싫다"를 고를 수 없다.
const CONSENT_ITEMS = [
    'view'   => '최근 본 글·작품, 마지막 방문 시각',   // recent_posts · recent_works · last_visit
    'search' => '최근 검색어',                          // recent_search
];

function consent_cookie_options(int $expires): array {
    return [
        'expires'  => $expires,
        'path'     => '/',
        'samesite' => 'Lax',
        'secure'   => !empty($_SERVER['HTTPS']),
        // httponly를 켠다 — 이제 JS가 만질 일이 없다. 서버만 읽고 쓴다.
        'httponly' => true,
    ];
}

// 지금 저장된 동의를 읽는다. 아직 안 물어봤으면 null.
//   ★ 반환값이 '없음(null)'과 '전부 거절([])'을 구분한다.
//     안 물어본 것과 거절한 것은 **다른 사실**이다 — 전자는 물어야 하고 후자는 물으면 안 된다.
//     (한 번 거절했는데 갈 때마다 또 묻는 건 동의를 강요하는 것이다)
//   ★ 캐시(static)를 두지 않는다 — 저장한 직후에 다시 읽는 자리가 있어서,
//     캐시가 있으면 방금 바꾼 값이 아니라 옛 값이 나온다.
function consent_state(): ?array {
    $raw = $_COOKIE[CONSENT_COOKIE] ?? null;
    if (!is_string($raw) || strlen($raw) > 300) {
        return null;
    }
    $data = json_decode($raw, true);
    if (!is_array($data)) {
        return null;                    // 고쳐놨거나 깨졌으면 '안 물어본 것'으로 본다 → 다시 묻는다
    }

    // ★ 버전이 다르면 없는 것으로 친다 → 배너가 다시 뜬다.
    if ((int) ($data['v'] ?? 0) !== CONSENT_VERSION) {
        return null;
    }

    // 우리가 아는 항목만 남긴다. 모르는 이름이 섞여 들어와도 여기서 걸러진다.
    $items = [];
    foreach (array_keys(CONSENT_ITEMS) as $key) {
        $items[$key] = !empty($data[$key]);
    }

    return $items;
}

// 언제 골랐나. 없으면 0.
//   ★ 지금은 화면에 보여주는 데만 쓴다. 하지만 증빙에서 **제일 먼저 묻는 것이 '언제'** 라
//     처음부터 담아뒀다. (DB로 옮길 때 이 값이 그대로 한 칸이 된다)
function consent_saved_at(): int {
    $raw = $_COOKIE[CONSENT_COOKIE] ?? null;
    if (!is_string($raw)) {
        return 0;
    }
    $data = json_decode($raw, true);
    $at   = is_array($data) ? ($data['at'] ?? 0) : 0;

    // 쿠키는 사용자가 고칠 수 있다 → 숫자인지 확인하고, 미래 시각이면 믿지 않는다.
    if (!is_int($at) || $at <= 0 || $at > time()) {
        return 0;
    }
    return $at;
}

// 이 항목에 동의했나? (쿠키를 심기 직전에 부른다)
//   ★ 안 물어본 상태(null)에서도 false다 — **모르면 안 심는다**가 기본값이어야 한다.
function has_consent(string $item): bool {
    $state = consent_state();
    return $state !== null && !empty($state[$item]);
}

// 배너를 띄워야 하나?
function needs_consent(): bool {
    return consent_state() === null;
}

// 고른 결과를 저장한다. $items = ['view' => true, 'search' => false] 모양.
//   ★ 아직은 쿠키다. 곧 DB에도 남긴다 — 그때 이 함수 안만 고치면 된다.
//     `at`(고른 시각)은 지금 쓰이지 않지만 넣어둔다. 증빙에서 제일 먼저 묻는 것이 '언제'다.
function save_consent(array $items): void {
    $data = ['v' => CONSENT_VERSION, 'at' => time()];
    foreach (array_keys(CONSENT_ITEMS) as $key) {
        $data[$key] = !empty($items[$key]) ? 1 : 0;
    }
    $json = (string) json_encode($data);

    setcookie(CONSENT_COOKIE, $json, consent_cookie_options(time() + CONSENT_DAYS * 86400));
    $_COOKIE[CONSENT_COOKIE] = $json;   // ★ 이 요청에서도 곧바로 새 값이 읽히게
}

// 동의를 지운다 (= 안 물어본 상태로 되돌린다). 철회 화면에서 쓸 예정.
function forget_consent(): void {
    unset($_COOKIE[CONSENT_COOKIE]);
    setcookie(CONSENT_COOKIE, '', consent_cookie_options(time() - 3600));
}
