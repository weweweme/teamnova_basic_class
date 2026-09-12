<?php

namespace App\Http\Middleware;

use App\Services\Prefs;
use Closure;
use Illuminate\Http\Request;

// ============================================================
// ApplyCookieConsent — 동의하지 않은 항목의 쿠키를 매 요청 치운다
//
//   ★ 동의 화면에서 한 번 치우는 것으로는 부족하다.
//     동의 전에 이미 심긴 쿠키, 정책 판이 바뀌어 동의가 무효가 된 경우가 남는다.
//     "앞으로 안 심겠다"가 아니라 "지금 것도 치우겠다"가 되려면 계속 확인해야 한다.
// ============================================================
class ApplyCookieConsent
{
    public function handle(Request $request, Closure $next)
    {
        app(Prefs::class)->forgetUnconsented($request);

        return $next($request);
    }
}
