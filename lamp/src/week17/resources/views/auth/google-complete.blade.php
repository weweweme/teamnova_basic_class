@extends('layouts.app')

@section('title', '가입 마무리')

@section('container', 'narrow')

@section('content')
  <h1>가입 마무리</h1>

  <p class="muted">
    구글 계정 확인이 끝났습니다. 아래 내용으로 계정을 만듭니다.
  </p>

  <section class="settings-section">
    <dl class="settings-info">
      <dt>구글 계정</dt>
      <dd>{{ $profile['email'] }}</dd>

      <dt>아이디</dt>
      <dd>
        {{ $username }}
        <br><span class="muted">주소와 글 작성자 표시에 쓰입니다. 나중에 바꿀 수 없습니다.</span>
      </dd>

      <dt>닉네임</dt>
      <dd>
        {{ $nickname }}
        <br><span class="muted">글에 보이는 이름입니다. 설정에서 바꿀 수 있습니다.</span>
      </dd>
    </dl>
  </section>

  <form class="auth-form" method="post" action="{{ route('google.complete') }}">
    @csrf

    {{-- ★ 구글에서 받은 동의는 '구글이 우리에게 정보를 넘겨도 되는가'에 대한 것이다.
         우리가 그 정보를 무슨 목적으로 쓰는가는 여기서 따로 받는다. --}}
    <label class="auth-check">
      <input type="checkbox" name="agree" value="1" @checked(old('agree'))>
      <span><a href="/terms" target="_blank">이용약관</a>과
            <a href="/privacy" target="_blank">개인정보처리방침</a>에 동의합니다</span>
    </label>
    @error('agree')<span class="muted">{{ $message }}</span>@enderror

    <button type="submit">동의하고 가입하기</button>
  </form>

  <p class="muted">
    동의하지 않으시면 계정이 만들어지지 않습니다. <a href="/login">로그인 화면으로</a>
  </p>
@endsection
