@extends('layouts.app')

@section('title', $q === '' ? '유저 검색' : "'{$q}' 유저 검색")

@section('container', 'narrow')

@section('content')
  <h1>🔍 통합검색</h1>

  @include('partials.search-bar', ['action' => '/search/users'])

  @if ($q === '')
    <p class="muted">아이디나 닉네임으로 찾습니다. (예: 영화광)</p>
  @elseif ($users->total() === 0)
    <p class="muted">'{{ $q }}'와 일치하는 회원이 없습니다.</p>
  @else
    <p class="muted">👤 유저 {{ $users->total() }}명 · {{ $users->currentPage() }}/{{ $users->lastPage() }} 페이지</p>
    @include('partials.search-users')
    {{ $users->links() }}
  @endif
@endsection
