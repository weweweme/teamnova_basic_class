@extends('layouts.app')
@section('title', '리뷰 커뮤니티')

@section('content')
  <h1>🎬 영화·드라마 리뷰 커뮤니티</h1>

  <section>
    <h2>우리 커뮤니티에서 이야기 중</h2>
    <ul class="work-row">
      @forelse ($works as $work)
        <li>
          <a href="/works/{{ $work->slug }}">
            @if ($work->poster_url)<img src="{{ $work->poster_url }}" alt="" width="80" loading="lazy">@endif
            <span>{{ $work->title }}</span>
          </a>
          <small class="muted">글 {{ $work->posts_count }}개</small>
        </li>
      @empty
        <li class="muted">아직 글이 없습니다.</li>
      @endforelse
    </ul>
  </section>

  <section>
    <h2>지금 뜨는 글</h2>
    <ul class="post-list">
      @foreach ($hot as $post)
        <li>
          <a href="/posts/{{ $post->id }}">{{ $post->title }}</a>
          <small class="muted">{{ $post->media->title }} · 조회 {{ $post->views }} · 💬 {{ $post->comments_count }}</small>
        </li>
      @endforeach
    </ul>
  </section>

  <section>
    <h2>최근 글</h2>
    <ul class="post-list">
      @foreach ($recent as $post)
        <li>
          <a href="/posts/{{ $post->id }}">{{ $post->title }}</a>
          <small class="muted">{{ $post->author->nickname }} · {{ $post->created_at->diffForHumans() }}</small>
        </li>
      @endforeach
    </ul>
    <p><a href="/posts">글 전체 보기 →</a></p>
  </section>

  <section>
    <h2>이번 주 인기작 (TMDB)</h2>
    {{-- 아직 우리 게시판에 없는 작품도 있다. 둘러보기용 줄이다 --}}
    <ul class="work-row">
      @forelse ($trending as $m)
        <li>
          @if ($m['poster_url'])<img src="{{ $m['poster_url'] }}" alt="" width="80" loading="lazy">@endif
          <span>{{ $m['title'] }}</span>
          <small class="muted">{{ $m['year'] }}</small>
        </li>
      @empty
        <li class="muted">지금은 불러올 수 없습니다.</li>
      @endforelse
    </ul>
  </section>
@endsection
