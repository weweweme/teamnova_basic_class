<?php
// ============================================================
// comments.php — '댓글(comment)' 도메인 모듈
//   데이터는 MariaDB의 comments 표에서 온다.
//   글 목록의 '댓글 N개'는 count_comments()로 실제 개수를 센다.
// ============================================================

require_once __DIR__ . '/db.php';

const COMMENT_MAX = 500;   // 댓글 최대 글자 수

// ── 특정 글의 댓글 목록 (원댓글 + 답글을 한 번에) ───────────
//   댓글은 author_id(번호)만 갖고 있으므로, users와 이어 닉네임(author)을 붙인다.
//   ★ 답글까지 포함해 '한 번의 쿼리'로 가져온다.
//     원댓글을 먼저 읽고 각각의 답글을 또 물어보면(= 댓글 20개면 쿼리 21번)
//     DB를 쓸데없이 스무 번 더 두드린다. 흔히 N+1 문제라 부르는 낭비다.
//
//   [정렬] ORDER BY COALESCE(parent_id, id), id
//     COALESCE(A, B) = "A가 있으면 A, 없으면 B". 여기선 '내가 속한 묶음의 번호'가 된다.
//       · 원댓글(5번)  → parent_id가 없으니 자기 번호 5
//       · 그 답글(9번) → parent_id가 5니까 5
//     → 둘 다 '5번 묶음'이 되어 나란히 붙는다. 그 안에서는 id 순(먼저 쓴 게 위).
//     답글은 부모보다 항상 나중에 생기니 번호가 더 커서, 부모가 묶음 맨 위에 온다.
//
//   [지워진 댓글] 지운 댓글도 '자리'는 남기고 "삭제된 댓글입니다"로 표시한다.
//     · 대화 맥락이 보존된다 — 답글이 달려 있었다면 고아가 되지 않는다.
//     · 수정에 '(수정됨)'을 붙인 것과 같은 이유다. 지우면 흔적까지 사라진다면,
//       말을 바꾸고 싶은 사람은 '수정' 대신 '삭제 후 재작성'을 하면 그만이라
//       (수정됨) 표시를 우회하는 구멍이 열린다.
//     · 소프트삭제(행은 남고 deleted_at만 찍힘)가 화면에도 그대로 드러난다.
//     ★ 단 내용과 작성자는 화면에 내보내지 않는다 — 자리만 남기는 것이 목적이므로.
//       내용은 SQL에서 빈 문자열로 바꾼다. 화면에서 안 그리는 것만으로는 부족하다:
//       HTML 소스에 남으면 개발자도구로 그대로 읽힌다.
function get_comments(int $postId): array {
    $sql = "
        SELECT c.id, c.post_id AS postId,
               c.parent_id AS parentId,   -- NULL이면 원댓글, 값이 있으면 그 번호 댓글의 답글
               u.username AS author,      -- 아이디(소유권 확인용)
               u.nickname AS authorNick,  -- 표시 이름(화면에 보이는 닉네임)
               -- 댓글 작성자의 총 글 수 (등급 배지용)
               (SELECT COUNT(*) FROM posts pc WHERE pc.author_id = c.author_id AND pc.deleted_at IS NULL) AS authorPostCount,
               -- 지워진 댓글은 내용을 아예 내보내지 않는다 (자리만 남기는 것이므로)
               CASE WHEN c.deleted_at IS NULL THEN c.content ELSE '' END AS content,
               c.edited_at AS editedAt,             -- 값이 있으면 화면에 '(수정됨)'
               c.deleted_at IS NOT NULL AS isDeleted -- 1이면 '삭제된 댓글입니다' 자리
        FROM comments c
        JOIN users u ON c.author_id = u.id
        WHERE c.post_id = ?   -- 지워진 것도 포함해 전부 가져온다 (자리를 남기므로)
        ORDER BY COALESCE(c.parent_id, c.id), c.id
    ";
    $stmt = db()->prepare($sql);
    $stmt->execute([$postId]);
    return $stmt->fetchAll();
}

