@extends('layouts.app')

@section('title', '비밀번호 확인')

@section('container', 'narrow')

@section('content')
  <h1>🔐 비밀번호 확인</h1>
  <p class="muted">
    되돌릴 수 없는 작업이라 한 번 더 확인합니다.
    확인하면 <strong>{{ (int) (config('auth.password_timeout') / 60) }}분</strong> 동안은 다시 묻지 않아요.
  </p>

  <form class="settings-form settings-form-col" method="post" action="/confirm-password">
    @csrf
    <label>비밀번호
      <input type="password" name="password" required autofocus>
    </label>
    @error('password')<span class="muted">{{ $message }}</span>@enderror
    <button type="submit">확인</button>
  </form>

  <p class="muted"><a href="/settings">← 설정으로 돌아가기</a></p>
@endsection
