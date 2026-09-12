@extends('layouts.app')
@section('title', '설정')

@section('content')
  <h1>⚙️ 설정</h1>
  <p class="muted">{{ auth()->user()->username }} 님 · {{ auth()->user()->joined_at->format('Y-m-d') }} 가입</p>

  <section>
    <h2>닉네임</h2>
    <form method="post" action="/settings/nickname">
      @csrf
      @method('PATCH')
      <input type="text" name="nickname" value="{{ old('nickname', auth()->user()->nickname) }}" maxlength="20" required>
      <button type="submit">변경</button>
      @error('nickname')<span class="muted">{{ $message }}</span>@enderror
    </form>
  </section>

  <section>
    <h2>비밀번호 변경</h2>
    <p class="muted">비밀번호를 바꾸면 다른 기기의 로그인은 자동으로 해제됩니다.</p>
    <form method="post" action="/settings/password">
      @csrf
      @method('PATCH')
      <p><label>현재 비밀번호 <input type="password" name="current" required></label>
        @error('current')<span class="muted">{{ $message }}</span>@enderror</p>
      <p><label>새 비밀번호 <input type="password" name="new" minlength="4" required></label>
        @error('new')<span class="muted">{{ $message }}</span>@enderror</p>
      <p><label>새 비밀번호 확인 <input type="password" name="new_confirmation" minlength="4" required></label></p>
      <button type="submit">변경</button>
    </form>
  </section>

  <section>
    <h2>내 기기</h2>
    <p class="muted">이 계정으로 로그인한 적 있는 기기 목록입니다. 낯선 기기가 있으면 해제하세요.</p>
    <ul class="device-list">
      @foreach ($devices as $device)
        <li>
          <b>{{ $device->describe() }}</b>
          @if ($device->device_id === $thisDeviceId)
            <span class="tag">지금 이 기기</span>
          @endif
          <small class="muted">
            마지막 로그인 {{ $device->last_login_at?->diffForHumans() }}
            · 처음 본 날 {{ $device->first_seen_at?->format('Y-m-d') }}
            @if ($device->hasKey()) · 🔑 도장 등록됨 @endif
          </small>

          @if ($device->device_id !== $thisDeviceId)
            <form class="delete-form" method="post" action="/settings/devices"
                  onsubmit="return confirm('이 기기를 해제하면 그 기기의 로그인도 끊깁니다. 진행할까요?')">
              @csrf
              @method('DELETE')
              <input type="hidden" name="device_id" value="{{ $device->device_id }}">
              <input type="password" name="password" placeholder="비밀번호 확인" required>
              <button type="submit">해제</button>
            </form>
          @endif
        </li>
      @endforeach
    </ul>
    @error('device_id')<span class="muted">{{ $message }}</span>@enderror
  </section>

  <section>
    <h2>다른 기기 로그아웃</h2>
    <p class="muted">이 기기의 로그인은 유지하고, 다른 곳에서 로그인된 것만 끊습니다.</p>
    <form method="post" action="/settings/logout-others">
      @csrf
      <p><label>비밀번호 확인 <input type="password" name="password" required></label>
        @error('password')<span class="muted">{{ $message }}</span>@enderror</p>
      <button type="submit">다른 기기 모두 로그아웃</button>
    </form>
  </section>
@endsection
