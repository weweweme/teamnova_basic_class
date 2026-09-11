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

      {{-- @auth / @guest = 로그인 여부로 갈리는 블록.
           지금 is_logged_in() 으로 if 문을 쓰던 자리다. --}}
      @auth
        {{-- auth()->user() 로 로그인한 회원의 모델을 바로 꺼낸다 --}}
        <a href="/trash">휴지통</a>
        <span class="nav-user">{{ auth()->user()->nickname }}님</span>

        {{-- 로그아웃은 상태를 바꾸는 동작이라 링크가 아니라 POST 폼 --}}
        <form class="logout-form" method="post" action="/logout">
          @csrf
          <button type="submit">로그아웃</button>
        </form>
      @else
        <a href="/login">로그인</a>
        <a href="/register">회원가입</a>
      @endauth
    </nav>
  </header>

  <main class="container">
    {{-- 플래시 알림 — 리다이렉트하면서 with('status', …) 로 남긴 쪽지를 한 번만 보여준다.
         우리 set_flash() 와 같은 것이고, 그리는 자리도 여기 한 곳뿐이다. --}}
    @if (session('status'))
      <p class="flash">{{ session('status') }}</p>
    @endif

    @yield('content')
  </main>
</body>
</html>
