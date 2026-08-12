<?php
// ============================================================
// upload.php — 이미지 업로드 처리 모듈
//   ★ 파일 업로드는 '사용자가 서버에 파일을 올리는' 일이라 위험하다.
//     검증 없이 받으면 이미지로 위장한 .php(악성코드)를 올려 서버를 장악할 수 있다.
//     그래서 아래 3중 방어를 반드시 거친다:
//       ① 진짜 이미지인가 (확장자 말고 '내용'으로 확인)
//       ② 크기 제한
//       ③ 파일명은 우리가 새로 짓는다 (사용자가 준 이름 절대 안 씀)
// ============================================================

require_once __DIR__ . '/config.php';   // (경로 상수 등이 필요하면)

const AVATAR_DIR      = __DIR__ . '/../uploads/avatars';   // 실제 저장 폴더(서버 경로)
const AVATAR_URL_BASE = '/uploads/avatars';                // 화면에서 쓸 주소
const AVATAR_MAX_BYTE = 2 * 1024 * 1024;                   // 2MB 제한

// 허용할 이미지 종류: 'MIME 타입' => '확장자'
//   MIME 타입 = 파일의 '진짜 종류'. finfo가 파일 내용을 열어 판별한다(확장자로 속일 수 없음).
const AVATAR_ALLOWED = [
    'image/jpeg' => 'jpg',
    'image/png'  => 'png',
    'image/gif'  => 'gif',
    'image/webp' => 'webp',
];

// ── 아바타 이미지를 저장하고, 저장된 '주소'를 돌려준다. 실패하면 null. ──
//   $file: $_FILES['avatar'] 배열 하나 ('tmp_name','size','error' 등이 들어있음)
//   $userId: 파일명에 쓸 회원 번호 (겹치지 않는 이름을 만들려고)
function save_avatar(array $file, int $userId): ?string {
    // ── ① 업로드 자체가 정상인가? ────────────────────────────
    //   error가 0(UPLOAD_ERR_OK)이 아니면 뭔가 잘못된 것 (용량 초과 등).
    if (!isset($file['error']) || $file['error'] !== UPLOAD_ERR_OK) {
        return null;
    }
    //   is_uploaded_file: 이 파일이 '진짜 HTTP 업로드로 온 것'인지 확인.
    //     (공격자가 서버의 다른 파일 경로를 넘겨 훔쳐보려는 걸 막는다)
    if (!is_uploaded_file($file['tmp_name'])) {
        return null;
    }

    // ── ② 크기 제한 ──────────────────────────────────────────
    if ($file['size'] > AVATAR_MAX_BYTE || $file['size'] === 0) {
        return null;
    }

    // ── ③ '진짜 이미지'인가 — 내용으로 확인 (확장자 안 믿음) ──
    //   finfo가 파일의 앞부분(매직 넘버)을 읽어 실제 종류를 알아낸다.
    //   'evil.php'를 'photo.jpg'로 이름만 바꿔 올려도 여기서 걸린다.
    $finfo = new finfo(FILEINFO_MIME_TYPE);
    $mime  = $finfo->file($file['tmp_name']);
    if (!isset(AVATAR_ALLOWED[$mime])) {
        return null;                     // 허용 목록에 없는 종류 → 거부
    }
    $ext = AVATAR_ALLOWED[$mime];        // 우리가 정한 확장자 (사용자 것 안 씀)

    // ── ④ 파일명은 우리가 새로 짓는다 ────────────────────────
    //   사용자가 준 이름(../../evil 같은 경로 공격)을 절대 쓰지 않는다.
    //   'user<번호>.<확장자>' → 회원마다 하나. (새로 올리면 덮어써짐)
    $filename = 'user' . $userId . '.' . $ext;
    $destPath = AVATAR_DIR . '/' . $filename;

    // ── ⑤ 임시 파일을 최종 위치로 옮긴다 ────────────────────
    //   move_uploaded_file: 업로드 임시파일을 목적지로 이동. (실패하면 false)
    if (!move_uploaded_file($file['tmp_name'], $destPath)) {
        return null;
    }

    // 화면에서 쓸 주소를 돌려준다.
    //   ?v=시각 을 붙여 캐시 무력화 (같은 이름으로 덮어써도 브라우저가 새 이미지를 받게)
    return AVATAR_URL_BASE . '/' . $filename . '?v=' . filemtime($destPath);
}
