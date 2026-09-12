{{--
  쿠키 동의 배너 — JS 0줄. 폼 하나이고, 누르면 POST 가 간다.
  ★ .cookie-notice 는 style.css 에서 position: fixed 로 화면 아래 가운데 뜬다.
    폼을 둘로 나누면 그 배치가 깨진다 (버튼 3개가 같은 폼 안에 있어야 한다).
  ★ name="choice" 가 같고 value 가 다르다 → 누른 버튼의 값만 서버로 간다.
--}}
<form class="cookie-notice" method="post" action="/consent">
  @csrf

  <div class="cookie-notice-text">
    <p><strong>쿠키 사용에 동의해 주세요.</strong></p>

    {{-- 필수는 고를 수 없다 — 없으면 로그인·글쓰기가 아예 안 된다. 대신 무엇인지 밝힌다. --}}
    <p class="muted cookie-required">
      <strong>필수</strong> — 로그인 유지, 위조 요청 방어, 기기 번호(<strong>로그인 기기 목록</strong>).
      서비스가 돌아가려면 반드시 필요해서 <strong>끌 수 없습니다.</strong>
    </p>

    {{-- ★ 기본값은 꺼짐. 동의가 미리 켜져 있으면 그건 동의가 아니다. --}}
    @foreach ($consentItems as $key => $label)
      <label class="cookie-item">
        <input type="checkbox" name="items[]" value="{{ $key }}" @checked(in_array($key, $consentChecked ?? [], true))>
        <span>선택 — {{ $label }}</span>
      </label>
    @endforeach

    {{-- ★ 지금 누르는 것이 '되돌릴 수 있는 선택'임을 그 자리에서 알린다.
         거둘 방법을 나중에 찾아야 한다면 그건 있으나 마나다. --}}
    <p class="muted consent-hint">
      <a href="/cookies">쿠키 설정</a>에서 언제든 바꾸거나 <strong>철회</strong>할 수 있습니다.
    </p>
  </div>

  {{-- ★ '모두 동의'만 눈에 띄게 하고 '거절'을 흐리게 하면 그게 곧 유도(dark pattern)다.
       셋을 같은 무게로 둔다. --}}
  <div class="cookie-notice-actions">
    <button type="submit" name="choice" value="all" class="btn-consent-all">모두 동의</button>
    <button type="submit" name="choice" value="selected">선택한 것만</button>
    <button type="submit" name="choice" value="none" class="btn-consent-none">거절</button>
  </div>
</form>
