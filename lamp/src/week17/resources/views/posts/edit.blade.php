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

    {{-- 본문 사이에 사진 넣기 — 파일을 고르면 올라가고, 커서 자리에 표기가 끼워진다 --}}
    <div class="write-image">
      <label class="btn-upload">
        🖼 사진 넣기
        <input type="file" id="body-image" accept="image/*" hidden>
      </label>
      <span id="body-image-status" class="muted">본문에서 사진을 넣을 자리를 클릭한 뒤 눌러 주세요</span>
    </div>

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

@push('scripts')
{{-- 본문 사이에 사진 넣기 — 파일을 고르면 올리고, 커서가 있던 자리에 표기를 끼운다.
     ★ 올리는 일과 검사는 서버가 한다. 여기서 하는 것은 '어디에 끼울지'뿐이다. --}}
<script>
(function () {
  const picker = document.getElementById('body-image');
  const status = document.getElementById('body-image-status');
  const body   = document.querySelector('.write-form textarea[name="content"]');
  if (!picker || !body) return;

  picker.addEventListener('change', async function () {
    const file = picker.files[0];
    if (!file) return;

    status.textContent = '올리는 중…';

    const form = new FormData();
    form.append('image', file);
    form.append('_token', document.querySelector('meta[name="csrf-token"]').content);

    try {
      const res  = await fetch('/posts/images', { method: 'POST', body: form, credentials: 'same-origin' });
      const json = await res.json();

      if (!res.ok) {
        // 검사에 걸리면 서버가 이유를 알려준다 (형식·크기)
        status.textContent = json.message || '사진을 올리지 못했습니다';
        return;
      }

      // 커서가 있던 자리에 끼워 넣는다. 앞뒤로 빈 줄을 둬야 문단으로 떨어진다.
      const mark  = '\n\n![](' + json.url + ')\n\n';
      const at    = body.selectionStart;
      body.value  = body.value.slice(0, at) + mark + body.value.slice(body.selectionEnd);
      body.selectionStart = body.selectionEnd = at + mark.length;
      body.focus();

      status.textContent = '넣었습니다. 글을 등록하면 본문에 사진이 보입니다';
    } catch (e) {
      status.textContent = '사진을 올리지 못했습니다';
    } finally {
      picker.value = '';
    }
  });
})();
</script>
@endpush
