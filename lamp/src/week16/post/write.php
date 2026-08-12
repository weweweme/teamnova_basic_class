<?php
// ============================================================
// post/write.php — 글쓰기 폼  [GET 요청]
//   이 파일은 '폼을 보여주기만' 한다. 실제 저장은 create.php(POST)가 담당.
//   ?work=parasite 로 들어오면(게시판에서 '글쓰기'를 누른 경우) 그 작품이 미리 선택된다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/works.php';   // 작품 목록을 고르게 하려고
require_once __DIR__ . '/../includes/drafts.php';  // 쓰다 만 초안 되살리기

// ★ 로그인해야 글을 쓸 수 있다. (안 했으면 로그인 페이지로)
require_login();

// 이 페이지는 좁은 중앙 컬럼(760px)으로 — 제목·포스터·폼이 같은 왼쪽 선에 정렬되게.
$containerClass = 'narrow';

// 어느 작품에 쓰는 글인지 — 게시판에서 ?work=slug 로 넘어온다.
//   글쓰기는 항상 '특정 작품 게시판'에서 시작하므로, 작품은 고정이다(고르는 게 아니라).
$work     = get_str('work', '');
$workInfo = get_work($work);   // DB에 있으면 그 정보, 없으면 TMDB에서 (폴백)

// 이 작품에 쓰다 만 초안이 있으면 꺼내 온다 (없으면 빈 배열).
//   ★ 작품별로 따로 담기 때문에, 다른 작품 글쓰기를 열면 여기 아무것도 안 나온다.
$draft = get_draft($work);

// 작품이 정해지지 않았으면(주소로 직접 들어옴 등) → 검색으로 안내
if ($workInfo === null) {
    $pageTitle = '글쓰기';
    require __DIR__ . '/../includes/header.php';
    echo '<p class="muted">먼저 <a href="/search/">작품을 검색</a>해 게시판에 들어간 뒤 글쓰기를 눌러주세요.</p>';
    require __DIR__ . '/../includes/footer.php';
    exit;
}

$pageTitle = '글쓰기';
require __DIR__ . '/../includes/header.php';
?>

  <h1>글쓰기</h1>

  <!-- 어느 작품에 쓰는지 포스터로 한눈에 (게시판에서 넘어온 그 작품) -->
  <div class="write-context">
    <?php if (!empty($workInfo['poster_url'])): ?>
      <img class="write-context-poster" src="<?= e($workInfo['poster_url']) ?>" alt="" loading="lazy">
    <?php endif; ?>
    <div>
      <span class="muted">리뷰 작성</span>
      <strong><?= e($workInfo['title']) ?></strong>
    </div>
  </div>

  <?php // 길이 초과 거절 안내는 header.php가 주소(?flash=)에서 읽어 그린다 ?>

  <!-- 폼(form) = 사용자 입력을 모아 서버로 '제출'하는 상자.
       method="post" : POST로 보낸다 (데이터가 주소에 안 보이고 '봉투 안'으로).
       action="/post/create.php" : 제출하면 이 파일이 처리한다. -->
  <form class="write-form" method="post" action="/post/create.php">
    <?= csrf_field() ?>

    <!-- 어느 작품 글인지는 고정 — 게시판에서 넘어온 그 작품. hidden으로 slug를 함께 보낸다.
         (dropdown이 아닌 이유: 글쓰기는 특정 작품 게시판에서만 시작하므로 작품이 이미 정해짐) -->
    <input type="hidden" name="work" value="<?= e($work) ?>">

    <?php /* ★ 값 채우기 = 세션에 저장된 초안($draft). 아래 셋 다 같은 출처다.
             초안은 JS가 몇 초마다 저장하고, 검증에 걸려 되돌아왔을 때도 서버가 갱신한다. */ ?>

    <!-- label = 입력칸 설명표. input의 name = 서버에서 값 꺼낼 '열쇠'($_POST['title']) -->
    <label>제목
      <input type="text" name="title" maxlength="100" required value="<?= e($draft['title'] ?? '') ?>">
    </label>

    <!-- textarea = 여러 줄 입력칸 (내용용)
         ★ textarea는 value 속성이 없다. 여는 태그와 닫는 태그 '사이'가 곧 값이다.
           그래서 되살릴 값도 태그 안쪽에 출력한다.
           줄바꿈이 값에 그대로 들어가므로 여는 태그 바로 뒤에 붙여 쓴다. -->
    <label>내용
      <textarea name="content" rows="6" maxlength="5000" required><?= e($draft['content'] ?? '') ?></textarea>
    </label>

    <!-- radio = 여러 개 중 하나만 선택. 같은 name이면 한 묶음.
         ★ 라디오 동그라미는 숨기고(label에 opacity 0으로) 라벨을 '버튼'처럼 보이게 한다.
           CSS의 label:has(input:checked)로 '지금 고른 것'만 색을 넣어 강조. -->
    <fieldset class="sentiment-field">
      <legend>감상</legend>
      <?php // 되돌아온 값이 있으면 그걸 고른 상태로, 없으면 기본값 '호평'. ?>
      <?php $oldSentiment = ($draft['sentiment'] ?? '') !== '' ? $draft['sentiment'] : '호평'; ?>
      <div class="sentiment-opts">
        <label class="sentiment-opt s-good"><input type="radio" name="sentiment" value="호평" <?= $oldSentiment === '호평' ? 'checked' : '' ?>><span>👍 호평</span></label>
        <label class="sentiment-opt s-mid"><input type="radio" name="sentiment" value="보통" <?= $oldSentiment === '보통' ? 'checked' : '' ?>><span>😐 보통</span></label>
        <label class="sentiment-opt s-bad"><input type="radio" name="sentiment" value="혹평" <?= $oldSentiment === '혹평' ? 'checked' : '' ?>><span>👎 혹평</span></label>
      </div>
    </fieldset>

    <!-- submit 버튼을 누르면 → 위 값들이 POST로 create.php에 전송됨 -->
    <div class="write-submit">
      <button type="submit">등록</button>
      <?php // 자동 저장 상태를 알려주는 자리. JS가 글자를 채운다(처음엔 비어 있음). ?>
      <span id="draft-status" class="muted"></span>
    </div>
  </form>

