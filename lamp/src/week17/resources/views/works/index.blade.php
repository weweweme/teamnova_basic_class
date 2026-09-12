@extends('layouts.app')

@section('title', '작품 둘러보기')

@section('content')
  <h1 class="wide-title">작품 둘러보기</h1>

  @if ($recentWorks)
    @include('partials.media-row', ['title' => '👀 최근 본 작품', 'size' => 'sm', 'items' => $recentWorks])
  @endif

  {{-- 메인 축: 장르 탭 --}}
  <div class="genre-tabs">
    <a class="{{ $genre === '' ? 'active' : '' }}" href="?{{ http_build_query(['media' => $media]) }}">전체</a>
    @foreach ($genres as $name)
      <a class="{{ $genre === $name ? 'active' : '' }}"
         href="?{{ http_build_query(['genre' => $name, 'media' => $media]) }}">{{ $name }}</a>
    @endforeach
  </div>

  {{-- 보조 필터: 전체 / 영화 / 드라마 / 애니 --}}
  <div class="media-filter">
    @foreach (['all' => '전체', 'movie' => '영화', 'tv' => '드라마', 'anime' => '애니'] as $key => $label)
      <a class="{{ $media === $key ? 'active' : '' }}"
         href="?{{ http_build_query(array_filter(['genre' => $genre, 'media' => $key])) }}">{{ $label }}</a>
    @endforeach
  </div>

  {{-- 포스터 그리드 — 비어서 시작하고 JS 가 첫 페이지부터 채운다.
       data-start-page="0" → main.js 가 (0+1)=1페이지부터 /api/browse 에 요청한다.
       ★ 서버가 TMDB 를 기다렸다 그리면 첫 화면이 그만큼 늦게 뜬다.
         우선 뼈대만 보내고, 포스터는 화면이 뜬 뒤에 채운다. --}}
  <div class="poster-grid" id="browse-grid"
       data-genre="{{ $genre }}" data-media="{{ $media }}" data-start-page="0">
    @for ($i = 0; $i < 12; $i++)<span class="row-skeleton"></span>@endfor
  </div>

  {{-- 무한 스크롤 감지용 — 이게 화면에 보이면 JS 가 다음 페이지를 부른다 --}}
  <div id="browse-sentinel" class="browse-sentinel">불러오는 중…</div>
@endsection
