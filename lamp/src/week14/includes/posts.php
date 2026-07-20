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
        ['id'=>1,'ticker'=>'005930','stock'=>'삼성전자','title'=>'지금 들어가도 될까요?','author'=>'개미1','sentiment'=>'매수','views'=>320,'comments'=>12,'created'=>7,'content'=>"요즘 반도체 업황이 살아나는 것 같은데\n지금 들어가도 괜찮을까요?"],
        ['id'=>2,'ticker'=>'005930','stock'=>'삼성전자','title'=>'실적 발표 한줄 정리','author'=>'투자왕','sentiment'=>'중립','views'=>210,'comments'=>5,'created'=>6,'content'=>"이번 분기 실적은 시장 예상에 부합.\n다음 분기 가이던스가 관건."],
        ['id'=>3,'ticker'=>'005930','stock'=>'삼성전자','title'=>'외국인 매도 무섭네요','author'=>'불안러','sentiment'=>'매도','views'=>540,'comments'=>8,'created'=>5,'content'=>"외국인 순매도가 계속되네요. 조심하세요."],
        ['id'=>4,'ticker'=>'005930','stock'=>'삼성전자','title'=>'배당 재투자 계획','author'=>'장기러','sentiment'=>'매수','views'=>150,'comments'=>25,'created'=>4,'content'=>"배당 받으면 그대로 재투자할 생각입니다."],
        ['id'=>5,'ticker'=>'005930','stock'=>'삼성전자','title'=>'단기 조정 오나요','author'=>'차티','sentiment'=>'매도','views'=>420,'comments'=>18,'created'=>3,'content'=>"차트상 단기 조정 가능성 있어 보입니다."],
        ['id'=>6,'ticker'=>'005930','stock'=>'삼성전자','title'=>'뉴스 공유합니다','author'=>'뉴스봇','sentiment'=>'중립','views'=>90,'comments'=>40,'created'=>2,'content'=>"관련 뉴스 정리해서 공유합니다."],
        ['id'=>7,'ticker'=>'005930','stock'=>'삼성전자','title'=>'저점 매수 각?','author'=>'개미2','sentiment'=>'매수','views'=>270,'comments'=>3,'created'=>1,'content'=>"슬슬 저점 같은데 다들 어떻게 보세요?"],
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
