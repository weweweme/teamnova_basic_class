<?php
// ============================================================
// header.php — 모든 페이지 '맨 위'에 공통으로 들어가는 조각
//   페이지마다 복붙하지 않고 include로 한 번만 관리 →
//   메뉴 하나 바꾸면 전체 페이지에 반영됨.
// ============================================================

// e() 함수가 필요하니 util을 먼저 불러온다.
//   require_once = "이미 불러왔으면 또 안 부른다"(중복 방지).
require_once __DIR__ . '/util.php';
require_once __DIR__ . '/auth.php';   // 로그인 상태에 따라 메뉴가 달라지므로
require_once __DIR__ . '/level.php';  // 작성자 옆 등급 배지(user_level) — 모든 화면에서 씀
require_once __DIR__ . '/notifications.php';  // 상단바 🔔 안읽은 개수
require_once __DIR__ . '/devices.php';        // 새 기기 로그인 알림
require_once __DIR__ . '/prefs.php';          // 쿠키 안내 배너 (footer.php에서 씀)

// ★ week16에서 여기 있던 'URL 리라이터' 블록이 통째로 사라졌다.
//   week15는 세션이 없어서 신원(?as=영화광)을 링크 30여 곳에 빠짐없이 붙여야 했고,
//   손으로는 반드시 빠뜨리므로 PHP 내장 리라이터(output_add_rewrite_var)를 켜서
//   출력되는 모든 <a href>와 <form>에 자동으로 끼워 넣었다.
//   → 신원이 세션으로 옮겨가면서 붙일 것 자체가 없어졌다. 링크는 이제 그냥 링크다.

// ── 상단바 검색창에 미리 채워둘 검색어 ──────────────────────
//   검색 화면에서는 방금 친 말이 칸에 남아 있어야 한다 — 한 글자만 고쳐 다시 찾는 일이 잦다.
//   ★ 검색 화면일 때만 채운다. ?q= 는 게시판에도 있는데(그 게시판 안에서만 찾는 말이라)
//     그것까지 끌어다 채우면 "전체 검색"인 줄 알고 엉뚱한 결과를 기대하게 된다.
$isSearchPage = str_starts_with($_SERVER['REQUEST_URI'] ?? '', '/search/');
$topbarQuery  = $isSearchPage ? get_str('q') : '';

// 컨테이너에 붙일 추가 클래스 (페이지가 정해줄 수 있음).
//   예) 좁은 페이지는 $containerClass = 'narrow' 로 콘텐츠를 중앙 컬럼에 담는다.
$containerClass = $containerClass ?? '';
// 페이지 제목: include 하기 전에 $pageTitle 을 정해주면 그게 뜨고,
//   안 정했으면 기본값을 쓴다. (?? = '왼쪽이 없으면 오른쪽')
$pageTitle = $pageTitle ?? '리뷰 커뮤니티';
?>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title><?= e($pageTitle) ?></title>
  <!-- 공통 스타일 연결 (외부 CSS 방식):
       브라우저가 이 <link>를 보고 style.css 를 '따로' 한 번 더 요청해서 가져와 적용한다.
       이 <link>가 header에 있으니, header를 include하는 '모든 페이지'가 CSS를 자동으로 물려받음.
       rel="stylesheet" = 관계가 스타일시트 / href = CSS 파일 위치.
       경로가 '/'로 시작 = week14 최상위 기준(어느 폴더 페이지든 같은 경로로 찾음). -->
  <?php
  // ── 캐시 무력화(cache busting) ────────────────────────────────
  //   [문제] 브라우저는 한 번 받은 CSS를 '같은 주소'면 저장(캐시)해두고 다시 안 받아온다.
  //          → style.css를 고쳐도 화면은 옛날 그대로. (개발할 때 제일 헷갈리는 함정)
  //
  //   [해결] 주소 뒤에 그 파일의 '마지막 수정시각'을 ?v= 로 붙인다.
  //          filemtime() = 파일이 마지막으로 바뀐 시각을 숫자로 돌려줌
  //                        (1970년부터 흐른 초. 예: 1784557902)
  //          · CSS를 고치면 → 시각이 바뀜 → 주소가 달라짐 → 브라우저가 '처음 보는 주소'라 새로 받음
  //          · 안 고쳤으면 → 주소 그대로 → 캐시 재사용(빠름)
  //
  //   [왜 통하나 — 핵심]
  //          ?v=... 는 서버 입장에선 '의미 없는 꼬리표'다. 붙이든 말든 어차피 같은 style.css를 준다.
  //          하지만 브라우저는 '주소 전체'를 열쇠로 캐시를 관리하므로,
  //          꼬리표만 달라져도 '다른 파일'로 보고 새로 받아온다.
  //          → 서버 동작은 그대로 두고 브라우저 캐시만 정확히 갱신시키는 실무 표준 기법.
  //
  //   file_exists 먼저 확인: 파일이 없으면 filemtime()이 경고를 내므로
  //   '있는지 확인 후 사용'(Tester-Doer). 없으면 임시로 '1'을 쓴다.
  $cssPath = __DIR__ . '/../assets/css/style.css';
  $cssVer  = file_exists($cssPath) ? filemtime($cssPath) : '1';
  ?>
  <link rel="stylesheet" href="/assets/css/style.css?v=<?= e((string)$cssVer) ?>">

  <?php
  // JS도 CSS와 같은 이유로 캐시 무력화(?v=수정시각)
  $jsPath = __DIR__ . '/../assets/js/main.js';
  $jsVer  = file_exists($jsPath) ? filemtime($jsPath) : '1';
  ?>
  <!-- defer = "HTML을 다 읽은 뒤에 실행해라".
       이게 없으면 <head>에서 JS가 먼저 돌아 아직 만들어지지 않은 요소를 못 찾는다.
       (예전에 <script>를 body 맨 아래 뒀던 이유와 같은 문제를, defer로 더 깔끔하게 해결) -->
  <script src="/assets/js/main.js?v=<?= e((string)$jsVer) ?>" defer></script>
