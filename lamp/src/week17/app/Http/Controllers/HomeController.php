<?php

namespace App\Http\Controllers;

use App\Models\Media;
use App\Models\Post;
use App\Services\Tmdb;

// ============================================================
// HomeController — 홈 화면
//   지금 index.php 에 해당한다.
//
//   ★ 원래 화면은 무거운 TMDB 줄을 JS로 나중에 불러온다(api/row.php).
//     여기서는 캐시(30분)를 믿고 서버가 한 번에 그린다 — JS 없이 같은 화면이 나온다.
//     스크롤에 맞춰 더 불러오는 동작은 main.js 를 옮길 때 함께 붙인다.
// ============================================================
class HomeController extends Controller
{
    public function index(Tmdb $tmdb)
    {
        return view('home', [
            // 우리 DB = 우리 정체성. 맨 위는 '우리 커뮤니티에서 이야기 중'인 작품.
            'works'  => Media::withCount('posts')->having('posts_count', '>', 0)
                             ->orderByDesc('posts_count')->limit(8)->get(),

            'recent' => Post::with(['author', 'media'])->withCount('comments')
                            ->latest('id')->limit(8)->get(),

            // 조회 + 댓글 가중 — 게시판 '인기' 탭과 같은 기준
            'hot'    => Post::with(['author', 'media'])->withCount('comments')
                            ->orderByRaw('(views + (SELECT COUNT(*) FROM comments c WHERE c.post_id = posts.id) * 10) DESC')
                            ->limit(8)->get(),

            'trending' => $tmdb->trending(12),
        ]);
    }
}
