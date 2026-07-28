<?php
// ============================================================
// settings/nickname.php — 닉네임(표시 이름) 변경  [POST → PRG]
//   아이디(로그인 키)는 그대로 두고 nickname만 바꾼다.
// ============================================================
require_once __DIR__ . '/../includes/util.php';
require_once __DIR__ . '/../includes/auth.php';

require_login();   // 로그인 본인만

// POST로 온 게 아니면(주소 직접 입력 등) 설정으로 되돌린다
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Location: /settings.php');
    exit;
}

$nickname = trim(post_str('nickname'));

// 검증: 1~20자 (닉네임은 표시용이라 중복은 허용 — 신원은 아이디가 담당)
if ($nickname === '' || mb_strlen($nickname) > 20) {
    set_flash('닉네임은 1~20자로 입력해 주세요.', 'error');
    header('Location: /settings.php');
    exit;
}

set_nickname(current_user_id(), $nickname);

set_flash('✏️ 닉네임이 변경되었습니다.');
header('Location: /settings.php');
exit;
