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

  const hasKey = boot.dataset.hasKey === '1';
  const back   = boot.dataset.back || '/';

  function showError() {
    document.getElementById('key-status')?.setAttribute('hidden', '');
    document.getElementById('key-error')?.removeAttribute('hidden');
  }

  // ★ 등록이냐 연장이냐는 **서버가 정해준 대로** 따른다.
  //   브라우저에 도장이 있는지로 판단하면, 서버엔 등록돼 있는데 브라우저에서 지운 경우
  //   (개인정보 보호 모드·저장소 청소) 새 도장을 만들어 등록하려다 거부당한다.
  const run = hasKey ? window.deviceKey.refresh : window.deviceKey.enroll;

  run()
    .then(() => { location.replace(back); })
    .catch(async () => {
      // 연장이 실패했다면 — 브라우저 쪽 도장이 사라진 경우다.
      //   ★ 서버엔 아직 옛 도장이 등록돼 있어서 새로 등록할 수도 없다.
      //     이때는 **다시 로그인하는 수밖에 없다.** 그게 정상 동작이다 —
      //     도장을 갈아 끼우는 길이 열려 있으면 훔친 쪽도 그 길을 쓴다.
      showError();
    });
})();
