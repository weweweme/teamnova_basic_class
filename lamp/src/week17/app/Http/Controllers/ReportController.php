<?php

namespace App\Http\Controllers;

use App\Models\Post;
use App\Models\Report;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

// ============================================================
// ReportController — 글 신고
//   지금 report/create.php 에 해당한다.
// ============================================================
class ReportController extends Controller
{
    public function store(Request $request, Post $post)
    {
        $data = $request->validate([
            // Rule::in(...) 으로 정해 둔 값만 받는다. 목록은 모델 한 곳에 있다.
            'reason' => ['required', Rule::in(Report::REASONS)],
        ]);

        // ★ firstOrCreate = 같은 조건의 줄이 있으면 그걸 주고, 없으면 만든다.
        //   (reporter_id, post_id) 가 UNIQUE 라 중복 신고는 DB가 막지만,
        //   미리 확인하면 '이미 신고했습니다' 안내를 매끄럽게 줄 수 있다.
        //   wasRecentlyCreated = 방금 만들어진 줄인가.
        $report = Report::firstOrCreate(
            ['post_id' => $post->id, 'reporter_id' => $request->user()->id],
            ['reason'  => $data['reason']],
        );

        return back()->with('status', $report->wasRecentlyCreated
            ? '신고를 접수했습니다.'
            : '이미 신고한 글입니다.');
    }
}
