@extends('layouts.app')

@section('title', '글 수정')

@section('content')
  <h1>글 수정</h1>

  <form method="post" action="/posts/{{ $post->id }}">
    @csrf
    {{-- ★ HTML 폼은 GET·POST 만 보낼 수 있다.
         @method('PUT') 은 숨김칸(_method=PUT)을 넣어 "사실은 PUT 이다"라고 알려준다.
         Laravel 이 그 값을 보고 PUT 라우트로 연결한다. --}}
    @method('PUT')

    <p>
      <label>작품
        <select name="media_id" required>
          @foreach ($mediaList as $media)
            {{-- old() 가 있으면 그 값을, 없으면 지금 글의 값을 고른 상태로 --}}
            <option value="{{ $media->id }}" @selected(old('media_id', $post->media_id) == $media->id)>{{ $media->title }}</option>
          @endforeach
        </select>
      </label>
    </p>

    <p>
      <label>감상
        <select name="sentiment" required>
          @foreach (['호평', '보통', '혹평'] as $s)
            <option value="{{ $s }}" @selected(old('sentiment', $post->sentiment) === $s)>{{ $s }}</option>
          @endforeach
        </select>
      </label>
    </p>

    <p>
      <label>제목
        <input type="text" name="title" value="{{ old('title', $post->title) }}" maxlength="100" required>
      </label>
      @error('title')<span class="muted">{{ $message }}</span>@enderror
    </p>

    <p>
      <label>내용
        <textarea name="content" rows="10" maxlength="5000" required>{{ old('content', $post->content) }}</textarea>
      </label>
      @error('content')<span class="muted">{{ $message }}</span>@enderror
    </p>

    <button type="submit">수정</button>
    <a href="/posts/{{ $post->id }}">취소</a>
  </form>
@endsection
