@extends('layouts.app')

@section('title', '쿠키 설정')

@section('container', 'narrow')

@section('content')
  <h1>🍪 쿠키 설정</h1>

  @if (! $decided)
    <p class="muted">아직 아무것도 고르지 않으셨습니다. 아래에서 정해 주세요.</p>
  @else
    <p class="muted">
      선택하신 내용입니다.
      <span class="consent-version">안내 v1</span>
    </p>
  @endif

  <form class="consent-form" method="post" action="/consent">
    @csrf
    {{-- 배너에서 눌렀는지, 이 설정 화면에서 눌렀는지를 기록에 남긴다 --}}
    <input type="hidden" name="source" value="settings">

    <section class="settings-section">
      <h2>필수</h2>
      <p class="muted">
        로그인 유지 · 위조 요청 방어 · 화면 설정(정렬·감상·글 수) ·
        방금 한 일에 대한 알림.
      </p>
      <p class="muted">
        <strong>기기 번호</strong> — 이 브라우저를 알아보는 무작위 번호입니다.
        <strong>로그인한 기기 목록</strong>에만 씁니다.
      </p>
      <p class="muted cookie-required">
        이게 없으면 <strong>로그인도 글쓰기도 안 됩니다.</strong> 그래서 끌 수 없습니다.
      </p>
    </section>

    <section class="settings-section">
      <h2>선택</h2>
      @foreach (\App\Services\Consent::LABELS as $key => $label)
        <label class="cookie-item">
          <input type="checkbox" name="items[]" value="{{ $key }}" @checked(in_array($key, $checked, true))>
          <span>{{ $label }}</span>
        </label>
      @endforeach

      <div class="consent-actions">
        <button type="submit" name="choice" value="selected">이대로 저장</button>
        <button type="submit" name="choice" value="none" class="btn-consent-none">모두 철회</button>
      </div>
    </section>
  </form>

  <section class="settings-section">
    <h2>지금 이 브라우저에 있는 쿠키</h2>

    <p class="cookie-list-label">필수 <span class="muted">({{ count($essential) }}개)</span></p>
    <p class="cookie-list">
      @if ($essential){{ implode(' · ', $essential) }}@else<span class="muted">없음</span>@endif
    </p>

    <p class="cookie-list-label">선택 <span class="muted">({{ count($optional) }}개)</span></p>
    <p class="cookie-list">
      @if ($optional)
        {{ implode(' · ', $optional) }}
      @else
        <span class="muted">없음 — 동의하지 않으셨거나 아직 쌓인 것이 없습니다.</span>
      @endif
    </p>

    <p class="muted consent-hint">
      ※ 세션 번호표와 알림·폼 값은 <strong>창을 닫으면 사라집니다.</strong>
      직접 확인하시려면 <strong>F12 → Application → Cookies</strong>를 열어보세요.
    </p>
  </section>

  <section class="settings-section">
    <h2>동의 기록 <span class="muted consent-version">서버에 보관</span></h2>

    @if ($history->isEmpty())
      <p class="muted">아직 기록이 없습니다.</p>
    @else
      <ul class="consent-history">
        @foreach ($history as $row)
          @php
            $picked = array_filter(explode(',', (string) $row->items));
            $names  = array_map(fn ($k) => \App\Services\Consent::LABELS[$k] ?? $k, $picked);
          @endphp
          <li>
            <span class="consent-when">{{ \Illuminate\Support\Carbon::parse($row->created_at)->format('Y-m-d H:i') }}</span>
            <strong>{{ $row->action === 'accept' ? '동의' : '거절·철회' }}</strong>
            <span class="muted">· {{ $row->source === 'settings' ? '쿠키 설정' : '동의 창' }}에서</span>
            <span class="muted">· 안내 v{{ $row->policy_version }}</span>
            <span class="muted">· {{ $row->user_id === null ? '비회원' : '회원 #' . $row->user_id }}</span>
            @if ($row->ip_prefix)
              <span class="muted">· 접속지 {{ $row->ip_prefix }}</span>
            @endif
            <div class="consent-items">
              @if ($names){{ implode(' + ', $names) }}@else<span class="muted">선택 항목 없음 (필수만)</span>@endif
            </div>
          </li>
        @endforeach
      </ul>

      <p class="muted consent-hint">
        ※ 이 기록은 <strong>고쳐지지 않고 쌓이기만 합니다.</strong>
        철회해도 지난 줄을 지우지 않아요 — <strong>'동의했다가 철회함'과 '동의한 적 없음'은 다른 사실</strong>이니까요.
      </p>
    @endif
  </section>

  <form class="consent-reset" method="post" action="/consent">
    @csrf
    <button type="submit" name="choice" value="reset" class="btn-link">동의 창 다시 보기</button>
  </form>
@endsection
