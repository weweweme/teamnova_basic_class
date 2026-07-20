<?php
// ============================================================
// util.php — 여러 페이지에서 공통으로 쓰는 '도구 함수' 모음
//   (Java로 치면 static 유틸 메서드만 모아둔 클래스 같은 것)
// ============================================================

// e() : 사용자 입력을 화면에 '안전하게' 출력하는 함수.
//   왜 필요? 사용자가 글에 <script>나쁜코드</script> 를 적으면,
//   그대로 화면에 꽂을 때 브라우저가 진짜 실행해버림(= XSS 공격).
//   htmlspecialchars()가 < > " & 같은 특수문자를 '무해한 글자'로 바꿔줌.
//   → 앞으로 사용자 데이터를 화면에 찍을 땐 무조건 e()로 감싼다.
function e(string $text): string {
    return htmlspecialchars($text, ENT_QUOTES, 'UTF-8');
}

// query_url() : 지금 주소의 GET 파라미터는 '유지'하고, 일부만 바꾼 새 주소를 만든다.
//   왜 필요? 정렬 탭을 눌러도 '종목·필터'가 살아있어야 하고,
//            필터를 눌러도 '정렬'이 살아있어야 하니까.
//            (안 그러면 탭 누를 때마다 다른 조건이 날아감)
//   예) 지금 ?ticker=005930&sort=views 인 상태에서
//       query_url('/board/', ['sentiment'=>'매수'])
//       → /board/?ticker=005930&sort=views&sentiment=매수
function query_url(string $path, array $overrides = []): string {
    // array_merge : 현재 $_GET 위에 $overrides를 덮어쓴다(같은 키면 새 값이 이김).
    $params = array_merge($_GET, $overrides);

    // 값이 빈 것('')은 주소에서 아예 빼버린다.
    //   왜 이렇게 하나?
    //   ① 지저분한 빈 파라미터 방지 — '전체' 필터를 고르면 sentiment=''가 되는데,
    //      그대로 두면 /board/?ticker=005930&sort=new&sentiment=  처럼 꼬리가 남는다.
    //   ② 상태가 주소에 정직하게 드러남 — "sentiment 항목 자체가 없음" = "필터 안 걸림".
    //      (빈 값으로 남겨두면 '필터를 건 건가 만 건가' 헷갈림)
    //   ③ 같은 화면인데 주소가 여러 개가 되는 걸 막음 —
    //      '?sentiment=' 와 '아무것도 없음'은 결과가 똑같은데 URL은 서로 달라진다.
    //      URL이 갈라지면 공유·북마크·캐시 입장에서 같은 페이지를 다른 것으로 취급해 낭비.
    //   fn($v) => ... 는 '짧은 익명 함수' (Java 람다와 같은 것).
    $params = array_filter($params, fn($v) => $v !== '' && $v !== null);

    // http_build_query : 배열을 'a=1&b=2' 형태 쿼리문자열로 (한글·특수문자 자동 인코딩).
    return $params ? $path . '?' . http_build_query($params) : $path;
}
