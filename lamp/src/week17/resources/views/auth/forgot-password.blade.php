@extends('layouts.app')

@section('title', '비밀번호 찾기')

@section('container', 'narrow')

@section('content')
  <h1>비밀번호 찾기</h1>
  <p class="muted">가입할 때 적은 이메일 주소를 넣어 주세요. 비밀번호를 새로 정하는 링크를 보내 드립니다.</p>

  <form class="auth-form" method="post" action="/forgot-password">
    @csrf

    <label>이메일
      <input type="email" name="email" required autofocus value="{{ old('email') }}" maxlength="100">
    </label>
    @error('email')<span class="muted">{{ $message }}</span>@enderror

    <button type="submit">링크 받기</button>
  </form>

  <p class="muted">
    {{-- ★ 가입 여부를 알려 주지 않는다. 있든 없든 같은 안내가 나간다 --}}
    등록되지 않은 주소라도 같은 안내가 표시됩니다. 남의 가입 여부를 확인하는 데 쓰이지 않도록 한 것입니다.<br>
    <a href="/login">로그인으로 돌아가기</a>
  </p>
@endsection
