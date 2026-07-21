<?php
// ============================================================
// index.php — 홈 화면  [GET 요청]
//   주소창에 그냥 접속(http://localhost/) = GET 요청.
//   지금은 DB가 없으니 '더미(가짜) 데이터'를 모듈에서 가져와 쓴다.
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/posts.php';    // 글 모듈
require_once __DIR__ . '/includes/works.php';    // 작품 모듈

// 작품 목록 (works 모듈에서 — 더 이상 여기서 직접 들고 있지 않는다)
$works = get_works();

// 인기 글 3개 = 전체 글을 '인기순'으로 정렬한 뒤 앞에서 3개만 자르기.
//   board·search에서 쓰던 정렬·자르기 함수를 그대로 재사용 (모듈로 빼둔 덕분!)
$posts = paginate_posts(sort_posts(get_posts(), 'hot'), 1, 3);

// 탭 제목을 정하고 → 공통 상단(header)을 불러온다.
$pageTitle = '홈 · 리뷰 커뮤니티';
require __DIR__ . '/includes/header.php';
?>

  <?php
  // ── 완료 알림(flash) ─────────────────────────────────────
  //   isset($_GET['posted']) = 주소에 ?posted 가 '있냐?' → true/false 반환.
  //   왜 isset? 없는 값을 그냥 꺼내면 PHP 경고가 나므로, '있는지 먼저 확인'(Tester-Doer).
  //   메시지 '내용'은 여기(div), '생김새'(초록 박스)는 style.css 의 .flash 규칙.
  ?>
  <?php if (isset($_GET['posted'])): ?>
    <div class="flash">✅ 글이 등록되었습니다. <small>(임시 저장 — 브라우저를 닫으면 초기화됩니다)</small></div>
  <?php endif; ?>
  <?php if (isset($_GET['loggedin'])): ?>
    <div class="flash">👋 <?= e((string)current_user()) ?>님, 환영합니다!</div>
  <?php endif; ?>

  <h1>작품</h1>
  <ul class="work-list">
    <?php foreach ($works as $w): ?>
      <!-- 작품 게시판으로 이동: GET으로 work를 주소에 실어 보냄 (?work=...) -->
      <li>
        <a href="/board/?work=<?= e($w['slug']) ?>"><?= e($w['title']) ?></a>
        <span class="post-stat"><?= e($w['genre']) ?> · <?= e((string)$w['year']) ?></span>
      </li>
    <?php endforeach; ?>
  </ul>

  <h1>인기 글</h1>
  <ul class="post-list">
    <?php foreach ($posts as $p): ?>
      <!-- 글 보기로 이동: GET으로 id를 주소에 실어 보냄 (?id=...) -->
      <li>
        <a href="/post/view.php?id=<?= e((string)$p['id']) ?>"><?= e($p['title']) ?></a>
        <span class="tag"><?= e($p['workTitle']) ?></span>
      </li>
    <?php endforeach; ?>
  </ul>

<?php
// 공통 하단(footer)을 불러와 문서를 닫는다.
require __DIR__ . '/includes/footer.php';
