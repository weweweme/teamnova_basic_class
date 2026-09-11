<?php

namespace App\Http\Controllers;

use App\Models\Post;
use Illuminate\Http\Request;

// ============================================================
// LikeController — 글 추천 켜고 끄기
//   지금 like/toggle.php 에 해당한다.
// ============================================================
class LikeController extends Controller
{
    public function toggle(Request $request, Post $post)
    {
        // ★ toggle() = 이어져 있으면 끊고, 없으면 잇는다.
        //   지금 toggle_like() 가 SELECT → 있으면 DELETE / 없으면 INSERT 로 하던 일이다.
        //   돌려주는 배열로 어느 쪽이었는지 알 수 있다.
        $result = $post->likers()->toggle($request->user()->id);

        $liked = ! empty($result['attached']);

        return back()->with('status', $liked ? '추천했습니다.' : '추천을 취소했습니다.');
    }
}
