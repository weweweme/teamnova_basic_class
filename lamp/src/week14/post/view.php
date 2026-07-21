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

  <?php // 추천·신고 처리 후 리다이렉트해오면 완료 알림 ?>
  <?php if (isset($_GET['liked'])): ?>
    <div class="flash">👍 추천했습니다. <small>(지금은 숫자가 실제로 늘진 않는 껍데기)</small></div>
  <?php endif; ?>
  <?php if (isset($_GET['reported'])): ?>
    <div class="flash">🚩 신고가 접수되었습니다. <small>(지금은 저장 안 되는 껍데기)</small></div>
  <?php endif; ?>
  <?php if (isset($_GET['updated'])): ?>
    <div class="flash">✏️ 글이 수정되었습니다. <small>(지금은 반영 안 되는 껍데기)</small></div>
  <?php endif; ?>

  <!-- 글에 대한 '행동'들 — 둘 다 서버 상태를 바꾸므로 링크가 아니라 POST 폼 -->
  <div class="post-actions">

    <!-- 추천: hidden으로 어느 글인지(post_id)를 함께 보낸다 -->
    <form class="like-form" method="post" action="/like/toggle.php">
      <input type="hidden" name="post_id" value="<?= e((string)$id) ?>">
      <button type="submit">👍 추천 <?= e((string)$post['likes']) ?></button>
    </form>

    <!-- 신고 버튼: 누르면 아래 팝업만 연다. (실제 전송은 팝업 안의 폼이 담당)
         type="button" = "이 버튼은 폼 제출용이 아니다"라는 표시.
         (이 버튼은 폼 밖에 있어서 없어도 문제는 없지만, 의도를 분명히 해둔다) -->
    <button type="button" class="btn-report" id="report-open">🚩 신고</button>

    <!-- 수정: '수정 폼 화면으로 이동'하는 것이므로 GET 링크가 맞다.
         (실제 수정 저장은 그 폼이 POST로 보낸다 — 조회는 GET / 변경은 POST) -->
    <a class="btn-edit" href="/post/edit.php?id=<?= e((string)$id) ?>">✏️ 수정</a>

  </div>

  <!-- 신고 팝업 — <dialog> = HTML이 기본으로 제공하는 '모달 창' 태그.
       평소엔 숨겨져 있다가 JS의 showModal()로 열린다.
       ★ 팝업은 '보여주는 방식'만 바꾼 것이고,
         실제 신고는 여전히 이 안의 폼이 POST로 보낸다 (흐름은 그대로). -->
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
        <option value="허위정보">허위정보</option>
        <option value="기타">기타</option>
      </select>

      <div class="modal-actions">
        <!-- ★ 취소 버튼은 반드시 type="button" !
             <button>의 type 기본값은 submit 이라서, '폼 안'에 있는 버튼은
             type을 안 적으면 누르는 순간 폼이 전송된다.
             → 여기서 type을 빼면 '취소'를 눌렀는데 신고가 접수되는 사고가 난다. -->
        <button type="button" id="report-cancel" class="btn-cancel">취소</button>

        <!-- 이쪽이 진짜 제출 버튼 (기본값이 submit이라 생략 가능하지만 의도를 명시) -->
        <button type="submit" class="btn-danger">신고</button>
      </div>
    </form>
  </dialog>

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
