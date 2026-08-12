<?php
// ============================================================
// trash/index.php — 휴지통 (내가 삭제한 글)  [GET]
//   ★ 열 때마다 '보관 기간 지난 글'을 먼저 자동 영구삭제한다(lazy purge).
//     이 환경엔 cron이 없어, 스케줄러 대신 요청에 얹어 정리한다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/posts.php';

require_login();
$userId = current_user_id();

// ① 보관 기간(30일) 지난 휴지통 글 자동 영구삭제
purge_expired_trash();

// ② 내 휴지통 목록
$trashed = get_trashed_posts($userId);

$pageTitle = '휴지통';
$containerClass = 'narrow';
require __DIR__ . '/../includes/header.php';
?>

  <h1>🗑 휴지통</h1>
  <p class="muted">
    <a href="/settings/">← 설정으로</a> ·
    삭제한 글은 <strong><?= TRASH_RETENTION_DAYS ?>일</strong> 동안 보관 후 자동으로 영구 삭제됩니다.
  </p>

  <?php if (!$trashed): ?>
    <p class="muted">휴지통이 비어 있습니다.</p>
  <?php else: ?>
    <ul class="trash-list">
      <?php foreach ($trashed as $t): ?>
        <?php
          // 남은 보관일 = 보관기간 - 지난 일수 (음수면 0으로)
          $daysPassed = (int) floor((time() - (int)$t['deletedAt']) / 86400);
          $daysLeft   = max(0, TRASH_RETENTION_DAYS - $daysPassed);
        ?>
        <li class="trash-item">
          <div class="trash-info">
            <?php // 지워진 글이라 제목은 링크 없이 텍스트로 ?>
            <strong><?= e($t['title']) ?></strong>
            <span class="trash-meta">
              <?= e($t['workTitle']) ?> ·
              <?= $daysLeft > 0 ? $daysLeft . '일 후 자동 삭제' : '오늘 자동 삭제 예정' ?>
            </span>
          </div>
          <div class="trash-actions">
            <!-- 되돌리기: 기존 복원 핸들러 재사용 -->
            <form method="post" action="/post/restore.php">
              <?= csrf_field() ?>
              <input type="hidden" name="id" value="<?= e((string)$t['id']) ?>">
              <button type="submit" class="btn-restore">↩️ 되돌리기</button>
            </form>
            <!-- 지금 영구삭제: 되돌릴 수 없으니 JS로 확인창 (delete-form 클래스) -->
            <form class="delete-form" method="post" action="/post/purge.php">
              <?= csrf_field() ?>
              <input type="hidden" name="id" value="<?= e((string)$t['id']) ?>">
              <button type="submit" class="btn-delete">🔥 영구삭제</button>
            </form>
          </div>
        </li>
      <?php endforeach; ?>
    </ul>
  <?php endif; ?>

<?php require __DIR__ . '/../includes/footer.php'; ?>
