<?php
// ============================================================
// remember.php — '로그인 유지'(자동 로그인)
//
//   [왜 필요한가 — 세션의 한계]
//     세션은 브라우저를 닫으면 사라진다. 그게 세션의 성질이고, 공용 PC를 생각하면 옳다.
//     하지만 내 PC에서는 "다음에도 로그인돼 있길" 바란다.
//     → 브라우저에 **오래 남는 쿠키**를 따로 주고, 그 쿠키로 세션을 되살린다.
//
//   ★★ 세션과 쿠키의 역할이 여기서 딱 갈린다:
//     · 세션 = 짧고 안전한 '지금 이 방문'   (서버가 들고 있다)
//     · 쿠키 = 길지만 못 믿을 '다음에 또 올게' (사용자 PC에 있다)
//     그래서 쿠키에는 **신원이 아니라 '표'만** 담고, 그 표가 맞는지는 서버가 확인한다.
//
//   [담는 방식]
//     ❌ 쿠키에 아이디·비밀번호   → 사용자가 고칠 수 있다. week15의 ?as= 와 같은 실수.
//     ⭕ 쿠키에 무작위 토큰       → 서버 DB에는 그 토큰의 '지문'(해시)만 저장.
//        고쳐봐야 우리 표에 없는 값이라 통과 못 하고, DB가 유출돼도 원래 토큰을 못 되돌린다.
//
//   ─────────────────────────────────────────────────────────
//   [★★★ 인정하고 시작하는 것 — 탈취 자체는 막을 수 없다]
//     이 방식은 **bearer(소지자) 토큰**이다. 지하철 표와 같아서 **누가 들고 있든 통과**시킨다.
//     쿠키는 사용자 기기에 있고, 그 기기가 뚫리면 우리가 할 수 있는 일이 없다.
//     (완전히 끊으려면 '쿠키를 가져도 소용없게' 만들어야 하는데, 그게 DBSC다.
//      HTTPS 전용이고 localhost 예외가 없어 이 프로젝트에서는 못 쓴다)
//
//     그래서 목표를 바꾼다:
//       ① 훔칠 기회를 줄인다   — httponly · samesite · secure
//       ② 훔쳐도 값을 낮춘다   — 자동 로그인은 '약한 신원'이라 민감 작업을 못 한다 (auth.php)
//       ③ ★ 훔친 걸 알아챈다  — **재사용 탐지.** 이 파일의 핵심이다
//
//   [★ 재사용 탐지 — 회전만으로는 부족했다]
//     예전에도 회전(쓸 때마다 새 토큰)은 했다. 그런데 회전할 때 **행을 지웠기 때문에**,
//     훔친 옛 토큰이 다시 나타나도 '없는 토큰'으로 무시할 뿐이었다.
//     "옛날에 유효했던 것"과 "그냥 가짜"를 구분 못 했다 — **도난 신호를 버리고 있었다.**
//
//     지금은 쿠키를 두 조각으로 나눈다:   series : token
//       · series = 이 기기의 **로그인 계열** ID. 회전해도 안 바뀐다 (평문, 찾는 열쇠)
//       · token  = 접속마다 갈리는 값       (지문만 저장, 증명하는 값)
//     회전은 DELETE가 아니라 **token_hash만 UPDATE** 한다 → 계열이 살아남는다.
//
//     판정이 셋으로 갈린다:
//       ① series ○ · token ○ → 정상. 토큰만 회전
//       ② series ○ · token ✗ → ★ **도난.** 같은 계열이 두 갈래로 갈라졌다 → 전부 폐기
//       ③ series ✗           → 만료/위조. 조용히 무시
//
//   [★★ ②일 때 왜 '한쪽'이 아니라 '전부'인가]
//     갈라졌다는 것만 알 뿐, **어느 쪽이 주인이고 어느 쪽이 도둑인지 서버는 모른다.**
//     한쪽만 살리면 도둑을 살릴 수도 있다. 둘 다 끊고 비밀번호를 다시 묻는 것이
//     유일하게 안전한 대응이다.
// ============================================================

require_once __DIR__ . '/db.php';
require_once __DIR__ . '/session.php';
require_once __DIR__ . '/session_db.php';   // 도난 판정 시 세션까지 함께 끊으려고

// 쿠키 이름. 값은 'series:token' 두 조각이다.
const REMEMBER_COOKIE = 'remember';

// 며칠 동안 유지할지.
//   ★ 이제 이건 **절대 상한**이다 — 회전해도 늘어나지 않는다.
//     예전에는 회전할 때마다 다시 30일을 줘서, 30일에 한 번만 들어와도 영원히 살았다.
//   (NIST SP 800-63B는 단일 요소 인증의 재인증 상한을 30일로 본다)
const REMEMBER_DAYS = 30;

