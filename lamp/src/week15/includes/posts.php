<?php
// ============================================================
// posts.php — '글(post)' 데이터 + 관련 동작을 모아둔 도메인 모듈
//   데이터는 MariaDB의 posts 표에서 가져온다 (읽기는 JOIN, 쓰기는 INSERT/UPDATE).
//   화면 파일은 이 함수들만 부르므로, 저장 방식이 세션→DB로 바뀌어도 화면은 그대로다.
// ============================================================

require_once __DIR__ . '/db.php';         // DB 연결
require_once __DIR__ . '/auth.php';       // current_user_id() (추천 여부 확인)
require_once __DIR__ . '/comments.php';   // 댓글 모듈 (일부 함수에서 사용)

// ── 입력 길이 제한 (매직값 금지 — 이름 붙인 상수로) ──────────
const POST_TITLE_MAX   = 100;
const POST_CONTENT_MAX = 5000;
const SEARCH_QUERY_MAX = 50;

// ── 모든 글을 DB에서 가져온다 (JOIN으로 여러 표를 합쳐서) ──
//   ★ 정규화로 '나눠 둔' 표들을 화면용으로 '다시 합치는' 게 JOIN. (발표자료의 그 JOIN)
//     · 글(posts)     — 제목·내용·감상·조회수
//     · 작품(media)   — slug·제목      ← posts.media_id = media.id 로 연결
//     · 회원(users)   — 닉네임          ← posts.author_id = users.id 로 연결
//     · 댓글 수·추천 수 — 하위질의(서브쿼리)로 그 글의 개수를 세어 붙인다
//   반환 배열의 '모양'은 week14와 똑같게 맞춘다(work·workTitle·author·comments·likes…)
//   → 그래야 board·필터·정렬 함수가 한 줄도 안 바뀐다. ('함수 속만 바꾼다'의 핵심)
function get_posts(): array {
    $sql = "
        SELECT
            p.id,
            m.slug        AS work,        -- 주소용 작품 이름 (?work=...)
            m.title       AS workTitle,   -- 작품 제목
            p.title,
            u.username    AS author,      -- 작성자 아이디(신원 키: 소유권 확인·프로필 URL용)
            u.nickname    AS authorNick,  -- 작성자 표시 이름(화면에 보이는 닉네임)
            p.sentiment,
            p.views,
            p.content,
            UNIX_TIMESTAMP(p.created_at) AS created,   -- 정렬용 숫자(최신일수록 큼)
            (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comments,  -- 이 글의 댓글 수
            (SELECT COUNT(*) FROM likes    l WHERE l.post_id = p.id) AS likes      -- 이 글의 추천 수
        FROM posts p
        JOIN media m ON p.media_id  = m.id   -- 글 ↔ 작품 잇기
        JOIN users u ON p.author_id = u.id   -- 글 ↔ 회원 잇기
        WHERE p.deleted_at IS NULL           -- 소프트삭제: 지워진 글은 뺀다
        ORDER BY p.id DESC                    -- 기본은 최신 글이 위 (정렬 탭이 다시 정할 수 있음)
    ";
    // query()는 결과를 돌려주고, fetchAll()로 '전부 배열로' 받는다.
    //   (FETCH_ASSOC 설정이라 각 줄이 ['title'=>…, 'author'=>…] 모양 = week14와 동일)
    return db()->query($sql)->fetchAll();
}

// '지워진' 글 하나 찾기 (되돌리기에서 주인 확인용). 안 지워졌거나 없으면 null.
//   소프트삭제라 글이 DB에 그대로 있다 → deleted_at 이 '있는'(지워진) 것만 찾는다.
function get_deleted_post(int $id): ?array {
    $sql = "SELECT p.id, u.username AS author
            FROM posts p JOIN users u ON p.author_id = u.id
            WHERE p.id = ? AND p.deleted_at IS NOT NULL";
    $stmt = db()->prepare($sql);
    $stmt->execute([$id]);
    $row = $stmt->fetch();
    return $row !== false ? $row : null;
}

// id로 글 하나 찾기. 없으면 null. (Tester-Doer: 호출한 쪽에서 null 체크)
//   get_posts()와 같은 JOIN이지만 WHERE p.id = ? 로 한 건만 가져온다.
function get_post(int $id): ?array {
    $sql = "
        SELECT
            p.id, m.slug AS work, m.title AS workTitle, p.title,
            u.username AS author, u.nickname AS authorNick, p.sentiment, p.views, p.content,
            UNIX_TIMESTAMP(p.created_at) AS created,
            (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comments,
            (SELECT COUNT(*) FROM likes    l WHERE l.post_id = p.id) AS likes
        FROM posts p
        JOIN media m ON p.media_id  = m.id
        JOIN users u ON p.author_id = u.id
        WHERE p.id = ? AND p.deleted_at IS NULL
    ";
    $stmt = db()->prepare($sql);
    $stmt->execute([$id]);
    $row = $stmt->fetch();
    return $row !== false ? $row : null;
}

// ── DB에 쓰기 (INSERT / UPDATE / 소프트삭제) ─────────────────
//   글 번호(id)는 AUTO_INCREMENT가 자동으로 매기므로 next_post_id 같은 건 필요 없다.

// 새 글 저장 → 새 글 번호(id)를 돌려준다
//   $work=작품 slug, $author=작성자 닉네임 → DB엔 번호로 저장하므로 각각 id로 바꾼다.
//   (작품은 글쓰기 전에 media 표에 이미 있어야 한다 — create.php가 ensure_media로 보장)
function add_post(string $work, string $workTitle, string $title, string $content, string $sentiment, string $author): int {
    // slug → media.id,  닉네임 → users.id 로 변환 (외래키는 번호로 저장하므로)
    $mediaId  = (int) db_scalar('SELECT id FROM media WHERE slug = ?', [$work]);
    $authorId = (int) db_scalar('SELECT id FROM users WHERE username = ?', [$author]);

    $stmt = db()->prepare(
        'INSERT INTO posts (author_id, media_id, title, content, sentiment)
         VALUES (?, ?, ?, ?, ?)'
    );
    $stmt->execute([$authorId, $mediaId, $title, $content, $sentiment]);
    return (int) db()->lastInsertId();
}

// 글 수정 (UPDATE posts SET … WHERE id = ?)
function update_post(int $id, string $title, string $content, string $sentiment): void {
    $stmt = db()->prepare(
        'UPDATE posts SET title = ?, content = ?, sentiment = ? WHERE id = ?'
    );
    $stmt->execute([$title, $content, $sentiment, $id]);
}

// 글 삭제 — ★ 소프트삭제: 진짜 지우지 않고 deleted_at 에 '지운 시각'을 적는다.
//   글은 DB에 그대로 있고, get_posts()가 deleted_at IS NULL 로 걸러 화면에서만 사라진다.
//   → 그 시각을 다시 NULL 로 되돌리면 복구(되돌리기). 실무의 흔한 방식.
function delete_post(int $id): void {
    db()->prepare('UPDATE posts SET deleted_at = NOW() WHERE id = ?')->execute([$id]);
}

// 삭제 되돌리기 — deleted_at 을 다시 NULL 로 (지운 표시를 지운다).
function restore_post(int $id): void {
    db()->prepare('UPDATE posts SET deleted_at = NULL WHERE id = ?')->execute([$id]);
}

// ── 최근 본 글 ───────────────────────────────────────────────
const RECENT_POSTS_MAX = 5;   // 몇 개까지 기억할지

// 방금 본 글을 '최근 본 글' 맨 앞에 넣는다. (post/view.php가 호출)
//
//   ★ "GET 화면인데 세션에 뭘 쓰는 게 규칙 위반 아닌가?" — 아니다.
//     GET이 지켜야 할 '안전(safe)' 규칙은 **남들이 보는 자료를 바꾸지 말라**는 뜻이다.
//     여기서 남기는 건 '내 열람 기록'일 뿐, 글·댓글은 하나도 안 건드린다.
//     그래서 새로고침해도, 남이 같은 주소를 열어도 결과가 달라지지 않는다.
//     (쇼핑몰의 '최근 본 상품', 조회수 집계도 같은 이유로 GET에서 처리한다)
function remember_recent_post(int $id): void {
    $recent = $_SESSION['recent_posts'] ?? [];

    // 이미 목록에 있으면 일단 빼낸다 → 같은 글이 여러 줄로 쌓이는 걸 막고,
    // 아래에서 맨 앞에 다시 넣으면 자연스럽게 '가장 최근'으로 올라온다.
    $recent = array_values(array_filter($recent, fn($seenId) => $seenId !== $id));

    array_unshift($recent, $id);                                  // 맨 앞에 넣기
    $_SESSION['recent_posts'] = array_slice($recent, 0, RECENT_POSTS_MAX);   // 넘치면 오래된 것부터 버림
}

// 최근 본 글 목록 (글 전체를 돌려준다)
//   ★ 그 사이 지워진 글은 get_post()가 null을 주므로 자동으로 빠진다. (Tester-Doer)
function get_recent_posts(): array {
    $result = [];
    foreach ($_SESSION['recent_posts'] ?? [] as $id) {
        $post = get_post($id);
        if ($post !== null) {
            $result[] = $post;
        }
    }
    return $result;
}

// 내가 이 글을 추천했는가?
//   likes 표에 (내 user_id, 이 글 post_id) 조합이 있으면 추천한 것.
function has_liked(int $postId): bool {
    $userId = current_user_id();
    if ($userId === 0) {
        return false;                     // 로그인 안 했으면 추천했을 리 없다
    }
    $found = db_scalar(
        'SELECT 1 FROM likes WHERE user_id = ? AND post_id = ?',
        [$userId, $postId]
    );
    return $found !== false;
}

// 추천 토글: 이미 눌렀으면 취소(DELETE), 안 눌렀으면 추천(INSERT).
//   ★ likes 표의 복합 기본키 (user_id, post_id) 덕분에 '1인 1회'가 구조로 보장된다.
function toggle_like(int $postId): void {
    $userId = current_user_id();
    if ($userId === 0) {
        return;                           // 로그인 안 했으면 아무것도 안 함
    }
    if (has_liked($postId)) {
        // 이미 추천함 → 그 줄을 지운다 (추천 취소)
        db()->prepare('DELETE FROM likes WHERE user_id = ? AND post_id = ?')
            ->execute([$userId, $postId]);
    } else {
        // 아직 안 함 → 줄을 넣는다 (추천)
        db()->prepare('INSERT INTO likes (user_id, post_id) VALUES (?, ?)')
            ->execute([$userId, $postId]);
    }
}

// ── 목록 가공 함수들 (필터·검색·정렬·페이징) ─────────────────

// 특정 작성자(author)의 글만 걸러낸다. (프로필 페이지에서 사용)
function filter_posts_by_author(array $posts, string $author): array {
    $result = [];
    foreach ($posts as $p) {
        if ($p['author'] === $author) {
            $result[] = $p;
        }
    }
    return $result;
}

// 특정 작품(work)의 글만 걸러낸다.
//   작품 게시판은 '그 작품 글'만 보여야 하므로, 목록 만들기의 '첫 단계'가 이것이다.
function filter_posts_by_work(array $posts, string $work): array {
    $result = [];
    foreach ($posts as $p) {
        if ($p['work'] === $work) {
            $result[] = $p;
        }
    }
    return $result;
}

// 검색어(q)가 '제목 또는 내용'에 들어있는 글만 걸러낸다.
//   mb_stripos = 한글 안전한 '포함 여부' 찾기. 못 찾으면 false를 돌려주므로 !== false 로 비교.
function search_posts(array $posts, string $q): array {
    if ($q === '') {
        return $posts;
    }
    $result = [];
    foreach ($posts as $p) {
        // 복합 조건은 이름 붙인 boolean으로 쪼개 읽기 쉽게
        $inTitle   = mb_stripos($p['title'], $q)   !== false;
        $inContent = mb_stripos($p['content'], $q) !== false;
        if ($inTitle || $inContent) {
            $result[] = $p;
        }
    }
    return $result;
}

// 글 목록에서 '그 페이지에 해당하는 분량'만 잘라낸다.
//   $page는 1부터 시작. array_slice(배열, 시작위치, 개수)로 자른다.
function paginate_posts(array $posts, int $page, int $perPage): array {
    $offset = ($page - 1) * $perPage;   // 몇 번째부터 자를지
    return array_slice($posts, $offset, $perPage);
}

// 감상(호평/보통/혹평)으로 글을 걸러낸다. 빈 문자열이면 '전체'.
function filter_posts_by_sentiment(array $posts, string $sentiment): array {
    if ($sentiment === '') {
        return $posts;
    }
    $result = [];
    foreach ($posts as $p) {
        if ($p['sentiment'] === $sentiment) {
            $result[] = $p;
        }
    }
    return $result;
}

// 글 목록을 정렬 기준(sort)대로 정렬해서 돌려준다.
//   usort() = Java의 list.sort(Comparator)와 같음. (b - a면 큰 값이 위로 = 내림차순)
function sort_posts(array $posts, string $sort): array {
    usort($posts, function ($a, $b) use ($sort) {
        switch ($sort) {
            case 'views':    return $b['views']    - $a['views'];       // 조회순
            case 'comments': return $b['comments'] - $a['comments'];    // 댓글순
            case 'hot':      return ($b['views'] + $b['comments'] * 10) // 인기순(조회+댓글 가중)
                                  - ($a['views'] + $a['comments'] * 10);
            case 'new':
            default:         return $b['created']  - $a['created'];     // 최신순
        }
    });
    return $posts;
}
