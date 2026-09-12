@extends('layouts.app')

@section('title', '글 수정')

@section('container', 'narrow')

@section('content')
  <h1>글 수정</h1>

  {{-- 어느 작품의 글을 고치는 중인지 한눈에 (글쓰기 화면과 같은 조각) --}}
  <div class="write-context">
    @if ($post->media->poster_url)
      <img class="write-context-poster" src="{{ $post->media->poster_url }}" alt="" loading="lazy">
    @endif
    <div>
      <span class="muted">리뷰 수정</span>
      <strong>{{ $post->media->title }}</strong>
    </div>
  </div>

  <form class="write-form" method="post" action="/posts/{{ $post->id }}">
    @csrf
    {{-- ★ HTML 폼은 GET·POST 만 보낼 수 있다.
         @method('PUT') 은 숨김칸(_method=PUT)을 넣어 "사실은 PUT 이다"라고 알려준다.
         Laravel 이 그 값을 보고 PUT 라우트로 연결한다. --}}
    @method('PUT')

    {{-- 작품은 바꾸지 않는다 — 고치는 것은 '이 작품에 쓴 리뷰'다 --}}
    <input type="hidden" name="media_id" value="{{ $post->media_id }}">

    <label>제목
      <input type="text" name="title" maxlength="100" required value="{{ old('title', $post->title) }}">
    </label>
    @error('title')<span class="muted">{{ $message }}</span>@enderror

    <label>내용
      <textarea name="content" rows="6" maxlength="5000" required>{{ old('content', $post->content) }}</textarea>
    </label>
    @error('content')<span class="muted">{{ $message }}</span>@enderror

    <fieldset class="sentiment-field">
      <legend>감상</legend>
      @php $picked = old('sentiment', $post->sentiment); @endphp
      <div class="sentiment-opts">
        @foreach (['호평' => ['s-good', '👍'], '보통' => ['s-mid', '😐'], '혹평' => ['s-bad', '👎']] as $s => [$cls, $icon])
          <label class="sentiment-opt {{ $cls }}">
            <input type="radio" name="sentiment" value="{{ $s }}" @checked($picked === $s)><span>{{ $icon }} {{ $s }}</span>
          </label>
        @endforeach
      </div>
    </fieldset>

    <div class="write-submit">
      <button type="submit">수정 완료</button>
      <a class="btn-cancel" href="/posts/{{ $post->id }}">취소</a>
    </div>
  </form>
@endsection
