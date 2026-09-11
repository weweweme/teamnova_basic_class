@extends('layouts.app')
@section('title', "'{$q}' 글 검색")

@section('content')
  <h1>🔍 글 검색</h1>
  @include('search._bar', ['action' => '/search/posts'])

  @if ($q === '')
    <p class="muted">글 제목과 내용에서 찾습니다. (예: 인생 영화, 결말)</p>
  @else
    <p class="muted">'{{ $q }}' 결과 {{ $posts->total() }}개</p>
    <ul class="post-list board-list">
      @foreach ($posts as $post)
        <li>
          <span class="post-left">
            <a href="/posts/{{ $post->id }}">{{ $post->title }}</a>
            <span class="post-comments">💬 {{ $post->comments_count }}</span>
          </span>
          <span class="post-right">{{ $post->author->nickname }} · {{ $post->created_at->diffForHumans() }}</span>
        </li>
      @endforeach
    </ul>
    {{ $posts->links() }}
  @endif
@endsection
