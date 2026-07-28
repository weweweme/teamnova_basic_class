<?php
// ============================================================
// settings.php — 내 정보 설정 (마이페이지 안의 '설정' 뎁스)  [GET]
//   공개 프로필(profile.php)과 분리된 '내 전용' 편집 화면.
//   지금은 프로필 이미지 변경 + 계정 정보. (나중에 비밀번호 변경 등 추가 가능)
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

// ★ 로그인 필수 — 내 설정이니 로그인한 본인만.
require_login();

$username = (string) current_user();
$userRow  = find_user($username);
$avatar   = $userRow['avatar'] ?? null;
$nickname = $userRow['nickname'] ?? $username;   // 표시 이름(변경 폼에 미리 채워둔다)

$pageTitle = '설정';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>⚙️ 설정</h1>
  <p class="muted"><a href="/profile/?user=<?= urlencode($username) ?>">← 내 프로필로</a></p>

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

      <!-- 파일 업로드 폼: enctype="multipart/form-data" 필수 (파일을 담아 보내는 방식)
           ★ 자동제출(onchange) 대신 main.js가 파일 선택을 가로채서
             올리기 전에 256px로 줄이고(WebP) 제출한다. JS 실패 시엔 원본 그대로 제출. -->
      <form class="avatar-form" method="post" action="/profile/avatar.php" enctype="multipart/form-data">
        <label class="btn-upload">
          📷 이미지 변경
          <input type="file" name="avatar" id="avatar-input" accept="image/*" hidden>
        </label>
        <span class="muted">JPG·PNG·GIF·WebP · 업로드 시 자동으로 256px로 최적화</span>
      </form>
    </div>
  </section>

  <!-- ── 닉네임(표시 이름) 변경 ───────────────────────────── -->
  <section class="settings-section">
    <h2>닉네임</h2>
    <p class="muted">글·댓글·프로필에 보이는 이름이에요. (로그인 아이디는 안 바뀝니다)</p>
    <form class="settings-form" method="post" action="/settings/nickname.php">
      <input type="text" name="nickname" value="<?= e($nickname) ?>"
             maxlength="20" required>
      <button type="submit">저장</button>
    </form>
  </section>

  <!-- ── 비밀번호 변경 ─────────────────────────────────────── -->
  <section class="settings-section">
    <h2>비밀번호 변경</h2>
    <form class="settings-form settings-form-col" method="post" action="/settings/password.php">
      <label>현재 비밀번호
        <input type="password" name="current" required>
      </label>
      <label>새 비밀번호
        <input type="password" name="new" minlength="4" required>
      </label>
      <button type="submit">변경</button>
    </form>
  </section>

  <!-- ── 휴지통 ─────────────────────────────────────────────── -->
  <section class="settings-section">
    <h2>휴지통</h2>
    <p class="muted">삭제한 글을 되돌리거나 영구 삭제할 수 있어요.</p>
    <a class="btn-settings" href="/trash/">🗑 휴지통 열기</a>
  </section>

  <!-- ── 계정 정보 (아이디는 못 바꿈 — 신원 키) ──────────────── -->
  <section class="settings-section">
    <h2>계정 정보</h2>
    <dl class="settings-info">
      <dt>아이디</dt>
      <dd><?= e($username) ?></dd>
    </dl>
  </section>

<?php require __DIR__ . '/../includes/footer.php'; ?>
