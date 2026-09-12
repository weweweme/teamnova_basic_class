<?php

namespace App\Http\Controllers;

use App\Services\Consent;
use App\Services\Prefs;
use Illuminate\Http\Request;

// ============================================================
// ConsentController — 쿠키 동의 결정 받기
//   지금 consent.php 화면에 해당한다. JS 없이 폼 POST 하나로 끝난다.
// ============================================================
class ConsentController extends Controller
{
    public function store(Request $request, Consent $consent, Prefs $prefs)
    {
        $data = $request->validate([
            'items'   => ['nullable', 'array'],
            'items.*' => ['in:' . implode(',', Consent::ITEMS)],
            'source'  => ['nullable', 'string', 'max:10'],
        ]);

        $consent->record($request, $data['items'] ?? [], $data['source'] ?? 'banner');

        // 거절한 항목의 쿠키는 지금 바로 치운다
        $prefs->forgetUnconsented($request);

        return back()->with('status', '쿠키 설정을 저장했습니다.');
    }
}
