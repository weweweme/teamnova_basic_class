// ============================================================
// main.js — 페이지의 '동작(JS)'을 담는 곳
//   지금은 '신고 팝업'을 열고 닫는 일만 한다.
//   ★ 중요: 실제 신고 전송은 여전히 '폼의 POST'가 한다.
//     JS는 창을 보여주고 감추는 '연출'만 담당 (GET/POST 흐름은 그대로).
// ============================================================

// ★ week16에서 여기 있던 withIdentity() 함수가 사라졌다.
//   week15는 '지금 누구인지'가 주소(?as=영화광)에 실려 다녔다. 서버가 그리는 링크는
//   PHP 리라이터가 자동으로 붙여줬지만, 여기서 JS가 '새로 만드는' 링크는 PHP가
//   손댈 수 없다 — 이미 HTML을 보내버린 뒤라서. 그래서 같은 일을 하는 함수를
//   JS에도 하나 더 두고, 링크를 만들 때마다 통과시켜야 했다.
//   하나라도 빠뜨리면 그 카드를 누르는 순간 로그아웃되는, 찾기 어려운 버그였다.
//   → 신원이 세션(서버 금고)으로 옮겨가면서 이 이중 관리가 통째로 없어졌다.

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
//   ★ 알림 '내용'은 서버(PHP)가 flash 쿠키에서 꺼내 이미 그려놨고,
//     JS는 몇 초 뒤 걷어내는 '연출'만 담당한다.
//
//   ★ week16에서 여기 있던 '주소 청소' 블록이 사라졌다.
//     week15는 알림이 주소(?flash=…)에 적혀 있어서, 그냥 두면 새로고침할 때마다 또 떴다.
//     그래서 화면을 그린 직후 JS가 history.replaceState로 주소에서 알림 파라미터
//     4개를 지워야 했다. util.php의 FLASH_KEYS와 똑같은 목록을 여기에도 하나 더 두고,
//     한쪽을 고치면 다른 쪽도 같이 고쳐야 하는 관리 부담까지 딸려 있었다.
//     → 알림이 주소를 떠나면서 주소에 아무것도 안 남는다. 지울 것이 없어졌다.

const FLASH_STAY_MS        = 3000;   // 보통 알림: 3초
const FLASH_STAY_ACTION_MS = 8000;   // '되돌리기' 버튼이 있으면 더 오래 (누를 시간을 줘야 하니까)
const FLASH_FADE_MS        = 400;    // 흐려지는 데 걸리는 시간 (CSS transition 과 맞춤)

const flash = document.querySelector('.flash');

