<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Services\Accounts;
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
    // 탈퇴 대기 중인 계정으로 로그인했을 때, 선택 화면까지 들고 가는 자리
    public const PENDING_KEY = 'account_pending_restore';

    // ── 탈퇴 화면 (GET /settings/leave) ─────────────────────
    public function edit(Request $request)
    {
        return view('settings.leave', [
            'me'    => $request->user(),
            'posts' => $request->user()->posts()->count(),
            'days'  => Accounts::GRACE_DAYS,
        ]);
    }

    // ── 탈퇴 처리 (DELETE /settings/leave) ──────────────────
    public function destroy(Request $request, Accounts $accounts)
    {
        $user = $request->user();

        // ★ 아이디를 직접 입력하게 한다.
        //   되돌리기 어려운 동작 앞에서 손을 한 번 멈추게 하는 장치다(GitHub 이 저장소를
        //   지울 때 쓰는 방식). 본인 확인은 이미 password.confirm 이 했다 —
        //   이건 '남이 하는 것'이 아니라 '내가 실수로 하는 것'을 막는다.
        $data = $request->validate([
            'username' => ['required', 'string'],
            // 'grace'  = 30일 뒤에 지운다 (되돌릴 수 있다)
            // 'now'    = 지금 바로 지운다 (되돌릴 수 없다)
            'mode'     => ['required', 'in:grace,now'],
        ]);

        if ($data['username'] !== $user->username) {
            throw ValidationException::withMessages([
                'username' => '아이디가 일치하지 않습니다.',
            ]);
        }

        $now = $data['mode'] === 'now';

        DB::transaction(function () use ($user, $request, $accounts, $now) {
            // ① 탈퇴 표시 — 줄은 남고 화면에서만 사라진다
            $user->delete();

            if ($now) {
                // ★ 바로 지우기를 고른 경우. 유예 기간을 기다리지 않는다.
                //   무엇을 지우는지는 Accounts 한 곳에만 적혀 있다 —
                //   예약 작업이 30일 뒤에 하는 일과 정확히 같은 일을 지금 한다.
                $accounts->erase($user);
            } else {
                // ② 로그인 중인 모든 기기를 끊는다.
                //   ★ 이걸 빼면 탈퇴했는데 다른 창에서는 계속 로그인 상태다.
                //     sessions 는 users 를 CASCADE 로 참조하지만 줄을 지우지 않았으므로
                //     자동으로 사라지지 않는다 — 직접 지운다.
                DB::table('sessions')->where('user_id', $user->id)->delete();

                // ③ 기기 목록과 초안도 지금 정리한다. 되돌릴 때 필요한 값이 아니다.
                DB::table('user_devices')->where('user_id', $user->id)->delete();
                DB::table('drafts')->where('user_id', $user->id)->delete();
            }

            Auth::logout();
            $request->session()->invalidate();
            $request->session()->regenerateToken();
        });

        return redirect('/')->with('status', $now
            ? '계정을 삭제했습니다. 쓰신 글은 남고 작성자만 가려집니다.'
            : '탈퇴했습니다. ' . Accounts::GRACE_DAYS . '일 안에 다시 로그인하시면 계정이 되돌아옵니다.');
    }

    // ── 탈퇴 대기 중인 계정으로 로그인했을 때 (GET /account/restore) ──
    //   ★ 조용히 되돌리지 않는다.
    //     저장된 비밀번호로 습관처럼 로그인하거나 구글 버튼을 무심코 누르면
    //     본인은 지운 줄 아는 계정이 말없이 살아난다. 사용자가 요청한 삭제가
    //     본인도 모르게 취소되는 것이라, 한 번 묻고 고르게 한다.
    public function restoreForm(Request $request, Accounts $accounts)
    {
        $user = $this->pendingUser($request);

        if (! $user) {
            return redirect('/login');
        }

        return view('account.restore', [
            'user' => $user,
            'left' => $accounts->daysLeft($user),
        ]);
    }

    // ── 고른 대로 처리 (POST /account/restore) ──────────────
    public function restoreSubmit(Request $request, Accounts $accounts)
    {
        $user = $this->pendingUser($request);

        if (! $user) {
            return redirect('/login');
        }

        $data = $request->validate(['choice' => ['required', 'in:restore,erase']]);

        $request->session()->forget(self::PENDING_KEY);

        if ($data['choice'] === 'erase') {
            $accounts->erase($user);

            return redirect('/')->with('status', '계정을 삭제했습니다. 쓰신 글은 남고 작성자만 가려집니다.');
        }

        $accounts->restore($user);
        Auth::login($user);
        $request->session()->regenerate();

        return redirect('/')->with('status', $user->nickname . '님, 계정이 복구되었습니다.');
    }

    // 로그인에 성공했지만 탈퇴 대기 중이라 아직 들여보내지 않은 사람
    private function pendingUser(Request $request): ?User
    {
        $id = $request->session()->get(self::PENDING_KEY);

        if (! $id) {
            return null;
        }

        $user = User::withTrashed()->find($id);

        return $user && $user->trashed() && ! $user->anonymized_at ? $user : null;
    }
}
