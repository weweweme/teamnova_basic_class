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

  {{-- JS가 POST 할 때 쓸 CSRF 토큰. 폼이 아니라 fetch 로 보낼 때 필요하다 --}}
  <meta name="csrf-token" content="{{ csrf_token() }}">

  @auth
    @if (config('auth.device_key_required'))
      {{-- 기기 도장 — 남은 확인 시간을 알려 주면 JS가 만료 직전에 다시 찍는다.
           ★ 판정은 서버가 한다. 이 값은 '언제 다시 찍을지' 정하는 데만 쓴다. --}}
      @if (session('key_enroll_ok') && ! app(\App\Services\DeviceKey::class)->publicKeyFor(auth()->id(), app(\App\Services\DeviceTracker::class)->deviceId(request())))
        {{-- 이 기기에 아직 도장이 없고 등록 창이 열려 있다 → JS가 등록한다 --}}
        <meta name="key-enroll" content="1">
      @endif
      <meta name="key-proof-left"
            content="{{ max(0, config('auth.device_key_proof_ttl') - (time() - (int) session('key_proof_at', 0))) }}">
      <meta name="key-proof-margin" content="{{ (int) min(20, config('auth.device_key_proof_ttl') / 3) }}">
      <script src="{{ asset('assets/js/device-key.js') }}" defer></script>
    @endif
  @endauth

  {{-- 화면의 '동작' — 신고·삭제 확인 창, 토스트, 글자 수, 가로줄 화살표,
       무한 스크롤, 예고편, 자동 로그아웃 카운트다운.
       ★ defer = HTML 을 다 읽은 뒤에 실행한다. 그래야 요소를 찾을 수 있다. --}}
  <script src="{{ asset('assets/js/main.js') }}?v={{ filemtime(public_path('assets/js/main.js')) }}" defer></script>
</head>
<body>
  <header class="topbar">
    <a class="logo" href="/">🎬 리뷰 커뮤니티</a>

    <form class="topbar-search" method="get" action="/search" role="search">
      <input type="search" name="q" value="{{ request('q') }}" maxlength="50"
             placeholder="작품 · 글 · 유저 검색" aria-label="통합검색">
      <button type="submit" aria-label="검색">🔍</button>
    </form>

    <nav>
      <a href="/">홈</a>
      <a href="/posts">글 목록</a>
      <a href="/works">작품</a>
      <a href="/rank">랭킹</a>

      {{-- @auth / @guest = 로그인 여부로 갈리는 블록.
           지금 is_logged_in() 으로 if 문을 쓰던 자리다. --}}
      @auth
        {{-- 🔔 안 읽은 알림 개수. 있으면 뱃지로 표시한다 --}}
        @php $unread = auth()->user()->unreadNotifications()->count(); @endphp
        <a class="nav-bell" href="/notifications" title="알림">🔔@if ($unread > 0)<span class="nav-bell-badge">{{ $unread > 99 ? '99+' : $unread }}</span>@endif</a>
        <a href="/trash">휴지통</a>
        <a href="/settings">설정</a>

        {{-- 자동 로그아웃까지 남은 시간. 숫자는 JS 가 1초마다 다시 그린다.
             ★ 판정은 서버(EnsureNotIdle)가 한다 — 이건 보여주기일 뿐이다. --}}
        <span class="idle-timer" id="idle-timer"
              data-left="{{ max(0, (int) config('auth.idle_timeout') - (time() - (int) session('last_seen', time()))) }}"
              title="이 시간 동안 아무 동작이 없으면 자동 로그아웃됩니다">⏱ --:--</span>

        <a class="nav-user" href="/users/{{ auth()->user()->username }}">{{ auth()->user()->nickname }}님</a>

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

  {{-- ★ 화면마다 본문 폭이 다르다. 글쓰기·로그인·설정처럼 폼 위주 화면은 'narrow'.
       기존 코드의 $containerClass 와 같은 역할이다. --}}
  <main class="container @yield('container')">
    {{-- 플래시 알림 — 리다이렉트하면서 with('status', …) 로 남긴 쪽지를 한 번만 보여준다.
         우리 set_flash() 와 같은 것이고, 그리는 자리도 여기 한 곳뿐이다.
         몇 초 뒤 걷어내는 '연출'은 main.js 가 한다. --}}
    @if (session('status') || session('error'))
      <div @class(['flash', 'flash-error' => session('error')])>
        <span class="flash-text">{{ session('error') ?? session('status') }}</span>
        <button type="button" class="flash-close" aria-label="알림 닫기">×</button>
      </div>
    @endif

    @yield('content')
  </main>

  <footer class="foot">
    <small>🎬 리뷰 커뮤니티 · 영화·드라마 리뷰 커뮤니티 · TMDB 제공</small>
    <small><a href="/cookies">🍪 쿠키 설정</a></small>
  </footer>

  {{-- 삭제 확인 팝업 — 모든 페이지에 하나만 두고 재사용한다.
       ★ 실제 삭제는 각 삭제 폼의 POST 가 한다. 이 창은 '한 번 더 묻기'만 담당. --}}
  <dialog id="confirm-dialog" class="modal">
    <h3>정말 삭제할까요?</h3>
    <p class="confirm-text" id="confirm-message">삭제하면 목록에서 사라집니다.</p>
    <p class="confirm-sub">삭제 후 알림의 '되돌리기'로 복구할 수 있어요.</p>
    <div class="modal-actions">
      <button type="button" id="confirm-cancel" class="btn-cancel">취소</button>
      <button type="button" id="confirm-ok" class="btn-danger">삭제</button>
    </div>
  </dialog>

  {{-- 아직 결정하지 않았으면 쿠키 배너를 보여준다 --}}
  @if (! app(\App\Services\Consent::class)->decided(request()))
    @include('partials.consent-banner', ['consentItems' => \App\Services\Consent::LABELS, 'consentChecked' => []])
  @endif

  {{-- 화면 하나에만 필요한 스크립트는 여기로 모인다 (@push('scripts')) --}}
  @stack('scripts')
</body>
</html>
