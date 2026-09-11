@extends('layouts.app')

@section('title', '작품')

@section('content')
  <h1>작품</h1>

  <ul class="work-list">
    @foreach ($works as $work)
      <li>
        <a href="/works/{{ $work->slug }}">
          @if ($work->poster_url)
            <img src="{{ $work->poster_url }}" alt="" width="60" loading="lazy">
          @endif
          <b>{{ $work->title }}</b>
        </a>
        <small class="muted">
          {{ $work->year }}{{ $work->genre ? ' · ' . $work->genre : '' }} · 글 {{ $work->posts_count }}개
        </small>
      </li>
    @endforeach
  </ul>
@endsection
