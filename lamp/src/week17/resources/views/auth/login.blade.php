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

  <p class="muted">
    테스트 계정: <code>영화광 / 1234</code> · <code>해석러 / 1234</code> · <code>심야극장 / 1234</code><br>
    계정이 없나요? <a href="/register">회원가입</a>
  </p>
@endsection
