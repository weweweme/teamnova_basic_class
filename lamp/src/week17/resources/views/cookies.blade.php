@extends('layouts.app')
@section('title', '쿠키 설정')

@section('content')
  <h1>🍪 쿠키 설정</h1>

  @if ($decided)
    <p class="muted">현재 허용한 항목: {{ $checked ? implode(', ', array_map(fn ($k) => \App\Services\Consent::LABELS[$k], $checked)) : '없음 (모두 거절)' }}</p>
  @else
    <p class="muted">아직 선택하지 않았습니다.</p>
  @endif

  <section class="consent-form">
    <h2>필수 쿠키 (끌 수 없음)</h2>
    <p class="muted cookie-required">
      로그인 유지, 위조 요청 방어(CSRF), 기기 번호. 이것이 없으면 로그인과 글쓰기가 동작하지 않습니다.
    </p>

    <h2>선택 쿠키</h2>
    <form method="post" action="/consent">
      @csrf
      @foreach (\App\Services\Consent::LABELS as $key => $label)
        <label class="cookie-item">
          <input type="checkbox" name="items[]" value="{{ $key }}" @checked(in_array($key, $checked, true))>
          <span>{{ $label }}</span>
        </label>
      @endforeach

      <div class="consent-actions">
        <button type="submit" name="choice" value="selected">선택한 것만 허용</button>
        <button type="submit" name="choice" value="all" class="btn-consent-all">모두 허용</button>
        <button type="submit" name="choice" value="none" class="btn-consent-none">모두 철회</button>
      </div>
    </form>
  </section>
@endsection
