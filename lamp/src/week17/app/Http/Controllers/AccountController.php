<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\ValidationException;

// ============================================================
// AccountController — 회원 탈퇴
//
//   ★ 줄을 지우지 않는다(소프트 삭제) —
//     · 글과 댓글은 남긴다. 커뮤니티 글은 혼자 쓴 것이 아니라 대화라서,
//       지우면 남의 글에 달린 흐름이 중간에 끊긴다. (Reddit 도 같은 방식이다)
//     · 그래서 글은 작성자를 계속 가리켜야 하고, 가리킬 줄이 있어야 한다.
//     · DB 도 같은 말을 한다 — posts.author_id 가 RESTRICT 라
//       글이 있는 회원은 애초에 삭제되지 않는다.
//
//   ★ 두 단계로 나눈다 —
//     지금        deleted_at 을 찍는다. 화면에서 사라지고 로그인이 막힌다. 되돌릴 수 있다.
//     30일 뒤     예약 작업이 아이디·이메일·구글 연결·아바타를 실제로 비운다. 되돌릴 수 없다.
//
//     deleted_at 만 찍고 끝내면 '감춘 것'이지 '지운 것'이 아니다.
//     개인정보는 DB 에 그대로 읽을 수 있게 남아 있기 때문이다.
// ============================================================
class AccountController extends Controller
{
    // 유예 기간 — 이 안에 다시 로그인하면 되돌아온다
    public const GRACE_DAYS = 30;

    // ── 탈퇴 화면 (GET /settings/leave) ─────────────────────
    public function edit(Request $request)
    {
        return view('settings.leave', [
            'me'    => $request->user(),
            'posts' => $request->user()->posts()->count(),
            'days'  => self::GRACE_DAYS,
        ]);
    }

    // ── 탈퇴 처리 (DELETE /settings/leave) ──────────────────
    public function destroy(Request $request)
    {
        $user = $request->user();

        // ★ 아이디를 직접 입력하게 한다.
        //   되돌리기 어려운 동작 앞에서 손을 한 번 멈추게 하는 장치다(GitHub 이 저장소를
        //   지울 때 쓰는 방식). 본인 확인은 이미 password.confirm 이 했다 —
        //   이건 '남이 하는 것'이 아니라 '내가 실수로 하는 것'을 막는다.
        $request->validate([
            'username' => ['required', 'string'],
        ]);

        if ($request->input('username') !== $user->username) {
            throw ValidationException::withMessages([
                'username' => '아이디가 일치하지 않습니다.',
            ]);
        }

        DB::transaction(function () use ($user, $request) {
            // ① 탈퇴 표시 — 줄은 남고 화면에서만 사라진다
            $user->delete();

            // ② 로그인 중인 모든 기기를 끊는다.
            //   ★ 이걸 빼면 탈퇴했는데 다른 창에서는 계속 로그인 상태다.
            //     sessions 는 users 를 CASCADE 로 참조하지만 줄을 지우지 않았으므로
            //     자동으로 사라지지 않는다 — 직접 지운다.
            DB::table('sessions')->where('user_id', $user->id)->delete();

            // ③ 기기 목록과 초안도 지금 정리한다. 되돌릴 때 필요한 값이 아니다.
            DB::table('user_devices')->where('user_id', $user->id)->delete();
            DB::table('drafts')->where('user_id', $user->id)->delete();

            Auth::logout();
            $request->session()->invalidate();
            $request->session()->regenerateToken();
        });

        return redirect('/')->with('status',
            '탈퇴했습니다. ' . self::GRACE_DAYS . '일 안에 다시 로그인하시면 계정이 되돌아옵니다.');
    }

    // ── 되돌리기 ────────────────────────────────────────────
    //   ★ 로그인에 성공한 순간 부른다. 유예 기간 안이면 탈퇴를 취소한다.
    //     '다시 로그인하면 돌아온다'는 넷플릭스 등이 쓰는 방식이고,
    //     사용자에게 따로 설명할 것이 없다는 점이 장점이다.
    public static function restoreIfWithinGrace(User $user): bool
    {
        if (! $user->trashed() || $user->anonymized_at) {
            return false;
        }

        $user->restore();

        return true;
    }
}
