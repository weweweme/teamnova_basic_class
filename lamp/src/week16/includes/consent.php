<?php
// ============================================================
// consent.php — 쿠키 사용 '동의'
//
//   [무엇이 바뀌었나 — 확인 → 동의]
//     원래는 `cookie_notice=1` 하나였다. "안내를 읽었다"는 표시였고 버튼도 [확인]뿐이었다.
//     그런데 우리가 심는 쿠키를 목록으로 적어놓고 보니 두 무리가 섞여 있었다:
//
//       · **사용자가 시킨 것** — 정렬 탭을 눌렀으니 그 정렬을 기억한다 (pref_sort …)
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
//
//   [★★★ 그래서 같은 사실이 두 군데에 있다 — 역할이 다르다]
//     · **쿠키** = 판단용 캐시. "배너를 띄울까? 이 쿠키를 심어도 되나?"를 DB 왕복 없이 답한다
//     · **consent_log 표** = 증빙 원본. 지워지지 않고 쌓이기만 한다
//     쿠키가 사라져도 사실은 안 사라진다. 그게 이 구조의 전부다.
//
//   [갖춰진 것 — 하나라도 빠지면 '동의'가 아니다]
//     ① 거절할 수 있다        — 버튼이 [확인] 하나면 물어본 게 아니다
//     ② 항목별로 고를 수 있다  — 검색 기록과 열람 기록을 따로
//     ③ 철회할 수 있다        — 비회원도. `/cookies.php` (그래서 settings/ 안에 두지 않았다)
//     ④ 동의 전엔 안 심는다   — `has_consent()` 게이트가 '모르면 안 심는다'를 기본값으로
//     ⑤ 증빙이 남는다         — `consent_log` (append-only)
// ============================================================

// 증빙은 서버에 남긴다 — 쿠키는 사용자가 지울 수 있어 증거가 못 되기 때문이다.
require_once __DIR__ . '/db.php';

const CONSENT_COOKIE = 'consent';
const CONSENT_DAYS   = 90;

// ★ 방침 버전. 안내 문구나 쿠키 목록이 바뀌면 이 숫자를 올린다.
//   그러면 옛 버전에 동의한 사람에게 배너가 다시 뜬다.
//   [왜 필요한가]
//     `cookie_notice=1` 에는 **무엇에 동의했는지가 없었다.** 목록을 바꿔도 값이 그대로 1이라
//     다시 묻지 않았다. "저는 옛날 안내에만 동의했는데요"라고 하면 반박할 수가 없다.
// ★ 2로 올렸다 — 항목에 'stats'가 늘었기 때문이다.
//   버전을 안 올리면 **이미 동의한 사람에게는 새 항목을 묻지 않고 지나간다.**
//   "무엇에 동의했는지"가 달라졌으면 다시 물어야 한다.
const CONSENT_VERSION = 2;

// 물어보는 항목. 여기 없는 쿠키는 '필수'라 묻지 않는다.
//   ★ 검색 기록을 따로 뺀 이유: **검색어는 사람이 무엇을 궁금해했는지 그 자체**라
//     열람 기록보다 민감하다. 하나로 묶으면 "글 기록은 되는데 검색은 싫다"를 고를 수 없다.
const CONSENT_ITEMS = [
    'view'   => '최근 본 글·작품, 마지막 방문 시각',   // recent_posts · recent_works · last_visit
    'search' => '최근 검색어',                          // recent_search
    // ★ 나중에 추가된 항목. 원래는 device 쿠키가 이 일을 겸했는데,
    //   "여러 목적이면 전부 면제여야 한다"는 규정에 걸려 **쿠키를 쪼개고 물어보게** 바꿨다.
    //   (근거와 판단은 includes/view_id.php 주석에)
    'stats'  => '조회수 중복 방지 (같은 글을 하루 한 번만 세기)',   // viewer
];

// ── 이 브라우저의 동의 식별자 ────────────────────────────────
//   [왜 필요한가]
//     증빙은 DB에 남기는데, 배너는 **로그인 안 한 사람에게도** 뜬다.
//     "누가 눌렀는지"를 적을 칸이 있어야 하므로 무작위 번호를 하나 만들어 쿠키에 넣는다.
//   ★ 아이러니 — **동의 기록을 남기려고 만든 식별자 자체가 수집이다.**
//     그래서 이것 말고는 아무것도 안 담는다. 뜻 없는 무작위 값 하나뿐이다.
//   ★ 세션 번호표와 모양이 같지만 **성격이 다르다** — 저건 훔치면 그 사람이 되는 열쇠고,
//     이건 훔쳐봐야 같은 동의 화면을 볼 뿐이다. 그래서 DB에 지문이 아니라 원본을 담는다.
function consent_id(): string {
    $data = consent_payload();
    $id   = $data['id'] ?? '';

    // 모양이 안 맞으면(없거나 고쳐놨거나) 새로 만든다.
    if (!is_string($id) || !preg_match('/^[a-f0-9]{32}$/', $id)) {
        $id = bin2hex(random_bytes(16));
    }
    return $id;
}

