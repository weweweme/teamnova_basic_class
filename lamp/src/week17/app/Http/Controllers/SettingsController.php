<?php

namespace App\Http\Controllers;

use App\Models\Device;
use App\Services\DeviceTracker;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\Rules\Password;

// ============================================================
// SettingsController — 내 설정 (닉네임 · 비밀번호 · 다른 기기 로그아웃)
//   지금 settings/index.php · nickname.php · password.php · logout_all.php 에 해당한다.
// ============================================================
class SettingsController extends Controller
{
    public function index(Request $request, DeviceTracker $devices)
    {
        return view('settings.index', [
            'devices'      => Device::where('user_id', $request->user()->id)
                                    ->orderByDesc('last_login_at')->get(),
            'thisDeviceId' => $devices->deviceId($request),
        ]);
    }

    // ── 기기 하나 끊기 ──────────────────────────────────────
    //   ★ 목록에서 지우는 것만으로는 부족하다. 그 기기의 세션까지 함께 끊어야
    //     '끊었다'는 말이 사실이 된다. (DeviceTracker::revoke 가 둘 다 한다)
    public function revokeDevice(Request $request, DeviceTracker $devices)
    {
        // ★ 비밀번호를 다시 묻지 않는다. 이 주소는 password.confirm 뒤에 있어서
        //   '방금 본인 확인을 마친 상태'에서만 닿을 수 있다 (15분 유효).
        $data = $request->validate([
            'device_id' => ['required', 'string'],
        ]);

        $ok = $devices->revoke($request->user()->id, $data['device_id']);

        return back()->with('status', $ok ? '기기를 해제했습니다.' : '해제할 수 없는 기기입니다.');
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