// 알림이 없는 페이지가 대부분이니 '있는지 먼저 확인'(Tester-Doer).
if (flash) {
    // ── 걷어내기 ───────────────────────────────────────────
    //   '몇 초 뒤 자동으로'와 '× 를 눌러 바로'가 똑같이 동작해야 하므로 함수로 묶는다.
    function dismissFlash() {
        // 클래스만 붙이면 CSS의 transition 이 알아서 부드럽게 흐리게 만든다.
        flash.classList.add('fade-out');

        // 다 흐려진 뒤엔 아예 걷어낸다. (투명해도 클릭을 가로막을 수 있으므로)
        setTimeout(function () {
            flash.remove();
        }, FLASH_FADE_MS);
    }

    // 알림 안에 버튼이 있으면 = 사용자가 눌러야 할 것이 있다 → 더 오래 보여준다.
    const hasAction = flash.querySelector('.flash-action') !== null;
    const stayMs    = hasAction ? FLASH_STAY_ACTION_MS : FLASH_STAY_MS;

    // setTimeout(할 일, 밀리초) = "이만큼 기다렸다가 이 일을 해라" (예약 실행)
    //   반환값(예약표)을 들고 있어야, 사용자가 먼저 닫았을 때 예약을 취소할 수 있다.
    const autoTimer = setTimeout(dismissFlash, stayMs);

    // × 버튼 — 기다리지 않고 바로 닫는다.
    const closeButton = flash.querySelector('.flash-close');
    if (closeButton) {
        closeButton.addEventListener('click', function () {
            // 이미 손으로 닫았으니 예약된 자동 닫기는 취소한다.
            //   (안 하면 사라진 뒤에 타이머가 깨어나 없는 요소를 건드린다)
            clearTimeout(autoTimer);
            dismissFlash();
        });
    }
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


// ── 작성 중 이탈 경고 → week16에서 걷어냄 ────────────────────
//   여기 원래 beforeunload 경고가 있었다. 글을 쓰다 나가면 브라우저가
//   "변경사항이 저장되지 않을 수 있습니다"를 물어보게 하는 코드였다.
//
//   ★ 없앤 이유: 이제 **거짓말이 되기 때문**이다.
//     글쓰기 폼은 쓰는 동안 세션에 초안을 저장하고(post/write.php + api/draft.php),
//     페이지를 떠나기 직전에도 sendBeacon으로 한 번 더 저장한다.
//     실제로는 저장되는데 "저장 안 될 수 있다"고 물으면, 새로고침할 때마다
//     쓸데없는 확인창이 뜨고 사용자는 그 경고를 신뢰하지 않게 된다.
//
//   ★ 그 뒤 임시저장이 **버튼 방식**으로 바뀌면서(안 누르면 진짜로 날아감) 경고가 다시
//     사실이 되었고, **post/write.php 의 초안 스크립트 안으로 옮겨** 되살렸다.
//     거기 있어야 '저장 뒤 바뀐 게 있나(dirty)'를 알 수 있어서 **저장했을 땐 안 묻는다.**
//     → 경고와 저장 상태는 같은 자리에 있어야 정직해진다.


// ── 작품 가로 줄: 좌우 화살표 + 마우스휠 가로 스크롤 ─────────
//   [문제] 스크롤바를 숨겨서, 일반 마우스(세로 휠만)로는 옆으로 못 넘긴다.
//     (터치·트랙패드는 원래 잘 됨. 데스크톱 마우스만 문제)
//   [해결] ① 넷플릭스식 ‹ › 버튼을 넣고  ② 줄 위에서 세로 휠을 가로로 바꿔준다.

document.querySelectorAll('.row-scroll').forEach(function (scroll) {
    // ① 줄을 감싸는 상자를 만들어 그 안에 넣는다 (화살표를 얹을 기준).
    const wrap = document.createElement('div');
    wrap.className = 'row-wrap';
    scroll.parentNode.insertBefore(wrap, scroll);
    wrap.appendChild(scroll);

    // ② 좌우 버튼 만들기
    const prev = document.createElement('button');
    const next = document.createElement('button');
    prev.className = 'row-arrow prev'; prev.textContent = '‹'; prev.type = 'button';
    next.className = 'row-arrow next'; next.textContent = '›'; next.type = 'button';
    wrap.append(prev, next);

    // 한 번에 얼마나 넘길지 = 보이는 폭의 85% (거의 한 화면씩)
    function step() { return scroll.clientWidth * 0.85; }

    prev.addEventListener('click', function () {
        scroll.scrollBy({ left: -step(), behavior: 'smooth' });
    });
    next.addEventListener('click', function () {
        scroll.scrollBy({ left: step(), behavior: 'smooth' });
    });

    // 끝에 닿으면 그쪽 화살표는 숨긴다 (더 갈 곳 없으니).
    function updateArrows() {
        prev.hidden = scroll.scrollLeft <= 0;
        // scrollWidth(전체) - clientWidth(보이는) = 최대 스크롤 위치. -1은 반올림 여유.
        next.hidden = scroll.scrollLeft >= scroll.scrollWidth - scroll.clientWidth - 1;
    }
    scroll.addEventListener('scroll', updateArrows);
    updateArrows();   // 처음 상태

    // ③ 세로 휠 → 가로 스크롤 (데스크톱 마우스 편의)
    scroll.addEventListener('wheel', function (e) {
        // 이미 가로 입력(트랙패드)이면 놔둔다. 세로 휠일 때만 가로로 바꾼다.
        if (Math.abs(e.deltaY) > Math.abs(e.deltaX)) {
            scroll.scrollLeft += e.deltaY;
            e.preventDefault();   // 페이지가 세로로 스크롤되는 것 막기
        }
    }, { passive: false });
});


// ── 작품 둘러보기: 무한 스크롤 ───────────────────────────────
//   [문제] 첫 페이지(20~40개)만 서버가 그려준다. 더 보려면 계속 불러와야 한다.
//   [해결] 화면 맨 아래 '감지용 요소(sentinel)'가 보이면(스크롤 도달),
//     JS가 api/browse.php에서 다음 페이지를 받아와(fetch) 그리드에 이어붙인다.
//   ★ IntersectionObserver = '이 요소가 화면에 보이나?'를 감시하는 브라우저 기능.
//     스크롤 이벤트를 매번 계산하는 것보다 가볍고 정확하다. (무한스크롤 표준)

const grid     = document.querySelector('#browse-grid');
const sentinel = document.querySelector('#browse-sentinel');

if (grid && sentinel) {
    sentinel.style.display = 'block';   // JS가 있을 때만 감지용 요소를 보이게

    // 시작 페이지: 서버가 미리 그린 페이지 수. data-start-page="0"이면 JS가 1페이지부터 채운다.
    //   (지연 로딩 — 서버는 스켈레톤만 보내고 포스터는 여기서 받아온다)
    let page    = grid.dataset.startPage !== undefined ? Number(grid.dataset.startPage) : 1;
    let loading = false;   // 중복 요청 방지 (한 번에 하나만)
    let done    = false;   // 더 없으면 멈춤

    const genre = grid.dataset.genre || '';
    const media = grid.dataset.media || 'all';

    // 포스터 카드 하나를 만들어 그리드에 추가
    function addCard(item) {
        const a = document.createElement('a');
        a.className = 'row-card';
        a.href = '/board/?work=tmdb-' + item.tmdb_id;
        a.innerHTML =
            '<img class="row-poster" src="' + item.poster_url + '" alt="" loading="lazy">' +
            '<span class="row-title"></span>' +
            '<span class="post-stat"></span>';
        // 제목·연도는 textContent로 넣는다 (사용자 데이터를 HTML로 안 박아 XSS 방지)
        a.querySelector('.row-title').textContent = item.title;
        a.querySelector('.post-stat').textContent = item.year || '';
        grid.appendChild(a);
    }

    // 다음 페이지 불러오기
    async function loadNext() {
        if (loading || done) return;
        loading = true;
        page += 1;

        const url = '/api/browse.php?genre=' + encodeURIComponent(genre)
                  + '&media=' + encodeURIComponent(media) + '&page=' + page;
        try {
            const res  = await fetch(url);
            const data = await res.json();
            // 첫 응답이 오면 로딩 자리표시(스켈레톤)를 걷어낸다 (있을 때만)
            grid.querySelectorAll('.row-skeleton').forEach(function (s) { s.remove(); });
            if (!data.items || data.items.length === 0) {
                done = true;                       // 더 없음
                sentinel.textContent = '마지막까지 다 봤어요';
            } else {
                data.items.forEach(addCard);
            }
        } catch (e) {
            sentinel.textContent = '불러오기에 실패했어요';
        }
        loading = false;
    }

    // sentinel이 화면에 들어오면 다음 페이지 로드
    const observer = new IntersectionObserver(function (entries) {
        if (entries[0].isIntersecting) {
            loadNext();
        }
    }, { rootMargin: '400px' });   // 400px 미리 감지 → 끊김 없이 이어짐

    observer.observe(sentinel);
}

// ── 예고편 모달: '▶ 예고편 보기'를 누르면 유튜브를 dialog 안에서 재생 ──
//   버튼의 data-trailer(영상 키)를 읽어 유튜브 embed 주소를 만든다.
//   ★ src는 '열 때' 넣고 '닫을 때' 비운다 → 미리 로딩 안 함(빠름) + 닫으면 소리 정지.
const trailerModal = document.getElementById('trailer-modal');
if (trailerModal) {
    const iframe = document.getElementById('trailer-iframe');

    document.querySelectorAll('.btn-trailer').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const key = btn.dataset.trailer;
            // youtube-nocookie = 추적 쿠키 없는 유튜브 임베드. autoplay=1 자동재생, rel=0 관련영상 최소화.
            iframe.src = 'https://www.youtube-nocookie.com/embed/' + key + '?autoplay=1&rel=0';
            trailerModal.showModal();
        });
    });

    // 닫힐 때(닫기 버튼·ESC·바깥클릭 모두) src를 비워 재생을 멈춘다.
    trailerModal.addEventListener('close', function () {
        iframe.src = '';
    });
    // 모달 바깥(어두운 배경)을 클릭하면 닫기
    trailerModal.addEventListener('click', function (e) {
        if (e.target === trailerModal) {
            trailerModal.close();
        }
    });
}

