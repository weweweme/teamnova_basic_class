@extends('layouts.app')

@section('title', '글 목록')

@section('content')
  <h1>글 목록</h1>

  {{-- 로그인한 사람에게만 글쓰기 버튼을 보여준다.
       ★ 버튼을 숨기는 건 '보기 좋게' 하는 것일 뿐 방어가 아니다.
         실제 차단은 라우트의 auth 미들웨어가 한다. --}}
  @auth
    <p><a href="/posts/create">✏️ 글쓰기</a></p>
  @endauth
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

  {{-- 한 페이지 개수 — 고르면 기억한다 (쿠키 동의가 있을 때만) --}}
  <p class="muted">
    한 페이지에
    @foreach ([15, 30, 50] as $n)
      <a href="?per_page={{ $n }}">{{ $n === $perPage ? "[$n]" : $n }}</a>
    @endforeach
    개씩
  </p>
@endsection
