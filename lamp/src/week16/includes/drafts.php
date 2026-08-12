<?php
// ============================================================
// drafts.php — 글쓰기 임시저장(초안)
//
//   [무엇을 고치는가]
//     길게 쓰던 리뷰가 날아가는 경우는 셋이다:
//       ① 서버가 거절했을 때 (제목이 너무 길다 등)   ← util.php의 old() 로도 막을 수 있다
//       ② 실수로 새로고침했을 때
//       ③ 다른 페이지 눌렀다가 돌아왔을 때
//     ①만 막는 건 반쪽이다. 셋 다 막으려면 **쓰는 동안 계속 저장**해야 한다.
//
//   [왜 세션인가]
//     본문은 주소에도 쿠키에도 못 싣는다 — 길이 제한(쿠키 4KB)에 걸리고,
//     주소에 실으면 브라우저 기록·서버 로그에 글이 통째로 남는다.
//     서버가 들고 있어야 하고, 그게 세션이다.
//
//   [★ 한계도 세션의 성질에서 그대로 나온다]
//     **창을 닫으면 사라진다.** 세션이 그런 물건이기 때문이다(30분 만료도 마찬가지).
//     "브라우저가 꺼져도 복구"까지 하려면 세션이 아니라 **DB 표**에 둬야 한다.
//     → 지금 목표는 '방문 중에는 절대 안 잃는다'까지다. 할 수 있는 것과 없는 것을 구분한다.
//
//   [왜 작품별로 나눠 담나]
//     초안을 하나만 두면, 기생충 글을 쓰다 만 상태에서 다른 작품 글쓰기를 열었을 때
//     엉뚱한 내용이 채워진다. 작품 slug를 열쇠로 나눠 담으면 그 문제가 없다.
// ============================================================

// 초안을 담는 세션 열쇠. 안쪽은 [작품slug => ['title'=>…, 'content'=>…, 'sentiment'=>…]]
const SESSION_DRAFTS = 'drafts';

// 초안을 몇 개까지 들고 있을지.
//   ★ 제한을 두는 이유: 세션은 요청마다 통째로 읽고 쓴다. 초안이 무한정 쌓이면
//     글과 상관없는 모든 페이지가 그만큼 무거워진다. (본문은 최대 5,000자다)
const DRAFT_MAX = 3;

// 초안 저장. 같은 작품이면 덮어쓴다.
//   ★ 깨진 글자를 걸러내는 이유는 keep_old_input()과 같다 —
//     사용자가 친 값이 세션(=DB의 payload)으로 들어가는 자리이기 때문.
function save_draft(string $work, array $values): void {
    if ($work === '') {
        return;
    }

    $drafts = $_SESSION[SESSION_DRAFTS] ?? [];

    $drafts[$work] = [
        'title'     => mb_convert_encoding((string) ($values['title']   ?? ''), 'UTF-8', 'UTF-8'),
        'content'   => mb_convert_encoding((string) ($values['content'] ?? ''), 'UTF-8', 'UTF-8'),
        'sentiment' => (string) ($values['sentiment'] ?? ''),
    ];

    // 넘치면 오래된 것부터 버린다. PHP 배열은 넣은 순서를 기억하므로 앞쪽이 오래된 것이다.
    //   ★ 방금 넣은 것은 맨 뒤라 잘리지 않는다.
    if (count($drafts) > DRAFT_MAX) {
        $drafts = array_slice($drafts, -DRAFT_MAX, null, true);
    }

    $_SESSION[SESSION_DRAFTS] = $drafts;
}

// 그 작품의 초안을 꺼낸다. 없으면 빈 배열.
//   ★ old()와 달리 꺼내면서 지우지 않는다(read-once가 아니다).
//     초안은 '직전 제출 한 번'이 아니라 '쓰는 동안 계속' 살아 있어야 하는 값이기 때문.
//     지우는 시점은 딱 하나 — 글이 실제로 등록됐을 때(forget_draft).
function get_draft(string $work): array {
    $draft = $_SESSION[SESSION_DRAFTS][$work] ?? [];
    return is_array($draft) ? $draft : [];
}

// 그 작품의 초안을 버린다. (글 등록에 성공했을 때)
//   ★ 안 버리면 방금 올린 글이 다음 글쓰기 화면에 그대로 되살아난다.
function forget_draft(string $work): void {
    unset($_SESSION[SESSION_DRAFTS][$work]);

    // 마지막 초안이었으면 열쇠 자체를 없앤다.
    //   ★ 안 그러면 세션에 빈 껍데기(drafts|a:0:{})가 남아 요청마다 같이 읽고 쓰인다.
    //     동작에는 지장이 없지만, 안 쓰는 값은 남기지 않는 편이 낫다.
    if (empty($_SESSION[SESSION_DRAFTS])) {
        unset($_SESSION[SESSION_DRAFTS]);
    }
}