</head>
<body>
  <!-- 공통 상단 메뉴바: 어느 페이지에서든 여기로 이동 가능 -->
  <header class="topbar">
    <a class="logo" href="/">🎬 리뷰 커뮤니티</a>

    <?php // ── 통합검색: 메뉴 링크 대신 '입력칸'을 상단바에 둔다 ──
          //   검색 페이지로 한 번 이동한 뒤 다시 검색어를 치는 두 단계를,
          //   어느 화면에서든 바로 치는 한 단계로 줄인다. (포털·유튜브가 이렇게 한다)
          //   ★ method="get" 이라 검색어가 주소에 남는다 → 결과를 그대로 공유·북마크할 수 있다.
          //   ★ type="search" = 검색용 입력칸. 브라우저가 지운 내역·× 지우기 버튼을 알아서 붙여준다. ?>
    <form class="topbar-search" method="get" action="/search/" role="search">
      <input type="search" name="q" value="<?= e($topbarQuery) ?>" maxlength="50"
             placeholder="작품 · 글 · 유저 검색" aria-label="통합검색">
      <button type="submit" aria-label="검색">🔍</button>
    </form>

    <nav>
      <a href="/">홈</a>
      <a href="/works/">작품</a>
      <a href="/rank/">랭킹</a>
      <?php // 주소에 실려온 신원이 진짜 회원이면 메뉴가 달라진다 (is_logged_in) ?>
      <?php if (is_logged_in()): ?>
        <?php // 글쓰기는 '작품 게시판'에서 시작하는 구조(작품 slug 필요)라 상단바 메뉴는 두지 않는다.
              //   → '작품'·'검색'으로 작품에 들어가 게시판의 ✏️ 글쓰기로 시작. ?>
        <?php // 🔔 알림: 안 읽은 개수가 있으면 빨간 뱃지로 표시 ?>
        <?php $unread = count_unread_notifications(current_user_id()); ?>
        <a class="nav-bell" href="/notifications/" title="알림">🔔<?php
          if ($unread > 0): ?><span class="nav-bell-badge"><?= $unread > 99 ? '99+' : (int)$unread ?></span><?php endif; ?></a>
        <!-- 내 이름을 누르면 내 프로필로 (GET으로 user 전달)
             urlencode = 한글 아이디를 주소에 안전하게 넣기 위해 변환 -->
        <?php // ⏱ 자동 로그아웃까지 남은 시간. 서버가 준 초를 JS가 1초씩 깎는다.
              //   ★ 판정은 서버가 한다 — 이건 '보여주기'일 뿐이라, 0이 되면 새로고침해서
              //     서버의 판단을 받는다(그 요청에서 로그아웃 처리 + 안내가 뜬다). ?>
        <span class="idle-timer" id="idle-timer"
              data-left="<?= (int) idle_seconds_left() ?>"
              title="이 시간 동안 아무 동작이 없으면 자동 로그아웃됩니다">⏱ --:--</span>
        <a class="nav-user" href="/profile/?user=<?= urlencode((string)current_user()) ?>"><?= e(current_nickname()) ?>님</a>
        <!-- 로그아웃은 '상태를 바꾸는' 동작이라 링크(GET)가 아니라 POST 폼 버튼 -->
        <form class="logout-form" method="post" action="/auth/logout.php">
          <?= csrf_field() ?>
          <button type="submit">로그아웃</button>
        </form>
      <?php else: ?>
        <a href="/auth/login.php">로그인</a>
        <a href="/auth/signup.php">회원가입</a>
      <?php endif; ?>
    </nav>
  </header>

  <!-- 각 페이지의 '실제 내용'은 이 아래(main)에 채워진다 -->
  <main class="container <?= e($containerClass) ?>">

    <?php
    // ── 플래시 알림 ──────────────────────────────────────────
    //   액션 파일(create/delete/…)이 set_flash()로 세션에 남긴 쪽지를 꺼내 보여준다.
    //   ★ 여기 한 군데서만 그린다 → 페이지마다 알림 코드를 복붙할 필요가 없다.
    //   ★ take_flash()는 꺼내면서 flash 쿠키를 지운다(read-once) → 새로고침해도 다시 뜨지 않는다.
    //     week15에는 알림이 주소에 있어서 JS가 주소창을 청소해야 했다. 그 일이 없어졌다.
    $flash = take_flash();

    // ── 새 기기 로그인 알림 ──────────────────────────────────
    //   ★ **다른 곳에서 내 계정에 로그인이 있었다**를 주인에게 알린다.
    //     지금 이 기기는 제외한다 — 방금 자기가 로그인한 걸 자기에게 알려봐야 소용없다.
    //   ★ 알림이 이미 떠 있으면 덮어쓰지 않는다. 두 개를 겹쳐 띄우면 둘 다 안 읽힌다.
    //   ⚠️ **한계**: 이건 '로그인'을 잡는다. **세션 쿠키를 훔쳐 붙여넣은 경우엔 로그인이
    //     없으므로 안 잡힌다.** 비밀번호 유출에는 강하고 세션 탈취에는 약하다 — 다른 공격이다.
    if ($flash === null && is_logged_in()) {
        $newDevices = take_new_devices(current_user_id());
        if ($newDevices) {
            $names = array_map(fn($d) => describe_device($d['user_agent']), $newDevices);
            $flash = [
                'message' => '🔔 새 기기에서 로그인되었습니다 — ' . implode(' · ', $names)
                             . '. 본인이 아니라면 비밀번호를 바꿔 주세요.',
                'type'    => 'error',
                'action'  => null,
            ];
        }
    }
    ?>
    <?php if ($flash !== null): ?>
      <?php // 화면 우상단에 '떠 있는' 토스트. JS가 몇 초 뒤 스르륵 걷어낸다. ?>
      <div class="flash flash-<?= e($flash['type']) ?>">
        <span class="flash-text"><?= e($flash['message']) ?></span>

        <?php // '되돌리기' 같은 후속 동작 버튼 (있을 때만) ?>
        <?php if (!empty($flash['action'])): ?>
          <form method="post" action="<?= e($flash['action']['url']) ?>">
            <?= csrf_field() ?>
            <?php // 어느 글을 되돌릴지 등은 hidden으로 함께 보낸다 ?>
            <?php foreach ($flash['action']['fields'] as $name => $value): ?>
              <input type="hidden" name="<?= e($name) ?>" value="<?= e((string)$value) ?>">
            <?php endforeach; ?>
            <button type="submit" class="flash-action"><?= e($flash['action']['label']) ?></button>
          </form>
        <?php endif; ?>

        <?php // 바로 닫기 — 몇 초를 기다리지 않아도 되게. JS가 클릭을 받아 걷어낸다.
              //   ★ type="button" 필수: <button>의 기본값은 submit이라, 폼 안이 아니어도
              //     나중에 폼으로 감싸는 순간 엉뚱하게 전송돼버린다.
              //   aria-label = 화면을 못 보는 사람에게 '×'가 무슨 버튼인지 읽어주는 이름. ?>
        <button type="button" class="flash-close" aria-label="알림 닫기">×</button>
      </div>
    <?php endif; ?>

