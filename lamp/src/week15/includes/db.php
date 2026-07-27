<?php
// ============================================================
// db.php — MariaDB 연결 담당 (한 곳에서만 관리)
//   모든 도메인 모듈(posts·comments·auth…)이 이 db()를 통해 DB에 접근한다.
//   ★ 연결 정보(host·비번)는 config.php에 따로 두고 여기선 쓰기만 한다.
// ============================================================

require_once __DIR__ . '/config.php';   // DB_HOST, DB_USER 등

// ── DB 연결을 돌려준다 (없으면 만들고, 있으면 재사용) ────────
//   ★ static: 한 요청 안에서 연결을 '딱 한 번만' 만든다.
//     한 페이지를 그리는 데 get_posts()·get_comments()… 함수가 수십 번 불린다.
//     매번 새로 연결하면 낭비이므로, 처음 만든 연결을 계속 재사용한다.
//     (week14의 "반복 호출에서 매번 새로 만들지 말라"와 같은 원칙)
function db(): PDO {
    static $pdo = null;

    if ($pdo === null) {
        // DSN(Data Source Name) = "어디에 어떻게 붙을지" 한 줄 주소.
        //   charset=utf8mb4 : 한글·이모지까지 안전하게 (utf8만 쓰면 일부 이모지 깨짐)
        $dsn = 'mysql:host=' . DB_HOST
             . ';port=' . DB_PORT
             . ';dbname=' . DB_NAME
             . ';charset=utf8mb4';

        // PDO 연결 생성 + 동작 옵션 지정
        $pdo = new PDO($dsn, DB_USER, DB_PASS, [
            // ① 에러가 나면 '조용히 넘어가지 말고' 예외를 던져라.
            //    → 실수(오타난 SQL 등)를 바로 알 수 있다. (기본값은 조용히 실패라 위험)
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,

            // ② 결과를 '열 이름을 키로 하는 배열'로 받아라.
            //    → $row['title'] 처럼 이름으로 접근. (week14 배열과 똑같은 모양)
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,

            // ③ Prepared Statement를 '진짜로' 서버에서 처리하라 (에뮬레이션 끄기).
            //    → SQL 인젝션 방어가 더 확실해지고, 숫자/문자 타입도 정확히 지켜진다.
            PDO::ATTR_EMULATE_PREPARES   => false,
        ]);
    }

    return $pdo;
}

// ── 값 하나만 꺼내는 편의 함수 ──────────────────────────────
//   "SELECT id FROM ... WHERE ..." 처럼 결과가 값 하나일 때, 매번
//   prepare→execute→fetchColumn 세 줄 쓰기 번거로워서 한 줄로 감쌌다.
//   없으면 false를 돌려준다. (호출한 쪽에서 (int) 형변환 등으로 처리)
function db_scalar(string $sql, array $params = []): mixed {
    $stmt = db()->prepare($sql);
    $stmt->execute($params);
    return $stmt->fetchColumn();
}
