<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

// ============================================================
// ConfirmPasswordController — 민감한 화면에 들어가기 전 비밀번호 재확인
//   지금 settings/reauth.php · confirm.php 에 해당한다.
//
//   ★ 왜 필요한가 — 로그인한 채로 자리를 비운 사이 누가 설정을 바꿔 버릴 수 있다.
//     그래서 '로그인했나'와 별개로 '지금 본인이 맞나'를 한 번 더 묻는다.
//   ★ 한 번 확인하면 config/auth.php 의 password_timeout(15분) 동안 다시 묻지 않는다.
// ============================================================
class ConfirmPasswordController extends Controller
{
    public function create()
    {
        return view('auth.confirm-password');
    }

    public function store(Request $request)
    {
        $request->validate([
            'password' => ['required', 'current_password'],
        ], ['password.current_password' => '비밀번호가 일치하지 않습니다.']);

        // ★ 이 한 줄이 '방금 확인했다'를 세션에 기록한다.
        //   password.confirm 미들웨어가 이 값을 보고 통과시킨다.
        $request->session()->put('auth.password_confirmed_at', time());

        return redirect()->intended('/settings');
    }
}
