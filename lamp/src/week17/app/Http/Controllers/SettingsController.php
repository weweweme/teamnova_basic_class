<?php

namespace App\Http\Controllers;

use App\Models\Device;
use App\Services\DeviceTracker;
use App\Notifications\PasswordChanged;
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

        // 보안 알림 — 내가 바꾼 것이 아니라면 이 메일이 알아챌 통로가 된다
        if ($request->user()->email) {
            $request->user()->notify(new PasswordChanged());
        }

        return back()->with('status', '비밀번호를 변경했습니다. 다른 기기의 로그인은 해제되었습니다.');
    }

    // ── 이메일 주소 (PATCH /settings/email) ──────────────────
    //   ★ 주소를 적는 일과 알림을 켜는 일을 갈라 두었다.
    //     주소는 '인증'을 거쳐야 쓸 수 있고, 알림은 그 뒤에 켜는 것이라 순서가 있다.
    //     한 폼에 묶어 두면 저장을 눌렀을 때 무엇이 일어났는지 알기 어렵다.
    public function email(Request $request)
    {
        $user = $request->user();

        $data = $request->validate([
            'email' => ['nullable', 'email', 'max:100', 'unique:users,email,' . $user->id],
        ]);

        $email = $data['email'] ?: null;

        // ① 주소를 지웠다 — 인증 기록과 알림도 같이 내린다
        if ($email === null) {
            $user->forceFill([
                'email'             => null,
                'email_verified_at' => null,
                'notify_activity'   => false,
            ])->save();

            return back()->with('status', '이메일 주소를 지웠습니다. 활동 알림도 함께 껐습니다.');
        }

        // ② 다른 주소로 바꿨다 — 인증을 처음부터 다시 받는다
        //   ★ 옛 주소를 인증했다고 해서 새 주소의 주인이라는 뜻은 아니다.
        if ($email !== $user->email) {
            $user->forceFill([
                'email'             => $email,
                'email_verified_at' => null,
                'notify_activity'   => false,
            ])->save();

            $user->sendEmailVerificationNotification();

            return back()->with('status', $email . ' 로 인증 메일을 보냈습니다. 메일 속 링크를 눌러 주세요.');
        }

        // ③ 같은 주소인데 이미 인증했다 — 할 일이 없다
        if ($user->hasVerifiedEmail()) {
            return back()->with('status', '이미 인증된 주소입니다.');
        }

        // ④ 같은 주소인데 아직 인증 전이다 — 메일을 다시 보낸다
        $user->sendEmailVerificationNotification();

        return back()->with('status', $email . ' 로 인증 메일을 다시 보냈습니다.');
    }

    // ── 활동 알림 켜고 끄기 (PATCH /settings/notifications) ───
    //   ★ 인증을 마친 주소에서만 켤 수 있다. 보안 알림은 여기 없다(끌 수 없다).
    public function notifications(Request $request)
    {
        $user = $request->user();

        $data = $request->validate([
            'notify_activity' => ['nullable', 'boolean'],
        ]);

        $wants = (bool) ($data['notify_activity'] ?? false);

        // ★ 화면에서도 막지만, 판정은 서버가 다시 한다.
        if ($wants && ! $user->hasVerifiedEmail()) {
            return back()->withErrors([
                'notify_activity' => '이메일 인증을 마친 뒤에 켤 수 있습니다.',
            ]);
        }

        $user->update(['notify_activity' => $wants]);

        return back()->with('status', $wants ? '이제 댓글 알림 메일을 보내 드립니다.' : '활동 알림을 껐습니다.');
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
