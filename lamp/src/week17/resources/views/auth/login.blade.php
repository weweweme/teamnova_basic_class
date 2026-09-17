@extends('layouts.app')

@section('title', '로그인')

@section('container', 'narrow')

@section('content')
  <h1>로그인</h1>

  {{-- 로그인 정보는 민감하므로 반드시 POST (주소에 비밀번호가 남으면 큰일) --}}
  <form class="auth-form" method="post" action="/login">
    {{-- ★ @csrf 한 줄이면 숨김 토큰이 들어간다.
         검사는 미들웨어가 한다 — 우리가 require_csrf() 를 부르던 25곳이 사라지는 자리다. --}}
    @csrf

    <label>아이디
      {{-- old('username') = 방금 입력했던 값. 검증 실패로 되돌아왔을 때 다시 채워 준다 --}}
      <input type="text" name="username" required autofocus value="{{ old('username') }}" maxlength="20">
    </label>
    {{-- @error = 그 칸에 오류가 있으면 이 블록을 그린다 --}}
    @error('username')<span class="muted">{{ $message }}</span>@enderror

    <label>비밀번호
      {{-- type="password" = 입력 글자가 ●●●로 가려진다
           ★ 여기만은 절대 value 를 채우지 않는다. 되살리려면 어딘가에 평문으로 담아야 하는데,
             비밀번호는 그 '어딘가'가 존재해서는 안 되는 값이다. --}}
      <input type="password" name="password" required>
    </label>
    @error('password')<span class="muted">{{ $message }}</span>@enderror

    <button type="submit">로그인</button>
  </form>

  {{-- ── 구글로 로그인 ──────────────────────────────────────
       키가 .env 에 없으면 아예 보여주지 않는다 — 눌러도 안 되는 버튼을 두지 않는다. --}}
  @if (config('services.google.client_id'))
    <div class="auth-divider"><span>또는</span></div>

    <a class="btn-google" href="/auth/google">
      <svg viewBox="0 0 48 48" width="18" height="18" aria-hidden="true">
        <path fill="#4285F4" d="M45.1 24.5c0-1.6-.1-2.8-.4-4H24v7.3h12.1c-.2 2-1.6 5-4.5 7l-.1.3 6.5 5 .5.1c4.1-3.8 6.6-9.4 6.6-15.7"/>
        <path fill="#34A853" d="M24 46c5.9 0 10.9-1.9 14.5-5.3l-6.9-5.4c-1.9 1.3-4.4 2.2-7.6 2.2-5.8 0-10.7-3.8-12.4-9l-.3.1-6.8 5.2-.1.3C8 40.9 15.4 46 24 46"/>
        <path fill="#FBBC05" d="M11.6 28.5c-.5-1.4-.7-2.9-.7-4.5s.3-3.1.7-4.5v-.3l-6.9-5.3-.2.1C2.9 17 2 20.4 2 24s.9 7 2.5 10l7.1-5.5"/>
        <path fill="#EB4335" d="M24 10.5c4.1 0 6.9 1.8 8.5 3.3l6.2-6C34.9 4.4 29.9 2 24 2 15.4 2 8 7.1 4.5 14l7.1 5.5c1.7-5.2 6.6-9 12.4-9"/>
      </svg>
      <span>구글 계정으로 계속하기</span>
    </a>

    <p class="muted auth-google-note">
      처음이라면 계정이 자동으로 만들어집니다. 닉네임은 설정에서 바꿀 수 있습니다.
    </p>
  @endif

  <p class="muted">
    테스트 계정: <code>영화광 / 1234</code> · <code>해석러 / 1234</code> · <code>심야극장 / 1234</code><br>
    비밀번호를 잊으셨나요? <a href="/forgot-password">비밀번호 찾기</a><br>
    계정이 없나요? <a href="/register">회원가입</a>
  </p>
@endsection