<script>
// ── 임시저장(초안) 자동 저장 ────────────────────────────────
//   입력이 '멈춘 뒤' 2초에 한 번만 서버로 보낸다.
//   ★ 글자를 칠 때마다 보내면 요청이 수백 번 간다. 그래서 타이머를 매번 새로 건다
//     (= 디바운스). 계속 치는 동안에는 타이머가 계속 밀려서 안 보내진다.
(function () {
  const form   = document.querySelector('.write-form');
  const status = document.getElementById('draft-status');
  if (!form) return;

  let timer = null;
  let lastSent = '';           // 마지막으로 보낸 내용 — 같으면 안 보낸다(헛요청 방지)

  function collect() {
    const data = new FormData(form);
    return {
      work:      data.get('work')      || '',
      title:     data.get('title')     || '',
      content:   data.get('content')   || '',
      sentiment: data.get('sentiment') || '',
      // ★ CSRF 토큰도 같이 보낸다. 폼에 이미 hidden으로 들어 있는 그 값이다.
      _token:    data.get('_token')    || ''
    };
  }

  async function save() {
    const values = collect();
    if (values.title === '' && values.content === '') return;   // 빈 폼은 저장 안 함

    const body = new URLSearchParams(values).toString();
    if (body === lastSent) return;                              // 바뀐 게 없으면 안 보냄

    try {
      const res = await fetch('/api/draft.php', {
        method:  'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body:    body
      });
      const json = await res.json();
      if (json.ok) {
        lastSent = body;
        status.textContent = '임시저장됨 ' + json.at;
      }
    } catch (e) {
      // 저장에 실패해도 글쓰기 자체를 막지는 않는다 — 어디까지나 보조 장치다.
      status.textContent = '임시저장 실패 (계속 쓰셔도 됩니다)';
    }
  }

  form.addEventListener('input', function () {
    clearTimeout(timer);                 // 이전 예약을 취소하고
    timer = setTimeout(save, 2000);      // 2초 뒤로 다시 예약
    status.textContent = '';
  });

  // 등록을 누르면 예약된 저장은 취소한다 (등록되면 초안은 서버가 지운다).
  form.addEventListener('submit', function () { clearTimeout(timer); });
})();
</script>

<?php require __DIR__ . '/../includes/footer.php'; ?>