// 도난이 감지되면 이 알림을 남긴다. 세션을 전부 끊으므로 **세션에는 못 적는다.**
//   ★ 알림을 쿠키로 옮겨둔 덕을 여기서 본다 — 세션이 통째로 사라져도 이유를 전할 수 있다.
//     (세션에 있었다면 "로그아웃됐는데 왜인지 모르는" 상태가 됐을 것이다)
const REMEMBER_THEFT_MESSAGE =
    '⚠️ 다른 곳에서 같은 로그인 정보가 사용되어 모든 기기에서 로그아웃했습니다. 다시 로그인해 주세요.';

// ── 새 계열을 만든다 (비밀번호로 로그인해서 '로그인 유지'를 켰을 때) ──
function remember_issue(int $userId): void {
    // 만료된 표를 먼저 치운다. cron이 없으므로 누가 로그인할 때 같이 청소한다.
    db()->query('DELETE FROM remember_tokens WHERE expires_at < NOW()');

    $series = bin2hex(random_bytes(16));   // 32글자. 찾는 열쇠라 비밀이 아니다
    $token  = bin2hex(random_bytes(32));   // 64글자. 증명하는 값이라 지문만 저장한다

    $now       = time();
    $expiresAt = $now + REMEMBER_DAYS * 86400;

    db()->prepare(
        'INSERT INTO remember_tokens (user_id, series, token_hash, issued_at, expires_at, last_used_at, user_agent)
         VALUES (?, ?, ?, ?, ?, ?, ?)'
    )->execute([
        $userId,
        $series,
        hash('sha256', $token),
        date('Y-m-d H:i:s', $now),
        date('Y-m-d H:i:s', $expiresAt),
        date('Y-m-d H:i:s', $now),
        mb_substr((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''), 0, 255),
    ]);

    remember_put_cookie($series, $token, $expiresAt);
}

// ── 쿠키를 확인한다. 통과하면 회원 번호, 아니면 0 ──────────────
//   ⚠️ 쿠키를 굽고 세션을 건드리므로 **출력이 시작되기 전에** 불려야 한다.
//      (auth.php가 파일을 로드하는 시점에 부른다)
function remember_lookup(): int {
    [$series, $token] = remember_split_cookie();
    if ($series === '') {
        return 0;
    }

    // ★ series로 행을 찾는다. token으로 찾지 않는다 —
    //   token으로 찾으면 '틀린 토큰'과 '없는 계열'이 똑같이 "행 없음"이 되어 ②를 판정할 수 없다.
    $stmt = db()->prepare('SELECT id, user_id, token_hash, expires_at FROM remember_tokens WHERE series = ?');
    $stmt->execute([$series]);
    $row = $stmt->fetch();

    // ③ 계열 자체가 없다 — 만료됐거나 지어낸 값이다. 쓸모없는 쿠키를 떼어준다.
    if ($row === false) {
        remember_clear_cookie();
        return 0;
    }

    // 만료된 계열은 도난이 아니라 그냥 수명이 다한 것이다. 조용히 정리한다.
    if (strtotime((string) $row['expires_at']) <= time()) {
        db()->prepare('DELETE FROM remember_tokens WHERE id = ?')->execute([$row['id']]);
        remember_clear_cookie();
        return 0;
    }

    // ★★ ② 계열은 있는데 토큰이 다르다 = 같은 계열이 두 갈래로 갈라졌다 = 도난.
    //   hash_equals = 타이밍 공격을 막는 비교. CSRF 토큰을 비교할 때와 같은 이유다.
    if (!hash_equals((string) $row['token_hash'], hash('sha256', $token))) {
        remember_handle_theft((int) $row['user_id']);
        return 0;
    }

    // ① 정상 — 토큰만 갈아 끼운다.
    //   ★ DELETE가 아니라 UPDATE다. series·issued_at·expires_at은 그대로 둔다.
    //     series가 남아야 다음에 옛 토큰이 오면 ②로 잡힌다.
    //     expires_at을 안 건드려야 수명이 무한정 늘어나지 않는다.
    $newToken = bin2hex(random_bytes(32));
    db()->prepare('UPDATE remember_tokens SET token_hash = ?, last_used_at = NOW() WHERE id = ?')
        ->execute([hash('sha256', $newToken), $row['id']]);

    remember_put_cookie($series, $newToken, strtotime((string) $row['expires_at']));

    return (int) $row['user_id'];
}

