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