// ── 홈: 무거운 TMDB 가로줄을 '화면이 뜬 뒤' 채운다 (초기 로딩을 빠르게) ──
//   서버는 빈 스켈레톤(.lazy-row)만 보냈고, 여기서 api/row.php로 실제 포스터를 받아 채운다.
//   ★ 이게 "일단 이동하고 동적으로 받아오기"의 핵심 — 홈 HTML은 우리 DB 것만 담아 즉시 뜬다.
document.querySelectorAll('.lazy-row').forEach(function (row) {
    const kind   = row.dataset.kind;                 // trending | movie | tv
    const scroll = row.querySelector('.row-scroll');

    fetch('/api/row.php?kind=' + encodeURIComponent(kind))
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (!data.items || data.items.length === 0) {
                row.remove();                        // 받아온 게 없으면 그 줄은 지운다
                return;
            }
            scroll.innerHTML = '';                    // 스켈레톤 치우고
            data.items.forEach(function (m) {         // 실제 카드로 채운다
                scroll.appendChild(buildRowCard(m));
            });
            // '오늘의 발견'은 인기작(trending) 데이터에서 하나 골라 채운다
            if (kind === 'trending') {
                fillDailyPick(data.items);
            }
        })
        .catch(function () { row.remove(); });        // 네트워크 실패 시 조용히 제거
});

