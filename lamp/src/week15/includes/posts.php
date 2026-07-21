<?php
// ============================================================
// posts.php — '글(post)' 데이터 + 관련 동작을 모아둔 도메인 모듈
//
// ★ 지금은 DB가 없어서 두 곳에서 데이터를 가져온다:
//     ① base_posts()  — 처음부터 있는 더미 글 (코드에 박아둠)
//     ② $_SESSION     — 이번 접속에서 사용자가 새로 쓰거나 고친 것 (임시 보관함)
//   get_posts()가 이 둘을 '합쳐서' 돌려주므로, 바깥 페이지는 DB가 있는 것처럼 쓸 수 있다.
//   → 나중에 MariaDB를 붙이면 이 파일 '속'만 SQL로 바꾸면 되고, 페이지들은 그대로다.
//   ※ 세션이라 브라우저를 닫거나 로그아웃하면 초기화된다 (시연/연습용).
// ============================================================

//   댓글 수는 comments 모듈에서 '실제 댓글을 세어서' 채운다 (숫자만 따로 박아두면 실물과 어긋남).
require_once __DIR__ . '/comments.php';

// ── 입력 길이 제한 (매직값 금지 — 이름 붙인 상수로) ──────────
const POST_TITLE_MAX   = 100;
const POST_CONTENT_MAX = 5000;
const SEARCH_QUERY_MAX = 50;

