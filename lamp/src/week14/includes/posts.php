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
    return [
        // ── 기생충 ──
        ['id'=>1,'work'=>'parasite','workTitle'=>'기생충','title'=>'계단 연출이 진짜 미쳤다','author'=>'영화광','sentiment'=>'호평','views'=>320,'comments'=>12,'likes'=>45,'created'=>10,'content'=>"위아래로 오르내리는 계단이 계급을 그대로 보여주더라고요.\n두 번째 보니 더 잘 보입니다."],
        ['id'=>2,'work'=>'parasite','workTitle'=>'기생충','title'=>'결말 해석 정리해봤어요','author'=>'해석러','sentiment'=>'보통','views'=>210,'comments'=>5,'likes'=>12,'created'=>9,'content'=>"마지막 장면이 현실인지 상상인지 의견이 갈리는데,\n저는 상상 쪽에 가깝다고 봅니다."],
        ['id'=>3,'work'=>'parasite','workTitle'=>'기생충','title'=>'후반부는 좀 과했다고 봅니다','author'=>'쓴소리','sentiment'=>'혹평','views'=>540,'comments'=>8,'likes'=>30,'created'=>8,'content'=>"전반부 긴장감은 최고였는데 후반이 급하게 느껴졌어요."],
        ['id'=>4,'work'=>'parasite','workTitle'=>'기생충','title'=>'음악이 이렇게 좋았나요','author'=>'영화광','sentiment'=>'호평','views'=>150,'comments'=>25,'likes'=>8,'created'=>7,'content'=>"OST 따로 듣는데 장면이 계속 떠오릅니다."],
        ['id'=>5,'work'=>'parasite','workTitle'=>'기생충','title'=>'솔직히 과대평가 아닌가요','author'=>'삐딱이','sentiment'=>'혹평','views'=>420,'comments'=>18,'likes'=>22,'created'=>6,'content'=>"잘 만든 건 맞는데 이 정도 열광은 좀 과한 듯."],
        // ── 오징어 게임 ──
        ['id'=>6,'work'=>'squidgame','workTitle'=>'오징어 게임','title'=>'1화부터 몰입도 미쳤음','author'=>'정주행러','sentiment'=>'호평','views'=>380,'comments'=>22,'likes'=>51,'created'=>5,'content'=>"밤새 다 봤습니다. 첫 게임 장면이 압권."],
        ['id'=>7,'work'=>'squidgame','workTitle'=>'오징어 게임','title'=>'후반 전개가 아쉬워요','author'=>'쓴소리','sentiment'=>'혹평','views'=>260,'comments'=>14,'likes'=>17,'created'=>4,'content'=>"캐릭터 소모가 좀 심했다고 느꼈습니다."],
        ['id'=>8,'work'=>'squidgame','workTitle'=>'오징어 게임','title'=>'해외 반응 모아봤습니다','author'=>'정보통','sentiment'=>'보통','views'=>90,'comments'=>40,'likes'=>5,'created'=>3,'content'=>"관련 리뷰들 정리해서 공유합니다."],
        // ── 인터스텔라 ──
        ['id'=>9,'work'=>'interstellar','workTitle'=>'인터스텔라','title'=>'도킹 장면은 극장에서 봐야 함','author'=>'영화광','sentiment'=>'호평','views'=>300,'comments'=>9,'likes'=>26,'created'=>2,'content'=>"음향까지 합쳐지니 완전히 다른 경험이었어요."],
        ['id'=>10,'work'=>'interstellar','workTitle'=>'인터스텔라','title'=>'과학 고증 어디까지 맞나요','author'=>'질문러','sentiment'=>'보통','views'=>120,'comments'=>3,'likes'=>9,'created'=>1,'content'=>"블랙홀 묘사가 실제 이론과 얼마나 가까운지 궁금합니다."],
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
