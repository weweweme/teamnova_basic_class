@extends('layouts.app')

@section('title', '질문에 답하기')

@section('container', 'narrow')

@section('content')
  <h1>질문에 답해 주세요</h1>

  <section class="settings-section">
    <p class="muted">아이디 <code>{{ $user->username }}</code> 의 질문입니다.</p>
    <p class="recovery-question">{{ $user->recovery_question }}</p>
  </section>

  <form class="auth-form" method="post" action="/forgot-password/question/verify">
    @csrf
    <input type="hidden" name="username" value="{{ $user->username }}">

    <label>답
      <input type="text" name="answer" required autofocus maxlength="100" autocomplete="off" value="{{ old('answer') }}">
    </label>
    @error('answer')<span class="muted">{{ $message }}</span>@enderror

    <label>새 비밀번호
      <input type="password" name="password" required minlength="4">
    </label>
    @error('password')<span class="muted">{{ $message }}</span>@enderror

    <label>새 비밀번호 확인
      <input type="password" name="password_confirmation" required minlength="4">
    </label>

    <button type="submit">비밀번호 바꾸기</button>
  </form>

  <p class="muted">
    답이 맞으면 비밀번호가 바뀌고 <strong>로그인 중인 모든 기기가 해제됩니다.</strong><br>
    <a href="/login">로그인으로 돌아가기</a>
  </p>
@endsection
