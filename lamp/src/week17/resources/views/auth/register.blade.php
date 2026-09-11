@extends('layouts.app')

@section('title', '회원가입')

@section('content')
  <h1>회원가입</h1>

  <form method="post" action="/register">
    @csrf

    <p>
      <label>아이디
        <input type="text" name="username" value="{{ old('username') }}" maxlength="20" required autofocus>
      </label>
      @error('username')<span class="muted">{{ $message }}</span>@enderror
    </p>

    <p>
      <label>비밀번호 (4자 이상)
        <input type="password" name="password" minlength="4" required>
      </label>
      @error('password')<span class="muted">{{ $message }}</span>@enderror
    </p>

    <button type="submit">가입하기</button>
  </form>
@endsection
