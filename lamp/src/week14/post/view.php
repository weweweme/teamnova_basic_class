<?php
// ============================================================
// post/view.php — 글 보기  [GET 요청]
//   홈·종목토론방이 보낸  ?id=1  을 받아서 글 하나를 보여준다.
//   board(?ticker=)와 똑같은 'GET으로 값 받기' 패턴 (이번엔 ?id=).
// ============================================================
require_once __DIR__ . '/../includes/util.php';

// ── 1) id 받기 ───────────────────────────────────────────────
//   (int) = 정수로 '강제 형변환'. id는 숫자여야 하니, ?id=abc 같은 건 0이 되고
//           ?id=5 는 5가 된다. (숫자 아닌 이상한 입력을 걸러내는 1차 안전장치)
//   ?? 0 = 아예 id가 없으면 0.
$id = (int)($_GET['id'] ?? 0);

// ── 2) 더미 글 데이터 (나중 posts 테이블에서 id로 조회로 교체) ──
//   키(1, 2)가 글 번호. 값은 글 한 건(제목·작성자·종목·심리·내용).
$allPosts = [
    1 => [
        'title'     => '삼성전자 지금 들어가도 될까요?',
        'author'    => '개미1',
        'stock'     => '삼성전자',
        'sentiment' => '매수',
        'content'   => "요즘 반도체 업황이 살아나는 것 같은데\n지금 들어가도 괜찮을까요?",
    ],
    2 => [
        'title'     => '하이닉스 실적 발표 한줄 정리',
        'author'    => '투자왕',
        'stock'     => 'SK하이닉스',
        'sentiment' => '중립',
        'content'   => "이번 분기 실적은 시장 예상에 부합.\n다음 분기 가이던스가 관건.",
    ],
];

// ── 3) 검증(Tester-Doer): 그 번호의 글이 있나? 없으면 안내 후 종료 ──
if (!isset($allPosts[$id])) {
    $pageTitle = '글을 찾을 수 없음';
    require __DIR__ . '/../includes/header.php';
    echo '<p>존재하지 않는 글입니다. <a href="/">홈으로</a></p>';
    require __DIR__ . '/../includes/footer.php';
    exit;
}

// 여기까지 왔으면 글이 존재 → 꺼낸다.
//   $post = "GET으로 온 id에 해당하는 글 한 건".
//   ★ GET은 id를 '전달'만 할 뿐, 그 id로 실제 데이터를 '찾는 것'은 서버(여기)의 일.
//   지금은 데이터 출처가 위의 하드코딩 배열($allPosts)이라 배열에서 꺼내지만,
//   나중 DB를 붙이면 이 줄이 "SELECT * FROM posts WHERE id=$id" 조회로 바뀐다(패턴은 동일).
$post = $allPosts[$id];

// ── 더미 댓글 목록 (나중 comments 테이블에서 post_id로 조회로 교체) ──
$comments = [
    ['author' => '주주1', 'content' => '저도 그렇게 봅니다.'],
    ['author' => '개미2', 'content' => '음, 신중하게 접근해야 할 듯요.'],
];

$pageTitle = $post['title'];
require __DIR__ . '/../includes/header.php';
?>

  <!-- article = '독립적인 하나의 글'을 뜻하는 의미(시맨틱) 태그.
       <div>로 해도 작동은 하지만, article은 "이건 완결된 하나의 글"이란 뜻을 담아
       검색엔진·스크린리더가 구조를 이해하게 함(실무 FM). class="post"는 CSS 이름표. -->
  <article class="post">

    <!-- h1 = 이 글의 큰 제목. e()로 안전 출력 -->
    <h1><?= e($post['title']) ?></h1>

    <!-- p = 한 문단. 여기선 글의 부가정보(메타: 심리·종목·작성자)를 담음 -->
    <p class="post-meta">
      <!-- span = 글자 일부를 감싸는 '인라인' 작은 상자(줄바꿈 안 함). tag 클래스로 배지 색 입힘 -->
      <span class="tag"><?= e($post['sentiment']) ?></span>
      <!-- · 는 태그가 아니라 그냥 '가운뎃점' 글자(구분용) -->
      <?= e($post['stock']) ?> · <?= e($post['author']) ?>
    </p>

    <!-- div = 의미 없는 범용 상자(구역 묶기용). 여기선 본문을 담음.
         nl2br(e(...)) : 먼저 e()로 안전 처리 → nl2br로 줄바꿈(\n)을 <br>로.
         순서 중요! e()를 먼저 해야 우리가 넣는 <br>까지 무해화되지 않는다. -->
    <div class="post-content"><?= nl2br(e($post['content'])) ?></div>
  </article>

  <!-- section = '주제로 묶인 한 구획'을 뜻하는 의미 태그. 여기선 '댓글 구역'.
       article(독립 글) vs section(구획) vs div(의미 없는 상자) — 뜻이 서로 다름.
       (다음 단계에서 이 안에 댓글 목록 + 작성 폼을 넣고 comment/create.php 로 POST 연결) -->
  <section class="comments">
    <!-- h2 = 소제목 (h1보다 한 단계 작은 제목) -->
    <h2>댓글</h2>

    <?php
    // create.php가 댓글 처리 후 ?commented=1 로 리다이렉트해오면 완료 알림.
    // (글쓰기의 ?posted=1 과 똑같은 방식)
    ?>
    <?php if (isset($_GET['commented'])): ?>
      <div class="flash">✅ 댓글이 등록되었습니다. <small>(지금은 저장 안 되는 껍데기)</small></div>
    <?php endif; ?>

    <!-- 더미 댓글 목록 (나중 comments 테이블에서 post_id로 조회로 교체) -->
    <ul class="comment-list">
      <?php foreach ($comments as $c): ?>
        <li>
          <span class="comment-author"><?= e($c['author']) ?></span>
          <?= e($c['content']) ?>
        </li>
      <?php endforeach; ?>
    </ul>

    <!-- 댓글 작성 폼 → comment/create.php 로 POST 전송 -->
    <form class="comment-form" method="post" action="/comment/create.php">
      <!-- hidden = 화면엔 안 보이지만 폼과 '함께 전송'되는 값.
           이 댓글이 '몇 번 글'에 달리는지 서버에 알려주려고 현재 글 id를 실어 보낸다.
           (그래야 create.php가 처리 후 '그 글로' 되돌아갈 수 있음) -->
      <input type="hidden" name="post_id" value="<?= e((string)$id) ?>">
      <textarea name="content" rows="3" placeholder="댓글을 입력하세요" required></textarea>
      <button type="submit">댓글 등록</button>
    </form>
  </section>

<?php require __DIR__ . '/../includes/footer.php'; ?>
