<?php
// ============================================================
// works.php — '작품(영화·드라마)' 데이터 + 관련 동작을 모아둔 도메인 모듈
//   posts.php(글 전용)와 짝을 이루는 파일.
//   지금은 더미 배열이지만, 나중 DB를 붙이면 이 함수들 '속'만 works 테이블 조회로 바꾸면 됨.
// ============================================================

// ① 처음부터 있는 더미 작품 (코드에 박힌 원본)
//   slug = 주소에 쓰는 짧은 영문 이름 (예: /board/?work=parasite)
//   upVotes/downVotes = 추천/비추천 투표 집계 (나중 votes 테이블에서 세어올 값)
function base_works(): array {
    return [
        [
            'slug' => 'parasite', 'title' => '기생충', 'genre' => '영화',
            'year' => 2019, 'director' => '봉준호',
            'summary' => '전원 백수인 기택네 장남 기우가 부잣집 과외 면접을 보러 가면서 시작되는 이야기.',
            'upVotes' => 128, 'downVotes' => 72,
        ],
        [
            'slug' => 'squidgame', 'title' => '오징어 게임', 'genre' => '드라마',
            'year' => 2021, 'director' => '황동혁',
            'summary' => '빚에 쫓기는 사람들이 456억 원의 상금이 걸린 의문의 서바이벌에 참가한다.',
            'upVotes' => 95, 'downVotes' => 105,
        ],
        [
            'slug' => 'interstellar', 'title' => '인터스텔라', 'genre' => '영화',
            'year' => 2014, 'director' => '크리스토퍼 놀란',
            'summary' => '황폐해진 지구를 떠나 인류의 새로운 터전을 찾아 우주로 향하는 탐사대의 여정.',
            'upVotes' => 160, 'downVotes' => 40,
        ],
        [
            'slug' => 'oldboy', 'title' => '올드보이', 'genre' => '영화',
            'year' => 2003, 'director' => '박찬욱',
            'summary' => '영문도 모른 채 15년간 감금됐던 남자가 풀려나 복수의 실마리를 쫓는다.',
            'upVotes' => 142, 'downVotes' => 38,
        ],
        [
            'slug' => 'memories', 'title' => '살인의 추억', 'genre' => '영화',
            'year' => 2003, 'director' => '봉준호',
            'summary' => '1980년대 시골 마을에서 벌어진 연쇄살인을 쫓는 두 형사의 이야기.',
            'upVotes' => 151, 'downVotes' => 29,
        ],
        [
            'slug' => 'inception', 'title' => '인셉션', 'genre' => '영화',
            'year' => 2010, 'director' => '크리스토퍼 놀란',
            'summary' => '타인의 꿈에 들어가 생각을 훔치는 남자가, 이번엔 생각을 심어야 하는 임무를 맡는다.',
            'upVotes' => 173, 'downVotes' => 47,
        ],
        [
            'slug' => 'lalaland', 'title' => '라라랜드', 'genre' => '영화',
            'year' => 2016, 'director' => '데이미언 셔젤',
            'summary' => '꿈을 좇는 배우 지망생과 재즈 피아니스트가 LA에서 만나 사랑에 빠진다.',
            'upVotes' => 118, 'downVotes' => 62,
        ],
        [
            'slug' => 'mrsunshine', 'title' => '미스터 션샤인', 'genre' => '드라마',
            'year' => 2018, 'director' => '이응복',
            'summary' => '노비의 아들로 태어나 미국 군인이 된 남자가 구한말 조선으로 돌아온다.',
            'upVotes' => 88, 'downVotes' => 32,
        ],
        [
            'slug' => 'signal', 'title' => '시그널', 'genre' => '드라마',
            'year' => 2016, 'director' => '김원석',
            'summary' => '낡은 무전기로 과거의 형사와 연결된 프로파일러가 미제 사건을 파헤친다.',
            'upVotes' => 134, 'downVotes' => 26,
        ],
        [
            'slug' => 'myahjussi', 'title' => '나의 아저씨', 'genre' => '드라마',
            'year' => 2018, 'director' => '김원석',
            'summary' => '삶의 무게를 견디던 중년 남자와 상처투성이 젊은 여자가 서로를 알아본다.',
            'upVotes' => 147, 'downVotes' => 33,
        ],
    ];
}