// 가로줄 카드 하나 만들기 (media_row.php의 'sm' 카드와 같은 모양)
//   구조는 innerHTML로, 사용자 데이터(제목)는 textContent로 넣어 XSS 방지.
function buildRowCard(m) {
    const a = document.createElement('a');
    a.className = 'row-card';
    a.href = '/board/?work=tmdb-' + m.tmdb_id;
    a.innerHTML =
        '<img class="row-poster" src="' + encodeURI(m.poster_url) + '" alt="" loading="lazy">' +
        '<span class="row-title"></span>';
    a.querySelector('.row-title').textContent = m.title;
    return a;
}

// ── 설정: 프로필 이미지 '올리기 전에' 브라우저에서 줄인다 ──────
//   서버엔 이미지 처리 도구(GD)가 없어서, 브라우저 Canvas로 256px 정사각형
//   WebP로 압축해 보낸다. 2MB 원본이 ~20KB로 줄어 업로드가 빠르고 크기도 통일된다.
//   ★ 서버의 3중 검증(MIME·크기·파일명)은 그대로 → 이중 방어.
const AVATAR_SIZE = 256;                       // 저장할 정사각형 한 변(px)
const AVATAR_MAX_ORIGINAL = 15 * 1024 * 1024;  // 원본 상한(브라우저 메모리 보호) 15MB

const avatarInput = document.getElementById('avatar-input');
if (avatarInput) {
    const form = avatarInput.closest('form');

    avatarInput.addEventListener('change', async function () {
        const file = avatarInput.files[0];
        if (!file) {
            return;
        }
        // 사전검사 ①: 진짜 이미지 종류인가 (아니면 즉시 막고 안내)
        if (!file.type.startsWith('image/')) {
            alert('이미지 파일만 올릴 수 있어요 (JPG·PNG·GIF·WebP).');
            avatarInput.value = '';
            return;
        }
        // 사전검사 ②: 원본이 터무니없이 크면 거부 (줄이기 전에 메모리 폭발 방지)
        if (file.size > AVATAR_MAX_ORIGINAL) {
            alert('사진이 너무 큽니다. 15MB 이하로 올려주세요.');
            avatarInput.value = '';
            return;
        }

        try {
            const blob = await resizeImageToSquare(file, AVATAR_SIZE);
            // 줄인 이미지(WebP)로 input의 파일을 교체해서 제출한다.
            //   DataTransfer = input.files를 프로그램으로 바꿔 끼우는 표준 방법.
            const dt = new DataTransfer();
            dt.items.add(new File([blob], 'avatar.webp', { type: 'image/webp' }));
            avatarInput.files = dt.files;
        } catch (e) {
            // 줄이기에 실패하면 원본 그대로 보낸다 (서버가 2MB·MIME 재검사하니 안전)
        }
        form.submit();
    });
}

