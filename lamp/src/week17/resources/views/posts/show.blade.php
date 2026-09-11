@extends('layouts.app')

@section('title', $post->title)

@section('content')
  <article>
    <h1>{{ $post->title }}</h1>
    <p class="muted">
      {{-- 관계를 점으로 타고 들어간다. JOIN을 우리가 쓰지 않는다 --}}
      <b>{{ $post->author->nickname }}</b>
      · 작품 {{ $post->media->title }}
      · {{ $post->created_at->diffForHumans() }}
      · 조회 {{ $post->views }}
    </p>
    {{-- nl2br 은 줄바꿈을 <br> 로 바꾼다. e() 로 먼저 escape 한 뒤라야 안전하다 --}}
    <div class="post-content">{!! nl2br(e($post->content)) !!}</div>
  </article>

  <hr>

  <h2>댓글 {{ $comments->total() }}개</h2>
  <p class="muted">{{ $comments->currentPage() }}/{{ $comments->lastPage() }} 페이지</p>

  <ul class="comment-list">
    @foreach ($comments as $comment)
      {{-- 답글이면 한 단 들여쓴다 (parent_id 가 있으면 답글) --}}
      <li style="margin-left: {{ $comment->parent_id ? '2rem' : '0' }}">
        @if ($comment->trashed())
          {{-- trashed() = 지워진 행인가. 내용과 작성자는 내보내지 않고 자리만 남긴다 --}}
          <i class="muted">삭제된 댓글입니다</i>
        @else
          <b>{{ $comment->author->nickname }}</b> :
          {{ $comment->content }}
          <small class="muted">({{ $comment->created_at->diffForHumans() }})</small>
        @endif
      </li>
    @endforeach
  </ul>

  {{ $comments->links() }}

  <p><a href="/posts">← 목록으로</a></p>
@endsection
