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

  const SERVER_LIMIT = 16 * 1024 * 1024;   // 서버가 받는 한도 (줄인 뒤 기준)
  const RESIZE_OVER  = 400 * 1024;         // 이보다 크면 줄여서 보낸다
  const MAX_EDGE     = 1600;               // 긴 변 기준 최대 픽셀

  const mb = (n) => Math.round(n / 1024 / 1024 * 10) / 10;

  // 캔버스에 다시 그려서 크기를 줄인다. WebP 로 내보내면 같은 화질에 용량이 훨씬 작다.
  async function shrink(file) {
    // createImageBitmap — 큰 사진을 열 때 메모리를 덜 쓴다. 없으면 <img> 로 되돌아간다.
    const src = typeof createImageBitmap === 'function'
      ? await createImageBitmap(file)
      : await new Promise(function (resolve, reject) {
          const img = new Image();
          img.onload  = function () { resolve(img); };
          img.onerror = reject;
          img.src = URL.createObjectURL(file);
        });

    const scale  = Math.min(1, MAX_EDGE / Math.max(src.width, src.height));
    const canvas = document.createElement('canvas');
    canvas.width  = Math.round(src.width  * scale);
    canvas.height = Math.round(src.height * scale);
    canvas.getContext('2d').drawImage(src, 0, 0, canvas.width, canvas.height);
    if (src.close) { src.close(); }

    return new Promise(function (resolve, reject) {
      canvas.toBlob(function (blob) {
        blob ? resolve(new File([blob], 'photo.webp', { type: 'image/webp' })) : reject(new Error('변환 실패'));
      }, 'image/webp', 0.85);
    });
  }

  picker.addEventListener('change', async function () {
    const file = picker.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      status.textContent = '이미지 파일만 올릴 수 있습니다';
      picker.value = '';
      return;
    }

    // ★ 크기로 미리 막지 않는다. 어떤 사진을 골라도 일단 받고, 줄여서 보낸다.
    //   요즘 사진은 한 장에 8MB가 넘고 가로가 4000px 를 넘는데 화면에 보이는 폭은 700px 남짓이다.
    //   긴 변 1600px WebP 로 줄이면 대개 0.3MB 안팎이 된다.
    //   (아바타 업로드가 쓰는 방법과 같다 — 서버의 검사는 그대로 살아 있다)
    let upload = file;

    if (file.size > RESIZE_OVER || file.type !== 'image/webp') {
      status.textContent = '사진을 줄이는 중… (' + mb(file.size) + 'MB)';
      try {
        upload = await shrink(file);
      } catch (e) {
        upload = file;                 // 줄이기에 실패하면 원본을 그대로 보낸다
      }
    }

    // 줄인 뒤에도 서버 한도를 넘으면 그때 알린다 (아주 큰 원본에서 줄이기가 실패한 경우)
    if (upload.size > SERVER_LIMIT) {
      status.textContent = '사진이 너무 큽니다 (' + mb(upload.size) + 'MB). 조금 줄여서 올려 주세요';
      picker.value = '';
      return;
    }

    status.textContent = '올리는 중… (' + mb(upload.size) + 'MB)';

    const form = new FormData();
    form.append('image', upload);
    form.append('_token', document.querySelector('meta[name="csrf-token"]').content);

    try {
      // ★ Accept 를 붙여야 검사에 걸렸을 때 서버가 JSON 으로 답한다.
      //   안 붙이면 '폼으로 되돌리기'로 처리되어 HTML 이 오고, 여기서 읽을 수 없다.
      const res  = await fetch('/posts/images', {
        method: 'POST',
        body: form,
        credentials: 'same-origin',
        headers: { 'Accept': 'application/json' },
      });
      const json = await res.json();

      if (!res.ok) {
        // 검사에 걸리면 서버가 이유를 알려준다 (형식·크기)
        const why = json.errors && json.errors.image ? json.errors.image[0] : json.message;
        status.textContent = why || '사진을 올리지 못했습니다';
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
