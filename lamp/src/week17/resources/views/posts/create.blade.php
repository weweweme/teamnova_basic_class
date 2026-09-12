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
