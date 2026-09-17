@extends('layouts.app')

@section('title', '이용약관')

@section('container', 'narrow')

@section('content')
  <h1>이용약관</h1>
  <p class="muted">제{{ \App\Services\Consent::TERMS_VERSION }}판 · 2026-09-17 적용</p>

  <section class="settings-section legal">
    <h2>1. 이 서비스는 무엇인가</h2>
    <p>영화와 드라마를 보고 감상을 남기고 읽는 커뮤니티입니다. 작품 정보는 TMDB에서 받아옵니다.</p>
    <p class="muted">학습 목적으로 만든 서비스입니다. 중요한 자료를 이곳에만 보관하지 마세요.</p>
  </section>

  <section class="settings-section legal">
    <h2>2. 계정</h2>
    <ul>
      <li>아이디는 한 번 정하면 바꿀 수 없습니다. 주소와 글 작성자 표시에 쓰이기 때문입니다. 닉네임은 설정에서 바꿀 수 있습니다.</li>
      <li>비밀번호는 본인이 관리합니다. 이메일을 등록하지 않으면 비밀번호를 잊었을 때 되찾을 방법이 없습니다.</li>
      <li>한 사람이 여러 계정을 만들어 추천·투표를 늘리는 일은 하지 말아 주세요.</li>
      <li>구글 계정으로 가입한 경우, 그 계정과 이곳의 계정은 별개로 관리됩니다. 서로 연결하는 기능은 없습니다.</li>
    </ul>
  </section>

  <section class="settings-section legal">
    <h2>3. 올린 글</h2>
    <ul>
      <li>글과 댓글의 저작권은 쓴 사람에게 있습니다. 이 서비스는 그것을 보여주기 위해서만 사용합니다.</li>
      <li>남을 괴롭히거나, 남의 글을 베끼거나, 광고를 목적으로 하는 글은 지울 수 있습니다.</li>
      <li>글을 지우면 휴지통으로 가고, 거기서 되돌리거나 완전히 지울 수 있습니다.</li>
      <li><strong>탈퇴해도 쓰신 글과 댓글은 남습니다.</strong> 작성자 표시만 '탈퇴한 사용자'로 바뀝니다.
          커뮤니티의 글은 혼자 쓴 것이 아니라 대화라서, 지우면 남의 글에 달린 흐름이 끊기기 때문입니다.
          이 점에 동의하지 않으신다면 탈퇴 전에 글을 직접 지워 주세요.</li>
    </ul>
  </section>

  <section class="settings-section legal">
    <h2>4. 하지 말아야 할 것</h2>
    <ul>
      <li>자동 프로그램으로 글을 대량으로 올리거나 자료를 긁어가는 일</li>
      <li>남의 계정으로 접속하려는 시도</li>
      <li>서비스를 고장 내려는 시도</li>
    </ul>
  </section>

  <section class="settings-section legal">
    <h2>5. 책임</h2>
    <p>
      학습 목적의 서비스라 예고 없이 멈추거나 자료가 사라질 수 있습니다.
      그로 인한 손해에 대해 책임지지 않습니다.
    </p>
  </section>

  <section class="settings-section legal">
    <h2>6. 약관이 바뀌면</h2>
    <p>바뀐 내용은 이 화면에 올리고 판 번호를 올립니다. 계속 사용하시면 바뀐 약관에 동의한 것으로 봅니다.</p>
  </section>

  <p class="muted"><a href="/privacy">개인정보처리방침 보기 →</a></p>
@endsection
