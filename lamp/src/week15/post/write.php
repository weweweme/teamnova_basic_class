<?php
// ============================================================
// post/write.php — 글쓰기 폼  [GET 요청]
//   이 파일은 '폼을 보여주기만' 한다. 실제 저장은 create.php(POST)가 담당.
//   ?work=parasite 로 들어오면(게시판에서 '글쓰기'를 누른 경우) 그 작품이 미리 선택된다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/works.php';   // 작품 목록을 고르게 하려고

// ★ 로그인해야 글을 쓸 수 있다. (안 했으면 로그인 페이지로)
require_login();

// 어느 작품에 쓰는 글인지 — 게시판에서 ?work=slug 로 넘어온다.
//   글쓰기는 항상 '특정 작품 게시판'에서 시작하므로, 작품은 고정이다(고르는 게 아니라).
$work     = get_str('work', '');
$workInfo = get_work($work);   // DB에 있으면 그 정보, 없으면 TMDB에서 (폴백)

// 작품이 정해지지 않았으면(주소로 직접 들어옴 등) → 검색으로 안내
if ($workInfo === null) {
    $pageTitle = '글쓰기';
    require __DIR__ . '/../includes/header.php';
    echo '<p class="muted">먼저 <a href="/search.php">작품을 검색</a>해 게시판에 들어간 뒤 글쓰기를 눌러주세요.</p>';
    require __DIR__ . '/../includes/footer.php';
    exit;
}

$pageTitle = '글쓰기';
require __DIR__ . '/../includes/header.php';
?>

  <h1>글쓰기</h1>
  <p class="muted"><strong><?= e($workInfo['title']) ?></strong> 에 리뷰를 씁니다.</p>

  <?php // 길이 초과 거절 안내는 header.php가 세션에서 꺼내 그린다 ?>

  <!-- 폼(form) = 사용자 입력을 모아 서버로 '제출'하는 상자.
       method="post" : POST로 보낸다 (데이터가 주소에 안 보이고 '봉투 안'으로).
       action="/post/create.php" : 제출하면 이 파일이 처리한다. -->
  <form class="write-form" method="post" action="/post/create.php">

    <!-- 어느 작품 글인지는 고정 — 게시판에서 넘어온 그 작품. hidden으로 slug를 함께 보낸다.
         (dropdown이 아닌 이유: 글쓰기는 특정 작품 게시판에서만 시작하므로 작품이 이미 정해짐) -->
    <input type="hidden" name="work" value="<?= e($work) ?>">

    <!-- label = 입력칸 설명표. input의 name = 서버에서 값 꺼낼 '열쇠'($_POST['title']) -->
    <label>제목
      <input type="text" name="title" maxlength="100" required>
    </label>

    <!-- textarea = 여러 줄 입력칸 (내용용) -->
    <label>내용
      <textarea name="content" rows="6" maxlength="5000" required></textarea>
    </label>

    <!-- radio = 여러 개 중 하나만 선택. 같은 name이면 한 묶음. -->
    <fieldset>
      <legend>감상</legend>
      <label><input type="radio" name="sentiment" value="호평" checked> 호평</label>
      <label><input type="radio" name="sentiment" value="보통"> 보통</label>
      <label><input type="radio" name="sentiment" value="혹평"> 혹평</label>
    </fieldset>

    <!-- submit 버튼을 누르면 → 위 값들이 POST로 create.php에 전송됨 -->
    <button type="submit">등록</button>
  </form>

<?php require __DIR__ . '/../includes/footer.php'; ?>
