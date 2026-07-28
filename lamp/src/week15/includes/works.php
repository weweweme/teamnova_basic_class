<?php
// ============================================================
// works.php — '작품(media)' 조회 + 투표 도메인 모듈
//   데이터는 우리 DB(media 표) + TMDB(아직 저장 안 된 작품)에서 온다.
//   투표는 votes 표를 쓴다.
//   ★ board가 쓰는 반환 모양: slug·title·genre·year·summary·poster_url·upVotes·downVotes
// ============================================================

require_once __DIR__ . '/db.php';
require_once __DIR__ . '/auth.php';    // current_user_id()
require_once __DIR__ . '/tmdb.php';    // TMDB 폴백 조회
require_once __DIR__ . '/media.php';   // ensure_media() — 작품을 우리 DB에 저장

// ── 작품 하나 조회 (우리 DB 먼저, 없으면 TMDB) ──────────────
//   board가 /board/?work=tmdb-496243 로 들어올 때 쓴다.
//   ① media 표에 있으면 → 그 정보 + 투표 집계
//   ② 아직 없으면(아무도 글·투표 안 함) → TMDB에서 가져와 보여줌 (투표수 0)
//   ③ 진짜 없으면 → null
function get_work(string $slug): ?array {
    // ① 우리 DB에서 찾기
    $stmt = db()->prepare('SELECT * FROM media WHERE slug = ?');
    $stmt->execute([$slug]);
    $row = $stmt->fetch();

    if ($row !== false) {
        $counts = media_vote_counts((int) $row['id']);   // 투표 집계
        return [
            'slug'       => $row['slug'],
            'title'      => $row['title'],
            'genre'      => $row['genre'],
            'year'       => $row['year'],
            'summary'    => $row['overview'] ?? '',       // board는 'summary'라는 이름으로 쓴다
            'poster_url' => $row['poster_url'] ?? '',
            'upVotes'    => $counts['up'],
            'downVotes'  => $counts['down'],
        ];
    }

    // ② DB에 없지만 'tmdb-<번호>' 슬러그면 → TMDB에서 가져온다 (아직 저장 안 된 작품)
    if (str_starts_with($slug, 'tmdb-')) {
        $tmdbId = (int) substr($slug, 5);            // 'tmdb-496243' → 496243
        $item = tmdb_find_by_id($tmdbId);
        if ($item !== null) {
            return [
                'slug'       => $slug,
                'title'      => $item['title'],
                'genre'      => $item['genre'],
                'year'       => $item['year'],
                'summary'    => $item['overview'] ?? '',
                'poster_url' => $item['poster_url'] ?? '',
                'upVotes'    => 0,                    // 아직 우리 DB에 없으니 투표도 0
                'downVotes'  => 0,
            ];
        }
    }

    return null;                                     // 어디에도 없음
}

// ── 이 작품의 추천/비추천 표 수를 센다 ─────────────────────
function media_vote_counts(int $mediaId): array {
    $up   = (int) db_scalar(
        "SELECT COUNT(*) FROM votes WHERE media_id = ? AND choice = '추천'",   [$mediaId]);
    $down = (int) db_scalar(
        "SELECT COUNT(*) FROM votes WHERE media_id = ? AND choice = '비추천'", [$mediaId]);
    return ['up' => $up, 'down' => $down];
}

// ── 내가 이 작품에 무엇을 투표했나? ('추천'/'비추천'/안 했으면 null) ──
function my_vote(string $slug): ?string {
    $userId  = current_user_id();
    $mediaId = (int) db_scalar('SELECT id FROM media WHERE slug = ?', [$slug]);
    if ($userId === 0 || $mediaId === 0) {
        return null;                     // 로그인 안 했거나, 작품이 아직 DB에 없으면 투표도 없다
    }
    $choice = db_scalar(
        'SELECT choice FROM votes WHERE user_id = ? AND media_id = ?',
        [$userId, $mediaId]
    );
    return $choice !== false ? $choice : null;
}

