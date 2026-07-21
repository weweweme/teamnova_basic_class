<?php
// ============================================================
// works.php — 전체 작품 목록  [GET 요청]
//   ?genre=영화 / 드라마 로 장르를 걸러 본다.
//   각 작품의 '글 수'와 '추천 비율'까지 함께 보여준다.
//
//   ※ 파일 이름이 includes/works.php(모듈)와 같지만 폴더가 달라 서로 다른 파일이다.
//     이 파일 = 화면(페이지) / includes/works.php = 데이터·기능 모듈.
// ============================================================
require_once __DIR__ . '/includes/util.php';
require_once __DIR__ . '/includes/works.php';   // 작품 데이터 모듈
require_once __DIR__ . '/includes/posts.php';   // 글 수를 세려고 함께 사용

// ── 1) 장르 필터 받기 + 화이트리스트 검증 ────────────────────
$genre = $_GET['genre'] ?? '';
if (!in_array($genre, ['영화', '드라마'], true)) {
    $genre = '';   // 이상한 값이면 '전체'로
}

// ── 2) 작품 목록 걸러내기 + 글 목록 미리 가져오기 ────────────
$works    = filter_works_by_genre(get_works(), $genre);
$allPosts = get_posts();   // 반복문 안에서 매번 부르지 않도록 '한 번만' 가져와 재사용

$genres = ['' => '전체', '영화' => '영화', '드라마' => '드라마'];

$pageTitle = '전체 작품';
require __DIR__ . '/includes/header.php';
?>

  <h1>전체 작품</h1>

  <!-- 장르 필터: ?genre= 만 바꾼다 -->
  <div class="filter-tabs">
    <?php foreach ($genres as $key => $label): ?>
      <a class="<?= $genre === $key ? 'active' : '' ?>"
         href="<?= e(query_url('/works.php', ['genre' => $key])) ?>"><?= e($label) ?></a>
    <?php endforeach; ?>
  </div>

  <p class="muted">총 <?= count($works) ?>개 작품</p>

  <?php if (!$works): ?>
    <p class="muted">해당 장르의 작품이 없습니다.</p>
  <?php else: ?>
    <ul class="work-list">
      <?php foreach ($works as $w): ?>
        <?php
          // 이 작품의 글 수 (posts 모듈 재사용)
          $postCount = count(filter_posts_by_work($allPosts, $w['slug']));

          // 추천 비율 (0으로 나누기 방지 — 먼저 확인)
          $totalVotes = $w['upVotes'] + $w['downVotes'];
          $upPct      = $totalVotes > 0 ? (int)round($w['upVotes'] / $totalVotes * 100) : 0;
        ?>
        <li>
          <!-- 작품명을 누르면 그 작품 게시판으로 -->
          <a href="/board/?work=<?= e($w['slug']) ?>"><?= e($w['title']) ?></a>
          <span class="tag">추천 <?= $upPct ?>%</span>
          <span class="post-stat"><?= e($w['genre']) ?> · <?= e((string)$w['year']) ?> · <?= e($w['director']) ?> · 글 <?= $postCount ?>개</span>
        </li>
      <?php endforeach; ?>
    </ul>
  <?php endif; ?>

<?php require __DIR__ . '/includes/footer.php'; ?>
