<?php
// ============================================================
// post/create.php — 글 저장 처리  [POST 요청 → PRG]
//   write.php 폼이 POST로 보낸 값을 받아서, (지금은 저장한 '척')
//   그 작품 게시판으로 redirect 한다. = 실무 FM의 '액션(처리)' 담당.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/works.php';   // 작품이 실제로 있는지 확인하려고

// ── 0) POST로 온 게 맞나? (주소로 직접 들어오는 것 차단) ──────
//   $_SERVER = PHP가 '자동으로' 만들어 채워주는 특별한 내장 배열(= 슈퍼글로벌).
//     선언 안 해도, 함수 안이든 밖이든 어디서나 바로 쓸 수 있다.
//     ($_GET·$_POST도 같은 슈퍼글로벌. PHP 엔진이 요청 때 채워줌)
//   $_SERVER['REQUEST_METHOD'] = 이번 요청이 GET인지 POST인지.
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /post/write.php');
    exit;
}

// ── 1) POST로 온 값 받기 ($_GET이 아니라 $_POST!) ────────────
//   trim() = 앞뒤 공백 제거. ?? '' = 값이 없으면 빈 문자열.
$work      = $_POST['work']         ?? '';   // 어느 작품 글인지
$title     = trim($_POST['title']   ?? '');
$content   = trim($_POST['content'] ?? '');
$sentiment = $_POST['sentiment']    ?? '보통';

// 감상 값은 허용된 것만 (화이트리스트)
if (!in_array($sentiment, ['호평', '보통', '혹평'], true)) {
    $sentiment = '보통';
}

// ── 2) 검증: 제목/내용이 비었으면 다시 폼으로 ────────────────
if ($title === '' || $content === '') {
    header('Location: /post/write.php');
    exit;
}
// 작품 검증: 실제로 존재하는 작품이어야 한다.
//   (select엔 3개뿐이지만, 요청을 조작하면 아무 값이나 보낼 수 있으므로 서버에서 확인)
if (get_work($work) === null) {
    header('Location: /post/write.php');
    exit;
}

// ── 3) 저장 (지금은 stub) ────────────────────────────────────
//   여기서 DB(posts 테이블)에 INSERT '했다 치고' 넘어간다.

// ── 4) PRG: 처리 끝나면 반드시 redirect (GET 페이지로) ───────
//   header() = PHP 내장 함수. 'HTTP 응답 헤더'(브라우저에 주는 지시문)를 보낸다.
//   'Location: 주소' 헤더 = "브라우저야, 이 주소로 가라" → 리다이렉트(302 자동).
//   ⚠️ header()는 화면(HTML)이 한 글자라도 출력되기 전에 불러야 한다.
//   왜 redirect? 처리 화면을 그대로 보여주면 '새로고침' 시 POST 재전송 → 글 중복 등록.
//   글은 '그 작품'에 속하므로, 홈이 아니라 그 작품 게시판으로 돌려보낸다.
header('Location: /board/?work=' . urlencode($work) . '&posted=1');
exit;
