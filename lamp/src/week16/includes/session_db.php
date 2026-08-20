<?php
// ============================================================
// session_db.php — 세션을 '서버의 임시 파일'이 아니라 DB 표(sessions)에 저장한다
//
//   [원래는 어디에 있었나]
//     PHP 기본값은 서버의 임시 파일이다. 세션 하나 = 파일 하나:
//       /tmp/sess_4adbe17d…  →  recent_posts|a:3:{…}csrf_token|s:64:"…";
//     파일 이름 뒤쪽이 곧 브라우저가 들고 있는 번호표(PHPSESSID)다.
//
//   [왜 옮기나 — 이유 셋]
//     ① 서버를 여러 대로 늘리면 파일은 서로 공유가 안 된다.
//        1번 서버에서 로그인했는데 다음 요청이 2번 서버로 가면 "누구세요?"가 된다.
//     ② '이 회원의 세션을 전부 끊어라'가 가능해진다. (user_id 칼럼)
//        파일 방식으로는 남의 기기 세션 파일을 찾을 방법이 아예 없다.
//     ③ 눈에 보인다. 세션이 추상적인 개념이 아니라 표의 '행'이 된다.
//
//   [PHP가 이걸 어떻게 허락하나]
//     PHP는 '세션을 어디에 어떻게 저장할지'를 통째로 갈아 끼울 수 있게 만들어 뒀다.
//     SessionHandlerInterface 가 요구하는 메서드 6개만 채우면, 그 뒤로는
//     $_SESSION['user_id'] 처럼 쓰는 코드는 **한 글자도 바꿀 필요가 없다.**
//     ★ 이게 인터페이스의 쓸모다 — 쓰는 쪽은 그대로, 속만 바꿔 끼운다.
//
//   [★ 왜 세션 ID를 그대로 안 넣고 지문(SHA-256)으로 넣나]
//     세션 ID는 '지금 로그인한 상태' 그 자체다. 아는 사람이 곧 그 사람이 된다.
//     DB가 유출됐을 때 원본이 적혀 있으면 그 값을 쿠키에 넣어 남의 계정에 들어갈 수 있다.
//     지문만 있으면 원래 값을 되돌릴 수 없으므로 쓸모가 없다.
//     → **서버가 들고 있는 건 언제나 지문**이다. 원본은 브라우저에만 있다.
//     ※ Django·Laravel의 기본값은 원본 저장이다. 우리는 한 단계 더 조인 쪽을 골랐다.
//
//   [실무에서는]
//     트래픽이 큰 곳은 DB보다 Redis를 쓴다 — 세션은 요청마다 읽고 쓰는데 DB엔 그게 부담이고,
//     Redis는 '몇 초 뒤 알아서 사라짐'(TTL)이 내장돼 있어 세션에 더 맞는 그릇이기 때문.
//     다만 Django는 기본이 DB 세션이고 Laravel도 database 드라이버를 공식 제공한다.
//     즉 DB 세션은 변칙이 아니라 정식 선택지 중 하나다.
// ============================================================

require_once __DIR__ . '/db.php';

// 세션 하나가 서버에서 살아 있는 시간(초). 마지막 사용 시각으로부터 잰다.
//   ★ 쿠키 쪽 만료와 별개로 서버에도 두는 이유: 쿠키 만료는 '브라우저가 지키는 약속'이라
//     사용자가 늘릴 수 있다. 진짜 기준은 언제나 서버가 쥐고 있어야 한다.
const SESSION_TTL = 1800;          // 30분

// ── 조회 판정 세션 — 이름과 수명 ────────────────────────────
//   ★★ 이 두 상수가 **여기** 있는 이유 (실제로 한 번 크게 밟았다):
//     처음엔 view_session.php에 뒀는데, 그 파일을 안 부르는 화면
//     (예: auth/authenticate.php)에서 **세션 저장이 통째로 터졌다** —
//         Fatal error: Undefined constant "VIEW_SESSION_NAME"
//         #1 session_write_close()
//     세션 저장이 실패하면 그 요청의 $_SESSION이 통째로 사라지므로,
//     **로그인해도 로그인이 안 되는** 증상이 된다. 화면엔 아무 말도 안 나온다.
//
//   ★ 옮길 곳을 고른 기준: **쓰는 파일이 곧 소유자.**
//     세션 이름에 따라 수명을 다르게 주는 판단은 아래 write()가 한다 → 여기가 맞다.
//   ⚠️ 반대로 이 파일이 view_session.php를 require 하면 안 된다 —
//     그쪽은 '세션이 이미 열려 있다'는 전제로 session.php를 부르므로 고리가 생긴다.
const VIEW_SESSION_NAME = 'VIEWSESS';   // 쿠키를 안 쓰므로 이름은 저장소 구분용일 뿐
const VIEW_SESSION_DAYS = 1;            // 판정은 하루 단위

