<?php
// ============================================================
// stocks.php — '종목(stock)' 데이터 + 관련 동작을 모아둔 도메인 모듈
//   posts.php(글 전용)와 짝을 이루는 파일.
//   지금은 더미 배열이지만, 나중 DB를 붙이면 이 함수들 '속'만 stocks 테이블 조회로 바꾸면 됨.
// ============================================================

// 우리가 다루는 종목 목록 (나중 stocks 테이블 조회로 교체)
//   buyVotes/sellVotes = 투자심리 투표 집계 (나중 votes 테이블에서 세어올 값)
function get_stocks(): array {
    return [
        ['ticker' => '005930', 'name' => '삼성전자',   'market' => 'KOSPI',  'buyVotes' => 128, 'sellVotes' => 72],
        ['ticker' => '000660', 'name' => 'SK하이닉스', 'market' => 'KOSPI',  'buyVotes' => 95,  'sellVotes' => 105],
        ['ticker' => 'AAPL',   'name' => '애플',       'market' => 'NASDAQ', 'buyVotes' => 60,  'sellVotes' => 40],
    ];
}

// ticker로 종목 '한 건 전체'를 찾는다. 없으면 null.
function get_stock(string $ticker): ?array {
    foreach (get_stocks() as $s) {
        if ($s['ticker'] === $ticker) {
            return $s;
        }
    }
    return null;
}

// 검색어(q)가 '종목명 또는 종목코드'에 들어있는 종목만 걸러낸다.
//   예) '삼성' → 삼성전자 / '005930' → 삼성전자 / '하이' → SK하이닉스
//   mb_stripos = 한글 안전한 '문자열 포함 여부' 찾기. 못 찾으면 false.
function search_stocks(array $stocks, string $q): array {
    if ($q === '') {
        return $stocks;             // 검색어 없으면 전체
    }
    $result = [];
    foreach ($stocks as $s) {
        // ── mb_stripos(찾을 대상, 찾을 문자열) ──────────────────────────
        //   대상 문자열 안에 그 글자가 '들어있는지' 찾는 함수.
        //   · 반환값: 찾으면 '몇 번째 글자인지' 위치(0, 1, 2 …), 못 찾으면 false
        //   · 앞의 mb_ = multibyte(여러 바이트). 한글처럼 한 글자가 여러 바이트인 문자도
        //     안전하게 다룬다. (그냥 stripos를 쓰면 한글에서 위치 계산이 어긋날 수 있음)
        //   · 가운데 i = insensitive(무시). 대소문자를 구분하지 않는다
        //     → 'aapl'로 검색해도 'AAPL'을 찾아줌.
        //   Java로 치면 name.toLowerCase().indexOf(q) 와 비슷 (Java는 못 찾으면 -1을 돌려줌)
        //
        //   ★ 왜 !== false 로 비교하나? (PHP의 유명한 함정)
        //     맨 앞(0번째)에서 찾으면 0을 돌려주는데, if(0)은 '거짓'으로 취급된다.
        //     그냥 if(mb_stripos(...)) 라고 쓰면 '맨 앞에서 찾았는데 못 찾았다'고 잘못 판단함.
        //     그래서 값+타입을 엄격히 보는 !== 로 "false가 아니면 = 찾은 것"이라 판단한다.
        //
        //   복합 조건은 이름 붙인 boolean으로 쪼개서 읽기 쉽게 (CLAUDE.md 규칙)
        $inName   = mb_stripos($s['name'], $q)   !== false;
        $inTicker = mb_stripos($s['ticker'], $q) !== false;
        if ($inName || $inTicker) {
            $result[] = $s;
        }
    }
    return $result;
}

// ticker로 종목명 찾기. 없으면 null.
//   (Tester-Doer: null이 올 수 있으니 부르는 쪽에서 ?? 로 처리한다)
function get_stock_name(string $ticker): ?string {
    foreach (get_stocks() as $s) {
        if ($s['ticker'] === $ticker) {
            return $s['name'];
        }
    }
    return null;
}
