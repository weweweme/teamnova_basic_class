<?php

namespace App\Http\Controllers;

use App\Models\Media;
use App\Models\Post;
use Illuminate\Http\Request;

// ============================================================
// PostController — 글 목록 · 글 보기 · 글쓰기
//   지금 board/index.php · post/view.php · post/write.php · post/create.php
//   네 파일에 해당한다.
// ============================================================
class PostController extends Controller
{
    // ── 글 목록 (GET /posts) ────────────────────────────────
    public function index()
    {
        // with('author')     — 글쓴이를 미리 한 번에 읽어 둔다
        // withCount('comments') — 글마다 댓글 개수를 서브쿼리 한 번으로 붙인다
        //   ★ 둘 다 N+1(목록 15개에 쿼리 31번) 방지용이다. 지금 get_posts() 가 손으로 짜 둔 일이다.
        $posts = Post::with('author')->withCount('comments')->latest('id')->paginate(15);

        return view('posts.index', ['posts' => $posts]);
    }

    // ── 글 보기 (GET /posts/{post}) ─────────────────────────
    //   ★ 매개변수에 Post 타입을 적으면 주소의 값으로 알아서 찾아 넣어 준다(라우트 모델 바인딩).
    //     못 찾거나 소프트삭제된 글이면 자동 404 — "없으면 홈으로" if 문이 사라진다.
    public function show(Post $post)
    {
        $comments = $post->comments()
            ->with('author')
            ->withTrashed()                              // 지운 댓글도 '자리'로 남긴다
            ->orderByRaw('COALESCE(parent_id, id), id')  // 원댓글 바로 밑에 그 답글
            ->paginate(20, ['*'], 'cpage');              // 답글까지 합쳐 20줄씩

        return view('posts.show', ['post' => $post, 'comments' => $comments]);
    }

    // ── 글쓰기 화면 (GET /posts/create) ─────────────────────
    public function create()
    {
        // 어느 작품에 대한 글인지 고르게 한다.
        //   ※ 원래는 '작품 게시판'에서 시작하는 구조라 작품이 이미 정해져 있다.
        //     작품 화면은 5단계에서 옮기므로, 그전까지는 목록에서 고르게 둔다.
        return view('posts.create', ['mediaList' => Media::orderBy('title')->get()]);
    }

    // ── 글 저장 (POST /posts) ───────────────────────────────
    public function store(Request $request)
    {
        // 길이 한도는 기존 상수와 같게 맞췄다 (제목 100 · 본문 5000).
        //   exists:media,id — 실제로 있는 작품 번호인지 DB에 물어본다
        //   in:…           — 정해 둔 값 중 하나인지 (표의 enum 과 같은 값)
        $data = $request->validate([
            'media_id'  => ['required', 'integer', 'exists:media,id'],
            'title'     => ['required', 'string', 'max:100'],
            'content'   => ['required', 'string', 'max:5000'],
            'sentiment' => ['required', 'in:호평,보통,혹평'],
        ]);

        // ★ 글쓴이는 입력값에서 받지 않는다. 로그인한 사람으로 서버가 정한다.
        //   받으면 남의 이름으로 글을 쓸 수 있다.
        $data['author_id'] = $request->user()->id;

        $post = Post::create($data);

        return redirect("/posts/{$post->id}")->with('status', '글이 등록되었습니다.');
    }
}
