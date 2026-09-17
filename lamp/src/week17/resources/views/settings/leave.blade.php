@extends('layouts.app')

@section('title', '회원 탈퇴')

@section('container', 'narrow')

@section('content')
  <h1>회원 탈퇴</h1>

  <section class="settings-section">
    <h2>탈퇴하면 이렇게 됩니다</h2>

    <dl class="settings-info leave-info">
      <dt>내가 쓴 글·댓글</dt>
      <dd>
        그대로 남고, 작성자만 <strong>탈퇴한 사용자</strong>로 보입니다.<br>
        <span class="muted">지우면 남의 글에 달린 대화가 중간에 끊기기 때문입니다.</span>
      </dd>

      <dt>로그인</dt>
      <dd>바로 막힙니다. 로그인 중인 다른 기기도 모두 해제됩니다.</dd>

      <dt>되돌리기</dt>
      <dd>
        <strong>{{ $days }}일</strong> 안에 다시 로그인하시면 계정이 그대로 돌아옵니다.<br>
        <span class="muted">그 기간이 지나면 아이디·이메일·프로필 사진이 지워지고 되돌릴 수 없습니다.</span>
      </dd>

      @if ($posts > 0)
        <dt>지금 쓰신 글</dt>
        <dd>{{ $posts }}개 — 위 규칙대로 남습니다.</dd>
      @endif
    </dl>
  </section>

  <section class="settings-section">
    <h2>확인</h2>
    <p class="muted">
      정말 탈퇴하시려면 아래에 아이디 <code>{{ $me->username }}</code> 를 입력해 주세요.
    </p>

    <form class="settings-form settings-form-col" method="post" action="/settings/leave">
      @csrf
      @method('DELETE')

      <label>아이디
        <input type="text" name="username" autocomplete="off" placeholder="{{ $me->username }}" required>
      </label>
      @error('username')<span class="muted">{{ $message }}</span>@enderror

      <button type="submit" class="btn-danger">탈퇴하기</button>
    </form>

    <p class="muted"><a href="/settings">← 설정으로 돌아가기</a></p>
  </section>
@endsection
