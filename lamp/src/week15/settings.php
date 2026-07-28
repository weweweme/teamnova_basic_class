<?php
// ============================================================
// settings.php — 내 정보 설정 (마이페이지 안의 '설정' 뎁스)  [GET]
//   공개 프로필(profile.php)과 분리된 '내 전용' 편집 화면.
//   지금은 프로필 이미지 변경 + 계정 정보. (나중에 비밀번호 변경 등 추가 가능)
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/auth.php';

// ★ 로그인 필수 — 내 설정이니 로그인한 본인만.
require_login();

$username = (string) current_user();
$userRow  = find_user($username);
$avatar   = $userRow['avatar'] ?? null;

$pageTitle = '설정';
$containerClass = 'narrow';
require __DIR__ . '/includes/header.php';
?>

  <h1>⚙️ 설정</h1>
  <p class="muted"><a href="/profile.php?user=<?= urlencode($username) ?>">← 내 프로필로</a></p>

  <?php // 업로드 성공/실패 안내는 header.php가 세션(플래시)에서 꺼내 그린다 ?>

  <!-- ── 프로필 이미지 ─────────────────────────────────────── -->
  <section class="settings-section">
    <h2>프로필 이미지</h2>
    <div class="settings-avatar">
      <?php if ($avatar): ?>
        <img class="avatar" src="<?= e($avatar) ?>" alt="">
      <?php else: ?>
        <span class="avatar avatar-empty"><?= e(mb_substr($username, 0, 1)) ?></span>
      <?php endif; ?>

      <!-- 파일 업로드 폼: enctype="multipart/form-data" 필수 (파일을 담아 보내는 방식) -->
      <form class="avatar-form" method="post" action="/profile/avatar.php" enctype="multipart/form-data">
        <label class="btn-upload">
          📷 이미지 변경
          <input type="file" name="avatar" accept="image/*" onchange="this.form.submit()" hidden>
        </label>
        <span class="muted">JPG·PNG·GIF·WebP · 2MB 이하</span>
      </form>
    </div>
  </section>

  <!-- ── 계정 정보 (지금은 읽기 전용) ──────────────────────── -->
  <section class="settings-section">
    <h2>계정 정보</h2>
    <dl class="settings-info">
      <dt>아이디</dt>
      <dd><?= e($username) ?></dd>
    </dl>
  </section>

<?php require __DIR__ . '/includes/footer.php'; ?>
