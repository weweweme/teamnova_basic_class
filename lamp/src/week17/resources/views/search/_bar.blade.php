{{-- 검색창 — 통합/글/유저/작품 화면이 함께 쓴다.
     @include 로 끼워 넣는다 (지금 render_search_bar() 가 하던 일) --}}
<form class="search-bar" method="get" action="{{ $action ?? '/search' }}" role="search">
  <input type="search" name="q" value="{{ $q }}" maxlength="50" placeholder="작품 · 글 · 유저 검색" autofocus>
  <button type="submit">🔍</button>
</form>

<nav class="search-tabs">
  <a href="/search?q={{ urlencode($q) }}">통합</a>
  <a href="/search/posts?q={{ urlencode($q) }}">글</a>
  <a href="/search/users?q={{ urlencode($q) }}">유저</a>
  <a href="/search/works?q={{ urlencode($q) }}">작품</a>
</nav>