// ── ① 처음부터 있는 더미 글 (코드에 박힌 원본) ───────────────
function base_posts(): array {
    // created = 클수록 최신 (최신순 정렬에 쓰는 값). id가 작을수록 최신이 되도록 45 - id 로 맞춤.
    // comments 칸은 여기 없다 — 실제 댓글을 세어서 get_posts()가 채운다.
    return [
        // ── 기생충 (7개) ──
        ['id'=>1,'work'=>'parasite','workTitle'=>'기생충','title'=>'계단 연출이 진짜 미쳤다','author'=>'영화광','sentiment'=>'호평','views'=>320,'likes'=>45,'created'=>44,'content'=>"위아래로 오르내리는 계단이 계급을 그대로 보여주더라고요.\n두 번째 보니 더 잘 보입니다."],
        ['id'=>2,'work'=>'parasite','workTitle'=>'기생충','title'=>'결말 해석 정리해봤어요','author'=>'해석러','sentiment'=>'보통','views'=>210,'likes'=>12,'created'=>43,'content'=>"마지막 장면이 현실인지 상상인지 의견이 갈리는데,\n저는 상상 쪽에 가깝다고 봅니다."],
        ['id'=>3,'work'=>'parasite','workTitle'=>'기생충','title'=>'후반부는 좀 과했다고 봅니다','author'=>'쓴소리','sentiment'=>'혹평','views'=>540,'likes'=>30,'created'=>42,'content'=>"전반부 긴장감은 최고였는데 후반이 급하게 느껴졌어요."],
        ['id'=>4,'work'=>'parasite','workTitle'=>'기생충','title'=>'음악이 이렇게 좋았나요','author'=>'영화광','sentiment'=>'호평','views'=>150,'likes'=>8,'created'=>41,'content'=>"OST 따로 듣는데 장면이 계속 떠오릅니다."],
        ['id'=>5,'work'=>'parasite','workTitle'=>'기생충','title'=>'솔직히 과대평가 아닌가요','author'=>'삐딱이','sentiment'=>'혹평','views'=>420,'likes'=>22,'created'=>40,'content'=>"잘 만든 건 맞는데 이 정도 열광은 좀 과한 듯."],
        ['id'=>6,'work'=>'parasite','workTitle'=>'기생충','title'=>'지하실 반전 처음 봤을 때','author'=>'팝콘각','sentiment'=>'호평','views'=>275,'likes'=>33,'created'=>39,'content'=>"아무 정보 없이 봐서 그 장면에서 진짜 소리 질렀습니다."],
        ['id'=>7,'work'=>'parasite','workTitle'=>'기생충','title'=>'냄새라는 소재가 핵심인 듯','author'=>'해석러','sentiment'=>'호평','views'=>198,'likes'=>19,'created'=>38,'content'=>"돈으로도 못 지우는 게 냄새라는 설정이 잔인하면서 정확합니다."],
        // ── 오징어 게임 (6개) ──
        ['id'=>8,'work'=>'squidgame','workTitle'=>'오징어 게임','title'=>'1화부터 몰입도 미쳤음','author'=>'정주행러','sentiment'=>'호평','views'=>380,'likes'=>51,'created'=>37,'content'=>"밤새 다 봤습니다. 첫 게임 장면이 압권."],
        ['id'=>9,'work'=>'squidgame','workTitle'=>'오징어 게임','title'=>'달고나 실제로 해봤습니다','author'=>'영화광','sentiment'=>'호평','views'=>610,'likes'=>88,'created'=>36,'content'=>"우산 모양은 진짜 불가능에 가깝네요. 세 번 실패했습니다."],
        ['id'=>10,'work'=>'squidgame','workTitle'=>'오징어 게임','title'=>'후반 전개가 아쉬워요','author'=>'쓴소리','sentiment'=>'혹평','views'=>260,'likes'=>17,'created'=>35,'content'=>"캐릭터 소모가 좀 심했다고 느꼈습니다."],
        ['id'=>11,'work'=>'squidgame','workTitle'=>'오징어 게임','title'=>'해외 반응 모아봤습니다','author'=>'정보통','sentiment'=>'보통','views'=>90,'likes'=>5,'created'=>34,'content'=>"관련 리뷰들 정리해서 공유합니다."],
        ['id'=>12,'work'=>'squidgame','workTitle'=>'오징어 게임','title'=>'캐릭터 서사가 아까웠다','author'=>'삐딱이','sentiment'=>'혹평','views'=>233,'likes'=>14,'created'=>33,'content'=>"몇몇 인물은 조금만 더 풀어줬으면 훨씬 좋았을 텐데요."],
        ['id'=>13,'work'=>'squidgame','workTitle'=>'오징어 게임','title'=>'미술·세트가 진짜 대단함','author'=>'주말영화','sentiment'=>'호평','views'=>187,'likes'=>26,'created'=>32,'content'=>"계단 미로 색감은 보자마자 기억에 박히더라고요."],
        // ── 인터스텔라 (5개) ──
        ['id'=>14,'work'=>'interstellar','workTitle'=>'인터스텔라','title'=>'도킹 장면은 극장에서 봐야 함','author'=>'심야극장','sentiment'=>'호평','views'=>300,'likes'=>26,'created'=>31,'content'=>"음향까지 합쳐지니 완전히 다른 경험이었어요."],
        ['id'=>15,'work'=>'interstellar','workTitle'=>'인터스텔라','title'=>'과학 고증 어디까지 맞나요','author'=>'영화광','sentiment'=>'보통','views'=>120,'likes'=>9,'created'=>30,'content'=>"블랙홀 묘사가 실제 이론과 얼마나 가까운지 궁금합니다."],
        ['id'=>16,'work'=>'interstellar','workTitle'=>'인터스텔라','title'=>'시간 개념 정리해봤습니다','author'=>'해석러','sentiment'=>'호평','views'=>445,'likes'=>62,'created'=>29,'content'=>"중력이 셀수록 시간이 느리게 간다는 것만 잡으면 나머지는 따라옵니다."],
        ['id'=>17,'work'=>'interstellar','workTitle'=>'인터스텔라','title'=>'감정선이 과하다는 의견에 대해','author'=>'리뷰러','sentiment'=>'보통','views'=>156,'likes'=>11,'created'=>28,'content'=>"저는 그 감정선이 있어서 과학 얘기가 남는다고 봅니다."],
        ['id'=>18,'work'=>'interstellar','workTitle'=>'인터스텔라','title'=>'후반 5차원 공간은 좀...','author'=>'삐딱이','sentiment'=>'혹평','views'=>289,'likes'=>20,'created'=>27,'content'=>"거기서부터는 설득이 아니라 우겨넣기처럼 느껴졌어요."],
        // ── 올드보이 (4개) ──
        ['id'=>19,'work'=>'oldboy','workTitle'=>'올드보이','title'=>'복도 액션이 아직도 회자되는 이유','author'=>'심야극장','sentiment'=>'호평','views'=>512,'likes'=>71,'created'=>26,'content'=>"컷을 안 나누니까 지치는 게 그대로 전해집니다."],
        ['id'=>20,'work'=>'oldboy','workTitle'=>'올드보이','title'=>'20년 지나도 안 낡았다','author'=>'주말영화','sentiment'=>'호평','views'=>234,'likes'=>29,'created'=>25,'content'=>"어제 다시 봤는데 촬영이 지금 기준으로도 세련됐어요."],
        ['id'=>21,'work'=>'oldboy','workTitle'=>'올드보이','title'=>'결말 해석이 너무 무겁다','author'=>'영화광','sentiment'=>'보통','views'=>178,'likes'=>15,'created'=>24,'content'=>"완성도는 인정하는데 다시 볼 엄두는 잘 안 나네요."],
        ['id'=>22,'work'=>'oldboy','workTitle'=>'올드보이','title'=>'지금 보면 호불호 갈릴 듯','author'=>'쓴소리','sentiment'=>'혹평','views'=>145,'likes'=>9,'created'=>23,'content'=>"수위가 세서 처음 보는 분께 권하긴 어렵습니다."],
        // ── 살인의 추억 (4개) ──
        ['id'=>23,'work'=>'memories','workTitle'=>'살인의 추억','title'=>'마지막 그 표정 하나로 끝냄','author'=>'해석러','sentiment'=>'호평','views'=>398,'likes'=>55,'created'=>22,'content'=>"카메라를 정면으로 보는 그 컷이 관객한테 묻는 것 같았어요."],
        ['id'=>24,'work'=>'memories','workTitle'=>'살인의 추억','title'=>'실화 기반이라 더 먹먹함','author'=>'정보통','sentiment'=>'호평','views'=>267,'likes'=>31,'created'=>21,'content'=>"사건 배경을 알고 보면 장면마다 무게가 다릅니다."],
        ['id'=>25,'work'=>'memories','workTitle'=>'살인의 추억','title'=>'형사 캐릭터 대비가 훌륭','author'=>'리뷰러','sentiment'=>'호평','views'=>189,'likes'=>18,'created'=>20,'content'=>"둘이 서서히 서로를 닮아가는 흐름이 좋았습니다."],
        ['id'=>26,'work'=>'memories','workTitle'=>'살인의 추억','title'=>'중반부 늘어진다는 느낌','author'=>'삐딱이','sentiment'=>'보통','views'=>132,'likes'=>7,'created'=>19,'content'=>"수사 반복 구간이 조금 길게 느껴졌어요."],
        // ── 인셉션 (5개) ──
        ['id'=>27,'work'=>'inception','workTitle'=>'인셉션','title'=>'킥 개념 아직도 헷갈립니다','author'=>'질문러','sentiment'=>'보통','views'=>356,'likes'=>24,'created'=>18,'content'=>"떨어지는 충격으로 깨어난다는 건 알겠는데 층마다 타이밍이 어렵네요."],
        ['id'=>28,'work'=>'inception','workTitle'=>'인셉션','title'=>'팽이 결말 드디어 이해함','author'=>'영화광','sentiment'=>'호평','views'=>723,'likes'=>96,'created'=>17,'content'=>"팽이가 아니라 마지막에 카메라가 어디를 향했는지가 핵심이더라고요."],
        ['id'=>29,'work'=>'inception','workTitle'=>'인셉션','title'=>'복도 회전 액션은 실제 세트','author'=>'정보통','sentiment'=>'호평','views'=>291,'likes'=>37,'created'=>16,'content'=>"통째로 돌아가는 세트를 만들어 찍었다고 합니다."],
        ['id'=>30,'work'=>'inception','workTitle'=>'인셉션','title'=>'설명이 너무 친절해서 아쉬움','author'=>'쓴소리','sentiment'=>'혹평','views'=>167,'likes'=>12,'created'=>15,'content'=>"대사로 규칙을 다 알려주니 추리할 여지가 줄었어요."],
        ['id'=>31,'work'=>'inception','workTitle'=>'인셉션','title'=>'음악이 시간 개념을 살린다','author'=>'팝콘각','sentiment'=>'호평','views'=>205,'likes'=>22,'created'=>14,'content'=>"느려지는 층마다 음악도 같이 늘어지는 게 소름이었습니다."],
        // ── 라라랜드 (4개) ──
        ['id'=>32,'work'=>'lalaland','workTitle'=>'라라랜드','title'=>'오프닝 고속도로 씬 실화냐','author'=>'주말영화','sentiment'=>'호평','views'=>342,'likes'=>48,'created'=>13,'content'=>"진짜 고속도로를 막고 찍었다는 게 아직도 안 믿깁니다."],
        ['id'=>33,'work'=>'lalaland','workTitle'=>'라라랜드','title'=>'결말 때문에 며칠 앓았습니다','author'=>'영화광','sentiment'=>'호평','views'=>468,'likes'=>67,'created'=>12,'content'=>"해피엔딩이 아닌데 이상하게 위로가 되는 결말이었어요."],
        ['id'=>34,'work'=>'lalaland','workTitle'=>'라라랜드','title'=>'뮤지컬 싫어해도 볼만한가요','author'=>'질문러','sentiment'=>'보통','views'=>121,'likes'=>6,'created'=>11,'content'=>"노래가 계속 나오는 영화는 잘 안 맞는데 고민 중입니다."],
        ['id'=>35,'work'=>'lalaland','workTitle'=>'라라랜드','title'=>'스토리는 평범하다고 봅니다','author'=>'삐딱이','sentiment'=>'혹평','views'=>176,'likes'=>13,'created'=>10,'content'=>"연출과 음악이 좋은 거지 이야기 자체는 익숙했어요."],
        // ── 미스터 션샤인 (3개) ──
        ['id'=>36,'work'=>'mrsunshine','workTitle'=>'미스터 션샤인','title'=>'영상미 하나는 최고였다','author'=>'리뷰러','sentiment'=>'호평','views'=>254,'likes'=>33,'created'=>9,'content'=>"화면을 정지시켜도 그림 같은 장면이 많았습니다."],
        ['id'=>37,'work'=>'mrsunshine','workTitle'=>'미스터 션샤인','title'=>'역사 고증 논란 정리','author'=>'정보통','sentiment'=>'보통','views'=>312,'likes'=>28,'created'=>8,'content'=>"창작과 사실이 섞인 부분을 따로 정리해봤습니다."],
        ['id'=>38,'work'=>'mrsunshine','workTitle'=>'미스터 션샤인','title'=>'초반 진입장벽이 좀 있어요','author'=>'쓴소리','sentiment'=>'보통','views'=>98,'likes'=>5,'created'=>7,'content'=>"인물이 한꺼번에 나와서 3화까지는 정리가 안 되더군요."],
        // ── 시그널 (3개) ──
        ['id'=>39,'work'=>'signal','workTitle'=>'시그널','title'=>'무전기 설정이 반칙급으로 좋음','author'=>'정주행러','sentiment'=>'호평','views'=>411,'likes'=>59,'created'=>6,'content'=>"과거를 바꾸면 현재가 바뀐다는 규칙을 끝까지 지키는 게 대단합니다."],
        ['id'=>40,'work'=>'signal','workTitle'=>'시그널','title'=>'시즌2는 언제 나오나요','author'=>'영화광','sentiment'=>'보통','views'=>288,'likes'=>21,'created'=>5,'content'=>"몇 년째 기다리는 중입니다. 소식 아시는 분 계신가요."],
        ['id'=>41,'work'=>'signal','workTitle'=>'시그널','title'=>'실제 사건 모티프 정리','author'=>'정보통','sentiment'=>'호평','views'=>199,'likes'=>17,'created'=>4,'content'=>"각 에피소드가 어떤 사건을 참고했는지 모아봤습니다."],
        // ── 나의 아저씨 (3개) ──
        ['id'=>42,'work'=>'myahjussi','workTitle'=>'나의 아저씨','title'=>'제목만 보고 넘겼던 게 후회됨','author'=>'심야극장','sentiment'=>'호평','views'=>377,'likes'=>52,'created'=>3,'content'=>"제목 때문에 몇 년을 미뤘는데 보고 나서 제 편견을 반성했습니다."],
        ['id'=>43,'work'=>'myahjussi','workTitle'=>'나의 아저씨','title'=>'대사 하나하나가 위로였다','author'=>'자막러','sentiment'=>'호평','views'=>263,'likes'=>38,'created'=>2,'content'=>"별말 아닌 대사인데 상황이 얹히니까 계속 곱씹게 되네요."],
        ['id'=>44,'work'=>'myahjussi','workTitle'=>'나의 아저씨','title'=>'초반이 무겁다는 평에 대해','author'=>'리뷰러','sentiment'=>'보통','views'=>141,'likes'=>10,'created'=>1,'content'=>"초반을 넘기면 분위기가 달라지니 4화까지는 보시길 권합니다."],
    ];
}

