<?php

namespace App\Http\Controllers;

use App\Models\Media;
use App\Models\Post;
use App\Models\User;
use Illuminate\Support\Facades\DB;

// ============================================================
// RankController — 랭킹 (명예의 전당)
//   지금 rank/index.php + includes/ranking.php 에 해당한다.
// ============================================================
class RankController extends Controller
{
    private const LIMIT = 20;

    public function index()
    {
        return view('rank.index', [
            'users' => $this->users(),
            'posts' => $this->posts(),
            'works' => $this->works(),
        ]);
    }

    // ── 유저 랭킹 — 받은 추천 많은 순, 같으면 글 많은 순 ────
    //   ★ 지금은 users ⋈ posts ⋈ likes 세 표를 JOIN 하고,
    //     줄이 불어나는 걸 COUNT(DISTINCT …) 로 다시 눌러 준다.
    //     withCount 는 표를 잇지 않고 '서브쿼리'로 세므로 그 문제가 처음부터 없다.
    private function users()
    {
        return User::select('users.*')
            ->withCount('posts')
            // ★ '내 글들이 받은 추천 총합'은 관계 하나로 안 나온다 (users → posts → likes 2단계).
            //   그래서 값을 셀 작은 질의를 select 목록에 끼워 넣는다(상관 서브쿼리).
            //   ⚠ 지금은 세 표를 JOIN 하고 COUNT(DISTINCT …) 로 불어난 줄을 다시 눌렀다.
            //     서브쿼리는 표를 잇지 않으므로 줄이 불어나는 문제가 처음부터 없다.
            ->addSelect(['likes_received_count' => DB::table('likes')
                ->join('posts', 'posts.id', '=', 'likes.post_id')
                ->whereColumn('posts.author_id', 'users.id')
                ->whereNull('posts.deleted_at')          // 지운 글이 받은 추천은 빼고
                ->selectRaw('count(*)'),
            ])
            ->having('posts_count', '>', 0)          // 글 있는 유저만
            ->orderByDesc('likes_received_count')
            ->orderByDesc('posts_count')
            ->limit(self::LIMIT)
            ->get();
    }

    // ── 글 랭킹 — 추천 많은 순 ─────────────────────────────
    private function posts()
    {
        return Post::with('author')->withCount(['likers', 'comments'])
            ->orderByDesc('likers_count')
            ->orderByDesc('views')
            ->limit(self::LIMIT)
            ->get();
    }

    // ── 작품 랭킹 — 글 많은 순 ─────────────────────────────
    private function works()
    {
        return Media::withCount('posts')
            ->having('posts_count', '>', 0)
            ->orderByDesc('posts_count')
            ->limit(self::LIMIT)
            ->get();
    }
}
