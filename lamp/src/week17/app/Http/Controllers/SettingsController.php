<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\Rules\Password;

// ============================================================
// SettingsController — 내 설정 (닉네임 · 비밀번호 · 다른 기기 로그아웃)
//   지금 settings/index.php · nickname.php · password.php · logout_all.php 에 해당한다.
// ============================================================
class SettingsController extends Controller
{
    public function index()
    {
        return view('settings.index');
    }

    // ── 닉네임 변경 ─────────────────────────────────────────
    public function nickname(Request $request)
    {
        $data = $request->validate([
            'nickname' => ['required', 'string', 'max:20'],
        ]);

        $request->user()->update($data);

        return back()->with('status', '닉네임이 변경되었습니다.');
    }

    // ── 비밀번호 변경 ───────────────────────────────────────
    public function password(Request $request)
    {
        $data = $request->validate([
            // ★ 'current_password' 규칙 = 로그인한 사람의 지금 비밀번호와 맞는지 확인한다.
            //   지금은 verify_login() 으로 다시 조회해서 비교하던 자리다.
            'current' => ['required', 'current_password'],
            'new'     => ['required', Password::min(4), 'confirmed'],
        ], [
            'current.current_password' => '현재 비밀번호가 일치하지 않습니다.',
            'new.confirmed'            => '새 비밀번호 확인이 일치하지 않습니다.',
        ]);

        // 모델의 'hashed' 캐스트가 저장할 때 알아서 해시한다
        $request->user()->update(['password' => $data['new']]);

        // ★ 비밀번호를 바꿨으면 다른 기기의 로그인은 끊는 것이 맞다.
        //   이 기기만 남기고 나머지 세션을 무효화한다 — Laravel 내장이다.
        Auth::logoutOtherDevices($data['new']);

        return back()->with('status', '비밀번호를 변경했습니다. 다른 기기의 로그인은 해제되었습니다.');
    }

    // ── 다른 기기 로그아웃 (비밀번호 변경 없이) ─────────────
    public function logoutOtherDevices(Request $request)
    {
        $data = $request->validate([
            'password' => ['required', 'current_password'],
        ], [
            'password.current_password' => '비밀번호가 일치하지 않습니다.',
        ]);

        // ★ 내장 기능. "지금 이 기기는 유지하고 나머지만 끊는다".
        //   지금 destroy_other_sessions() 가 sessions 표를 직접 지우던 일이다.
        Auth::logoutOtherDevices($data['password']);

        return back()->with('status', '다른 기기의 로그인을 모두 해제했습니다.');
    }
}
