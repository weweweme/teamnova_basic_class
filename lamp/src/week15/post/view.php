<?php
// ============================================================
// post/view.php — 글 보기  [GET 요청]
//   ?id= 로 글 번호를 받아 글 하나를 보여준다. (홈·게시판이 링크로 보냄)
//   ★ 글 데이터는 posts 모듈(get_post)에서 가져온다 → 게시판과 '같은 출처'.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';   // 로그인·소유권에 따라 화면이 달라지므로
require_once __DIR__ . '/../includes/posts.php';
require_once __DIR__ . '/../includes/comments.php';   // 이 글의 댓글 목록

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

// ── 이 글의 댓글 목록 (comments 모듈이 더미 + 이번 접속에 쓴 것을 합쳐서 준다) ──
$comments = get_comments($id);

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
      <?= level_badge_html((int)$post['authorPostCount']) ?>
      <a href="/profile/?user=<?= e($post['author']) ?>"><?= e($post['authorNick']) ?></a>
    </p>
    <!-- nl2br(e(...)) : e()로 먼저 안전 처리 → nl2br로 줄바꿈(\n)을 <br>로. (순서 중요) -->
    <div class="post-content"><?= nl2br(e($post['content'])) ?></div>
  </article>

  <?php // 추천·신고·수정·권한거부 알림은 header.php가 주소(?flash=)에서 읽어 그린다 (set_flash) ?>

  <!-- 글에 대한 '행동'들 — 상태를 바꾸는 것은 링크가 아니라 POST 폼 -->
  <div class="post-actions">

    <?php if (is_logged_in()): ?>

      <!-- 추천: hidden으로 어느 글인지(post_id)를 함께 보낸다.
           ★ 1인 1회 — 이미 눌렀으면 버튼이 채워지고, 다시 누르면 취소된다. -->
      <form class="like-form" method="post" action="/like/toggle.php">
        <input type="hidden" name="post_id" value="<?= e((string)$id) ?>">
        <button type="submit" class="<?= has_liked($id) ? 'liked' : '' ?>">
          <?= has_liked($id) ? '👍 추천함' : '👍 추천' ?> <?= e((string)$post['likes']) ?>
        </button>
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

    <?php if (!$comments): ?>
      <p class="muted">아직 댓글이 없습니다. 첫 댓글을 남겨보세요!</p>
    <?php endif; ?>

    <?php
    // 답글 폼을 '어느 댓글 아래' 열어둘지를 주소로 정한다 → /post/view.php?id=3&reply=7
    //   ★ JS 없이 서버가 그린다. 이 프로젝트가 신원(?as=)·알림(?flash=)을 주소로 나르는 것과 같은 방식.
    //     "지금 화면이 어떤 상태인지"가 주소에 그대로 드러나서, 새로고침해도 그 상태가 유지된다.
    $replyTo = get_int('reply');
    ?>

    <!-- 댓글 목록 (원댓글과 답글이 한 배열에 섞여 있고, 이미 부모-자식 순으로 정렬돼 있다) -->
    <ul class="comment-list">
      <?php foreach ($comments as $c): ?>
        <?php
        // parentId가 있으면 답글 → 들여쓰기 클래스를 준다.
        $isReply = $c['parentId'] !== null;
        // 답글을 달거나 취소했을 때 그 자리로 되돌아오도록 앵커(#c7)를 붙인다.
        $anchor    = '#c' . $c['id'];
        $replyUrl  = query_url('/post/view.php', ['reply' => $c['id'], 'edit' => null]) . $anchor;
        $cancelUrl = query_url('/post/view.php', ['reply' => null,     'edit' => null]) . $anchor;
        ?>
        <li id="c<?= e((string)$c['id']) ?>" class="<?= $isReply ? 'comment-reply' : '' ?>">

          <?php if ($c['isDeleted']): ?>
            <?php // 답글이 남아 있어서 자리만 지키는 원댓글 (내용은 서버가 이미 비워서 보냈다) ?>
            <span class="muted">삭제된 댓글입니다</span>
          <?php else: ?>
            <span class="comment-author"><?= level_badge_html((int)$c['authorPostCount']) ?> <?= e($c['authorNick']) ?></span>
            <?= e($c['content']) ?>
            <?php if ($c['editedAt'] !== null): ?>
              <?php // 몰래 말을 바꾸지 못하게 수정 사실을 드러낸다 ?>
              <span class="muted comment-edited">(수정됨)</span>
            <?php endif; ?>

            <?php // 답글 버튼은 '원댓글'에만 — 답글의 답글은 만들지 않는다(깊이 1단계) ?>
            <?php if (!$isReply && is_logged_in()): ?>
              <a class="comment-action" href="<?= e($replyUrl) ?>">답글</a>
            <?php endif; ?>

            <?php // 댓글도 '내가 쓴 것'만 삭제 버튼을 보여준다 ?>
            <?php if (is_owner($c['author'])): ?>
              <!-- 댓글 삭제: 어느 댓글인지(comment_id)와 돌아갈 글(post_id)을 함께 보낸다 -->
              <form class="delete-form comment-delete" method="post" action="/comment/delete.php">
                <input type="hidden" name="comment_id" value="<?= e((string)$c['id']) ?>">
                <input type="hidden" name="post_id" value="<?= e((string)$id) ?>">
                <button type="submit">삭제</button>
              </form>
            <?php endif; ?>
          <?php endif; ?>

          <?php // 이 댓글에 '답글 달기'를 눌러둔 상태라면 그 자리에 폼을 편다 ?>
          <?php if ($replyTo === (int)$c['id'] && !$isReply && !$c['isDeleted'] && is_logged_in()): ?>
            <form class="comment-form comment-reply-form" method="post" action="/comment/create.php">
              <input type="hidden" name="post_id" value="<?= e((string)$id) ?>">
              <?php // 누구에게 다는 답글인지. 서버가 resolve_parent_id()로 한 번 더 검사한다. ?>
              <input type="hidden" name="parent_id" value="<?= e((string)$c['id']) ?>">
              <textarea name="content" rows="2" maxlength="500"
                        placeholder="<?= e($c['authorNick']) ?>님에게 답글" required></textarea>
              <button type="submit">답글 등록</button>
              <a class="comment-action" href="<?= e($cancelUrl) ?>">취소</a>
            </form>
          <?php endif; ?>
        </li>
      <?php endforeach; ?>
    </ul>

    <?php if (is_logged_in()): ?>
      <!-- 댓글 작성 폼 → comment/create.php 로 POST
           ★ 작성자는 폼에 없다! 서버가 current_user()로 직접 알아낸다(위조 방지). -->
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
