@extends('layouts.app')
@section('title', $q === '' ? '검색' : "'{$q}' 검색")

@section('content')
  <h1>🔍 통합검색</h1>
  @include('search._bar')

  @if ($q === '')
    <p class="muted">작품 제목, 글 제목·내용, 유저 이름으로 찾습니다.</p>

    {{-- 최근 검색어 — 쿠키 동의(search)가 있을 때만 쌓인다 --}}
    @if ($recent)
      <p class="muted">최근 검색어:
        @foreach ($recent as $word)
          <a href="/search?q={{ urlencode($word) }}">{{ $word }}</a>@if (! $loop->last) · @endif
        @endforeach
      </p>
    @endif
  @else
    <section>
      <h2>📝 글 <a class="muted" href="/search/posts?q={{ urlencode($q) }}">더보기</a></h2>
      @forelse ($posts as $post)
        <p><a href="/posts/{{ $post->id }}">{{ $post->title }}</a>
          <small class="muted">{{ $post->author->nickname }} · 💬 {{ $post->comments_count }}</small></p>
      @empty
        <p class="muted">찾은 글이 없습니다.</p>
      @endforelse
    </section>

    <section>
      <h2>👤 유저 <a class="muted" href="/search/users?q={{ urlencode($q) }}">더보기</a></h2>
      @forelse ($users as $user)
        <p>{{ $user->nickname }} <small class="muted">@ {{ $user->username }}</small></p>
      @empty
        <p class="muted">찾은 유저가 없습니다.</p>
      @endforelse
    </section>

    <section>
      <h2>🎬 작품 <a class="muted" href="/search/works?q={{ urlencode($q) }}">더보기</a></h2>
      @forelse ($works as $work)
        <p><a href="/works/{{ $work->slug }}">{{ $work->title }}</a>
          <small class="muted">글 {{ $work->posts_count }}개</small></p>
      @empty
        <p class="muted">우리 게시판에 없는 작품입니다. <a href="/search/works?q={{ urlencode($q) }}">작품 검색에서 찾아보기</a></p>
      @endforelse
    </section>
  @endif
@endsection
