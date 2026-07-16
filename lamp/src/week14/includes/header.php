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
  <!-- 공통 스타일. 경로가 '/'로 시작 = week14 최상위 기준(어느 폴더 페이지든 동일하게 작동) -->
  <link rel="stylesheet" href="/assets/css/style.css">
</head>
<body>
  <!-- 공통 상단 메뉴바: 어느 페이지에서든 여기로 이동 가능 -->
  <header class="topbar">
    <a class="logo" href="/">📈 종목토론방</a>
    <nav>
      <a href="/">홈</a>
      <a href="/auth/login.php">로그인</a>
      <a href="/auth/signup.php">회원가입</a>
      <a href="/post/write.php">글쓰기</a>
    </nav>
  </header>

  <!-- 각 페이지의 '실제 내용'은 이 아래(main)에 채워진다 -->
  <main class="container">
