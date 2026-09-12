<?php

namespace App\Http\Middleware;

use App\Services\DeviceKey;
use App\Services\DeviceTracker;
use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;

// ============================================================
// RequireDeviceKeyProof — 도장을 확인받아야 화면을 열 수 있게 한다
//   지금 includes/device_key.php 의 DEVICE_KEY_REQUIRED 판정에 해당한다.
//
//   ★ '세션을 연장한다'가 아니다. 갱신되는 건 '마지막으로 도장을 확인한 시각'뿐이고,
//     그 확인이 만료되면 세션이 살아 있어도 화면을 열 수 없다.
//     → 훔친 쪽은 도장이 없으므로 아무리 요청해도 시간을 밀지 못한다.
// ============================================================
class RequireDeviceKeyProof
{
    // 도장 없이도 지나가야 하는 주소들.
    //   ★ 이게 없으면 도장을 찍으러 가는 길 자체가 막힌다.
    private const ALLOW = ['session/*', 'login', 'logout', 'register'];

    public function handle(Request $request, Closure $next)
    {
        if (! config('auth.device_key_required') || ! Auth::check() || $request->is(self::ALLOW)) {
            return $next($request);
        }

        $keys = app(DeviceKey::class);

        // 이미 최근에 확인했으면 통과
        if ($keys->proofIsFresh($request)) {
            return $next($request);
        }

        // 아직 도장을 등록하지 않은 기기 — 등록 창이 열려 있으면 통과시켜 JS가 등록하게 둔다
        $deviceId = app(DeviceTracker::class)->deviceId($request);
        if ($keys->publicKeyFor(Auth::id(), $deviceId) === null) {
            return $keys->canEnroll($request)
                ? $next($request)
                : $this->blocked($request, 'key_missing');
        }

        return $this->blocked($request, 'proof_expired');
    }

    private function blocked(Request $request, string $reason)
    {
        // JS가 부르는 요청이면 JSON 으로, 사람이 여는 화면이면 안내 화면으로
        if ($request->expectsJson()) {
            return response()->json(['error' => $reason], 403);
        }

        return response()->view('session.proof-needed', ['reason' => $reason], 403);
    }
}
