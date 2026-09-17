<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Notifications\PasswordChanged;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\Rules\Password as PasswordRule;

// ============================================================
// RecoveryQuestionController — 질문과 답으로 비밀번호 찾기
//
//   ★ 왜 있나 — 이메일은 선택이다. 주소를 넣지 않은 회원에게는
//     메일로 링크를 보내는 길이 없다. 이 통로가 없으면 계정을 영영 잃는다.
//
//   ⚠ 이 방식은 메일보다 약하다 —
//     답이 짧고 추측 가능한 경우가 많고, 비밀번호를 우회하는 두 번째 문이 된다.
//     그래서 아래 장치를 함께 둔다.
//       · 답은 해시로만 저장한다 (비밀번호와 같은 취급)
//       · 시도 횟수를 제한한다 (라우트의 throttle) — 답을 하나씩 넣어 보는 것을 막는다
//       · 성공하면 다른 기기의 로그인을 전부 끊는다
//       · 이메일이 있으면 '비밀번호가 바뀌었다'고 알린다 — 내가 한 일이 아니면 알아챌 통로
//
//   ★ 두 걸음으로 나눈다 —
//     ① 아이디를 넣으면 그 계정의 질문을 보여 준다
//     ② 답과 새 비밀번호를 함께 받는다
//
//     ⚠ ①에서 '질문이 있다/없다'가 드러난다 = 그 아이디가 있다는 뜻이 된다.
//       메일 쪽은 이것을 감췄지만(같은 안내를 보여준다), 여기서는 감출 수 없다 —
//       질문을 보여주지 않으면 답할 수가 없기 때문이다.
//       대신 시도 횟수를 제한해 아이디를 훑는 데 쓰이지 못하게 한다.
// ============================================================
class RecoveryQuestionController extends Controller
{
    // ── ① 아이디 넣는 화면 (GET /forgot-password/question) ──
    public function ask()
    {
        return view('auth.recovery-ask');
    }

    // ── ① 질문 보여주기 (POST /forgot-password/question) ────
    public function show(Request $request)
    {
        $data = $request->validate(['username' => ['required', 'string', 'max:20']]);

        $user = User::where('username', $data['username'])->first();

        if (! $user || ! $user->recovery_question) {
            throw ValidationException::withMessages([
                'username' => '그 아이디로는 질문 찾기를 쓸 수 없습니다. 질문을 정해 두지 않았거나 없는 아이디입니다.',
            ]);
        }

        return view('auth.recovery-answer', ['user' => $user]);
    }

    // ── ② 답과 새 비밀번호 (POST /forgot-password/question/verify) ──
    public function update(Request $request)
    {
        $user = User::where('username', (string) $request->input('username'))->first();

        // ★ 틀렸을 때 '아이디 넣는 화면'으로 되돌리지 않는다.
        //   답 화면은 POST 로 그려진 것이라 back() 이 갈 자리가 없다. 그대로 두면
        //   사용자는 처음 화면으로 튕기고 왜 안 됐는지도 알 수 없다.
        //   그래서 답 화면을 다시 그리면서 오류를 함께 실어 보낸다.
        $fail = function (string $field, string $message) use ($user, $request) {
            // ★ withErrors() 는 View 에 있는 것이고 Response 에는 없다.
            //   response()->view(...) 로 감싸면 그 메서드를 잃는다. view() 를 그대로 돌려준다.
            return view('auth.recovery-answer', ['user' => $user])
                ->withErrors([$field => $message]);
        };

        if (! $user || ! $user->recovery_question) {
            return redirect('/forgot-password/question')
                ->withErrors(['username' => '그 아이디로는 질문 찾기를 쓸 수 없습니다.']);
        }

        $data = $request->validate([
            'username' => ['required', 'string', 'max:20'],
            'answer'   => ['required', 'string', 'max:100'],
            'password' => ['required', 'string', PasswordRule::min(4), 'confirmed'],
        ], [
            'password.confirmed' => '새 비밀번호 확인이 일치하지 않습니다.',
        ]);

        // ★ 답이 틀렸을 때와 답을 정해 두지 않았을 때 같은 문구를 쓴다.
        //   문구가 다르면 그 차이만으로 상태를 알아낼 수 있다.
        if (! $user->recovery_answer || ! Hash::check(self::normalize($data['answer']), $user->recovery_answer)) {
            return $fail('answer', '답이 일치하지 않습니다.');
        }

        $user->update(['password' => $data['password']]);

        // 이 기기만 남기는 것이 아니라 전부 끊는다 — 지금 로그인한 상태가 아니기 때문이다.
        \Illuminate\Support\Facades\DB::table('sessions')->where('user_id', $user->id)->delete();

        // 보안 알림. 내가 한 일이 아니라면 이 메일이 알아챌 통로가 된다.
        if ($user->email && $user->hasVerifiedEmail()) {
            $user->notify(new PasswordChanged());
        }

        return redirect('/login')->with('status', '비밀번호를 새로 정했습니다. 로그인해 주세요.');
    }

    // ── 답 맞추기 전 정리 ───────────────────────────────────
    //   ★ 사람은 같은 답을 매번 똑같이 치지 않는다. 앞뒤 공백, 대소문자, 사이 공백이 달라진다.
    //     저장할 때와 맞출 때 같은 방식으로 다듬어야 한다.
    //     이 함수는 설정 화면에서도 부르므로 public 이다.
    public static function normalize(string $answer): string
    {
        return mb_strtolower(preg_replace('/\s+/u', ' ', trim($answer)));
    }
}
