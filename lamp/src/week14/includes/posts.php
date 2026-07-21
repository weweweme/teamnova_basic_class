<?php
// ============================================================
// posts.php — '글(post)' 데이터 + 관련 동작을 모아둔 도메인 모듈
//   ★ util.php(어디서나 쓰는 범용 헬퍼)와 달리, 여기는 '글' 주제 전용.
//   지금은 더미 배열이지만, 나중 DB를 붙이면 이 함수들 '속'만 SELECT로 바꾸면 됨.
//   → 바깥 페이지(board·view·search…)는 안 바뀜 = 관심사 분리의 이점.
// ============================================================

// 더미 글 전체. 호출할 때마다 '새 배열'을 돌려준다(get_ 이지만 더미라 매번 생성).
//   work      = 어느 작품 게시판의 글인지 (works 모듈의 slug와 짝)
//   sentiment = 호평 / 보통 / 혹평
//   정렬이 눈에 보이게 views·comments·created를 일부러 다르게 둠.
function get_posts(): array {
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

// id로 글 하나 찾기. 없으면 null. (Tester-Doer: 호출한 쪽에서 null 체크)
function get_post(int $id): ?array {
    foreach (get_posts() as $p) {
        if ($p['id'] === $id) {
            return $p;
        }
    }
    return null;
}

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
//   예) perPage=3 일 때  1페이지→0번부터 3개, 2페이지→3번부터 3개
function paginate_posts(array $posts, int $page, int $perPage): array {
    $offset = ($page - 1) * $perPage;   // 몇 번째부터 자를지
    return array_slice($posts, $offset, $perPage);
}

// 감상(호평/보통/혹평)으로 글을 걸러낸다.
//   $sentiment가 빈 문자열이면 '전체'라는 뜻 → 거르지 않고 그대로 돌려준다.
function filter_posts_by_sentiment(array $posts, string $sentiment): array {
    if ($sentiment === '') {
        return $posts;              // 전체 = 필터 없음
    }
    $result = [];
    foreach ($posts as $p) {
        if ($p['sentiment'] === $sentiment) {
            $result[] = $p;         // 조건에 맞는 것만 새 배열에 담는다
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
