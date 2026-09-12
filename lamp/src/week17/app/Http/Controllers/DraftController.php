<?php

namespace App\Http\Controllers;

use App\Services\DraftBox;
use Illuminate\Http\Request;

// ============================================================
// DraftController — 글쓰기 도중 초안 저장 (브라우저 JS가 부른다)
//   지금 api/draft.php 에 해당한다.
// ============================================================
class DraftController extends Controller
{
    public function store(Request $request, DraftBox $drafts)
    {
        $data = $request->validate([
            'work_slug' => ['required', 'string', 'max:100'],
            'title'     => ['nullable', 'string', 'max:200'],
            'content'   => ['nullable', 'string', 'max:5000'],
            'sentiment' => ['nullable', 'string', 'max:10'],
        ]);

        $drafts->save($request->user()->id, $data['work_slug'], $data);

        return response()->json(['ok' => true]);
    }
}
