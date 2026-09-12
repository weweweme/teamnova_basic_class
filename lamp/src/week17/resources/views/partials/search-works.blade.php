{{-- 작품 검색 결과 — 포스터 + 제목 + 장르·연도 --}}
<ul class="media-list">
  @foreach ($works as $w)
    <li>
      {{-- TMDB 작품엔 우리 slug 가 없다 → 'tmdb-<번호>'.
           누군가 글을 쓰는 순간 그 이름으로 media 표에 저장된다. --}}
      <a href="/works/{{ $w['slug'] }}">
        @if ($w['poster_url'])
          <img class="poster" src="{{ $w['poster_url'] }}" alt="" loading="lazy">
        @else
          <span class="poster poster-empty">No Image</span>
        @endif
        <span class="media-info">
          <strong>@highlight($w['title'], $q)</strong>
          <span class="post-stat">{{ $w['genre'] ?? '' }} · {{ $w['year'] }}</span>
        </span>
      </a>
    </li>
  @endforeach
</ul>
