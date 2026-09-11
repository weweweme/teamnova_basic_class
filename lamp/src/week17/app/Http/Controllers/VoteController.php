<?php

namespace App\Http\Controllers;

use App\Models\Media;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

// ============================================================
// VoteController — 작품 감상 투표 (추천 / 비추천)
//   지금 vote/sentiment.php + includes/works.php 의 toggle_vote() 에 해당한다.
// ============================================================
class VoteController extends Controller
{
    public function store(Request $request, Media $media)
    {
        $data = $request->validate([
            'choice' => ['required', Rule::in(['추천', '비추천'])],
        ]);

        $userId  = $request->user()->id;
        $current = $media->voters()->where('user_id', $userId)->first()?->pivot->choice;

        // ★ 이어주는 표에 값이 딸려 있으면 toggle() 하나로는 부족하다. 세 갈래로 나뉜다.
        if ($current === $data['choice']) {
            $media->voters()->detach($userId);                                  // 같은 걸 또 누름 → 취소
        } elseif ($current === null) {
            $media->voters()->attach($userId, ['choice' => $data['choice']]);   // 처음 투표
        } else {
            $media->voters()->updateExistingPivot($userId, ['choice' => $data['choice']]);  // 바꿔 투표
        }

        return back()->with('status', '투표를 반영했습니다.');
    }
}
