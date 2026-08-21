<?php
// ============================================================
// login_guard.php — 로그인 시도 제한 (무차별 대입 방어)
//
//   [지금까지 비어 있던 자리]
//     세션 고정 · CSRF · 탈취 대응 · HTTPS — 전부 **'훔친 열쇠'를 막는 방어**였다.
//     **정문을 두드려 여는 것**은 아무도 안 막고 있었다.
//     비밀번호를 몇 번이든 틀려도 되면, 자동 스크립트에게는 시간 문제일 뿐이다.
//
//   [★ 세는 자리를 고르는 것도 '그릇 고르기'다]
//     · 세션 → ❌ 브라우저만 바꾸면 0부터 다시
//     · 쿠키 → ❌ 지우면 끝. 애초에 공격자 손에 있는 값
//     · DB   → ⭕ 공격자가 못 건드리는 유일한 자리
//     이번 주차의 기준 그대로다: **틀리면 손해 보는 값은 서버에.**
//     "몇 번 틀렸나"는 공격자가 0으로 되돌리고 싶은 값이므로 사용자 쪽에 두면 안 된다.
//
//   [★★ 잠그는 대상을 잘못 고르면 그게 또 공격이 된다]
//     "아이디 5번 실패 → 그 계정 잠금"은 위험하다.
//     **공격자가 남의 아이디를 일부러 틀려서 그 사람을 못 들어오게 만들 수 있다**(계정 잠금 DoS).
//     → 그래서 **(아이디 + 접속지) 조합**으로 센다. 막히는 건 **두드린 자리**뿐이고,
//       진짜 주인은 다른 곳에서 접속하므로 멀쩡하다.
//     → 여기에 **접속지 전체** 카운트를 하나 더 둔다.
//       아이디를 바꿔가며 훑는 공격은 위 조합에 안 걸리기 때문이다.
//
//   [왜 '지연'이 아니라 '거부'인가]
//     실무에서는 실패할수록 응답을 늦추는 방법도 쓴다. 여기서는 안 쓴다 —
//     PHP는 요청 하나가 프로세스 하나를 붙잡으므로, **일부러 늦추면 그게 곧 자원 고갈**이 된다.
//     공격자가 느린 요청을 잔뜩 보내 서버를 마비시킬 수 있다. 거부는 즉시 끝나서 그 위험이 없다.
// ============================================================

require_once __DIR__ . '/db.php';

// 판정에 쓰는 시간 창(분). 이보다 오래된 실패는 없는 셈 친다.
const LOGIN_WINDOW_MINUTES = 15;

// (아이디 + 접속지) 조합으로 이만큼 실패하면 막는다.
//   사람이 오타로 틀리는 횟수보다는 넉넉하게, 자동 대입에는 턱없이 모자라게.
const LOGIN_MAX_PER_ACCOUNT = 5;

// 접속지 하나에서 (아이디를 바꿔가며) 이만큼 실패하면 막는다.
//   ★ 위보다 훨씬 크게 잡는다 — 공유 와이파이·회사망처럼 **여러 사람이 같은 IP**를 쓸 수 있어서,
//     너무 빡빡하면 애먼 사람이 막힌다.
const LOGIN_MAX_PER_IP = 20;

// 접속지 지문. 원본 IP를 들고 있을 이유가 없다 — 우리가 하는 건 '같은 곳인가' 비교뿐이다.
//   ★ 익명화가 목적은 아니다(IPv4는 경우의 수가 적어 되돌리기 쉽다).
//     '굳이 원본을 보관하지 않는다'는 정도의 의미다.
function login_ip_fingerprint(): string {
    return hash('sha256', (string) ($_SERVER['REMOTE_ADDR'] ?? ''));
}

