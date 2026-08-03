<?php
// ============================================================
// post/edit.php — 글 수정 폼  [GET 요청]
//   ?id= 로 기존 글을 불러와 폼에 '미리 채워서' 보여준다.
//   실제 수정 저장은 update.php(POST)가 담당 — 뷰/액션 분리.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/posts.php';
require_once __DIR__ . '/../includes/works.php';   // 어느 작품 글인지 포스터로 보여주려고

// ★ 로그인해야 수정 화면에 들어올 수 있다.
require_login();

// 글쓰기 화면과 같은 좁은 중앙 컬럼(760px) — 제목·포스터·폼이 같은 왼쪽 선에 정렬되게.
//   이게 없으면 폼만 가운데로 모이고 제목·부제는 넓은 컨테이너 왼쪽 끝에 붙어 어긋나 보인다.
$containerClass = 'narrow';

// ── 1) 수정할 글 찾기 ────────────────────────────────────────
$id   = get_int('id', 0);
$post = get_post($id);

if ($post === null) {
    $pageTitle = '글을 찾을 수 없음';
    require __DIR__ . '/../includes/header.php';
    echo '<p>존재하지 않는 글입니다. <a href="/">홈으로</a></p>';
    require __DIR__ . '/../includes/footer.php';
    exit;
}

// ★ 소유권 확인: 남의 글은 수정할 수 없다.
//   화면에서 '수정' 버튼을 안 보여주는 것만으론 부족하다 —
//   주소(/post/edit.php?id=3)를 직접 쳐서 들어올 수 있으므로 여기서 막는다.
if (!is_owner($post['author'])) {
    set_flash('본인이 쓴 글만 수정·삭제할 수 있습니다.', 'error');
    redirect('/post/view.php', ['id' => $id]);
}

$sentiments = ['호평', '보통', '혹평'];

// 어느 작품 글인지 — 포스터를 보여주려고 작품 정보를 가져온다(글쓰기 화면과 동일).
//   DB에 없으면 TMDB에서 받아온다. 못 찾으면 포스터 없이 제목만 쓴다.
$workInfo = get_work($post['work']);

$pageTitle = '글 수정';
require __DIR__ . '/../includes/header.php';
?>

  <h1>글 수정</h1>

  <!-- 어느 작품의 글을 고치는 중인지 한눈에 (글쓰기 화면과 같은 조각) -->
  <div class="write-context">
    <?php if (!empty($workInfo['poster_url'])): ?>
      <img class="write-context-poster" src="<?= e($workInfo['poster_url']) ?>" alt="" loading="lazy">
    <?php endif; ?>
    <div>
      <span class="muted">리뷰 수정</span>
      <strong><?= e($post['workTitle']) ?></strong>
    </div>
  </div>

  <form class="write-form" method="post" action="/post/update.php">
    <?= csrf_field() ?>

    <!-- 어느 글을 수정하는지 서버에 알려준다 (화면엔 안 보이지만 함께 전송) -->
    <input type="hidden" name="id" value="<?= e((string)$id) ?>">

    <label>제목
      <!-- input은 value="..." 속성에 기존 값을 넣어 '미리 채운다' -->
      <input type="text" name="title" maxlength="100" value="<?= e($post['title']) ?>" required>
    </label>

    <label>내용
      <!-- ★ textarea는 value 속성이 없다!
           여는 태그와 닫는 태그 '사이'에 넣어야 미리 채워진다.
           그리고 사이의 공백·줄바꿈이 그대로 내용이 되므로 붙여서 쓴다. -->
      <textarea name="content" rows="6" maxlength="5000" required><?= e($post['content']) ?></textarea>
    </label>

    <fieldset>
      <legend>감상</legend>
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
