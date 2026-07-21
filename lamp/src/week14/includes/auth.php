<?php
// ============================================================
// auth.php — '인증(로그인)' 도메인 모듈
//   더미 사용자 목록 + 로그인 확인 + 세션 읽기/쓰기를 모아둔 곳.
//   나중 DB를 붙이면 get_users() 속만 users 테이블 조회로 바꾸면 된다.
// ============================================================

// 더미 사용자 (나중 users 테이블)
//   ★★ 비밀번호는 '절대' 그대로 저장하지 않는다.
//      password_hash()로 만든 '해시'(단방향으로 뒤섞은 값)만 저장한다.
//      해시는 되돌릴 수 없어서, DB가 털려도 원래 비밀번호를 알 수 없다.
//      (실무 철칙. 평문 저장은 사고 나면 그대로 유출)
//   테스트 계정:  영화광 / 1234        ·  admin / admin1234
function get_users(): array {
    return [
        ['username' => '영화광', 'passwordHash' => '$2y$12$0TjGD.ZDDHTWGRc4zCPaxeKviHRa4PxEg5nmlPJ8GT5X8SGdFn5cG'],
        ['username' => 'admin',  'passwordHash' => '$2y$12$lRkvX788AuKPJdH1CAihFuLn680SATlMtCiQL.JQfRTWsPqmcB48G'],
    ];
}

// 아이디로 사용자 찾기. 없으면 null.
function find_user(string $username): ?array {
    foreach (get_users() as $u) {
        if ($u['username'] === $username) {
            return $u;
        }
    }
    return null;
}

// 아이디+비밀번호가 맞는지 확인. 맞으면 사용자 배열, 틀리면 null.
//   password_verify(입력한 비번, 저장된 해시) = 해시와 대조해 맞는지 확인.
//     ★ 해시를 '풀어서' 비교하는 게 아니라, 입력값을 같은 방식으로 뒤섞어 비교한다.
function verify_login(string $username, string $password): ?array {
    $user = find_user($username);

    // 아이디가 없으면 실패
    if ($user === null) {
        return null;
    }
    // 비밀번호가 틀리면 실패
    if (!password_verify($password, $user['passwordHash'])) {
        return null;
    }
    return $user;
}

// ── 세션에 로그인 상태 쓰기/읽기/지우기 ──────────────────────

// 로그인 처리 = 세션 금고에 사용자 이름을 적어둔다.
function login_user(string $username): void {
    // session_regenerate_id = 로그인 순간 '번호표'를 새로 발급한다.
    //   왜? 남이 미리 알아낸 번호표로 내 로그인 상태를 가로채는 공격(세션 고정)을 막기 위해. (실무 필수)
    session_regenerate_id(true);
    $_SESSION['user'] = $username;
}

// 로그아웃 = 세션 금고를 비우고 폐기.
function logout_user(): void {
    $_SESSION = [];       // 담긴 값 모두 비우고
    session_destroy();    // 세션 자체를 폐기
}

// 지금 로그인한 사용자 이름. 로그인 안 했으면 null.
function current_user(): ?string {
    return $_SESSION['user'] ?? null;
}

// 로그인 상태인가?
function is_logged_in(): bool {
    return current_user() !== null;
}
