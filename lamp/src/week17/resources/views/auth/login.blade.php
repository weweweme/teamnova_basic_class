@extends('layouts.app')

@section('title', '로그인')

@section('content')
  <h1>로그인</h1>

  <form method="post" action="/login">
    {{-- ★ @csrf 한 줄이면 숨김 토큰이 들어간다.
         검사는 미들웨어가 한다 — 우리가 require_csrf() 를 부르던 26곳이 사라지는 자리다. --}}
    @csrf

    <p>
      <label>아이디
        {{-- old('username') = 방금 입력했던 값. 검증 실패로 되돌아왔을 때 다시 채워 준다 --}}
        <input type="text" name="username" value="{{ old('username') }}" maxlength="20" required autofocus>
      </label>
      {{-- @error = 그 칸에 오류가 있으면 이 블록을 그린다 --}}
      @error('username')
        <span class="muted">{{ $message }}</span>
      @enderror
    </p>

    <p>
      <label>비밀번호
        <input type="password" name="password" required>
      </label>
      @error('password')
        <span class="muted">{{ $message }}</span>
      @enderror
    </p>

    <button type="submit">로그인</button>
  </form>
@endsection
