<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Services\Consent;
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

    public function store(Request $request, Consent $consent)
    {
        // ── 입력값 검사 ─────────────────────────────────────
        //   ★ unique:users,username 한 마디가 '이미 있는 아이디인가' 검사를 대신한다.
        //     지금은 find_user() 로 직접 조회해서 if 로 걸렀다.
        //     검사에 실패하면 폼으로 되돌아가고, 입력값과 오류 문구가 함께 남는다.
        //
        //   규칙은 기존과 같게 맞췄다 — 아이디 20자 이내, 비밀번호 4자 이상.
        $data = $request->validate([
            // ★ unique 는 모델이 아니라 표를 직접 본다. 그래서 탈퇴한 회원의 아이디도
            //   '이미 있음'으로 친다 — 유예 기간 안에 돌아올 수 있으니 그게 맞는 동작이다.
            'username' => ['required', 'string', 'max:20', 'unique:users,username'],
            // 'confirmed' = password_confirmation 칸과 같은지 본다. 칸 이름이 규칙이다.
            'password' => ['required', 'string', 'min:4', 'confirmed'],
            // 'accepted' = 체크되어 있어야 통과한다. 체크 안 하면 여기서 멈춘다.
            'agree'    => ['accepted'],
        ], [
            // 기본 문구 대신 우리 말투로 바꾼다 ('필드' 같은 말이 안 나오게)
            'username.unique'    => '이미 있는 아이디입니다.',
            'password.min'       => '비밀번호는 4자 이상으로 정해 주세요.',
            'password.confirmed' => '비밀번호 확인이 일치하지 않습니다.',
            'agree.accepted'     => '이용약관과 개인정보처리방침에 동의해 주세요.',
        ]);

        // ── 저장 ────────────────────────────────────────────
        //   ★ 비밀번호를 해시하는 코드가 없다.
        //     User 모델의 casts()에 'password' => 'hashed' 를 걸어 둬서 저장할 때 알아서 해시된다.
        //   ★ joined_at 도 적지 않는다. CREATED_AT = 'joined_at' 이라 Eloquent 가 채운다.
        //   닉네임은 기존 동작대로 아이디와 같게 시작한다 (설정에서 바꾼다).
        $user = User::create([
            'username' => $data['username'],
            'nickname' => $data['username'],
            'password' => $data['password'],
        ]);

        // ★ 동의를 받았다는 사실을 남긴다.
        //   체크박스만 두고 기록하지 않으면 '동의를 받았다'고 말할 근거가 없다.
        //   쿠키 동의와 같은 표에 쌓인다 — source 로 어디서 온 동의인지 구분한다.
        $consent->recordTerms($request, $user->id, 'signup');

        // 기존 동작과 같게 — 가입 후 자동 로그인하지 않고 로그인 화면으로 보낸다.
        return redirect('/login')->with('status', '회원가입이 완료되었습니다. 로그인해 주세요.');
    }
}
