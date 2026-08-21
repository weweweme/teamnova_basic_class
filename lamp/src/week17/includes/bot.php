<?php
// ============================================================
// bot.php — 사람이 아닌 방문자(크롤러·봇) 가려내기
//
//   [왜 필요한가 — 실제로 재본 숫자]
//     쿠키를 안 받는 상대가 30번 요청하면 **세션 행이 30개** 쌓인다(요청 1개 = 행 1개).
//     크롤러가 초당 10요청으로 30분(세션 TTL) 훑으면 **18,000행**이다.
//     용량이야 2MB 남짓이라 견디지만, 문제는 그게 아니다:
//       ① **읽기만 하는 상대에게 세션을 만들 이유가 없다** — 순수한 낭비다
//       ② 조회수가 **사람이 안 본 만큼 부풀려진다**
//       ③ `sessions` 표가 쓰레기로 가득 차 **정작 봐야 할 행이 안 보인다**
//
//   ★ 이건 **세션을 DB에 둔 대가**다. 파일이나 Redis라면 신경 쓸 일이 아니다
//     (Redis는 TTL이 내장이라 알아서 사라진다). DB에 두니 비용이 눈에 보이게 됐다.
//
//   [★ 판별의 한계를 인정하고 쓴다]
//     User-Agent는 **보내는 쪽이 마음대로 정하는 값**이다. 숨기려는 봇은 못 잡는다.
//     그래도 쓰는 이유: 검색엔진처럼 **정직한 크롤러가 트래픽의 대부분**이고,
//     그것들은 스스로를 밝히기 때문이다. 숨기는 봇은 **다른 층에서 막을 문제**다.
//     → 그래서 이 판별은 **보안 장치가 아니라 청소 장치**다. 틀려도 손해가 작은 쪽으로 쓴다.
//
//   [틀렸을 때 무슨 일이 생기나 — 이게 설계의 기준이다]
//     · 사람을 봇으로 오인하면 → 세션이 안 생겨 **로그인이 안 된다** (치명적)
//     · 봇을 사람으로 오인하면 → 행이 조금 쌓인다 (사소함)
//     → **의심스러우면 사람으로 본다.** 목록을 좁게 유지하는 이유다.
// ============================================================

// User-Agent에 이 조각이 들어 있으면 봇으로 본다. (소문자로 비교)
//   ★ 'bot'·'crawl'·'spider'는 이름에 스스로 밝히는 관행이 굳어 있어 넓게 잡아도 안전하다.
//   ★ 자동화 도구(curl·python-requests…)도 넣는다 — 사람이 브라우저로 오는 경우가 아니다.
//   ※ 'Mozilla'로 시작한다고 사람인 것은 아니다. 거의 모든 봇이 그렇게 위장한다.
const BOT_SIGNATURES = [
    // 스스로 밝히는 크롤러
    'bot', 'crawl', 'spider', 'slurp', 'archiver',
    // 대표적인 이름들 (위 조각에 안 걸리는 것만)
    'facebookexternalhit', 'ia_archiver', 'yandex', 'baidu', 'sogou', 'duckduck',
    // 미리보기·요약 수집기
    'preview', 'embedly', 'quora link', 'whatsapp', 'telegram',
    // 자동화 도구·헤드리스 브라우저
    'curl/', 'wget', 'python-requests', 'python-urllib', 'go-http-client',
    'java/', 'okhttp', 'axios', 'scrapy', 'headless', 'phantomjs',
];

// 지금 요청이 봇인가?
//   ★ 한 요청 안에서 여러 번 불리므로 결과를 기억한다(static).
//     문자열 비교가 싸긴 해도, 매번 배열을 훑을 이유는 없다.
function is_bot(): bool {
    static $cached = null;
    if ($cached !== null) {
        return $cached;
    }

    $ua = strtolower((string) ($_SERVER['HTTP_USER_AGENT'] ?? ''));

    // ★ User-Agent가 아예 없으면 봇으로 본다.
    //   정상 브라우저는 반드시 보낸다. 안 보내는 건 스크립트이거나 숨기려는 쪽이다.
    if ($ua === '') {
        return $cached = true;
    }

    foreach (BOT_SIGNATURES as $needle) {
        if (str_contains($ua, $needle)) {
            return $cached = true;
        }
    }
    return $cached = false;
}
