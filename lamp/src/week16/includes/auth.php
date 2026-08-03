<?php
// ============================================================
// auth.php — '인증(로그인)' 도메인 모듈
//   회원 조회 + 로그인 확인 + '지금 누구인지' 읽기를 모아둔 곳.
// ============================================================

require_once __DIR__ . '/db.php';     // users 표를 조회하므로
require_once __DIR__ . '/util.php';   // 신원을 주소에서 꺼내므로 (get_str·IDENTITY_KEY·redirect)

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
    //   닉네임은 처음엔 아이디와 같게 둔다 (나중에 설정에서 바꿀 수 있음).
    $hash = password_hash($password, PASSWORD_DEFAULT);
    $stmt = db()->prepare('INSERT INTO users (username, nickname, password) VALUES (?, ?, ?)');
    $stmt->execute([$username, $username, $hash]);
    return (int) db()->lastInsertId();
}

// 회원의 아바타(프로필 이미지) 주소를 저장한다.
function set_avatar(int $userId, string $url): void {
    db()->prepare('UPDATE users SET avatar = ? WHERE id = ?')->execute([$url, $userId]);
}

// 닉네임(표시 이름) 변경. 아이디(로그인 키)는 그대로 두고 nickname만 바꾼다.
function set_nickname(int $userId, string $nickname): void {
    db()->prepare('UPDATE users SET nickname = ? WHERE id = ?')->execute([$nickname, $userId]);
}

// 비밀번호 변경. 새 평문을 해시로 바꿔 저장한다. (원본은 안 남김)
function set_password(int $userId, string $newPassword): void {
    $hash = password_hash($newPassword, PASSWORD_DEFAULT);
    db()->prepare('UPDATE users SET password = ? WHERE id = ?')->execute([$hash, $userId]);
}

// 지금 로그인한 사용자의 '표시 이름(닉네임)'. 로그인 안 했으면 null.
//   주소에는 아이디만 실려오므로, users 표에서 찾아온 줄에서 닉네임을 꺼낸다.
function current_nickname(): ?string {
    $user = current_user_row();
    if ($user === null) {
        return null;
    }
    // 닉네임이 비어 있으면 아이디로 대체한다.
    $nickname = (string) ($user['nickname'] ?? '');
    return $nickname !== '' ? $nickname : (string) $user['username'];
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

// ── '지금 누구인지' 읽기 (주소에 실려온 신원) ─────────────────
//
//   [로그인 상태를 어디에 두나]
//     HTTP는 요청 하나하나가 서로를 기억하지 못하고, PHP 스크립트는 요청 한 번을
//     처리한 뒤 죽는다. 그래서 서버가 '방금 누가 왔었는지'를 스스로 기억할 수 없다.
//     → 그래서 '지금 누구인지'를 주소에 실어(?as=영화광) 매 요청마다 다시 알려준다.
//
//     심는 곳    : 로그인 성공 시 login_and_redirect() 가 주소에 얹는다.
//     이어붙이는 곳: util.php 의 build_url() — 그 뒤로는 링크·폼·리다이렉트가 자동으로 물고 다닌다.
//
//   ★★ 솔직히 말하면 이 방식은 '사칭'을 막지 못한다.
//      주소창의 as= 값을 남의 아이디로 고쳐 치면 그대로 그 사람이 되어버린다.
//      비밀번호 확인(verify_login)은 로그인하는 순간 딱 한 번 일어나고,
//      그 뒤의 요청들은 "내가 누구라고 주장하는 값"을 그냥 믿기 때문이다.
//
//      막을 방법이 없는 이유: 브라우저가 보내는 값은 GET이든 POST든 사용자가 다 고칠 수 있다.
//      (hidden 필드로 숨겨도 개발자도구로 고쳐 보낼 수 있으니 마찬가지다)
//      → 진짜 해결책은 '정답을 서버가 자기 쪽에 들고 있는 것' = 세션. 다음 주차 주제.

// 지금 요청에 실려온 회원 정보(users 표 한 줄). 신원이 없거나 없는 아이디면 null.
//   ★ users 표에 있는 아이디인지 꼭 확인한다.
//     안 하면 ?as=아무개 같은 '유령 사용자'가 통과해서, 글쓰기 때 작성자 번호를
//     찾지 못해 외래키 오류로 페이지가 통째로 터진다.
function current_user_row(): ?array {
    // 한 요청 안에서 current_user()·current_user_id()·current_nickname()이
    // 여러 번 불리므로, 조회 결과를 기억해 DB 조회를 1회로 끝낸다.
    //   static = 함수가 끝나도 값이 남아있는 변수 (C#의 static 지역변수와 같은 개념).
    //   첫 값을 false로 둔 이유: null은 '로그인 안 함'이라는 결과라서,
    //   '아직 안 알아봄'과 구분할 표시가 따로 필요하다.
    static $cached = false;
    if ($cached !== false) {
        return $cached;
    }

    $username = identity_from_request();
    $cached = $username === '' ? null : find_user($username);
    return $cached;
}

// 지금 로그인한 사용자 이름(아이디). 로그인 안 했으면 null.
function current_user(): ?string {
    return current_user_row()['username'] ?? null;
}

// 지금 로그인한 사용자의 DB id. 로그인 안 했으면 0.
//   likes·votes 표는 user_id(번호)를 쓰므로, 아이디를 번호로 바꿔줄 때 쓴다.
function current_user_id(): int {
    return (int) (current_user_row()['id'] ?? 0);
}

// 로그인 상태인가?
function is_logged_in(): bool {
    return current_user_row() !== null;
}

// 로그인 성공 → 신원을 주소에 심어서 보낸다.
//   세션에 '기록'하는 게 아니라, 다음 주소에 ?as=아이디 를 붙여 '넘겨준다'.
function login_and_redirect(string $username, string $path = '/'): never {
    redirect($path, [IDENTITY_KEY => $username]);
}

// 로그아웃 → 주소에서 신원을 뗀다.
//   ★ 서버에는 지울 것이 없다. 애초에 서버가 아무것도 기억하지 않기 때문이다.
//     (세션 방식이라면 여기서 서버 금고를 비워야 했다)
function logout_and_redirect(string $path = '/'): never {
    redirect($path, [IDENTITY_KEY => null]);   // null = build_url()이 주소에서 빼버린다
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
        redirect('/auth/login.php');
    }
}

// 이 글/댓글의 주인이 지금 로그인한 사람인가?
//   남의 글을 수정·삭제하지 못하게 막을 때 쓴다.
function is_owner(string $author): bool {
    return is_logged_in() && current_user() === $author;
}
