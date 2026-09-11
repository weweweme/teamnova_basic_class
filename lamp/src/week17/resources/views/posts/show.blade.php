@extends('layouts.app')

@section('title', $post->title)

@section('content')
  <article>
    <h1>{{ $post->title }}</h1>
    <p class="muted">
      {{-- 관계를 점으로 타고 들어간다. JOIN을 우리가 쓰지 않는다 --}}
      <b>{{ $post->author->nickname }}</b>
      · 작품 {{ $post->media->title }}
      · {{ $post->created_at->diffForHumans() }}@if ($post->edited_at) <span class="muted">(수정됨)</span>@endif
      · 조회 {{ $post->views }}
    </p>
    {{-- nl2br 은 줄바꿈을 <br> 로 바꾼다. e() 로 먼저 escape 한 뒤라야 안전하다 --}}
    <div class="post-content">{!! nl2br(e($post->content)) !!}</div>

    {{-- ★ @can('update', $post) = PostPolicy::update() 가 true 일 때만 그린다.
         컨트롤러의 authorize() 와 같은 판단을 쓰므로, 규칙이 한 곳에만 있다.
         (버튼을 숨기는 건 보기 좋게 하는 것이고, 실제 차단은 컨트롤러가 한다) --}}
    @can('update', $post)
      <p>
        <a class="btn-edit" href="/posts/{{ $post->id }}/edit">수정</a>

        <form class="delete-form" method="post" action="/posts/{{ $post->id }}"
              onsubmit="return confirm('이 글을 삭제할까요?')">
          @csrf
          @method('DELETE')
          <button class="btn-delete" type="submit">삭제</button>
        </form>
      </p>
    @endcan
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
