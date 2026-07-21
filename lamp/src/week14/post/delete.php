<?php
// ============================================================
// post/delete.php — 글 삭제 처리  [POST 요청 → PRG]
//
//   ★ 삭제를 절대 GET 링크로 만들면 안 되는 이유 (유명한 사고 사례)
//     <a href="/post/delete.php?id=3">삭제</a> 처럼 만들면,
//     검색엔진 크롤러나 브라우저의 '미리 불러오기'가 그 링크를 훑기만 해도
//     글이 지워진다. 실제로 이런 식으로 사이트 글이 전멸한 사고가 여러 번 있었다.
//     → 상태를 바꾸는 동작(특히 삭제)은 반드시 POST.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/posts.php';

// ★ 로그인 필수 — 화면에서 버튼을 숨겨도 요청은 조작할 수 있으므로
//   '처리하는 쪽'에서 반드시 다시 확인한다. (안 했으면 로그인 페이지로 보내고 중단)
require_login();

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

// ── 1) 값 받기 + 대상 글 찾기 ────────────────────────────────
$id   = post_int('id', 0);
$post = get_post($id);

// ── 2) 검증: 실제로 있는 글인지 확인 ─────────────────────────
if ($id <= 0 || $post === null) {
    header('Location: /');
    exit;
}

// ★ 소유권 확인: 남의 글은 삭제할 수 없다 (요청 조작 방어)
if (!is_owner($post['author'])) {
    header('Location: /post/view.php?id=' . $id . '&denied=1');
    exit;
}

// ── 3) 삭제 ──────────────────────────────────────────────────
//   임시 보관함(세션)에 '지운 글 번호'를 기록해 목록에서 빠지게 한다.
//   나중엔 이 한 줄이 DELETE FROM posts WHERE id = ? 로 바뀐다.
delete_post($id);

// ── 4) PRG: 삭제된 글로는 돌아갈 수 없으니 그 작품 게시판으로 ──
//   (삭제 전에 $post에서 work를 미리 꺼내둔 덕분에 어디로 갈지 알 수 있다)
header('Location: /board/?work=' . urlencode($post['work']) . '&deleted=1');
exit;
