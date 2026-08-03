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
//   [쓰고 나면 바꾼다 — 회전(rotation)]
//     토큰을 한 번 쓸 때마다 새 토큰으로 갈아 끼우고 옛것은 지운다.
//     훔친 토큰이 오래 살아남지 못하게 하는, 실무에서 표준으로 쓰는 방법이다.
// ============================================================

require_once __DIR__ . '/db.php';
require_once __DIR__ . '/session.php';

// 쿠키 이름. 값은 '토큰 문자열' 하나뿐이다.
const REMEMBER_COOKIE = 'remember';

// 며칠 동안 유지할지. 쿠키 만료와 DB의 expires_at 을 같은 값으로 맞춘다.
//   ★ 둘 다 두는 이유: 쿠키 만료는 브라우저가 지키는 약속이라 사용자가 늘려버릴 수 있다.
//     서버 쪽(expires_at)이 진짜 기준이고, 쿠키 만료는 '보통은 알아서 사라지게' 하는 편의다.
const REMEMBER_DAYS = 30;

// 새 토큰을 만들어 DB에 지문을 남기고 브라우저에 쿠키를 굽는다.
//   로그인할 때 '로그인 유지'를 체크했으면 호출한다.
function remember_issue(int $userId): void {
    // 만료된 표를 먼저 치운다.
    //   cron(정해진 시각에 도는 자동 작업)이 없으므로, 누가 로그인할 때 같이 청소한다.
    //   휴지통(purge_expired_trash)에서 쓴 것과 같은 방식이다.
    db()->query('DELETE FROM remember_tokens WHERE expires_at < NOW()');

    // 추측 불가능한 토큰. 32바이트(256비트)를 16진수 64글자로.
    //   ★ rand()·uniqid()는 안 된다 — 재현 가능하거나 시간 기반이라 맞힐 수 있다.
    $token = bin2hex(random_bytes(32));

    // DB에는 원본이 아니라 '지문'만 남긴다 (DB 유출 대비).
    $expiresAt = date('Y-m-d H:i:s', time() + REMEMBER_DAYS * 86400);   // 86400초 = 하루
    db()->prepare('INSERT INTO remember_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)')
        ->execute([$userId, hash('sha256', $token), $expiresAt]);

    // 브라우저에는 원본 토큰을 준다.
    setcookie(REMEMBER_COOKIE, $token, remember_cookie_options(time() + REMEMBER_DAYS * 86400));
}

// 쿠키를 확인해 '이 사람이 맞다'면 회원 번호를, 아니면 0을 돌려준다.
//   ★ 통과했으면 토큰을 새것으로 갈아 끼운다(회전).
//   ⚠️ 쿠키를 굽으므로 화면 출력이 시작되기 전에 불려야 한다 → auth.php가 파일 로드 시점에 부른다.
function remember_lookup(): int {
    $token = $_COOKIE[REMEMBER_COOKIE] ?? '';
    if (!is_string($token) || $token === '') {
        return 0;
    }

    // 받은 토큰의 지문을 만들어 그 지문으로 찾는다.
    //   ★ 토큰 원본으로 찾는 게 아니라 지문으로 찾는다 — DB에 원본이 없으니 당연하다.
    //   만료된 표는 아예 안 걸리게 조건에 넣는다(청소가 늦어도 안전하도록).
    $stmt = db()->prepare(
        'SELECT id, user_id FROM remember_tokens WHERE token_hash = ? AND expires_at > NOW()'
    );
    $stmt->execute([hash('sha256', $token)]);
    $row = $stmt->fetch();

    if ($row === false) {
        // 없는/만료된 토큰이다. 쓸모없는 쿠키를 계속 들고 다니지 않게 떼어준다.
        remember_clear_cookie();
        return 0;
    }

    // 회전: 방금 쓴 표는 버리고 새 표를 발급한다.
    //   ★ 누가 이 토큰을 훔쳤더라도, 진짜 주인이 한 번 접속하는 순간 훔친 표가 무효가 된다.
    db()->prepare('DELETE FROM remember_tokens WHERE id = ?')->execute([$row['id']]);
    remember_issue((int) $row['user_id']);

    return (int) $row['user_id'];
}

// 로그아웃할 때: 이 브라우저의 표를 DB에서 지우고 쿠키도 회수한다.
//   ★ 쿠키만 지우면 DB에 표가 남아 '유효한 표'가 떠돈다. 둘 다 지워야 진짜 로그아웃이다.
function remember_forget(): void {
    $token = $_COOKIE[REMEMBER_COOKIE] ?? '';
    if (is_string($token) && $token !== '') {
        db()->prepare('DELETE FROM remember_tokens WHERE token_hash = ?')
            ->execute([hash('sha256', $token)]);
    }
    remember_clear_cookie();
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
