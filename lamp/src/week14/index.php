<?php
// ============================================================
// index.php — 홈 화면  [GET 요청]
//   주소창에 그냥 접속(http://localhost/) = GET 요청.
//   지금은 DB가 없으니 '더미(가짜) 데이터'를 PHP 배열로 만들어 쓴다.
//   나중에 이 배열을 MariaDB 조회 결과로 갈아끼우면 끝.
// ============================================================

require_once __DIR__ . '/includes/util.php';

// 더미 종목 목록 (나중 stocks 테이블 대체)
$stocks = [
    ['ticker' => '005930', 'name' => '삼성전자'],
    ['ticker' => '000660', 'name' => 'SK하이닉스'],
    ['ticker' => 'AAPL',   'name' => '애플'],
];

// 더미 인기 글 (나중 posts 테이블 대체)
$posts = [
    ['id' => 1, 'title' => '삼성전자 지금 들어가도 될까요?', 'stock' => '삼성전자'],
    ['id' => 2, 'title' => '하이닉스 실적 발표 한줄 정리',   'stock' => 'SK하이닉스'],
];

// 탭 제목을 정하고 → 공통 상단(header)을 불러온다.
$pageTitle = '홈 · 종목토론방';
require __DIR__ . '/includes/header.php';
?>

  <?php
  // ── 완료 알림(flash) ─────────────────────────────────────
  //   create.php가 글 처리 후 '/?posted=1' 로 리다이렉트해온다.
  //   isset($_GET['posted']) = 주소에 ?posted 가 '있냐?' → true/false 반환.
  //     있을 때(= 글쓰기 직후)만 아래 초록 박스를 보여준다.
  //     왜 isset? 없는 값을 그냥 꺼내면 PHP 경고가 나므로, '있는지 먼저 확인'(Tester-Doer).
  //   메시지 '내용'은 여기(div), '생김새'(초록 박스)는 style.css 의 .flash 규칙.
  //     둘을 잇는 건 class="flash" (이름표).
  ?>
  <?php if (isset($_GET['posted'])): ?>
    <div class="flash">✅ 글이 등록되었습니다. <small>(지금은 저장 안 되는 껍데기예요)</small></div>
  <?php endif; ?>

  <h1>인기 종목</h1>
  <ul class="stock-list">
    <?php foreach ($stocks as $s): ?>
      <!-- 종목 토론방으로 이동: GET으로 ticker를 주소에 실어 보냄 (?ticker=...) -->
      <li>
        <a href="/board/?ticker=<?= e($s['ticker']) ?>">
          <?= e($s['name']) ?> (<?= e($s['ticker']) ?>)
        </a>
      </li>
    <?php endforeach; ?>
  </ul>

  <h1>인기 글</h1>
  <ul class="post-list">
    <?php foreach ($posts as $p): ?>
      <!-- 글 보기로 이동: GET으로 id를 주소에 실어 보냄 (?id=...) -->
      <li>
        <a href="/post/view.php?id=<?= e((string)$p['id']) ?>">
          <?= e($p['title']) ?>
        </a>
        <span class="tag"><?= e($p['stock']) ?></span>
      </li>
    <?php endforeach; ?>
  </ul>

<?php
// 공통 하단(footer)을 불러와 문서를 닫는다.
require __DIR__ . '/includes/footer.php';
