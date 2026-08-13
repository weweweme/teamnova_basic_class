<?php
// ============================================================
// prefs.php — '취향' 쿠키
//
//   [무엇을 담나]
//     세션에 담은 것들(로그인·알림·최근 본 글·조회수)은 전부 **틀리면 손해 보는 값**이었다.
//     여기 담는 건 반대다 — 틀려도 사용자가 다시 한 번 누르면 그만인 값:
//       · 게시판 정렬 기본값 (최신/인기/조회/댓글 중 뭘 즐겨 보나)
//       · 최근 검색어
//
//   [★ 왜 이건 쿠키가 맞나]
//     ① 브라우저를 닫아도 남아야 한다. 세션은 닫으면 사라지므로 애초에 못 한다.
//     ② 로그인 안 한 사람도 써야 한다. DB에 넣으면 회원만 쓰는 기능이 된다.
//     ③ 조작당해도 손해가 없다. 남이 내 정렬 기본값을 바꿔봐야 "탭이 하나 다르게 열린다"뿐이다.
//     세 조건이 다 맞을 때가 쿠키 자리다.
//
//   [★★ 쿠키를 읽을 때의 철칙]
//     쿠키는 사용자 PC에 있다 = **사용자가 마음대로 고칠 수 있다.**
//     그래서 읽은 값을 절대 그대로 믿지 않는다 — 우리가 아는 값인지 확인하고 쓴다.
//     세션과 결정적으로 다른 점이고, week15의 ?as= 가 뚫린 이유와 정확히 같은 이야기다.
//     (여기서는 '허용 목록에 있는 값인가' · '글자 수가 정상인가'를 확인한다)
// ============================================================

require_once __DIR__ . '/util.php';    // e() · get_str
require_once __DIR__ . '/posts.php';   // SEARCH_QUERY_MAX — 검색어 길이 제한은 검색 기능이 정한 값이라
                                       //   여기서 따로 정하지 않고 그쪽을 따른다 (숫자가 둘로 갈라지지 않게)

// 취향은 오래 기억해도 손해가 없으므로 넉넉히 잡는다.
const PREF_DAYS = 90;

// 쿠키 이름들
const PREF_SORT_COOKIE     = 'pref_sort';     // 게시판 정렬 기본값
const RECENT_SEARCH_COOKIE = 'recent_search'; // 최근 검색어 (JSON 배열)
const RECENT_POSTS_COOKIE  = 'recent_posts';  // 최근 본 글 번호 ("3,2,1")
const PREF_SENTIMENT_COOKIE = 'pref_sentiment'; // 게시판 감상 필터 기본값 ('' | 호평 | 보통 | 혹평)
const LAST_VISIT_COOKIE     = 'last_visit';     // 마지막으로 게시판을 본 시각(초)
const COOKIE_NOTICE_COOKIE  = 'cookie_notice';  // 쿠키 안내를 읽었는지 (JS가 심는 유일한 쿠키)
const RECENT_WORKS_COOKIE   = 'recent_works';   // 최근 본 작품 slug ("tmdb-496243,tmdb-27205")
const PER_PAGE_COOKIE       = 'per_page';       // 게시판 한 페이지 글 수 (15 | 30 | 50)

// 최근 검색어를 몇 개까지 기억할지
const RECENT_SEARCH_MAX = 5;

// 최근 본 글을 몇 개까지 기억할지 (홈 사이드바에 보여줄 만큼만)
const RECENT_POSTS_MAX = 5;

// 취향 쿠키에 공통으로 붙일 옵션.
//   ★ httponly를 켜지 '않는' 유일한 자리다 — 나중에 JS가 읽어 쓸 수도 있는 값이고,
//     훔쳐가 봐야 '이 사람은 조회순을 좋아한다' 정도라 가릴 이유가 없기 때문이다.
//     (로그인 토큰은 반대다 — remember.php에서는 반드시 httponly를 켠다)
function pref_cookie_options(): array {
    return [
        'expires'  => time() + PREF_DAYS * 86400,
        'path'     => '/',
        'samesite' => 'Lax',
        'secure'   => !empty($_SERVER['HTTPS']),
    ];
}

