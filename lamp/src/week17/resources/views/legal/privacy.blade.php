@extends('layouts.app')

@section('title', '개인정보처리방침')

@section('container', 'narrow')

@section('content')
  <h1>개인정보처리방침</h1>
  <p class="muted">제{{ \App\Services\Consent::TERMS_VERSION }}판 · 2026-09-17 적용</p>

  <section class="settings-section legal">
    <h2>1. 무엇을 받는가</h2>

    <h3>가입할 때</h3>
    <ul>
      <li><strong>아이디, 비밀번호</strong> — 비밀번호는 원래 글자를 알 수 없는 형태(해시)로만 보관합니다. 저희도 볼 수 없습니다.</li>
      <li><strong>구글로 가입한 경우</strong> — 구글에서 계정 식별 번호, 이름, 이메일 주소를 받습니다. 그 외에는 받지 않습니다.</li>
    </ul>

    <h3>쓰시는 동안</h3>
    <ul>
      <li><strong>이메일 주소</strong> — 넣지 않아도 됩니다. 넣으시면 인증 메일을 보내고, 그 뒤에 알림 메일과 비밀번호 찾기에 씁니다.</li>
      <li><strong>닉네임, 프로필 사진</strong> — 넣지 않아도 됩니다.</li>
      <li><strong>글, 댓글, 추천, 작품 투표, 신고 기록</strong></li>
      <li><strong>기기 정보</strong> — 브라우저 종류와 운영체제. '로그인한 기기' 목록과 새 기기 로그인 알림에 씁니다.</li>
      <li><strong>접속 기록</strong> — 로그인 시도 기록(같은 곳에서 반복 시도를 막기 위해), 글 조회 기록(조회수 집계).
          IP 주소는 <strong>앞부분만</strong> 남겨서 개인을 특정할 수 없게 합니다.</li>
    </ul>
  </section>

  <section class="settings-section legal">
    <h2>2. 쿠키</h2>
    <p>두 종류로 나누어 다룹니다. <a href="/cookies">쿠키 설정</a>에서 언제든 바꾸실 수 있습니다.</p>
    <ul>
      <li><strong>꼭 필요한 것</strong> — 로그인 상태 유지, 위조 요청 차단. 동의 대상이 아닙니다. 없으면 로그인이 동작하지 않습니다.</li>
      <li><strong>고르실 수 있는 것</strong> — 최근 본 글·작품, 최근 검색어, 정렬·목록 개수 기억.
          <strong>동의하기 전에는 만들지 않습니다.</strong></li>
    </ul>
  </section>

  <section class="settings-section legal">
    <h2>3. 밖으로 나가는 것</h2>
    <ul>
      <li><strong>TMDB</strong> — 작품 정보를 받아오려고 검색어와 작품 번호를 보냅니다. 회원 정보는 보내지 않습니다.</li>
      <li><strong>Google</strong> — 구글 로그인을 고르신 경우에만. 로그인 확인을 위해 오가며, 저희가 구글에 보내는 회원 정보는 없습니다.</li>
      <li><strong>메일</strong> — 알림·인증 메일을 보내기 위해 메일 서버를 거칩니다.</li>
    </ul>
    <p class="muted">그 밖의 곳에 회원 정보를 넘기거나 팔지 않습니다.</p>
  </section>

  <section class="settings-section legal">
    <h2>4. 얼마나 보관하는가</h2>
    <table class="legal-table">
      <tr><th>때</th><th>무슨 일이 일어나는가</th></tr>
      <tr>
        <td>탈퇴 즉시</td>
        <td>로그인이 막히고, 로그인 중인 모든 기기가 해제됩니다. 화면에서 회원 정보가 사라집니다.
            기기 목록과 작성 중이던 초안은 이때 지웁니다.</td>
      </tr>
      <tr>
        <td>탈퇴 후 {{ \App\Services\Accounts::GRACE_DAYS }}일 이내</td>
        <td>다시 로그인하시면 계정이 그대로 돌아옵니다.</td>
      </tr>
      <tr>
        <td>탈퇴 후 {{ \App\Services\Accounts::GRACE_DAYS }}일이 지나면</td>
        <td><strong>아이디, 이메일 주소, 구글 계정 연결, 프로필 사진을 지웁니다.</strong>
            받은 알림 기록도 지웁니다. 되돌릴 수 없습니다.</td>
      </tr>
      <tr>
        <td>그 뒤</td>
        <td>글과 댓글은 남지만 누가 썼는지 알 수 없습니다. 작성자는 '탈퇴한 사용자'로만 보입니다.</td>
      </tr>
    </table>
    <p class="muted">지우는 일은 사람이 기억해서 하지 않습니다. 매일 정해진 시각에 자동으로 실행됩니다.</p>
  </section>

  <section class="settings-section legal">
    <h2>5. 직접 하실 수 있는 것</h2>
    <ul>
      <li><a href="/settings">설정</a> — 닉네임·프로필 사진 바꾸기, 이메일 등록·삭제, 알림 끄고 켜기</li>
      <li><a href="/settings">로그인한 기기</a> — 기기별로 해제하거나 한 번에 모두 해제</li>
      <li><a href="/cookies">쿠키 설정</a> — 동의 바꾸기, 지금까지의 동의 기록 보기</li>
      <li><a href="/settings/leave">회원 탈퇴</a></li>
      <li>알림 메일 맨 아래 링크로, 로그인하지 않고도 알림을 끌 수 있습니다</li>
    </ul>
  </section>

  <section class="settings-section legal">
    <h2>6. 안전하게 지키려고 하는 일</h2>
    <ul>
      <li>비밀번호는 해시로만 보관합니다.</li>
      <li>설정처럼 민감한 화면은 로그인과 별개로 본인 확인을 한 번 더 합니다.</li>
      <li>비밀번호를 바꾸거나 새 기기에서 로그인하면 알려 드립니다. 이 알림은 끌 수 없습니다.</li>
      <li>메일 속 링크에는 서명이 들어 있어, 주소를 고치면 동작하지 않습니다.</li>
    </ul>
  </section>

  <p class="muted"><a href="/terms">이용약관 보기 →</a></p>
@endsection
