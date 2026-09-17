@extends('layouts.app')

@section('title', '본인 확인')

@section('container', 'narrow')

@section('content')
  @php($viaGoogle = (bool) auth()->user()->google_id)

  <h1>🔐 본인 확인</h1>
  <p class="muted">
    계정 정보를 다루는 화면입니다.<br>
    로그인한 채로 자리를 비운 사이 다른 사람이 들어오는 것을 막기 위해, 본인이 맞는지 한 번 더 확인합니다.
  </p>
  <p class="muted">
    @if ($viaGoogle)
      {{-- 구글로 가입한 계정에는 비밀번호가 없다. 로그인할 때 쓴 그 수단으로 확인한다. --}}
      아래 버튼을 누르면 <strong>구글이 비밀번호를 다시 묻습니다.</strong>
    @endif
    확인하면 <strong>{{ (int) (config('auth.password_timeout') / 60) }}분</strong> 동안 다시 묻지 않습니다.
  </p>

  {{-- ★ 구글로 가입한 사람은 비밀번호를 모른다(가입할 때 난수로 채운다).
       그런 계정에는 비밀번호 칸을 보여 주지 않는다 — 채울 수 없는 칸이다.
       로그인할 때 쓴 그 수단으로 다시 확인받는다. --}}
  @if ($viaGoogle)
    <a class="btn-google" href="{{ route('google.confirm') }}">
      <svg viewBox="0 0 48 48" width="18" height="18" aria-hidden="true">
        <path fill="#4285F4" d="M45.1 24.5c0-1.6-.1-2.8-.4-4H24v7.3h12.1c-.2 2-1.6 5-4.5 7l-.1.3 6.5 5 .5.1c4.1-3.8 6.6-9.4 6.6-15.7"/>
        <path fill="#34A853" d="M24 46c5.9 0 10.9-1.9 14.5-5.3l-6.9-5.4c-1.9 1.3-4.4 2.2-7.6 2.2-5.8 0-10.7-3.8-12.4-9l-.3.1-6.8 5.2-.1.3C8 40.9 15.4 46 24 46"/>
        <path fill="#FBBC05" d="M11.6 28.5c-.5-1.4-.7-2.9-.7-4.5s.3-3.1.7-4.5v-.3l-6.9-5.3-.2.1C2.9 17 2 20.4 2 24s.9 7 2.5 10l7.1-5.5"/>
        <path fill="#EB4335" d="M24 10.5c4.1 0 6.9 1.8 8.5 3.3l6.2-6C34.9 4.4 29.9 2 24 2 15.4 2 8 7.1 4.5 14l7.1 5.5c1.7-5.2 6.6-9 12.4-9"/>
      </svg>
      <span>구글 계정으로 본인 확인</span>
    </a>
  @else
    <form class="settings-form settings-form-col" method="post" action="/confirm-password">
      @csrf
      <label>비밀번호
        <input type="password" name="password" required autofocus>
      </label>
      @error('password')<span class="muted">{{ $message }}</span>@enderror
      <button type="submit">확인</button>
    </form>
  @endif

  <p class="muted"><a href="/settings">← 설정으로 돌아가기</a></p>
@endsection