// 이미지를 '정사각형 size×size'로 중앙 크롭해서 WebP Blob으로 만든다.
//   cover 방식: 짧은 변을 꽉 채우고 넘치는 부분은 잘라내 정사각형을 만든다.
function resizeImageToSquare(file, size) {
    return new Promise(function (resolve, reject) {
        const img = new Image();
        const url = URL.createObjectURL(file);
        img.onload = function () {
            URL.revokeObjectURL(url);                 // 다 썼으니 메모리 해제
            const canvas = document.createElement('canvas');
            canvas.width = size;
            canvas.height = size;
            const ctx = canvas.getContext('2d');
            // 짧은 변 기준으로 확대비를 잡아 정사각형을 꽉 채우고, 넘침은 중앙 크롭
            const scale = Math.max(size / img.width, size / img.height);
            const w = img.width * scale;
            const h = img.height * scale;
            ctx.drawImage(img, (size - w) / 2, (size - h) / 2, w, h);
            // WebP로 압축(품질 0.85). WebP 미지원 옛 브라우저면 PNG로 떨어진다.
            canvas.toBlob(function (blob) {
                blob ? resolve(blob) : reject(new Error('toBlob 실패'));
            }, 'image/webp', 0.85);
        };
        img.onerror = function () {
            URL.revokeObjectURL(url);
            reject(new Error('이미지 로드 실패'));
        };
        img.src = url;
    });
}

// '오늘의 발견' 사이드 카드 채우기 — 인기작 중 하나를 무작위로
function fillDailyPick(items) {
    const box = document.getElementById('daily-pick-box');
    const link = document.getElementById('daily-pick');
    if (!box || !link || items.length === 0) {
        return;
    }
    const m = items[Math.floor(Math.random() * items.length)];   // 새로고침마다 바뀜(발견의 재미)
    link.href = '/board/?work=tmdb-' + m.tmdb_id;
    link.querySelector('img').src = m.poster_url;
    link.querySelector('.side-pick-title').textContent = m.title;
    box.hidden = false;                              // 다 채워졌으니 이제 보여준다
}


// ── ⏱ 자동 로그아웃 카운트다운 (상단바) ─────────────────────
//   서버가 내려준 '남은 초'를 1초씩 깎아 보여준다.
//   ★ 이 숫자가 로그아웃을 시키는 게 아니다 — 판정은 서버가 한다.
//     0이 되면 새로고침해서 서버의 판단을 받는다(그 요청에서 로그아웃 + 안내가 뜬다).
//   ★ 페이지를 새로 열 때마다 서버가 last_seen을 갱신하므로 카운트다운도 다시 시작된다.
//     즉 '이 화면에 가만히 머문 시간'이 표시되는 셈이다.
const idleTimer = document.getElementById('idle-timer');

if (idleTimer) {
    let left = parseInt(idleTimer.dataset.left, 10) || 0;

    // 남은 초를 mm:ss 로. padStart = 한 자리 수 앞에 0을 채워 '9:5'가 아니라 '09:05'로.
    function renderIdle() {
        const m = Math.floor(left / 60);
        const s = left % 60;
        idleTimer.textContent = '⏱ ' + String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0');

        // 2분 이하로 남으면 색을 바꿔 눈에 띄게 한다.
        idleTimer.classList.toggle('idle-warn', left <= 120);
    }

    renderIdle();

    const tick = setInterval(function () {
        left -= 1;
        if (left <= 0) {
            clearInterval(tick);
            location.reload();      // 서버에게 판단을 맡긴다
            return;
        }
        renderIdle();
    }, 1000);
}