// ── 게시판 정렬 기본값 ───────────────────────────────────────

// 방금 고른 정렬을 기억한다. (게시판이 ?sort= 를 받았을 때 호출)
function remember_sort(string $sort): void {
    setcookie(PREF_SORT_COOKIE, $sort, pref_cookie_options());
}

// 기억해 둔 정렬을 꺼낸다. 없거나 이상한 값이면 $default.
//   ★★ $allowed(허용 목록)를 반드시 받는다. 쿠키 값을 그대로 쓰면
//     ?sort= 자리에 아무 문자열이나 흘러들어가고, 그 값이 화면·쿼리로 퍼진다.
//     "쿠키는 입력이다" — 주소로 들어온 값과 똑같이 취급해야 한다.
function preferred_sort(array $allowed, string $default): string {
    $saved = $_COOKIE[PREF_SORT_COOKIE] ?? '';
    if (!is_string($saved) || !in_array($saved, $allowed, true)) {
        return $default;
    }
    return $saved;
}

// ── 게시판 감상 필터 기본값 ──────────────────────────────────
//   정렬(pref_sort)과 완전히 같은 자리다 — "나는 혹평만 골라 본다"도 취향이기 때문.
//   ★ 그래서 새 규칙을 만들지 않고 같은 함수 모양을 그대로 따른다.
//     같은 성격의 값이 서로 다른 방식으로 저장되면, 나중에 한쪽만 고치는 실수가 난다.

// 고른 감상 필터를 기억한다. ('전체'도 기억한다 — 빈 문자열이 곧 '전체'라는 선택이다)
function remember_sentiment(string $sentiment): void {
    setcookie(PREF_SENTIMENT_COOKIE, $sentiment, pref_cookie_options());
}

// 기억해 둔 감상 필터를 꺼낸다. 허용 목록에 없으면 $default.
//   ★★ 쿠키에서 읽은 값은 주소로 들어온 값과 똑같이 취급한다 — 반드시 허용 목록과 대조한다.
//     그냥 쓰면 ?sentiment= 자리에 아무 문자열이나 흘러들어가고, 그 값이 화면·쿼리로 퍼진다.
function preferred_sentiment(array $allowed, string $default): string {
    $saved = $_COOKIE[PREF_SENTIMENT_COOKIE] ?? '';
    if (!is_string($saved) || !in_array($saved, $allowed, true)) {
        return $default;
    }
    return $saved;
}


// ── 최근 검색어 ──────────────────────────────────────────────

// 방금 검색한 말을 목록 맨 앞에 넣는다.
//   '최근 본 글'(세션)과 규칙이 같다 — 있으면 빼고, 앞에 넣고, 넘치면 자른다.
function remember_search(string $query): void {
    $recent = get_recent_searches();

    // 같은 말을 또 검색하면 줄이 늘지 않고 맨 앞으로 올라온다.
    $recent = array_values(array_filter($recent, fn($old) => $old !== $query));
    array_unshift($recent, $query);
    $recent = array_slice($recent, 0, RECENT_SEARCH_MAX);

    // 여러 개를 한 칸에 담아야 하므로 JSON 한 줄로 바꾼다.
    //   JSON_UNESCAPED_UNICODE = 한글을 \uXXXX로 바꾸지 않는다(쿠키가 쓸데없이 길어지지 않게).
    setcookie(RECENT_SEARCH_COOKIE, json_encode($recent, JSON_UNESCAPED_UNICODE), pref_cookie_options());
}

