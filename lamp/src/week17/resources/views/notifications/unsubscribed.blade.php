@extends('layouts.app')

@section('title', '알림을 껐습니다')

@section('container', 'narrow')

@section('content')
  <h1>알림을 껐습니다</h1>

  <p class="muted">
    {{ $user->nickname }}님, 앞으로 댓글·답글 알림 메일을 보내지 않습니다.
  </p>

  <p class="muted">
    다시 받으시려면 <a href="/settings">설정</a>에서 켜실 수 있습니다.<br>
    새 기기 로그인과 비밀번호 변경 같은 <strong>보안 알림은 계속 갑니다.</strong>
    본인이 하지 않은 일을 알리는 것이라 끌 수 없습니다.
  </p>

  <p><a href="/">← 홈으로</a></p>
@endsection
