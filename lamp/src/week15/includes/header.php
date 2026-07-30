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

// ── 신원(?as=)을 이 페이지의 모든 링크·폼에 자동으로 붙인다 ──
//   [왜 필요한가]
//     세션이 없으니 '지금 누구인지'를 매 요청마다 주소로 알려줘야 한다.
//     그런데 링크가 30곳이 넘어서, 하나씩 손으로 붙이면 반드시 어딘가를 빠뜨린다.
//     빠뜨린 링크를 누르는 순간 로그아웃되는데, 원인을 찾기가 아주 어렵다.
//
//   [해결] PHP 내장 URL 리라이터를 쓴다.
//     output_add_rewrite_var() = "지금부터 출력되는 HTML의 링크·폼에 이 값을 끼워 넣어라".
//       · <a href="/board/">        → <a href="/board/?as=영화광">
//       · <form action="/x.php">    → 안에 <input type="hidden" name="as" ...> 를 심어준다
//       · 외부 절대 주소(TMDB 이미지 등)·#앵커·mailto: 는 건드리지 않는다
//       · 값은 링크에선 URL 인코딩, hidden 필드에선 HTML 이스케이프되어 안전하다
//     url_rewriter.tags 기본값은 'form=' 뿐이라, 링크(a)까지 포함되도록 직접 지정한다.
//
//   ★ 출력이 시작되기 전에 불러야 한다 → 그래서 HTML보다 위인 여기에 둔다.
//   ★ current_user()로 '실제로 users 표에 있는 아이디'일 때만 켠다.
//     (주소에 아무 값이나 넣어도 그 쓰레기값이 온 사이트 링크에 퍼지지 않도록)
$identity = current_user();
if ($identity !== null) {
    ini_set('url_rewriter.tags', 'a=href,area=href,form=,fieldset=');
    output_add_rewrite_var(IDENTITY_KEY, $identity);
}

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
    <nav>
      <a href="/">홈</a>
      <a href="/works/">작품</a>
      <a href="/rank/">랭킹</a>
      <a href="/search/">검색</a>
      <?php // 세션에 로그인 정보가 있으면 메뉴가 달라진다 ?>
      <?php if (is_logged_in()): ?>
        <?php // 글쓰기는 '작품 게시판'에서 시작하는 구조(작품 slug 필요)라 상단바 메뉴는 두지 않는다.
              //   → '작품'·'검색'으로 작품에 들어가 게시판의 ✏️ 글쓰기로 시작. ?>
        <?php // 🔔 알림: 안 읽은 개수가 있으면 빨간 뱃지로 표시 ?>
        <?php $unread = count_unread_notifications(current_user_id()); ?>
        <a class="nav-bell" href="/notifications/" title="알림">🔔<?php
          if ($unread > 0): ?><span class="nav-bell-badge"><?= $unread > 99 ? '99+' : (int)$unread ?></span><?php endif; ?></a>
        <!-- 내 이름을 누르면 내 프로필로 (GET으로 user 전달)
             urlencode = 한글 아이디를 주소에 안전하게 넣기 위해 변환 -->
        <a class="nav-user" href="/profile/?user=<?= urlencode((string)current_user()) ?>"><?= e(current_nickname()) ?>님</a>
        <!-- 로그아웃은 '상태를 바꾸는' 동작이라 링크(GET)가 아니라 POST 폼 버튼 -->
        <form class="logout-form" method="post" action="/auth/logout.php">
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
    //   액션 파일(create/delete/…)이 set_flash()로 남긴 쪽지를 꺼내 보여준다.
    //   ★ 여기 한 군데서만 그린다 → 페이지마다 알림 코드를 복붙할 필요가 없다.
    //   take_flash()는 꺼내면서 지우므로, 새로고침하면 다시 뜨지 않는다.
    $flash = take_flash();
    ?>
    <?php if ($flash !== null): ?>
      <?php // 화면 우상단에 '떠 있는' 토스트. JS가 몇 초 뒤 스르륵 걷어낸다. ?>
      <div class="flash flash-<?= e($flash['type']) ?>">
        <span class="flash-text"><?= e($flash['message']) ?></span>

        <?php // '되돌리기' 같은 후속 동작 버튼 (있을 때만) ?>
        <?php if (!empty($flash['action'])): ?>
          <form method="post" action="<?= e($flash['action']['url']) ?>">
            <?php // 어느 글을 되돌릴지 등은 hidden으로 함께 보낸다 ?>
            <?php foreach ($flash['action']['fields'] as $name => $value): ?>
              <input type="hidden" name="<?= e($name) ?>" value="<?= e((string)$value) ?>">
            <?php endforeach; ?>
            <button type="submit" class="flash-action"><?= e($flash['action']['label']) ?></button>
          </form>
        <?php endif; ?>
      </div>
    <?php endif; ?>

