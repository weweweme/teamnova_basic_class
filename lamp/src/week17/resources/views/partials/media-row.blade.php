{{--
  가로 스크롤 포스터 줄 — 지금 includes/media_row.php 의 render_media_row() 자리.
  ★ 클래스 이름과 구조를 style.css 가 기대하는 그대로 맞춘다.
    (.media-row > .row-scroll > a.row-card > img.row-poster + span.row-title + span.row-meta)
--}}
<section class="media-row media-row-{{ $size ?? 'sm' }}">
  <h2>{{ $title }}</h2>
  <div class="row-scroll">
    @foreach ($items as $item)
      <a class="row-card" href="{{ $item['url'] }}">
        @if ($item['poster_url'])
          <img class="row-poster" src="{{ $item['poster_url'] }}" alt="" loading="lazy">
        @endif
        <span class="row-title">{{ $item['title'] }}</span>
        @if (! empty($item['meta']))
          <span class="row-meta">{{ $item['meta'] }}</span>
        @endif
      </a>
    @endforeach
  </div>
</section>
