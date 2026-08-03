<?php
// ============================================================
// auth/login.php — 로그인 폼  [GET 요청]
//   폼을 보여주기만 한다. 실제 검증은 authenticate.php(POST)가 담당.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

// 이미 로그인했으면 굳이 로그인 화면을 볼 필요 없다 → 홈으로.
if (is_logged_in()) {
    redirect('/');
}

$pageTitle = '로그인';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>로그인</h1>

  <?php // 가입 완료 / 로그인 실패 / 로그인 필요 안내는 header.php가 주소(?flash=)에서 읽어 그린다 ?>

  <!-- 로그인 정보는 민감하므로 반드시 POST (주소에 비밀번호가 남으면 큰일) -->
  <form class="auth-form" method="post" action="/auth/authenticate.php">
    <?= csrf_field() ?>
    <label>아이디
      <input type="text" name="username" required autofocus>
    </label>
    <label>비밀번호
      <!-- type="password" = 입력 글자가 ●●●로 가려진다 -->
      <input type="password" name="password" required>
    </label>
    <button type="submit">로그인</button>
  </form>

  <p class="muted">
    <?php // seed.sql로 만들어진 계정들. 비밀번호는 전부 1234로 같다(시연용). ?>
    테스트 계정: <code>영화광 / 1234</code> · <code>해석러 / 1234</code> · <code>심야극장 / 1234</code><br>
    계정이 없나요? <a href="/auth/signup.php">회원가입</a>
  </p>

<?php require __DIR__ . '/../includes/footer.php'; ?>