// ── ② 원본 + 임시 보관함(세션)을 '합쳐서' 돌려준다 ───────────
//   나중에 DB가 생기면 이 함수는 "SELECT * FROM posts" 한 줄로 바뀐다.
function get_posts(): array {
    $posts = base_posts();

    // 이번 접속에서 새로 쓴 글을 뒤에 붙인다
    foreach ($_SESSION['new_posts'] ?? [] as $p) {
        $posts[] = $p;
    }

    $edited  = $_SESSION['edited_posts']  ?? [];   // [글번호 => 바뀐 값들]
    $deleted = $_SESSION['deleted_posts'] ?? [];   // [지운 글번호, …]

    $result = [];
    foreach ($posts as $p) {
        // 지운 글은 건너뛴다
        if (in_array($p['id'], $deleted, true)) {
            continue;
        }
        // 수정한 내용이 있으면 덮어쓴다 (array_merge = 같은 키면 뒤엣것이 이김)
        if (isset($edited[$p['id']])) {
            $p = array_merge($p, $edited[$p['id']]);
        }
        // ★ 추천은 '1인 1회'다. 내가 눌렀으면 +1, 안 눌렀으면 그대로.
        //   (여러 번 눌러도 계속 오르면 안 되므로 '횟수'가 아니라 '눌렀나/안 눌렀나'로 관리)
        $p['likes'] += has_liked($p['id']) ? 1 : 0;

        // ★ 댓글 수는 '실제 댓글을 세어서' 채운다 → 목록의 숫자와 실물이 항상 일치한다.
        //   (댓글을 달면 목록의 '댓글 N'도 같이 올라간다)
        $p['comments'] = count_comments($p['id']);

        $result[] = $p;
    }
    return $result;
}

