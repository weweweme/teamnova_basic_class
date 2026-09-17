<?php

namespace App\Http\Controllers;

use App\Models\User;

// ============================================================
// UnsubscribeController — 메일 속 '알림 그만 받기' 링크
//
//   ★ 로그인하지 않고도 눌러야 한다. 메일을 받은 사람은 보통 로그인 상태가 아니다.
//     그렇다고 주소에 회원 번호만 적으면 남의 알림을 아무나 꺼 버릴 수 있다.
//
//   ★ 그래서 서명된 주소를 쓴다. 주소를 만들 때 라라벨이 서명을 붙이고,
//     들어올 때 signed 미들웨어가 그 서명을 확인한다.
//     번호를 하나라도 고치면 서명이 깨져서 403 으로 막힌다. 확인 코드는 우리가 쓰지 않는다.
// ============================================================
class UnsubscribeController extends Controller
{
    public function __invoke(User $user)
    {
        // 보안 알림(새 기기 로그인 · 비밀번호 변경)은 그대로 둔다. 활동 알림만 끈다.
        $user->update(['notify_activity' => false]);

        return view('notifications.unsubscribed', ['user' => $user]);
    }
}
