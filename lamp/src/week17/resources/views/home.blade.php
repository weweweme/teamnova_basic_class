@extends('layouts.app')
@section('title', '리뷰 커뮤니티')

@section('content')
  @if ($hero)
    <div class="hero-area">
      {{-- 왼쪽: 큰 히어로 배너 --}}
      <a class="hero" href="/works/{{ $hero->slug }}"
         @if ($heroBackdrop) style="background-image: url('{{ $heroBackdrop }}')" @endif>
        <div class="hero-inner">
          <h1 class="hero-title">{{ $hero->title }}</h1>
          <p class="hero-desc">{{ mb_substr($hero->overview ?? '', 0, 110) }}…</p>
          <span class="hero-cta">리뷰 보러 가기 →</span>
        </div>
      </a>

      {{-- 오른쪽: 사이드바 (넓은 화면에서만 옆에, 좁으면 아래로) --}}
      <aside class="hero-side">
        @if ($hot->isNotEmpty())
          <section class="side-box">
            <h3>🔥 지금 뜨는 글</h3>
            <ol class="side-hot">
              @foreach ($hot as $p)
                <li>
                  <a href="/posts/{{ $p->id }}">{{ $p->title }}</a>
                  <span class="side-meta">
                    {{ $p->media->title }} ·
                    @if ((int) $p->recent_views > 0) 최근 {{ (int) $p->recent_views }}회
                    @else 조회 {{ $p->views }} @endif
                  </span>
                </li>
              @endforeach
            </ol>
          </section>
        @endif

        @if ($recentViewed->isNotEmpty())
          <section class="side-box">
            <h3>👀 최근 본 글</h3>
            <ol class="side-hot">
              @foreach ($recentViewed as $p)
                <li>
                  <a href="/posts/{{ $p->id }}">{{ $p->title }}</a>
                  <span class="side-meta">{{ $p->media->title }} · {{ $p->author->nickname }}</span>
                </li>
              @endforeach
            </ol>
          </section>
        @endif
        {{-- 오늘의 발견 — 비어서 시작하고, main.js 가 '이번 주 인기작' 응답에서
             하나를 골라 채운다(hidden 이 벗겨진다). 새로고침마다 바뀐다. --}}
        <section class="side-box" id="daily-pick-box" hidden>
          <h3>🎲 오늘의 발견</h3>
          <a class="side-pick" id="daily-pick" href="#">
            <img alt="" loading="lazy">
            <span class="side-pick-title"></span>
          </a>
        </section>
      </aside>
    </div>
  @endif

  @include('partials.media-row', [
    'title' => '🔥 우리 커뮤니티에서 이야기 중',
    'size'  => 'lg',
    'items' => $community->map(fn ($m) => [
        'url'        => '/works/' . $m->slug,
        'poster_url' => $m->poster_url,
        'title'      => $m->title,
        'meta'       => '💬 ' . $m->posts_count,
    ])->all(),
  ])

  {{-- TMDB 가로줄 — 서버는 회색 자리표시만 보내고, 화면이 뜬 뒤 main.js 가
       /api/row 로 실제 포스터를 받아 채운다.
       ★ 이게 '일단 화면부터 띄우고 나중에 채우기'의 핵심 — 홈 HTML 은 우리 DB 것만 담아 즉시 뜬다. --}}
  @foreach (['trending' => '이번 주 인기작', 'movie' => '인기 영화', 'tv' => '인기 드라마'] as $kind => $label)
    <section class="media-row media-row-sm lazy-row" data-kind="{{ $kind }}">
      <h2>{{ $label }}</h2>
      <div class="row-scroll">
        @for ($i = 0; $i < 8; $i++)<span class="row-skeleton"></span>@endfor
      </div>
    </section>
  @endforeach

  {{-- ── 게시판: 최근 올라온 글 ──────────────────────────── --}}
  <section class="home-board">
    <div class="home-board-grid">
      <div class="board-col">
        <h2>📋 최근 올라온 글</h2>
        <ul class="post-list">
          @foreach ($recent as $p)
            <li>
              <a href="/posts/{{ $p->id }}">{{ $p->title }}</a>
              <span class="tag">{{ $p->sentiment }}</span>
              <a class="post-stat" href="/works/{{ $p->media->slug }}">{{ $p->media->title }}</a>
              <span class="post-stat">· {{ $p->author->nickname }} · 💬 {{ $p->comments_count }}</span>
            </li>
          @endforeach
        </ul>
      </div>

      <div class="board-col">
        <h2>💬 댓글 많은 글</h2>
        <ul class="post-list">
          @foreach ($discussed as $p)
            <li>
              <a href="/posts/{{ $p->id }}">{{ $p->title }}</a>
              <span class="tag">{{ $p->sentiment }}</span>
              <a class="post-stat" href="/works/{{ $p->media->slug }}">{{ $p->media->title }}</a>
              <span class="post-stat">· 💬 {{ $p->comments_count }}</span>
            </li>
          @endforeach
        </ul>
      </div>
    </div>
  </section>
@endsection