// '지워진' 글 하나 찾기. 안 지워졌거나 없으면 null.
//   되돌리기(restore)에서 "정말 지워진 글인지 + 주인이 맞는지" 확인할 때 쓴다.
//   ★ get_post()는 지워진 글을 걸러내고 주므로 여기선 원본을 직접 뒤진다.
function get_deleted_post(int $id): ?array {
    $deleted = $_SESSION['deleted_posts'] ?? [];
    if (!in_array($id, $deleted, true)) {
        return null;                      // 지워진 적 없으면 되돌릴 것도 없다
    }
    $all = base_posts();
    foreach ($_SESSION['new_posts'] ?? [] as $p) {
        $all[] = $p;
    }
    foreach ($all as $p) {
        if ($p['id'] === $id) {
            return $p;
        }
    }
    return null;
}

// id로 글 하나 찾기. 없으면 null. (Tester-Doer: 호출한 쪽에서 null 체크)
function get_post(int $id): ?array {
    foreach (get_posts() as $p) {
        if ($p['id'] === $id) {
            return $p;
        }
    }
    return null;
}

// ── 임시 보관함에 쓰기 (나중엔 INSERT/UPDATE/DELETE로 교체) ──

// 다음 글 번호 만들기 (지금 있는 것 중 가장 큰 번호 + 1)
function next_post_id(): int {
    $max = 0;
    foreach (base_posts() as $p) {
        $max = max($max, $p['id']);
    }
    foreach ($_SESSION['new_posts'] ?? [] as $p) {
        $max = max($max, $p['id']);
    }
    return $max + 1;
}

