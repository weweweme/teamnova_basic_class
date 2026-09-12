{{--
  쿠키 동의 배너 — JS 0줄. 그냥 폼이고, 누르면 POST 가 간다.
  ★ 필수 쿠키(로그인·CSRF)는 동의 대상이 아니다. 여기 있는 것은 전부 '선택'이다.
--}}
<aside class="consent-banner">
  <p><b>쿠키 사용 안내</b> — 아래 항목은 켜지 않아도 사이트 이용에 지장이 없습니다.</p>

  <form method="post" action="/consent">
    @csrf
    <input type="hidden" name="source" value="banner">

    @foreach ($consentItems as $key => $label)
      <label><input type="checkbox" name="items[]" value="{{ $key }}" checked> {{ $label }}</label>
    @endforeach

    <button type="submit">선택한 항목 허용</button>
  </form>

  {{-- 거절은 '아무 항목도 안 보내는' 같은 폼이다 --}}
  <form method="post" action="/consent">
    @csrf
    <input type="hidden" name="source" value="banner">
    <button type="submit">모두 거절</button>
  </form>
</aside>
