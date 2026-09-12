<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

// ============================================================
// EnsureNotIdle — 일정 시간 아무 동작이 없으면 자동 로그아웃
//   지금 includes/auth.php 의 IDLE_LIMIT · last_seen 판정에 해당한다.
//
//   ★ 판정은 서버가 한다. 화면의 카운트다운은 '보여주기'일 뿐이다.
//     브라우저 시계는 사용자가 얼마든지 바꿀 수 있기 때문이다.
//
//   ★ 유휴 한도(20분)는 세션 수명(30분)보다 반드시 짧아야 한다.
//     반대면 세션이 먼저 끊겨서 '유휴 로그아웃 안내'를 띄울 기회가 없다.
// ============================================================
class EnsureNotIdle
{
    private const KEY = 'last_seen';

    public function handle(Request $request, Closure $next)
    {
        if (! Auth::check()) {
            return $next($request);
        }

        $limit = (int) config('auth.idle_timeout');
        $last  = $request->session()->get(self::KEY);

        if ($last !== null && time() - $last >= $limit) {
            Auth::logout();
            $request->session()->invalidate();
            $request->session()->regenerateToken();

            return redirect('/login')->with(
                'status',
                intdiv($limit, 60) . '분 동안 움직임이 없어 자동으로 로그아웃되었습니다.'
            );
        }

        // 움직였으므로 시계를 다시 맞춘다
        $request->session()->put(self::KEY, time());

        return $next($request);
    }
}
