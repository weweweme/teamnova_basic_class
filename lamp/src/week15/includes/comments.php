<?php
// ============================================================
// comments.php — '댓글(comment)' 도메인 모듈
//   데이터는 MariaDB의 comments 표에서 온다.
//   글 목록의 '댓글 N개'는 count_comments()로 실제 개수를 센다.
// ============================================================

require_once __DIR__ . '/db.php';

const COMMENT_MAX = 500;   // 댓글 최대 글자 수

// ── 특정 글의 댓글 목록 (작성자 닉네임까지 JOIN으로) ────────
//   댓글은 author_id(번호)만 갖고 있으므로, users와 이어 닉네임(author)을 붙인다.
//   반환 모양은 week14와 동일: id·postId·author·content
function get_comments(int $postId): array {
    $sql = "
        SELECT c.id, c.post_id AS postId,
               u.username AS author,      -- 아이디(소유권 확인용)
               u.nickname AS authorNick,  -- 표시 이름(화면에 보이는 닉네임)
               -- 댓글 작성자의 총 글 수 (등급 배지용)
               (SELECT COUNT(*) FROM posts pc WHERE pc.author_id = c.author_id AND pc.deleted_at IS NULL) AS authorPostCount,
               c.content
        FROM comments c
        JOIN users u ON c.author_id = u.id
        WHERE c.post_id = ? AND c.deleted_at IS NULL   -- 이 글의, 안 지워진 댓글만
        ORDER BY c.id ASC                              -- 단 순서(먼저 쓴 게 위)
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
//   $author(닉네임)를 users.id로 바꿔서 INSERT (댓글도 작성자를 번호로 참조).
function add_comment(int $postId, string $author, string $content): int {
    $authorId = (int) db_scalar('SELECT id FROM users WHERE username = ?', [$author]);
    $stmt = db()->prepare(
        'INSERT INTO comments (post_id, author_id, content) VALUES (?, ?, ?)'
    );
    $stmt->execute([$postId, $authorId, $content]);
    return (int) db()->lastInsertId();
}

// ── 댓글 삭제 (소프트삭제: deleted_at 에 시각 기록) ──────────
//   글과 같은 방식 — 진짜 지우지 않아 되돌리기가 가능하다.
function delete_comment(int $id): void {
    db()->prepare('UPDATE comments SET deleted_at = NOW() WHERE id = ?')->execute([$id]);
}

// ── '지워진' 댓글 찾기 (되돌리기에서 주인 확인용). 없으면 null. ──
function get_deleted_comment(int $id): ?array {
    $sql = "SELECT c.id, c.post_id AS postId, u.username AS author
            FROM comments c JOIN users u ON c.author_id = u.id
            WHERE c.id = ? AND c.deleted_at IS NOT NULL";
    $stmt = db()->prepare($sql);
    $stmt->execute([$id]);
    $row = $stmt->fetch();
    return $row !== false ? $row : null;
}

// ── 댓글 삭제 되돌리기 (deleted_at 을 다시 NULL 로) ─────────
function restore_comment(int $id): void {
    db()->prepare('UPDATE comments SET deleted_at = NULL WHERE id = ?')->execute([$id]);
}
