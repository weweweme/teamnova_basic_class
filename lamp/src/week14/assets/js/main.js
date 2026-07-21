// ============================================================
// main.js — 페이지의 '동작(JS)'을 담는 곳
//   지금은 '신고 팝업'을 열고 닫는 일만 한다.
//   ★ 중요: 실제 신고 전송은 여전히 '폼의 POST'가 한다.
//     JS는 창을 보여주고 감추는 '연출'만 담당 (GET/POST 흐름은 그대로).
// ============================================================

// ── 신고 팝업 열고 닫기 ──────────────────────────────────────
//   발표자료에서 본 JS의 3단계 그대로:
//   ① 요소 고르기  →  ② 이벤트 연결  →  ③ DOM 조작(동작)

// ① 요소 고르기 (#은 id로 찾으라는 뜻 — CSS 선택자와 같은 문법)
const reportOpen   = document.querySelector('#report-open');    // 신고 버튼
const reportDialog = document.querySelector('#report-dialog');  // 팝업 창
const reportCancel = document.querySelector('#report-cancel');  // 취소 버튼

// 이 파일은 '모든 페이지'에서 불러오는데, 홈·목록엔 신고 버튼이 없다.
// 없는 요소에 이벤트를 걸면 에러가 나므로 '있는지 먼저 확인'(Tester-Doer).
if (reportOpen && reportDialog && reportCancel) {

    // ② 클릭 이벤트 연결 → ③ 팝업 열기
    //   showModal() = <dialog>를 '모달'로 연다.
    //   (뒤 배경이 어두워지고, 팝업 밖은 클릭이 막힌다)
    reportOpen.addEventListener('click', function () {
        reportDialog.showModal();
    });

    // 취소 버튼 → 팝업 닫기
    //   close() = 팝업 닫기. Esc 키로 닫히는 건 <dialog>가 기본으로 해준다.
    reportCancel.addEventListener('click', function () {
        reportDialog.close();
    });
}


// ── 삭제 전 확인창 ───────────────────────────────────────────
//   삭제는 되돌릴 수 없으니 보내기 전에 한 번 더 묻는다.
//   querySelectorAll = 조건에 맞는 요소를 '전부' 고른다 (querySelector는 첫 하나만).
//   글 삭제 폼과 댓글 삭제 폼 모두 class="delete-form" 을 갖고 있어 한 번에 처리된다.
const deleteForms = document.querySelectorAll('.delete-form');

deleteForms.forEach(function (form) {
    // 'submit' = 폼이 전송되기 '직전'에 발생하는 이벤트.
    //   여기서 막으면 전송이 취소된다.
    form.addEventListener('submit', function (event) {
        // confirm() = 확인/취소 창을 띄우고, 확인이면 true / 취소면 false를 돌려준다.
        const ok = confirm('정말 삭제할까요? 되돌릴 수 없습니다.');

        if (!ok) {
            // preventDefault() = '원래 일어날 일'(= 폼 전송)을 막는다.
            //   이게 없으면 취소를 눌러도 그냥 전송돼버린다.
            event.preventDefault();
        }
    });
});
