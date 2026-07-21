<?php
// ============================================================
// comments.php — '댓글(comment)' 도메인 모듈
//   posts.php와 같은 방식: 코드에 박힌 더미 + 세션 임시 보관함을 합쳐서 돌려준다.
//   나중 DB를 붙이면 이 파일 속만 SQL로 바꾸면 된다.
//
//   ★ 글 목록에 보이는 '댓글 N개'는 여기 있는 댓글을 '세어서' 만든다.
//     (예전엔 글에 숫자를 따로 박아뒀더니 실제 댓글 수와 어긋났음 → 단일 출처로 통일)
// ============================================================

const COMMENT_MAX = 500;   // 댓글 최대 글자 수

// 처음부터 있는 더미 댓글 (postId = 어느 글에 달린 댓글인지)
function base_comments(): array {
    return [
        // 글 1 (계단 연출) — 3개
        ['id'=>101,'postId'=>1,'author'=>'리뷰러','content'=>'저도 그 계단 장면에서 소름 돋았어요.'],
        ['id'=>102,'postId'=>1,'author'=>'영화광','content'=>'두 번째 볼 때 훨씬 잘 보이더라고요.'],
        ['id'=>103,'postId'=>1,'author'=>'주말영화','content'=>'감독 인터뷰 보니 의도한 연출이 맞더군요.'],
        // 글 2 (결말 해석) — 1개
        ['id'=>104,'postId'=>2,'author'=>'해석러','content'=>'저는 현실 쪽에 한 표입니다.'],
        // 글 3 (후반부 과했다) — 5개
        ['id'=>105,'postId'=>3,'author'=>'영화광','content'=>'전반부가 워낙 좋아서 더 그렇게 느껴진 듯해요.'],
        ['id'=>106,'postId'=>3,'author'=>'리뷰러','content'=>'저는 후반부도 좋았어요.'],
        ['id'=>107,'postId'=>3,'author'=>'삐딱이','content'=>'동의합니다. 마무리가 급했어요.'],
        ['id'=>108,'postId'=>3,'author'=>'주말영화','content'=>'그 장면은 상징으로 보면 이해가 됩니다.'],
        ['id'=>109,'postId'=>3,'author'=>'정주행러','content'=>'의견 갈리는 게 이 영화의 매력인 듯.'],
        // 글 4 (음악) — 2개
        ['id'=>110,'postId'=>4,'author'=>'리뷰러','content'=>'OST 진짜 좋죠. 저도 자주 듣습니다.'],
        ['id'=>111,'postId'=>4,'author'=>'질문러','content'=>'어느 곡이 제일 좋으셨나요?'],
        // 글 6 (오징어 1화) — 4개
        ['id'=>112,'postId'=>6,'author'=>'영화광','content'=>'첫 게임 연출이 정말 강렬했어요.'],
        ['id'=>113,'postId'=>6,'author'=>'주말영화','content'=>'저도 밤새 봤습니다 ㅋㅋ'],
        ['id'=>114,'postId'=>6,'author'=>'해석러','content'=>'색감 대비가 인상적이었어요.'],
        ['id'=>115,'postId'=>6,'author'=>'정보통','content'=>'해외에서도 이 장면 반응이 제일 좋았대요.'],
        // 글 7 (후반 전개) — 2개
        ['id'=>116,'postId'=>7,'author'=>'정주행러','content'=>'저는 그래도 끝까지 재밌게 봤어요.'],
        ['id'=>117,'postId'=>7,'author'=>'리뷰러','content'=>'캐릭터를 좀 더 살렸으면 좋았을 텐데요.'],
        // 글 8 (해외 반응) — 6개
        ['id'=>118,'postId'=>8,'author'=>'영화광','content'=>'정리 감사합니다!'],
        ['id'=>119,'postId'=>8,'author'=>'질문러','content'=>'출처도 같이 올려주실 수 있나요?'],
        ['id'=>120,'postId'=>8,'author'=>'주말영화','content'=>'해외 반응이 이렇게 뜨거운 줄 몰랐네요.'],
        ['id'=>121,'postId'=>8,'author'=>'삐딱이','content'=>'과장된 기사도 섞여 있는 것 같아요.'],
        ['id'=>122,'postId'=>8,'author'=>'해석러','content'=>'번역 뉘앙스 차이도 있을 듯합니다.'],
        ['id'=>123,'postId'=>8,'author'=>'정주행러','content'=>'잘 봤습니다. 다음 편도 부탁드려요.'],
        // 글 9 (도킹 장면) — 1개
        ['id'=>124,'postId'=>9,'author'=>'리뷰류','content'=>'아이맥스로 다시 보고 싶네요.'],
        // 글 5, 10 — 댓글 없음 (빈 상태도 보여주기 위해)
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

// 특정 글의 댓글 '개수' (목록 화면의 "댓글 N" 표시에 사용)
function count_comments(int $postId): int {
    return count(get_comments($postId));
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
