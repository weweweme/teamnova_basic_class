@extends('layouts.app')

@section('title', '글쓰기')

@section('container', 'narrow')

@section('content')
  <h1>글쓰기</h1>

  @if ($work)
    {{-- 어느 작품에 쓰는지 포스터로 한눈에 (게시판에서 넘어온 그 작품) --}}
    <div class="write-context">
      @if ($work->poster_url)
        <img class="write-context-poster" src="{{ $work->poster_url }}" alt="" loading="lazy">
      @endif
      <div>
        <span class="muted">리뷰 작성</span>
        <strong>{{ $work->title }}</strong>
      </div>
    </div>
  @endif

  <form class="write-form" method="post" action="/posts">
    {{-- ★ @csrf 한 줄이면 숨김 토큰이 들어간다.
         검사는 미들웨어가 한다 — 우리가 require_csrf() 를 부르던 25곳이 사라지는 자리다. --}}
    @csrf

    @if ($work)
      {{-- 작품은 고정 — 게시판에서 넘어온 그 작품. 화면엔 안 보이지만 함께 전송된다. --}}
      <input type="hidden" name="work" value="{{ $work->slug }}">
    @else
      <label>작품
        <select name="media_id" required>
          @foreach ($mediaList as $media)
            <option value="{{ $media->id }}" @selected(old('media_id') == $media->id)>{{ $media->title }}</option>
          @endforeach
        </select>
      </label>
      @error('media_id')<span class="muted">{{ $message }}</span>@enderror
    @endif

    {{-- label = 입력칸 설명표. input 의 name 이 서버에서 값을 꺼낼 열쇠가 된다. --}}
    <label>제목
      <input type="text" name="title" maxlength="100" required value="{{ old('title', $draft->title ?? '') }}">
    </label>
    @error('title')<span class="muted">{{ $message }}</span>@enderror

    {{-- ★ textarea 는 value 속성이 없다. 여는 태그와 닫는 태그 '사이'가 곧 값이다.
         줄바꿈이 값에 그대로 들어가므로 여는 태그 바로 뒤에 붙여 쓴다. --}}
    <label>내용
      <textarea name="content" rows="6" maxlength="5000" required>{{ old('content', $draft->content ?? '') }}</textarea>
    </label>
    @error('content')<span class="muted">{{ $message }}</span>@enderror

    {{-- 본문 사이에 사진 넣기 — 파일을 고르면 올라가고, 커서 자리에 표기가 끼워진다 --}}
    <div class="write-image">
      <label class="btn-photo">
        🖼 사진 넣기
        <input type="file" id="body-image" accept="image/*" hidden>
      </label>
      <span id="body-image-status" class="muted">본문에서 사진을 넣을 자리를 클릭한 뒤 눌러 주세요 (한 글에 {{ \App\Models\Post::MAX_IMAGES }}장까지)</span>
    </div>

    {{-- radio = 여러 개 중 하나만 선택. 같은 name 이면 한 묶음.
         ★ 동그라미는 숨기고 라벨을 버튼처럼 보이게 한다 (CSS 의 label:has(input:checked)). --}}
    <fieldset class="sentiment-field">
      <legend>감상</legend>
      @php $picked = old('sentiment', $draft->sentiment ?? '') ?: '호평'; @endphp
      <div class="sentiment-opts">
        @foreach (['호평' => ['s-good', '👍'], '보통' => ['s-mid', '😐'], '혹평' => ['s-bad', '👎']] as $s => [$cls, $icon])
          <label class="sentiment-opt {{ $cls }}">
            <input type="radio" name="sentiment" value="{{ $s }}" @checked($picked === $s)><span>{{ $icon }} {{ $s }}</span>
          </label>
        @endforeach
      </div>
    </fieldset>
    @error('sentiment')<span class="muted">{{ $message }}</span>@enderror

    <div class="write-submit">
      <button type="submit">등록</button>
      @if ($work)
        <button type="button" id="draft-save" class="btn-draft" data-work="{{ $work->slug }}">💾 임시저장</button>
        <span id="draft-status" class="muted">
          @if ($draft && ($draft->title !== '' || $draft->content !== ''))
            이어서 쓰는 중 (임시저장해 둔 글)
          @endif
        </span>
      @endif
    </div>
  </form>
@endsection

@push('scripts')
{{-- 임시저장 — 쓰던 글을 서버(drafts 표)에 담아 둔다.
     ★ 브라우저가 아니라 서버에 담는 이유: 다른 기기에서 이어 쓸 수 있고,
       실수로 창을 닫아도 남는다. --}}
<script>
(function () {
  const form   = document.querySelector('.write-form');
  const button = document.getElementById('draft-save');
  const status = document.getElementById('draft-status');
  if (!form || !button) return;

  let dirty = false;
  form.addEventListener('input', function () { dirty = true; });

  button.addEventListener('click', async function () {
    const data = new FormData(form);
    const values = {
      work_slug: button.dataset.work,
      title:     data.get('title')     || '',
      content:   data.get('content')   || '',
      sentiment: data.get('sentiment') || '',
      _token:    data.get('_token')    || ''
    };
    if (values.title === '' && values.content === '') {
      status.textContent = '쓴 내용이 없어요';
      return;
    }

    button.disabled = true;                 // 연타로 같은 요청이 여러 번 가지 않게
    status.textContent = '저장 중…';
    try {
      const res  = await fetch('/drafts', {
        method:  'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body:    new URLSearchParams(values).toString()
      });
      const json = await res.json();
      if (json.ok) {
        dirty = false;                      // 저장했으니 나가도 잃을 게 없다
        status.textContent = '임시저장됨 ' + json.at + ' · 창을 닫아도 남아요';
      } else {
        status.textContent = '임시저장 실패';
      }
    } catch (e) {
      status.textContent = '임시저장 실패 (계속 쓰셔도 됩니다)';
    } finally {
      button.disabled = false;
    }
  });

  form.addEventListener('submit', function () { dirty = false; });
  window.addEventListener('beforeunload', function (event) {
    if (dirty) { event.preventDefault(); }
  });
})();
</script>
@endpush

@push('scripts')
{{-- 본문 사이에 사진 넣기 — 파일을 고르면 올리고, 커서가 있던 자리에 표기를 끼운다.
     ★ 올리는 일과 검사는 서버가 한다. 여기서 하는 것은 '어디에 끼울지'뿐이다. --}}
<script>
(function () {
  const picker = document.getElementById('body-image');
  const status = document.getElementById('body-image-status');
  const body   = document.querySelector('.write-form textarea[name="content"]');
  if (!picker || !body) return;

  const MAX_IMAGES   = {{ \App\Models\Post::MAX_IMAGES }};   // 한 글에 넣을 수 있는 장수
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

    // 한 글에 넣을 수 있는 장수 제한 — 본문에 이미 들어 있는 표기를 센다
    const already = (body.value.match(/!\[[^\]]*\]\([^)]+\)/g) || []).length;
    if (already >= MAX_IMAGES) {
      status.textContent = '한 글에 사진은 ' + MAX_IMAGES + '장까지 넣을 수 있습니다';
      picker.value = '';
      return;
    }

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

      status.textContent = '넣었습니다 (' + (already + 1) + '/' + MAX_IMAGES + '장). 글을 등록하면 본문에 사진이 보입니다';
    } catch (e) {
      status.textContent = '사진을 올리지 못했습니다';
    } finally {
      picker.value = '';
    }
  });
})();
</script>
@endpush
