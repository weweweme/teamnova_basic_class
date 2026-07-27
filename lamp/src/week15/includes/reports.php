<?php
// ============================================================
// reports.php — '신고(report)' 도메인 모듈
//   글 신고를 reports 표에 저장한다.
// ============================================================

require_once __DIR__ . '/db.php';

// 허용된 신고 사유 (화이트리스트). 폼과 DB ENUM과 이 목록이 일치해야 한다.
const ALLOWED_REASONS = ['스팸/광고', '욕설/비방', '스포일러', '기타'];

// ── 신고 저장 ───────────────────────────────────────────────
//   성공하면 true. 이미 같은 사람이 같은 글을 신고했으면(중복) false.
//   $reporterId: 신고한 회원 id (current_user_id())
function add_report(int $postId, int $reporterId, string $reason): bool {
    // 이미 신고했나? (reporter_id, post_id) UNIQUE라 중복이면 DB가 막지만,
    //   먼저 확인해 두면 '중복입니다' 같은 안내를 매끄럽게 줄 수 있다 (Tester-Doer).
    $already = db_scalar(
        'SELECT 1 FROM reports WHERE reporter_id = ? AND post_id = ?',
        [$reporterId, $postId]
    );
    if ($already !== false) {
        return false;                     // 이미 신고함 → 중복
    }

    $stmt = db()->prepare(
        'INSERT INTO reports (post_id, reporter_id, reason) VALUES (?, ?, ?)'
    );
    $stmt->execute([$postId, $reporterId, $reason]);
    return true;
}
