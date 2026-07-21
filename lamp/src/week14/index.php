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

  <?php // 로그인·글등록 완료 알림은 header.php가 세션에서 꺼내 그린다 (set_flash) ?>

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
