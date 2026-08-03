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
    <!-- 로그인 유지 (week16 쿠키 주제)
         세션은 브라우저를 닫으면 사라진다 — 그게 세션의 성질이다.
         체크하면 30일짜리 쿠키를 따로 받아, 다음에 와도 로그인 상태가 되살아난다.
         ★ 체크박스는 '체크했을 때만' 값이 전송된다. 안 하면 아예 안 온다(0이 오는 게 아니다).
           그래서 서버는 "값이 왔나?"만 보면 된다. -->
    <label class="auth-remember">
      <input type="checkbox" name="remember" value="1">
      로그인 유지 <span class="muted">(공용 PC에서는 체크하지 마세요)</span>
    </label>
    <button type="submit">로그인</button>
  </form>

  <p class="muted">
    <?php // seed.sql로 만들어진 계정들. 비밀번호는 전부 1234로 같다(시연용). ?>
    테스트 계정: <code>영화광 / 1234</code> · <code>해석러 / 1234</code> · <code>심야극장 / 1234</code><br>
    계정이 없나요? <a href="/auth/signup.php">회원가입</a>
  </p>

<?php require __DIR__ . '/../includes/footer.php'; ?>
