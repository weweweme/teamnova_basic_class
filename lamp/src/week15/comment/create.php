<?php
// ============================================================
// comment/create.php — 댓글 저장 처리  [POST 요청 → PRG]
//   view.php의 댓글 폼이 POST로 보낸 값을 받아 (지금은 저장한 '척')
//   '그 글'로 다시 리다이렉트한다. (write→create와 같은 PRG 패턴)
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/comments.php';       // add_comment(), COMMENT_MAX
require_once __DIR__ . '/../includes/notifications.php';   // 글 작성자에게 알림

// ★ 로그인 필수 — 화면에서 버튼을 숨겨도 요청은 조작할 수 있으므로
//   '처리하는 쪽'에서 반드시 다시 확인한다. (안 했으면 로그인 페이지로 보내고 중단)
require_login();

// ── 0) POST로 온 게 맞나? 아니면 홈으로 ──────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    redirect('/');
}

// ── 1) 값 받기 ───────────────────────────────────────────────
//   post_id = 이 댓글이 달릴 '글 번호' (폼의 hidden으로 넘어옴).
//   (int)로 정수 강제 → 숫자 아닌 값 방어.
$postId  = post_int('post_id', 0);
$content = trim(post_str('content'));

// ── 2) 검증: 글 번호가 없거나 내용이 비거나 너무 길면 되돌린다 ──
//   (COMMENT_MAX는 comments 모듈에 정의돼 있다)
if ($postId <= 0 || $content === '' || mb_strlen($content) > COMMENT_MAX) {
    // 글 번호가 있으면 그 글로, 없으면 홈으로.
    if ($postId > 0) {
        redirect('/post/view.php', ['id' => $postId]);
    }
    redirect('/');
}

// ── 3) 저장 ──────────────────────────────────────────────────
//   ★ 작성자는 폼이 아니라 current_user()에서 가져온다 (남의 이름으로 쓰는 위조 방지).
//     주소로 온 신원을 users 표에서 확인한 값이라, 폼에 뭘 적어 보내든 소용없다.
$author = current_user();

$commentId = add_comment($postId, (string)$author, $content);

// ── 3-1) 글 작성자에게 알림 (내 글에 남이 댓글 단 경우만) ────
//   글 주인의 id를 찾아, 댓글 단 사람(나)과 다르면 알림을 남긴다.
//   (create_notification 안에서도 '자기 자신'은 한 번 더 걸러낸다)
$recipientId = (int) db_scalar('SELECT author_id FROM posts WHERE id = ?', [$postId]);
create_notification($recipientId, current_user_id(), $postId, $commentId);

// ── 4) PRG: '그 글'로 다시 리다이렉트 (+댓글 완료 표시) ───────
//   글쓰기는 홈으로 갔지만, 댓글은 '방금 그 글'로 돌아가야 자연스럽다.
//   그래서 post_id를 리다이렉트 주소에 넣어 동적으로 목적지를 만든다.
set_flash('✅ 댓글이 등록되었습니다.');
redirect('/post/view.php', ['id' => $postId]);
