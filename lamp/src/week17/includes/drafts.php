<?php
// ============================================================
// drafts.php — 글쓰기 임시저장(초안)
//
//   [무엇을 고치는가]
//     길게 쓰던 리뷰가 날아가는 경우는 넷이다:
//       ① 서버가 거절했을 때 (제목이 너무 길다 등)
//       ② 실수로 새로고침했을 때
//       ③ 다른 페이지 눌렀다가 돌아왔을 때
//       ④ 브라우저를 닫았다 다시 열었을 때
//     ①②③만 막는 건 반쪽이다. ④까지 막으려면 **브라우저 밖**에 적어둬야 한다.
//
//   [★ 어디에 담나 — 세션에서 DB로 옮겼다]
//     처음엔 세션에 담았다. 그런데 세션은 **창을 닫으면 사라진다.** 그게 세션의 성질이라
//     고칠 수가 없는데, 임시저장은 창을 닫아도 살아 있어야 쓸모가 있다.
//     그릇의 성질과 기능의 성격이 어긋나 있었다.
//
//     그릇을 고를 때 던지는 질문은 하나다 — **"이 값의 주인이 누구인가."**
//       · 최근 본 글  → 주인이 **브라우저** (로그인 안 해도 쌓인다)  → 쿠키
//       · 로그인 신원 → 주인이 **이 방문**                          → 세션
//       · 글 초안     → 주인이 **회원**   (기기가 바뀌어도 내 것)    → DB  ← 여기
//     글쓰기는 로그인 필수라 초안의 주인은 언제나 회원이다.
//
//   [옮겨서 얻은 것]
//     · 창을 닫아도, 30분이 지나도 남는다     · 다른 기기에서 이어 쓸 수 있다
//     · 세션이 가벼워졌다 — 세션은 요청마다 통째로 오가는데 본문(최대 5,000자)이 빠졌다
//     · 그래서 개수 제한도 풀렸다 (세션일 때는 1개만 들고 있어야 했다)
//
//   [★★ 그런데 바꾼 건 이 파일 속뿐이다]
//     save_draft() · get_draft() · forget_draft() — 이름도 인자도 그대로 두었다.
//     그래서 부르는 쪽(write.php · create.php · api/draft.php)은 **한 글자도 안 고쳤다.**
//     세션 저장소를 파일에서 DB로 바꿀 때와 똑같다 — 쓰는 쪽은 그대로, 속만 갈아 끼운다.
// ============================================================

require_once __DIR__ . '/db.php';
require_once __DIR__ . '/auth.php';    // 초안의 주인 = 지금 로그인한 회원
require_once __DIR__ . '/posts.php';   // 글자 수 상한(POST_TITLE_MAX·POST_CONTENT_MAX)

// 한 사람이 초안을 몇 개까지 들고 있을지.
//   ★ 세션일 때는 1개였다 — 요청마다 통째로 읽고 써서 무거웠기 때문.
//     표는 필요할 때만 읽으므로 넉넉히 둘 수 있다. 작품별로 하나씩이라 5개면 충분하다.
const DRAFT_MAX = 5;

// 손대지 않은 초안을 며칠까지 보관할지. (휴지통과 같은 방식의 자동 정리)
const DRAFT_KEEP_DAYS = 30;

