@extends('layouts.app')
@section('title', "'{$q}' 유저 검색")

@section('content')
  <h1>🔍 유저 검색</h1>
  @include('search._bar', ['action' => '/search/users'])

  @if ($q !== '')
    <p class="muted">'{{ $q }}' 결과 {{ $users->total() }}명</p>
    <ul>
      @foreach ($users as $user)
        <li>{{ $user->nickname }} <small class="muted">@ {{ $user->username }}</small></li>
      @endforeach
    </ul>
    {{ $users->links() }}
  @endif
@endsection
