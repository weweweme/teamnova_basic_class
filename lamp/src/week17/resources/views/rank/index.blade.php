@extends('layouts.app')

@section('title', '랭킹')

@section('container', 'narrow')

@section('content')
  <h1>🏆 랭킹</h1>

  {{-- 탭: ?tab= 만 바꾼다 --}}
  <div class="rank-tabs">
    @foreach ($tabs as $key => $label)
      <a class="{{ $tab === $key ? 'active' : '' }}" href="?tab={{ $key }}">{{ $label }}</a>
    @endforeach
  </div>

  @php $medals = [1 => '🥇', 2 => '🥈', 3 => '🥉']; @endphp

  @if ($rows->isEmpty())
    <p class="muted">
      @if ($tab === 'users') 아직 글을 쓴 유저가 없습니다.
      @elseif ($tab === 'posts') 아직 글이 없습니다.
      @else 아직 글이 달린 작품이 없습니다. @endif
    </p>
  @else
    <ol class="rank-list">
      @foreach ($rows as $i => $row)
        @php $rank = $i + 1; @endphp
        <li class="rank-item">
          <span class="rank-num rank-{{ $rank }}">{{ $medals[$rank] ?? $rank }}</span>

          @if ($tab === 'works')
            @php $total = $row->up_votes + $row->down_votes; @endphp
            <a class="rank-body" href="/works/{{ $row->slug }}">
              <img class="rank-poster" src="{{ $row->poster_url }}" alt="" loading="lazy">
              <span class="rank-info">
                <strong>{{ $row->title }}</strong>
                <span class="rank-meta">💬 글 {{ $row->posts_count }}개@if ($total > 0) · 👍 추천 {{ round($row->up_votes / $total * 100) }}%@endif</span>
              </span>
            </a>

          @elseif ($tab === 'users')
            <a class="rank-body" href="/users/{{ $row->username }}">
              @if ($row->avatar)
                <img class="rank-avatar" src="{{ $row->avatar }}" alt="">
              @else
                {{-- 사진이 없으면 닉네임 첫 글자로 대신한다 --}}
                <span class="rank-avatar rank-avatar-empty">{{ mb_substr($row->nickname, 0, 1) }}</span>
              @endif
              <span class="rank-info">
                <strong>{!! $row->levelBadge !!} {{ $row->nickname }}</strong>
                <span class="rank-meta">👍 받은 추천 {{ $row->likes_received_count }} · ✍️ 글 {{ $row->posts_count }}</span>
              </span>
            </a>

          @else
            <a class="rank-body rank-body-post" href="/posts/{{ $row->id }}">
              <span class="rank-info">
                <strong>{{ $row->title }} <span class="tag">{{ $row->sentiment }}</span></strong>
                <span class="rank-meta">{{ $row->media->title }} · {!! $row->author->levelBadge !!} {{ $row->author->nickname }}
                  · 👁 {{ $row->views }} · 💬 {{ $row->comments_count }} · 👍 {{ $row->likers_count }}</span>
              </span>
            </a>
          @endif
        </li>
      @endforeach
    </ol>
  @endif
@endsection
