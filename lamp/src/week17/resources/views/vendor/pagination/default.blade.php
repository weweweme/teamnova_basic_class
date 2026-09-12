{{--
  페이지 번호 — 기존 board/index.php 의 마크업을 그대로 옮긴 것.

  ★ Laravel 기본 출력은 Tailwind 용 HTML 이라 우리 style.css 와 전혀 맞지 않는다.
    이 파일을 resources/views/vendor/pagination/default.blade.php 에 두면
    {{ $posts->links() }} 가 이 화면을 쓴다. (덮어쓰기 규칙)

  구조: nav.pagination > a.page-nav + div.page-numbers > a.page-num[.active] + a.page-nav
--}}
@if ($paginator->hasPages())
  <nav class="pagination">
    @if ($paginator->onFirstPage())
      <span class="page-nav disabled">← 이전</span>
    @else
      <a class="page-nav" href="{{ $paginator->previousPageUrl() }}">← 이전</a>
    @endif

    <div class="page-numbers">
      @foreach ($elements as $element)
        {{-- 페이지가 아주 많을 때 Laravel 이 끼워 넣는 '…' --}}
        @if (is_string($element))
          <span class="page-num disabled">{{ $element }}</span>
        @endif

        @if (is_array($element))
          @foreach ($element as $page => $url)
            <a class="page-num {{ $page == $paginator->currentPage() ? 'active' : '' }}"
               href="{{ $url }}">{{ $page }}</a>
          @endforeach
        @endif
      @endforeach
    </div>

    @if ($paginator->hasMorePages())
      <a class="page-nav" href="{{ $paginator->nextPageUrl() }}">다음 →</a>
    @else
      <span class="page-nav disabled">다음 →</span>
    @endif
  </nav>
@endif
