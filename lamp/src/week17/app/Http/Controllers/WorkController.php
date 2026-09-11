<?php

namespace App\Http\Controllers;

use App\Models\Media;
use App\Models\Post;
use Illuminate\Http\Request;

// ============================================================
// WorkController — 작품 목록 · 작품 게시판
//   지금 works/index.php · board/index.php(작품별) 에 해당한다.
// ============================================================
class WorkController extends Controller
{
    // ── 작품 목록 (GET /works) ──────────────────────────────
    public function index()
    {
        $works = Media::withCount('posts')->orderByDesc('posts_count')->get();

        return view('works.index', ['works' => $works]);
    }

    // ── 작품 게시판 (GET /works/{slug}) ─────────────────────
    //   ★ {media:slug} 라고 적으면 id 가 아니라 slug 로 찾는다.
    //     주소가 /works/parasite 처럼 읽히게 된다.
    public function show(Request $request, Media $media)
    {
        $posts = Post::with('author')->withCount('comments')
            ->where('media_id', $media->id)
            ->latest('id')
            ->paginate(15);

        // 감상 투표 집계 — 추천/비추천이 각각 몇 표인지.
        //   ★ 한 번 읽어서 PHP 쪽에서 센다. 표가 작아(작품당 수십) 쿼리를 두 번 보낼 이유가 없다.
        $votes = $media->voters->groupBy(fn ($u) => $u->pivot->choice)->map->count();

        return view('works.show', [
            'media'   => $media,
            'posts'   => $posts,
            'up'      => $votes['추천']   ?? 0,
            'down'    => $votes['비추천'] ?? 0,
            // 내가 어느 쪽에 투표했는지 (안 했으면 null)
            'myVote'  => $request->user()
                ? $media->voters->firstWhere('id', $request->user()->id)?->pivot->choice
                : null,
        ]);
    }
}
