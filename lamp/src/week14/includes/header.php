<?php
// ============================================================
// header.php — 모든 페이지 '맨 위'에 공통으로 들어가는 조각
//   페이지마다 복붙하지 않고 include로 한 번만 관리 →
//   메뉴 하나 바꾸면 전체 페이지에 반영됨.
// ============================================================

// e() 함수가 필요하니 util을 먼저 불러온다.
//   require_once = "이미 불러왔으면 또 안 부른다"(중복 방지).
require_once __DIR__ . '/util.php';

// 페이지 제목: include 하기 전에 $pageTitle 을 정해주면 그게 뜨고,
//   안 정했으면 기본값을 쓴다. (?? = '왼쪽이 없으면 오른쪽')
$pageTitle = $pageTitle ?? '종목토론방';
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
      <a href="/works.php">작품</a>
      <a href="/search.php">검색</a>
      <a href="/post/write.php">글쓰기</a>
    </nav>
  </header>

  <!-- 각 페이지의 '실제 내용'은 이 아래(main)에 채워진다 -->
  <main class="container">
