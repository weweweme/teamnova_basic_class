<?php
// ============================================================
// session/verify.php — "기기 확인 중…"  [GET]
//
//   [★ 이 화면이 왜 필요한가]
//     도장은 **브라우저만 찍을 수 있다.** 서버 혼자서는 아무것도 못 한다.
//     그래서 "잠깐 여기 서서 도장 한 번 찍고 가라"는 자리가 하나 필요하다.
//
//   [언제 오나 — 두 경우뿐이다]
//     ① 로그인 직후        → 이 기기에 도장이 없으면 만들어 등록한다
//     ② 도장 확인이 만료됨 → 자국만 다시 찍는다
//     ※ 평소에는 **여기 올 일이 없다.** 만료 1분 전에 화면 뒤에서 미리 찍어두기 때문이다.
//       (device-key.js의 scheduleRefresh)
//
//   [★ 이 화면은 도장 검사에서 빠져 있다]
//     도장을 받으러 오는 길까지 막으면 아무도 도장을 못 찍는다 — 무한 루프가 된다.
//     is_key_exempt_path()가 `/session/`을 통째로 빼주는 이유다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/device_key.php';

// 로그인부터 안 돼 있으면 여기 있을 이유가 없다.
if (!is_logged_in()) {
    redirect('/auth/login.php');
}

// 확인이 끝나면 돌아갈 곳.
//   ★ 주소로 받은 값은 **반드시 우리 사이트 안인지 확인한다.** 안 그러면 오픈 리다이렉트다.
//     (`//evil.com`은 '/'로 시작하지만 바깥 주소다 — is_internal_path가 그것까지 막는다)
$back = get_str('back');
if (!is_internal_path($back)) {
    $back = take_intended('/');      // 로그인 직후라면 '원래 가려던 곳'이 여기 들어 있다
}

// 이 기기에 도장이 이미 있나? → 화면이 '등록'을 할지 '연장'을 할지 정한다.
$hasKey = device_public_key(current_user_id()) !== null;

$pageTitle      = '기기 확인';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>🔏 기기 확인 중…</h1>

  <p class="muted" id="key-status">
    <?= $hasKey ? '이 기기의 도장으로 확인하고 있습니다.' : '이 기기에 도장을 만들고 있습니다.' ?>
  </p>

  <?php // ★ 실패했을 때만 보이는 자리. 처음부터 띄우면 멀쩡한데도 겁을 준다. ?>
  <div id="key-error" hidden>
    <p><strong>이 기기의 도장을 확인하지 못했습니다.</strong></p>
    <?php // ★ 가장 흔한 원인은 '저장소를 지웠다'이다. 그때는 다시 로그인하면 풀린다 —
          //   비밀번호를 맞히면 도장을 새로 만들 수 있는 창이 열리기 때문이다. ?>
    <p class="muted">
      브라우저 저장 데이터를 지웠거나, 개인정보 보호 모드이거나,
      브라우저가 오래된 경우입니다.
      <strong>다시 로그인하면</strong> 이 기기의 도장을 새로 만듭니다.
    </p>
    <p><a href="/auth/logout.php">로그아웃하고 다시 로그인</a></p>
  </div>

  <?php // 값을 JS에 넘길 때는 data-* 속성으로 넘긴다.
        //   ★ JS 안에 PHP 변수를 직접 찍으면 따옴표 하나에 스크립트가 통째로 깨지고,
        //     값에 따라 XSS가 된다. 속성으로 넘기면 e()가 그대로 막아준다. ?>
  <div id="key-boot"
       data-has-key="<?= $hasKey ? '1' : '0' ?>"
       data-can-enroll="<?= can_enroll_key() ? '1' : '0' ?>"
       data-back="<?= e($back) ?>" hidden></div>

  <script src="/assets/js/verify-page.js?v=<?= e((string) @filemtime(__DIR__ . '/../assets/js/verify-page.js')) ?>" defer></script>

<?php require __DIR__ . '/../includes/footer.php'; ?>
