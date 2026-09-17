<?php

namespace App\Services;

use App\Models\User;
use Illuminate\Support\Facades\DB;

// ============================================================
// Accounts — 탈퇴한 계정을 다루는 규칙이 모인 곳
//
//   ★ 왜 한 곳에 모으나 —
//     '개인정보를 실제로 지운다'는 일이 두 군데서 일어난다.
//       · 유예 기간이 지나 예약 작업이 도는 경우
//       · 사용자가 '지금 완전히 삭제'를 고른 경우
//     같은 일을 두 벌 적으면 한쪽만 고치는 날이 오고, 그때부터 두 경로의 결과가 달라진다.
//     무엇을 지우는지는 여기 한 곳에만 적는다.
//
//   ★ 컨트롤러가 아니라 서비스에 두는 이유 —
//     예약 작업(artisan 명령)에는 요청도 화면도 없다. 컨트롤러를 부르게 하면
//     '화면을 위한 코드'와 '규칙'이 섞인다.
// ============================================================
class Accounts
{
    // 유예 기간 — 이 안에 다시 로그인하면 되돌릴 수 있다
    public const GRACE_DAYS = 30;

    // ── 되돌리기 ────────────────────────────────────────────
    //   개인정보를 이미 지운 계정은 되돌릴 수 없다.
    public function restore(User $user): bool
    {
        if (! $user->trashed() || $user->anonymized_at) {
            return false;
        }

        $user->restore();

        return true;
    }

    // 유예 기간 안에 있는가 = 되돌릴 수 있는가
    public function restorable(User $user): bool
    {
        return $user->trashed() && ! $user->anonymized_at;
    }

    // 되돌릴 수 있는 날이 며칠 남았나
    public function daysLeft(User $user): int
    {
        if (! $user->trashed()) {
            return 0;
        }

        return max(0, self::GRACE_DAYS - (int) $user->deleted_at->diffInDays(now()));
    }

    // ── 개인정보 지우기 ─────────────────────────────────────
    //   ★ 줄 자체는 지우지 않는다 —
    //     글과 댓글을 남기기로 했고(대화가 끊기지 않게), 글은 작성자를 가리켜야 한다.
    //     DB 도 같은 말을 한다 — posts.author_id 가 RESTRICT 라 글이 있는 회원은 지워지지 않는다.
    //     개인정보를 비운 줄은 더 이상 누구의 것도 아니다. 그것이 목적이다.
    public function erase(User $user): void
    {
        DB::transaction(function () use ($user) {
            // 얼굴 사진이 남아 있으면 지운 것이 아니다.
            if ($user->avatar) {
                $path = public_path('uploads/avatars/' . basename($user->avatar));

                if (is_file($path)) {
                    @unlink($path);
                }
            }

            // 아이디는 비울 수 없다(NOT NULL · unique) → 누구인지 알 수 없는 값으로 바꾼다.
            $user->forceFill([
                'username'          => 'deleted_' . $user->id,
                'nickname'          => '탈퇴한 사용자',
                'email'             => null,
                'email_verified_at' => null,
                'google_id'         => null,
                'avatar'            => null,
                'notify_activity'   => false,
                'password'          => bcrypt(bin2hex(random_bytes(32))),
                'anonymized_at'     => now(),
            ])->save();

            // 이 사람 앞으로 온 알림에는 '누가 누구에게' 가 남는다.
            DB::table('notifications')->where('user_id', $user->id)->delete();

            // 로그인·기기·초안 흔적도 남기지 않는다.
            DB::table('sessions')->where('user_id', $user->id)->delete();
            DB::table('user_devices')->where('user_id', $user->id)->delete();
            DB::table('drafts')->where('user_id', $user->id)->delete();
        });
    }
}