// 최근 검색어 목록. 쿠키가 없거나 이상하면 빈 배열.
//   ★★ 여기가 이 파일에서 제일 조심해야 하는 곳이다.
//     쿠키 값은 사용자가 통째로 바꿔 넣을 수 있다 → JSON이 아닐 수도, 배열이 아닐 수도,
//     안에 배열이나 숫자가 들어 있을 수도, 글자가 10만 자일 수도 있다.
//     그대로 화면에 뿌리면 깨지거나(치명적 오류) 이상한 게 섞여 들어간다.
//     → '문자열인 것만' '길이를 잘라서' '개수를 제한해' 받는다.
function get_recent_searches(): array {
    $raw = $_COOKIE[RECENT_SEARCH_COOKIE] ?? '';
    if (!is_string($raw) || $raw === '') {
        return [];
    }

    // true = 객체가 아니라 배열로 받는다. 깨진 JSON이면 null이 온다.
    $list = json_decode($raw, true);
    if (!is_array($list)) {
        return [];
    }

    $result = [];
    foreach ($list as $item) {
        if (!is_string($item) || $item === '') {
            continue;                                   // 문자열이 아닌 건 버린다
        }
        $result[] = mb_substr($item, 0, SEARCH_QUERY_MAX);   // 검색창과 같은 길이 제한
        if (count($result) >= RECENT_SEARCH_MAX) {
            break;                                      // 개수도 우리가 정한 만큼만
        }
    }
    return $result;
}


// ── 최근 본 글 ───────────────────────────────────────────────
//   [★ 왜 세션이 아니라 쿠키인가 — 판단을 한 번 바꾼 자리다]
//     처음에는 세션에 담았다. 그런데 우리가 세운 3조건에 비춰보니 셋 다 쿠키였다:
//       ① 브라우저를 닫아도 남아야 하나 → 그렇다. 세션에 두면 창을 껐다 켤 때마다 목록이 빈다
//       ② 로그인 안 한 사람도 쓰나       → 그렇다
//       ③ 조작당해도 손해가 없나         → 그렇다. 남의 글 번호를 넣어도 자기 화면에 그 글이 뜰 뿐
//     크기도 문제가 안 된다 — 번호 다섯 개면 "3,2,1,9,12" 남짓이라 쿠키 4KB에 한참 못 미친다.
//
//   [담는 모양]
//     최근 검색어는 한글이 섞여 JSON으로 담지만, 여기는 숫자뿐이라 콤마로 잇는다.
//     week15에 주소로 나르려다 포기했던 그 모양(?recent=1,5,9)과 같다 —
//     다만 이번엔 주소가 아니라 브라우저가 들고 다닌다.
//
//   [★★ 쿠키에서 읽은 값은 주소로 들어온 값과 똑같이 검증한다]
//     정수인지 · 양수인지 · 개수는 몇 개인지 전부 확인한다. (get_recent_post_ids)
//     '글이 실제로 있는지'는 여기서 안 본다 — 그건 글을 꺼내는 쪽(posts.php)이 하고,
//     지워진 글은 자연히 목록에서 빠진다.

// 이 글을 '방금 봤다'고 기록한다. (post/view.php가 부른다)
function remember_recent_post(int $id): void {
    if ($id <= 0) {
        return;
    }

    $recent = get_recent_post_ids();

    // 이미 있으면 빼고 맨 앞에 다시 넣는다 → 같은 글이 여러 줄로 쌓이지 않고 '가장 최근'이 된다.
    $recent = array_values(array_filter($recent, fn($seen) => $seen !== $id));
    array_unshift($recent, $id);
    $recent = array_slice($recent, 0, RECENT_POSTS_MAX);

    setcookie(RECENT_POSTS_COOKIE, implode(',', $recent), pref_cookie_options());
}

// 최근 본 글 번호 목록. 이상한 값은 전부 걸러낸다.
function get_recent_post_ids(): array {
    $raw = $_COOKIE[RECENT_POSTS_COOKIE] ?? '';
    if (!is_string($raw) || $raw === '') {
        return [];
    }

    $result = [];
    foreach (explode(',', $raw) as $piece) {
        // ctype_digit = '숫자로만 이뤄진 글자인가'. (int) 로 먼저 바꾸면 "3abc"가 3이 되어 통과한다.
        if (!ctype_digit($piece)) {
            continue;
        }
        $id = (int) $piece;
        if ($id <= 0 || in_array($id, $result, true)) {
            continue;                       // 0·중복은 버린다
        }
        $result[] = $id;
        if (count($result) >= RECENT_POSTS_MAX) {
            break;                          // 개수도 우리가 정한 만큼만
        }
    }
    return $result;
}


