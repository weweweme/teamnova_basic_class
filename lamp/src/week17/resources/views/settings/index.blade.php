@extends('layouts.app')
@section('title', '설정')

@section('content')
  <h1>⚙️ 설정</h1>
  <p class="muted">{{ auth()->user()->username }} 님 · {{ auth()->user()->joined_at->format('Y-m-d') }} 가입</p>

  <section>
    <h2>닉네임</h2>
    <form method="post" action="/settings/nickname">
      @csrf
      @method('PATCH')
      <input type="text" name="nickname" value="{{ old('nickname', auth()->user()->nickname) }}" maxlength="20" required>
      <button type="submit">변경</button>
      @error('nickname')<span class="muted">{{ $message }}</span>@enderror
    </form>
  </section>

  <section>
    <h2>비밀번호 변경</h2>
    <p class="muted">비밀번호를 바꾸면 다른 기기의 로그인은 자동으로 해제됩니다.</p>
    <form method="post" action="/settings/password">
      @csrf
      @method('PATCH')
      <p><label>현재 비밀번호 <input type="password" name="current" required></label>
        @error('current')<span class="muted">{{ $message }}</span>@enderror</p>
      <p><label>새 비밀번호 <input type="password" name="new" minlength="4" required></label>
        @error('new')<span class="muted">{{ $message }}</span>@enderror</p>
      <p><label>새 비밀번호 확인 <input type="password" name="new_confirmation" minlength="4" required></label></p>
      <button type="submit">변경</button>
    </form>
  </section>

  <section>
    <h2>다른 기기 로그아웃</h2>
    <p class="muted">이 기기의 로그인은 유지하고, 다른 곳에서 로그인된 것만 끊습니다.</p>
    <form method="post" action="/settings/logout-others">
      @csrf
      <p><label>비밀번호 확인 <input type="password" name="password" required></label>
        @error('password')<span class="muted">{{ $message }}</span>@enderror</p>
      <button type="submit">다른 기기 모두 로그아웃</button>
    </form>
  </section>
@endsection
