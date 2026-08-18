<?php
// ============================================================
// posts.php — '글(post)' 데이터 + 관련 동작을 모아둔 도메인 모듈
//   데이터는 MariaDB의 posts 표에서 가져온다 (읽기는 JOIN, 쓰기는 INSERT/UPDATE).
//   화면 파일은 이 함수들만 부르므로, 저장 방식이 세션→DB로 바뀌어도 화면은 그대로다.
// ============================================================

require_once __DIR__ . '/db.php';         // DB 연결
require_once __DIR__ . '/auth.php';       // current_user_id() (추천 여부 확인)
require_once __DIR__ . '/comments.php';   // 댓글 모듈 (일부 함수에서 사용)
require_once __DIR__ . '/view_id.php';    // viewer_key() — 조회 판정 전용 번호
require_once __DIR__ . '/bot.php';        // 봇 조회는 안 센다

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
            UNIX_TIMESTAMP(p.edited_at)  AS edited,    -- 안 고친 글은 NULL

            (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comments,  -- 이 글의 댓글 수
            (SELECT COUNT(*) FROM likes    l WHERE l.post_id = p.id) AS likes,     -- 이 글의 추천 수
            -- 작성자의 총 글 수 (등급 배지 계산용). 소프트삭제된 글은 뺀다.
            (SELECT COUNT(*) FROM posts pc WHERE pc.author_id = p.author_id AND pc.deleted_at IS NULL) AS authorPostCount
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

// ── 이 유저가 '추천(좋아요)'한 글 목록 ──────────────────────
//   ★ likes를 '거꾸로' 조회한다: 보통은 글→추천수를 세지만, 여기선
//     추천(likes)에서 출발해 그 글(posts)을 끌어온다 → "내가 누른 글 모아보기".
//   반환 모양은 get_posts와 동일 → 같은 화면 조각을 그대로 재사용할 수 있다.
function get_liked_posts(int $userId): array {
    $sql = "
        SELECT
            p.id,
            m.slug        AS work,
            m.title       AS workTitle,
            p.title,
            u.username    AS author,
            u.nickname    AS authorNick,
            p.sentiment,
            p.views,
            p.content,
            UNIX_TIMESTAMP(p.created_at) AS created,
            UNIX_TIMESTAMP(p.edited_at)  AS edited,
            (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comments,
            (SELECT COUNT(*) FROM likes    l WHERE l.post_id = p.id) AS likes,
            (SELECT COUNT(*) FROM posts pc WHERE pc.author_id = p.author_id AND pc.deleted_at IS NULL) AS authorPostCount
        FROM likes lk                              -- ★ 추천에서 출발
        JOIN posts p ON lk.post_id  = p.id AND p.deleted_at IS NULL   -- 그 추천이 달린 글
        JOIN media m ON p.media_id  = m.id
        JOIN users u ON p.author_id = u.id
        WHERE lk.user_id = ?                        -- 이 사람이 누른 추천만
        ORDER BY p.id DESC                          -- 최근 글이 위 (likes엔 시각이 없어 글 기준)
    ";
    $stmt = db()->prepare($sql);
    $stmt->execute([$userId]);
    return $stmt->fetchAll();
}

// ── 통합검색: 제목·내용에 검색어가 든 글을 DB가 직접 골라준다 ──
//   ★ 아래 search_posts()와 '하는 일'은 같지만 방식이 다르다.
//     그쪽은 글을 전부 PHP 배열로 가져온 뒤 거른다. 게시판은 이미 '한 작품 글'만
//     다루니 그래도 되지만, 통합검색은 사이트 전체를 뒤지므로 DB에 맡기고
//     필요한 만큼(LIMIT)만 받아온다. 183개든 18만 개든 화면에 필요한 건 20개뿐이다.
//
//   돌려주는 배열 모양은 get_posts()와 똑같이 맞춘다 → 목록을 그리는 화면 코드를 그대로 쓸 수 있다.
//
//   ★ LIMIT 자리에만 ? 를 안 쓰고 값을 직접 박는 이유
//     이 프로젝트는 '진짜' Prepared Statement를 쓰는데(db.php의 EMULATE_PREPARES=false),
//     ? 로 넘긴 값은 기본적으로 문자열이라 LIMIT '20' 이 되어 DB가 거부한다.
//     대신 (int) 로 강제 형변환해 숫자만 들어가게 막는다 — 글자는 0이 되어 무해하다.
function search_posts_db(string $q, int $limit, int $offset = 0): array {
    $sql = "
        SELECT
            p.id,
            m.slug        AS work,
            m.title       AS workTitle,
            p.title,
            u.username    AS author,
            u.nickname    AS authorNick,
            p.sentiment,
            p.views,
            p.content,
            UNIX_TIMESTAMP(p.created_at) AS created,
            UNIX_TIMESTAMP(p.edited_at)  AS edited,
            (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comments,
            (SELECT COUNT(*) FROM likes    l WHERE l.post_id = p.id) AS likes,
            (SELECT COUNT(*) FROM posts pc WHERE pc.author_id = p.author_id AND pc.deleted_at IS NULL) AS authorPostCount
        FROM posts p
        JOIN media m ON p.media_id  = m.id
        JOIN users u ON p.author_id = u.id
        WHERE p.deleted_at IS NULL
          AND (p.title LIKE ? OR p.content LIKE ?)   -- 제목 '또는' 본문에 들어 있으면 찾는다
        ORDER BY p.id DESC                            -- 최신 글이 위
        LIMIT " . (int) $limit . " OFFSET " . (int) $offset . "
    ";
    $pattern = create_like_pattern($q);
    $stmt = db()->prepare($sql);
    $stmt->execute([$pattern, $pattern]);   // 제목·본문 두 자리에 같은 패턴
    return $stmt->fetchAll();
}

// 검색에 걸린 글이 전부 몇 개인가 (더보기 표시·페이지 수 계산용).
//   ★ 목록을 받아 count()하지 않고 DB에 세게 한다. 개수만 필요한데
//     글 내용까지 전부 실어 나르는 건 낭비다. (COUNT는 DB가 가장 잘하는 일)
function count_search_posts(string $q): int {
    $pattern = create_like_pattern($q);
    return (int) db_scalar(
        'SELECT COUNT(*) FROM posts
          WHERE deleted_at IS NULL AND (title LIKE ? OR content LIKE ?)',
        [$pattern, $pattern]
    );
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
            UNIX_TIMESTAMP(p.edited_at)  AS edited,   -- 값이 있으면 '(수정됨)' (댓글과 같은 규칙)
            (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comments,
            (SELECT COUNT(*) FROM likes    l WHERE l.post_id = p.id) AS likes,
            (SELECT COUNT(*) FROM posts pc WHERE pc.author_id = p.author_id AND pc.deleted_at IS NULL) AS authorPostCount
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
//   ★ edited_at 을 함께 찍는다 — 화면에 '(수정됨)'을 띄우기 위해서다.
//     표시가 없으면 글을 올려 반응을 받은 뒤 내용을 슬쩍 바꿔도 읽는 사람이 알 수 없다.
//     created_at(처음 쓴 시각)과 작성자는 건드리지 않는다. 고친 건 내용뿐이니까.
function update_post(int $id, string $title, string $content, string $sentiment): void {
    $stmt = db()->prepare(
        'UPDATE posts SET title = ?, content = ?, sentiment = ?, edited_at = NOW() WHERE id = ?'
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

// ── 휴지통 ───────────────────────────────────────────────────
//   삭제한 글은 deleted_at 표식만 있고 DB엔 남아있다(소프트삭제).
//   이 표식을 '타임스탬프'로 써서, 일정 기간 보관 후 진짜로 지운다.
const TRASH_RETENTION_DAYS = 30;   // 휴지통 보관 기간(일). 지나면 자동 영구삭제.

// 내가 삭제한(휴지통에 있는) 글 목록 — 최근에 지운 것이 위.
function get_trashed_posts(int $userId): array {
    $sql = "
        SELECT p.id, p.title,
               m.title AS workTitle, m.slug AS work,
               UNIX_TIMESTAMP(p.deleted_at) AS deletedAt
        FROM posts p
        JOIN media m ON p.media_id = m.id
        WHERE p.author_id = ? AND p.deleted_at IS NOT NULL   -- 내가 지운 글만
        ORDER BY p.deleted_at DESC
    ";
    $stmt = db()->prepare($sql);
    $stmt->execute([$userId]);
    return $stmt->fetchAll();
}

// 영구삭제(지금 비우기) — 내 휴지통 글만 진짜 DELETE.
//   ★ author_id·deleted_at 조건을 WHERE에 함께 걸어, 남의 글·살아있는 글은 못 지운다.
//   posts를 지우면 그 댓글·추천·신고·알림도 외래키 CASCADE로 함께 사라진다.
function hard_delete_post(int $id, int $userId): void {
    db()->prepare('DELETE FROM posts WHERE id = ? AND author_id = ? AND deleted_at IS NOT NULL')
        ->execute([$id, $userId]);
}

// 자동 영구삭제 — 보관 기간이 지난 휴지통 글을 한꺼번에 진짜 삭제.
//   ★ 이 환경은 cron이 없어, 휴지통을 열 때 이 함수를 불러 '요청에 얹어' 정리한다(lazy purge).
//   반환: 이번에 지워진 글 수.
function purge_expired_trash(): int {
    $sql = 'DELETE FROM posts
            WHERE deleted_at IS NOT NULL
              AND deleted_at < NOW() - INTERVAL ' . TRASH_RETENTION_DAYS . ' DAY';
    $stmt = db()->query($sql);
    return $stmt->rowCount();
}

// ── 최근 본 글 ───────────────────────────────────────────────
//   ★★ week15가 "못 만든다"고 비워뒀던 자리다.
//
//   [왜 week15에서는 못 만들었나]
//     '최근 본 글'은 "이 브라우저가 방금 뭘 봤는지"를 **계속 쌓아야** 하는 기능이다.
//     week15는 서버가 기억할 것을 전부 주소에 실어 날랐는데, 이건 실을 수가 없었다 —
//     ?recent=1,5,9,12,… 는 볼수록 길어지고, 무엇보다 **뒤로가기 한 번에 옛 목록으로 되돌아간다.**
//     '한 번 쓰고 버리는 값'(신원·알림)은 주소로도 되지만, '쌓이는 값'은 안 된다.
//
//   [왜 DB 표(recent_views)로 안 만드나]
//     표를 파면 기록의 주인이 '브라우저'에서 '회원'으로 바뀐다.
//     → 로그인 안 한 방문자는 아예 기록되지 않는다. 같은 기능이 아니라 **다른 기능**이 된다.
//
//   [★ 어디에 담나 — 세션에서 쿠키로 옮겼다]
//     처음엔 세션에 담았다. 그런데 세션은 **창을 닫으면 사라져서**, 브라우저를 껐다 켤 때마다
//     목록이 비어 있었다. 기능의 성격과 안 맞는다.
//     우리가 세운 3조건(①닫아도 남아야 함 ②비로그인도 씀 ③조작당해도 손해 없음)에
//     비춰보니 셋 다 쿠키였다. → 담고 꺼내는 일은 prefs.php가 맡는다.
//     ★ 이 파일은 '번호를 글로 바꾸는 일'만 한다 — 저장 방식이 또 바뀌어도 여기는 안 고친다.

function get_recent_posts(): array {
    $result = [];
    foreach (get_recent_post_ids() as $id) {
        $post = get_post((int) $id);
        if ($post !== null) {
            $result[] = $post;
        }
    }
    return $result;
}

// ── 조회수 ───────────────────────────────────────────────────
//   ★★ week15까지 posts.views 는 **한 번도 안 움직였다.** seed에 넣어둔 숫자가 굳어 있었고,
//     게시판의 '조회' 정렬 탭은 사실상 장식이었다. 이제 진짜로 센다.
//
//   [왜 이제야 되나]
//     조회수는 "이 사람이 이 글을 이미 봤나"를 서버가 기억해야 한다.
//     그 기억할 곳이 없어서 미뤄져 있던 것이다 — '최근 본 글'과 똑같은 사정.
//
//   [★ 왜 쿠키가 아니라 세션인가 — 이 주차의 판단 기준이 그대로 나오는 자리]
//     쿠키에 두면 값이 사용자 PC에 있다 → 지우고 새로고침을 반복해
//     **조회수를 얼마든지 부풀릴 수 있다.** 조회수는 랭킹·정렬에 쓰이므로
//     '틀리면 손해 보는 값'이다 → 서버가 들고 있어야 한다.
//
//   [★★ 그런데 세션도 답이 아니었다 — "시간을 아무도 정하지 않았다"]
//     처음엔 세션에 본 글 번호를 쌓았다: $_SESSION['viewed_posts'] = [12, 9, 3]
//     여기엔 **시각이 없다.** "몇 시간 뒤에 다시 셀 것인가"를 정할 방법 자체가 없었고,
//     대신 **세션 수명(유휴 20분)이 그 정책을 우연히 대신 정하고** 있었다.
//     게다가 세션 쿠키는 브라우저를 닫으면 사라진다 → **창을 껐다 켜면 다시 세어졌다.**
//     F5 연타는 막았는데 정작 부풀리기는 쉬웠던 것이다.
//     그리고 배열에 상한이 없어 글을 많이 볼수록 payload가 계속 무거워졌다
//     (세션은 요청마다 통째로 읽고 쓴다 — 글과 무관한 페이지까지 느려진다).
//
//   [실무는 어떻게 하나]
//     · Discourse       : topic_views 표, viewed_at이 **DATE**, 하루 1회.
//                         로그인은 user_id, 아니면 IP. 앞에 Redis를 문지기로 둔다.
//     · django-hitcount : 창이 **설정값**(기본 7일). 세션키+IP+UA로 판정.
//     공통점 셋: ①**창이 숫자로 코드에 있다** ②세션이 아니라 **표** ③판정 키가 **여러 단계**
//
//   [→ 그래서 post_views 표로 옮겼다]  (sql/migrations/012_post_views.sql)
//     ★ '주인이 누구인가'를 다시 물으니 답이 달랐다 —
//       조회 기록의 주인은 **'이 방문'이 아니라 '이 브라우저'** 였다. 세션 자리가 아니었던 것이다.

// 같은 사람으로 볼 판정 키.
//   ★ Discourse는 (로그인 → user_id / 아니면 IP)인데, 우리는 IP 대신 **device 쿠키**를 쓴다.
//     IP는 **공유된다** — 회사망·공용 와이파이면 여러 사람이 한 명으로 뭉뚱그려진다.
//     device 쿠키는 브라우저마다 하나라 그보다 정확하다.
//   ★ 새 식별자를 만들지 않았다 — 기기 목록에 쓰려고 이미 심어둔 쿠키를 그대로 쓴다.
//
//   [★★ IP를 대체 키로 두려다 뺐다 — 오히려 두 번 세어졌다]
//     처음엔 "쿠키가 없으면 IP로"를 넣었다. 그런데 신규 방문자는 이렇게 된다:
//       첫 요청  → 쿠키가 아직 없다  → 'i:<IP>'  로 1회
//       둘째 요청 → 쿠키가 생겼다    → 'd:<기기>' 로 **또 1회**
//     **한 사람이 두 번 세어진다.** 모든 신규 방문자에게 생기는 일이라 흔하기까지 하다.
//     → device_id()를 **그냥 부른다.** 없으면 만들어 심으면서 $_COOKIE에도 곧바로 채우므로,
//       **첫 요청부터 같은 키**가 나온다. 키가 하나면 두 번 세어질 일이 없다.
//
//   [그럼 쿠키를 안 받는 상대는?]
//     매 요청 새 번호가 생겨 계속 세어진다. 그게 원래 IP 대체 키가 막으려던 것인데 —
//     ★ **그 자리는 봇 판별(is_bot)이 이미 막는다.** 쿠키를 안 받는 상대의 대부분은 크롤러다.
//     남는 건 '쿠키를 막아둔 사람'인데 드물고, 그 사람은 로그인도 안 되는 상태다.
//     ※ 판정을 한 겹 줄이는 대신 **두 번 세는 버그를 없앴다.** 둘 중 후자가 훨씬 흔하다.
function viewer_key(): string {
    if (is_logged_in()) {
        return 'u:' . current_user_id();
    }

    // ★ 동의했으면 전용 번호로, 아니면 **접속지 지문으로** 판정한다.
    //   거절한 사람에게는 **아무것도 심지 않는다** — 대신 이미 오고 있는 값(IP)만 쓴다.
    //   ※ 그 대가로 같은 접속지를 쓰는 사람들이 한 명으로 묶인다. 거절의 대가는 정확도이지
    //     '판정을 아예 포기하는 것'이 아니다 — 포기하면 조회수를 마음대로 부풀릴 수 있다.
    $viewId = view_id();

    return $viewId !== null
        ? 'v:' . $viewId
        : 'i:' . hash('sha256', (string) ($_SERVER['REMOTE_ADDR'] ?? ''));
}

// 조회 기록을 며칠 보관할지.
//   ★ 처음엔 '판정에 필요한 만큼'만 두려고 이틀이었다. 그런데 이 표가 생기면서
//     **'최근 며칠간 몇 번 봤나'를 셀 수 있게 됐다** → '지금 뜨는 글'의 근거가 된다.
//     그래서 보관 기간이 곧 **"'지금'을 며칠로 볼 것인가"** 가 됐다.
//   ★ 판정(하루)과 집계(7일)의 기간이 다르다 — **쓰임이 다르면 기간도 다르다.**
const POST_VIEWS_KEEP_DAYS = 7;

// 최근 며칠간 이 글들이 몇 번 조회됐나. [글번호 => 횟수]
//   [★ 왜 필요한가 — '지금 뜨는 글'이 시간을 안 보고 있었다]
//     지금까지 '지금 뜨는 글'은 posts.views(누적)로 뽑았다. 그런데 그 값은
//     **글이 생긴 이후로 쌓이기만 한다** → 1년 전 글이 영원히 1등이고 오늘 글은 못 올라온다.
//     **이름은 '지금 뜨는'인데 동작은 '역대 많이 본'** 이었던 것이다.
//     ★ 조회수를 **세는 쪽**만 시간을 안 본 게 아니라 **쓰는 쪽**도 안 보고 있었다.
//
//   [왜 이제 가능한가]
//     post_views에 **날짜가 남기 때문**이다. 세션에 번호만 담던 시절엔 셀 수가 없었다.
//     → 같은 표가 '중복 방지'와 '최근 인기'를 동시에 떠받친다.
function recent_view_counts(int $days = POST_VIEWS_KEEP_DAYS): array {
    $stmt = db()->prepare(
        'SELECT post_id, COUNT(*) AS hits
           FROM post_views
          WHERE viewed_on >= ?
          GROUP BY post_id'
    );
    $stmt->execute([date('Y-m-d', strtotime('-' . $days . ' days'))]);

    $counts = [];
    foreach ($stmt->fetchAll() as $row) {
        $counts[(int) $row['post_id']] = (int) $row['hits'];
    }
    return $counts;
}

// '지금 뜨는 글' — 최근 조회가 많은 순.
//   ★ 최근 조회가 같거나 없으면 **누적 조회수로 갈린다.**
//     [왜 그 대비가 필요한가]
//       표가 방금 생겼으니 처음엔 기록이 거의 없다. 최근 조회만 보면 **화면이 텅 빈다.**
//       기존 글에도 누적 조회수는 있으므로, 그걸 뒷순위 기준으로 두면 자연스럽게 채워진다.
//       기록이 쌓일수록 앞 기준이 이기면서 **저절로 '최근'으로 옮겨간다.**
function trending_posts(array $posts, int $limit): array {
    $recent = recent_view_counts();

    // ★ 최근 조회수를 각 글에 붙여 둔다 — **화면이 그 숫자를 그대로 보여주기 위해서**다.
    //   [왜 중요한가]
    //     정렬은 '최근 조회'로 하는데 화면에는 '누적 조회'를 찍고 있었다. 그러면
    //     **조회 502짜리가 조회 1497짜리보다 위에** 뜬다 — 보는 사람은 순서를 이해할 수 없다.
    //   ★ 원칙 하나로 정리된다: **줄마다, 그 줄의 순서를 결정한 숫자를 보여준다.**
    foreach ($posts as &$post) {
        $post['recentViews'] = $recent[$post['id']] ?? 0;
    }
    unset($post);   // 참조로 돌린 뒤에는 반드시 끊는다 (안 끊으면 마지막 원소가 다음 루프에서 덮인다)

    usort($posts, function ($a, $b) {
        if ($a['recentViews'] !== $b['recentViews']) {
            return $b['recentViews'] - $a['recentViews'];   // 최근 조회 많은 순
        }
        return $b['views'] - $a['views'];                   // 같으면 누적으로 (초기 대비)
    });

    return array_slice($posts, 0, $limit);
}

// 이 접속지가 오늘 이 글을 이미 봤나?
//   ★ 쿠키가 없던 요청에서만 부른다. 그 외에는 device 쿠키가 더 정확하다.
function ip_viewed_today(int $postId, ?string $ipHash): bool {
    if ($ipHash === null) {
        return false;
    }
    $stmt = db()->prepare(
        'SELECT 1 FROM post_views WHERE post_id = ? AND ip_hash = ? AND viewed_on = ? LIMIT 1'
    );
    $stmt->execute([$postId, $ipHash, date('Y-m-d')]);

    return $stmt->fetchColumn() !== false;
}

// 이 글의 조회수를 1 올린다. 단, **오늘 이미 봤으면** 아무 일도 하지 않는다.
//   돌려주는 값 = 실제로 올렸는지. 화면이 이 값을 보고 방금 올린 1을 반영한다.
//
//   ★★ 판정과 기록을 **쿼리 한 방**으로 한다.
//     "조회해서 있는지 보고 → 없으면 넣기"로 나누면 그 사이에 다른 요청이 끼어들어
//     두 번 세어질 수 있다. ON DUPLICATE KEY UPDATE는 **DB가 한 번에 판단**하므로 그 틈이 없다.
//     (세션 write()에서 UPSERT를 쓴 것과 같은 이유다)
//
//   영향받은 행 수로 결과를 읽는다 — MariaDB의 약속이다:
//     1 = 새로 넣음(처음 봄)   2 = 갱신됨(날짜가 바뀜)   0 = 그대로(오늘 이미 봄)
function count_post_view(int $id): bool {
    // ★ 봇은 조회수에 넣지 않는다. 사람이 안 본 만큼 부풀려질 이유가 없다.
    //   (봇에게는 세션도 안 만든다 — includes/bot.php)
    if (is_bot()) {
        return false;
    }

    // ── 쿠키를 지우고 다시 온 상대 막기 ──────────────────────
    //   [왜 이게 필요한가 — 조회수가 첫 화면을 결정하기 때문]
    //     조회수는 표시만 되는 숫자가 아니다. 홈 '지금 뜨는 글'·게시판 정렬·랭킹을 움직인다.
    //     **부풀리면 첫 화면에 올라간다** → 부풀릴 이유가 실제로 있다.
    //     게다가 '지금 뜨는 글'을 최근 7일 기준으로 바꾸면서, 누적 5,000짜리 글을
    //     이기는 데 **열 번이면 충분**해졌다. 개선 하나가 다른 쪽 구멍을 키운 것이다.
    //
    //   ★ 검사 대상을 **쿠키가 없던 요청**으로 좁힌다.
    //     항상 IP로 판정하면(Discourse 방식) 같은 건물 사람들이 통째로 한 명이 된다.
    //     여기서는:
    //       · 쿠키 지우고 재방문   → 같은 IP가 오늘 이미 봄 → **막힌다**
    //       · 공유 IP의 다른 사람 → **자기가 처음 여는 글 하나만** 안 세어진다.
    //         그 뒤엔 쿠키가 생겨 정상 판정된다 (오탐의 범위가 좁다)
    //
    //   ★ 로그인 사용자는 건너뛴다 — 회원 번호로 이미 정확히 판정된다.
    //     ('필요 없는 개인정보는 안 모은다' — Discourse 소스에도 같은 주석이 있다)
    //   ★★ 순서가 중요하다: viewer_key()가 **쿠키를 심기 전에** 확인해야 한다.
    //     심고 나면 $_COOKIE에 값이 채워져서 '원래 있었는지'를 알 수 없게 된다.
    $loggedIn = is_logged_in();
    // ★ 조회 판정 전용 쿠키가 **원래 있었는지**를 본다. (기기 목록용 device 쿠키가 아니다 —
    //   그건 다른 목적이라 쪼갰다. includes/view_id.php)
    $hadCookie = isset($_COOKIE[VIEW_ID_COOKIE]);
    $ipHash   = $loggedIn ? null : hash('sha256', (string) ($_SERVER['REMOTE_ADDR'] ?? ''));

    // ★★ 판정 키를 **IP 검사보다 먼저** 구한다.
    //   [왜 — 실제로 밟은 함정]
    //     IP 검사에 걸려 여기서 return 하면 viewer_key()까지 가지 못하고,
    //     그러면 **판정 번호 쿠키를 심을 기회 자체가 없다.**
    //     한 번 IP로 막힌 사람은 영영 쿠키를 못 받아 계속 IP로만 판정된다 —
    //     공유 IP에서는 **하루 종일 아무 글도 안 세어지는** 상태가 된다.
    //   ★ $hadCookie를 그 위에서 이미 읽어뒀으므로 순서를 바꿔도 판정은 그대로다.
    $viewerKey = viewer_key();

    if (!$loggedIn && !$hadCookie && ip_viewed_today($id, $ipHash)) {
        return false;
    }

    // ★★ 날짜를 DB의 CURDATE()가 아니라 **PHP에서 계산해 넘긴다.**
    //   [왜 — 실제로 밟은 함정]
    //     DB는 UTC로, PHP는 Asia/Seoul로 돌아간다. CURDATE()를 쓰면 하루의 경계가
    //     **자정이 아니라 오전 9시**가 된다. 사용자 입장에서는:
    //       밤 11시에 본 글을 **새벽 1시에 다시 봐도 안 세어진다** (UTC로는 아직 같은 날)
    //     "하루에 한 번"이라고 정해놓고 정작 그 '하루'가 우리가 아는 하루가 아니었던 것이다.
    //   ★ 규칙은 하나다 — **시각 계산은 한쪽 세계 안에서만 한다.**
    //     여기서는 넣는 쪽·비교하는 쪽·지우는 쪽을 전부 PHP(한국 시각) 기준으로 맞춘다.
    //     (login_guard.php는 반대로 전부 DB 안에서 계산한다. 섞는 것만 아니면 어느 쪽이든 좋다)
    $today = date('Y-m-d');

    $stmt = db()->prepare(
        'INSERT INTO post_views (post_id, viewer_key, ip_hash, viewed_on) VALUES (?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE
             ip_hash   = VALUES(ip_hash),
             viewed_on = IF(viewed_on < ?, ?, viewed_on)'
    );
    $stmt->execute([$id, $viewerKey, $ipHash, $today, $today, $today]);

    if ($stmt->rowCount() === 0) {
        return false;                     // 오늘 이미 셌다
    }

    // views = views + 1 을 DB가 직접 계산하게 맡긴다.
    //   ★ PHP로 "읽어서 +1 해서 쓰기"를 하면, 두 사람이 동시에 열었을 때
    //     둘 다 320을 읽고 둘 다 321을 써서 한 번이 사라진다(경쟁 상태).
    //     DB 안에서 더하면 그런 일이 없다.
    db()->prepare('UPDATE posts SET views = views + 1 WHERE id = ?')->execute([$id]);

    // 지난 기록 청소. 판정에 쓰는 창(오늘)이 지나면 쓸모가 없다.
    //   ★ **새로 셌을 때만** 돈다 — 사람당 하루 한 번꼴이라 부담이 없다.
    //     (login_attempts를 실패할 때만 청소하는 것과 같은 방식)
    //   ★ 하루 여유를 둔다. 자정 근처에 경계에서 어긋나는 것을 피하려고.
    db()->prepare('DELETE FROM post_views WHERE viewed_on < ?')
        ->execute([date('Y-m-d', strtotime('-' . POST_VIEWS_KEEP_DAYS . ' days'))]);

    return true;
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