// ── 특정 글의 댓글 개수 (목록의 '댓글 N' 표시) ──────────────
//   COUNT(*)로 DB가 직접 센다. (get_comments 세는 것보다 가볍다)
function count_comments(int $postId): int {
    return (int) db_scalar(
        'SELECT COUNT(*) FROM comments WHERE post_id = ? AND deleted_at IS NULL',
        [$postId]
    );
}

// ── 댓글 페이지 나누기 ──────────────────────────────────────
//   ★ 자르는 단위는 '줄'이다. 원댓글이든 답글이든 한 줄로 세어 20줄씩 자른다.
//     묶음(원댓글 + 그에 달린 답글 전부) 단위로 자르면 페이지 길이가 들쭉날쭉해진다.
//     답글 100개가 달린 원댓글 하나가 걸리면 그 페이지만 100줄이 넘게 길어진다.
//     👍 한 페이지 길이가 일정하다.
//     👎 부모 댓글이 1페이지 끝에 있고 그 답글은 2페이지 첫머리에 나와 대화가 끊긴다.
//     길이가 일정한 쪽을 택했다 — 끊기는 묶음은 페이지 경계에 걸린 하나뿐이지만,
//     스크롤 길이는 답글이 많은 글이면 매번 문제가 되기 때문이다.
const COMMENTS_PER_PAGE = 20;   // 한 페이지에 보여줄 댓글 줄 수 (답글도 한 줄로 센다)

// 총 댓글 페이지 수. 댓글이 하나도 없어도 '1페이지'다 (0페이지는 없으므로).
function count_comment_pages(array $comments): int {
    return max(1, (int) ceil(count($comments) / COMMENTS_PER_PAGE));
}

// 이 페이지에 보여줄 줄만 남긴다.
//   get_comments()가 이미 묶음 순서(원댓글 바로 밑에 그 답글)로 정렬해 주므로,
//   그 순서 그대로 앞에서부터 20줄씩 잘라내면 된다.
function paginate_comments(array $comments, int $page): array {
    $offset = ($page - 1) * COMMENTS_PER_PAGE;
    return array_slice($comments, $offset, COMMENTS_PER_PAGE);
}

// 줄 순번(그 글에서 몇 번째 줄인가) → 몇 페이지인가.
//   1~20번째 → 1페이지, 21~40번째 → 2페이지.
function comment_page_of(int $position): int {
    return max(1, (int) ceil($position / COMMENTS_PER_PAGE));
}

// 이 댓글이 보이는 페이지 번호를 DB에 물어본다.
//   댓글을 쓰거나 고친 뒤 '그 댓글이 실제로 보이는 페이지'로 돌려보내는 데 쓴다.
//   없는 댓글이면 1페이지.
function find_comment_page(int $commentId): int {
    // 이 댓글이 어느 글의 것이고, 어느 묶음에 속하는지(= 원댓글 번호)
    $stmt = db()->prepare(
        'SELECT post_id, COALESCE(parent_id, id) AS rootId FROM comments WHERE id = ?'
    );
    $stmt->execute([$commentId]);
    $row = $stmt->fetch();
    if ($row === false) {
        return 1;
    }

    // 이 댓글이 화면에서 몇 번째 줄인가 = 자기보다 앞에 오는 줄의 수 + 자기 자신.
    //   화면 순서는 get_comments()의 ORDER BY COALESCE(parent_id, id), id 다.
    //   그래서 '앞'의 기준도 두 단계다 — 먼저 묶음 번호, 같은 묶음이면 댓글 번호.
    //   ★ 지운 댓글도 화면에 자리가 남으므로 여기서도 함께 센다 (화면과 기준이 같아야 한다).
    $position = (int) db_scalar(
        'SELECT COUNT(*) FROM comments
          WHERE post_id = ?
            AND (COALESCE(parent_id, id) < ?
              OR (COALESCE(parent_id, id) = ? AND id <= ?))',
        [$row['post_id'], $row['rootId'], $row['rootId'], $commentId]
    );
    return comment_page_of($position);
}

// 주소(?cpage=)에 실을 값. 1페이지면 null → 주소에서 아예 빠진다.
//   '아무것도 없음'이 곧 1페이지라, 같은 화면에 주소가 두 개 생기지 않는다.
function comment_page_param(int $page): ?int {
    return $page > 1 ? $page : null;
}

