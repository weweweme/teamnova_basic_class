<?php

namespace App\Http\Controllers;

use App\Services\Consent;
use App\Services\Prefs;
use Illuminate\Http\Request;

// ============================================================
// ConsentController — 쿠키 동의 결정 받기 · 설정 화면
//   지금 consent.php · cookies.php 에 해당한다. JS 없이 폼 POST 하나로 끝난다.
// ============================================================
class ConsentController extends Controller
{
    // ── 쿠키 설정 화면 (GET /cookies) ───────────────────────
    //   ★ 동의를 '거둘 수 있는 자리'가 반드시 있어야 한다.
    //     거둘 방법을 찾아 헤매야 한다면 그 동의는 있으나 마나다.
    public function edit(Request $request, Consent $consent)
    {
        // 지금 이 브라우저가 들고 있는 쿠키를 '필수 / 선택'으로 갈라 보여준다.
        //   ★ 말로만 '필수는 이런 것들'이라고 하지 않고 실제 이름을 보여준다.
        //   선택 쿠키가 무엇인지는 Prefs 가 한 곳에 정리해 두었다 (쿠키 이름 → 동의 항목).
        $names     = array_keys($request->cookies->all());
        $optional  = array_values(array_intersect($names, array_keys(Prefs::OWNED_BY)));
        $essential = array_values(array_diff($names, $optional));

        return view('cookies', [
            'decided' => $consent->decided($request),
            'checked' => array_values(array_filter(
                Consent::ITEMS,
                fn ($item) => $consent->has($request, $item)
            )),
            'essential' => $essential,
            'optional'  => $optional,
            'history'   => $consent->history($request),
        ]);
    }

    public function store(Request $request, Consent $consent, Prefs $prefs)
    {
        $data = $request->validate([
            'choice'  => ['required', 'in:all,selected,none,reset'],
            'source'  => ['nullable', 'in:banner,settings'],
            'items'   => ['nullable', 'array'],
            'items.*' => ['in:' . implode(',', Consent::ITEMS)],
        ]);

        // '동의 창 다시 보기' — 결정을 지우기만 한다 (기록은 남는다)
        if ($data['choice'] === 'reset') {
            $consent->forget();
            $prefs->forgetUnconsented($request);

            return back()->with('status', '동의 창을 다시 보여드릴게요.');
        }

        // 누른 버튼에 따라 무엇에 동의한 것인지 정한다
        $items = match ($data['choice']) {
            'all'      => Consent::ITEMS,
            'selected' => $data['items'] ?? [],
            'none'     => [],
        };

        $consent->record($request, $items, $data['source'] ?? 'banner');

        // 거절한 항목의 쿠키는 지금 바로 치운다
        $prefs->forgetUnconsented($request);

        return back()->with('status', '쿠키 설정을 저장했습니다.');
    }
}
