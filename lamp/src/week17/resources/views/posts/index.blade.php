@extends('layouts.app')

@section('title', '글 목록')

@section('content')
  <h1 class="narrow-title">전체 글</h1>

  {{-- 정렬 탭 · 감상 필터 · 도구줄 · 글 목록 · 페이지 이동은
       작품 게시판과 완전히 같은 화면이라 조각 하나로 같이 쓴다. --}}
  @include('partials.board', ['writeUrl' => '/posts/create'])
@endsection
