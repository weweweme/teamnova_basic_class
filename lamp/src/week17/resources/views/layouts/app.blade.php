{{--
  공통 레이아웃 — 지금 includes/header.php + footer.php 가 하던 일
    ★ 지금은 화면 파일마다 require 로 위아래를 붙였다.
      Blade 에서는 화면이 이 레이아웃을 '상속'한다.
        · @yield('이름')   — 자식이 채워 넣을 구멍
        · @section('이름') — 자식이 그 구멍을 채우는 선언
--}}
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">

  {{-- @yield 의 두 번째 인자는 '자식이 안 채웠을 때 쓸 기본값' --}}
  <title>@yield('title', '리뷰 커뮤니티')</title>

  {{--
    asset() 은 public/ 기준 주소를 만들어 준다.
    ?v= 에 파일 수정 시각을 붙여 캐시를 무효화한다 — 지금 header.php 가 하던 것과 같은 방식.
  --}}
  <link rel="stylesheet"
        href="{{ asset('assets/css/style.css') }}?v={{ filemtime(public_path('assets/css/style.css')) }}">

  {{--
    ⚠ main.js 는 아직 붙이지 않는다.
      그 안의 기능(자동 로그아웃 카운트다운·임시저장·신고 창)이 아직 Laravel 쪽에 없어서,
      지금 붙이면 없는 주소로 요청을 보낸다. 해당 기능을 옮기는 단계에서 함께 붙인다.
  --}}
</head>
<body>
  <header class="topbar">
    <a class="logo" href="/posts">🎬 리뷰 커뮤니티</a>

    {{-- 통합검색 — 5단계에서 실제 주소를 연결한다 --}}
    <form class="topbar-search" method="get" action="/search" role="search">
      <input type="search" name="q" maxlength="50"
             placeholder="작품 · 글 · 유저 검색" aria-label="통합검색">
      <button type="submit" aria-label="검색">🔍</button>
    </form>

    <nav>
      <a href="/posts">글 목록</a>
      {{-- 로그인 상태에 따라 갈리는 메뉴(알림·내 프로필·로그아웃)는 2단계에서 --}}
      <a href="/login">로그인</a>
    </nav>
  </header>

  <main class="container">
    @yield('content')
  </main>
</body>
</html>
