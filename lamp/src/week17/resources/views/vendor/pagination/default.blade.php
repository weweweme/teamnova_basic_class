{{--
  페이지 번호 — 전통 게시판식 '묶음(블록) 페이징'.

  ★ Laravel 기본 출력은 Tailwind 용 HTML 이라 우리 style.css 와 전혀 맞지 않는다.
    이 파일을 resources/views/vendor/pagination/default.blade.php 에 두면
    {{ $posts->links() }} 가 이 화면을 쓴다. (덮어쓰기 규칙)

  구조: nav.pagination > a.page-nav ×2 + div.page-numbers > a.page-num[.active] + a.page-nav ×2

  ★ 왜 $elements 를 안 쓰는가
    라라벨이 만들어 주는 $elements 는 '현재 쪽 좌우 몇 칸 + 생략표(…)' 방식이다.
    글이 2만 개(1,336쪽)가 되니 이전·다음이 한 칸씩만 움직여 쓸 수가 없었다.
    → 번호를 10개씩 끊어 묶고, 이전·다음이 그 묶음 단위로 건너뛰게 직접 계산한다.
--}}
@php
    // 한 번에 보여 줄 번호 개수. 이 값 하나만 바꾸면 묶음 크기가 바뀐다.
    $blockSize = 10;

    $current = $paginator->currentPage();
    $last    = $paginator->lastPage();

    // 지금 쪽이 속한 묶음의 처음·끝 번호.
    //   예) 14쪽이면 (14-1)/10 = 1 → 1*10+1 = 11 부터, 끝은 20.
    //   ★ intdiv 는 몫만 남기는 나눗셈이다. 14/10 = 1.4 가 아니라 1.
    $blockStart = intdiv($current - 1, $blockSize) * $blockSize + 1;
    $blockEnd   = min($blockStart + $blockSize - 1, $last);

    // 이전·다음은 '묶음 밖으로 한 칸' 나가는 쪽을 가리킨다.
    //   이전 → 앞 묶음의 마지막 쪽 / 다음 → 뒤 묶음의 첫 쪽.
    //   ★ 이렇게 두면 이전을 눌렀다가 다음을 누르면 원래 묶음으로 돌아온다.
    $prevBlock = $blockStart - 1;
    $nextBlock = $blockEnd + 1;
@endphp

@if ($paginator->hasPages())
  <nav class="pagination">
    {{-- 처음 — 1쪽에 있으면 갈 곳이 없다 --}}
    @if ($current <= 1)
      <span class="page-nav disabled">« 처음</span>
    @else
      <a class="page-nav" href="{{ $paginator->url(1) }}">« 처음</a>
    @endif

    {{-- 이전 묶음 — 첫 묶음(1~10)에 있으면 갈 곳이 없다 --}}
    @if ($prevBlock < 1)
      <span class="page-nav disabled">← 이전 {{ $blockSize }}</span>
    @else
      <a class="page-nav" href="{{ $paginator->url($prevBlock) }}">← 이전 {{ $blockSize }}</a>
    @endif

    <div class="page-numbers">
      @for ($page = $blockStart; $page <= $blockEnd; $page++)
        <a class="page-num {{ $page === $current ? 'active' : '' }}"
           href="{{ $paginator->url($page) }}">{{ $page }}</a>
      @endfor
    </div>

    {{-- 다음 묶음 — 마지막 묶음이면 갈 곳이 없다 --}}
    @if ($nextBlock > $last)
      <span class="page-nav disabled">다음 {{ $blockSize }} →</span>
    @else
      <a class="page-nav" href="{{ $paginator->url($nextBlock) }}">다음 {{ $blockSize }} →</a>
    @endif

    {{-- 마지막 --}}
    @if ($current >= $last)
      <span class="page-nav disabled">마지막 »</span>
    @else
      <a class="page-nav" href="{{ $paginator->url($last) }}">마지막 »</a>
    @endif
  </nav>
@endif
