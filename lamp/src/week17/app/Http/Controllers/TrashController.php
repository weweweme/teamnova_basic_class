<?php

namespace App\Http\Controllers;

use App\Models\Post;
use Illuminate\Foundation\Auth\Access\AuthorizesRequests;
use Illuminate\Http\Request;

// ============================================================
// TrashController — 휴지통 목록 · 되돌리기 · 영구삭제
//   지금 trash/index.php · post/restore.php · post/purge.php 에 해당한다.
// ============================================================
class TrashController extends Controller
{
    use AuthorizesRequests;

    // 보관 기간(일). 지나면 자동으로 진짜 삭제한다.
    private const RETENTION_DAYS = 30;

    // ── 휴지통 (GET /trash) ─────────────────────────────────
    public function index(Request $request)
    {
        // ① 보관 기간이 지난 글을 먼저 치운다 (열 때마다 하는 방식 — 지금과 같다)
        //   ★ onlyTrashed() = 지워진 것만. forceDelete() = 진짜 DELETE.
        //   subDays() 는 Carbon 이 제공한다 — 날짜 계산을 문자열로 짜지 않아도 된다.
        Post::onlyTrashed()
            ->where('deleted_at', '<', now()->subDays(self::RETENTION_DAYS))
            ->forceDelete();

        // ② 내가 지운 글만 보여준다
        $posts = Post::onlyTrashed()
            ->where('author_id', $request->user()->id)
            ->with('media')
            ->latest('deleted_at')
            ->paginate(15);

        return view('trash.index', [
            'posts'         => $posts,
            'retentionDays' => self::RETENTION_DAYS,
        ]);
    }

    // ── 되돌리기 (PATCH /posts/{post}/restore) ──────────────
    public function restore(Post $post)
    {
        $this->authorize('restore', $post);

        // ★ deleted_at 을 비우는 UPDATE 다. 글이 다시 목록에 나타난다.
        $post->restore();

        return redirect("/posts/{$post->id}")->with('status', '글을 되돌렸습니다.');
    }

    // ── 영구삭제 (DELETE /posts/{post}/force) ───────────────
    public function forceDelete(Post $post)
    {
        $this->authorize('forceDelete', $post);

        // ★ 이건 진짜 DELETE 다. 되돌릴 수 없다.
        $post->forceDelete();

        return redirect('/trash')->with('status', '글을 완전히 삭제했습니다.');
    }
}
