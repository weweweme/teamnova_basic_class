@extends('layouts.app')
@section('title', $user->nickname . ' 님의 프로필')

@section('content')
  <h1>
    @if ($user->avatar)
      <img src="{{ $user->avatar }}" alt="" width="48" height="48">
    @endif
    {{ $user->nickname }}
  </h1>
  <p class="muted">@ {{ $user->username }} · {{ $user->joined_at?->format('Y-m-d') }} 가입 · 글 {{ $posts->total() }}개</p>

  <ul class="post-list board-list">
    @foreach ($posts as $post)
      <li>
        <span class="post-left">
          <a href="/posts/{{ $post->id }}">{{ $post->title }}</a>
          <span class="tag">{{ $post->media->title }}</span>
          <span class="post-comments">💬 {{ $post->comments_count }}</span>
        </span>
        <span class="post-right">👍 {{ $post->likers_count }} · {{ $post->created_at->diffForHumans() }}</span>
      </li>
    @endforeach
  </ul>

  {{ $posts->links() }}
@endsection
