<?php

namespace App\Http\Controllers;

use App\Models\Media;
use App\Models\Post;
use App\Services\Prefs;
use App\Services\Tmdb;
use Illuminate\Http\Request;

// ============================================================
// WorkController — 작품 목록 · 작품 게시판
//   지금 works/index.php · board/index.php(작품별) 에 해당한다.
// ============================================================
class WorkController extends Controller
{
    // ── 작품 둘러보기 (GET /works) ──────────────────────────
    //   ★ 포스터는 여기서 안 그린다. 화면 뼈대만 보내고 JS 가 /api/browse 로 채운다.
    //     TMDB 응답을 기다렸다 그리면 첫 화면이 그만큼 늦게 뜨기 때문이다.
    public function index(Request $request, Prefs $prefs)
    {
        $genre = (string) $request->query('genre', '');
        if (! isset(Tmdb::GENRES[$genre])) {
            $genre = '';                   // 이상한 값이면 전체로
        }

        $media = (string) $request->query('media', 'all');
        if (! in_array($media, ['all', 'movie', 'tv', 'anime'], true)) {
            $media = 'all';
        }

        // 최근 본 작품 — 쿠키에 남은 슬러그로 찾는다 (동의했을 때만 쌓인다)
        $recentWorks = collect($prefs->recentWorkSlugs($request))
            ->map(fn ($slug) => Media::bySlug($slug))
            ->filter()
            ->map(fn ($m) => [
                'url'        => '/works/' . $m->slug,
                'poster_url' => $m->poster_url,
                'title'      => $m->title,
                'meta'       => $m->year,
            ])
            ->all();

        return view('works.index', [
            'genre'       => $genre,
            'media'       => $media,
            'genres'      => array_keys(Tmdb::GENRES),
            'recentWorks' => $recentWorks,
        ]);
    }

    // ── 작품 게시판 (GET /works/{slug}) ─────────────────────
    //   ★ {media:slug} 라고 적으면 id 가 아니라 slug 로 찾는다.
    //     주소가 /works/parasite 처럼 읽히게 된다.
    public function show(Request $request, Media $media, Prefs $prefs, Tmdb $tmdb)
    {
        // 최근 본 작품으로 기억한다 (쿠키 동의가 있을 때만 담긴다)
        $prefs->rememberRecentWork($request, $media->slug);

        // 정렬·감상·페이지 크기는 전체 글 목록과 같은 취향을 쓴다 (골라 두면 기억된다)
        if ($request->filled('per_page')) {
            $prefs->rememberPerPage($request, (int) $request->query('per_page'));
        }
        if ($request->filled('sort')) {
            $prefs->rememberSort($request, (string) $request->query('sort'), array_keys(Post::SORT_TABS));
        }
        if ($request->has('sentiment')) {
            $prefs->rememberSentiment($request, (string) $request->query('sentiment'), Post::SENTIMENTS);
        }

        $perPage   = $prefs->perPage($request);
        $sort      = $request->query('sort', $prefs->sort($request, array_keys(Post::SORT_TABS), 'new'));
        $sentiment = $request->query('sentiment', $prefs->sentiment($request, Post::SENTIMENTS));
        // 이 게시판 '안에서만' 찾는 말 (작품 검색은 상단 메뉴의 '검색')
        $q = mb_substr(trim((string) $request->query('q', '')), 0, 50);

        // board() = 감상 필터 · 검색어 · 정렬을 한 번에 거는 스코프 (Post 모델에 있다)
        $posts = Post::with(['author' => fn ($query) => $query->withCount('posts')])
            ->withCount(['comments', 'likers'])
            ->where('media_id', $media->id)
            ->board($sentiment, $q, $sort)
            ->paginate($perPage)
            ->withQueryString();

        // 감독·출연·예고편·분량은 우리 표에 없고 TMDB 에만 있다.
        //   ★ slug 가 tmdb-123 꼴일 때만 물어본다. 결과는 30분 캐시된다.
        $detail = str_starts_with($media->slug, 'tmdb-')
            ? $tmdb->detail((int) substr($media->slug, 5))
            : null;

        // 감상 투표 집계 — 추천/비추천이 각각 몇 표인지.
        //   ★ 한 번 읽어서 PHP 쪽에서 센다. 표가 작아(작품당 수십) 쿼리를 두 번 보낼 이유가 없다.
        $votes = $media->voters->groupBy(fn ($u) => $u->pivot->choice)->map->count();

        return view('works.show', [
            'media'      => $media,
            'detail'     => $detail,
            'posts'      => $posts,
            'up'         => $votes['추천']   ?? 0,
            'down'       => $votes['비추천'] ?? 0,
            // 내가 어느 쪽에 투표했는지 (안 했으면 null)
            'myVote'     => $request->user()
                ? $media->voters->firstWhere('id', $request->user()->id)?->pivot->choice
                : null,
            'perPage'    => $perPage,
            'sort'       => $sort,
            'sentiment'  => $sentiment,
            'q'          => $q,
            'sortTabs'   => Post::SORT_TABS,
            'sentiments' => Post::SENTIMENTS,
        ]);
    }
}
