<?php
// ============================================================
// media.php — '작품(media)' 도메인 모듈 (우리 DB 담당)
//   tmdb.php는 'TMDB에서 읽어오기', 이 파일은 '우리 DB에 저장/조회'를 담당한다.
//   week14의 works.php에 해당하지만, 데이터가 세션이 아니라 MariaDB로 바뀐다.
// ============================================================

require_once __DIR__ . '/db.php';

// ── 작품 하나를 우리 DB에 '보장'한다 (있으면 그대로, 없으면 저장) ──
//   글을 쓰려면 그 작품이 우리 media 표에 있어야 한다(외래키 때문).
//   TMDB에서 고른 작품을 넘기면, 이 함수가 우리 media.id를 돌려준다.
//     · 이미 있으면(tmdb_id로 확인) → 그 id 반환
//     · 없으면 → INSERT 후 새 id 반환
//   $tmdbItem: tmdb.php의 build_media_from_tmdb()가 만든 배열
function ensure_media(array $tmdbItem): int {
    $tmdbId = (int) ($tmdbItem['tmdb_id'] ?? 0);

    // ── ① 이미 우리 DB에 있나? (tmdb_id로 조회) ──────────────
    //   prepare + execute([...]) = Prepared Statement.
    //   값을 SQL 문자열에 직접 안 끼우고 ? 자리에 '따로' 넣는다 → SQL 인젝션 방어.
    //
    //   ★ 왜 막히나 (원리): SQL '구조'를 먼저 확정(prepare)하고 '값'은 나중에(execute).
    //     구조가 이미 고정됐으니 ? 에 뭘 넣어도 '명령'이 아니라 '찾을 값'으로만 처리됨.
    //       · 정상:  execute([496243])
    //                → WHERE tmdb_id = 496243  (그 값을 찾음)
    //       · 공격:  execute(["0; DROP TABLE media"])
    //                → tmdb_id 가 "0; DROP TABLE media" 인 행을 찾음 → 없음, 표는 멀쩡
    //                  (문자열 직접 연결이었다면 DROP TABLE 이 실행됐을 것)
    //     week14의 e()가 HTML을 '그냥 글자'로 만든 것과 같은 사상.
    //
    //   [동작 예시] $tmdbId = 496243 (기생충) 일 때
    //     prepare : "SELECT id FROM media WHERE tmdb_id = ?"  ← ?는 아직 빈 자리
    //     execute([496243]) : ? 에 496243을 끼워 실제 조회 실행
    //     · 기생충이 media 표에 이미 있으면 → fetchColumn()이 그 id(예: 1)를 반환
    //     · 아직 없으면                     → fetchColumn()이 false 반환
    $stmt = db()->prepare('SELECT id FROM media WHERE tmdb_id = ?');
    $stmt->execute([$tmdbId]);
    $existingId = $stmt->fetchColumn();   // 있으면 id(숫자, 예: 1), 없으면 false

    if ($existingId !== false) {
        return (int) $existingId;         // 이미 있으니 그 id를 그대로 쓴다
    }

    // ── ② 없으면 새로 저장(INSERT) ───────────────────────────
    //   slug: 주소용 영문 이름이 필요한데 TMDB엔 없으므로, 'tmdb-<번호>'로 만든다.
    //         (예: tmdb-496243) — tmdb_id가 UNIQUE라 slug도 자연히 안 겹침.
    $slug = 'tmdb-' . $tmdbId;

    //   [동작 예시] 기생충을 처음 저장할 때
    //     prepare : "INSERT INTO media (tmdb_id, slug, title, ...) VALUES (?, ?, ?, ...)"
    //     execute : 6개의 ? 자리에 아래 배열 값이 순서대로 들어감
    //       ? ? ? ? ? ?  ←  496243, 'tmdb-496243', '기생충', '영화', 2019, 'https://.../xx.jpg'
    //     → media 표에 새 줄이 생기고, id는 AUTO_INCREMENT가 자동으로 매김(예: 1)
    $stmt = db()->prepare(
        'INSERT INTO media (tmdb_id, slug, title, genre, year, poster_url)
         VALUES (?, ?, ?, ?, ?, ?)'
    );
    $stmt->execute([
        $tmdbId,                          // ? 1 → tmdb_id  (예: 496243)
        $slug,                            // ? 2 → slug     (예: 'tmdb-496243')
        $tmdbItem['title']      ?? '',    // ? 3 → title    (예: '기생충')
        $tmdbItem['genre']      ?? '',    // ? 4 → genre    (예: '영화')
        $tmdbItem['year']       ?? null,  // ? 5 → year     (예: 2019)
        $tmdbItem['poster_url'] ?? '',    // ? 6 → poster_url
    ]);

    // 방금 INSERT한 행의 id(AUTO_INCREMENT가 매긴 번호)를 돌려준다. (예: 1)
    return (int) db()->lastInsertId();
}

// ── id로 작품 하나 조회 (없으면 null) ───────────────────────
//   화면에서 "이 글이 어느 작품인지" 보여줄 때 쓴다.
function get_media(int $id): ?array {
    $stmt = db()->prepare('SELECT * FROM media WHERE id = ?');
    $stmt->execute([$id]);
    $row = $stmt->fetch();           // 한 줄 (없으면 false)
    return $row !== false ? $row : null;
}

// ── slug로 작품 조회 (없으면 null) ──────────────────────────
//   주소가 /board/?work=tmdb-496243 처럼 slug로 올 때 쓴다.
function get_media_by_slug(string $slug): ?array {
    $stmt = db()->prepare('SELECT * FROM media WHERE slug = ?');
    $stmt->execute([$slug]);
    $row = $stmt->fetch();
    return $row !== false ? $row : null;
}
