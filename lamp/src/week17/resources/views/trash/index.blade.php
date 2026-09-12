@extends('layouts.app')

@section('title', '휴지통')

@section('container', 'narrow')

@section('content')
  <h1>🗑 휴지통</h1>
  <p class="muted">
    <a href="/settings">← 설정으로</a> ·
    삭제한 글은 <strong>{{ $retentionDays }}일</strong> 동안 보관 후 자동으로 영구 삭제됩니다.
  </p>

  @if ($posts->isEmpty())
    <p class="muted">휴지통이 비어 있습니다.</p>
  @else
    <ul class="trash-list">
      @foreach ($posts as $post)
        @php
          // 남은 보관일 = 보관 기간 - 지난 일수 (음수면 0)
          //   deleted_at 이 Carbon 객체라 이런 계산이 바로 된다.
          $daysLeft = max(0, $retentionDays - (int) $post->deleted_at->diffInDays(now()));
        @endphp
        <li class="trash-item">
          <div class="trash-info">
            <strong>{{ $post->title }}</strong>
            <span class="trash-meta">
              {{ $post->media->title }} ·
              {{ $daysLeft > 0 ? $daysLeft . '일 후 자동 삭제' : '오늘 자동 삭제 예정' }}
            </span>
          </div>
          <div class="trash-actions">
            <form method="post" action="/posts/{{ $post->id }}/restore">
              @csrf
              @method('PATCH')
              <button type="submit" class="btn-restore">↩️ 되돌리기</button>
            </form>

            {{-- 영구삭제는 되돌릴 수 없다 → class="delete-form" 을 보고 main.js 가 확인창을 띄운다 --}}
            <form class="delete-form" method="post" action="/posts/{{ $post->id }}/force">
              @csrf
              @method('DELETE')
              <button type="submit" class="btn-delete">🔥 영구삭제</button>
            </form>
          </div>
        </li>
      @endforeach
    </ul>

    {{ $posts->links() }}
  @endif
@endsection