// 번호표(세션 ID) → DB에 넣을 지문.
//   ★ 클래스 밖에 둔 이유: 아래 핸들러 말고 '이 회원의 세션 전부 끊기'도 같은 지문을 내야 한다.
//     지문 내는 방법이 두 곳으로 갈리면, 한쪽을 고쳤을 때 다른 쪽이 조용히 못 찾게 된다.
//     (같은 값을 두 곳에서 만들면 한쪽만 고쳐졌을 때 조용히 어긋난다)
function session_fingerprint(string $sessionId): string {
    return hash('sha256', $sessionId);
}

// 이 회원의 세션 중 '지금 쓰는 것만 빼고' 전부 지운다. → 다른 기기가 즉시 로그아웃된다.
//   ★ 파일 방식으로는 이게 아예 불가능했다. 남의 기기 세션 파일을 찾을 방법이 없기 때문.
//     세션을 DB로 옮겨서 생긴 능력이다.
//   반환값 = 끊어낸 세션 수 (몇 곳에서 로그아웃됐는지 알림에 쓴다).
function destroy_other_sessions(int $userId, string $keepSessionId): int {
    $sql = 'DELETE FROM sessions WHERE user_id = ? AND id_hash <> ?';
    $stmt = db()->prepare($sql);
    $stmt->execute([$userId, session_fingerprint($keepSessionId)]);
    return $stmt->rowCount();
}

// ── 매 요청마다 번호표 갈아 끼우기 (rotation) ────────────────
//   [무엇을 줄이나]
//     번호표를 훔쳐가도 **주인이 다음 요청을 보내는 순간** 그 번호표는 옛것이 된다.
//     활동 중이라면 훔친 값의 수명이 **20~30분 → 30초 남짓**으로 줄어든다.
//
//   [★★ 왜 옛 번호표를 바로 안 죽이나 — 유예(grace)가 필요한 이유]
//     한 화면을 여는 동안 PHP 요청이 **여러 개 겹칠 수 있다**:
//       · 링크를 빠르게 연속 클릭  · 탭 두 개에서 동시에 조작
//       · F5 연타                  · 임시저장(fetch)과 페이지 이동이 겹칠 때
//     이때 각 요청이 새 번호표를 받으면 **브라우저는 마지막 것만 남긴다.**
//     옛것을 즉시 죽이면 나머지 요청들이 **일제히 로그아웃**된다.
//     → 그래서 옛 번호표를 **잠깐(30초) 더 살려둔다.** 겹친 요청이 무사히 끝날 시간이다.
//
//   [★ 한계도 분명히]
//     이건 **훔친 값의 수명을 줄이는 것**이지 **훔치는 걸 막는 게 아니다.**
//     실제 공격은 초 단위로 자동화되므로 30초 안에 이미 쓰인다.
//     그리고 주인이 손을 놓으면 회전이 멈춰서 효과도 멈춘다.
//     ※ 질적으로 다른 해법은 '자주 갈아주기'가 아니라 **'갈아줄 자격을 묻기'** (DBSC)다.
const SESSION_GRACE = 30;          // 옛 번호표를 몇 초 더 살려둘지

