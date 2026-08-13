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

// 지금 로그인해 둔 기기 목록. (sessions 표를 그대로 읽는다)
//   ★ 세션을 파일이 아니라 DB에 두었기 때문에 가능한 화면이다.
$mySessions   = list_sessions_for(current_user_id());
$thisDevice   = session_fingerprint(session_id());   // 지금 이 기기를 표시하려고
$otherSessions = count_other_sessions(current_user_id(), session_id());

$pageTitle = '설정';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>⚙️ 설정</h1>
  <p class="muted"><a href="/profile/?user=<?= urlencode($username) ?>">← 내 프로필로</a></p>

  <?php // 업로드 성공/실패 안내는 header.php가 flash 쿠키에서 읽어 그린다 ?>

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
        <?= csrf_field() ?>
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
      <?= csrf_field() ?>
      <input type="text" name="nickname" value="<?= e($nickname) ?>"
             maxlength="20" required>
      <button type="submit">저장</button>
    </form>
  </section>

  <!-- ── 비밀번호 변경 ─────────────────────────────────────── -->
  <section class="settings-section">
    <h2>비밀번호 변경</h2>
    <form class="settings-form settings-form-col" method="post" action="/settings/password.php">
      <?= csrf_field() ?>
      <label>현재 비밀번호
        <input type="password" name="current" required>
      </label>
      <label>새 비밀번호
        <input type="password" name="new" minlength="4" required>
      </label>
      <button type="submit">변경</button>
    </form>
  </section>

  <!-- ── 로그인 기기 관리 ───────────────────────────────────── -->
  <?php // 세션을 DB로 옮기면서 비로소 만들 수 있게 된 기능. (settings/logout_others.php 주석 참고) ?>
  <section class="settings-section">
    <h2>로그인 기기</h2>
    <?php if ($otherSessions > 0): ?>
      <p class="muted">지금 이 기기 외에 <strong><?= $otherSessions ?>곳</strong>에서 로그인되어 있어요.</p>
    <?php else: ?>
      <p class="muted">다른 기기에 로그인된 곳이 없어요.</p>
    <?php endif; ?>

    <?php // 기기 목록 — 세션 한 줄이 기기 하나다. ?>
    <ul class="device-list">
      <?php foreach ($mySessions as $sess): ?>
        <?php $isThis = $sess['id_hash'] === $thisDevice; ?>
        <li class="device-item">
          <div>
            <strong><?= e(describe_user_agent($sess['user_agent'])) ?></strong>
            <?php if ($isThis): ?><span class="device-current">이 기기</span><?php endif; ?>
            <div class="muted">
              <?= e((string) $sess['ip_address']) ?>
              · 마지막 활동 <?= e(format_time_short(strtotime((string) $sess['last_active']))) ?>
            </div>
          </div>
          <?php if (!$isThis): ?>
            <form method="post" action="/settings/logout_session.php">
              <?= csrf_field() ?>
              <?php // 지문을 그대로 실어 보낸다 — 원본 번호표가 아니라 지문이라 새어도 로그인엔 못 쓴다.
                    // 남의 것을 끊는 건 서버가 user_id로 막는다. ?>
              <input type="hidden" name="id_hash" value="<?= e($sess['id_hash']) ?>">
              <button type="submit" class="btn-settings">로그아웃</button>
            </form>
          <?php endif; ?>
        </li>
      <?php endforeach; ?>
    </ul>
    <p class="muted">PC방이나 남의 기기에 로그인해 둔 게 걱정되면 한 번에 끊을 수 있어요.
      <strong>지금 이 기기는 그대로 유지</strong>됩니다.</p>
    <form method="post" action="/settings/logout_others.php"
          onsubmit="return confirm('다른 기기에서 모두 로그아웃할까요?');">
      <?= csrf_field() ?>
      <button type="submit" class="btn-settings">🔒 다른 기기에서 모두 로그아웃</button>
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