// ── 댓글 하나 찾기 (소유권 확인용). 없으면 null. ────────────
function get_comment(int $id): ?array {
    $sql = "SELECT c.id, c.post_id AS postId, u.username AS author, c.content
            FROM comments c JOIN users u ON c.author_id = u.id
            WHERE c.id = ? AND c.deleted_at IS NULL";
    $stmt = db()->prepare($sql);
    $stmt->execute([$id]);
    $row = $stmt->fetch();
    return $row !== false ? $row : null;
}

// ── 댓글 저장 → 새 댓글 id 반환 ─────────────────────────────
//   $author(아이디)를 users.id로 바꿔서 INSERT (댓글도 작성자를 번호로 참조).
//   $parentId = 답글이면 부모 댓글 번호, 원댓글이면 null.
function add_comment(int $postId, string $author, string $content, ?int $parentId = null): int {
    $authorId = (int) db_scalar('SELECT id FROM users WHERE username = ?', [$author]);
    $stmt = db()->prepare(
        'INSERT INTO comments (post_id, author_id, parent_id, content) VALUES (?, ?, ?, ?)'
    );
    $stmt->execute([$postId, $authorId, $parentId, $content]);
    return (int) db()->lastInsertId();
}

// ── 답글의 '부모'를 정한다 (깊이 1단계 강제) ────────────────
//   폼에서 온 parent_id를 그대로 믿지 않고 여기서 한 번 거른다.
//   돌려주는 값이 그대로 comments.parent_id 에 들어간다.
//
//   ① 0 이하 / 없는 댓글 / 다른 글의 댓글 / 지워진 댓글 → null (= 그냥 원댓글로 단다)
//      ★ '다른 글의 댓글'을 막는 게 중요하다. 안 막으면 1번 글의 댓글이
//        2번 글에 달린 답글의 부모가 되어, 어느 글에서도 제대로 안 보이는 유령이 된다.
//   ② 부모로 지목된 것이 '이미 답글'이면 → 그 위의 원댓글을 부모로 삼는다.
//      그래서 답글에 답글을 달아도 깊이가 2단계를 넘지 않는다. (유튜브·인스타가 쓰는 방식)
function resolve_parent_id(int $parentId, int $postId): ?int {
    if ($parentId <= 0) {
        return null;   // 답글이 아니라 원댓글
    }

    $stmt = db()->prepare(
        'SELECT id, parent_id FROM comments
         WHERE id = ? AND post_id = ? AND deleted_at IS NULL'
    );
    $stmt->execute([$parentId, $postId]);
    $parent = $stmt->fetch();

    if ($parent === false) {
        return null;   // 없거나·남의 글 것이거나·지워진 댓글
    }

    // 부모가 이미 답글이면 그 부모(원댓글)에 붙인다
    if ($parent['parent_id'] !== null) {
        return (int) $parent['parent_id'];
    }
    return (int) $parent['id'];
}

// ── 댓글 수정 (내용 교체 + '고쳤다'는 사실 기록) ────────────
//   edited_at을 함께 찍는 이유: 화면에 '(수정됨)'을 띄우기 위해서다.
//   표시가 없으면 댓글을 단 뒤 몰래 말을 바꿔도 아무도 알 수 없다.
//   ★ 작성자·작성시각은 건드리지 않는다 — 고친 건 내용뿐이니까.
function update_comment(int $id, string $content): void {
    db()->prepare('UPDATE comments SET content = ?, edited_at = NOW() WHERE id = ?')
        ->execute([$content, $id]);
}

// ── 댓글 삭제 (소프트삭제: deleted_at 에 시각 기록) ──────────
//   글과 같은 방식 — 행은 남기고 '지운 시각'만 찍는다.
//   ★ 화면에는 "삭제된 댓글입니다"로 자리가 남고, 되돌리기 기능은 두지 않는다.
//     (글은 휴지통에서 30일간 되돌릴 수 있지만, 댓글은 한 줄짜리라 그럴 일이 드물다)
function delete_comment(int $id): void {
    db()->prepare('UPDATE comments SET deleted_at = NOW() WHERE id = ?')->execute([$id]);
}
