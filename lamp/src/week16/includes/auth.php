<?php
// ============================================================
// auth.php — '인증(로그인)' 도메인 모듈
//   회원 조회 + 로그인 확인 + '지금 누구인지' 읽기를 모아둔 곳.
// ============================================================

require_once __DIR__ . '/db.php';     // users 표를 조회하므로
require_once __DIR__ . '/util.php';   // redirect() · set_flash() (util.php가 session.php를 켜준다)
require_once __DIR__ . '/session_db.php';   // destroy_other_sessions — 함수를 쓰는 파일은 직접 require
require_once __DIR__ . '/devices.php';      // 로그인한 기기 기록
require_once __DIR__ . '/device_key.php';   // 기기 도장 — 세션 연장의 조건

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

// 회원 번호(id)로 회원 한 명 찾기. 없으면 null.
//   ★ 세션에는 아이디가 아니라 '번호'를 담는다 → 그걸 다시 회원 정보로 바꿀 때 쓴다.
//     왜 번호인가: 아이디(username)는 사람이 읽는 이름이라 언젠가 바꿀 수 있지만,
//     번호는 그 회원을 가리키는 변하지 않는 열쇠다(기본키). 외래키도 전부 번호를 쓴다.
function find_user_by_id(int $id): ?array {
    $stmt = db()->prepare('SELECT * FROM users WHERE id = ?');
    $stmt->execute([$id]);
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

// ── 아이디가 없을 때 대신 대조할 '가짜 해시' ─────────────────
//   [★★ 왜 이런 게 필요한가 — 시간으로 새는 정보]
//     예전 코드는 아이디가 없으면 곧바로 return 했다. 그런데 bcrypt는 **일부러 느리게**
//     설계돼 있어서(cost 12 → 수십~수백 ms), 있는 아이디와 없는 아이디의 응답 시간이 확연히 갈렸다:
//       · 없는 아이디 → 즉시 반환            (빠르다)
//       · 있는 아이디 → 해시 대조 후 반환    (느리다)
//     화면 메시지는 "아이디 또는 비밀번호가 올바르지 않습니다"로 뭉뚱그렸는데,
//     **응답 시간이 그 구분을 그대로 알려주고 있었다.** (계정 열거 — authenticate.php 주석 참고)
//
//   ★ CSRF 토큰을 hash_equals로 비교하는 것과 **정확히 같은 이야기**다:
//     "같은지 비교할 때는 **걸리는 시간까지 같아야 한다.**"
//     거기서는 글자 비교의 시간차였고, 여기서는 '비교를 하느냐 마느냐'의 시간차다.
//
//   ★ 이 값은 실제 비밀번호가 아니다. 아무 문자열의 해시이고, 어떤 비밀번호와도 맞지 않는다.
//     중요한 건 **우리가 쓰는 것과 같은 알고리즘·같은 cost**여야 한다는 것 —
//     그래야 계산에 걸리는 시간이 진짜와 같아진다. (지금은 bcrypt cost 12)
const DUMMY_PASSWORD_HASH = '$2y$12$SClCbG93inmWb6ntjXPXDeoRE7k.cU7/6LPxpqFCvfMxk5BWTHbNG';

// 아이디+비밀번호가 맞는지 확인. 맞으면 회원 배열, 틀리면 null.
//   password_verify(입력한 비번, 저장된 해시) = 해시와 대조해 맞는지 확인.
//     ★ 해시를 '풀어서' 비교하는 게 아니라, 입력값을 같은 방식으로 뒤섞어 비교한다.
function verify_login(string $username, string $password): ?array {
    $user = find_user($username);

    // ★ 아이디가 없어도 **대조는 한다.** 걸리는 시간을 맞추기 위해서다.
    //   (없으면 가짜 해시와 대조한다 → 어차피 안 맞지만 시간은 똑같이 쓴다)
    $hash    = $user['password'] ?? DUMMY_PASSWORD_HASH;
    $matches = password_verify($password, $hash);

    // ★ 판정도 한 번에 한다. 여기서 다시 갈라 return 하면 시간차가 되살아난다.
    return ($user !== null && $matches) ? $user : null;
}

// ── '지금 누구인지' 읽기 (세션에 적힌 신원) ───────────────────
//
//   [무엇이 바뀌었나 — week15 → week16]
//     week15: 주소에 실어 날랐다 → ?as=영화광
//             ★ 주소창의 as= 를 남의 아이디로 고치면 그대로 그 사람이 됐다(사칭).
//               비밀번호 확인은 로그인하는 순간 딱 한 번이고, 그 뒤 요청들은
//               "내가 누구라고 주장하는 값"을 그냥 믿었기 때문이다.
//               브라우저가 보내는 값은 GET이든 POST든 사용자가 다 고칠 수 있어서
//               hidden 필드로 숨겨도 소용없었다. 구조적으로 막을 수가 없었다.
//
//     week16: 서버 금고(세션)에 적어둔다 → $_SESSION['user_id']
//             ★ 값이 서버에 있으니 사용자가 손댈 수 없다. 브라우저가 들고 다니는 건
//               금고 번호표(쿠키 PHPSESSID)뿐이고, 번호를 위조해도 빈 금고만 열린다.
//               → 사칭이 구조적으로 막힌다. 이것이 week16의 핵심 성과다.
//
//     심는 곳  : 로그인 성공 시 login_and_redirect()
//     지우는 곳: 로그아웃 시 logout_and_redirect()
//     읽는 곳  : 아래 current_user_row() — 프로젝트 전체에서 여기 하나뿐이다.

// 지금 로그인한 회원 정보(users 표 한 줄). 로그인 안 했으면 null.
//   ★ 세션엔 번호만 있으므로 매 요청 DB에서 최신 정보를 읽어온다.
//     회원 정보를 통째로 세션에 넣지 않는 이유: 닉네임·아바타를 바꿔도
//     다시 로그인하기 전까지 옛날 값이 화면에 남는 버그가 생기기 때문이다.
//   ★ 표에 실제로 있는지 꼭 확인한다. 탈퇴 등으로 회원이 사라졌는데 세션만 남아 있으면
//     글쓰기 때 작성자 번호를 찾지 못해 외래키 오류로 페이지가 통째로 터진다.
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

    // 세션에 번호가 없으면 로그인 안 한 상태다. (?? 0 = 없으면 0)
    $userId = (int) ($_SESSION[SESSION_USER_ID] ?? 0);
    $cached = $userId === 0 ? null : find_user_by_id($userId);
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

// ── '원래 가려던 곳' 기억하기 ────────────────────────────────
//   로그인 안 한 사람이 글쓰기를 누르면 로그인 화면으로 보내는데, 로그인하고 나면
//   홈으로 튕겨서 다시 글쓰기를 찾아 들어가야 했다. 그 자리를 기억해 뒀다가 되돌려준다.
//
//   ★★ 왜 주소(?next=)가 아니라 세션인가 — 이게 이 기능의 핵심이다.
//     주소로 나르면 `/auth/login.php?next=…` 가 되는데, 그 값은 사용자가 고칠 수 있다.
//     공격자가 next 자리에 **남의 사이트 주소**를 넣은 링크를 뿌리면,
//     우리 사이트에서 로그인한 직후 가짜 로그인 화면으로 튕겨나간다(오픈 리다이렉트).
//     week15의 ?as= 와 똑같은 구조다 — 사용자 손에 쥐여준 값을 믿었다가 뚫리는 것.
//
//   ★ 그 값의 **출처**는 사용자가 보낸 주소($_SERVER['REQUEST_URI'])다.
//     "믿을 수 있는 그릇에 담았다"와 "담은 내용이 안전하다"는 다른 이야기다.
//
//   [★ 세션이 아니라 쿠키에 담는 이유]
//     이 값은 고쳐봐야 **자기가 엉뚱한 데로 갈 뿐**이다. 남는 것도, 남에게 미치는 것도 없다.
//     "틀리면 손해 보는 값만 세션"이라는 기준에 걸리지 않으므로 쿠키 자리다. (handoff.php)
//
//   [★★ 그래서 검사를 두 번 한다 — 적을 때 한 번, 읽을 때 또 한 번]
//     세션이었다면 적을 때 한 번이면 충분했다. 담아둔 뒤엔 아무도 못 건드리니까.
//     쿠키는 담아둔 뒤에도 사용자가 고칠 수 있다 → **읽는 쪽이 다시 확인해야 한다.**
//     이 두 줄이 이번 변경의 핵심이고, 쿠키를 쓸 때 가장 많이 빠뜨리는 부분이다.
const INTENDED_MAX_LEN = 300;   // 주소 길이 한도. 쿠키는 4KB뿐이라 미리 자른다.

// 이 주소가 '우리 사이트 안'인가?
//   ① 우리 사이트 안이어야 한다 = '/'로 시작.
//   ② 그런데 '//'로 시작하면 '/'로 시작하지만 **바깥 주소**다.
//      //evil.com 은 브라우저가 'https://evil.com'으로 읽는다(프로토콜 상대 주소).
//      오픈 리다이렉트 방어를 뚫는 가장 흔한 수법이라 반드시 함께 막는다.
//   ★ 함수로 뽑아낸 이유: 쓰는 곳이 두 곳(적을 때·읽을 때)이 됐기 때문이다.
//     같은 판단이 두 군데로 복사되면, 나중에 한쪽만 고쳐져서 그쪽만 뚫린다.
function is_internal_path(string $path): bool {
    return str_starts_with($path, '/') && !str_starts_with($path, '//');
}

// 로그인 후 돌아갈 곳을 적어둔다. 우리 사이트 안의 주소일 때만.
function remember_intended(string $path): void {
    if (!is_internal_path($path)) {
        return;                       // 수상하면 그냥 기억하지 않는다 → 로그인 후 홈으로
    }
    handoff_put(HANDOFF_INTENDED, ['path' => mb_substr($path, 0, INTENDED_MAX_LEN)]);
}

// 적어둔 목적지를 꺼낸다. 없거나 수상하면 $default.
//   ★ handoff_claim = 꺼내면서 브라우저에서도 지운다(read-once).
//     알림과 달리 **자동으로는 안 걷힌다** — 로그인 화면을 띄우는 요청에서 지워지면
//     정작 로그인할 때 목적지가 없기 때문이다. (handoff.php의 HANDOFF_NAMES 주석)
//     그래서 '다 썼다'를 아는 이 자리에서 직접 지운다.
//     안 지우면 다음에 그냥 로그인했을 때도 엉뚱하게 옛 주소로 끌려간다.
function take_intended(string $default = '/'): string {
    $data = handoff_claim(HANDOFF_INTENDED);
    $path = handoff_str($data['path'] ?? null, INTENDED_MAX_LEN);

    // ★★ 여기가 쿠키로 옮기면서 새로 생긴 줄이다.
    //   쿠키를 '//evil.com'으로 고쳐놓고 로그인하면, 이 검사가 없을 때
    //   로그인 직후 남의 사이트로 튕겨 나간다(오픈 리다이렉트).
    //   내가 나를 보내는 것이라 피해는 적지만, 같은 코드가 '로그인하면 여기로'
    //   같은 링크에 쓰이는 순간 진짜 구멍이 된다. 읽는 쪽에서 막는 게 원칙이다.
    return is_internal_path($path) ? $path : $default;
}

// ── 민감한 작업 앞에서 비밀번호를 다시 묻는다 (sudo 모드) ────
//   [무엇을 막나]
//     로그인한 채 자리를 비운 사이, 지나가던 사람이 **되돌릴 수 없는 일**을 저지르는 것.
//     글 영구삭제나 '다른 기기 모두 로그아웃' 같은 것들이다.
//     세션만으로는 "지금 키보드 앞에 있는 사람이 계정 주인인가"를 알 수 없다 —
//     세션은 '이 브라우저가 로그인했다'까지만 증명한다.
//
//   [어떻게 하나]
//     비밀번호를 마지막으로 확인한 시각을 세션에 적어두고, 민감한 작업 앞에서
//     그 시각이 너무 오래됐으면 **한 번 더 묻는다.** 창(15분) 안이면 그냥 통과.
//     GitHub·GitHub Actions·AWS 콘솔이 쓰는 방식이라 'sudo 모드'라고 부른다.
//
//   [★ 왜 로그아웃시키지 않고 '다시 묻기'인가]
//     로그아웃시키면 하던 일이 전부 끊긴다. 위험한 건 그 작업 하나뿐이므로,
//     **그 작업 앞에서만** 문턱을 세우는 게 맞다. 나머지는 그대로 쓰게 둔다.
const SESSION_AUTH_AT = 'auth_at';   // 비밀번호를 마지막으로 확인한 시각(초)
const SUDO_WINDOW     = 900;         // 15분. 이 안이면 다시 안 묻는다.

// 최근에 비밀번호를 확인했나?
function has_recent_auth(): bool {
    $at = (int) ($_SESSION[SESSION_AUTH_AT] ?? 0);
    return $at !== 0 && time() - $at <= SUDO_WINDOW;
}

// 확인한 지 오래됐으면 비밀번호 확인 화면으로 보낸다. (민감한 액션 파일이 맨 앞에서 부른다)
//   $backTo = 확인이 끝나면 돌아갈 화면.
function require_recent_auth(string $backTo): void {
    if (has_recent_auth()) {
        return;
    }
    // 확인 후 어디로 돌려보낼지는 '가려던 곳'과 같은 장치를 재사용한다.
    //   ★ 새 장치를 또 만들지 않는다 — 하는 일이 완전히 같기 때문이다.
    remember_intended($backTo);
    set_flash('🔐 안전을 위해 비밀번호를 한 번 더 확인합니다.', 'error');
    redirect('/settings/confirm.php');
}

// 세션에 '이 사람이 로그인했다'를 적는다.
//   ★ 로그인 화면을 거친 경우와 '로그인 유지' 쿠키로 되살아난 경우가 **둘 다 여기를 지난다.**
//     한쪽만 아래 방어를 빠뜨리면 그 경로만 조용히 뚫리는데, 그런 구멍은 눈에 잘 안 띈다.
function start_session_for(int $userId): void {
    // ★★ 번호표를 새것으로 갈아 끼운다 — 세션 고정 공격(session fixation) 방어.
    //   [어떤 공격인가]
    //     공격자가 자기 번호표를 피해자에게 미리 쥐여준다(링크에 세션 ID를 심는 등).
    //     피해자가 그 번호표를 든 채로 로그인하면, 서버는 '그 번호표 = 로그인된 사람'으로
    //     기록한다. 같은 번호표를 가진 공격자도 그대로 로그인 상태가 되어버린다.
    //   [왜 이 한 줄로 막히나]
    //     로그인하는 '순간' 번호를 새로 뽑아 쓰므로, 미리 쥐여준 옛 번호는 무효가 된다.
    //   true = 옛 세션 데이터를 서버에서 즉시 지운다.
    //     (안 지우면 옛 번호표가 한동안 살아 있어 방어가 반쪽이 된다)
    session_regenerate_id(true);

    // ★ 아이디(이름)가 아니라 번호를 담는다. 변하지 않는 열쇠이기 때문 (find_user_by_id 주석 참고).
    $_SESSION[SESSION_USER_ID] = $userId;

    // ★ 이 기기를 회원의 '로그인한 기기' 목록에 올린다 (이미 있으면 시각만 갱신).
    //   [왜 '다른 로그인을 전부 끊기'가 아니라 이것인가]
    //     한 곳만 허용하면 **PC와 폰을 같이 못 쓴다.** 그래서 여러 곳을 허용하되,
    //     대신 **볼 수 있게** 해준다 — 그게 기기 목록이다.
    //     ★★ 둘은 짝이다. **여러 곳을 허용하면서 목록이 없으면**, 훔친 세션이
    //       '내 기기 중 하나'로 숨어들어 아무도 눈치채지 못한다.
    //     (네이버·구글이 여러 기기를 허용하는 것도 '내 기기 목록'을 주기 때문이다)
    //
    //   ★ 처음 보는 기기면 주인에게 한 번 알린다 — header.php가 take_new_devices()로 띄운다.
    remember_device($userId);

    // 유휴 시계를 지금부터 시작한다.
    //   ★ 여기서 안 넣으면 '로그인한 그 순간'은 시계가 없는 상태가 된다.
    //     다음 페이지를 열어야 비로소 시작돼서, 만료 판정이 한 박자씩 밀린다.
    $_SESSION[SESSION_LAST_SEEN] = time();

    // ★ 도장을 (다시) 등록할 수 있는 짧은 창도 여기서 연다.
    //   [왜 여기인가]
    //     이 자리에 오는 길은 **비밀번호를 맞히는 것 하나뿐**이다.
    //     쿠키를 훔친 쪽에는 비밀번호가 없으므로, 이 조건이 곧 '주인만'을 뜻한다.
    //   ※ 브라우저 저장소를 지운 사용자가 스스로 잠기는 것을 푸는 유일한 길이다.
    open_key_enroll_window();

    // 방금 비밀번호를 확인하고 들어왔으므로 sudo 창도 지금부터 연다.
    //   ★ 여기에 오는 길은 이제 **비밀번호를 거친 하나뿐**이다.
    //     '로그인 유지'가 있던 시절에는 쿠키 한 장으로 들어오는 두 번째 길이 있어서,
    //     그쪽에서는 이 값을 도로 지워야 했다. 길이 하나가 되면서 그 예외가 사라졌다.
    $_SESSION[SESSION_AUTH_AT] = time();
}

// 로그인 성공 → 세션에 회원 번호를 적고 보낸다.
//   week15처럼 주소에 '넘겨주는' 게 아니라, 서버가 '기록'한다.
//   ★ 로그인 상태는 **세션 하나로만** 유지한다. 오래 사는 쿠키를 따로 주지 않는다.
//     세션 쿠키(PHPSESSID)는 만료 시각이 없어서 **브라우저를 닫으면 사라진다** —
//     그게 '자리를 뜨면 풀린다'는 성질이고, 우리가 일부러 남긴 성질이다.
function login_and_redirect(int $userId, string $path = '/'): never {
    start_session_for($userId);

    // 로그인 화면으로 밀려나기 전에 보던 곳이 있으면 그리로, 없으면 $path(기본 홈).
    //   ★ start_session_for()가 번호표를 갈아 끼웠는데도 이 값이 살아 있는 이유:
    //     session_regenerate_id()는 번호만 바꾸고 **금고 안의 내용은 그대로 옮겨준다.**
    $target = take_intended($path);

    // ★★ 도장을 쓰는 동안에는 곧바로 목적지로 보내지 않는다.
    //   비밀번호는 맞혔지만 **이 기기의 도장은 아직 확인하지 않았다.**
    //   그 둘은 다른 질문이다 — "비밀번호를 아는가"와 "늘 쓰던 그 기기인가".
    //   ※ 이 한 화면을 거치는 동안에만 '로그인은 됐는데 도장은 없는' 상태가 존재한다.
    //     그래서 이 구간이 우리가 남긴 한계 ③이다(device_key.php 주석).
    if (DEVICE_KEY_REQUIRED) {
        redirect('/session/verify.php?back=' . urlencode($target));
    }

    redirect($target);
}

// 로그아웃 → 서버 금고를 비운다.
//   ★ week15에는 여기서 할 일이 없었다. 서버가 아무것도 기억하지 않았으니
//     '?as= 를 안 붙인 주소로 보내는 것'이 곧 로그아웃이었다.
//     이제는 진짜로 지울 것이 생겼다 — 네 단계를 모두 밟아야 깨끗이 지워진다.
function logout_and_redirect(string $path = '/'): never {
    // ① 금고 안의 내용물을 비운다
    $_SESSION = [];

    // ② 브라우저가 든 번호표(쿠키)도 회수한다.
    //   ①만 하면 빈 금고에 번호표만 계속 들고 다니게 된다.
    //   '지우는 법'이 따로 있는 게 아니라, 만료 시각을 과거로 줘서 브라우저가 버리게 한다.
    //   ★ 쿠키를 심을 때 쓴 옵션(path·domain·secure…)을 그대로 넣어야 같은 쿠키로 인식해 지운다.
    //     한 글자라도 다르면 '다른 쿠키'로 보고 옛것이 그대로 남는다.
    if (ini_get('session.use_cookies')) {
        $params = session_get_cookie_params();
        setcookie(session_name(), '', [
            'expires'  => time() - 42000,        // 과거 시각 = "이미 만료됨, 버려라"
            'path'     => $params['path'],
            'domain'   => $params['domain'],
            'secure'   => $params['secure'],
            'httponly' => $params['httponly'],
            'samesite' => $params['samesite'],
        ]);
    }

    // ③ 서버 쪽 저장분을 완전히 파기한다
    session_destroy();

    redirect($path);
}

// ── 접근 제어 ────────────────────────────────────────────────

// 로그인 안 했으면 로그인 페이지로 보내고 실행을 멈춘다.
//   ★ 화면에서 버튼을 숨기는 것만으로는 절대 부족하다.
//     사용자는 주소를 직접 치거나 요청을 조작해 보낼 수 있으므로,
//     '처리하는 쪽(서버)'에서 반드시 다시 확인해야 한다.
//     (화면 숨김 = 편의, 서버 확인 = 진짜 보안)
function require_login(): void {
    if (!is_logged_in()) {
        // ★ 화면(GET)일 때만 돌아갈 곳으로 기억한다.
        //   POST 액션 주소를 기억해 봐야 소용없다 — 로그인 후 그리로 보내면 폼 값이 없는
        //   빈 POST가 되고, 그러면 또 튕긴다. 되돌아가서 의미가 있는 건 '보던 화면'뿐이다.
        if (($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'GET') {
            remember_intended($_SERVER['REQUEST_URI'] ?? '/');
        }
        set_flash('🔒 로그인이 필요한 기능입니다.', 'error');
        redirect('/auth/login.php');
    }
}

// 이 글/댓글의 주인이 지금 로그인한 사람인가?
//   남의 글을 수정·삭제하지 못하게 막을 때 쓴다.
function is_owner(string $author): bool {
    return is_logged_in() && current_user() === $author;
}

// ── 자동 로그인: '로그인 유지' 쿠키로 세션 되살리기 ───────────
//
//   [왜 함수 안이 아니라 여기서 바로 실행하나]
//     되살리는 과정에서 **쿠키를 새로 굽는다**(토큰 회전). 쿠키는 HTTP 헤더라
//     화면에 한 글자라도 출력된 뒤에는 보낼 수 없다.
//     auth.php는 모든 화면·액션 파일이 HTML보다 먼저 require 하므로, 여기서 실행하면
//     항상 '출력 전'이 보장된다. (current_user_row() 안에 넣었다면 호출 시점이
//      페이지마다 달라져서, 어떤 페이지에서는 출력 뒤에 불려 조용히 실패했을 것이다)
//
//   [조건 두 개]
//     ① 세션이 비어 있을 때만 — 이미 로그인돼 있으면 할 일이 없다.
//     ② 쿠키가 있을 때만 — 없으면 DB를 조회할 이유조차 없다(대부분의 요청이 여기서 끝난다).
// ── ① 자리를 오래 비웠으면 로그아웃시킨다 ────────────────────
//   [무엇을 막나]
//     PC방·강의실 컴퓨터에 로그인해 둔 채 자리를 뜨는 경우다. 다음 사람이 그대로 쓴다.
//     은행·관공서 사이트가 "○분간 활동이 없어 로그아웃되었습니다"를 띄우는 그 장치.
//
//   [세션 만료와 뭐가 다른가]
//     세션 자체도 SESSION_TTL(30분)이 지나면 사라진다. 그런데 그건 **조용히** 사라진다 —
//     사용자는 이유도 모른 채 로그아웃돼 있다.
//     여기서는 그보다 **짧은 한도**로 우리가 먼저 감지해서, 이유를 알려주고 끊는다.
//     (세션이 아직 살아 있어야 알림을 남길 수 있으므로 반드시 TTL보다 짧아야 한다)
//
//   [★ '로그인 유지'와 충돌하는 것 아닌가]
//     끊는 대상은 **세션(지금 이 방문)** 하나뿐이다. 되살릴 다른 쿠키가 없으므로 그대로 끝난다.
//     그 쿠키는 사용자가 "이 기기는 내 것"이라고 직접 체크한 의사표시이기 때문이다.
//     → '로그인 유지'를 켠 사람은 곧바로 다시 로그인되고, 안 켠 사람만 로그인 화면으로 간다.
//       (그래서 공용 PC에서는 '로그인 유지'를 켜지 말라고 하는 것이다)
const IDLE_LIMIT = 1200;                // 20분. SESSION_TTL(30분)보다 반드시 짧게.
const SESSION_LAST_SEEN = 'last_seen';   // 마지막으로 움직인 시각(초)

// 자동 로그아웃까지 남은 시간(초). 로그인 안 했으면 0.
//   ★ 화면에 카운트다운을 띄우려고 쓴다. 판정은 어디까지나 서버가 하고,
//     이 숫자는 '지금 이 페이지를 연 시점' 기준의 참고값이다.
//     (페이지를 새로 열 때마다 last_seen이 갱신되므로 카운트다운도 다시 시작된다)
function idle_seconds_left(): int {
    if (empty($_SESSION[SESSION_USER_ID])) {
        return 0;
    }
    $lastSeen = (int) ($_SESSION[SESSION_LAST_SEEN] ?? time());
    return max(0, IDLE_LIMIT - (time() - $lastSeen));
}

if (!empty($_SESSION[SESSION_USER_ID])) {
    $lastSeen = (int) ($_SESSION[SESSION_LAST_SEEN] ?? 0);

    if ($lastSeen !== 0 && time() - $lastSeen > IDLE_LIMIT) {
        // 세션을 비운다. 되살릴 쿠키가 따로 없으므로 이걸로 끝이다 — 다시 로그인해야 한다.
        $_SESSION = [];
        set_flash('⏰ 오랫동안 활동이 없어 자동으로 로그아웃되었습니다.', 'error');
    } else {
        // 움직였으니 시각을 갱신한다 → 쓰는 동안에는 계속 연장된다(sliding).
        //   ★ 여기서 갱신하는 건 '마지막으로 움직인 시각'뿐이다.
        //     비밀번호 확인 시각(auth_at)까지 같이 갱신하면 sudo 창이 영원히 안 닫힌다.
        $_SESSION[SESSION_LAST_SEEN] = time();
    }
}

// ── ② 도장 확인이 만료됐으면 확인 화면으로 보낸다 ────────────
//   [★ 이 열 줄이 이번 변경의 핵심이다]
//     위 ①(유휴)은 **요청만 오면 계속 밀린다**(sliding). 그래서 세션을 훔친 쪽도
//     20분마다 한 번씩만 요청하면 무기한 유지할 수 있었다 —
//     "아무도 안 쓰면 끊는 장치"였지, "주인이 안 쓰면 끊는 장치"가 아니었다.
//
//     이제 미는 조건이 하나 더 붙는다: **10분마다 도장을 찍어야 한다.**
//     도장은 그 브라우저 안에만 있고 꺼낼 수 없으므로,
//     → **훔친 쿠키의 수명이 최대 10분으로 못 박힌다.**
//
//   [★ 왜 여기(auth.php 맨 아래)인가]
//     화면 파일들이 저마다 검사를 부르게 하면 **언젠가 한 곳이 빠진다.**
//     모든 화면이 반드시 지나는 자리에 한 번만 두는 것이 안전하다.
//     (require_login()을 화면마다 부르는 것과 같은 이유로, 그보다 더 아래 계층에 둔다)
//
//   [★ 예외 두 곳을 반드시 빼야 한다]
//     도장을 받으러 가는 길(`/session/`)과 로그인·로그아웃(`/auth/`)까지 막으면
//     **아무도 도장을 못 찍는다** — 화면이 무한히 튕긴다.
if (DEVICE_KEY_REQUIRED && !is_key_exempt_path() && !has_key_proof() && is_logged_in()) {
    // ★ 돌아갈 곳은 GET일 때만 기억한다. POST 주소로 되돌아가 봐야 값이 없는 빈 POST가 된다.
    //   (require_login()이 remember_intended를 GET에만 거는 것과 같은 판단)
    $backTo = ($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'GET'
        ? (string) ($_SERVER['REQUEST_URI'] ?? '/')
        : '/';

    redirect('/session/verify.php?back=' . urlencode($backTo));
}


