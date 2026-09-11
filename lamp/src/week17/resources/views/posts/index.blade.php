@extends('layouts.app')

@section('title', '글 목록')

@section('content')
  <h1>글 목록</h1>
  <p class="muted">
    전체 {{ $posts->total() }}개 중 {{ $posts->currentPage() }}/{{ $posts->lastPage() }} 페이지
  </p>

  {{-- ★ 클래스 이름과 구조를 기존 board/index.php 와 똑같이 맞춘다.
       style.css 는 '.post-list li > .post-left / .post-right' 구조를 전제로 쓰여 있어서,
       이름만 같고 구조가 다르면 스타일이 어긋난다. --}}
  <ul class="post-list board-list">
    @foreach ($posts as $post)
      <li>
        <span class="post-left">
          <a href="/posts/{{ $post->id }}" title="{{ $post->title }}">{{ $post->title }}</a>
          <span class="tag">{{ $post->sentiment }}</span>
          <span class="post-comments">💬 {{ $post->comments_count }}</span>
        </span>
        <span class="post-right">
          {{ $post->author->nickname }}
          · <time datetime="{{ $post->created_at->toIso8601String() }}"
                  title="{{ $post->created_at->format('Y-m-d H:i') }} 작성">{{ $post->created_at->diffForHumans() }}</time>
        </span>
      </li>
    @endforeach
  </ul>

  {{ $posts->links() }}
@endsection
