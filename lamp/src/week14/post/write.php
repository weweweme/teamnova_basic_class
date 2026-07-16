<?php
// ============================================================
// post/write.php — 글쓰기 폼  [GET 요청]
//   이 파일은 '폼을 보여주기만' 한다. 실제 저장은 create.php(POST)가 담당.
//   = 실무 FM의 '뷰(보여주기)'와 '액션(처리)' 분리.
// ============================================================
require_once __DIR__ . '/../includes/util.php';

$pageTitle = '글쓰기';
require __DIR__ . '/../includes/header.php';
?>

  <h1>글쓰기</h1>

  <!-- 폼(form) = 사용자 입력을 모아 서버로 '제출'하는 상자.
       method="post" : POST로 보낸다 (데이터가 주소에 안 보이고 '봉투 안'으로).
       action="/post/create.php" : 제출하면 이 파일이 처리한다 (Intent의 목적지). -->
  <form class="write-form" method="post" action="/post/create.php">

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
      <legend>투자 심리</legend>
      <label><input type="radio" name="sentiment" value="매수" checked> 매수</label>
      <label><input type="radio" name="sentiment" value="중립"> 중립</label>
      <label><input type="radio" name="sentiment" value="매도"> 매도</label>
    </fieldset>

    <!-- submit 버튼을 누르면 → 위 값들이 POST로 create.php에 전송됨 -->
    <button type="submit">등록</button>
  </form>

<?php require __DIR__ . '/../includes/footer.php'; ?>
