<?php

namespace App\Http\Controllers;

use App\Models\Media;
use App\Models\Post;
use App\Services\Prefs;
use App\Services\Tmdb;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

// ============================================================
// HomeController — 홈 화면
//   지금 index.php 에 해당한다.
//
//   ★ 원래 화면은 무거운 TMDB 줄을 JS로 나중에 불러온다(api/row.php).
//     여기서는 캐시(30분)를 믿고 서버가 한 번에 그린다.
//     스크롤에 맞춰 더 불러오는 동작은 main.js 를 옮길 때 함께 붙인다.
// ============================================================
class HomeController extends Controller
{
    public function index(Request $request, Tmdb $tmdb, Prefs $prefs)
    {
        // 우리 DB = 우리 정체성. 맨 위 줄은 '우리 커뮤니티에서 이야기 중'인 작품.
        //   ★ 5개만 보여준다. 우리 작품 수가 많지 않아 다 늘어놓으면 줄이 아니라 목록처럼 보인다.
        $community = Media::withCount('posts')->having('posts_count', '>', 0)
            ->orderByDesc('posts_count')->limit(5)->get();

        // 히어로 — 그중 맨 앞 작품. 배경은 가로 이미지(backdrop)를 쓴다.
        $hero = $community->first();
        $heroBackdrop = $hero?->tmdb_id ? $tmdb->backdropFor((int) $hero->tmdb_id) : null;

        // 지금 뜨는 글 — 최근 7일 조회를 합쳐 센다 (post_view_daily).
        //   ★ 누적 조회수로 뽑으면 '오래돼서 많이 쌓인 글'이 계속 위에 남는다.
        $hot = Post::with('media')
            ->select('posts.*')
            ->addSelect(['recent_views' => DB::table('post_view_daily')
                ->whereColumn('post_view_daily.post_id', 'posts.id')
                ->where('viewed_on', '>=', now()->subDays(7)->toDateString())
                ->selectRaw('COALESCE(SUM(views), 0)'),
            ])
            ->orderByDesc('recent_views')->orderByDesc('views')
            ->limit(5)->get();

        // 최근 본 글 — 쿠키에 남은 번호로 찾는다 (동의했을 때만 쌓인다)
        $recentIds   = $prefs->recentPostIds($request);
        $recentViewed = $recentIds
            ? Post::with(['media', 'author'])->whereIn('id', $recentIds)
                  ->orderByRaw('FIELD(id, ' . implode(',', $recentIds) . ')')->get()
            : collect();

        return view('home', [
            'community'    => $community,
            'hero'         => $hero,
            'heroBackdrop' => $heroBackdrop,
            'hot'          => $hot,
            'recentViewed' => $recentViewed,
            'trending'     => $tmdb->trending(12),

            'recent'    => Post::with(['author', 'media'])->withCount('comments')
                               ->latest('id')->limit(8)->get(),
            'discussed' => Post::with(['author', 'media'])->withCount('comments')
                               ->orderByDesc('comments_count')->limit(8)->get(),
        ]);
    }
}
