<?php
// ============================================================
// ranking.php — 랭킹(리더보드) 집계 모듈
//   "누가/무엇이 제일 활발한가"를 여러 표를 JOIN해 센다.
//   ★ 작품 랭킹은 works 모듈(get_community_works), 글 랭킹은 posts 모듈을
//     그대로 재사용한다. 이 파일은 '유저 랭킹'처럼 새 집계가 필요한 것만 담당.
// ============================================================
require_once __DIR__ . '/db.php';   // db()

// ── 유저 랭킹 (명예의 전당): 작성 글 수 · 받은 추천 수 ──────
//   세 표를 잇는다:
//     users  (누구)                     u
//       └ posts  (그 사람이 쓴 글)        p   ← author_id 로 연결
//            └ likes (그 글이 받은 추천)   l   ← post_id 로 연결
//   ★ 한 유저의 글이 여러 개, 글마다 추천이 여러 개라 JOIN하면 줄이 불어난다.
//     그래서 글 수는 COUNT(DISTINCT p.id)(중복 제거), 추천은 COUNT(l.post_id)(빈칸 무시)로 센다.
//   LEFT JOIN likes = 추천 0개인 글도 유저는 남긴다(추천 없다고 유저가 사라지면 안 됨).
function rank_users(int $limit): array {
    $sql = "
        SELECT u.id, u.username, u.nickname, u.avatar,
               COUNT(DISTINCT p.id) AS postCount,      -- 쓴 글 수 (JOIN으로 불어난 줄을 중복 제거)
               COUNT(l.post_id)     AS likesReceived   -- 그 글들이 받은 추천 총합 (빈칸 NULL은 안 셈)
        FROM users u
        JOIN posts p      ON p.author_id = u.id AND p.deleted_at IS NULL   -- 글 있는 유저만
        LEFT JOIN likes l ON l.post_id   = p.id                            -- 추천 0개여도 유지
        GROUP BY u.id, u.username, u.nickname, u.avatar
        ORDER BY likesReceived DESC, postCount DESC   -- 추천 많은 순, 같으면 글 많은 순
        LIMIT " . (int) $limit . "
    ";
    return db()->query($sql)->fetchAll();
}
