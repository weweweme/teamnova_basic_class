@extends('layouts.app')

@section('title', '회원가입')

@section('container', 'narrow')

@section('content')
  <h1>회원가입</h1>

  <form class="auth-form" method="post" action="/register">
    @csrf

    <label>아이디
      <input type="text" name="username" required autofocus value="{{ old('username') }}" maxlength="20">
    </label>
    @error('username')<span class="muted">{{ $message }}</span>@enderror

    <label>비밀번호
      {{-- minlength = 최소 글자 수(브라우저 1차 검사). 서버에서도 다시 확인한다. --}}
      <input type="password" name="password" required minlength="4">
    </label>
    @error('password')<span class="muted">{{ $message }}</span>@enderror

    <button type="submit">가입하기</button>
  </form>

  <p class="muted">
    이미 계정이 있나요? <a href="/login">로그인</a>
  </p>
@endsection
