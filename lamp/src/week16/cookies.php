<?php
// ============================================================
// cookies.php — 쿠키 설정 / 동의 철회  [GET]
//
//   [★ 왜 settings/ 안이 아니라 여기 있나]
//     `/settings/`는 **로그인해야 들어간다.** 그런데 동의는 **로그인 안 한 사람에게도** 받는다.
//     동의는 받아놓고 철회는 회원만 할 수 있으면, 그건 철회할 수 있는 게 아니다.
//     → 로그인과 무관한 자리에 두고, 푸터에서 어느 화면에서든 갈 수 있게 한다.
//
//   [★★ '철회'가 왜 필요한가 — 확인과 동의의 결정적 차이]
//     확인은 '읽었다'는 표시라 되돌릴 게 없다. 동의는 허락이므로 **언제든 거둘 수 있어야 한다.**
//     거둘 방법이 없으면 처음 누른 것도 동의가 아니었던 셈이 된다.
//
//   [화면이 하는 일 셋]
//     ① 지금 무엇에 동의했는지 보여준다 (언제·어떤 방침 버전에)
//     ② 항목별로 다시 고르게 한다 (일부만 켜고 끄기)
//     ③ 지금 브라우저에 실제로 무엇이 심어져 있는지 보여준다
//        ★ ③이 있어야 "지웠다"는 말을 **눈으로 확인**할 수 있다.
//          말로만 지웠다고 하면 그건 또 믿어달라는 것이다.
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/auth.php';    // 로그인했으면 다른 기기의 기록까지 보여주려고
require_once __DIR__ . '/includes/prefs.php';

$state = consent_state();               // null = 아직 안 고름
$saved = consent_saved_at();            // 고른 시각 (0이면 없음)

// ★ 서버에 남은 증빙을 그대로 보여준다.
//   **증빙은 보여줄 수 있어야 의미가 있다** — "기록하고 있습니다"라고 말만 하는 것과
//   그 줄을 화면에 띄우는 것은 다르다. 사용자도 자기 기록을 볼 권리가 있다.
$myUserId = is_logged_in() ? current_user_id() : null;
$history  = consent_history(consent_id(), $myUserId);

// 화면에 쓸 말. 코드값(grant·revoke…)을 그대로 보여주면 아무도 못 읽는다.
$actionLabels = [
    'grant'  => '동의',
    'revoke' => '철회',
    'reset'  => '선택 지움',
    'link'   => '회원과 연결',
];
$sourceLabels = [
    'banner'   => '배너',
    'settings' => '쿠키 설정',
    'login'    => '로그인',
];

// 지금 브라우저에 실제로 있는 쿠키를 '필수 / 선택'으로 갈라 보여준다.
//   ★ 값은 안 보여준다 — 화면에 굳이 늘어놓을 이유가 없고, 개발자 도구가 그 일을 더 잘한다.
$essential = [];
$optional  = [];
foreach (array_keys($_COOKIE) as $name) {
    if (!is_string($name)) {
        continue;
    }
    if (in_array($name, OPTIONAL_COOKIES, true)) {
        $optional[] = $name;
    } else {
        $essential[] = $name;
    }
}
sort($essential);
sort($optional);

