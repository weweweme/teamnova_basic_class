@extends('layouts.app')

@section('title', '휴지통')

@section('content')
  <h1>휴지통</h1>
  <p class="muted">지운 글은 {{ $retentionDays }}일 동안 보관하고, 그 뒤에는 자동으로 완전히 삭제됩니다.</p>

  @if ($posts->isEmpty())
    <p class="muted">휴지통이 비어 있습니다.</p>
  @else
    <ul class="post-list">
      @foreach ($posts as $post)
        @php
          // 남은 보관일 = 보관 기간 - 지난 일수 (음수면 0)
          //   deleted_at 이 Carbon 객체라 이런 계산이 바로 된다.
          $daysLeft = max(0, $retentionDays - (int) $post->deleted_at->diffInDays(now()));
        @endphp
        <li>
          <span class="post-left">
            {{ $post->title }}
            <span class="tag">{{ $post->media->title }}</span>
          </span>
          <span class="post-right">
            <span class="muted">{{ $post->deleted_at->format('Y-m-d') }} 삭제 · {{ $daysLeft }}일 남음</span>

            <form method="post" action="/posts/{{ $post->id }}/restore">
              @csrf
              @method('PATCH')
              <button class="btn-restore" type="submit">↩️ 되돌리기</button>
            </form>

            <form class="delete-form" method="post" action="/posts/{{ $post->id }}/force"
                  onsubmit="return confirm('완전히 삭제하면 되돌릴 수 없습니다. 진행할까요?')">
              @csrf
              @method('DELETE')
              <button class="btn-danger" type="submit">완전 삭제</button>
            </form>
          </span>
        </li>
      @endforeach
    </ul>

    {{ $posts->links() }}
  @endif
@endsection
