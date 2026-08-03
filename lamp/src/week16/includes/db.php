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

// ── LIKE 검색용 패턴 만들기 ─────────────────────────────────
//   '기생' → '%기생%' = "앞뒤에 뭐가 붙든 이 글자가 들어 있으면 찾아라".
//
//   ★ 왜 % 를 SQL이 아니라 '값'에 붙이나
//     WHERE title LIKE '%기생%' 처럼 SQL 문장에 검색어를 직접 이어붙이면,
//     누군가 검색창에 SQL 조각을 적어 보내는 순간 그게 명령이 되어버린다(SQL 인젝션).
//     % 까지 값에 담아 ? 자리에 넣으면, DB는 그것을 끝까지 '찾을 글자'로만 취급한다.
//
//   ★ % 와 _ 는 LIKE에서 특별한 뜻을 가진 글자다 (% = 아무 글자 여러 개, _ = 아무 글자 하나).
//     검색어에 그대로 들어오면 뜻이 뒤틀린다 — '100%'로 검색하면 '100'으로 시작하는 게 전부 걸린다.
//     그래서 앞에 \ 를 붙여 '그냥 글자'로 만든다. \ 자체를 먼저 바꾸는 순서가 중요하다.
function create_like_pattern(string $keyword): string {
    $escaped = str_replace(['\\', '%', '_'], ['\\\\', '\%', '\_'], $keyword);
    return '%' . $escaped . '%';
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
