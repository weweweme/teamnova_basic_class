@extends('layouts.app')

@section('title', $isMe ? '내 프로필' : $user->nickname . ' 님의 프로필')

@section('container', 'narrow')

@section('content')
  <div class="profile-head">
    {{-- 아바타: 있으면 이미지, 없으면 닉네임 첫 글자로 만든 자리표시 --}}
    @if ($user->avatar)
      <img class="avatar" src="{{ $user->avatar }}" alt="">
    @else
      <span class="avatar avatar-empty">{{ mb_substr($user->nickname, 0, 1) }}</span>
    @endif

    <div class="profile-head-text">
      <h1>
        {{ $user->nickname }}
        <small>{{ $isMe ? '— 내 프로필' : '님의 프로필' }}</small>
      </h1>
      <span class="lvl-chip" title="작성 글 {{ $postCount }}개">{{ $user->level['badge'] }} {{ $user->level['name'] }}</span>
    </div>
  </div>

  @if ($isMe)
    <div class="profile-actions">
      <a class="btn-settings" href="/settings">⚙️ 설정</a>
      {{-- 새 글은 '어느 작품'인지 먼저 골라야 하므로 검색으로 안내 --}}
      <a class="btn-write" href="/search">✏️ 새 글 쓰기</a>
    </div>
  @endif

  {{-- 활동 통계 카드 3개 --}}
  <div class="profile-stats">
    <div><strong>{{ $postCount }}</strong><span>작성 글</span></div>
    <div><strong>{{ $totalViews }}</strong><span>총 조회</span></div>
    <div><strong>{{ $likesReceived }}</strong><span>받은 추천</span></div>
  </div>

  {{-- 탭: 작성한 글 | 좋아요한 글 (?tab= 만 바꾼다) --}}
  <div class="profile-tabs">
    <a class="{{ $tab === 'posts' ? 'active' : '' }}" href="?tab=posts">✍️ 작성한 글 {{ $postCount }}</a>
    <a class="{{ $tab === 'liked' ? 'active' : '' }}" href="?tab=liked">👍 좋아요한 글</a>
  </div>

  @if ($posts->isEmpty())
    <p class="muted">{{ $tab === 'liked' ? '아직 좋아요한 글이 없습니다.' : '작성한 글이 없습니다.' }}</p>
  @else
    <ul class="post-list">
      @foreach ($posts as $post)
        <li>
          <a href="/posts/{{ $post->id }}">{{ $post->title }}</a>
          <span class="tag">{{ $post->sentiment }}</span>
          {{-- 어느 작품 글인지 표시 + 그 게시판으로 바로 갈 수 있게 링크 --}}
          <a class="post-stat" href="/works/{{ $post->media->slug }}">{{ $post->media->title }}</a>
          @if ($tab === 'liked')
            <span class="post-stat">· {!! $post->author->levelBadge !!} {{ $post->author->nickname }}</span>
          @endif
        </li>
      @endforeach
    </ul>

    {{ $posts->links() }}
  @endif
@endsection
