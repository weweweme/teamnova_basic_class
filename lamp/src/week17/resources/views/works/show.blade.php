@extends('layouts.app')

@section('title', $media->title . ' 게시판')

@section('content')
  <h1 class="narrow-title">{{ $media->title }}
    <small>({{ $media->genre }} · {{ $media->year }}@if ($detail && $detail['runtimeText']) · {{ $detail['runtimeText'] }}@endif)</small>
  </h1>

  {{-- 작품 정보 (포스터 + 감독·출연 + 줄거리 + 예고편) --}}
  <div class="work-info">
    @if ($media->poster_url)
      <img class="poster" src="{{ $media->poster_url }}" alt="" loading="lazy">
    @endif
    <div class="work-detail">
      {{-- 감독(영화) / 제작(드라마) — 이름이 있을 때만 --}}
      @if ($detail && $detail['creditName'])
        <p class="work-credit"><span class="k">{{ $detail['creditLabel'] }}</span>{{ $detail['creditName'] }}</p>
      @elseif ($media->director)
        <p class="work-credit"><span class="k">감독</span>{{ $media->director }}</p>
      @endif

      {{-- 출연진 — 있을 때만, 가운뎃점으로 이어 붙여서 --}}
      @if ($detail && $detail['cast'])
        <p class="work-credit"><span class="k">출연</span>{{ implode(' · ', $detail['cast']) }}</p>
      @endif

      <p class="work-summary">{{ $media->overview }}</p>

      {{-- 예고편 버튼 — 유튜브 영상이 있을 때만.
           data-trailer 에 영상 키를 실어 두면 main.js 가 읽어 팝업을 연다. --}}
      @if ($detail && $detail['trailerKey'])
        <button type="button" class="btn-trailer" data-trailer="{{ $detail['trailerKey'] }}">▶ 예고편 보기</button>
      @endif
    </div>
  </div>

  {{-- 작품 추천/비추천 투표 — '글'이 아니라 '작품'에 대한 POST --}}
  <section class="vote-box">
    <h2>이 작품, 추천하시나요?</h2>

    @php
      $totalVotes = $up + $down;
      $upPct   = $totalVotes ? round($up   / $totalVotes * 100) : 0;
      $downPct = $totalVotes ? 100 - $upPct : 0;
    @endphp

    @if ($totalVotes > 0)
      {{-- 막대그래프: 두 칸의 너비(%)를 style로 직접 지정해 비율을 표현 --}}
      <div class="vote-bar">
        <div class="vote-buy"  style="width: {{ $upPct }}%">추천 {{ $upPct }}%</div>
        <div class="vote-sell" style="width: {{ $downPct }}%">비추천 {{ $downPct }}%</div>
      </div>
      <p class="muted">총 {{ $totalVotes }}표</p>
    @else
      <p class="muted">아직 투표가 없습니다.</p>
    @endif

    {{-- 투표 = 서버 상태를 바꾸는 동작 → POST.
         ★ 제출 버튼에 name과 value를 달면 '어느 버튼을 눌렀는지'가 전송된다.
           덕분에 폼 하나로 버튼 두 개를 구분해서 처리할 수 있다.
         ★ 내가 고른 쪽 버튼을 채워서 표시한다 (버튼 색이 곧 '내 선택' 표시다). --}}
    @auth
      <form class="vote-form" method="post" action="/works/{{ $media->slug }}/vote">
        @csrf
        <button type="submit" name="choice" value="추천"
                @class(['btn-buy', 'voted-up' => $myVote === '추천'])>👍 추천</button>
        <button type="submit" name="choice" value="비추천"
                @class(['btn-sell', 'voted-down' => $myVote === '비추천'])>👎 비추천</button>
      </form>
    @endauth
  </section>

  {{-- 이 게시판 '안에서만' 글 검색 (작품 검색은 상단 메뉴의 '검색') --}}
  <form class="search-form" method="get" action="/works/{{ $media->slug }}">
    <input type="text" name="q" maxlength="50" value="{{ $q }}" placeholder="이 작품 글 검색">
    <button type="submit">검색</button>
  </form>

  @if ($q !== '')
    <p class="muted">
      '{{ $q }}' 검색 중 — <a href="/works/{{ $media->slug }}">검색 해제</a>
    </p>
  @endif

  @include('partials.board', ['writeUrl' => '/posts/create?work=' . $media->slug])

  {{-- 예고편 팝업 — 버튼을 누르면 이 <dialog> 가 화면 위에 뜬다 (신고 팝업과 같은 방식).
       ★ iframe 의 src 는 비워 둔다 → 화면을 열 때 유튜브를 미리 받지 않아 빠르다.
         버튼을 누르는 그 순간 main.js 가 src 를 채워 재생하고, 닫으면 다시 비워 멈춘다. --}}
  @if ($detail && $detail['trailerKey'])
    <dialog id="trailer-modal" class="trailer-modal">
      <div class="trailer-frame">
        <iframe id="trailer-iframe" src="" title="예고편"
                allow="autoplay; encrypted-media; picture-in-picture" allowfullscreen></iframe>
      </div>
      <form method="dialog"><button class="trailer-close">닫기 ✕</button></form>
    </dialog>
  @endif
@endsection
