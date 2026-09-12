@extends('layouts.app')

@section('title', $q === '' ? '작품 검색' : "'{$q}' 작품 검색")

@section('container', 'narrow')

@section('content')
  <h1>🔍 통합검색</h1>

  @include('partials.search-bar', ['action' => '/search/works'])

  @if ($q === '')
    <p class="muted">영화·드라마 제목으로 검색해 보세요. (예: 기생충, 인셉션)</p>
  @elseif (! $works)
    <p class="muted">'{{ $q }}'와 일치하는 작품이 없습니다.</p>
  @else
    <p class="muted">🎬 작품 {{ count($works) }}개</p>
    @include('partials.search-works')
  @endif
@endsection