// ── 투표 토글 (안 했으면 투표 / 같은 걸 또 누르면 취소 / 반대면 갈아타기) ──
//   ★ 투표하려면 그 작품이 media 표에 있어야 한다(외래키). 아직 없으면 TMDB에서
//     가져와 먼저 저장한다(ensure_media). → 첫 투표가 곧 작품을 우리 DB로 들여온다.
function toggle_vote(string $slug, string $choice): void {
    $userId  = current_user_id();
    if ($userId === 0) {
        return;                          // 로그인 안 했으면 아무것도 안 함
    }
    $mediaId = ensure_media_by_slug($slug);   // 없으면 TMDB에서 가져와 저장하고 id 반환
    if ($mediaId === 0) {
        return;                          // 작품 자체가 없으면(잘못된 slug) 중단
    }

    $current = my_vote($slug);
    if ($current === $choice) {
        // 같은 걸 또 누름 → 취소 (그 줄 삭제)
        db()->prepare('DELETE FROM votes WHERE user_id = ? AND media_id = ?')
            ->execute([$userId, $mediaId]);
    } elseif ($current === null) {
        // 처음 투표 → 새 줄
        db()->prepare('INSERT INTO votes (user_id, media_id, choice) VALUES (?, ?, ?)')
            ->execute([$userId, $mediaId, $choice]);
    } else {
        // 반대쪽으로 갈아타기 → choice만 바꿈 (줄은 그대로 = 1인 1표 유지)
        db()->prepare('UPDATE votes SET choice = ? WHERE user_id = ? AND media_id = ?')
            ->execute([$choice, $userId, $mediaId]);
    }
}

// ── slug로 media.id를 얻는다. 없으면 TMDB에서 가져와 저장하고 그 id를 준다. ──
//   글쓰기·투표처럼 '작품이 반드시 우리 DB에 있어야 하는' 순간에 쓴다.
function ensure_media_by_slug(string $slug): int {
    $mediaId = (int) db_scalar('SELECT id FROM media WHERE slug = ?', [$slug]);
    if ($mediaId !== 0) {
        return $mediaId;                 // 이미 있으면 그대로
    }
    if (str_starts_with($slug, 'tmdb-')) {
        $item = tmdb_find_by_id((int) substr($slug, 5));
        if ($item !== null) {
            return ensure_media($item);  // TMDB 정보로 저장하고 새 id 반환
        }
    }
    return 0;                            // 저장할 수 없음
}

// ── 우리 커뮤니티에서 '이야기 중인' 작품 (홈의 우리 줄) ─────
//   글이 1개 이상 달린 작품만, 글 많은 순. 각 작품에 커뮤니티 지표(글 수·투표)를 붙인다.
//   ★ TMDB 목록과 달리 이건 '우리 것' — 우리 유저 반응이 있는 작품.
//   $limit: 몇 개까지 (홈은 그리드 5칸에 맞춰 5개만 넘긴다). LIMIT엔 값을 바인딩하기
//           까다로워서 (int) 형변환으로 안전하게 문자열에 박는다(SQL 주입 걱정 없음).
function get_community_works(int $limit = 20): array {
    $sql = "
        SELECT m.slug, m.title, m.genre, m.year, m.poster_url,
               (SELECT COUNT(*) FROM posts p WHERE p.media_id = m.id AND p.deleted_at IS NULL) AS postCount
        FROM media m
        HAVING postCount > 0          -- 글 달린 작품만
        ORDER BY postCount DESC        -- 이야기 많은 순(=핫한 순)
        LIMIT " . (int) $limit . "
    ";
    $rows = db()->query($sql)->fetchAll();
    foreach ($rows as &$row) {
        $counts = media_vote_counts((int) db_scalar('SELECT id FROM media WHERE slug = ?', [$row['slug']]));
        $total = $counts['up'] + $counts['down'];
        $row['upPct'] = $total > 0 ? (int) round($counts['up'] / $total * 100) : null;  // 투표 없으면 null
    }
    return $rows;
}

// ── slug로 작품 제목만 (없으면 null) ────────────────────────
function get_work_title(string $slug): ?string {
    $work = get_work($slug);
    return $work['title'] ?? null;
}
