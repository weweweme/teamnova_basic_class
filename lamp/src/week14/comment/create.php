<?php
// ============================================================
// comment/create.php — 댓글 저장 처리  [POST 요청 → PRG]
//   view.php의 댓글 폼이 POST로 보낸 값을 받아 (지금은 저장한 '척')
//   '그 글'로 다시 리다이렉트한다. (write→create와 같은 PRG 패턴)
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

// ★ 로그인 필수 — 화면에서 버튼을 숨겨도 요청은 조작할 수 있으므로
//   '처리하는 쪽'에서 반드시 다시 확인한다. (안 했으면 로그인 페이지로 보내고 중단)
require_login();

// ── 0) POST로 온 게 맞나? 아니면 홈으로 ──────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 값 받기 ───────────────────────────────────────────────
//   post_id = 이 댓글이 달릴 '글 번호' (폼의 hidden으로 넘어옴).
//   (int)로 정수 강제 → 숫자 아닌 값 방어.
$postId  = (int)($_POST['post_id'] ?? 0);
$content = trim($_POST['content'] ?? '');

// ── 2) 검증: 글 번호가 없거나 내용이 비면 되돌린다 ───────────
if ($postId <= 0 || $content === '') {
    // 글 번호가 있으면 그 글로, 없으면 홈으로.
    //   "..$postId.." → 큰따옴표 안에서는 $변수가 값으로 '치환'된다(문자열 보간).
    $back = $postId > 0 ? "/post/view.php?id=$postId" : '/';
    header("Location: $back");
    exit;
}

// ── 3) 저장 (지금은 stub) ────────────────────────────────────
//   ★ 작성자는 폼이 아니라 세션에서 가져온다 (남의 이름으로 쓰는 위조 방지).
$author = current_user();

//   나중 comments 테이블에 INSERT '했다 치고' 넘어간다. (post_id, content, author)

// ── 4) PRG: '그 글'로 다시 리다이렉트 (+댓글 완료 표시) ───────
//   글쓰기는 홈으로 갔지만, 댓글은 '방금 그 글'로 돌아가야 자연스럽다.
//   그래서 post_id를 리다이렉트 주소에 넣어 동적으로 목적지를 만든다.
header("Location: /post/view.php?id=$postId&commented=1");
exit;
