{{-- ============================================================
     partials/board — 게시판 본체 (정렬 탭 · 감상 필터 · 도구줄 · 글 목록 · 페이지 이동)

       ★ 왜 따로 떼 놓나
         전체 글 목록(/posts)과 작품 게시판(/works/{slug})은 위쪽만 다르고
         '글을 줄 세워 보여주는 부분'은 완전히 같은 화면이다.
         지금 board/index.php 는 그 둘을 한 파일에서 if 로 갈라 놓았는데,
         Blade 에서는 같은 조각을 @include 로 두 곳에서 불러 쓴다.

       받는 값
         $posts      페이지네이터
         $sort       현재 정렬 키 / $sortTabs   정렬 탭 목록
         $sentiment  현재 감상 필터 / $sentiments 감상 목록
         $perPage    한 페이지 글 수
         $writeUrl   글쓰기 버튼이 갈 주소
         $q          이 게시판 안에서 검색한 말 (없으면 '')

       ★ 클래스 이름과 구조는 기존 board/index.php 와 똑같이 맞춘다.
         style.css 가 이 구조를 전제로 쓰여 있어, 이름만 같고 중첩이 다르면 배치가 어긋난다.
     ============================================================ --}}
@php
  $q = $q ?? '';
  // 지금 주소의 조건은 그대로 두고 한 항목만 바꾼 링크를 만든다.
  //   ★ page 는 항상 뺀다 — 조건이 바뀌면 1페이지부터 다시 봐야 하기 때문이다.
  $link = fn (array $changes) => '?' . http_build_query(
      array_filter(array_merge(request()->except(['page']), $changes), fn ($v) => $v !== '')
  );
@endphp

{{-- 정렬 탭 — ?sort= 만 바꾼다 --}}
<div class="sort-tabs">
  @foreach ($sortTabs as $key => $label)
    <a class="{{ $sort === $key ? 'active' : '' }}" href="{{ $link(['sort' => $key]) }}">{{ $label }}</a>
  @endforeach
</div>

{{-- 감상 필터 — '전체'는 값이 비어 주소에서 아예 빠진다 --}}
<div class="filter-tabs">
  <a class="{{ $sentiment === '' ? 'active' : '' }}" href="{{ $link(['sentiment' => '']) }}">전체</a>
  @foreach ($sentiments as $s)
    <a class="{{ $sentiment === $s ? 'active' : '' }}" href="{{ $link(['sentiment' => $s]) }}">{{ $s }}</a>
  @endforeach
</div>

<div class="board-toolbar">
  <p class="muted">총 {{ $posts->total() }}개 · {{ $posts->currentPage() }}/{{ $posts->lastPage() }} 페이지</p>

  {{-- 한 페이지에 몇 개씩 볼지 — 고르면 쿠키에 기억된다(취향) --}}
  <span class="per-page">
    @foreach ([15, 30, 50] as $n)
      <a class="{{ $perPage === $n ? 'active' : '' }}" href="{{ $link(['per_page' => $n]) }}">{{ $n }}</a>
    @endforeach
  </span>

  @auth
    <a class="btn-write" href="{{ $writeUrl }}">✏️ 글쓰기</a>
  @endauth
</div>

@if ($posts->isEmpty())
  <p class="muted board-empty">해당 조건의 글이 없습니다.</p>
@else
  {{-- board-list = 게시판 전용 배치(제목 왼쪽 / 정보 오른쪽). 홈의 목록과 구분하려고 붙인다. --}}
  <ul class="post-list board-list">
    @foreach ($posts as $post)
      @php
        // 고친 글은 '최종 수정 시각'을 보여준다 — 목록에서 보고 싶은 건
        // "이 글이 마지막으로 언제 달라졌나"이기 때문이다.
        $shownAt  = $post->edited_at ?? $post->created_at;
        $timeHint = $post->created_at->format('Y-m-d H:i') . ' 작성'
                  . ($post->edited_at ? ' · ' . $post->edited_at->format('Y-m-d H:i') . ' 수정' : '');
      @endphp
      <li>
        {{-- ── 왼쪽: 제목 · 감상 · 댓글 수 (글을 고르는 데 필요한 것들) ── --}}
        <span class="post-left">
          {{-- 검색 중이면 제목에서 찾은 글자를 형광펜으로 칠한다 (검색 화면과 같은 조각) --}}
          <a href="/posts/{{ $post->id }}" title="{{ $post->title }}">@highlight($post->title, $q)</a>
          <span class="tag">{{ $post->sentiment }}</span>
          @if ($post->comments_count > 0)
            <span class="post-comments">💬 {{ $post->comments_count }}</span>
          @endif
        </span>

        {{-- ── 오른쪽: 작성자 · 조회 · 시각 (부가 정보) ── --}}
        <span class="post-right">
          {!! $post->author->levelBadge !!} {{ $post->author->nickname }}
          · 조회 {{ $post->views }}
          · <time datetime="{{ $shownAt->toIso8601String() }}" title="{{ $timeHint }}">{{ $shownAt->diffForHumans() }}</time>{{ $post->edited_at ? ' 수정' : '' }}
        </span>
      </li>
    @endforeach
  </ul>

  {{ $posts->links() }}
@endif
