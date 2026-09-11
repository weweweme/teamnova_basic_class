@extends('layouts.app')

@section('title', '글쓰기')

@section('content')
  <h1>글쓰기</h1>

  <form method="post" action="/posts">
    @csrf

    <p>
      <label>작품
        <select name="media_id" required>
          @foreach ($mediaList as $media)
            {{-- old() 로 되돌아왔을 때 고른 값을 유지한다 --}}
            <option value="{{ $media->id }}" @selected(old('media_id') == $media->id)>{{ $media->title }}</option>
          @endforeach
        </select>
      </label>
      @error('media_id')<span class="muted">{{ $message }}</span>@enderror
    </p>

    <p>
      <label>감상
        <select name="sentiment" required>
          @foreach (['호평', '보통', '혹평'] as $s)
            <option value="{{ $s }}" @selected(old('sentiment') === $s)>{{ $s }}</option>
          @endforeach
        </select>
      </label>
      @error('sentiment')<span class="muted">{{ $message }}</span>@enderror
    </p>

    <p>
      <label>제목
        <input type="text" name="title" value="{{ old('title') }}" maxlength="100" required>
      </label>
      @error('title')<span class="muted">{{ $message }}</span>@enderror
    </p>

    <p>
      <label>내용
        <textarea name="content" rows="10" maxlength="5000" required>{{ old('content') }}</textarea>
      </label>
      @error('content')<span class="muted">{{ $message }}</span>@enderror
    </p>

    <button type="submit">등록</button>
    <a href="/posts">취소</a>
  </form>
@endsection
