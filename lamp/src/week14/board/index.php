<?php
// ============================================================
// board/index.php — 종목 토론방  [GET 요청]
//   홈에서 보낸  /board/?ticker=005930  을 '받아서'
//   그 종목의 토론방 화면을 보여준다.
//   = 안드로이드 Intent의 getStringExtra("ticker") 자리(받는 쪽).
// ============================================================

// 이 파일은 board/ 폴더 '안'이라, 공통 조각은 한 단계 위(..)에서 불러온다.
require_once __DIR__ . '/../includes/util.php';

// ── 1) GET으로 온 ticker 받기 ────────────────────────────────
//   $_GET = 주소 뒤 ?이름=값 들이 담기는 PHP 기본 배열.
//   ?? '' = ticker가 아예 없으면(그냥 /board/ 로 접속) 빈 문자열로 둔다.
$ticker = $_GET['ticker'] ?? '';

// ── 2) 검증(Tester-Doer): ticker가 없으면 먼저 걸러낸다 ────────
//   실무 FM: 값이 없을 수 있으면 '먼저 확인'하고 분기. (있다 치고 진행 금지)
if ($ticker === '') {
    $pageTitle = '종목 토론방';
    require __DIR__ . '/../includes/header.php';
    echo '<p>종목을 선택해 주세요. <a href="/">홈으로</a></p>';
    require __DIR__ . '/../includes/footer.php';
    exit;   // 여기서 실행 종료 → 아래 코드는 안 돌린다.
}

// ── 3) 더미: ticker → 종목명 (나중 stocks 테이블 조회로 교체) ──
$stockNames = [
    '005930' => '삼성전자',
    '000660' => 'SK하이닉스',
    'AAPL'   => '애플',
];
// 목록에 없는 ticker면 '알 수 없는 종목'으로.
$name = $stockNames[$ticker] ?? '알 수 없는 종목';

// ── 4) 더미 글 목록 (나중 posts 테이블에서 ticker로 조회) ──────
$posts = [
    ['id' => 1, 'title' => '지금 들어가도 될까요?', 'sentiment' => '매수'],
    ['id' => 2, 'title' => '실적 발표 한줄 정리',    'sentiment' => '중립'],
];

$pageTitle = $name . ' 토론방';
require __DIR__ . '/../includes/header.php';
?>

  <!-- 종목 헤더: 이름 + 코드.
       ★ ticker는 '주소(URL)로 들어온 값' = 사용자가 조작 가능 → 반드시 e()로 감싼다(XSS 방지). -->
  <h1><?= e($name) ?> <small>(<?= e($ticker) ?>)</small></h1>

  <!-- TradingView 현재가·차트 위젯 자리 (나중에 임베드 코드로 교체) -->
  <div class="widget-placeholder">📈 현재가 · 차트 위젯 자리 (나중 연결)</div>

  <h2>토론 글</h2>
  <ul class="post-list">
    <?php foreach ($posts as $p): ?>
      <li>
        <!-- 글 보기로 이동: GET으로 id 전달 -->
        <a href="/post/view.php?id=<?= e((string)$p['id']) ?>">
          <?= e($p['title']) ?>
        </a>
        <span class="tag"><?= e($p['sentiment']) ?></span>
      </li>
    <?php endforeach; ?>
  </ul>

<?php require __DIR__ . '/../includes/footer.php'; ?>
