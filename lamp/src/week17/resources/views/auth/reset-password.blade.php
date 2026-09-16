@extends('layouts.app')

@section('title', '새 비밀번호 정하기')

@section('container', 'narrow')

@section('content')
  <h1>새 비밀번호 정하기</h1>

  <form class="auth-form" method="post" action="/reset-password">
    @csrf
    {{-- 메일 링크에 실려 온 토큰. 화면에는 안 보이지만 함께 전송된다 --}}
    <input type="hidden" name="token" value="{{ $token }}">

    <label>이메일
      <input type="email" name="email" required value="{{ old('email', $email) }}" maxlength="100">
    </label>
    @error('email')<span class="muted">{{ $message }}</span>@enderror

    <label>새 비밀번호
      <input type="password" name="password" required minlength="4">
    </label>
    @error('password')<span class="muted">{{ $message }}</span>@enderror

    <label>새 비밀번호 확인
      <input type="password" name="password_confirmation" required minlength="4">
    </label>

    <button type="submit">비밀번호 바꾸기</button>
  </form>
@endsection
