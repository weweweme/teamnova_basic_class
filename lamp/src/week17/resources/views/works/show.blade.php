@extends('layouts.app')

@section('title', $media->title)

@section('content')
  <article class="work-header">
    @if ($media->poster_url)
      <img src="{{ $media->poster_url }}" alt="" width="120" loading="lazy">
    @endif
    <h1>{{ $media->title }}</h1>
    <p class="muted">
      {{ $media->year }}{{ $media->director ? ' · ' . $media->director : '' }}{{ $media->genre ? ' · ' . $media->genre : '' }}
    </p>
    @if ($media->overview)
      <p>{{ $media->overview }}</p>
    @endif

    {{-- 감상 투표 --}}
    @php $total = $up + $down; @endphp
    <p class="vote">
      👍 {{ $up }}{{ $total ? ' (' . round($up / $total * 100) . '%)' : '' }}
      · 👎 {{ $down }}{{ $total ? ' (' . round($down / $total * 100) . '%)' : '' }}
    </p>

    @auth
      <form method="post" action="/works/{{ $media->slug }}/vote">
        @csrf
        {{-- 같은 걸 다시 누르면 취소, 다른 걸 누르면 바꿔 투표 --}}
        <button name="choice" value="추천" @class(['on' => $myVote === '추천'])>👍 추천</button>
        <button name="choice" value="비추천" @class(['on' => $myVote === '비추천'])>👎 비추천</button>
      </form>
      @if ($myVote)
        <p class="muted">내 투표: {{ $myVote }} (같은 걸 다시 누르면 취소됩니다)</p>
      @endif
    @endauth
  </article>

  <hr>

  <h2>이 작품의 글 {{ $posts->total() }}개</h2>

  @auth
    <p><a href="/posts/create">✏️ 글쓰기</a></p>
  @endauth

  <ul class="post-list board-list">
    @foreach ($posts as $post)
      <li>
        <span class="post-left">
          <a href="/posts/{{ $post->id }}">{{ $post->title }}</a>
          <span class="tag">{{ $post->sentiment }}</span>
          <span class="post-comments">💬 {{ $post->comments_count }}</span>
        </span>
        <span class="post-right">
          {{ $post->author->nickname }} · {{ $post->created_at->diffForHumans() }}
        </span>
      </li>
    @endforeach
  </ul>

  {{ $posts->links() }}
@endsection
