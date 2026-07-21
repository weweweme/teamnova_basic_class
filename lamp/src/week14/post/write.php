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

// 게시판에서 넘어온 작품 (없으면 빈 값 → 사용자가 직접 고름)
$work  = $_GET['work'] ?? '';
$works = get_works();

$pageTitle = '글쓰기';
require __DIR__ . '/../includes/header.php';
?>

  <h1>글쓰기</h1>

  <!-- 폼(form) = 사용자 입력을 모아 서버로 '제출'하는 상자.
       method="post" : POST로 보낸다 (데이터가 주소에 안 보이고 '봉투 안'으로).
       action="/post/create.php" : 제출하면 이 파일이 처리한다. -->
  <form class="write-form" method="post" action="/post/create.php">

    <!-- 어느 작품 글인지 고른다.
         게시판에서 넘어왔다면(?work=) 그 작품이 미리 선택되도록 selected를 붙인다. -->
    <label>작품
      <select name="work" required>
        <option value="">— 작품 선택 —</option>
        <?php foreach ($works as $w): ?>
          <option value="<?= e($w['slug']) ?>"
                  <?= $work === $w['slug'] ? 'selected' : '' ?>>
            <?= e($w['title']) ?> (<?= e($w['genre']) ?> · <?= e((string)$w['year']) ?>)
          </option>
        <?php endforeach; ?>
      </select>
    </label>

    <!-- label = 입력칸 설명표. input의 name = 서버에서 값 꺼낼 '열쇠'($_POST['title']) -->
    <label>제목
      <input type="text" name="title" required>
    </label>

    <!-- textarea = 여러 줄 입력칸 (내용용) -->
    <label>내용
      <textarea name="content" rows="6" required></textarea>
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
