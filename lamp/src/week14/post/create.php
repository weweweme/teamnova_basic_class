<?php
// ============================================================
// post/create.php — 글 저장 처리  [POST 요청 → PRG]
//   write.php 폼이 POST로 보낸 값을 받아서, (지금은 저장한 '척')
//   홈으로 redirect 한다. = 실무 FM의 '액션(처리)' 담당.
// ============================================================
require_once __DIR__ . '/../includes/util.php';

// ── 0) POST로 온 게 맞나? (주소로 직접 들어오는 것 차단) ──────
//   $_SERVER = PHP가 '자동으로' 만들어 채워주는 특별한 내장 배열(= 슈퍼글로벌).
//     선언 안 해도, 함수 안이든 밖이든 어디서나 바로 쓸 수 있다.
//     ($_GET·$_POST도 같은 슈퍼글로벌. PHP 엔진이 요청 때 채워줌)
//     $_SERVER엔 요청/서버 정보가 담김: 메서드·주소·접속 IP 등.
//   $_SERVER['REQUEST_METHOD'] = 이번 요청이 GET인지 POST인지.
//   처리 파일은 POST가 아니면 폼으로 돌려보낸다.
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /post/write.php');
    exit;
}

// ── 1) POST로 온 값 받기 ($_GET이 아니라 $_POST!) ────────────
//   trim() = 앞뒤 공백 제거. ?? '' = 값이 없으면 빈 문자열.
$title     = trim($_POST['title']   ?? '');
$content   = trim($_POST['content'] ?? '');
$sentiment = $_POST['sentiment']    ?? '중립';

// ── 2) 검증: 제목/내용이 비었으면 다시 폼으로 ────────────────
if ($title === '' || $content === '') {
    header('Location: /post/write.php');
    exit;
}

// ── 3) 저장 (지금은 stub) ────────────────────────────────────
//   여기서 DB(posts 테이블)에 INSERT '했다 치고' 넘어간다.
//   (나중 MariaDB 붙이면 이 자리에 실제 저장 코드가 들어감)

// ── 4) PRG: 처리 끝나면 반드시 redirect (GET 페이지로) ───────
//   header() = PHP 내장 함수. 'HTTP 응답 헤더'(브라우저에 주는 지시문)를 보낸다.
//     응답은 [헤더](메타·지시) + [본문](HTML)로 나뉘는데, header()는 [헤더]에 한 줄 넣는 것.
//   'Location: 주소' 헤더 = "브라우저야, 이 주소로 가라" → 브라우저가 그 주소를
//     스스로 다시 요청(= 리다이렉트, 상태코드 302 자동). Java의 response.sendRedirect() 격.
//   ⚠️ header()는 화면(HTML)이 한 글자라도 출력되기 전에 불러야 한다.
//      (헤더가 본문보다 먼저 나가야 해서. 그래서 이 파일엔 위에 HTML·echo·공백이 없다)
//   왜 redirect? 처리 화면을 그대로 보여주면 '새로고침' 시 POST 재전송 → 글 중복 등록.
//     redirect 하면 브라우저가 홈을 GET으로 새로 부르므로, 새로고침해도 안전. = Post-Redirect-Get.
header('Location: /?posted=1');
exit;
