@extends('layouts.app')

@section('title', $post->title)

@section('content')
  <article>
    <h1>{{ $post->title }}</h1>
    <p class="muted">
      {{-- 관계를 점으로 타고 들어간다. JOIN을 우리가 쓰지 않는다 --}}
      <b>{{ $post->author->nickname }}</b>
      · 작품 {{ $post->media->title }}
      · {{ $post->created_at->diffForHumans() }}@if ($post->edited_at) (수정됨)@endif
      · 조회 {{ $post->views }}
    </p>

    {{-- nl2br 은 줄바꿈을 <br> 로 바꾼다. e() 로 먼저 escape 한 뒤라야 안전하다 --}}
    <div class="post-content">{!! nl2br(e($post->content)) !!}</div>

    {{-- @can('update', $post) = PostPolicy::update() 가 true 일 때만 그린다.
         컨트롤러의 authorize() 와 같은 판단을 쓰므로 규칙이 한 곳에만 있다. --}}
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

  <section class="comments">
    <h2>댓글 {{ $comments->total() }}개</h2>

    <ul class="comment-list">
      @foreach ($comments as $comment)
        {{-- id="c12" — 작성·수정 후 이 자리로 바로 스크롤되게 하는 앵커 --}}
        {{-- 답글이면 한 단 들여쓴다 --}}
        <li id="c{{ $comment->id }}" style="margin-left: {{ $comment->parent_id ? '2rem' : '0' }}">
          @if ($comment->trashed())
            {{-- trashed() = 지워진 행인가. 내용과 작성자는 내보내지 않고 자리만 남긴다 --}}
            <i class="muted">삭제된 댓글입니다</i>

          @elseif (request('edit') == $comment->id)
            {{-- ?edit=12 로 들어오면 그 댓글만 수정 폼으로 바뀐다 (기존 화면과 같은 방식) --}}
            <form class="comment-form comment-edit-form" method="post" action="/comments/{{ $comment->id }}">
              @csrf
              @method('PUT')
              <textarea name="content" rows="2" maxlength="500" required>{{ old('content', $comment->content) }}</textarea>
              <button type="submit">저장</button>
              <a href="{{ url()->current() }}?{{ http_build_query(request()->except('edit')) }}">취소</a>
            </form>

          @else
            <b class="comment-author">{{ $comment->author->nickname }}</b> :
            {{ $comment->content }}
            <small class="muted">({{ $comment->created_at->diffForHumans() }}@if ($comment->edited_at) · 수정됨 @endif)</small>

            @auth
              <span class="comment-action">
                {{-- 지금 주소의 조건(cpage 등)을 유지한 채 reply/edit 만 덧붙인다 --}}
                <a href="?{{ http_build_query(array_merge(request()->except('edit'), ['reply' => $comment->id])) }}#c{{ $comment->id }}">답글</a>

                @can('update', $comment)
                  <a href="?{{ http_build_query(array_merge(request()->except('reply'), ['edit' => $comment->id])) }}#c{{ $comment->id }}">수정</a>

                  <form class="delete-form comment-delete" method="post" action="/comments/{{ $comment->id }}"
                        onsubmit="return confirm('이 댓글을 삭제할까요?')">
                    @csrf
                    @method('DELETE')
                    <button type="submit">삭제</button>
                  </form>
                @endcan
              </span>
            @endauth
          @endif

          {{-- ?reply=12 로 들어오면 그 댓글 아래에 답글 폼이 열린다 --}}
          @auth
            @if (request('reply') == $comment->id && ! $comment->trashed())
              <form class="comment-form comment-reply-form" method="post" action="/posts/{{ $post->id }}/comments">
                @csrf
                {{-- 어느 댓글에 다는 답글인지 함께 보낸다 --}}
                <input type="hidden" name="parent_id" value="{{ $comment->parent_id ?? $comment->id }}">
                <textarea name="content" rows="2" maxlength="500" placeholder="답글을 입력하세요" required></textarea>
                <button type="submit">답글 등록</button>
                <a href="{{ url()->current() }}?{{ http_build_query(request()->except('reply')) }}">취소</a>
              </form>
            @endif
          @endauth
        </li>
      @endforeach
    </ul>

    {{-- 페이지 번호. 주소의 다른 조건(reply·edit)은 빼고 cpage 만 남긴다 --}}
    {{ $comments->links() }}

    @auth
      <form class="comment-form" method="post" action="/posts/{{ $post->id }}/comments">
        @csrf
        <textarea name="content" rows="3" maxlength="500" placeholder="댓글을 입력하세요" required>{{ old('content') }}</textarea>
        @error('content')<span class="muted">{{ $message }}</span>@enderror
        <button type="submit">댓글 등록</button>
      </form>
    @else
      <p class="muted"><a href="/login">로그인</a>하면 댓글을 쓸 수 있습니다.</p>
    @endauth
  </section>

  <p><a class="back-link" href="/posts">← 목록으로</a></p>
@endsection
