<?php
// ============================================================
// post/view.php — 글 보기  [GET 요청]
//   ?id= 로 글 번호를 받아 글 하나를 보여준다. (홈·게시판이 링크로 보냄)
//   ★ 글 데이터는 posts 모듈(get_post)에서 가져온다 → 게시판과 '같은 출처'.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';   // 로그인·소유권에 따라 화면이 달라지므로
require_once __DIR__ . '/../includes/posts.php';

// ── 1) id 받기 ───────────────────────────────────────────────
//   (int) = 정수로 강제 형변환. ?id=abc → 0, ?id=5 → 5. (숫자 아닌 입력 방어)
$id = get_int('id', 0);

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
    ['id' => 101, 'author' => '리뷰러', 'content' => '저도 그렇게 봤어요.'],
    ['id' => 102, 'author' => '영화광', 'content' => '공감합니다. 저도 그 장면이 인상 깊었어요.'],
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
      <!-- 어느 작품 글인지 → 그 작품 게시판으로 이동 -->
      <a href="/board/?work=<?= e($post['work']) ?>"><?= e($post['workTitle']) ?></a> ·
      <!-- 작성자 이름을 누르면 그 사람의 프로필로 (GET으로 user 전달) -->
      <a href="/profile.php?user=<?= e($post['author']) ?>"><?= e($post['author']) ?></a>
    </p>
    <!-- nl2br(e(...)) : e()로 먼저 안전 처리 → nl2br로 줄바꿈(\n)을 <br>로. (순서 중요) -->
    <div class="post-content"><?= nl2br(e($post['content'])) ?></div>
  </article>

  <?php // 추천·신고·수정 처리 후 리다이렉트해오면 완료 알림 ?>
  <?php if (isset($_GET['liked'])): ?>
    <div class="flash">👍 추천했습니다. <small>(지금은 숫자가 실제로 늘진 않는 껍데기)</small></div>
  <?php endif; ?>
  <?php if (isset($_GET['reported'])): ?>
    <div class="flash">🚩 신고가 접수되었습니다. <small>(지금은 저장 안 되는 껍데기)</small></div>
  <?php endif; ?>
  <?php if (isset($_GET['updated'])): ?>
    <div class="flash">✏️ 글이 수정되었습니다. <small>(지금은 반영 안 되는 껍데기)</small></div>
  <?php endif; ?>
  <?php // 남의 글을 수정·삭제하려다 서버에 막히면 여기로 돌아온다 ?>
  <?php if (isset($_GET['denied'])): ?>
    <div class="flash-error">🔒 본인이 쓴 글만 수정·삭제할 수 있습니다.</div>
  <?php endif; ?>

  <!-- 글에 대한 '행동'들 — 상태를 바꾸는 것은 링크가 아니라 POST 폼 -->
  <div class="post-actions">

    <?php if (is_logged_in()): ?>

      <!-- 추천: hidden으로 어느 글인지(post_id)를 함께 보낸다 -->
      <form class="like-form" method="post" action="/like/toggle.php">
        <input type="hidden" name="post_id" value="<?= e((string)$id) ?>">
        <button type="submit">👍 추천 <?= e((string)$post['likes']) ?></button>
      </form>

      <!-- 신고 버튼: 누르면 아래 팝업만 연다. type="button" = 폼 제출용이 아님 -->
      <button type="button" class="btn-report" id="report-open">🚩 신고</button>

      <?php // ★ 수정·삭제는 '내가 쓴 글'에만 보여준다 (소유권).
            //   단, 화면에서 숨기는 건 편의일 뿐 — 서버(edit/update/delete)에서도 다시 확인한다. ?>
      <?php if (is_owner($post['author'])): ?>

        <!-- 수정: '수정 폼 화면으로 이동'하는 것이므로 GET 링크가 맞다. -->
        <a class="btn-edit" href="/post/edit.php?id=<?= e((string)$id) ?>">✏️ 수정</a>

        <!-- 삭제: 되돌릴 수 없는 동작이라 반드시 POST 폼.
             class="delete-form" 을 보고 JS가 '정말 삭제할까요?' 확인창을 띄운다. -->
        <form class="delete-form" method="post" action="/post/delete.php">
          <input type="hidden" name="id" value="<?= e((string)$id) ?>">
          <button type="submit" class="btn-delete">🗑 삭제</button>
        </form>

      <?php endif; ?>

    <?php else: ?>
      <!-- 비로그인: 버튼 대신 안내 (서버에서도 막히지만, 미리 알려주는 게 친절) -->
      <p class="muted"><a href="/auth/login.php">로그인</a>하면 추천·신고·댓글을 남길 수 있어요.</p>
    <?php endif; ?>

  </div>

  <!-- 신고 팝업 — <dialog> = HTML이 기본으로 제공하는 '모달 창' 태그.
       평소엔 숨겨져 있다가 JS의 showModal()로 열린다.
       ★ 팝업은 '보여주는 방식'만 바꾼 것이고, 실제 신고는 이 안의 폼이 POST로 보낸다. -->
  <dialog id="report-dialog" class="modal">
    <form method="post" action="/report/create.php">
      <h3>신고하기</h3>
      <p class="muted">신고 사유를 선택해 주세요.</p>

      <input type="hidden" name="post_id" value="<?= e((string)$id) ?>">

      <!-- select = 여러 선택지 중 하나를 고르는 드롭다운.
           option의 value 가 실제로 전송되는 값 → $_POST['reason'] -->
      <select name="reason">
        <option value="스팸/광고">스팸/광고</option>
        <option value="욕설/비방">욕설/비방</option>
        <option value="스포일러">스포일러</option>
        <option value="기타">기타</option>
      </select>

      <div class="modal-actions">
        <!-- ★ 취소 버튼은 반드시 type="button" !
             <button>의 type 기본값은 submit 이라서, '폼 안'에 있는 버튼은
             type을 안 적으면 누르는 순간 폼이 전송된다. -->
        <button type="button" id="report-cancel" class="btn-cancel">취소</button>

        <!-- 이쪽이 진짜 제출 버튼 -->
        <button type="submit" class="btn-danger">신고</button>
      </div>
    </form>
  </dialog>

  <!-- section = '주제로 묶인 한 구획'(여기선 댓글 구역) -->
  <section class="comments">
    <h2>댓글</h2>

    <?php if (isset($_GET['commented'])): ?>
      <div class="flash">✅ 댓글이 등록되었습니다. <small>(지금은 저장 안 되는 껍데기)</small></div>
    <?php endif; ?>
    <?php if (isset($_GET['cdeleted'])): ?>
      <div class="flash">🗑 댓글이 삭제되었습니다. <small>(지금은 실제로 지워지진 않는 껍데기)</small></div>
    <?php endif; ?>

    <!-- 더미 댓글 목록 -->
    <ul class="comment-list">
      <?php foreach ($comments as $c): ?>
        <li>
          <span class="comment-author"><?= e($c['author']) ?></span>
          <?= e($c['content']) ?>
          <?php // 댓글도 '내가 쓴 것'만 삭제 버튼을 보여준다 ?>
          <?php if (is_owner($c['author'])): ?>
            <!-- 댓글 삭제: 어느 댓글인지(comment_id)와 돌아갈 글(post_id)을 함께 보낸다 -->
            <form class="delete-form comment-delete" method="post" action="/comment/delete.php">
              <input type="hidden" name="comment_id" value="<?= e((string)$c['id']) ?>">
              <input type="hidden" name="post_id" value="<?= e((string)$id) ?>">
              <button type="submit">삭제</button>
            </form>
          <?php endif; ?>
        </li>
      <?php endforeach; ?>
    </ul>

    <?php if (is_logged_in()): ?>
      <!-- 댓글 작성 폼 → comment/create.php 로 POST
           ★ 작성자는 폼에 없다! 서버가 세션에서 가져온다(위조 방지). -->
      <form class="comment-form" method="post" action="/comment/create.php">
        <!-- hidden = 화면엔 안 보이지만 함께 전송되는 값. 이 댓글이 '몇 번 글'인지 알려줌. -->
        <input type="hidden" name="post_id" value="<?= e((string)$id) ?>">
        <textarea name="content" rows="3" maxlength="500" placeholder="댓글을 입력하세요" required></textarea>
        <button type="submit">댓글 등록</button>
      </form>
    <?php else: ?>
      <p class="muted"><a href="/auth/login.php">로그인</a> 후 댓글을 남길 수 있어요.</p>
    <?php endif; ?>
  </section>

<?php require __DIR__ . '/../includes/footer.php'; ?>
