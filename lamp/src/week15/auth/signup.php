<?php
// ============================================================
// auth/signup.php — 회원가입 폼  [GET 요청]
//   폼을 보여주기만 한다. 실제 가입 처리는 register.php(POST)가 담당.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

if (is_logged_in()) {
    header('Location: /');
    exit;
}

$pageTitle = '회원가입';
require __DIR__ . '/../includes/header.php';
?>

  <h1>회원가입</h1>

  <?php // 가입 실패 안내는 header.php가 세션에서 꺼내 그린다 ?>

  <form class="auth-form" method="post" action="/auth/register.php">
    <label>아이디
      <input type="text" name="username" required autofocus>
    </label>
    <label>비밀번호
      <!-- minlength = 최소 글자 수(브라우저 1차 검사). 서버에서도 다시 확인한다. -->
      <input type="password" name="password" required minlength="4">
    </label>
    <button type="submit">가입하기</button>
  </form>

  <p class="muted">
    ※ 지금은 저장할 DB가 없어 <strong>실제로 계정이 만들어지진 않아요</strong>(껍데기).<br>
    이미 계정이 있나요? <a href="/auth/login.php">로그인</a>
  </p>

<?php require __DIR__ . '/../includes/footer.php'; ?>
