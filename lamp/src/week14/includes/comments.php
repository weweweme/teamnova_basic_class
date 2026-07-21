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
        // ── 기생충 ──
        ['id'=>101,'postId'=>1,'author'=>'리뷰러','content'=>'저도 그 계단 장면에서 소름 돋았어요.'],
        ['id'=>102,'postId'=>1,'author'=>'영화광','content'=>'두 번째 볼 때 훨씬 잘 보이더라고요.'],
        ['id'=>103,'postId'=>1,'author'=>'주말영화','content'=>'감독 인터뷰 보니 의도한 연출이 맞더군요.'],
        ['id'=>104,'postId'=>1,'author'=>'팝콘각','content'=>'계단 개수까지 세어봤다는 글도 봤습니다 ㅋㅋ'],
        ['id'=>105,'postId'=>2,'author'=>'해석러','content'=>'저는 현실 쪽에 한 표입니다.'],
        ['id'=>106,'postId'=>2,'author'=>'심야극장','content'=>'상상이라고 봐야 마지막 컷이 설명되더라고요.'],
        ['id'=>107,'postId'=>3,'author'=>'영화광','content'=>'전반부가 워낙 좋아서 더 그렇게 느껴진 듯해요.'],
        ['id'=>108,'postId'=>3,'author'=>'리뷰러','content'=>'저는 후반부도 좋았어요.'],
        ['id'=>109,'postId'=>3,'author'=>'삐딱이','content'=>'동의합니다. 마무리가 급했어요.'],
        ['id'=>110,'postId'=>3,'author'=>'주말영화','content'=>'그 장면은 상징으로 보면 이해가 됩니다.'],
        ['id'=>111,'postId'=>3,'author'=>'정주행러','content'=>'의견 갈리는 게 이 영화의 매력인 듯.'],
        ['id'=>112,'postId'=>4,'author'=>'리뷰러','content'=>'OST 진짜 좋죠. 저도 자주 듣습니다.'],
        ['id'=>113,'postId'=>4,'author'=>'질문러','content'=>'어느 곡이 제일 좋으셨나요?'],
        ['id'=>114,'postId'=>5,'author'=>'정주행러','content'=>'기대치가 너무 올라간 상태로 보면 그럴 수 있죠.'],
        ['id'=>115,'postId'=>5,'author'=>'해석러','content'=>'저는 두 번째 볼 때 평가가 올라갔습니다.'],
        ['id'=>116,'postId'=>5,'author'=>'팝콘각','content'=>'취향 차이라고 봅니다.'],
        ['id'=>117,'postId'=>6,'author'=>'영화광','content'=>'저도 그 장면에서 진짜 놀랐어요.'],
        ['id'=>118,'postId'=>6,'author'=>'자막러','content'=>'아무 정보 없이 보는 게 정답인 영화죠.'],
        ['id'=>119,'postId'=>6,'author'=>'리뷰러','content'=>'그 순간 극장 분위기가 확 바뀌더군요.'],
        ['id'=>120,'postId'=>7,'author'=>'심야극장','content'=>'그 소재를 이렇게 끝까지 밀고 간 게 대단합니다.'],
        // ── 오징어 게임 ──
        ['id'=>121,'postId'=>8,'author'=>'영화광','content'=>'첫 게임 연출이 정말 강렬했어요.'],
        ['id'=>122,'postId'=>8,'author'=>'주말영화','content'=>'저도 밤새 봤습니다 ㅋㅋ'],
        ['id'=>123,'postId'=>8,'author'=>'해석러','content'=>'색감 대비가 인상적이었어요.'],
        ['id'=>124,'postId'=>8,'author'=>'정보통','content'=>'해외에서도 이 장면 반응이 제일 좋았대요.'],
        ['id'=>125,'postId'=>9,'author'=>'팝콘각','content'=>'저는 별 모양에서 포기했습니다.'],
        ['id'=>126,'postId'=>9,'author'=>'리뷰러','content'=>'바늘 달구는 게 핵심이라던데요.'],
        ['id'=>127,'postId'=>9,'author'=>'질문러','content'=>'설탕 비율 어떻게 하셨나요?'],
        ['id'=>128,'postId'=>9,'author'=>'주말영화','content'=>'사진 보고 웃었습니다 ㅋㅋㅋ'],
        ['id'=>129,'postId'=>9,'author'=>'자막러','content'=>'저도 주말에 도전해보려고요.'],
        ['id'=>130,'postId'=>9,'author'=>'정주행러','content'=>'우산은 진짜 사기 맞습니다.'],
        ['id'=>131,'postId'=>10,'author'=>'정주행러','content'=>'저는 그래도 끝까지 재밌게 봤어요.'],
        ['id'=>132,'postId'=>10,'author'=>'리뷰러','content'=>'캐릭터를 좀 더 살렸으면 좋았을 텐데요.'],
        ['id'=>133,'postId'=>11,'author'=>'영화광','content'=>'정리 감사합니다!'],
        ['id'=>134,'postId'=>11,'author'=>'질문러','content'=>'출처도 같이 올려주실 수 있나요?'],
        ['id'=>135,'postId'=>11,'author'=>'삐딱이','content'=>'과장된 기사도 섞여 있는 것 같아요.'],
        ['id'=>136,'postId'=>12,'author'=>'심야극장','content'=>'분량 문제였을 수도 있겠네요.'],
        ['id'=>137,'postId'=>13,'author'=>'팝콘각','content'=>'그 계단 사진 배경화면으로 쓰고 있습니다.'],
        ['id'=>138,'postId'=>13,'author'=>'해석러','content'=>'색을 일부러 유치하게 쓴 게 대비 효과인 듯해요.'],
        // ── 인터스텔라 ──
        ['id'=>139,'postId'=>14,'author'=>'리뷰러','content'=>'아이맥스로 다시 보고 싶네요.'],
        ['id'=>140,'postId'=>14,'author'=>'영화광','content'=>'그 장면 음악이 진짜 심장을 때립니다.'],
        ['id'=>141,'postId'=>14,'author'=>'자막러','content'=>'재개봉 소식 있으면 공유해주세요.'],
        ['id'=>142,'postId'=>15,'author'=>'정보통','content'=>'물리학자가 자문에 참여했다고 합니다.'],
        ['id'=>143,'postId'=>16,'author'=>'질문러','content'=>'이 설명 보고 드디어 이해했습니다. 감사합니다!'],
        ['id'=>144,'postId'=>16,'author'=>'영화광','content'=>'저장해뒀다가 다시 볼 때 참고할게요.'],
        ['id'=>145,'postId'=>16,'author'=>'삐딱이','content'=>'그래도 마지막 부분은 여전히 억지스럽습니다.'],
        ['id'=>146,'postId'=>16,'author'=>'주말영화','content'=>'정리 깔끔하네요. 잘 봤습니다.'],
        ['id'=>147,'postId'=>16,'author'=>'심야극장','content'=>'시간 얘기는 몇 번을 들어도 어렵네요.'],
        ['id'=>148,'postId'=>17,'author'=>'리뷰러','content'=>'저도 그 감정선 때문에 기억에 남습니다.'],
        ['id'=>149,'postId'=>17,'author'=>'쓴소리','content'=>'후반은 좀 과했다고 생각해요.'],
        ['id'=>150,'postId'=>18,'author'=>'해석러','content'=>'상징으로 보면 나쁘지 않았습니다.'],
        ['id'=>151,'postId'=>18,'author'=>'팝콘각','content'=>'저도 그 부분에서 몰입이 살짝 깨졌어요.'],
        // ── 올드보이 ──
        ['id'=>152,'postId'=>19,'author'=>'영화광','content'=>'한 번에 찍었다는 게 아직도 안 믿깁니다.'],
        ['id'=>153,'postId'=>19,'author'=>'주말영화','content'=>'많은 작품이 이 장면을 따라 했죠.'],
        ['id'=>154,'postId'=>19,'author'=>'자막러','content'=>'지치는 연기가 진짜라서 더 무섭습니다.'],
        ['id'=>155,'postId'=>19,'author'=>'정보통','content'=>'촬영에 며칠 걸렸다는 기록이 있더군요.'],
        ['id'=>156,'postId'=>20,'author'=>'리뷰러','content'=>'색보정이 요즘 감성이랑도 잘 맞아요.'],
        ['id'=>157,'postId'=>20,'author'=>'심야극장','content'=>'재개봉 때 극장에서 봤는데 좋았습니다.'],
        ['id'=>158,'postId'=>21,'author'=>'삐딱이','content'=>'저도 한 번 보고 다시는 못 보겠더라고요.'],
        // ── 살인의 추억 ──
        ['id'=>159,'postId'=>23,'author'=>'영화광','content'=>'그 마지막 컷 진짜 잊히지 않습니다.'],
        ['id'=>160,'postId'=>23,'author'=>'리뷰러','content'=>'관객을 향한 질문이라는 해석 좋네요.'],
        ['id'=>161,'postId'=>23,'author'=>'자막러','content'=>'극장에서 봤을 때 아무도 안 일어났어요.'],
        ['id'=>162,'postId'=>24,'author'=>'질문러','content'=>'사건 배경 정리된 글 있을까요?'],
        ['id'=>163,'postId'=>24,'author'=>'정보통','content'=>'다음 글에서 정리해보겠습니다.'],
        ['id'=>164,'postId'=>25,'author'=>'해석러','content'=>'두 사람이 뒤바뀌는 흐름이 핵심이죠.'],
        // ── 인셉션 ──
        ['id'=>165,'postId'=>27,'author'=>'해석러','content'=>'층마다 시간이 다르게 흐른다는 걸 잡으면 쉬워집니다.'],
        ['id'=>166,'postId'=>27,'author'=>'영화광','content'=>'저도 세 번째 보고 겨우 정리했어요.'],
        ['id'=>167,'postId'=>27,'author'=>'주말영화','content'=>'도표로 그려보면 확실히 이해됩니다.'],
        ['id'=>168,'postId'=>27,'author'=>'팝콘각','content'=>'저만 헷갈리는 게 아니었군요 ㅋㅋ'],
        ['id'=>169,'postId'=>28,'author'=>'질문러','content'=>'오 그 관점은 생각 못 했네요.'],
        ['id'=>170,'postId'=>28,'author'=>'리뷰러','content'=>'감독도 비슷한 얘기를 했다고 합니다.'],
        ['id'=>171,'postId'=>28,'author'=>'삐딱이','content'=>'그래도 열린 결말인 건 변함없죠.'],
        ['id'=>172,'postId'=>28,'author'=>'심야극장','content'=>'이 글 보고 다시 봤습니다. 다르게 보이네요.'],
        ['id'=>173,'postId'=>28,'author'=>'자막러','content'=>'명쾌한 정리 감사합니다.'],
        ['id'=>174,'postId'=>28,'author'=>'정주행러','content'=>'마지막 소리로 판단하는 사람도 있더라고요.'],
        ['id'=>175,'postId'=>29,'author'=>'팝콘각','content'=>'CG인 줄 알았는데 실제라니 놀랍네요.'],
        ['id'=>176,'postId'=>29,'author'=>'영화광','content'=>'메이킹 영상 꼭 보세요. 진짜 대단합니다.'],
        ['id'=>177,'postId'=>30,'author'=>'해석러','content'=>'저는 그 정도 설명은 필요했다고 봅니다.'],
        ['id'=>178,'postId'=>31,'author'=>'리뷰러','content'=>'그 곡 하나로 영화 분위기가 완성되죠.'],
        ['id'=>179,'postId'=>31,'author'=>'주말영화','content'=>'사운드트랙 자주 듣습니다.'],
        // ── 라라랜드 ──
        ['id'=>180,'postId'=>32,'author'=>'자막러','content'=>'실제로 이틀 동안 통제하고 찍었다고 하네요.'],
        ['id'=>181,'postId'=>32,'author'=>'영화광','content'=>'그 오프닝 때문에 바로 빠져들었습니다.'],
        ['id'=>182,'postId'=>32,'author'=>'질문러','content'=>'롱테이크로 보이는데 편집점이 있나요?'],
        ['id'=>183,'postId'=>33,'author'=>'심야극장','content'=>'저도 그 결말이라서 더 좋았어요.'],
        ['id'=>184,'postId'=>33,'author'=>'리뷰러','content'=>'마지막 눈빛 교환이 전부를 말하죠.'],
        ['id'=>185,'postId'=>33,'author'=>'삐딱이','content'=>'저는 그 연출이 좀 반칙 같았습니다.'],
        ['id'=>186,'postId'=>33,'author'=>'팝콘각','content'=>'며칠 앓았다는 말 완전 공감합니다.'],
        ['id'=>187,'postId'=>34,'author'=>'주말영화','content'=>'뮤지컬 안 좋아해도 이건 볼만합니다.'],
        ['id'=>188,'postId'=>34,'author'=>'정주행러','content'=>'노래보다 이야기 비중이 커서 괜찮아요.'],
        // ── 미스터 션샤인 / 시그널 / 나의 아저씨 ──
        ['id'=>189,'postId'=>36,'author'=>'자막러','content'=>'조명이 특히 좋았습니다.'],
        ['id'=>190,'postId'=>36,'author'=>'영화광','content'=>'배경음악도 잘 어울렸어요.'],
        ['id'=>191,'postId'=>37,'author'=>'해석러','content'=>'창작 부분은 창작으로 보면 될 듯합니다.'],
        ['id'=>192,'postId'=>37,'author'=>'질문러','content'=>'참고 자료 링크도 부탁드려요.'],
        ['id'=>193,'postId'=>39,'author'=>'영화광','content'=>'규칙을 안 어기는 게 정말 어려운 건데요.'],
        ['id'=>194,'postId'=>39,'author'=>'정보통','content'=>'각본이 촘촘하기로 유명하죠.'],
        ['id'=>195,'postId'=>39,'author'=>'리뷰러','content'=>'마지막 화 보고 한참 멍했습니다.'],
        ['id'=>196,'postId'=>40,'author'=>'정주행러','content'=>'저도 계속 기다리는 중입니다.'],
        ['id'=>197,'postId'=>42,'author'=>'자막러','content'=>'저도 제목 때문에 미뤘던 사람입니다.'],
        ['id'=>198,'postId'=>42,'author'=>'영화광','content'=>'4화 넘어가면서 완전히 달라지죠.'],
        ['id'=>199,'postId'=>43,'author'=>'심야극장','content'=>'대사 모아둔 글도 찾아봤어요.'],
        // 글 22·26·35·38·41·44 — 댓글 없음 (빈 상태도 보여주기 위해)
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