// 초안 저장. 같은 작품이면 덮어쓴다.
function save_draft(string $work, array $values): void {
    $userId = current_user_id();
    if ($userId === 0 || $work === '') {
        return;                       // 로그인 안 했거나 작품이 없으면 담을 곳이 없다
    }

    // 글자 수 상한을 서버에서 다시 건다.
    //   ★ 폼의 maxlength는 개발자도구로 지울 수 있다. 그대로 믿으면 누군가 100만 자를 보내
    //     칼럼이 넘치고 저장이 통째로 실패한다.
    //   ★ 깨진 글자도 걸러낸다 — DB는 문자셋을 검사하므로, UTF-8이 아닌 바이트가 섞이면
    //     저장이 실패한다. (파일이던 시절엔 아무 바이트나 그냥 적혔다)
    $title   = mb_substr(mb_convert_encoding((string) ($values['title']   ?? ''), 'UTF-8', 'UTF-8'), 0, POST_TITLE_MAX);
    $content = mb_substr(mb_convert_encoding((string) ($values['content'] ?? ''), 'UTF-8', 'UTF-8'), 0, POST_CONTENT_MAX);

    // 감상은 우리가 아는 셋만 (화이트리스트).
    $sentiment = (string) ($values['sentiment'] ?? '');
    if (!in_array($sentiment, ['호평', '보통', '혹평'], true)) {
        $sentiment = '';
    }

    // 같은 (회원, 작품)이면 새 줄을 만들지 않고 덮어쓴다.
    //   ★ '있나 확인 → 넣기'를 따로 하면 그 사이에 다른 요청이 끼어들 수 있다.
    //     ON DUPLICATE KEY UPDATE는 DB가 한 번에 판단하므로 그런 틈이 없다.
    $sql = 'INSERT INTO drafts (user_id, work_slug, title, content, sentiment, updated_at)
            VALUES (?, ?, ?, ?, ?, NOW())
            ON DUPLICATE KEY UPDATE
                title = VALUES(title), content = VALUES(content),
                sentiment = VALUES(sentiment), updated_at = NOW()';
    db()->prepare($sql)->execute([$userId, $work, $title, $content, $sentiment]);

    trim_drafts($userId);
}

// 그 작품의 초안을 꺼낸다. 없으면 빈 배열.
//   ★ 꺼내면서 지우지 않는다(read-once가 아니다). 초안은 '쓰는 동안 계속' 살아 있어야 한다.
//     지우는 시점은 딱 하나 — 글이 실제로 등록됐을 때(forget_draft).
function get_draft(string $work): array {
    $userId = current_user_id();
    if ($userId === 0 || $work === '') {
        return [];
    }

    $stmt = db()->prepare(
        'SELECT title, content, sentiment FROM drafts WHERE user_id = ? AND work_slug = ?'
    );
    $stmt->execute([$userId, $work]);
    $row = $stmt->fetch();
    return $row !== false ? $row : [];
}

// 그 작품의 초안을 버린다. (글 등록에 성공했을 때)
//   ★ 안 버리면 방금 올린 글이 다음 글쓰기 화면에 그대로 되살아난다.
function forget_draft(string $work): void {
    $userId = current_user_id();
    if ($userId === 0 || $work === '') {
        return;
    }
    db()->prepare('DELETE FROM drafts WHERE user_id = ? AND work_slug = ?')
        ->execute([$userId, $work]);
}

// 넘치거나 오래된 초안을 정리한다. (저장할 때마다 함께 돈다)
//   ★ 따로 청소 작업을 돌리지 않고 저장하는 김에 치운다 — 휴지통이 열릴 때
//     기간 지난 글을 지우는 것과 같은 방식(lazy purge).
function trim_drafts(int $userId): void {
    // ① 오래 손대지 않은 것부터 버린다.
    db()->prepare('DELETE FROM drafts
                    WHERE user_id = ?
                      AND updated_at < NOW() - INTERVAL ' . DRAFT_KEEP_DAYS . ' DAY')
        ->execute([$userId]);

    // ② 그러고도 넘치면 오래된 것부터 버린다.
    //   ★ LIMIT은 DELETE에 직접 못 쓰는 경우가 있어, 남길 것을 먼저 고르고 그 밖을 지운다.
    $stmt = db()->prepare(
        'SELECT work_slug FROM drafts WHERE user_id = ? ORDER BY updated_at DESC LIMIT ' . DRAFT_MAX
    );
    $stmt->execute([$userId]);
    $keep = $stmt->fetchAll(PDO::FETCH_COLUMN);

    if (count($keep) < DRAFT_MAX) {
        return;                       // 아직 안 넘쳤다
    }

    // 남길 목록에 없는 것들을 지운다.
    $marks = implode(',', array_fill(0, count($keep), '?'));
    db()->prepare("DELETE FROM drafts WHERE user_id = ? AND work_slug NOT IN ($marks)")
        ->execute(array_merge([$userId], $keep));
}
