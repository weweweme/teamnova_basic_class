<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8">
  <title>Blade 연습</title>
</head>
<body>
  {{-- 이 괄호가 Blade 주석이다. HTML로 나가지 않는다 (<!-- --> 와 다름) --}}

  <p>안녕하세요, {{ $name }} 님</p>

  {{--
    ★ 아래 값에는 <script>alert(1)</script> 가 들어 있다.
      그런데도 경고창이 뜨지 않고 글자로 보인다.
      {{ }} 가 값을 내보내기 전에 htmlspecialchars 를 거치기 때문이다.
      지금 우리 프로젝트에서 e() 를 243곳에 손으로 붙이던 일이 이것이다.
  --}}
  <p>escape 확인 : {{ $danger }}</p>

  {{--
    일부러 escape를 끄려면 {!! $danger !!} 를 쓴다.
    ★ 사용자가 쓴 값에는 절대 쓰지 않는다 — 그 순간 XSS 통로가 된다.
      (우리 프로젝트에서 '삭제된 댓글' 자리 같은, 우리가 만든 HTML에만 쓴다)
  --}}
</body>
</html>
