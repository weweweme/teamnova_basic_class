<?php
// ============================================================
// auth.php — '인증(로그인)' 도메인 모듈
//   회원 조회 + 로그인 확인 + 세션 읽기/쓰기를 모아둔 곳.
// ============================================================

require_once __DIR__ . '/db.php';   // current_user_id()가 users 표를 조회하므로

// ★★ 비밀번호는 '절대' 그대로 저장하지 않는다.
//    password_hash()로 만든 '해시'(단방향으로 뒤섞은 값)만 users.password에 저장한다.
//    해시는 되돌릴 수 없어서, DB가 털려도 원래 비밀번호를 알 수 없다. (실무 철칙)
//    테스트 계정(seed): 영화광 / 1234

// 아이디로 회원 한 명 찾기 (users 표에서). 없으면 null.
function find_user(string $username): ?array {
    $stmt = db()->prepare('SELECT * FROM users WHERE username = ?');
    $stmt->execute([$username]);
    $row = $stmt->fetch();
    return $row !== false ? $row : null;
}

// 새 회원 저장 (회원가입). 성공하면 새 회원 id, 아이디 중복이면 0.
function create_user(string $username, string $password): int {
    if (find_user($username) !== null) {
        return 0;                         // 이미 있는 아이디
    }
    // 평문 비번을 해시로 바꿔 저장 (원본 비번은 어디에도 안 남긴다)
    $hash = password_hash($password, PASSWORD_DEFAULT);
    $stmt = db()->prepare('INSERT INTO users (username, password) VALUES (?, ?)');
    $stmt->execute([$username, $hash]);
    return (int) db()->lastInsertId();
}

// 아이디+비밀번호가 맞는지 확인. 맞으면 회원 배열, 틀리면 null.
//   password_verify(입력한 비번, 저장된 해시) = 해시와 대조해 맞는지 확인.
//     ★ 해시를 '풀어서' 비교하는 게 아니라, 입력값을 같은 방식으로 뒤섞어 비교한다.
function verify_login(string $username, string $password): ?array {
    $user = find_user($username);

    // 아이디가 없으면 실패
    if ($user === null) {
        return null;
    }
    // 비밀번호가 틀리면 실패 (users 표의 password 열에 해시가 들어있음)
    if (!password_verify($password, $user['password'])) {
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

// 지금 로그인한 사용자의 DB id. 로그인 안 했으면 0.
//   likes·votes 표는 user_id(번호)를 쓰므로, 닉네임을 id로 바꿔줄 때 쓴다.
//   (세션엔 닉네임만 있고, 번호는 users 표에서 찾아온다)
function current_user_id(): int {
    $username = current_user();
    if ($username === null) {
        return 0;
    }
    return (int) db_scalar('SELECT id FROM users WHERE username = ?', [$username]);
}

// 로그인 상태인가?
function is_logged_in(): bool {
    return current_user() !== null;
}

// ── 접근 제어 ────────────────────────────────────────────────

// 로그인 안 했으면 로그인 페이지로 보내고 실행을 멈춘다.
//   ★ 화면에서 버튼을 숨기는 것만으로는 절대 부족하다.
//     사용자는 주소를 직접 치거나 요청을 조작해 보낼 수 있으므로,
//     '처리하는 쪽(서버)'에서 반드시 다시 확인해야 한다.
//     (화면 숨김 = 편의, 서버 확인 = 진짜 보안)
function require_login(): void {
    if (!is_logged_in()) {
        set_flash('🔒 로그인이 필요한 기능입니다.', 'error');
        header('Location: /auth/login.php');
        exit;
    }
}

// 이 글/댓글의 주인이 지금 로그인한 사람인가?
//   남의 글을 수정·삭제하지 못하게 막을 때 쓴다.
function is_owner(string $author): bool {
    return is_logged_in() && current_user() === $author;
}
