<?php
// ============================================================
// posts.php — '글(post)' 데이터 + 관련 동작을 모아둔 도메인 모듈
//   ★ util.php(어디서나 쓰는 범용 헬퍼)와 달리, 여기는 '글' 주제 전용.
//   지금은 더미 배열이지만, 나중 DB를 붙이면 이 함수들 '속'만 SELECT로 바꾸면 됨.
//   → 바깥 페이지(board·view·search…)는 안 바뀜 = 관심사 분리의 이점.
// ============================================================

// 더미 글 전체. 호출할 때마다 '새 배열'을 돌려준다(get_ 이지만 더미라 매번 생성).
//   정렬이 눈에 보이게 views·comments·created를 일부러 다르게 둠.
function get_posts(): array {
    return [
        // ── 삼성전자(005930) ──
        ['id'=>1,'ticker'=>'005930','stock'=>'삼성전자','title'=>'지금 들어가도 될까요?','author'=>'개미1','sentiment'=>'매수','views'=>320,'comments'=>12,'created'=>10,'content'=>"요즘 반도체 업황이 살아나는 것 같은데\n지금 들어가도 괜찮을까요?"],
        ['id'=>2,'ticker'=>'005930','stock'=>'삼성전자','title'=>'실적 발표 한줄 정리','author'=>'투자왕','sentiment'=>'중립','views'=>210,'comments'=>5,'created'=>9,'content'=>"이번 분기 실적은 시장 예상에 부합.\n다음 분기 가이던스가 관건."],
        ['id'=>3,'ticker'=>'005930','stock'=>'삼성전자','title'=>'외국인 매도 무섭네요','author'=>'불안러','sentiment'=>'매도','views'=>540,'comments'=>8,'created'=>8,'content'=>"외국인 순매도가 계속되네요. 조심하세요."],
        ['id'=>4,'ticker'=>'005930','stock'=>'삼성전자','title'=>'배당 재투자 계획','author'=>'장기러','sentiment'=>'매수','views'=>150,'comments'=>25,'created'=>7,'content'=>"배당 받으면 그대로 재투자할 생각입니다."],
        ['id'=>5,'ticker'=>'005930','stock'=>'삼성전자','title'=>'단기 조정 오나요','author'=>'차티','sentiment'=>'매도','views'=>420,'comments'=>18,'created'=>6,'content'=>"차트상 단기 조정 가능성 있어 보입니다."],
        // ── SK하이닉스(000660) ──
        ['id'=>6,'ticker'=>'000660','stock'=>'SK하이닉스','title'=>'HBM 수요 어떻게 보세요','author'=>'메모리팬','sentiment'=>'매수','views'=>380,'comments'=>22,'created'=>5,'content'=>"HBM 수요가 계속 늘 것 같은데 어떻게 보시나요?"],
        ['id'=>7,'ticker'=>'000660','stock'=>'SK하이닉스','title'=>'고점 아닌가요','author'=>'신중이','sentiment'=>'매도','views'=>260,'comments'=>14,'created'=>4,'content'=>"많이 올라서 지금은 부담스럽습니다."],
        ['id'=>8,'ticker'=>'000660','stock'=>'SK하이닉스','title'=>'뉴스 공유합니다','author'=>'뉴스봇','sentiment'=>'중립','views'=>90,'comments'=>40,'created'=>3,'content'=>"관련 뉴스 정리해서 공유합니다."],
        // ── 애플(AAPL) ──
        ['id'=>9,'ticker'=>'AAPL','stock'=>'애플','title'=>'아이폰 신제품 기대','author'=>'사과러','sentiment'=>'매수','views'=>300,'comments'=>9,'created'=>2,'content'=>"신제품 사이클 기대해봅니다."],
        ['id'=>10,'ticker'=>'AAPL','stock'=>'애플','title'=>'환율이 부담이네요','author'=>'달러맨','sentiment'=>'중립','views'=>120,'comments'=>3,'created'=>1,'content'=>"환율 때문에 원화 기준 수익률이 애매하네요."],
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

// 특정 종목(ticker)의 글만 걸러낸다.
//   종목 토론방은 '그 종목 글'만 보여야 하므로, 목록 만들기의 '첫 단계'가 이것이다.
function filter_posts_by_ticker(array $posts, string $ticker): array {
    $result = [];
    foreach ($posts as $p) {
        if ($p['ticker'] === $ticker) {
            $result[] = $p;
        }
    }
    return $result;
}

// 검색어(q)가 '제목 또는 내용'에 들어있는 글만 걸러낸다.
//   mb_stripos(대상, 찾을것) = 문자열 안에 그게 있는지 찾기 (mb_ = 한글 안전한 버전).
//     찾으면 '몇 번째 글자인지'(숫자), 못 찾으면 false 를 돌려준다.
//     → false가 '아니면' 포함된 것. (0번째에서 찾으면 0인데, 0도 '없음'으로 착각하지 않도록 !== 로 비교)
function search_posts(array $posts, string $q): array {
    if ($q === '') {
        return $posts;              // 검색어 없으면 거르지 않음
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
//   예) perPage=3 일 때  1페이지→0번부터 3개, 2페이지→3번부터 3개, 3페이지→6번부터 3개
function paginate_posts(array $posts, int $page, int $perPage): array {
    $offset = ($page - 1) * $perPage;   // 몇 번째부터 자를지
    return array_slice($posts, $offset, $perPage);
}

// 심리(매수/매도/중립)로 글을 걸러낸다.
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