// 쿠키에 든 원본 배열. 없거나 깨졌으면 빈 배열.
function consent_payload(): array {
    $raw = $_COOKIE[CONSENT_COOKIE] ?? null;
    if (!is_string($raw) || strlen($raw) > 300) {
        return [];
    }
    $data = json_decode($raw, true);

    return is_array($data) ? $data : [];
}

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
//   ★ **쿠키와 DB 양쪽에 적는다.** 쿠키는 다음 요청부터 빨리 판단하려고,
//     DB는 나중에 증명하려고. 호출부는 그 사정을 몰라도 된다 — 이 함수 하나만 부르면 된다.
function save_consent(array $items, string $source = 'banner', ?int $userId = null): void {
    // ★ 식별자는 '고르기 전에' 정한다 — 아래에서 쿠키를 덮어쓰기 때문이다.
    //   이미 있으면 그대로 이어 쓴다 → 같은 브라우저의 이력이 한 줄로 이어진다.
    $id = consent_id();

    $data = ['v' => CONSENT_VERSION, 'at' => time(), 'id' => $id];
    foreach (array_keys(CONSENT_ITEMS) as $key) {
        $data[$key] = !empty($items[$key]) ? 1 : 0;
    }
    $json = (string) json_encode($data);

    setcookie(CONSENT_COOKIE, $json, consent_cookie_options(time() + CONSENT_DAYS * 86400));
    $_COOKIE[CONSENT_COOKIE] = $json;   // ★ 이 요청에서도 곧바로 새 값이 읽히게

    // ★★ 그리고 서버에도 남긴다. 쿠키는 사용자가 지울 수 있어 증거가 못 되기 때문이다.
    //   전부 껐으면 '철회'로 기록한다 — 동의와 철회는 다른 사건이다.
    log_consent($id, $userId, $items, count(array_filter($items)) > 0 ? 'grant' : 'revoke', $source);
}

// ── 증빙 남기기 ──────────────────────────────────────────────
//   ★★ 이 표는 **고치지 않는다(append-only).** 바꾸든 철회하든 새 줄을 넣는다.
//     "동의했다가 철회함"과 "동의한 적 없음"은 **다른 사실**이라, 덮어쓰면 그 차이가 사라진다.
//   ★ 실패해도 화면을 막지 않는다 — 동의 자체는 이미 쿠키에 반영됐다.
//     여기서 예외를 던지면 "동의를 눌렀는데 화면이 깨진다"가 된다. 기록은 부수적인 일이다.
//     (실무라면 이 자리에서 별도 로그를 남겨 나중에 확인한다)
function log_consent(string $consentId, ?int $userId, array $items, string $action, string $source): void {
    $snapshot = [];
    foreach (array_keys(CONSENT_ITEMS) as $key) {
        $snapshot[$key] = !empty($items[$key]) ? 1 : 0;
    }

    try {
        db()->prepare(
            'INSERT INTO consent_log (consent_id, user_id, action, source, policy_version, items, user_agent)
             VALUES (?, ?, ?, ?, ?, ?, ?)'
        )->execute([
            $consentId,
            $userId,
            $action,
            $source,
            CONSENT_VERSION,
            (string) json_encode($snapshot),
            mb_substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 255),
        ]);
    } catch (Throwable) {
        // 삼킨다. 위 주석 참고.
    }
}

// 이 브라우저(또는 이 회원)의 동의 이력. 최근 것이 먼저.
//   ★ 화면에 보여주려고 만든 함수다 — **증빙은 보여줄 수 있어야 의미가 있다.**
//     "기록은 남기고 있습니다"라고 말만 하는 것과, 그 줄을 화면에 띄우는 것은 다르다.
function consent_history(string $consentId, ?int $userId, int $limit = 10): array {
    try {
        // 이 브라우저의 것 + (로그인했으면) 이 회원이 다른 기기에서 고른 것까지.
        $stmt = db()->prepare(
            'SELECT action, source, policy_version, items, user_id, UNIX_TIMESTAMP(created_at) AS at
               FROM consent_log
              WHERE consent_id = ? OR (? IS NOT NULL AND user_id = ?)
              ORDER BY id DESC
              LIMIT ' . (int) $limit
        );
        $stmt->execute([$consentId, $userId, $userId]);

        return $stmt->fetchAll();
    } catch (Throwable) {
        return [];
    }
}

// 로그인했을 때 '이 브라우저의 동의 = 이 회원의 것'을 이어 붙인다.
//   ★ 기존 줄을 UPDATE 하지 않고 **새 줄('link')을 넣는다.** append-only를 지키기 위해서다.
//     기록되는 사실도 다르다 — "그때 동의했다"가 아니라 "이 시점에 회원과 연결됐다"이다.
//   ★ 이미 연결된 브라우저면 아무 일도 하지 않는다 (로그인할 때마다 줄이 쌓이지 않게).
function link_consent_to_user(int $userId): void {
    $state = consent_state();
    if ($state === null) {
        return;                         // 아직 아무것도 안 골랐으면 이을 것이 없다
    }
    $id = consent_id();

    try {
        $stmt = db()->prepare('SELECT user_id FROM consent_log WHERE consent_id = ? ORDER BY id DESC LIMIT 1');
        $stmt->execute([$id]);
        $last = $stmt->fetch();

        if ($last === false || (int) ($last['user_id'] ?? 0) === $userId) {
            return;                     // 기록이 없거나 이미 이 회원 것이면 그대로 둔다
        }
    } catch (Throwable) {
        return;
    }

    log_consent($id, $userId, $state, 'link', 'login');
}

// 동의를 지운다 (= 안 물어본 상태로 되돌린다).
//   ★ **쿠키만 지운다. 서버 기록은 그대로 둔다.**
//     여기서 DB 줄까지 지우면 "동의를 지우면 증빙도 지워진다"가 되어, 증빙이 아니게 된다.
//     대신 '지웠다'는 사실 자체를 새 줄로 남긴다 — 그것도 사건이다.
function forget_consent(?int $userId = null): void {
    $id    = consent_id();
    $state = consent_state();

    unset($_COOKIE[CONSENT_COOKIE]);
    setcookie(CONSENT_COOKIE, '', consent_cookie_options(time() - 3600));

    if ($state !== null) {
        log_consent($id, $userId, [], 'reset', 'settings');
    }
}
