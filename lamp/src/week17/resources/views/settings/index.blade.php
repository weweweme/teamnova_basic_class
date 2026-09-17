@extends('layouts.app')

@section('title', '설정')

@section('container', 'narrow')

@section('content')
  <h1>⚙️ 설정</h1>
  <p class="muted"><a href="/users/{{ auth()->user()->username }}">← 내 프로필로</a></p>

  {{-- ── 프로필 이미지 ─────────────────────────────────────── --}}
  <section class="settings-section">
    <h2>프로필 이미지</h2>
    <div class="settings-avatar">
      @if (auth()->user()->avatar)
        <img class="avatar" src="{{ auth()->user()->avatar }}" alt="">
      @else
        <span class="avatar avatar-empty">{{ mb_substr(auth()->user()->nickname, 0, 1) }}</span>
      @endif

      {{-- ★ 파일을 보내는 폼은 enctype 을 지정해야 한다. 없으면 파일 이름만 전송된다.
           자동제출(onchange) 대신 main.js 가 파일 선택을 가로채서
           올리기 전에 256px WebP 로 줄이고 제출한다. JS 가 실패하면 원본 그대로 간다. --}}
      <form class="avatar-form" method="post" action="/settings/avatar" enctype="multipart/form-data">
        @csrf
        <label class="btn-upload">
          📷 이미지 변경
          <input type="file" name="avatar" id="avatar-input" accept="image/*" hidden>
        </label>
        <span class="muted">JPG·PNG·GIF·WebP · 업로드 시 자동으로 256px로 최적화</span>
      </form>
    </div>
    @error('avatar')<p class="muted">{{ $message }}</p>@enderror
  </section>

  {{-- ── 닉네임(표시 이름) 변경 ───────────────────────────── --}}
  <section class="settings-section">
    <h2>닉네임</h2>
    <p class="muted">글·댓글·프로필에 보이는 이름이에요. (로그인 아이디는 안 바뀝니다)</p>
    <form class="settings-form" method="post" action="/settings/nickname">
      @csrf
      @method('PATCH')
      <input type="text" name="nickname" value="{{ old('nickname', auth()->user()->nickname) }}" maxlength="20" required>
      <button type="submit">저장</button>
    </form>
    @error('nickname')<p class="muted">{{ $message }}</p>@enderror
  </section>

  {{-- ── 비밀번호 변경 ─────────────────────────────────────── --}}
  <section class="settings-section">
    <h2>비밀번호 변경</h2>
    <p class="muted">비밀번호를 바꾸면 다른 기기의 로그인은 자동으로 해제됩니다.</p>
    <form class="settings-form settings-form-col" method="post" action="/settings/password">
      @csrf
      @method('PATCH')
      <label>현재 비밀번호
        <input type="password" name="current" required>
      </label>
      @error('current')<span class="muted">{{ $message }}</span>@enderror
      <label>새 비밀번호
        <input type="password" name="new" minlength="4" required>
      </label>
      @error('new')<span class="muted">{{ $message }}</span>@enderror
      <label>새 비밀번호 확인
        <input type="password" name="new_confirmation" minlength="4" required>
      </label>
      <button type="submit">변경</button>
    </form>
  </section>

  {{-- ── 메일 알림 ─────────────────────────────────────────── --}}
  {{-- ★ id 를 붙여 두면 저장 뒤 이 자리로 돌아올 수 있다 (/settings#mail) --}}
  <section class="settings-section" id="mail">
    <h2>메일 알림</h2>
    @php
      $me       = auth()->user();
      $verified = $me->hasVerifiedEmail();
    @endphp

    <p class="muted">
      주소를 적으면 그 주소로 <strong>인증 메일</strong>이 갑니다.
      메일 속 링크를 눌러야 주인으로 인정되고, 그 뒤에 알림을 켤 수 있습니다.
    </p>

    {{-- ① 이메일 주소 — 적고, 인증받는다 --}}
    <form class="settings-form settings-form-col" method="post" action="/settings/email">
      @csrf
      @method('PATCH')

      <label>이메일
        <input type="email" name="email" maxlength="100"
               value="{{ old('email', $me->email) }}" placeholder="비워 두면 메일을 받지 않습니다">
      </label>
      @error('email')<span class="muted">{{ $message }}</span>@enderror

      @if ($me->email)
        <p class="verify-state {{ $verified ? 'is-verified' : 'is-pending' }}">
          @if ($verified)
            ✓ 인증된 주소입니다 ({{ $me->email_verified_at->format('Y-m-d') }} 인증)
          @else
            ● 아직 인증하지 않은 주소입니다. 아래 버튼을 누르면 인증 메일이 갑니다.
          @endif
        </p>
      @endif

      {{-- 버튼 이름이 지금 할 일을 말한다.
           ★ '다시 보내기'라고 쓰지 않는다 — 전에 보낸 적이 있는지 우리는 알지 못한다.
             가입할 때 적어 둔 주소는 인증 메일이 나간 적이 없다. --}}
      <button type="submit">{{ $verified ? '주소 바꾸기' : '인증 메일 보내기' }}</button>
    </form>

    {{-- ② 활동 알림 — 인증을 마쳐야 켤 수 있다 --}}
    <form class="settings-form settings-form-col settings-subform" method="post" action="/settings/notifications">
      @csrf
      @method('PATCH')

      <label class="settings-check">
        <input type="checkbox" name="notify_activity" value="1"
               @checked(old('notify_activity', $me->notify_activity)) @disabled(! $verified)>
        <span>내 글에 댓글이 달리면 메일로 알려 주세요</span>
      </label>
      @error('notify_activity')<span class="muted">{{ $message }}</span>@enderror

      @unless ($verified)
        <span class="muted">이메일 인증을 마쳐야 켤 수 있습니다.</span>
      @endunless

      <button type="submit" @disabled(! $verified)>알림 설정 저장</button>
    </form>

    <p class="muted">
      새 기기 로그인과 비밀번호 변경 같은 <strong>보안 알림은 끌 수 없습니다.</strong>
      본인이 하지 않은 일을 알리는 것이라, 끄면 알림의 목적이 사라집니다.
    </p>
  </section>

  {{-- ── 로그인한 기기 ───────────────────────────────────── --}}
  <section class="settings-section">
    <h2>로그인한 기기</h2>
    <p class="muted">
      이 계정으로 로그인한 적 있는 기기들이에요.
      <strong>모르는 기기가 있으면 끊고 비밀번호를 바꿔 주세요.</strong>
    </p>

    <ul class="device-list">
      @foreach ($devices as $device)
        @php $isThis = $device->device_id === $thisDeviceId; @endphp
        <li class="device-item">
          <div>
            <strong>{{ $device->describe() }}</strong>
            @if ($isThis)<span class="device-current">이 기기</span>@endif
            <div class="muted">
              마지막 로그인 {{ $device->last_login_at?->format('Y-m-d H:i') }}
              · 처음 {{ $device->first_seen_at?->format('Y-m-d H:i') }}
            </div>
            <div class="muted">
              @if ($device->hasKey())
                🔏 기기 도장 있음
              @else
                도장 없음 — 다음 로그인 때 만들어집니다
              @endif
            </div>
          </div>

          @if (! $isThis)
            <form method="post" action="/settings/devices">
              @csrf
              @method('DELETE')
              <input type="hidden" name="device_id" value="{{ $device->device_id }}">
              <button type="submit" class="btn-settings">끊기</button>
            </form>
          @endif
        </li>
      @endforeach
    </ul>
    @error('device_id')<p class="muted">{{ $message }}</p>@enderror

    {{-- 다른 기기 로그아웃 — 이 기기의 로그인은 그대로 둔다.
         ★ 여기만 비밀번호를 한 번 더 받는다. Laravel 의 내장 기능이
           '세션에 담아 둔 비밀번호 해시'를 새로 맞추는 데 원문이 필요하기 때문이다. --}}
    <form class="settings-form" method="post" action="/settings/logout-others">
      @csrf
      <input type="password" name="password" placeholder="비밀번호 확인" required>
      <button type="submit" class="btn-settings">🔒 모든 기기에서 로그아웃</button>
    </form>
    <p class="muted">
      계정이 털린 것 같으면 <strong>여기를 먼저 누르고</strong> 비밀번호를 바꾸세요.
      <strong>이 기기는 그대로 유지</strong>됩니다.
    </p>
    @error('password')<p class="muted">{{ $message }}</p>@enderror
  </section>

  {{-- ── 휴지통 ─────────────────────────────────────────────── --}}
  <section class="settings-section">
    <h2>휴지통</h2>
    <p class="muted">삭제한 글을 되돌리거나 영구 삭제할 수 있어요.</p>
    <a class="btn-settings" href="/trash">🗑 휴지통 열기</a>
  </section>

  {{-- ── 계정 정보 (아이디는 못 바꿈 — 신원 키) ──────────────── --}}
  <section class="settings-section">
    <h2>계정 정보</h2>
    <dl class="settings-info">
      <dt>아이디</dt>
      <dd>{{ auth()->user()->username }}</dd>
      <dt>가입일</dt>
      <dd>{{ auth()->user()->joined_at?->format('Y-m-d') }}</dd>
    </dl>
  </section>
@endsection

{{-- ── 메일 알림 칸만 다시 그리기 ───────────────────────────
     ★ 폼은 그대로 두고 그 위에 얹는다(점진적 향상).
       · JS 가 동작하면  — 폼 전송을 가로채 이 칸만 갈아 끼운다
       · JS 가 없으면    — 폼이 원래대로 전송되어 화면 전체가 새로 그려진다
       어느 쪽이든 서버가 하는 일과 화면의 결과는 같다. 화면을 만드는 곳도
       settings/index.blade.php 한 곳뿐이라, 같은 마크업을 두 벌 쓰지 않는다. --}}
@push('scripts')
<script>
(function () {
  const section = document.getElementById('mail');
  if (!section || !window.fetch || !window.DOMParser) return;   // 못 하면 그냥 폼으로 둔다

  // 서버가 보낸 새 화면에서 필요한 조각만 꺼내 바꿔 끼운다
  function apply(html) {
    const doc  = new DOMParser().parseFromString(html, 'text/html');
    const next = doc.getElementById('mail');
    if (!next) return false;                                     // 설정 화면이 아니면 포기

    section.innerHTML = next.innerHTML;

    // 안내 쪽지도 옮겨 온다 (없으면 있던 것을 치운다)
    const oldFlash = document.querySelector('.flash');
    const newFlash = doc.querySelector('.flash');
    if (newFlash) {
      oldFlash ? oldFlash.replaceWith(newFlash) : section.parentNode.insertBefore(newFlash, section.parentNode.firstChild);
    } else if (oldFlash) {
      oldFlash.remove();
    }

    bind();                                                      // 갈아 낀 폼에 다시 붙인다
    return true;
  }

  function bind() {
    section.querySelectorAll('form').forEach(function (form) {
      form.addEventListener('submit', async function (e) {
        e.preventDefault();

        const button = form.querySelector('button[type=submit]');
        const label  = button ? button.textContent : '';
        if (button) { button.disabled = true; button.textContent = '보내는 중…'; }

        try {
          // fetch 는 리다이렉트를 따라간다 → 돌아오는 것은 저장이 끝난 설정 화면이다.
          // ★ Accept: text/html 을 붙이고, X-Requested-With 는 붙이지 않는다.
          //   Laravel 은 'X-Requested-With: XMLHttpRequest' 와 'Accept: */*' 가 함께 있을 때
          //   JSON 을 원하는 요청으로 보고, 입력 오류를 422 JSON 으로 돌려준다(실측 확인).
          //   우리가 원하는 것은 평소와 같은 화면이다 — 오류 문구까지 그려진 HTML.
          const res  = await fetch(form.action, {
            method: 'POST',
            body: new FormData(form),
            headers: { 'Accept': 'text/html' },
            credentials: 'same-origin',
          });
          const html = await res.text();

          if (!apply(html)) { form.submit(); }                   // 예상 밖 응답이면 원래 방식으로
        } catch (err) {
          form.submit();                                         // 실패하면 원래 방식으로
        } finally {
          if (button && button.isConnected) { button.disabled = false; button.textContent = label; }
        }
      });
    });
  }

  bind();
})();
</script>
@endpush
