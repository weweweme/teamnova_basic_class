<?php

namespace App\Http\Controllers;

use App\Models\Media;
use App\Models\Post;
use App\Models\Report;
use App\Services\DraftBox;
use App\Services\Prefs;
use App\Services\ViewCounter;
use Illuminate\Foundation\Auth\Access\AuthorizesRequests;
use Illuminate\Http\Request;

// ============================================================
// PostController — 글 목록 · 글 보기 · 글쓰기
//   지금 board/index.php · post/view.php · post/write.php · post/create.php
//   네 파일에 해당한다.
// ============================================================
class PostController extends Controller
{
    // authorize() 를 쓰기 위한 트레이트. Laravel 11부터 기본 Controller 에 들어 있지 않다.
    use AuthorizesRequests;

    // ── 글 목록 (GET /posts) ────────────────────────────────
    public function index(Request $request, Prefs $prefs)
    {
        // with('author')     — 글쓴이를 미리 한 번에 읽어 둔다
        // withCount('comments') — 글마다 댓글 개수를 서브쿼리 한 번으로 붙인다
        //   ★ 둘 다 N+1(목록 15개에 쿼리 31번) 방지용이다. 지금 get_posts() 가 손으로 짜 둔 일이다.
        // ?per_page= 로 바꾸면 기억해 둔다 (동의한 경우에만 쿠키에 담긴다)
        if ($request->filled('per_page')) {
            $prefs->rememberPerPage($request, (int) $request->query('per_page'));
        }

        // 정렬·감상 필터도 고르면 기억한다
        if ($request->filled('sort')) {
            $prefs->rememberSort($request, (string) $request->query('sort'), array_keys(Post::SORT_TABS));
        }
        if ($request->has('sentiment')) {
            $prefs->rememberSentiment($request, (string) $request->query('sentiment'), Post::SENTIMENTS);
        }

        $perPage   = $prefs->perPage($request);
        $sort      = $request->query('sort', $prefs->sort($request, array_keys(Post::SORT_TABS), 'new'));
        $sentiment = $request->query('sentiment', $prefs->sentiment($request, Post::SENTIMENTS));

        //   author 를 불러올 때 그 사람의 글 수도 함께 센다 (등급 배지에 쓴다).
        //   ★ 목록 15개마다 따로 세면 쿼리가 15번 더 나간다 → 관계 쪽에 withCount 를 얹는다.
        //   board() 는 Post 모델에 둔 스코프다 — 감상 필터 · 검색어 · 정렬을 한 번에 건다.
        //   (전체 글 목록에는 게시판 안 검색이 없으므로 검색어는 빈 값)
        $posts = Post::with(['author' => fn ($q) => $q->withCount('posts')])
                     ->withCount(['comments', 'likers'])
                     ->board($sentiment, '', $sort)
                     ->paginate($perPage)
                     ->withQueryString();

        return view('posts.index', [
            'posts'     => $posts,
            'perPage'   => $perPage,
            'sort'      => $sort,
            'sentiment' => $sentiment,
            'sortTabs'  => Post::SORT_TABS,
            'sentiments' => Post::SENTIMENTS,
        ]);
    }

    // ── 글 보기 (GET /posts/{post}) ─────────────────────────
    //   ★ 매개변수에 Post 타입을 적으면 주소의 값으로 알아서 찾아 넣어 준다(라우트 모델 바인딩).
    //     못 찾거나 소프트삭제된 글이면 자동 404 — "없으면 홈으로" if 문이 사라진다.
    public function show(Request $request, Post $post, ViewCounter $viewCounter, Prefs $prefs)
    {
        // 글쓴이 옆 등급 배지에 쓸 '그 사람의 글 수'도 함께 센다
        $post->load(['media', 'author' => fn ($q) => $q->withCount('posts')]);

        $comments = $post->comments()
            ->with(['author' => fn ($q) => $q->withCount('posts')])
            ->withTrashed()                              // 지운 댓글도 '자리'로 남긴다
            ->orderByRaw('COALESCE(parent_id, id), id')  // 원댓글 바로 밑에 그 답글
            ->paginate(20, ['*'], 'cpage')               // 답글까지 합쳐 20줄씩
            // 페이지를 넘겨도 다른 조건은 유지하되, '답글/수정 중'은 끌고 가지 않는다.
            // #comments = 페이지를 넘기면 댓글 자리로 바로 내려간다.
            ->appends($request->except(['reply', 'edit', 'cpage']))
            ->fragment('comments');

        // ── 조회수 ─────────────────────────────────────────
        //   ★ 판정과 집계는 우리 규칙이라 서비스 클래스로 옮겼다 (ViewCounter).
        //     올랐으면 화면에도 반영한다 — $post 는 올리기 '전'에 읽어온 값이라
        //     안 더하면 새로고침해야 반영된 것처럼 보인다.
        if ($viewCounter->count($request, $post)) {
            $post->views++;
        }

        // 추천 수를 붙인다 (likers_count). 이미 불러온 모델에 덧붙일 때는 loadCount().
        $post->loadCount('likers');

        // 내가 추천했는지 — 추천한 사람 전부를 불러오지 않고 '있는지'만 묻는다.
        $liked = $request->user()
            ? $post->likers()->where('user_id', $request->user()->id)->exists()
            : false;

        // 최근 본 글로 기억한다 (동의한 경우에만 쿠키에 담긴다)
        $prefs->rememberRecentPost($request, $post->id);

        return view('posts.show', [
            'post'          => $post,
            'comments'      => $comments,
            'liked'         => $liked,
            'reportReasons' => Report::REASONS,
        ]);
    }

