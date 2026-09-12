<?php

namespace App\Http\Controllers;

use App\Models\Media;
use App\Models\Post;
use App\Models\User;
use App\Services\Prefs;
use App\Services\Tmdb;
use Illuminate\Http\Request;

// ============================================================
// SearchController — 통합검색 · 작품 검색 · 글 검색 · 유저 검색
//   지금 search/index.php · works.php · posts.php · users.php 에 해당한다.
// ============================================================
class SearchController extends Controller
{
    private const PER_PAGE    = 20;   // 전용 화면의 한 페이지 개수
    private const PREVIEW_MAX = 5;    // 통합검색에서 종류별로 보여줄 개수

    // ── 통합검색 (GET /search?q=…) ──────────────────────────
    public function index(Request $request, Tmdb $tmdb, Prefs $prefs)
    {
        $q = $this->query($request);

        // 검색어를 기억한다 (쿠키 동의가 있을 때만 담긴다)
        if ($q !== '') {
            $prefs->rememberSearch($request, $q);
        }

        return view('search.index', [
            'q'         => $q,
            'recent'    => $prefs->recentSearches($request),
            'works'     => $q === '' ? [] : $this->workResults($tmdb, $q, self::PREVIEW_MAX),
            'posts'     => $q === '' ? collect() : $this->postQuery($q)->limit(self::PREVIEW_MAX)->get(),
            'users'     => $q === '' ? collect() : $this->userQuery($q)->limit(self::PREVIEW_MAX)->get(),
            // '더보기'를 보여줄지 정하려면 전체 개수가 필요하다.
            //   ★ 목록을 다 불러와 세지 않는다 — DB 에 개수만 묻는다.
            'postTotal' => $q === '' ? 0 : $this->postQuery($q)->count(),
            'userTotal' => $q === '' ? 0 : $this->userQuery($q)->count(),
        ]);
    }

    // ── 작품 검색 전용 (GET /search/works) ──────────────────
    //   ★ 우리 표에 있는 작품과 TMDB 결과를 한 목록으로 합친다.
    //     주소는 둘 다 /works/tmdb-<번호> 로 같다 — 표에 없으면 TMDB 에서 받아 보여준다.
    public function works(Request $request, Tmdb $tmdb)
    {
        $q = $this->query($request);

        return view('search.works', [
            'q'     => $q,
            'works' => $q === '' ? [] : $this->workResults($tmdb, $q, self::PER_PAGE),
        ]);
    }

    // ── 글 검색 전용 (GET /search/posts) ────────────────────
    public function posts(Request $request)
    {
        $q = $this->query($request);

        return view('search.posts', [
            'q'     => $q,
            // ★ 자르는 일은 DB 가 한다. 글이 아무리 많아도 서버가 든 건 이 페이지 20개뿐이다.
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

    // ── 공통 ────────────────────────────────────────────────

    private function query(Request $request): string
    {
        return mb_substr(trim((string) $request->query('q', '')), 0, 50);
    }

    // ★ LIKE 의 특수문자(% _ \)를 그대로 넘기면 '아무거나'로 해석된다.
    //   검색어에 % 가 들어오면 전부 걸리게 되므로 escape 한다.
    private function like(string $q): string
    {
        return '%' . addcslashes($q, '%_' . chr(92)) . '%';
    }

    // 작품 결과 — 우리 표에 있는 것을 앞에, TMDB 에서 찾은 나머지를 뒤에
    private function workResults(Tmdb $tmdb, string $q, int $limit): array
    {
        $ours = Media::where('title', 'like', $this->like($q))
            ->withCount('posts')->orderByDesc('posts_count')->limit($limit)->get()
            ->map(fn ($m) => [
                'slug'       => $m->slug,
                'title'      => $m->title,
                'genre'      => $m->genre,
                'year'       => $m->year,
                'poster_url' => $m->poster_url,
            ])->all();

        // 같은 작품이 두 번 보이지 않게, 이미 담은 slug 는 뺀다
        $have  = array_column($ours, 'slug');
        $extra = array_filter($tmdb->searchMovies($q, $limit), fn ($m) => ! in_array($m['slug'], $have, true));

        return array_slice(array_merge($ours, array_values($extra)), 0, $limit);
    }

    private function postQuery(string $q)
    {
        $like = $this->like($q);

        return Post::with(['media', 'author' => fn ($w) => $w->withCount('posts')])
            ->withCount(['comments', 'likers'])
            ->where(fn ($w) => $w->where('title', 'like', $like)->orWhere('content', 'like', $like))
            ->latest('id');
    }

    private function userQuery(string $q)
    {
        $like = $this->like($q);

        return User::withCount(['posts', 'comments'])
            ->where(fn ($w) => $w->where('username', 'like', $like)->orWhere('nickname', 'like', $like))
            ->orderBy('id');
    }
}
