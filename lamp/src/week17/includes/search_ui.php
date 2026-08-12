<?php
// ============================================================
// search_ui.php — 검색 화면 네 개가 함께 쓰는 조각 모음
//   통합검색(/search/) · 작품 · 글 · 유저 전용 페이지가
//   같은 검색창·탭·결과 줄을 쓴다. 한 곳에 모아 두면 모양이 어긋나지 않고,
//   결과 줄을 고칠 일이 생겼을 때 네 파일을 뒤지지 않아도 된다.
// ============================================================

require_once __DIR__ . '/posts.php';   // 검색어 길이 제한(SEARCH_QUERY_MAX)

// 카테고리 전용 페이지에서 한 페이지에 보여줄 결과 수
const RESULTS_PER_PAGE = 20;

// 탭 목록: 키 => [화면에 보일 이름, 그 화면의 주소]
//   ★ 키는 '지금 어느 탭인가'를 가리키는 이름표일 뿐 주소에 실리지 않는다.
//     탭마다 주소(파일)가 따로 있어서, 어느 결과를 보는 중인지가 주소에 그대로 드러난다.
const SEARCH_TABS = [
    'all'   => ['통합',    '/search/'],
    'works' => ['🎬 작품', '/search/works.php'],
    'posts' => ['📝 글',   '/search/posts.php'],
    'users' => ['👤 유저', '/search/users.php'],
];

// ── 검색창 + 탭 줄 ──────────────────────────────────────────
//   $activeTab = SEARCH_TABS의 키. 그 탭만 색이 채워진다.
function render_search_bar(string $q, string $activeTab): void {
    // 폼을 다시 제출하면 '지금 보고 있는 탭'에서 다시 검색된다.
    //   작품 탭에서 다른 말을 검색했는데 통합으로 튕기면 하던 일이 끊긴다.
    $action = SEARCH_TABS[$activeTab][1];
    ?>
    <form class="search-form" method="get" action="<?= e($action) ?>">
      <input type="text" name="q" maxlength="<?= SEARCH_QUERY_MAX ?>" value="<?= e($q) ?>"
             placeholder="작품 · 글 · 유저 통합검색">
      <button type="submit">검색</button>
    </form>

    <div class="search-tabs">
      <?php foreach (SEARCH_TABS as $key => [$label, $path]): ?>
        <?php // 'page' => '' 로 페이지를 지운다 — 3페이지를 보다 탭을 옮기면 새 목록의 3페이지가 아니라 처음부터다 ?>
        <a class="<?= $activeTab === $key ? 'active' : '' ?>"
           href="<?= e(query_url($path, ['page' => ''])) ?>"><?= e($label) ?></a>
      <?php endforeach; ?>
    </div>
    <?php
}

// ── 본문에서 '검색어가 있는 부분'을 잘라 보여준다 ────────────
//   ★ 왜 앞에서 그냥 자르지 않나
//     검색어가 본문 500자 뒤쪽에 있으면, 앞 120자만 보여줘 봐야
//     "왜 이 글이 걸렸지?"를 알 수 없다. 그래서 찾은 자리 근처를 잘라 온다.
//   앞뒤가 잘렸다는 뜻으로 … 를 붙인다. (책에서 인용문 줄일 때 쓰는 그 기호)
function create_search_snippet(string $content, string $q, int $length = 120): string {
    // 줄바꿈·연속 공백을 한 칸으로 — 목록에서는 한 줄로 보여야 하므로
    $flat  = trim(preg_replace('/\s+/u', ' ', $content));
    $found = $q === '' ? false : mb_stripos($flat, $q);

    // 찾은 자리 30글자 앞에서 시작한다 (앞 문맥이 조금 보여야 읽힌다)
    $start = ($found === false || $found < 30) ? 0 : $found - 30;

    $snippet = mb_substr($flat, $start, $length);
    $prefix  = $start > 0 ? '… ' : '';
    $suffix  = mb_strlen($flat) > $start + $length ? ' …' : '';

    // create_highlighted가 이스케이프(e)까지 해주므로 여기서 또 하지 않는다
    return $prefix . create_highlighted($snippet, $q) . $suffix;
}

// ── 결과 줄 ①: 작품 (TMDB) ─────────────────────────────────
function render_work_results(array $works, string $q): void {
    ?>
    <ul class="media-list">
      <?php foreach ($works as $w): ?>
        <?php // TMDB 작품엔 우리 slug가 없다 → 'tmdb-<번호>'. 누군가 글을 쓰는 순간 이 이름으로 저장된다. ?>
        <li>
          <a href="/board/?work=tmdb-<?= e((string)$w['tmdb_id']) ?>">
            <?php if ($w['poster_url'] !== ''): ?>
              <img class="poster" src="<?= e($w['poster_url']) ?>" alt="" loading="lazy">
            <?php else: ?>
              <span class="poster poster-empty">No Image</span>
            <?php endif; ?>
            <span class="media-info">
              <strong><?= create_highlighted($w['title'], $q) ?></strong>
              <span class="post-stat"><?= e($w['genre']) ?> · <?= e((string)($w['year'] ?? '')) ?></span>
            </span>
          </a>
        </li>
      <?php endforeach; ?>
    </ul>
    <?php
}

