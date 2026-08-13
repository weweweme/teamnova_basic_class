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

    <?php /* ★ 값 채우기 = DB에 저장해 둔 초안($draft). 아래 셋 다 같은 출처다.
             초안은 '💾 임시저장'을 눌렀을 때 저장되고, 등록했다가 검증에 걸려
             되돌아왔을 때도 서버가 갱신한다(create.php). */ ?>

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
      <?php // type="button" — 이걸 빼면 폼이 그냥 제출된다(button의 기본 동작이 submit이므로). ?>
      <button type="button" id="draft-save" class="btn-draft">💾 임시저장</button>
      <?php // 저장 결과를 알려주는 자리. JS가 글자를 채운다(처음엔 비어 있음). ?>
      <span id="draft-status" class="muted">
        <?php if (!empty($draft['title']) || !empty($draft['content'])): ?>
          이어서 쓰는 중 (임시저장해 둔 글)
        <?php endif; ?>
      </span>
    </div>
  </form>

<script>
// ── 임시저장(초안) — 버튼을 눌렀을 때만 저장한다 ────────────
//   [왜 자동 저장이 아니라 버튼인가]
//     자동 저장은 편하지만 **언제 저장됐는지 사용자가 모른다.** 글이 남았는지 아닌지를
//     화면이 아니라 '느낌'으로 판단하게 된다.
//     버튼은 **내가 눌렀으니 저장됐다**가 확실하다. 서버 요청도 누른 만큼만 간다.
//   ★ 대신 안 누르고 새로고침하면 그 내용은 날아간다. 그게 '명시적 저장'의 대가다.
//     (등록을 눌렀다가 검증에 걸린 경우는 서버가 알아서 초안을 갱신한다 — create.php)
(function () {
  const form   = document.querySelector('.write-form');
  const button = document.getElementById('draft-save');
  const status = document.getElementById('draft-status');
  if (!form || !button) return;

  // 'dirty' = 마지막 저장 이후 손댄 흔적이 있나. (편집기에서 쓰는 표현)
  //   ★ 이 값이 있어야 이탈 경고가 **정직해진다** — 저장한 뒤엔 묻지 않고,
  //     안 저장한 변경이 있을 때만 묻는다.
  let dirty = false;
  form.addEventListener('input', function () { dirty = true; });

  button.addEventListener('click', async function () {
    const data = new FormData(form);
    const values = {
      work:      data.get('work')      || '',
      title:     data.get('title')     || '',
      content:   data.get('content')   || '',
      sentiment: data.get('sentiment') || '',
      // ★ CSRF 토큰도 같이 보낸다. 폼에 이미 hidden으로 들어 있는 그 값이다.
      _token:    data.get('_token')    || ''
    };

    if (values.title === '' && values.content === '') {
      status.textContent = '쓴 내용이 없어요';
      return;
    }

    button.disabled = true;                 // 연타로 같은 요청이 여러 번 가지 않게
    status.textContent = '저장 중…';

    try {
      const res  = await fetch('/api/draft.php', {
        method:  'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body:    new URLSearchParams(values).toString()
      });
      const json = await res.json();
      // ★ 이제 초안은 세션이 아니라 DB 표에 있다 → 창을 닫아도, 다른 기기에서도 남는다.
      if (json.ok) {
        dirty = false;                      // 저장했으니 나가도 잃을 게 없다
        status.textContent = '임시저장됨 ' + json.at + ' · 창을 닫아도 남아요';
      } else {
        status.textContent = '임시저장 실패';
      }
    } catch (e) {
      // 저장에 실패해도 글쓰기 자체를 막지는 않는다 — 어디까지나 보조 장치다.
      status.textContent = '임시저장 실패 (계속 쓰셔도 됩니다)';
    } finally {
      button.disabled = false;
    }
  });

  // 등록은 정상적인 이탈이므로 경고하지 않는다.
  //   ★ submit 이벤트가 beforeunload 보다 먼저 일어나기 때문에 이 순서가 통한다.
  form.addEventListener('submit', function () { dirty = false; });

  // ── 저장 안 한 채로 떠나려 하면 한 번 묻는다 ──────────────
  //   [왜 다시 넣었나]
  //     자동 저장이던 시절엔 어차피 저장되니 이 경고가 **거짓말**이라 지웠다.
  //     지금은 **버튼을 눌러야만** 저장되므로, 안 누르고 나가면 진짜로 날아간다.
  //     → 경고가 사실이 되었으니 되살린다.
  //   ★ 단, '저장 뒤 바뀐 게 있을 때'만 묻는다. 저장하고 그대로 나가는데도 물으면
  //     사용자는 그 경고를 곧 무시하게 된다 — 늘 뜨는 경고는 없는 경고와 같다.
  //   ★ 문구는 우리가 못 정한다. 사이트가 겁주는 메시지를 띄우지 못하게
  //     브라우저가 정해진 문장만 보여준다. 우리는 '물어볼지 말지'만 정할 수 있다.
  window.addEventListener('beforeunload', function (event) {
    if (dirty) {
      event.preventDefault();
    }
  });
})();
</script>

<?php require __DIR__ . '/../includes/footer.php'; ?>
