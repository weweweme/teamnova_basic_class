@extends('layouts.app')
@section('title', '비밀번호 확인')

@section('content')
  <h1>비밀번호 확인</h1>
  <p class="muted">민감한 설정을 바꾸기 전에 본인 확인을 한 번 더 합니다.</p>

  <form method="post" action="/confirm-password">
    @csrf
    <p>
      <label>비밀번호 <input type="password" name="password" required autofocus></label>
      @error('password')<span class="muted">{{ $message }}</span>@enderror
    </p>
    <button type="submit">확인</button>
  </form>
@endsection