// ── 결과 줄 ②: 글 ──────────────────────────────────────────
//   게시판 목록과 달리 '어느 작품 글인지'를 함께 보여준다.
//   통합검색 결과는 여러 작품에서 섞여 오므로, 그게 없으면 어디 글인지 알 수 없다.
function render_post_results(array $posts, string $q): void {
    ?>
    <ul class="post-list search-post-list">
      <?php foreach ($posts as $p): ?>
        <li>
          <a class="search-post-title" href="/post/view.php?id=<?= e((string)$p['id']) ?>">
            <?= create_highlighted($p['title'], $q) ?>
          </a>
          <span class="tag"><?= e($p['sentiment']) ?></span>

          <?php // 본문 미리보기 — 검색어가 있는 부분을 잘라서 ?>
          <p class="search-snippet"><?= create_search_snippet((string)$p['content'], $q) ?></p>

          <span class="search-post-meta">
            <a href="/board/?work=<?= e($p['work']) ?>"><?= e($p['workTitle']) ?></a>
            · <?= level_badge_html((int)$p['authorPostCount']) ?> <?= e($p['authorNick']) ?>
            · <time datetime="<?= e(format_time_machine((int)$p['created'])) ?>"><?= e(format_time_short((int)$p['created'])) ?></time>
            <?php if ((int)$p['comments'] > 0): ?> · 💬 <?= (int)$p['comments'] ?><?php endif; ?>
            <?php if ((int)$p['likes'] > 0): ?> · 👍 <?= (int)$p['likes'] ?><?php endif; ?>
          </span>
        </li>
      <?php endforeach; ?>
    </ul>
    <?php
}

// ── 결과 줄 ③: 유저 ────────────────────────────────────────
function render_user_results(array $users, string $q): void {
    ?>
    <ul class="user-list">
      <?php foreach ($users as $u): ?>
        <li>
          <a href="/profile/?user=<?= urlencode((string)$u['username']) ?>">
            <?php // 프로필 사진이 없으면 닉네임 첫 글자를 원 안에 (프로필·랭킹과 같은 규칙) ?>
            <?php if (!empty($u['avatar'])): ?>
              <img class="user-avatar" src="<?= e($u['avatar']) ?>" alt="" loading="lazy">
            <?php else: ?>
              <span class="user-avatar user-avatar-empty"><?= e(mb_substr((string)$u['nickname'], 0, 1)) ?></span>
            <?php endif; ?>
            <span class="user-info">
              <strong><?= level_badge_html((int)$u['postCount']) ?> <?= create_highlighted((string)$u['nickname'], $q) ?></strong>
              <span class="user-meta">@<?= create_highlighted((string)$u['username'], $q) ?>
                · ✍️ 글 <?= (int)$u['postCount'] ?> · 💬 댓글 <?= (int)$u['commentCount'] ?></span>
            </span>
          </a>
        </li>
      <?php endforeach; ?>
    </ul>
    <?php
}

// ── 페이지 이동 줄 (카테고리 전용 페이지 3곳이 함께 쓴다) ────
//   게시판(.pagination)과 같은 모양. 검색어(q)는 query_url이 알아서 유지한다.
function render_search_pagination(string $path, int $page, int $totalPages): void {
    if ($totalPages <= 1) {
        return;                       // 한 페이지뿐이면 아예 안 그린다
    }
    ?>
    <nav class="pagination">
      <?php if ($page > 1): ?>
        <a class="page-nav" href="<?= e(query_url($path, ['page' => $page - 1])) ?>">← 이전</a>
      <?php else: ?>
        <span class="page-nav disabled">← 이전</span>
      <?php endif; ?>

      <div class="page-numbers">
        <?php for ($n = 1; $n <= $totalPages; $n++): ?>
          <a class="page-num <?= $n === $page ? 'active' : '' ?>"
             href="<?= e(query_url($path, ['page' => $n])) ?>"><?= $n ?></a>
        <?php endfor; ?>
      </div>

      <?php if ($page < $totalPages): ?>
        <a class="page-nav" href="<?= e(query_url($path, ['page' => $page + 1])) ?>">다음 →</a>
      <?php else: ?>
        <span class="page-nav disabled">다음 →</span>
      <?php endif; ?>
    </nav>
    <?php
}

// ── 검색어를 받아 다듬는다 (네 화면이 같은 규칙을 쓰도록) ────
//   앞뒤 공백을 떼고, 너무 긴 입력은 잘라낸다.
function create_search_query(): string {
    return mb_substr(trim(get_str('q')), 0, SEARCH_QUERY_MAX);
}

// 페이지 번호를 받아 1 미만이면 1로 (?page=0, ?page=-5 방어)
function create_search_page(): int {
    $page = get_int('page', 1);
    return $page < 1 ? 1 : $page;
}
