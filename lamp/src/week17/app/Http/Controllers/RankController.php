<?php

namespace App\Http\Controllers;

use App\Models\Media;
use App\Models\Post;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

// ============================================================
// RankController — 랭킹 (명예의 전당)
//   지금 rank/index.php + includes/ranking.php 에 해당한다.
//
//   ★ 화면은 탭 하나만 보여준다 → 고른 탭의 순위만 계산한다.
//     셋을 다 계산해 두고 하나만 그리면 나머지 두 번은 버려지는 일이다.
// ============================================================
class RankController extends Controller
{
    private const LIMIT = 10;   // 각 랭킹 상위 10개

    private const TABS = ['works' => '🎬 인기 작품', 'users' => '👑 명예의 전당', 'posts' => '🔥 화제의 글'];

    public function index(Request $request)
    {
        $tab = (string) $request->query('tab', 'works');
        if (! isset(self::TABS[$tab])) {
            $tab = 'works';
        }

        return view('rank.index', [
            'tab'  => $tab,
            'tabs' => self::TABS,
            'rows' => match ($tab) {
                'users' => $this->users(),
                'posts' => $this->posts(),
                default => $this->works(),
            },
        ]);
    }

    // ── 유저 랭킹 — 받은 추천 많은 순, 같으면 글 많은 순 ────
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

    // ── 글 랭킹 — 게시판의 '인기' 정렬과 같은 기준 ─────────
    private function posts()
    {
        return Post::with(['media', 'author' => fn ($q) => $q->withCount('posts')])
            ->withCount(['likers', 'comments'])
            ->board('', '', 'hot')
            ->limit(self::LIMIT)
            ->get();
    }

    // ── 작품 랭킹 — 글 많은 순 ─────────────────────────────
    //   추천 비율도 함께 보여준다 → 투표 수를 상관 서브쿼리로 붙인다.
    private function works()
    {
        return Media::withCount([
                'posts',
                'voters as up_votes'   => fn ($q) => $q->where('choice', '추천'),
                'voters as down_votes' => fn ($q) => $q->where('choice', '비추천'),
            ])
            ->having('posts_count', '>', 0)
            ->orderByDesc('posts_count')
            ->limit(self::LIMIT)
            ->get();
    }
}
