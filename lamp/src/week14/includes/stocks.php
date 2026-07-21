<?php
// ============================================================
// stocks.php — '종목(stock)' 데이터 + 관련 동작을 모아둔 도메인 모듈
//   posts.php(글 전용)와 짝을 이루는 파일.
//   지금은 더미 배열이지만, 나중 DB를 붙이면 이 함수들 '속'만 stocks 테이블 조회로 바꾸면 됨.
// ============================================================

// 우리가 다루는 종목 목록 (나중 stocks 테이블 조회로 교체)
function get_stocks(): array {
    return [
        ['ticker' => '005930', 'name' => '삼성전자',   'market' => 'KOSPI'],
        ['ticker' => '000660', 'name' => 'SK하이닉스', 'market' => 'KOSPI'],
        ['ticker' => 'AAPL',   'name' => '애플',       'market' => 'NASDAQ'],
    ];
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