// 지금 이 요청이 막혀 있나? 막혔으면 남은 초, 아니면 0.
//   ★ 반환을 '남은 초'로 두는 이유: 화면에 "N분 뒤에 다시" 라고 알려주려면 그 값이 필요하다.
//     그냥 막으면 사용자는 언제까지 기다려야 하는지 몰라 계속 두드린다.
function login_block_seconds(string $username): int {
    // ★★ '남은 시간'을 PHP가 아니라 **DB 안에서** 계산한다.
    //   [왜 — 실제로 밟은 함정]
    //     DB는 UTC로 돌아가고 PHP는 Asia/Seoul로 맞춰져 있다(util.php).
    //     그래서 MAX(attempted_at)로 꺼낸 '2026-08-15 10:11:54'를 PHP의 strtotime()에 넣으면
    //     **한국 시각으로 읽어 9시간을 잘못 계산한다** → 남은 시간이 -31521초가 나와
    //     "안 막힘"으로 판정됐다. 조건은 다 맞는데 잠금만 조용히 풀리는, 찾기 어려운 버그다.
    //   → 시각 계산은 **한쪽 세계 안에서만** 한다. 여기서는 전부 DB 시각으로 끝낸다.
    //     (화면에 시각을 그릴 때 UNIX_TIMESTAMP()를 쓰는 것과 같은 이유다)
    $sql = 'SELECT COUNT(*) AS ip_fails,
                   SUM(username = ?) AS pair_fails,
                   TIMESTAMPDIFF(SECOND, NOW(), DATE_ADD(MAX(attempted_at), INTERVAL ? MINUTE)) AS left_seconds
              FROM login_attempts
             WHERE ip_hash = ? AND attempted_at > DATE_SUB(NOW(), INTERVAL ? MINUTE)';

    $stmt = db()->prepare($sql);
    $stmt->execute([
        $username,
        LOGIN_WINDOW_MINUTES,          // 잠금이 풀리는 시각 = 마지막 실패 + 이만큼
        login_ip_fingerprint(),
        LOGIN_WINDOW_MINUTES,          // 세는 창도 같은 길이
    ]);
    $row = $stmt->fetch();

    if ($row === false || $row['left_seconds'] === null) {
        return 0;                      // 창 안에 실패가 하나도 없다
    }

    $overAccount = (int) $row['pair_fails'] >= LOGIN_MAX_PER_ACCOUNT;
    $overIp      = (int) $row['ip_fails']   >= LOGIN_MAX_PER_IP;
    if (!$overAccount && !$overIp) {
        return 0;
    }

    // ★ 잠금은 '마지막 실패로부터' 창이 끝날 때까지다.
    //   즉 계속 두드리면 계속 밀린다 — 공격자에게는 영영 안 열리고,
    //   진짜 주인은 잠시 손을 놓으면 저절로 풀린다.
    return max((int) $row['left_seconds'], 0);
}

// 실패를 한 줄 남긴다. (아이디가 없는 경우에도 남긴다)
function record_login_failure(string $username): void {
    // 창이 지난 줄은 판정에 안 쓰이므로 치운다. cron이 없어 여기서 함께 청소한다.
    //   ★ consent_log와 정반대다 — 저건 증빙이라 영원히 쌓고, 이건 판정용이라 곧 버린다.
    //     같은 '기록'이라도 **왜 남기는가**에 따라 수명이 갈린다.
    db()->prepare('DELETE FROM login_attempts WHERE attempted_at < DATE_SUB(NOW(), INTERVAL ? MINUTE)')
        ->execute([LOGIN_WINDOW_MINUTES]);

    db()->prepare('INSERT INTO login_attempts (username, ip_hash) VALUES (?, ?)')
        ->execute([mb_substr($username, 0, 50), login_ip_fingerprint()]);
}

// 로그인에 성공했으면 이 자리의 실패 기록을 지운다.
//   ★ 안 지우면, 몇 번 틀렸다가 제대로 들어온 사람이 **조금 뒤에 또 막힌다.**
//     성공은 "이 사람이 주인이 맞다"는 증거이므로 카운터를 되돌리는 게 맞다.
//   ★ 접속지 전체가 아니라 **이 아이디 + 이 접속지**만 지운다 —
//     한 계정에 성공했다고 해서, 같은 곳에서 다른 아이디들을 두드린 이력까지 없애주면
//     "아무 계정이나 하나 뚫으면 카운터가 초기화된다"가 되어 방어가 뚫린다.
function clear_login_failures(string $username): void {
    db()->prepare('DELETE FROM login_attempts WHERE username = ? AND ip_hash = ?')
        ->execute([$username, login_ip_fingerprint()]);
}
