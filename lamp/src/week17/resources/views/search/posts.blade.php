@extends('layouts.app')

@section('title', $q === '' ? '글 검색' : "'{$q}' 글 검색")

@section('container', 'narrow')

@section('content')
  <h1>🔍 통합검색</h1>

  @include('partials.search-bar', ['action' => '/search/posts'])

  @if ($q === '')
    <p class="muted">글 제목과 내용에서 찾습니다. (예: 인생 영화, 결말)</p>
  @elseif ($posts->total() === 0)
    <p class="muted">'{{ $q }}'가 들어간 글이 없습니다.</p>
  @else
    <p class="muted">📝 글 {{ $posts->total() }}개 · {{ $posts->currentPage() }}/{{ $posts->lastPage() }} 페이지</p>
    @include('partials.search-posts')
    {{ $posts->links() }}
  @endif
@endsection