// ── 도난 대응 — 이 회원의 모든 것을 끊는다 ────────────────────
//   ★ 세션까지 함께 끊어야 실효가 있다. 표만 지우면 이미 열려 있는 세션은 멀쩡히 살아 있다.
//   ★ 알림은 쿠키(flash)에 남는다 → 세션을 전부 없애도 이유를 전할 수 있다.
function remember_handle_theft(int $userId): void {
    remember_forget_all($userId);
    destroy_all_sessions($userId);
    remember_clear_cookie();

    // 지금 이 브라우저의 세션도 비운다. (도둑일 수도, 주인일 수도 있다 — 둘 다 끊는다)
    $_SESSION = [];

    set_flash(REMEMBER_THEFT_MESSAGE, 'error');

    // ★★ 반드시 리다이렉트한다. 그냥 두면 알림이 **한 박자 늦게** 뜬다.
    //   [왜 그런가]
    //     알림은 쿠키로 나르는데, 화면을 그리는 take_flash()는 **이번 요청에 들어온 쿠키**를 읽는다.
    //     지금 막 set_flash로 구운 쿠키는 **다음 요청부터** 들어온다.
    //     → 리다이렉트를 안 하면 이번 화면은 "이유 없이 로그아웃된 화면"이 되고,
    //       사용자가 한 번 더 눌러야 그제야 이유가 나온다. 그때는 이미 안 누를 수도 있다.
    //   ★ 이게 PRG(POST-리다이렉트-GET)를 쓰는 이유와 정확히 같다 —
    //     '무언가를 정한 응답'과 '그 결과를 보여주는 응답'을 나누는 것이다.
    //   ※ 무한 반복 걱정은 없다: 쿠키를 이미 회수했으므로 다음 요청은 여기까지 오지 않는다.
    redirect('/');
}

// ── 로그아웃: 이 계열만 지운다 ────────────────────────────────
//   ★ 쿠키만 지우면 DB에 표가 남아 '유효한 표'가 떠돈다. 둘 다 지워야 진짜 로그아웃이다.
function remember_forget(): void {
    [$series] = remember_split_cookie();
    if ($series !== '') {
        db()->prepare('DELETE FROM remember_tokens WHERE series = ?')->execute([$series]);
    }
    remember_clear_cookie();
}

// ── 이 회원의 계열을 전부 버린다 ──────────────────────────────
//   '다른 기기에서 모두 로그아웃' · 비밀번호 변경 · 도난 탐지에서 부른다.
//   ★ 세션만 끊고 이걸 안 하면 소용없다 — 다른 기기의 remember 쿠키가 다음 접속에
//     세션을 새로 만들어 **다시 로그인시켜 버린다.**
//   반환값 = 버린 계열 수.
function remember_forget_all(int $userId): int {
    $stmt = db()->prepare('DELETE FROM remember_tokens WHERE user_id = ?');
    $stmt->execute([$userId]);
    return $stmt->rowCount();
}

// ── 쿠키 다루기 ───────────────────────────────────────────────

// 'series:token' 으로 쪼갠다. 모양이 안 맞으면 ['', ''].
//   ★ 모양 검사를 여기서 한 번에 한다 — 16진수인지, 길이가 맞는지.
//     아무 문자열이나 그대로 DB 조회에 넘기지 않기 위해서다.
function remember_split_cookie(): array {
    $raw = $_COOKIE[REMEMBER_COOKIE] ?? '';
    if (!is_string($raw) || substr_count($raw, ':') !== 1) {
        return ['', ''];
    }
    [$series, $token] = explode(':', $raw, 2);

    if (!preg_match('/^[a-f0-9]{32}$/', $series) || !preg_match('/^[a-f0-9]{64}$/', $token)) {
        return ['', ''];
    }
    return [$series, $token];
}

function remember_put_cookie(string $series, string $token, int $expiresAt): void {
    setcookie(REMEMBER_COOKIE, $series . ':' . $token, remember_cookie_options($expiresAt));
}

// 쿠키에 붙일 안전장치. 발급할 때와 지울 때가 '한 글자도 다르면 안 되므로' 한곳에서 만든다.
//   ★ 옵션이 하나라도 다르면 브라우저가 '다른 쿠키'로 보고, 지우려던 옛 쿠키가 그대로 남는다.
function remember_cookie_options(int $expires): array {
    return [
        'expires'  => $expires,
        'path'     => '/',
        // httponly = JS가 못 읽는다 → 글에 <script>가 새어 들어가도(XSS) 토큰을 못 훔쳐간다.
        //   세션 번호표보다 오래 사는 값이라 이 방어가 더 중요하다.
        'httponly' => true,
        // samesite = 다른 사이트에서 시작된 요청에는 안 실린다 (CSRF 완화).
        'samesite' => 'Lax',
        // secure = https에서만. localhost(http)에서는 켜면 아예 저장이 안 되므로 접속 방식에 맞춘다.
        'secure'   => !empty($_SERVER['HTTPS']),
    ];
}

// 쿠키 회수 — 만료 시각을 과거로 줘서 브라우저가 스스로 버리게 한다.
function remember_clear_cookie(): void {
    setcookie(REMEMBER_COOKIE, '', remember_cookie_options(time() - 42000));
}