    // ── 글쓰기 화면 (GET /posts/create) ─────────────────────
    public function create(Request $request, DraftBox $drafts)
    {
        // 글쓰기는 '작품 게시판'에서 시작한다 → ?work=슬러그 로 어느 작품인지 넘어온다.
        //   전체 글 목록에서 눌렀을 때는 작품이 정해져 있지 않으므로 목록에서 고르게 둔다.
        $work = $request->query('work')
            ? Media::bySlug((string) $request->query('work'))
            : null;

        // 임시저장해 둔 글이 있으면 이어서 쓰게 채워 준다
        $draft = $work ? $drafts->get($request->user()->id, $work->slug) : null;

        return view('posts.create', [
            'work'      => $work,
            'draft'     => $draft,
            'mediaList' => $work ? null : Media::orderBy('title')->get(),
        ]);
    }

    public function store(Request $request, DraftBox $drafts)
    {
        // 길이 한도는 기존 상수와 같게 맞췄다 (제목 100 · 본문 5000).
        //   work      — 작품 게시판에서 왔을 때. 아직 media 표에 없는 작품일 수 있다.
        //   media_id  — 전체 글 목록에서 골라 왔을 때. 실제로 있는 번호인지 DB에 물어본다.
        //   in:…      — 정해 둔 값 중 하나인지 (표의 enum 과 같은 값)
        $data = $request->validate([
            'work'      => ['required_without:media_id', 'string', 'max:100'],
            'media_id'  => ['required_without:work', 'integer', 'exists:media,id'],
            'title'     => ['required', 'string', 'max:100'],
            'content'   => ['required', 'string', 'max:5000'],
            'sentiment' => ['required', 'in:호평,보통,혹평'],
        ]);

        // ★ 작품 게시판에서 왔으면, 글을 넣기 전에 그 작품이 표에 있게 만든다.
        //   아직 아무도 글을 쓰지 않은 작품은 media 표에 없어서
        //   그대로 넣으면 외래키(media_id)가 걸리지 않는다.
        if (! empty($data['work'])) {
            $media = Media::ensureBySlug($data['work']);
            if (! $media) {
                return back()->withInput()->with('error', '존재하지 않는 작품입니다.');
            }
            $data['media_id'] = $media->id;
        }
        unset($data['work']);

        // ★ 글쓴이는 입력값에서 받지 않는다. 로그인한 사람으로 서버가 정한다.
        //   받으면 남의 이름으로 글을 쓸 수 있다.
        $data['author_id'] = $request->user()->id;

        $post = Post::create($data);

        // 등록했으니 임시저장해 둔 초안은 지운다
        if ($request->filled('work')) {
            $drafts->forget($request->user()->id, (string) $request->input('work'));
        }

        return redirect("/posts/{$post->id}")->with('status', '글이 등록되었습니다.');
    }

    // ── 글 수정 화면 (GET /posts/{post}/edit) ───────────────
    public function edit(Post $post)
    {
        // ★ 소유권 확인. 통과 못 하면 403으로 끊긴다 — if 문과 리다이렉트를 우리가 쓰지 않는다.
        //   판단 내용은 PostPolicy::update() 한 곳에 있다.
        $this->authorize('update', $post);

        return view('posts.edit', ['post' => $post]);
    }

    // ── 글 수정 저장 (PUT /posts/{post}) ────────────────────
    public function update(Request $request, Post $post)
    {
        $this->authorize('update', $post);

        $data = $request->validate([
            'media_id'  => ['required', 'integer', 'exists:media,id'],
            'title'     => ['required', 'string', 'max:100'],
            'content'   => ['required', 'string', 'max:5000'],
            'sentiment' => ['required', 'in:호평,보통,혹평'],
        ]);

        // ★ 여기서 edited_at 이 자동으로 채워진다 (const UPDATED_AT = 'edited_at').
        //   그래서 화면의 '(수정됨)' 표시를 우리가 따로 관리하지 않아도 된다.
        $post->update($data);

        return redirect("/posts/{$post->id}")->with('status', '글을 수정했습니다.');
    }

    // ── 글 삭제 (DELETE /posts/{post}) ──────────────────────
    public function destroy(Post $post)
    {
        $this->authorize('delete', $post);

        // ★ SoftDeletes 라서 실제로는 DELETE 가 아니라 deleted_at 을 채우는 UPDATE 다.
        //   글은 휴지통으로 가고, 달려 있던 댓글도 화면에서 함께 사라진다.
        $post->delete();

        return redirect('/posts')->with('status', '글을 삭제했습니다.');
    }
}
