<?php
// ============================================================
// devices.php — '로그인한 기기' 목록
//
//   [★ 왜 세션과 따로 두나 — 두 가지를 혼동했었다]
//     처음엔 `sessions` 표를 그대로 기기 목록으로 썼다. 세션 한 줄 = 기기 하나로 본 것이다.
//     그런데 성격이 완전히 다르다:
//       · 세션 = **'지금 이 방문'** — 30분이면 죽는다. 요청마다 읽고 쓰는 뜨거운 값
//       · 기기 = **'회원의 기록'**  — 로그아웃해도 남아야 한다. 가끔 보는 차가운 값
//
//     혼동한 대가가 컸다:
//       ① **만료되면 목록에서 사라진다** → "지난주 PC방 로그인을 끊고 싶다"가 안 된다.
//          **기기 목록의 가장 큰 용도**인데 정작 그걸 못 했다.
//       ② **번호표 회전과 충돌한다** → 요청마다 id_hash가 바뀌니 한 줄이 매번 다른 걸 가리킨다.
//
//   [★★ 기준을 다시 대면 답이 나온다 — "이 값의 주인이 누구인가"]
//     브라우저면 쿠키 / 이 방문이면 세션 / **회원이면 DB 표.**
//     기기 기록은 세션이 죽어도 남아야 하므로 **회원의 것**이다.
//
//   [실무가 이렇게 하는 이유]
//     큰 서비스는 세션을 Redis에 둔다 → `sessions` 표 자체가 없다.
//     그래도 '내 기기 목록'은 멀쩡히 동작한다. **애초에 다른 표에 있기 때문**이다.
//     → 이 구조는 **세션을 어디에 두든 상관없다.**
//
//   [기기를 알아보는 방법]
//     오래 사는 무작위 값을 쿠키에 심는다. consent_id와 **완전히 같은 구조** —
//     쿠키엔 뜻 없는 열쇠, 내용은 전부 서버에.
//
//   [★ 이 쿠키는 '기기 목록' 전용이다]
//     예전엔 이 하나가 **조회수 중복 방지**도 겸했다. 편했지만 규정에 걸렸다 —
//     *"쿠키가 여러 목적을 가지면 모든 목적이 면제여야 동의가 필요 없다"*(WP194).
//     기기 목록은 보안이라 면제지만 조회수 측정은 아니어서, **섞여 있으면 면제가 깨진다.**
//     → 조회수 쪽은 `includes/view_id.php`로 떼어내고 **동의를 받는다.**
// ============================================================

require_once __DIR__ . '/db.php';

const DEVICE_COOKIE = 'device';
// 기기 이름표는 세션과 정반대로 오래 살아야 한다. 다만 **얼마나 오래인지도 근거가 필요하다.**
//   📄 Article 29 WP Opinion 04/2012 — 보안 쿠키는 *"제한된 기간"*(a limited persistent duration).
//      https://ec.europa.eu/justice/article-29/documentation/opinion-recommendation/files/2012/wp194_en.pdf
//   ★ 1년은 '제한된'이라 부르기 어렵다. 이 쿠키가 하는 일("지난달 PC방 로그인을 끊고 싶다")에
//     필요한 만큼이면 되므로 **90일**로 줄였다.
const DEVICE_DAYS   = 90;

function device_cookie_options(int $expires): array {
    return [
        'expires'  => $expires,
        'path'     => '/',
        'httponly' => true,      // JS가 만질 일이 없다
        'samesite' => 'Lax',
        'secure'   => !empty($_SERVER['HTTPS']),
    ];
}

// 이 브라우저의 기기 번호. 없으면 만들어 심는다.
//   ★ 로그인 여부와 무관하게 브라우저마다 하나씩 갖는다.
//     ※ 이것도 결국 식별자다. 그래서 **이름표 말고는 아무것도 담지 않는다.**
function device_id(): string {
    $id = $_COOKIE[DEVICE_COOKIE] ?? '';

    if (!is_string($id) || !preg_match('/^[a-f0-9]{32}$/', $id)) {
        $id = bin2hex(random_bytes(16));
        setcookie(DEVICE_COOKIE, $id, device_cookie_options(time() + DEVICE_DAYS * 86400));
        $_COOKIE[DEVICE_COOKIE] = $id;      // 이 요청에서도 곧바로 쓰이게
    }
    return $id;
}

// 로그인할 때 부른다. 이 기기를 회원의 목록에 올리거나, 이미 있으면 시각만 갱신한다.
//   ★ 처음 보는 기기면 announced=0 으로 남는다 → 주인에게 한 번 알린다.
//     이미 있던 기기면 announced는 건드리지 않는다(다시 알릴 이유가 없다).
function remember_device(int $userId): void {
    db()->prepare(
        'INSERT INTO user_devices (user_id, device_id, user_agent, first_seen_at, last_login_at, announced)
         VALUES (?, ?, ?, NOW(), NOW(), 0)
         ON DUPLICATE KEY UPDATE last_login_at = NOW(), user_agent = VALUES(user_agent)'
    )->execute([
        $userId,
        device_id(),
        mb_substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 255),
    ]);
}

// 이 브라우저가 '이 회원의 등록된 기기'인가?
//   ★ 로그인을 거친 기기만 user_devices에 줄이 생긴다.
//     쿠키만 훔쳐 온 브라우저는 로그인을 한 적이 없으므로 **여기서 걸린다.**
function is_known_device(int $userId): bool {
    $stmt = db()->prepare('SELECT 1 FROM user_devices WHERE user_id = ? AND device_id = ?');
    $stmt->execute([$userId, device_id()]);

    return $stmt->fetchColumn() !== false;
}

