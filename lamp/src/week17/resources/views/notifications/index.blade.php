@extends('layouts.app')

@section('title', '알림')

@section('container', 'narrow')

@section('content')
  <h1>🔔 알림</h1>

  @if ($notifications->isEmpty())
    <p class="muted">아직 알림이 없습니다. 내 글에 댓글이 달리거나 내 댓글에 답글이 달리면 여기에 표시돼요.</p>
  @else
    <ul class="notif-list">
      @foreach ($notifications as $n)
        @php
          // ★ 그 댓글이 몇 페이지에 있는지 계산해서 주소에 싣는다.
          //   안 그러면 알림을 눌러도 1페이지만 열려 정작 그 댓글이 화면에 없다.
          //   계산은 Comment::pageNumber() 한 곳에만 있다.
          $page = $n->comment?->pageNumber() ?? 1;
          $url  = "/posts/{$n->post_id}" . ($page > 1 ? "?cpage={$page}" : '') . "#c{$n->comment_id}";
        @endphp
        <li @class(['notif-item', 'notif-unread' => ! $n->is_read])>
          <a href="{{ $url }}">
            <span class="notif-text">
              @if ($n->type === 'reply')
                ↳ <strong>{{ $n->actor->nickname }}</strong>님이
                <strong>{{ $n->post->title }}</strong> 글에서 내 댓글에 답글을 남겼어요
              @else
                💬 <strong>{{ $n->actor->nickname }}</strong>님이
                <strong>{{ $n->post->title }}</strong> 글에 댓글을 남겼어요
              @endif
            </span>
            <span class="notif-time">{{ $n->created_at->diffForHumans() }}</span>
          </a>
        </li>
      @endforeach
    </ul>

    {{ $notifications->links() }}
  @endif
@endsection