$pageTitle      = '쿠키 설정';
$containerClass = 'narrow';
require __DIR__ . '/includes/header.php';
?>

  <h1>쿠키 설정</h1>

  <?php if ($state === null): ?>
    <p class="muted">아직 아무것도 고르지 않으셨습니다. 아래에서 정해 주세요.</p>
  <?php else: ?>
    <p class="muted">
      <?= $saved > 0 ? e(format_time_full($saved)) . '에 ' : '' ?>선택하신 내용입니다.
      <span class="consent-version">안내 v<?= (int) CONSENT_VERSION ?></span>
    </p>
  <?php endif; ?>

  <?php // ── ① 항목별로 다시 고르기 ─────────────────────────────
        //   배너와 같은 곳(/consent.php)으로 보낸다.
        //   ★ 동의를 적는 자리가 둘이 되면 언젠가 한쪽만 고쳐진다. 쓰는 문은 하나로 둔다. ?>
  <form class="consent-form" method="post" action="/consent.php">
    <?= csrf_field() ?>
    <input type="hidden" name="back" value="/cookies.php">

    <section class="settings-section">
      <h2>필수</h2>
      <p class="muted">
        로그인 유지 · 위조 요청 방어 · 화면 설정(정렬·감상·글 수) ·
        방금 한 일에 대한 알림.
      </p>
      <?php // ★ 기기 번호는 '무엇을 봤는지'를 담지 않는다 — 뜻 없는 무작위 값 하나뿐이다.
            //   그래도 **무엇에 쓰는지는 밝힌다.** 필수라고만 하고 목록에서 빼면
            //   공들여 만든 동의가 다시 불성실해진다. ?>
      <?php // ★ 예전엔 이 번호가 조회수 중복 방지도 겸했다. 목적이 둘이면 규정상 전부 면제여야
            //   동의 없이 심을 수 있어서, **조회수 판정은 쿠키를 안 쓰는 쪽으로 옮겼다.**
            //   (posts.php의 viewer_key 주석) ?>
      <p class="muted">
        <strong>기기 번호</strong> — 이 브라우저를 알아보는 무작위 번호입니다.
        <strong>로그인한 기기 목록</strong>에만 씁니다.
      </p>
      <p class="muted">
        이게 없으면 <strong>로그인도 글쓰기도 안 됩니다.</strong> 그래서 끌 수 없습니다.
      </p>
    </section>

    <section class="settings-section">
      <h2>선택</h2>
      <?php foreach (CONSENT_ITEMS as $key => $label): ?>
        <label class="cookie-item">
          <?php // checked = 지금 켜져 있는 것만. 안 고른 상태(null)면 전부 꺼짐이 기본이다. ?>
          <input type="checkbox" name="item_<?= e($key) ?>" value="1"
                 <?= !empty($state[$key]) ? 'checked' : '' ?>>
          <span><?= e($label) ?></span>
        </label>
      <?php endforeach; ?>

      <div class="consent-actions">
        <button type="submit" name="choice" value="selected">이대로 저장</button>
        <?php // 철회 = 선택 항목을 전부 끄고, 이미 쌓인 것까지 지운다.
              //   ★ 배너를 다시 띄우지 않는다 — 거둔 사람에게 또 묻는 건 조르는 것이다. ?>
        <button type="submit" name="choice" value="none" class="btn-consent-none">모두 철회</button>
      </div>
    </section>
  </form>

  <?php // ── ② 지금 브라우저에 실제로 있는 것 ────────────────────
        //   철회 버튼을 누른 뒤 이 목록에서 사라지는 것을 그 자리에서 볼 수 있다. ?>
  <section class="settings-section">
    <h2>지금 이 브라우저에 있는 쿠키</h2>

    <p class="cookie-list-label">필수 <span class="muted">(<?= count($essential) ?>개)</span></p>
    <p class="cookie-list"><?= $essential ? e(implode(' · ', $essential)) : '<span class="muted">없음</span>' ?></p>

    <p class="cookie-list-label">선택 <span class="muted">(<?= count($optional) ?>개)</span></p>
    <p class="cookie-list">
      <?php if ($optional): ?>
        <?= e(implode(' · ', $optional)) ?>
      <?php else: ?>
        <span class="muted">없음 — 동의하지 않으셨거나 아직 쌓인 것이 없습니다.</span>
      <?php endif; ?>
    </p>

    <p class="muted consent-hint">
      ※ 세션 번호표(<code>PHPSESSID</code>)와 알림·폼 값은 <strong>창을 닫으면 사라집니다.</strong>
      직접 확인하시려면 <strong>F12 → Application → Cookies</strong>를 열어보세요.
    </p>
  </section>

  <?php // ── ③ 서버에 남은 증빙 ──────────────────────────────────
        //   ★ 이 목록이 이 화면의 핵심이다.
        //     쿠키를 지워도 **이 줄들은 남는다** — 그래서 나중에 증명할 수 있다.
        //     반대로 쿠키만 있고 이게 없으면, "동의한 적 없다"는 말에 반박할 방법이 없다. ?>
  <section class="settings-section">
    <h2>동의 기록 <span class="muted consent-version">서버에 보관</span></h2>

    <?php if (!$history): ?>
      <p class="muted">아직 기록이 없습니다.</p>
    <?php else: ?>
      <ul class="consent-history">
        <?php foreach ($history as $row): ?>
          <?php
            // items는 DB에 {"view":1,"search":0} 문자열로 들어 있다 → 사람이 읽을 말로 바꾼다.
            $picked = json_decode((string) $row['items'], true);
            $names  = [];
            foreach (CONSENT_ITEMS as $key => $label) {
                if (!empty($picked[$key])) {
                    $names[] = $label;
                }
            }
          ?>
          <li>
            <span class="consent-when"><?= e(format_time_full((int) $row['at'])) ?></span>
            <strong><?= e($actionLabels[$row['action']] ?? $row['action']) ?></strong>
            <span class="muted">· <?= e($sourceLabels[$row['source']] ?? $row['source']) ?>에서</span>
            <span class="muted">· 안내 v<?= (int) $row['policy_version'] ?></span>
            <?php // 비로그인 기록은 user_id가 NULL이다. 그게 정상 — 동의는 로그인보다 먼저 받는다. ?>
            <span class="muted">· <?= $row['user_id'] === null ? '비회원' : '회원 #' . (int) $row['user_id'] ?></span>
            <div class="consent-items">
              <?= $names ? e(implode(' + ', $names)) : '<span class="muted">선택 항목 없음 (필수만)</span>' ?>
            </div>
          </li>
        <?php endforeach; ?>
      </ul>

      <p class="muted consent-hint">
        ※ 이 기록은 <strong>고쳐지지 않고 쌓이기만 합니다.</strong>
        철회해도 지난 줄을 지우지 않아요 — <strong>‘동의했다가 철회함’과 ‘동의한 적 없음’은 다른 사실</strong>이니까요.
      </p>
    <?php endif; ?>
  </section>

  <?php // ── ④ 동의 창 다시 보기 (철회가 아니다) ─────────────────
        //   기록을 아예 지워 '안 물어본 상태'로 되돌린다 → 배너가 다시 뜬다.
        //   ★ 철회와 다르다. 철회는 '싫다'는 선택이고, 이건 '처음부터 다시'다.
        //     버튼을 나눠둔 이유가 그것이다 — 둘을 한 버튼에 묶으면 어느 쪽인지 알 수 없다. ?>
  <form method="post" action="/consent.php" class="consent-reset">
    <?= csrf_field() ?>
    <input type="hidden" name="back" value="/cookies.php">
    <button type="submit" name="choice" value="reset" class="btn-link">동의 창 다시 보기</button>
  </form>

<?php require __DIR__ . '/includes/footer.php'; ?>
