<?php
// ============================================================
// post/create.php — 글 저장 처리  [POST 요청 → PRG]
//   write.php 폼이 POST로 보낸 값을 받아서, (지금은 저장한 '척')
//   그 작품 게시판으로 redirect 한다. = 실무 FM의 '액션(처리)' 담당.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/works.php';   // 작품이 실제로 있는지 확인하려고
require_once __DIR__ . '/../includes/posts.php';   // 길이 제한 상수(POST_TITLE_MAX 등)

// ★ 로그인 필수 — 화면에서 버튼을 숨겨도 요청은 조작할 수 있으므로
//   '처리하는 쪽'에서 반드시 다시 확인한다. (안 했으면 로그인 페이지로 보내고 중단)
require_login();

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
$work      = post_str('work', '');   // 어느 작품 글인지
$title     = trim(post_str('title'));
$content   = trim(post_str('content'));
$sentiment = post_str('sentiment', '보통');

// 감상 값은 허용된 것만 (화이트리스트)
if (!in_array($sentiment, ['호평', '보통', '혹평'], true)) {
    $sentiment = '보통';
}

// ── 2) 검증: 제목/내용이 비었으면 다시 폼으로 ────────────────
if ($title === '' || $content === '') {
    header('Location: /post/write.php');
    exit;
}
// 길이 제한 (mb_strlen = 한글도 1글자로 정확히 세는 글자 수)
//   ★ 브라우저의 maxlength는 개발자도구로 지울 수 있으므로 서버에서 반드시 확인.
if (mb_strlen($title) > POST_TITLE_MAX || mb_strlen($content) > POST_CONTENT_MAX) {
    header('Location: /post/write.php?toolong=1');
    exit;
}
// 작품 검증: 실제로 존재하는 작품이어야 한다.
//   (select엔 3개뿐이지만, 요청을 조작하면 아무 값이나 보낼 수 있으므로 서버에서 확인)
if (get_work($work) === null) {
    header('Location: /post/write.php');
    exit;
}

// ── 3) 저장 ──────────────────────────────────────────────────
//   ★ 작성자는 폼에서 받지 않는다! 세션에 있는 '지금 로그인한 사람'을 쓴다.
//     폼으로 받으면 남의 이름으로 글을 쓸 수 있기 때문(위조 방지).
$author    = current_user();
$workTitle = get_work($work)['title'];

//   임시 보관함(세션)에 저장한다. 나중 DB를 붙이면 이 한 줄이 INSERT로 바뀐다.
add_post($work, $workTitle, $title, $content, $sentiment, (string)$author);

// ── 4) PRG: 처리 끝나면 반드시 redirect (GET 페이지로) ───────
//   header() = PHP 내장 함수. 'HTTP 응답 헤더'(브라우저에 주는 지시문)를 보낸다.
//   'Location: 주소' 헤더 = "브라우저야, 이 주소로 가라" → 리다이렉트(302 자동).
//   ⚠️ header()는 화면(HTML)이 한 글자라도 출력되기 전에 불러야 한다.
//   왜 redirect? 처리 화면을 그대로 보여주면 '새로고침' 시 POST 재전송 → 글 중복 등록.
//   글은 '그 작품'에 속하므로, 홈이 아니라 그 작품 게시판으로 돌려보낸다.
header('Location: /board/?work=' . urlencode($work) . '&posted=1');
exit;
