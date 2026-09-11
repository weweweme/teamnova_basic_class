@extends('layouts.app')
@section('title', '랭킹')

@section('content')
  <h1>🏆 랭킹</h1>

  <section>
    <h2>명예의 전당 — 유저</h2>
    <p class="muted">받은 추천이 많은 순, 같으면 글이 많은 순</p>
    <ol class="rank-list">
      @foreach ($users as $user)
        <li>
          <b>{{ $user->nickname }}</b>
          <small class="muted">글 {{ $user->posts_count }}개 · 받은 추천 {{ $user->likes_received_count }}</small>
        </li>
      @endforeach
    </ol>
  </section>

  <section>
    <h2>인기 글</h2>
    <ol class="rank-list">
      @foreach ($posts as $post)
        <li>
          <a href="/posts/{{ $post->id }}">{{ $post->title }}</a>
          <small class="muted">
            {{ $post->author->nickname }} · 👍 {{ $post->likers_count }} · 💬 {{ $post->comments_count }} · 조회 {{ $post->views }}
          </small>
        </li>
      @endforeach
    </ol>
  </section>

  <section>
    <h2>글이 많은 작품</h2>
    <ol class="rank-list">
      @foreach ($works as $work)
        <li>
          <a href="/works/{{ $work->slug }}">{{ $work->title }}</a>
          <small class="muted">글 {{ $work->posts_count }}개</small>
        </li>
      @endforeach
    </ol>
  </section>
@endsection
