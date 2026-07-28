<?php
// ============================================================
// profile/avatar.php — 프로필 이미지 업로드 처리  [POST → PRG]
//   프로필 화면의 '이미지 변경' 폼이 보낸 파일을 받아 저장한다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';
require_once __DIR__ . '/../includes/upload.php';

// ★ 로그인 필수 — 남의 프로필 이미지를 못 바꾸게 (요청 조작 방어)
require_login();

// ── 0) POST로 온 게 맞나? ────────────────────────────────────
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /');
    exit;
}

$userId   = current_user_id();
$username = (string) current_user();

// ── 1) 파일이 실제로 왔나? ───────────────────────────────────
//   $_FILES = 업로드된 파일 정보를 담는 슈퍼글로벌 ($_POST의 '파일' 버전).
if (!isset($_FILES['avatar'])) {
    header('Location: /settings/');
    exit;
}

// ── 2) 저장 (upload 모듈이 3중 검증 후 저장) ─────────────────
//   save_avatar가 '진짜 이미지인가·크기·안전한 파일명'을 다 확인하고, 실패하면 null.
$url = save_avatar($_FILES['avatar'], $userId);

if ($url === null) {
    set_flash('이미지 업로드에 실패했습니다. (JPG·PNG·GIF·WebP, 2MB 이하)', 'error');
    header('Location: /settings/');
    exit;
}

// ── 3) DB에 이미지 주소 기록 ─────────────────────────────────
//   ★ 파일은 uploads 폴더에, '그 파일의 주소'만 DB에 저장한다.
//     (이미지 자체를 DB에 넣지 않는다 — 파일은 파일시스템, DB엔 경로. 실무 표준)
set_avatar($userId, $url);

// ── 4) PRG: 내 프로필로 ──────────────────────────────────────
set_flash('🖼️ 프로필 이미지가 변경되었습니다.');
header('Location: /settings/');
exit;