// ② 원본 + 이번 접속에서 누른 투표를 '합쳐서' 돌려준다
//   (posts.php와 같은 방식. 나중 DB가 생기면 votes 테이블 COUNT로 바뀐다)
function get_works(): array {
    $result = [];
    foreach (base_works() as $w) {
        // ★ 투표도 '1인 1표'. 내가 고른 쪽에만 1표를 더한다.
        //   (여러 번 눌러도 표가 계속 늘면 안 되므로 '내가 무엇을 골랐나'만 기록)
        $mine = my_vote($w['slug']);
        if ($mine === '추천') {
            $w['upVotes']++;
        } elseif ($mine === '비추천') {
            $w['downVotes']++;
        }
        $result[] = $w;
    }
    return $result;
}

// 내가 이 작품에 무엇을 투표했나? ('추천' / '비추천' / 안 했으면 null)
function my_vote(string $slug): ?string {
    return $_SESSION['my_vote'][$slug] ?? null;
}

// 투표 토글 — 글 추천(toggle_like)과 같은 규칙으로 맞춘다.
//   · 안 한 상태에서 누르면      → 투표
//   · 누른 걸 또 누르면          → 취소
//   · 반대쪽을 누르면            → 갈아타기 (총 표는 그대로, 한쪽에서 다른 쪽으로 옮겨감)
//   나중 DB에선: votes 테이블의 (work, user_id) 행을 DELETE / INSERT / UPDATE.
function toggle_vote(string $slug, string $choice): void {
    if (my_vote($slug) === $choice) {
        unset($_SESSION['my_vote'][$slug]);      // 같은 걸 또 눌렀다 = 취소
    } else {
        $_SESSION['my_vote'][$slug] = $choice;   // 처음 투표하거나 반대쪽으로 갈아탐
    }
}

// slug로 작품 '한 건 전체'를 찾는다. 없으면 null.
function get_work(string $slug): ?array {
    foreach (get_works() as $w) {
        if ($w['slug'] === $slug) {
            return $w;
        }
    }
    return null;
}

// slug로 작품 제목만 찾기. 없으면 null.
//   (Tester-Doer: null이 올 수 있으니 부르는 쪽에서 ?? 로 처리한다)
function get_work_title(string $slug): ?string {
    $work = get_work($slug);
    return $work['title'] ?? null;
}

// 장르(영화 / 드라마)로 작품을 걸러낸다. 빈 문자열이면 '전체'.
function filter_works_by_genre(array $works, string $genre): array {
    if ($genre === '') {
        return $works;
    }
    $result = [];
    foreach ($works as $w) {
        if ($w['genre'] === $genre) {
            $result[] = $w;
        }
    }
    return $result;
}

// 검색어(q)가 '작품 제목 또는 감독 이름'에 들어있는 작품만 걸러낸다.
//   예) '기생' → 기생충 / '놀란' → 인터스텔라
function search_works(array $works, string $q): array {
    if ($q === '') {
        return $works;             // 검색어 없으면 전체
    }
    $result = [];
    foreach ($works as $w) {
        // ── mb_stripos(찾을 대상, 찾을 문자열) ──────────────────────────
        //   대상 문자열 안에 그 글자가 '들어있는지' 찾는 함수.
        //   · 반환값: 찾으면 '몇 번째 글자인지' 위치(0, 1, 2 …), 못 찾으면 false
        //   · 앞의 mb_ = multibyte. 한글처럼 한 글자가 여러 바이트인 문자도 안전하게 다룬다.
        //   · 가운데 i = insensitive. 대소문자를 구분하지 않는다.
        //
        //   ★ 왜 !== false 로 비교하나? (PHP의 유명한 함정)
        //     맨 앞(0번째)에서 찾으면 0을 돌려주는데, if(0)은 '거짓'으로 취급된다.
        //     그래서 값+타입을 엄격히 보는 !== 로 "false가 아니면 = 찾은 것"이라 판단한다.
        //
        //   복합 조건은 이름 붙인 boolean으로 쪼개서 읽기 쉽게 (CLAUDE.md 규칙)
        $inTitle    = mb_stripos($w['title'], $q)    !== false;
        $inDirector = mb_stripos($w['director'], $q) !== false;
        if ($inTitle || $inDirector) {
            $result[] = $w;
        }
    }
    return $result;
}
