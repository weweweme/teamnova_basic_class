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
//     → remember_tokens와 완전히 같은 방침이다. **서버가 들고 있는 건 언제나 지문.**
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

// 번호표(세션 ID) → DB에 넣을 지문.
//   ★ 클래스 밖에 둔 이유: 아래 핸들러 말고 '다른 기기에서 로그아웃'도 같은 지문을 내야 한다.
//     지문 내는 방법이 두 곳으로 갈리면, 한쪽을 고쳤을 때 다른 쪽이 조용히 못 찾게 된다.
//     (remember.php가 쿠키 옵션을 한 함수에 모아 둔 것과 같은 이유다)
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

// 이 회원이 지금 몇 곳에서 로그인 중인가 (지금 쓰는 기기는 빼고).
//   설정 화면에 "다른 기기 2곳에서 로그인 중"을 보여주려고 쓴다.
//   ★ expires_at 조건을 거는 이유: 청소(gc)는 가끔 돌아서 만료된 행이 잠시 남아 있다.
//     그걸 세면 "로그인 중"이라고 거짓말을 하게 된다.
function count_other_sessions(int $userId, string $keepSessionId): int {
    $sql = 'SELECT COUNT(*) FROM sessions
             WHERE user_id = ? AND id_hash <> ? AND expires_at > NOW()';
    $stmt = db()->prepare($sql);
    $stmt->execute([$userId, session_fingerprint($keepSessionId)]);
    return (int) $stmt->fetchColumn();
}

// 세션을 DB에 읽고 쓰는 담당자.
//   PHP가 정해준 6개 메서드를 채우면 된다. 우리가 직접 부르는 메서드는 하나도 없고,
//   PHP가 알맞은 때에 알아서 부른다:
//     open   세션 시작할 때        close  요청이 끝날 때
//     read   $_SESSION을 채울 때   write  $_SESSION을 저장할 때
//     destroy  session_destroy()   gc     만료된 것 청소할 때
final class DbSessionHandler implements SessionHandlerInterface
{
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

        // user_id는 payload 안에도 들어 있지만, 칼럼으로 한 번 더 꺼내 둔다.
        //   payload는 통째로 직렬화된 덩어리라 SQL로 뒤질 수가 없기 때문이다.
        //   이 칼럼이 있어야 "이 회원의 세션 전부 삭제"를 WHERE 한 줄로 할 수 있다.
        $userId = $_SESSION[SESSION_USER_ID] ?? null;

        $sql = 'INSERT INTO sessions
                    (id_hash, user_id, payload, ip_address, user_agent, last_active, expires_at)
                VALUES (?, ?, ?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL ? SECOND))
                ON DUPLICATE KEY UPDATE
                    user_id     = VALUES(user_id),
                    payload     = VALUES(payload),
                    last_active = NOW(),
                    expires_at  = VALUES(expires_at)';

        return db()->prepare($sql)->execute([
            session_fingerprint($id),
            $userId !== null ? (int) $userId : null,
            $data,
            // 접속 정보. 세션 동작에는 필요 없지만 '내 로그인 기기 목록' 같은 화면에 쓰인다.
            //   길이를 잘라 두는 이유: 칼럼 길이를 넘으면 저장이 실패해 세션이 통째로 날아간다.
            substr($_SERVER['REMOTE_ADDR'] ?? '', 0, 45),
            substr($_SERVER['HTTP_USER_AGENT'] ?? '', 0, 255),
            SESSION_TTL,
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
