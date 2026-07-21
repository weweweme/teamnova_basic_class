<?php
// ============================================================
// comments.php — '댓글(comment)' 도메인 모듈
//   posts.php와 같은 방식: 코드에 박힌 더미 + 세션 임시 보관함을 합쳐서 돌려준다.
//   나중 DB를 붙이면 이 파일 속만 SQL로 바꾸면 된다.
// ============================================================

const COMMENT_MAX = 500;   // 댓글 최대 글자 수

// 처음부터 있는 더미 댓글 (postId = 어느 글에 달린 댓글인지)
function base_comments(): array {
    return [
        ['id' => 101, 'postId' => 1, 'author' => '리뷰러', 'content' => '저도 그렇게 봤어요.'],
        ['id' => 102, 'postId' => 1, 'author' => '영화광', 'content' => '공감합니다. 저도 그 장면이 인상 깊었어요.'],
        ['id' => 103, 'postId' => 3, 'author' => '해석러', 'content' => '후반부는 저도 조금 아쉬웠어요.'],
    ];
}

// 특정 글의 댓글 목록 (더미 + 이번 접속에서 쓴 것 − 지운 것)
function get_comments(int $postId): array {
    $all = base_comments();
    foreach ($_SESSION['new_comments'] ?? [] as $c) {
        $all[] = $c;
    }
    $deleted = $_SESSION['deleted_comments'] ?? [];

    $result = [];
    foreach ($all as $c) {
        if ($c['postId'] !== $postId) {
            continue;                                   // 다른 글의 댓글은 제외
        }
        if (in_array($c['id'], $deleted, true)) {
            continue;                                   // 지운 댓글은 제외
        }
        $result[] = $c;
    }
    return $result;
}

// 댓글 하나 찾기 (소유권 확인용). 없으면 null.
function get_comment(int $id): ?array {
    $all = base_comments();
    foreach ($_SESSION['new_comments'] ?? [] as $c) {
        $all[] = $c;
    }
    foreach ($all as $c) {
        if ($c['id'] === $id) {
            return $c;
        }
    }
    return null;
}

// 다음 댓글 번호
function next_comment_id(): int {
    $max = 0;
    foreach (base_comments() as $c) {
        $max = max($max, $c['id']);
    }
    foreach ($_SESSION['new_comments'] ?? [] as $c) {
        $max = max($max, $c['id']);
    }
    return $max + 1;
}

// 댓글 저장  (나중: INSERT INTO comments …)
function add_comment(int $postId, string $author, string $content): int {
    $id = next_comment_id();
    $_SESSION['new_comments'][] = [
        'id' => $id, 'postId' => $postId, 'author' => $author, 'content' => $content,
    ];
    return $id;
}

// 댓글 삭제 기록  (나중: DELETE FROM comments WHERE id = ?)
function delete_comment(int $id): void {
    $_SESSION['deleted_comments'][] = $id;
}
