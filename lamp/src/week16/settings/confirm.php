<?php
// ============================================================
// settings/confirm.php — 비밀번호 재확인 화면  [GET]
//   민감한 작업(글 영구삭제·다른 기기 로그아웃) 앞에서 require_recent_auth()가
//   여기로 보낸다. 확인이 끝나면 원래 하려던 화면으로 돌아간다.
//
//   ★ 로그아웃시키지 않는다는 점이 중요하다 — 위험한 건 그 작업 하나뿐이므로
//     문턱은 그 앞에만 세운다. 나머지 화면은 그대로 쓸 수 있다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

require_login();

$containerClass = 'narrow';
$pageTitle = '비밀번호 확인';
require __DIR__ . '/../includes/header.php';
?>

  <h1>🔐 비밀번호 확인</h1>
  <p class="muted">
    되돌릴 수 없는 작업이라 한 번 더 확인합니다.
    확인하면 <strong><?= (int) (SUDO_WINDOW / 60) ?>분</strong> 동안은 다시 묻지 않아요.
  </p>

  <form class="settings-form settings-form-col" method="post" action="/settings/reauth.php">
    <?= csrf_field() ?>
    <label>비밀번호
      <input type="password" name="password" required autofocus>
    </label>
    <button type="submit">확인</button>
  </form>

  <p class="muted"><a href="/settings/">← 설정으로 돌아가기</a></p>

<?php require __DIR__ . '/../includes/footer.php'; ?>
