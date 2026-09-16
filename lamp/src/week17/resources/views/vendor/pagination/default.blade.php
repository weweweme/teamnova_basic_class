{{--
  페이지 번호 — 기존 board/index.php 의 마크업을 바탕으로, 처음·마지막 버튼을 더한 것.

  ★ Laravel 기본 출력은 Tailwind 용 HTML 이라 우리 style.css 와 전혀 맞지 않는다.
    이 파일을 resources/views/vendor/pagination/default.blade.php 에 두면
    {{ $posts->links() }} 가 이 화면을 쓴다. (덮어쓰기 규칙)

  구조: nav.pagination > a.page-nav ×2 + div.page-numbers > a.page-num[.active] + a.page-nav ×2

  ★ $elements 는 페이지네이터가 만들어 준다. 가운데가 생략될 자리에는 '...' 문자열이 들어 있어서
    번호(배열)와 생략표(문자열)를 갈라서 그린다.
--}}
@if ($paginator->hasPages())
  <nav class="pagination">
    {{-- 처음 · 이전 — 1페이지에서는 둘 다 눌리지 않는다 --}}
    @if ($paginator->onFirstPage())
      <span class="page-nav disabled">« 처음</span>
      <span class="page-nav disabled">← 이전</span>
    @else
      <a class="page-nav" href="{{ $paginator->url(1) }}">« 처음</a>
      <a class="page-nav" href="{{ $paginator->previousPageUrl() }}">← 이전</a>
    @endif

    <div class="page-numbers">
      @foreach ($elements as $element)
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

    {{-- 다음 · 마지막 --}}
    @if ($paginator->hasMorePages())
      <a class="page-nav" href="{{ $paginator->nextPageUrl() }}">다음 →</a>
      <a class="page-nav" href="{{ $paginator->url($paginator->lastPage()) }}">마지막 »</a>
    @else
      <span class="page-nav disabled">다음 →</span>
      <span class="page-nav disabled">마지막 »</span>
    @endif
  </nav>
@endif
