@extends('layouts.app')
@section('title', '기기 확인 필요')

@section('content')
  <h1>기기 확인이 필요합니다</h1>
  @if ($reason === 'key_missing')
    <p>이 기기에는 아직 도장이 등록되어 있지 않습니다. 다시 로그인하면 등록됩니다.</p>
  @else
    <p>마지막 기기 확인이 만료되었습니다. 잠시 후 자동으로 다시 확인합니다.</p>
  @endif
  <p class="muted">이 화면은 JavaScript가 꺼져 있으면 계속 보일 수 있습니다.</p>
  <p><a href="/login">로그인 화면으로</a></p>
@endsection
