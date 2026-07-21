<?php
// ============================================================
// post/edit.php — 글 수정 폼  [GET 요청]
//   ?id= 로 기존 글을 불러와 폼에 '미리 채워서' 보여준다.
//   실제 수정 저장은 update.php(POST)가 담당 — 뷰/액션 분리.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/posts.php';

// ── 1) 수정할 글 찾기 ────────────────────────────────────────
$id   = (int)($_GET['id'] ?? 0);
$post = get_post($id);

if ($post === null) {
    $pageTitle = '글을 찾을 수 없음';
    require __DIR__ . '/../includes/header.php';
    echo '<p>존재하지 않는 글입니다. <a href="/">홈으로</a></p>';
    require __DIR__ . '/../includes/footer.php';
    exit;
}

$sentiments = ['매수', '중립', '매도'];

$pageTitle = '글 수정';
require __DIR__ . '/../includes/header.php';
?>

  <h1>글 수정</h1>

  <form class="write-form" method="post" action="/post/update.php">

    <!-- 어느 글을 수정하는지 서버에 알려준다 (화면엔 안 보이지만 함께 전송) -->
    <input type="hidden" name="id" value="<?= e((string)$id) ?>">

    <label>제목
      <!-- input은 value="..." 속성에 기존 값을 넣어 '미리 채운다' -->
      <input type="text" name="title" value="<?= e($post['title']) ?>" required>
    </label>

    <label>내용
      <!-- ★ textarea는 value 속성이 없다!
           여는 태그와 닫는 태그 '사이'에 넣어야 미리 채워진다.
           그리고 사이의 공백·줄바꿈이 그대로 내용이 되므로,
           <textarea ...> 바로 뒤에 붙여 쓰고 </textarea>도 붙여 닫는다. -->
      <textarea name="content" rows="6" required><?= e($post['content']) ?></textarea>
    </label>

    <fieldset>
      <legend>투자 심리</legend>
      <?php foreach ($sentiments as $s): ?>
        <!-- 기존에 골랐던 값이면 checked를 붙여 '미리 선택'해 둔다 -->
        <label>
          <input type="radio" name="sentiment" value="<?= e($s) ?>"
                 <?= $post['sentiment'] === $s ? 'checked' : '' ?>>
          <?= e($s) ?>
        </label>
      <?php endforeach; ?>
    </fieldset>

    <button type="submit">수정 완료</button>
  </form>

<?php require __DIR__ . '/../includes/footer.php'; ?>