// ── 마지막 방문 시각 → 게시판의 🆕 배지 ──────────────────────
//   [무엇을 하나]
//     "지난번에 왔다 간 뒤로 올라온 글"에 🆕 를 붙인다. 커뮤니티에서 흔히 보는 그것.
//
//   [★ 지금까지의 쿠키와 다른 점]
//     정렬·감상·최근 검색어는 **사용자가 고른 값**이었다. 이건 **시각**이다 —
//     사용자가 고르는 게 아니라 우리가 자동으로 적어둔다.
//     그래도 쿠키가 맞는 자리다: ①브라우저를 닫아도 남아야 하고 ②비로그인도 쓰고
//     ③조작당해도 배지가 잘못 뜰 뿐이다.
//
//   [왜 '볼 때마다' 갱신하지 않나]
//     열자마자 갱신하면 새로고침 한 번에 배지가 전부 사라진다. 방금 뭐가 새 글이었는지
//     확인할 틈이 없다. 그래서 **VISIT_GAP(30분) 이상 지났을 때만** 갱신한다
//     → 한 번 둘러보는 동안에는 배지가 그대로 있고, 다음에 다시 올 때 새로 계산된다.
const VISIT_GAP = 1800;      // 30분. 이보다 짧은 간격의 재방문은 '같은 방문'으로 친다.

// 지난번에 왔던 시각. 없거나 이상하면 0(= 배지를 안 붙인다).
function last_visit_at(): int {
    $raw = $_COOKIE[LAST_VISIT_COOKIE] ?? '';

    // ★ 쿠키는 사용자가 고칠 수 있다 — 숫자인지부터 확인한다.
    //   ctype_digit을 쓰는 이유: (int)로 먼저 바꾸면 "17abc"가 17로 통과한다.
    if (!is_string($raw) || !ctype_digit($raw)) {
        return 0;
    }

    $at = (int) $raw;

    // 미래 시각이 적혀 있으면(시계를 앞당겨 놓았거나 조작) 믿지 않는다.
    //   그대로 쓰면 '모든 글이 옛날 글'이 되어 배지가 영영 안 뜬다.
    return $at > time() ? 0 : $at;
}

// 방문 시각을 갱신한다. (게시판을 그린 뒤에 부른다)
function touch_visit(): void {
    if (time() - last_visit_at() < VISIT_GAP) {
        return;                       // 아직 '같은 방문' — 그대로 두어 배지를 유지한다
    }
    setcookie(LAST_VISIT_COOKIE, (string) time(), pref_cookie_options());
}


// ── 쿠키 안내 배너 ───────────────────────────────────────────
//   [무엇인가]
//     "이 사이트는 쿠키를 씁니다"를 한 번 알리고, 확인을 누르면 다시 안 띄운다.
//     유럽 GDPR 이후 거의 모든 사이트에 붙은 그 배너다.
//
//   [★ 이 프로젝트에서 이게 재밌는 이유]
//     **"쿠키를 쓰겠다는 안내를 읽었다"는 사실 자체를 쿠키에 적는다.**
//     달리 적을 데가 없다 — 로그인 안 한 사람에게도 기억해야 하고, 창을 닫아도 남아야 하니까.
//
//   [★★ 지금까지의 쿠키와 결정적으로 다른 점]
//     다른 쿠키는 전부 **서버가 setcookie()로 심는다.** 이건 **브라우저(JS)가 직접 심는다.**
//     그래서 화면이 즉시 사라지고 서버를 한 번도 안 거친다.
//     · 가능한 이유: 우리 취향 쿠키들은 `httponly`를 켜지 않기 때문이다(JS가 읽고 쓸 수 있다).
//     · 로그인 토큰(remember)·세션 번호표는 정반대다 — `httponly`를 켜서 JS가 아예 못 만진다.
//       훔쳐가면 계정이 넘어가는 값이기 때문. **값의 무게에 따라 다루는 방식이 갈린다.**
function has_seen_cookie_notice(): bool {
    return isset($_COOKIE[COOKIE_NOTICE_COOKIE]);
}


