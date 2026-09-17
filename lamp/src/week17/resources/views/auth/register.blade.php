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

    {{-- ★ 한 번 더 받는다. 여기서 오타가 나면 되돌릴 방법이 없다 —
         이메일을 안 받으므로 비밀번호 찾기도 쓸 수 없다.
         비밀번호 재설정 화면에는 이미 이 칸이 있었는데 가입에만 없었다. --}}
    <label>비밀번호 확인
      <input type="password" name="password_confirmation" required minlength="4">
    </label>
    @error('password_confirmation')<span class="muted">{{ $message }}</span>@enderror

    {{-- ★ 약관 동의 — 체크만 받고 끝내지 않는다. 누가 언제 무엇에 동의했는지 기록한다.
         쿠키 동의와 같은 표(consent_log)에 남는다. --}}
    <label class="settings-check">
      <input type="checkbox" name="agree" value="1" @checked(old('agree'))>
      <span><a href="/terms" target="_blank">이용약관</a>과
            <a href="/privacy" target="_blank">개인정보처리방침</a>에 동의합니다</span>
    </label>
    @error('agree')<span class="muted">{{ $message }}</span>@enderror

    <button type="submit">가입하기</button>
  </form>

  {{-- 로그인 화면과 같은 길을 여기에도 둔다 — 계정을 만들러 온 사람이 보는 화면이다 --}}
  @if (config('services.google.client_id'))
    <div class="auth-divider"><span>또는</span></div>

    <a class="btn-google" href="/auth/google">
      <svg viewBox="0 0 48 48" width="18" height="18" aria-hidden="true">
        <path fill="#4285F4" d="M45.1 24.5c0-1.6-.1-2.8-.4-4H24v7.3h12.1c-.2 2-1.6 5-4.5 7l-.1.3 6.5 5 .5.1c4.1-3.8 6.6-9.4 6.6-15.7"/>
        <path fill="#34A853" d="M24 46c5.9 0 10.9-1.9 14.5-5.3l-6.9-5.4c-1.9 1.3-4.4 2.2-7.6 2.2-5.8 0-10.7-3.8-12.4-9l-.3.1-6.8 5.2-.1.3C8 40.9 15.4 46 24 46"/>
        <path fill="#FBBC05" d="M11.6 28.5c-.5-1.4-.7-2.9-.7-4.5s.3-3.1.7-4.5v-.3l-6.9-5.3-.2.1C2.9 17 2 20.4 2 24s.9 7 2.5 10l7.1-5.5"/>
        <path fill="#EB4335" d="M24 10.5c4.1 0 6.9 1.8 8.5 3.3l6.2-6C34.9 4.4 29.9 2 24 2 15.4 2 8 7.1 4.5 14l7.1 5.5c1.7-5.2 6.6-9 12.4-9"/>
      </svg>
      <span>구글 계정으로 가입하기</span>
    </a>

    <p class="muted auth-google-note">
      구글로 가입하시면 <a href="/terms">이용약관</a>과
      <a href="/privacy">개인정보처리방침</a>에 동의하신 것으로 봅니다.
    </p>
  @endif

  <p class="muted">
    이미 계정이 있나요? <a href="/login">로그인</a>
  </p>
@endsection