// 새 글 저장 → 새 글 번호를 돌려준다  (나중: INSERT INTO posts …)
function add_post(string $work, string $workTitle, string $title, string $content, string $sentiment, string $author): int {
    $id = next_post_id();
    $_SESSION['new_posts'][] = [
        'id' => $id, 'work' => $work, 'workTitle' => $workTitle,
        'title' => $title, 'content' => $content, 'sentiment' => $sentiment,
        'author' => $author,
        'views' => 0, 'comments' => 0, 'likes' => 0,
        'created' => time(),   // 최신순 정렬에서 맨 위로 오도록 현재 시각
    ];
    return $id;
}

// 글 수정 내용 기록  (나중: UPDATE posts SET … WHERE id = ?)
function update_post(int $id, string $title, string $content, string $sentiment): void {
    $_SESSION['edited_posts'][$id] = [
        'title' => $title, 'content' => $content, 'sentiment' => $sentiment,
    ];
}

// 글 삭제 기록  (나중: DELETE FROM posts WHERE id = ?)
function delete_post(int $id): void {
    $_SESSION['deleted_posts'][] = $id;
}

// 삭제 되돌리기 — '지운 글 번호' 목록에서 그 번호만 빼면 글이 그대로 돌아온다.
//   ★ 원본을 진짜로 없애지 않고 '지웠다고 표시'만 했기 때문에 복구가 가능하다.
//     실무 DB도 DELETE 대신 deleted_at 칼럼을 채우는 '소프트 삭제'를 자주 쓴다.
//   array_values = 빠진 자리 때문에 생긴 번호 구멍을 메워 배열을 다시 촘촘하게 만든다.
function restore_post(int $id): void {
    $deleted = $_SESSION['deleted_posts'] ?? [];
    $_SESSION['deleted_posts'] = array_values(
        array_filter($deleted, fn($deletedId) => $deletedId !== $id)
    );
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
//   세션은 '이 브라우저 = 이 사용자'의 공간이므로, 여기 기록이 곧 '내 추천 여부'다.
function has_liked(int $postId): bool {
    return !empty($_SESSION['my_likes'][$postId]);
}

// 추천 토글: 이미 눌렀으면 취소, 안 눌렀으면 추천.
//   ★ 실제 서비스도 이렇게 동작한다(1인 1회, 다시 누르면 취소).
//   나중 DB에선: likes 테이블에 (user_id, post_id) 있으면 DELETE, 없으면 INSERT.
function toggle_like(int $postId): void {
    if (has_liked($postId)) {
        unset($_SESSION['my_likes'][$postId]);   // 추천 취소
    } else {
        $_SESSION['my_likes'][$postId] = true;   // 추천
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
