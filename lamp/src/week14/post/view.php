<?php
// ============================================================
// post/view.php — 글 보기  [GET 요청]
//   ?id= 로 글 번호를 받아 글 하나를 보여준다. (홈·board가 링크로 보냄)
//   ★ 글 데이터는 posts 모듈(get_post)에서 가져온다 → board와 '같은 출처'.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/posts.php';

// ── 1) id 받기 ───────────────────────────────────────────────
//   (int) = 정수로 강제 형변환. ?id=abc → 0, ?id=5 → 5. (숫자 아닌 입력 방어)
$id = (int)($_GET['id'] ?? 0);

// ── 2) 그 글 찾기 (posts 모듈에 맡김) ────────────────────────
//   get_post()는 없으면 null을 돌려줌 → null이면 안내 후 종료 (Tester-Doer).
$post = get_post($id);
if ($post === null) {
    $pageTitle = '글을 찾을 수 없음';
    require __DIR__ . '/../includes/header.php';
    echo '<p>존재하지 않는 글입니다. <a href="/">홈으로</a></p>';
    require __DIR__ . '/../includes/footer.php';
    exit;
}

// ── 더미 댓글 목록 (나중 comments 테이블에서 post_id로 조회로 교체) ──
$comments = [
    ['author' => '주주1', 'content' => '저도 그렇게 봅니다.'],
    ['author' => '개미2', 'content' => '음, 신중하게 접근해야 할 듯요.'],
];

$pageTitle = $post['title'];
require __DIR__ . '/../includes/header.php';
?>

  <!-- article = '독립적인 하나의 글'을 뜻하는 의미(시맨틱) 태그. class="post"는 CSS 이름표. -->
  <article class="post">
    <h1><?= e($post['title']) ?></h1>
    <p class="post-meta">
      <!-- span = 인라인 작은 상자(줄바꿈 안 함). tag 클래스로 배지 색 -->
      <span class="tag"><?= e($post['sentiment']) ?></span>
      <?= e($post['stock']) ?> · <?= e($post['author']) ?>
    </p>
    <!-- nl2br(e(...)) : e()로 먼저 안전 처리 → nl2br로 줄바꿈(\n)을 <br>로. (순서 중요) -->
    <div class="post-content"><?= nl2br(e($post['content'])) ?></div>
  </article>

  <!-- section = '주제로 묶인 한 구획'(여기선 댓글 구역) -->
  <section class="comments">
    <h2>댓글</h2>

    <?php // comment/create.php가 ?commented=1 로 리다이렉트해오면 완료 알림 ?>
    <?php if (isset($_GET['commented'])): ?>
      <div class="flash">✅ 댓글이 등록되었습니다. <small>(지금은 저장 안 되는 껍데기)</small></div>
    <?php endif; ?>

    <!-- 더미 댓글 목록 -->
    <ul class="comment-list">
      <?php foreach ($comments as $c): ?>
        <li>
          <span class="comment-author"><?= e($c['author']) ?></span>
          <?= e($c['content']) ?>
        </li>
      <?php endforeach; ?>
    </ul>

    <!-- 댓글 작성 폼 → comment/create.php 로 POST -->
    <form class="comment-form" method="post" action="/comment/create.php">
      <!-- hidden = 화면엔 안 보이지만 함께 전송되는 값. 이 댓글이 '몇 번 글'인지 알려줌. -->
      <input type="hidden" name="post_id" value="<?= e((string)$id) ?>">
      <textarea name="content" rows="3" placeholder="댓글을 입력하세요" required></textarea>
      <button type="submit">댓글 등록</button>
    </form>
  </section>

<?php require __DIR__ . '/../includes/footer.php'; ?>
