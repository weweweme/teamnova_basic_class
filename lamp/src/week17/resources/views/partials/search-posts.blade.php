{{-- 글 검색 결과 — 제목 + 본문 미리보기 + 작성 정보 --}}
<ul class="post-list search-post-list">
  @foreach ($posts as $p)
    <li>
      <a class="search-post-title" href="/posts/{{ $p->id }}">@highlight($p->title, $q)</a>
      <span class="tag">{{ $p->sentiment }}</span>

      {{-- 본문 미리보기 — 검색어가 있는 부분을 잘라서 --}}
      <p class="search-snippet">@snippet($p->content, $q)</p>

      <span class="search-post-meta">
        <a href="/works/{{ $p->media->slug }}">{{ $p->media->title }}</a>
        · {!! $p->author->levelBadge !!} {{ $p->author->nickname }}
        · <time datetime="{{ $p->created_at->toIso8601String() }}">{{ $p->created_at->format('Y-m-d') }}</time>
        @if ($p->comments_count > 0) · 💬 {{ $p->comments_count }}@endif
        @if ($p->likers_count > 0) · 👍 {{ $p->likers_count }}@endif
      </span>
    </li>
  @endforeach
</ul>
