@extends('layouts.app')

@section('title', '탈퇴 대기 중인 계정')

@section('container', 'narrow')

@section('content')
  <h1>탈퇴 대기 중인 계정입니다</h1>

  <p class="muted">
    <strong>{{ $user->deleted_at->format('Y년 n월 j일') }}</strong>에 탈퇴하신 계정입니다.
    아직 개인정보는 지워지지 않았고, <strong>{{ $left }}일</strong> 뒤에 지워집니다.
  </p>

  <section class="settings-section">
    <h2>어떻게 할까요?</h2>

    <form method="post" action="/account/restore" class="restore-choice">
      @csrf

      <button type="submit" name="choice" value="restore" class="btn-settings restore-keep">
        <strong>계정 복구하고 로그인</strong>
        <span class="muted">탈퇴를 취소합니다. 아이디·이메일·설정이 그대로 돌아옵니다.</span>
      </button>

      <button type="submit" name="choice" value="erase" class="btn-settings restore-erase">
        <strong>지금 완전히 삭제</strong>
        <span class="muted">
          {{ $left }}일을 기다리지 않고 지금 지웁니다.
          아이디·이메일·구글 연결·프로필 사진이 사라지고 <strong>되돌릴 수 없습니다.</strong>
          쓰신 글과 댓글은 남고 작성자만 '탈퇴한 사용자'로 보입니다.
        </span>
      </button>
    </form>
  </section>

  <p class="muted">
    아무것도 고르지 않고 나가셔도 됩니다. 계정은 탈퇴 대기 상태 그대로입니다.
    <a href="/">홈으로</a>
  </p>
@endsection
