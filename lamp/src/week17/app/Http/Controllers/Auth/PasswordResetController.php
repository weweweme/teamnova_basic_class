<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Password;
use Illuminate\Validation\Rules\Password as PasswordRule;
use Illuminate\Validation\ValidationException;

// ============================================================
// PasswordResetController — 비밀번호 찾기
//   레거시에는 없던 기능이다. 우리가 만든 코드는 화면 두 개와 이 파일뿐이고,
//   토큰 발급 · 메일 발송 · 만료 확인 · 재설정은 전부 프레임워크가 한다.
//
//   [흐름]
//     ① 이메일을 적는다        → Password::sendResetLink()
//     ② 메일의 링크로 들어온다 → 토큰이 들어 있는 주소
//     ③ 새 비밀번호를 적는다   → Password::reset()
//
//   ★ 우리가 password_reset_tokens 표를 직접 읽거나 쓰지 않는다.
//     토큰은 해시로 저장되고, 한 번 쓰면 지워지며, 기본 60분이 지나면 무효가 된다.
// ============================================================
class PasswordResetController extends Controller
{
    // ── ① 이메일을 받는 화면 (GET /forgot-password) ─────────
    public function request()
    {
        return view('auth.forgot-password');
    }

    // ── ① 링크 보내기 (POST /forgot-password) ───────────────
    public function send(Request $request)
    {
        $request->validate(['email' => ['required', 'email']]);

        // ★ 결과를 그대로 보여주지 않는다. "그 주소는 가입되어 있지 않다"고 알려 주면
        //   남의 가입 여부를 확인하는 도구가 된다(계정 열거). 항상 같은 문구로 답한다.
        Password::sendResetLink($request->only('email'));

        return back()->with('status', '메일을 보냈습니다. 받은 편지함을 확인해 주세요.');
    }

    // ── ② 새 비밀번호를 받는 화면 (GET /reset-password/{token}) ──
    public function edit(Request $request, string $token)
    {
        return view('auth.reset-password', [
            'token' => $token,
            'email' => (string) $request->query('email', ''),
        ]);
    }

    // ── ③ 실제로 바꾸기 (POST /reset-password) ──────────────
    public function update(Request $request)
    {
        $request->validate([
            'token'    => ['required'],
            'email'    => ['required', 'email'],
            'password' => ['required', 'confirmed', PasswordRule::min(4)],
        ]);

        // ★ 토큰 대조와 만료 확인은 이 한 줄 안에서 끝난다.
        //   통과하면 클로저가 불리고, 거기서 우리 규칙(비밀번호 저장 방식)만 적는다.
        $result = Password::reset(
            $request->only('email', 'password', 'password_confirmation', 'token'),
            function ($user, string $password) {
                // ★ remember_token 은 우리 표에 없다. 자동 로그인을 의도적으로 뺐기 때문이다.
                //   기본 예제는 이 자리에서 그 칸도 갱신하는데, 우리는 비밀번호만 바꾼다.
                $user->forceFill(['password' => Hash::make($password)])->save();
            }
        );

        if ($result !== Password::PasswordReset) {
            throw ValidationException::withMessages([
                'email' => '링크가 만료되었거나 올바르지 않습니다. 다시 요청해 주세요.',
            ]);
        }

        return redirect('/login')->with('status', '비밀번호를 바꿨습니다. 새 비밀번호로 로그인해 주세요.');
    }
}