// 아직 안 알린 '새 기기'가 있으면 그 목록을 돌려주고, 알렸다고 표시한다.
//   ★★ **지금 이 기기는 뺀다.** 방금 자기가 로그인한 걸 자기에게 알려봐야 소용없다.
//     알아야 할 사람은 **다른 곳에 있는 주인**이다.
//     → 그래서 주인이 **어느 기기로 접속하든** 그때 한 번 뜬다.
//   ★ 읽으면서 표시를 바꾼다(read-once) — 플래시와 같은 구조다.
function take_new_devices(int $userId): array {
    // ★★ 등록된 기기가 아니면 **아무것도 보여주지 않는다.**
    //   [왜 이 줄이 생겼나 — 시연하다 발견했다]
    //     세션 쿠키를 훔쳐 붙여넣은 브라우저에서 *"새 기기에서 로그인됨"* 알림이 떴다.
    //     그런데 이 알림은 **한 번 보이면 소비된다**(announced=1) →
    //     ★ 훔친 쪽이 알림을 보고, **정작 주인은 못 보게 된다.**
    //       못 잡는 데서 그치지 않고 **증거를 먹어치우고 있었다.**
    //
    //   [왜 이 조건이면 되나]
    //     훔친 브라우저에는 기기 줄이 없다(로그인을 한 적이 없으므로).
    //     → 알림을 **보지도 못하고 소비하지도 못한다.** 주인 쪽에 그대로 남는다.
    //     덤으로 "이 계정에 다른 기기가 있다"는 사실도 안 새어 나간다.
    if (!is_known_device($userId)) {
        return [];
    }

    $stmt = db()->prepare(
        'SELECT user_agent, UNIX_TIMESTAMP(first_seen_at) AS at
           FROM user_devices
          WHERE user_id = ? AND announced = 0 AND device_id <> ?'
    );
    $stmt->execute([$userId, device_id()]);
    $rows = $stmt->fetchAll();

    if ($rows) {
        db()->prepare('UPDATE user_devices SET announced = 1 WHERE user_id = ? AND device_id <> ?')
            ->execute([$userId, device_id()]);
    }
    return $rows;
}

// 이 회원의 기기 목록. 최근 로그인 순.
//   ★ 세션이 살아 있든 죽었든 **전부 나온다.** 그게 sessions 기반과의 결정적 차이다.
function list_devices(int $userId): array {
    $stmt = db()->prepare(
        // ★ 도장 자체(공개키)는 안 꺼낸다. 화면에 필요한 건 "있나 없나"뿐이다.
        //   필요 없는 값을 끌고 나오면 언젠가 그걸 어딘가에 찍게 된다.
        'SELECT device_id, user_agent,
                public_key IS NOT NULL AS has_key,
                UNIX_TIMESTAMP(key_added_at)  AS key_at,
                UNIX_TIMESTAMP(first_seen_at) AS first_at,
                UNIX_TIMESTAMP(last_login_at) AS last_at
           FROM user_devices
          WHERE user_id = ?
          ORDER BY last_login_at DESC'
    );
    $stmt->execute([$userId]);

    return $stmt->fetchAll();
}

// 기기 하나를 끊는다. 목록에서 지우고 **그 기기의 세션까지 함께** 끊는다.
//   ★ 표만 지우면 이미 열려 있는 창은 멀쩡히 살아 있다 —
//     로그아웃에서 밟았던 것과 같은 함정이다. "지울 것이 두 곳"이면 둘 다 지워야 한다.
//   ★ user_id 조건이 반드시 붙는다. 남의 기기 번호를 실어 보내도 안 지워진다.
function revoke_device(int $userId, string $deviceId): bool {
    if (!preg_match('/^[a-f0-9]{32}$/', $deviceId)) {
        return false;
    }

    $stmt = db()->prepare('DELETE FROM user_devices WHERE user_id = ? AND device_id = ?');
    $stmt->execute([$userId, $deviceId]);

    if ($stmt->rowCount() === 0) {
        return false;                 // 내 기기가 아니거나 이미 없다
    }

    db()->prepare('DELETE FROM sessions WHERE user_id = ? AND device_id = ?')
        ->execute([$userId, $deviceId]);

    return true;
}

// user_agent 문자열을 사람이 읽는 이름으로. ("Chrome · Windows")
//   ★ 어디까지나 **표시용**이다. 이 값으로 판정하지 않는다 —
//     사용자가 마음대로 보낼 수 있고, 브라우저 업데이트로도 바뀌기 때문이다.
function describe_device(?string $ua): string {
    $ua = (string) $ua;
    if ($ua === '') {
        return '알 수 없는 기기';
    }

    // 순서가 중요하다 — Edge·삼성 브라우저는 UA에 'Chrome'을 함께 달고 다닌다.
    $browsers = ['Edg' => 'Edge', 'SamsungBrowser' => '삼성 인터넷', 'OPR' => 'Opera',
                 'Chrome' => 'Chrome', 'Safari' => 'Safari', 'Firefox' => 'Firefox'];
    $systems  = ['Windows' => 'Windows', 'Android' => 'Android', 'iPhone' => 'iPhone',
                 'iPad' => 'iPad', 'Mac OS X' => 'macOS', 'Linux' => 'Linux'];

    $browser = '브라우저';
    foreach ($browsers as $needle => $label) {
        if (str_contains($ua, $needle)) { $browser = $label; break; }
    }
    $system = '';
    foreach ($systems as $needle => $label) {
        if (str_contains($ua, $needle)) { $system = $label; break; }
    }

    return $system === '' ? $browser : $browser . ' · ' . $system;
}
