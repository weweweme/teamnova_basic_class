@extends('layouts.app')
@section('title', "'{$q}' 작품 검색")

@section('content')
  <h1>🔍 작품 검색</h1>
  @include('search._bar', ['action' => '/search/works'])

  @if ($q !== '')
    <h2>우리 게시판에 있는 작품</h2>
    @forelse ($ours as $work)
      <p><a href="/works/{{ $work->slug }}">{{ $work->title }}</a>
        <small class="muted">글 {{ $work->posts_count }}개</small></p>
    @empty
      <p class="muted">없습니다.</p>
    @endforelse

    <h2>TMDB 검색 결과</h2>
    <p class="muted">아직 우리 게시판에 글이 없는 작품입니다.</p>
    @forelse ($fromTmdb as $m)
      <p>
        @if ($m['poster_url'])<img src="{{ $m['poster_url'] }}" alt="" width="40" loading="lazy">@endif
        {{ $m['title'] }} <small class="muted">{{ $m['year'] }}</small>
      </p>
    @empty
      <p class="muted">TMDB에서도 찾지 못했습니다.</p>
    @endforelse
  @endif
@endsection
