@extends('layouts.app')

@section('title', $post->title)

@section('content')
  <p class="back-link"><a href="/works/{{ $post->media->slug }}">← {{ $post->media->title }} 게시판</a></p>

  {{-- article = '독립적인 하나의 글'을 뜻하는 의미(시맨틱) 태그. class="post" 는 CSS 이름표. --}}
  <article class="post">
    <h1>{{ $post->title }}</h1>
    <p class="post-meta">
      <span class="tag">{{ $post->sentiment }}</span>
      {{-- 어느 작품 글인지 → 그 작품 게시판으로 이동 --}}
      <a href="/works/{{ $post->media->slug }}">{{ $post->media->title }}</a> ·
      {{-- 작성자 이름을 누르면 그 사람의 프로필로 --}}
      {!! $post->author->levelBadge !!}
      <a href="/users/{{ $post->author->username }}">{{ $post->author->nickname }}</a>
      @php $shownAt = $post->edited_at ?? $post->created_at; @endphp
      · <time datetime="{{ $shownAt->toIso8601String() }}"
              title="{{ $post->created_at->format('Y-m-d H:i') }} 작성">{{ $shownAt->format('Y-m-d H:i') }}</time>
      · <span class="muted">👁 {{ $post->views }}</span>
      @if ($post->edited_at)
        <span class="muted comment-edited">(수정됨)</span>
      @endif
    </p>
    {{-- nl2br(e(...)) : e() 로 먼저 안전 처리 → nl2br 로 줄바꿈(\n)을 <br> 로. (순서 중요) --}}
    <div class="post-content">{!! nl2br(e($post->content)) !!}</div>
  </article>

  {{-- 글에 대한 '행동'들 — 상태를 바꾸는 것은 링크가 아니라 POST 폼 --}}
  <div class="post-actions">
    @auth
      {{-- 추천: 1인 1회 — 이미 눌렀으면 버튼이 채워지고, 다시 누르면 취소된다 --}}
      <form class="like-form" method="post" action="/posts/{{ $post->id }}/like">
        @csrf
        <button type="submit" @class(['liked' => $liked])>
          {{ $liked ? '👍 추천함' : '👍 추천' }} {{ $post->likers_count }}
        </button>
      </form>

      {{-- 신고 버튼: 누르면 아래 팝업만 연다. type="button" = 폼 제출용이 아님 --}}
      @cannot('update', $post)
        <button type="button" class="btn-report" id="report-open">🚩 신고</button>
      @endcannot

      {{-- @can('update', $post) = PostPolicy::update() 가 true 일 때만 그린다.
           컨트롤러의 authorize() 와 같은 판단을 쓰므로 규칙이 한 곳에만 있다. --}}
      @can('update', $post)
        <a class="btn-edit" href="/posts/{{ $post->id }}/edit">✏️ 수정</a>
        {{-- 삭제는 되돌릴 수 없는 동작이라 반드시 POST 폼.
             class="delete-form" 을 보고 JS 가 '정말 삭제할까요?' 확인창을 띄운다. --}}
        <form class="delete-form" method="post" action="/posts/{{ $post->id }}">
          @csrf
          @method('DELETE')
          <button type="submit" class="btn-delete">🗑 삭제</button>
        </form>
      @endcan
    @else
      <p class="muted"><a href="/login">로그인</a>하면 추천·신고·댓글을 남길 수 있어요.</p>
    @endauth
  </div>

  {{-- 신고 팝업 — <dialog> = HTML 이 기본으로 제공하는 '모달 창' 태그 --}}
  @auth
    @cannot('update', $post)
      <dialog id="report-dialog" class="modal">
        <form method="post" action="/posts/{{ $post->id }}/report">
          @csrf
          <h3>신고하기</h3>
          <p class="muted">신고 사유를 선택해 주세요.</p>
          <select name="reason">
            @foreach ($reportReasons as $reason)
              <option value="{{ $reason }}">{{ $reason }}</option>
            @endforeach
          </select>
          <div class="modal-actions">
            {{-- ★ 취소 버튼은 반드시 type="button" — 기본값이 submit 이라 그냥 두면 눌리는 순간 전송된다 --}}
            <button type="button" id="report-cancel" class="btn-cancel">취소</button>
            <button type="submit" class="btn-danger">신고</button>
          </div>
        </form>
      </dialog>
    @endcannot
  @endauth

  {{-- section = '주제로 묶인 한 구획'(여기선 댓글 구역) --}}
  <section class="comments" id="comments">
    <h2>댓글</h2>

    @if ($comments->isEmpty())
      <p class="muted">아직 댓글이 없습니다. 첫 댓글을 남겨보세요!</p>
    @endif

    <ul class="comment-list">
      @foreach ($comments as $comment)
        @php
          $isReply = $comment->parent_id !== null;
          $anchor  = '#c' . $comment->id;
          // 지금 주소의 조건(cpage 등)은 그대로 두고 reply/edit 만 바꾼다
          $urlWith = fn (array $changes) => '?' . http_build_query(
              array_filter(array_merge(request()->except(['reply', 'edit']), $changes))
          ) . $anchor;
          // 이 댓글을 지금 수정 중인가 / 이 댓글에 답글을 다는 중인가
          //   ★ 답글에는 또 답글을 달 수 없다.
          $isEditing  = (int) request('edit')  === $comment->id && ! $comment->trashed() && auth()->user()?->can('update', $comment);
          $isReplying = (int) request('reply') === $comment->id && ! $isReply && ! $comment->trashed() && auth()->check();
        @endphp
        {{-- id="c12" — 작성·수정 후 이 자리로 바로 스크롤되게 하는 앵커.
             지금 작업 중인 댓글은 배경으로 강조 → 어느 댓글을 건드리는 중인지 한눈에 보인다. --}}
        <li id="c{{ $comment->id }}" @class(['comment-reply' => $isReply, 'comment-active' => $isEditing || $isReplying])>
          @if ($comment->trashed())
            {{-- 답글이 남아 있어서 자리만 지키는 원댓글 (내용은 내보내지 않는다) --}}
            <span class="muted">삭제된 댓글입니다</span>
          @else
            <span class="comment-author">{!! $comment->author->levelBadge !!} {{ $comment->author->nickname }}</span>

            @if ($isEditing)
              <form class="comment-form comment-edit-form" method="post" action="/comments/{{ $comment->id }}">
                @csrf
                @method('PUT')
                <textarea name="content" rows="2" maxlength="500" required>{{ old('content', $comment->content) }}</textarea>
                <button type="submit">수정 완료</button>
                <a class="comment-action" href="{{ $urlWith([]) }}">취소</a>
              </form>
            @else
              {{ $comment->content }}
              @if ($comment->edited_at)
                <span class="muted comment-edited">(수정됨)</span>
              @endif

              @auth
                @if (! $isReply)
                  <a class="comment-action" href="{{ $urlWith(['reply' => $comment->id]) }}">답글</a>
                @endif

                @can('update', $comment)
                  <a class="comment-action" href="{{ $urlWith(['edit' => $comment->id]) }}">수정</a>
                  <form class="delete-form comment-delete" method="post" action="/comments/{{ $comment->id }}">
                    @csrf
                    @method('DELETE')
                    <button type="submit">삭제</button>
                  </form>
                @endcan
              @endauth
            @endif
          @endif

          @if ($isReplying)
            <form class="comment-form comment-reply-form" method="post" action="/posts/{{ $post->id }}/comments">
              @csrf
              <input type="hidden" name="parent_id" value="{{ $comment->id }}">
              <textarea name="content" rows="2" maxlength="500"
                        placeholder="{{ $comment->author->nickname }}님에게 답글" required></textarea>
              <button type="submit">답글 등록</button>
              <a class="comment-action" href="{{ $urlWith([]) }}">취소</a>
            </form>
          @endif
        </li>
      @endforeach
    </ul>

    {{ $comments->links() }}

    @auth
      <form class="comment-form" method="post" action="/posts/{{ $post->id }}/comments">
        @csrf
        <textarea name="content" rows="3" maxlength="500" placeholder="댓글을 입력하세요" required>{{ old('content') }}</textarea>
        @error('content')<span class="muted">{{ $message }}</span>@enderror
        <button type="submit">댓글 등록</button>
      </form>
    @else
      <p class="muted"><a href="/login">로그인</a> 후 댓글을 남길 수 있어요.</p>
    @endauth
  </section>
@endsection
