@extends('layouts.app')

@section('title', $q === '' ? '통합검색' : "'{$q}' 검색")

@section('container', 'narrow')

@section('content')
  <h1>🔍 통합검색</h1>

  @include('partials.search-bar', ['action' => '/search'])

  {{-- 최근 검색어 — 쿠키 동의(search)가 있을 때만 쌓인다 --}}
  @if ($recent)
    <div class="recent-searches">
      <span class="muted">최근 검색어</span>
      @foreach ($recent as $past)
        <a href="/search?q={{ urlencode($past) }}">{{ $past }}</a>
      @endforeach
    </div>
  @endif

  @if ($q === '')
    <p class="muted">작품 · 글 · 유저를 한 번에 검색합니다. (예: 기생충, 인생 영화, 영화광)</p>
  @else
    <section class="search-section">
      <div class="search-section-head">
        <h2>🎬 작품</h2>
        @if ($works)
          <a class="search-more" href="/search/works?q={{ urlencode($q) }}">더보기 ›</a>
        @endif
      </div>
      @if ($works)
        @include('partials.search-works')
      @else
        <p class="muted search-empty">일치하는 작품이 없습니다.</p>
      @endif
    </section>

    <section class="search-section">
      <div class="search-section-head">
        <h2>📝 글 <span class="count">{{ $postTotal }}</span></h2>
        @if ($postTotal > $posts->count())
          <a class="search-more" href="/search/posts?q={{ urlencode($q) }}">더보기 ›</a>
        @endif
      </div>
      @if ($postTotal > 0)
        @include('partials.search-posts')
      @else
        <p class="muted search-empty">제목·내용에 '{{ $q }}'가 들어간 글이 없습니다.</p>
      @endif
    </section>

    <section class="search-section">
      <div class="search-section-head">
        <h2>👤 유저 <span class="count">{{ $userTotal }}</span></h2>
        @if ($userTotal > $users->count())
          <a class="search-more" href="/search/users?q={{ urlencode($q) }}">더보기 ›</a>
        @endif
      </div>
      @if ($userTotal > 0)
        @include('partials.search-users')
      @else
        <p class="muted search-empty">아이디·닉네임이 일치하는 회원이 없습니다.</p>
      @endif
    </section>
  @endif
@endsection