// ── 최근 본 작품 ─────────────────────────────────────────────
//   최근 본 글(recent_posts)과 같은 자리·같은 방식이다. 담는 것만 '글 번호'에서 '작품 slug'로 바뀐다.
//   ★ 검증이 달라지는 지점: 글은 숫자라 ctype_digit이면 끝인데, slug는 글자다.
//     그래서 **모양을 정해두고 그 모양만 통과**시킨다 (영문 소문자·숫자·하이픈).
//     쿠키에서 온 글자를 그대로 화면·쿼리에 흘리면 안 되기 때문이다.

// 이 작품을 '방금 봤다'고 기록한다. (작품 게시판을 열 때 부른다)
function remember_recent_work(string $slug): void {
    if ($slug === '' || !is_valid_work_slug($slug)) {
        return;
    }

    $recent = get_recent_work_slugs();
    $recent = array_values(array_filter($recent, fn($seen) => $seen !== $slug));
    array_unshift($recent, $slug);
    $recent = array_slice($recent, 0, RECENT_POSTS_MAX);   // 개수 기준은 최근 본 글과 같게

    setcookie(RECENT_WORKS_COOKIE, implode(',', $recent), pref_cookie_options());
}

// 최근 본 작품 slug 목록. 이상한 값은 전부 걸러낸다.
function get_recent_work_slugs(): array {
    $raw = $_COOKIE[RECENT_WORKS_COOKIE] ?? '';
    if (!is_string($raw) || $raw === '') {
        return [];
    }

    $result = [];
    foreach (explode(',', $raw) as $piece) {
        if (!is_valid_work_slug($piece) || in_array($piece, $result, true)) {
            continue;
        }
        $result[] = $piece;
        if (count($result) >= RECENT_POSTS_MAX) {
            break;
        }
    }
    return $result;
}

// slug 모양 검사 — 영문 소문자·숫자·하이픈만, 최대 50글자.
//   ★ '있는 작품인가'는 여기서 안 본다. 그건 작품을 꺼내는 쪽(works.php)이 판단하고,
//     없는 작품은 목록에서 자연히 빠진다. 여기서는 **모양만** 본다.
function is_valid_work_slug(string $slug): bool {
    return $slug !== '' && preg_match('/^[a-z0-9-]{1,50}$/', $slug) === 1;
}


// ── 게시판 한 페이지 글 수 ───────────────────────────────────
//   정렬·감상 필터와 완전히 같은 자리다 — "나는 한 화면에 많이 보는 게 편하다"도 취향이므로.
//   ★ 숫자라는 점만 다르다. 그래도 **허용 목록 대조**는 똑같이 한다 —
//     숫자라고 안심하고 그대로 쓰면 ?per=100000 으로 서버를 갈아버릴 수 있다.
//     "숫자니까 안전하다"가 아니라 "**우리가 정한 값인가**"가 기준이다.

// 고른 값을 기억한다.
function remember_per_page(int $perPage): void {
    setcookie(PER_PAGE_COOKIE, (string) $perPage, pref_cookie_options());
}

// 기억해 둔 값을 꺼낸다. 허용 목록에 없으면 $default.
function preferred_per_page(array $allowed, int $default): int {
    $raw = $_COOKIE[PER_PAGE_COOKIE] ?? '';
    if (!is_string($raw) || !ctype_digit($raw)) {
        return $default;
    }
    $value = (int) $raw;
    return in_array($value, $allowed, true) ? $value : $default;
}
