{{-- 검색창 + 탭 — 통합/작품/글/유저 네 화면이 함께 쓴다
     (지금 includes/search_ui.php 의 render_search_bar() 가 하던 일) --}}
<form class="search-form" method="get" action="{{ $action }}">
  <input type="text" name="q" maxlength="50" value="{{ $q }}"
         placeholder="작품 · 글 · 유저 통합검색">
  <button type="submit">검색</button>
</form>

<div class="search-tabs">
  @foreach (['/search' => '통합', '/search/works' => '🎬 작품', '/search/posts' => '📝 글', '/search/users' => '👤 유저'] as $path => $label)
    {{-- 페이지 번호는 안 싣는다 — 3페이지를 보다 탭을 옮기면 새 목록의 처음부터가 맞다 --}}
    <a class="{{ $action === $path ? 'active' : '' }}"
       href="{{ $path }}?q={{ urlencode($q) }}">{{ $label }}</a>
  @endforeach
</div>
