<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;

// ============================================================
// SessionPingController — "아직 쓰고 있어요" 신호
//   지금 session/ping.php 에 해당한다.
//
//   ★ 왜 필요한가 — 글을 길게 쓰는 동안에는 서버로 가는 요청이 없다.
//     그대로 두면 타이핑 중에 자동 로그아웃된다.
//     그래서 타이핑을 '활동'으로 세어 주기적으로 이 주소를 두드린다.
//   ※ 이 요청 자체가 EnsureNotIdle 을 지나므로 시계는 그 미들웨어가 갱신한다.
// ============================================================
class SessionPingController extends Controller
{
    public function __invoke(Request $request)
    {
        $limit = (int) config('auth.idle_timeout');
        $last  = (int) $request->session()->get('last_seen', time());

        // 화면의 카운트다운이 쓸 '남은 초'. 판정 자체는 서버가 이미 했다.
        return response()->json([
            'ok'   => true,
            'left' => max(0, $limit - (time() - $last)),
        ]);
    }
}
