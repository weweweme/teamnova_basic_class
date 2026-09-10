<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8">
  <title>글 목록</title>
</head>
<body>
  {{-- $posts 는 배열이 아니라 '페이지 하나를 담은 객체'다.
       그래서 반복도 되고, total()·currentPage()·links() 같은 것도 함께 갖고 있다. --}}
  <h1>글 목록 — 전체 {{ $posts->total() }}개 중 {{ $posts->currentPage() }}/{{ $posts->lastPage() }} 페이지</h1>

  <ul>
    @foreach ($posts as $post)
      <li>
        #{{ $post->id }} {{ $post->title }}
        {{-- created_at 이 Carbon 객체라 이런 계산이 바로 된다 --}}
        <small>({{ $post->created_at->diffForHumans() }})</small>
      </li>
    @endforeach
  </ul>

  {{-- ★ 이 한 줄이 페이지 번호 HTML을 만든다.
       지금 우리 코드에서 이전/다음·번호·현재 페이지 강조를 직접 그리던 부분이다.
       기본 출력이 Tailwind용이라 CSS를 안 붙인 지금은 밋밋하게 보인다. --}}
  {{ $posts->links() }}
</body>
</html>