// 옛 번호표의 수명만 짧게 줄인다. (지우지는 않는다)
//   ★ 수명이 다한 행은 같은 자리에서 치운다.
//     [왜 gc()에 맡기지 않나]
//       PHP는 청소를 **확률적으로**(기본 1/100) 부른다. 회전을 켜면 요청마다 행이 하나씩
//       늘어나므로, 청소가 올 때까지 표가 **수십 줄로 불어난다.**
//       세션이 눈에 보이는 게 이 프로젝트의 장점인데, **정작 봐야 할 행이 파묻힌다.**
//     ※ 요청마다 DELETE가 한 번 더 도는 대가는 있다. 우리 규모에서는 무시할 만하고,
//       표가 깨끗해서 얻는 게 더 크다고 판단했다.
// 이 회원의 **유예 중인 행**을 지금 전부 지운다.
//   [★ 왜 필요한가 — 로그아웃에 30초짜리 구멍이 있었다]
//     로그아웃은 **지금 쓰는 번호표의 행**만 지운다. 그런데 요청마다 회전이 일어나므로,
//     직전 몇 초 사이의 요청들이 남긴 **옛 행이 user_id를 든 채 30초씩 더 살아 있다.**
//     실측: 로그인하고 세 번 눌렀더니 uid가 붙은 행이 **네 개**였다.
//     그 30초 동안 옛 번호표로 접근하면 **로그아웃했는데 로그인 상태**다.
//
//   ★ 유예는 '겹친 요청을 살리려고' 준 것이지 '로그아웃을 늦추려고' 준 게 아니다.
//     끊는 자리에서는 걷어낸다.
//   ★ 지우는 대상을 '유예 중인 행'으로 좁힌 이유: 다른 기기의 **정상 세션**은
//     만료가 30분 뒤라 여기 안 걸린다. 어차피 30초 안에 죽을 행만 앞당겨 죽인다.
function destroy_graced_sessions(int $userId): void {
    db()->prepare('DELETE FROM sessions
                    WHERE user_id = ? AND expires_at <= DATE_ADD(NOW(), INTERVAL ? SECOND)')
        ->execute([$userId, SESSION_GRACE]);
}

function expire_session_soon(string $oldSessionId): void {
    db()->prepare('UPDATE sessions SET expires_at = DATE_ADD(NOW(), INTERVAL ? SECOND)
                    WHERE id_hash = ?')
        ->execute([SESSION_GRACE, session_fingerprint($oldSessionId)]);

    db()->query('DELETE FROM sessions WHERE expires_at < NOW()');
}

// 이 회원의 세션을 **지금 쓰는 것까지 포함해 전부** 지운다.
//   [언제 쓰나 — '아무도 남기지 않아야 하는' 상황]
//     · 비밀번호를 바꿨을 때
//   ★ destroy_other_sessions와 달리 **남기는 게 없다.**
//     도난 상황에서는 **어느 쪽이 주인이고 어느 쪽이 도둑인지 알 수 없기 때문**이다.
//     한 곳이라도 살려두면 그게 도둑일 수 있다. 그래서 전부 끊고 비밀번호를 다시 묻는다.
//   반환값 = 끊어낸 세션 수.
function destroy_all_sessions(int $userId): int {
    $stmt = db()->prepare('DELETE FROM sessions WHERE user_id = ?');
    $stmt->execute([$userId]);
    return $stmt->rowCount();
}

// 세션을 DB에 읽고 쓰는 담당자.
//   PHP가 정해준 6개 메서드를 채우면 된다. 우리가 직접 부르는 메서드는 하나도 없고,
//   PHP가 알맞은 때에 알아서 부른다:
//     open   세션 시작할 때        close  요청이 끝날 때
//     read   $_SESSION을 채울 때   write  $_SESSION을 저장할 때
//     destroy  session_destroy()   gc     만료된 것 청소할 때
// ★★ 인터페이스가 **둘**이다.
//   SessionHandlerInterface           — 읽고 쓰고 지우는 6개 (저장소를 바꿔 끼우는 부분)
//   SessionUpdateTimestampHandlerInterface — validateId + updateTimestamp
//
//   [★ 두 번째를 빠뜨려서 use_strict_mode 가 **꺼진 것과 같았다**]
//     `session.use_strict_mode = 1` 은 *"서버가 발급한 적 없는 번호표는 거부하라"* 는 설정인데,
//     PHP는 그 판단을 **핸들러의 validateId()에게 묻는다.** 우리가 그걸 구현하지 않아서
//     PHP는 물어볼 곳이 없었고, 결국 **아무 번호표나 받아들이고 있었다.**
//     실측: `PHPSESSID=deadbeef…` 를 지어내 보냈더니 거부는커녕 그 번호로 행이 생겼다.
//   ★ 설정을 켜는 것과 그 설정이 도는 것은 다르다. **켜뒀다고 적기 전에 시험해봐야 한다.**
final class DbSessionHandler implements SessionHandlerInterface, SessionUpdateTimestampHandlerInterface
{
    // 이 번호표가 **우리가 발급해서 실제로 저장한 것**인가?
    //   [PHP가 이 함수를 두 곳에서 쓴다]
    //     ① 사용자가 들고 온 번호표 검사 — false면 그 번호를 버리고 새로 발급한다
    //     ② 새 번호를 만들 때 충돌 검사 — false(=아직 없음)가 나올 때까지 다시 뽑는다
    //   → 그래서 뜻은 하나로 정리된다: **"이 번호로 살아 있는 세션이 있는가"**
    //
    //   ★ read()와 조건이 같아야 한다(`expires_at > NOW()`). 다르면
    //     "검사는 통과하는데 읽으면 비어 있는" 어긋난 상태가 생긴다.
    //
    //   ⚠️ 조회 판정 세션(VIEWSESS)은 **서버가 계산한 번호**를 쓴다. 그 번호는 처음엔
    //     저장된 적이 없으므로 여기서 false가 나고, 엄격 모드면 PHP가 번호를 갈아버려
    //     **매번 새 서랍이 열린다.** → view_session.php가 그 구간만 use_strict_mode를 끈다.
    //     (엄격 모드가 꺼져 있으면 PHP는 이 함수를 아예 부르지 않는다)
    public function validateId(string $id): bool {
        $stmt = db()->prepare('SELECT 1 FROM sessions WHERE id_hash = ? AND expires_at > NOW()');
        $stmt->execute([session_fingerprint($id)]);

        return (bool) $stmt->fetchColumn();
    }

    // 담긴 값이 하나도 안 바뀐 요청에서 PHP가 write() 대신 부른다(lazy_write).
    //   ★ 우리에겐 그런 요청도 **수명을 미는 일**이 남아 있으므로 write()와 같은 일을 한다.
    //     여기서 아무것도 안 하면 '읽기만 한 요청'에서 세션이 조용히 만료된다.
    public function updateTimestamp(string $id, string $data): bool {
        return $this->write($id, $data);
    }

    // 저장소를 여는 단계. 파일 방식이라면 파일을 열었겠지만,
    //   우리는 db()가 필요할 때 알아서 연결하므로 여기서 할 일이 없다.
    public function open(string $path, string $name): bool {
        return true;
    }

    public function close(): bool {
        return true;
    }

    // 번호표를 받아 그 세션의 내용을 문자열로 돌려준다. PHP가 이걸 $_SESSION으로 풀어준다.
    //   ★ 없을 때 false가 아니라 '빈 문자열'을 돌려줘야 한다.
    //     false는 '읽다가 실패했다'는 뜻이라 PHP가 경고를 낸다. 없는 건 실패가 아니다.
    public function read(string $id): string {
        $sql = 'SELECT payload FROM sessions WHERE id_hash = ? AND expires_at > NOW()';
        //   expires_at 조건을 같이 거는 이유: 청소(gc)는 가끔 돌기 때문에, 만료된 행이
        //   잠시 남아 있을 수 있다. 읽을 때 한 번 더 걸러야 '만료됐는데 로그인된' 상태를 막는다.
        $stmt = db()->prepare($sql);
        $stmt->execute([session_fingerprint($id)]);

        $payload = $stmt->fetchColumn();
        return $payload === false ? '' : (string) $payload;
    }

    // $_SESSION의 내용을 저장한다. 요청이 끝날 때 PHP가 부른다.
    //   같은 번호표가 이미 있으면 덮어쓰고, 없으면 새로 넣는다(UPSERT).
    //   ★ INSERT와 UPDATE를 따로 하면 '있는지 확인 → 넣기' 사이에 다른 요청이 끼어들 수 있다.
    //     ON DUPLICATE KEY UPDATE는 DB가 그 판단을 한 번에 하므로 그런 틈이 없다.
    public function write(string $id, string $data): bool {
        // ── 담긴 게 하나도 없으면 표에 남기지 않는다 ─────────────
        //   홈만 잠깐 보고 나가는 방문까지 행을 만들 이유가 없다. 그런 행이 쌓이면
        //   표가 '아무것도 아닌 것'으로 가득 차서, 정작 봐야 할 세션이 안 보인다.
        //
        //   ★ 그냥 '저장을 건너뛰기'로 하면 안 된다 — 함정이 있다.
        //     알림 하나만 들어 있던 세션이 그 알림을 읽고 비워졌을 때,
        //     건너뛰면 DB에는 **옛 payload(알림 포함)가 그대로 남는다.**
        //     그러면 다음 요청이 그걸 다시 읽어서 **알림이 되살아난다.**
        //   → 그래서 '비었으면 지운다'. 없던 행이면 지울 것도 없으니 그대로 아무 일도 안 일어난다.
        if ($data === '') {
            return $this->destroy($id);
        }

        // ★★ 판정 세션이냐 아니냐로 **적는 칸이 갈린다.**
        //   같은 표에 담기더라도 **무엇을 함께 적느냐**로 그 줄의 성격이 완전히 달라진다.
        $isViewSession = session_name() === VIEW_SESSION_NAME;

        // user_id는 payload 안에도 들어 있지만, 칼럼으로 한 번 더 꺼내 둔다.
        //   payload는 통째로 직렬화된 덩어리라 SQL로 뒤질 수가 없기 때문이다.
        //   이 칼럼이 있어야 "이 회원의 세션 전부 삭제"를 WHERE 한 줄로 할 수 있다.
        //   ★ 판정 세션에는 안 적는다 — 적으면 `WHERE user_id = ?` 한 줄로
        //     **"이 회원이 오늘 본 글 전부"** 가 나온다. 그게 없애려던 바로 그 표다.
        $userId = $isViewSession ? null : ($_SESSION[SESSION_USER_ID] ?? null);

        // ★ 이 세션이 '어느 기기의 것인지'도 함께 적는다.
        //   기기를 끊을 때 그 기기의 세션까지 지우려면 **칼럼**이어야 한다 —
        //   payload 안에 있으면 직렬화된 덩어리라 SQL로 못 찾는다. (user_id를 뺀 것과 같은 이유)
        //   ★ 판정 세션에는 안 적는다 — 위와 같은 이유이고, 애초에 판정 세션은
        //     '기기별로 끊는' 대상이 아니다. 적을 이유가 없는 칸은 안 적는다.
        $deviceId = $isViewSession ? null : (function_exists('device_id') ? device_id() : null);

        $sql = 'INSERT INTO sessions
                    (id_hash, user_id, device_id, payload, ip_address, user_agent, last_active, expires_at)
                VALUES (?, ?, ?, ?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? SECOND))
                ON DUPLICATE KEY UPDATE
                    user_id     = VALUES(user_id),
                    device_id   = VALUES(device_id),
                    payload     = VALUES(payload),
                    last_active = NOW(),
                    expires_at  = VALUES(expires_at)';

        return db()->prepare($sql)->execute([
            session_fingerprint($id),
            $userId !== null ? (int) $userId : null,
            $deviceId,
            $data,
            // 접속 정보. 세션 동작에는 필요 없지만, 이상한 접속을 나중에 되짚어볼 때 쓴다.
            //   길이를 잘라 두는 이유: 칼럼 길이를 넘으면 저장이 실패해 세션이 통째로 날아간다.
            //
            //   [★★ 판정 세션에는 비워 둔다 — 이걸 빠뜨려서 없앤 표가 되살아나 있었다]
            //     판정 세션의 payload에는 **"오늘 본 글 목록"** 이 들어 있다.
            //     그 옆 칸에 접속지를 적으면 한 줄이 이렇게 된다:
            //         접속지 172.18.0.1 │ 23·24·25번 글을 봤음
            //     → `SELECT ip_address, payload FROM sessions` 한 줄로 **열람 이력**이 나온다.
            //     `post_views` 표를 없앤 이유가 정확히 그것인데, 이름만 바꿔 그대로 있었다.
            //
            //     ★ 판정에는 이 칸을 **읽지도 않는다.** 판정은 '번호로 줄을 찾아
            //       안에 오늘 날짜가 있나'만 본다. → 비워도 동작이 하나도 안 달라진다.
            //     ⚠️ 완전한 익명은 아니다 — 접속지를 아는 사람은 번호를 계산해 그 줄을
            //       찾을 수 있다. 막히는 것은 **표를 훑어서 전부 뽑기** 쪽이다.
            $isViewSession ? null : substr($_SERVER['REMOTE_ADDR'] ?? '', 0, 45),
            $isViewSession ? null : substr($_SERVER['HTTP_USER_AGENT'] ?? '', 0, 255),
            // ★ 세션마다 수명이 다르다. 이름을 보고 갈라준다.
            //   판정 세션(VIEWSESSID)은 쿠키가 하루인데 서버 쪽만 30분이면
            //   **쿠키는 살아 있는데 내용이 비어 있는** 상태가 되어 매일 여러 번 세어진다.
            //   → 쿠키 수명과 저장분 수명은 **반드시 같이 간다.**
            $isViewSession ? VIEW_SESSION_DAYS * 86400 : SESSION_TTL,
        ]);
    }

    // 이 세션 하나를 없앤다. 로그아웃의 session_destroy()가 여기로 온다.
    public function destroy(string $id): bool {
        return db()->prepare('DELETE FROM sessions WHERE id_hash = ?')
                   ->execute([session_fingerprint($id)]);
    }

    // 만료된 세션 청소. PHP가 가끔(확률적으로) 부른다.
    //   반환값 = 지운 개수. 파일 방식에서는 오래된 파일을 지우던 자리다.
    public function gc(int $maxLifetime): int|false {
        $stmt = db()->prepare('DELETE FROM sessions WHERE expires_at < NOW()');
        $stmt->execute();
        return $stmt->rowCount();
    }
}
