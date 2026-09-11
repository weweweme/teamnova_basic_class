<?php

namespace App\Http\Controllers;

use App\Models\Media;
use App\Models\Post;
use App\Models\User;
use App\Services\Tmdb;
use Illuminate\Http\Request;

// ============================================================
// SearchController — 통합검색 · 글 검색 · 유저 검색 · 작품 검색
//   지금 search/index.php · posts.php · users.php · works.php 에 해당한다.
// ============================================================
class SearchController extends Controller
{
    private const PER_PAGE    = 20;   // 전용 화면의 한 페이지 개수
    private const PREVIEW_MAX = 5;    // 통합검색에서 종류별로 보여줄 개수

    // ── 통합검색 (GET /search?q=…) ──────────────────────────
    public function index(Request $request, Tmdb $tmdb)
    {
        $q = $this->query($request);

        return view('search.index', [
            'q'     => $q,
            'posts' => $q === '' ? collect() : $this->postQuery($q)->limit(self::PREVIEW_MAX)->get(),
            'users' => $q === '' ? collect() : $this->userQuery($q)->limit(self::PREVIEW_MAX)->get(),
            'works' => $q === '' ? collect() : $this->workQuery($q)->limit(self::PREVIEW_MAX)->get(),
        ]);
    }

    // ── 글 검색 전용 (GET /search/posts) ────────────────────
    public function posts(Request $request)
    {
        $q = $this->query($request);

        return view('search.posts', [
            'q'     => $q,
            // ★ 자르는 일은 DB가 한다. 글이 아무리 많아도 서버가 든 건 이 페이지 20개뿐이다.
            //   withQueryString() 을 붙여야 페이지 링크에 ?q= 가 함께 실린다.
            'posts' => $q === ''
                ? Post::whereRaw('1 = 0')->paginate(self::PER_PAGE)
                : $this->postQuery($q)->paginate(self::PER_PAGE)->withQueryString(),
        ]);
    }

    // ── 유저 검색 전용 (GET /search/users) ──────────────────
    public function users(Request $request)
    {
        $q = $this->query($request);

        return view('search.users', [
            'q'     => $q,
            'users' => $q === ''
                ? User::whereRaw('1 = 0')->paginate(self::PER_PAGE)
                : $this->userQuery($q)->paginate(self::PER_PAGE)->withQueryString(),
        ]);
    }

    // ── 작품 검색 전용 (GET /search/works) ──────────────────
    //   우리 DB에 있는 작품 + TMDB 검색 결과를 함께 보여준다.
    public function works(Request $request, Tmdb $tmdb)
    {
        $q = $this->query($request);

        $ours = $q === '' ? collect() : $this->workQuery($q)->get();

        // 이미 우리 DB에 있는 작품은 TMDB 목록에서 뺀다 (같은 작품이 두 번 보이지 않게)
        $haveSlugs = $ours->pluck('slug')->all();
        $fromTmdb  = $q === ''
            ? []
            : array_values(array_filter($tmdb->searchMovies($q), fn ($m) => ! in_array($m['slug'], $haveSlugs, true)));

        return view('search.works', ['q' => $q, 'ours' => $ours, 'fromTmdb' => $fromTmdb]);
    }

    // ── 공통 ────────────────────────────────────────────────

    private function query(Request $request): string
    {
        return trim((string) $request->query('q', ''));
    }

    // ★ LIKE 의 특수문자(% _ \)를 그대로 넘기면 '아무거나'로 해석된다.
    //   검색어에 % 가 들어오면 전부 걸리게 되므로 escape 한다.
    private function like(string $q): string
    {
        return '%' . addcslashes($q, '%_' . chr(92)) . '%';
    }

    private function postQuery(string $q)
    {
        $like = $this->like($q);

        return Post::with('author')->withCount('comments')
            ->where(fn ($w) => $w->where('title', 'like', $like)->orWhere('content', 'like', $like))
            ->latest('id');
    }

    private function userQuery(string $q)
    {
        $like = $this->like($q);

        return User::where(fn ($w) => $w->where('username', 'like', $like)->orWhere('nickname', 'like', $like))
            ->orderBy('id');
    }

    private function workQuery(string $q)
    {
        return Media::withCount('posts')->where('title', 'like', $this->like($q))->orderByDesc('posts_count');
    }
}
