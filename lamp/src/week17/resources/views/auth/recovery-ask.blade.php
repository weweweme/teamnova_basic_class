@extends('layouts.app')

@section('title', '질문으로 비밀번호 찾기')

@section('container', 'narrow')

@section('content')
  <h1>질문으로 비밀번호 찾기</h1>

  <p class="muted">
    이메일을 등록하지 않으셨다면 이 방법으로 찾으실 수 있습니다.<br>
    설정에서 <strong>비밀번호 찾기 질문</strong>을 미리 정해 두신 경우에만 됩니다.
  </p>

  <form class="auth-form" method="post" action="/forgot-password/question">
    @csrf

    <label>아이디
      <input type="text" name="username" required autofocus maxlength="20" value="{{ old('username') }}">
    </label>
    @error('username')<span class="muted">{{ $message }}</span>@enderror

    <button type="submit">질문 보기</button>
  </form>

  <p class="muted">
    이메일을 등록하셨다면 <a href="/forgot-password">메일로 찾기</a>가 더 안전합니다.<br>
    <a href="/login">로그인으로 돌아가기</a>
  </p>
@endsection
