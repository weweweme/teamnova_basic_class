<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;

// ============================================================
// RegisterController — 회원가입 화면 · 처리
//   지금 auth/signup.php(화면) · auth/register.php(처리) 두 파일에 해당한다.
// ============================================================
class RegisterController extends Controller
{
    public function create()
    {
        return view('auth.register');
    }

    public function store(Request $request)
    {
        // ── 입력값 검사 ─────────────────────────────────────
        //   ★ unique:users,username 한 마디가 '이미 있는 아이디인가' 검사를 대신한다.
        //     지금은 find_user() 로 직접 조회해서 if 로 걸렀다.
        //     검사에 실패하면 폼으로 되돌아가고, 입력값과 오류 문구가 함께 남는다.
        //
        //   규칙은 기존과 같게 맞췄다 — 아이디 20자 이내, 비밀번호 4자 이상.
        $data = $request->validate([
            'username' => ['required', 'string', 'max:20', 'unique:users,username'],
            'password' => ['required', 'string', 'min:4'],
        ], [
            // 기본 문구 대신 우리 말투로 바꾼다 ('필드' 같은 말이 안 나오게)
            'username.unique' => '이미 있는 아이디입니다.',
            'password.min'    => '비밀번호는 4자 이상으로 정해 주세요.',
        ]);

        // ── 저장 ────────────────────────────────────────────
        //   ★ 비밀번호를 해시하는 코드가 없다.
        //     User 모델의 casts()에 'password' => 'hashed' 를 걸어 둬서 저장할 때 알아서 해시된다.
        //   ★ joined_at 도 적지 않는다. CREATED_AT = 'joined_at' 이라 Eloquent 가 채운다.
        //   닉네임은 기존 동작대로 아이디와 같게 시작한다 (설정에서 바꾼다).
        User::create([
            'username' => $data['username'],
            'nickname' => $data['username'],
            'password' => $data['password'],
        ]);

        // 기존 동작과 같게 — 가입 후 자동 로그인하지 않고 로그인 화면으로 보낸다.
        return redirect('/login')->with('status', '회원가입이 완료되었습니다. 로그인해 주세요.');
    }
}
