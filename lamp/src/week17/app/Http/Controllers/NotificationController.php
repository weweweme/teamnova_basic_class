<?php

namespace App\Http\Controllers;

use App\Models\Notification;
use Illuminate\Http\Request;

// ============================================================
// NotificationController — 알림 목록
//   지금 notifications/index.php 에 해당한다.
// ============================================================
class NotificationController extends Controller
{
    public function index(Request $request)
    {
        $userId = $request->user()->id;

        $notifications = Notification::where('user_id', $userId)
            // ★ 목록 30개를 그리면서 보낸 사람·글·댓글을 따로 물어보면 쿼리가 90번 넘게 나간다(N+1).
            //   with() 로 한 번에 읽어 둔다. 지금 notifications.php 가 JOIN + 서브쿼리로 하던 일이다.
            ->with(['actor', 'post', 'comment'])
            // 지워진 글의 알림은 뺀다 — 눌러도 없는 글로 가기 때문.
            //   whereHas('post') = '살아 있는 글이 있는' 알림만 (Post 의 소프트삭제 스코프가 그대로 적용된다)
            ->whereHas('post')
            ->latest('id')
            ->paginate(30);

        // 목록을 여는 순간 전부 '읽음'으로. (지금과 같은 동작)
        Notification::where('user_id', $userId)->where('is_read', false)->update(['is_read' => true]);

        return view('notifications.index', ['notifications' => $notifications]);
    }
}
