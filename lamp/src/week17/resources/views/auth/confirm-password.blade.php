@extends('layouts.app')

@section('title', '비밀번호 확인')

@section('container', 'narrow')

@section('content')
  <h1>🔐 비밀번호 확인</h1>
  <p class="muted">
    계정 정보를 다루는 화면입니다.<br>
    로그인한 채로 자리를 비운 사이 다른 사람이 들어오는 것을 막기 위해, 비밀번호를 한 번 더 확인합니다.
  </p>
  <p class="muted">
    확인하면 <strong>{{ (int) (config('auth.password_timeout') / 60) }}분</strong> 동안 다시 묻지 않습니다.
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
