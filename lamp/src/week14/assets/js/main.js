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



// ── 삭제 전 확인창 (<dialog> 팝업) ──────────────────────────
//   삭제는 되돌릴 수 없는 동작이라 보내기 전에 한 번 더 묻는다.
//   예전엔 브라우저 기본 confirm() 을 썼는데, 신고 팝업과 생김새가 따로 놀아서
//   같은 <dialog> 방식으로 통일했다. (팝업은 footer.php 에 하나만 두고 재사용)
//
//   ★ 어려운 점: confirm() 은 답이 나올 때까지 코드가 '멈춰서' 기다려주지만,
//     <dialog> 는 멈추지 않는다. 창을 여는 순간 다음 줄이 바로 실행된다.
//   ★ 해결: 일단 전송을 무조건 막아두고(preventDefault),
//     사용자가 '삭제'를 누르면 그때 그 폼을 직접 전송한다.

const confirmDialog = document.querySelector('#confirm-dialog');
const confirmOk     = document.querySelector('#confirm-ok');
const confirmCancel = document.querySelector('#confirm-cancel');

// '어느 폼을 기다리는 중인지' 기억해둘 자리.
//   창을 연 폼과 나중에 전송할 폼이 같아야 하므로 붙잡아 둔다.
let pendingDeleteForm = null;

if (confirmDialog && confirmOk && confirmCancel) {

    // 글 삭제 폼과 댓글 삭제 폼 모두 class="delete-form" 을 갖고 있어 한 번에 처리된다.
    document.querySelectorAll('.delete-form').forEach(function (form) {

        // 'submit' = 폼이 전송되기 '직전'에 발생하는 이벤트.
        form.addEventListener('submit', function (event) {
            // preventDefault() = '원래 일어날 일'(= 폼 전송)을 막는다.
            //   확인 창의 답을 받기 전이므로 일단 무조건 막는다.
            event.preventDefault();

            pendingDeleteForm = form;      // 이 폼을 기다린다고 기억
            confirmDialog.showModal();     // 뒤 배경이 어두워지는 모달로 열기
        });
    });

    // '삭제' 확인 → 기억해둔 폼을 진짜로 전송한다.
    confirmOk.addEventListener('click', function () {
        confirmDialog.close();

        // form.submit() 은 submit 이벤트를 다시 일으키지 않는다.
        //   (다시 일으킨다면 확인 창이 무한히 열렸을 것)
        if (pendingDeleteForm) {
            pendingDeleteForm.submit();
        }
    });

    // '취소' → 창만 닫고 아무 일도 하지 않는다.
    confirmCancel.addEventListener('click', function () {
        confirmDialog.close();
        pendingDeleteForm = null;          // 기억 지우기
    });
}


// ── 알림(토스트) 자동으로 사라지게 하기 ─────────────────────
//   ★ 알림 '내용'은 서버(PHP)가 세션에서 꺼내 이미 그려놨고,
//     JS는 그걸 몇 초 뒤 '걷어내는 연출'만 담당한다. (GET/POST 흐름과 무관)

const FLASH_STAY_MS        = 3000;   // 보통 알림: 3초
const FLASH_STAY_ACTION_MS = 8000;   // '되돌리기' 버튼이 있으면 더 오래 (누를 시간을 줘야 하니까)
const FLASH_FADE_MS        = 400;    // 흐려지는 데 걸리는 시간 (CSS transition 과 맞춤)

const flash = document.querySelector('.flash');

// 알림이 없는 페이지가 대부분이니 '있는지 먼저 확인'(Tester-Doer).
if (flash) {
    // 알림 안에 버튼이 있으면 = 사용자가 눌러야 할 것이 있다 → 더 오래 보여준다.
    const hasAction = flash.querySelector('.flash-action') !== null;
    const stayMs    = hasAction ? FLASH_STAY_ACTION_MS : FLASH_STAY_MS;

    // setTimeout(할 일, 밀리초) = "이만큼 기다렸다가 이 일을 해라" (예약 실행)
    setTimeout(function () {
        // 클래스만 붙이면 CSS의 transition 이 알아서 부드럽게 흐리게 만든다.
        flash.classList.add('fade-out');

        // 다 흐려진 뒤엔 아예 걷어낸다. (투명해도 클릭을 가로막을 수 있으므로)
        setTimeout(function () {
            flash.remove();
        }, FLASH_FADE_MS);
    }, stayMs);
}


// ── 글자 수 카운터 ───────────────────────────────────────────
//   제목·내용·댓글 칸 아래에 "123 / 5000" 을 실시간으로 보여준다.
//
//   ★ 이건 '친절'이지 '방어'가 아니다.
//     화면 검사는 F12로 지워버릴 수 있으므로, 서버(create.php/update.php)에서
//     mb_strlen 으로 다시 센다. 화면 = 힌트, 서버 = 진짜 검사.
//
//   maxlength 속성이 붙은 칸을 자동으로 찾아 처리한다 → 칸이 늘어도 JS는 안 고쳐도 된다.
const LIMIT_WARN_RATIO = 0.9;   // 90%를 넘으면 색으로 경고

document.querySelectorAll('input[maxlength], textarea[maxlength]').forEach(function (field) {
    const max = Number(field.getAttribute('maxlength'));

    // 카운터를 표시할 작은 글자 상자를 만들어 입력칸 바로 뒤에 넣는다.
    const counter = document.createElement('div');
    counter.className = 'char-counter';
    field.insertAdjacentElement('afterend', counter);

    function update() {
        // field.value.length = 지금 입력된 글자 수
        const now = field.value.length;
        counter.textContent = now + ' / ' + max;

        // 한계에 가까워지면 색을 바꿔 미리 알려준다.
        counter.classList.toggle('near-limit', now >= max * LIMIT_WARN_RATIO);
    }

    // 'input' = 글자가 하나 바뀔 때마다 발생하는 이벤트 (붙여넣기·삭제도 포함).
    field.addEventListener('input', update);
    update();   // 처음에도 한 번 (수정 폼처럼 이미 글자가 있는 경우)
});


// ── 작성 중 이탈 경고 ────────────────────────────────────────
//   글을 쓰다가 실수로 뒤로가기·창닫기를 하면 쓰던 내용이 사라진다.
//   내용을 건드린 적이 있을 때만 경고한다. (빈 폼에서 나가는데 막으면 짜증남)
//
//   ★ 브라우저 보안 규칙: 경고 문구는 우리가 정할 수 없다.
//     사이트가 겁주는 메시지를 띄우는 걸 막으려고, 브라우저가 정해진 문장만 보여준다.
//     우리는 "물어볼지 말지"만 정할 수 있다.

const writeForm = document.querySelector('.write-form');

if (writeForm) {
    let isDirty = false;   // 'dirty' = 손댄 흔적이 있다는 뜻 (편집기에서 쓰는 표현)

    writeForm.addEventListener('input', function () {
        isDirty = true;
    });

    // 제출은 정상적인 이탈이므로 경고하지 않는다.
    //   ★ submit 이벤트가 beforeunload 보다 먼저 일어나기 때문에 이 순서가 통한다.
    writeForm.addEventListener('submit', function () {
        isDirty = false;
    });

    // 'beforeunload' = 페이지를 떠나기 직전에 발생하는 이벤트.
    window.addEventListener('beforeunload', function (event) {
        if (isDirty) {
            // preventDefault() 를 부르면 브라우저가 "정말 나갈까요?" 를 대신 물어본다.
            event.preventDefault();
        }
    });
}
