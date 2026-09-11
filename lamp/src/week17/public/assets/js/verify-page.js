// ============================================================
// verify-page.js — '기기 확인' 화면에서만 도는 조각
//
//   device-key.js가 '도장을 다루는 법'이라면, 이 파일은 '이 화면의 진행 순서'다.
//   ★ 둘을 나눈 이유: 도장 기능은 **모든 페이지**가 쓰고(만료 전 미리 찍기),
//     아래 순서는 **이 화면 하나**만 쓴다. 수명이 다르면 파일도 나눈다.
// ============================================================

(() => {
  'use strict';

  const boot = document.getElementById('key-boot');
  if (!boot || !window.deviceKey) {
    showError();
    return;
  }

  const hasKey    = boot.dataset.hasKey === '1';
  const canEnroll = boot.dataset.canEnroll === '1';
  const back      = boot.dataset.back || '/';

  function showError() {
    document.getElementById('key-status')?.setAttribute('hidden', '');
    document.getElementById('key-error')?.removeAttribute('hidden');
  }

  // ★ 등록이냐 연장이냐는 **서버가 정해준 대로** 따른다.
  //   브라우저에 도장이 있는지로 판단하면, 서버엔 등록돼 있는데 브라우저에서 지운 경우를
  //   구분하지 못한다.
  const first = hasKey ? window.deviceKey.refresh : window.deviceKey.enroll;

  first()
    .then(() => { location.replace(back); })
    .catch(() => {
      // 여기까지 왔다는 건 '서버엔 도장이 있는데 브라우저엔 없다'는 뜻이다.
      //   (사용자가 저장 데이터를 지운 경우가 대부분)
      //
      //   ★★ 이때 새로 등록해도 되는 조건은 딱 하나 — **비밀번호를 방금 확인했나.**
      //     쿠키를 훔친 쪽에는 비밀번호가 없으므로, 그 조건이 곧 '주인만'을 뜻한다.
      //     서버가 그 판단을 내려 data-can-enroll로 알려준다.
      if (!canEnroll) {
        showError();          // 다시 로그인하라는 안내가 뜬다
        return;
      }
      window.deviceKey.enroll()
        .then(() => { location.replace(back); })
        .catch(showError);
    });
})();
