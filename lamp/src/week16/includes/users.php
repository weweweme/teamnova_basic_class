<?php
// ============================================================
// users.php — '회원(user)' 찾기 모듈
//   ★ auth.php 와 무엇이 다른가
//     auth.php = "지금 이 요청을 보낸 사람이 누구인가" (로그인·신원 확인).
//     이 파일  = "이런 이름을 쓰는 회원이 있나" (남을 찾아 보여주기).
//     둘은 다루는 표(users)만 같을 뿐 하는 일이 달라서 파일을 나눈다.
//     검색을 auth.php에 넣으면 "인증 파일인데 왜 검색이 있지?"가 된다.
// ============================================================

require_once __DIR__ . '/db.php';

// ── 통합검색: 아이디·닉네임에 검색어가 든 회원 ───────────────
//   ★ 아이디(username)와 닉네임(nickname) 둘 다 본다.
//     아이디는 남에게 보여주는 이름이 아니지만, 프로필 주소가 아이디로 되어 있어
//     ('?user=영화광') 아이디로 찾는 사람도 실제로 있다.
//
//   활동이 많은 사람(글 수)이 위로 오게 정렬한다. 같은 이름 조각이 걸렸을 때
//   글 한 번 안 쓴 계정보다 활발한 사람을 먼저 보여주는 편이 찾는 데 도움이 된다.
//
//   LIMIT에 ? 를 안 쓰고 (int)로 값을 박는 이유는 posts.php의 search_posts_db()와 같다.
function search_users(string $q, int $limit, int $offset = 0): array {
    $sql = "
        SELECT
            u.id,
            u.username,                       -- 아이디 (프로필 주소 ?user= 에 쓰인다)
            u.nickname,                       -- 화면에 보이는 이름
            u.avatar,                         -- 프로필 사진 (없으면 NULL → 화면에서 첫 글자로 대체)
            UNIX_TIMESTAMP(u.joined_at) AS joined,
            -- 활동량: 지운 글·댓글은 빼고 센다 (등급 배지·정렬에 함께 쓰인다)
            (SELECT COUNT(*) FROM posts    p WHERE p.author_id = u.id AND p.deleted_at IS NULL) AS postCount,
            (SELECT COUNT(*) FROM comments c WHERE c.author_id = u.id AND c.deleted_at IS NULL) AS commentCount
        FROM users u
        WHERE u.username LIKE ? OR u.nickname LIKE ?
        ORDER BY postCount DESC, u.id ASC     -- 활발한 사람 먼저, 같으면 먼저 가입한 사람
        LIMIT " . (int) $limit . " OFFSET " . (int) $offset . "
    ";
    $pattern = create_like_pattern($q);
    $stmt = db()->prepare($sql);
    $stmt->execute([$pattern, $pattern]);
    return $stmt->fetchAll();
}

// 검색에 걸린 회원이 전부 몇 명인가 (더보기 표시·페이지 수 계산용).
function count_search_users(string $q): int {
    $pattern = create_like_pattern($q);
    return (int) db_scalar(
        'SELECT COUNT(*) FROM users WHERE username LIKE ? OR nickname LIKE ?',
        [$pattern, $pattern]
    );
}
