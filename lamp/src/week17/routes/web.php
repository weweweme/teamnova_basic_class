<?php

use App\Http\Controllers\Auth\LoginController;
use App\Http\Controllers\Auth\RegisterController;
use App\Models\Post;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return view('welcome');
});

// ── 연습 ① 주소 등록해 보기 ─────────────────────────────────
//   ★ 여기에 적지 않은 주소는 '존재하지 않는다'.
//     지금까지 우리 프로젝트는 파일을 만들면 그게 곧 주소였지만,
//     Laravel에서는 이 목록에 적어야 주소가 생긴다.
//     (안드로이드에서 매니페스트에 등록하지 않은 화면을 띄울 수 없는 것과 같다)
//
//   Route::get('주소', 실행할 코드)
//     · get  = 브라우저가 주소창으로 들어오는 방식(GET). 폼 전송은 Route::post
//     · 두 번째 인자는 지금은 함수지만, 실제로는 컨트롤러의 메서드를 지정한다
Route::get('/hello', function () {
    // ── 연습 ② 화면 파일에 값 넘기기 ─────────────────────────
    //   view('파일이름', ['변수명' => 값])
    //     · 'hello' → resources/views/hello.blade.php 를 찾는다
    //     · 넘긴 이름이 그 파일 안에서 $name · $danger 로 쓰인다
    //   ★ 화면을 '직접 출력'하지 않고 '돌려준다'는 점이 지금과 다르다.
    //     출력은 프레임워크가 한다. (Unity에서 화면을 직접 그리지 않는 것과 같다)
    return view('hello', [
        'name'   => '진수',
        'danger' => '<script>alert(1)</script>',   // escape 확인용
    ]);
});

// ── 연습 ④ 목록을 페이지로 자르기 ───────────────────────────
//   ★ paginate(15) 한 줄이 세 가지를 한다.
//     1) 총 몇 개인지 센다        → select count(*) ...
//     2) 그 페이지 분량만 가져온다 → ... limit 15 offset 0
//     3) 지금 몇 페이지인지 판단   → 주소의 ?page= 를 알아서 읽는다
//
//   지금 우리 코드(board/index.php)는 get_posts()로 190개를 전부 배열에 올린 뒤
//   array_slice로 15개를 잘랐다. 여기서는 DB가 자른다.
//
//   latest('id') = order by id desc. 최신 글이 위로.
Route::get('/posts', function () {
    // withCount('comments') = 글마다 댓글 개수를 세어 comments_count 로 붙여 준다.
    //   ★ 목록 15개를 그리려고 댓글 수를 15번 따로 물어보면 쿼리가 16번 나간다(N+1).
    //     withCount 는 서브쿼리 한 번으로 붙인다 — 지금 get_posts() 가 손으로 짜 둔 것과 같다.
    // with('author') = 글쓴이도 미리 한 번에 읽어 둔다 (역시 N+1 방지)
    $posts = Post::with('author')->withCount('comments')->latest('id')->paginate(15);

    // 화면에는 '자른 결과'만 넘긴다. 총 개수·현재 페이지·링크는 $posts 안에 함께 들어 있다.
    return view('posts.index', ['posts' => $posts]);
});

// ── 연습 ⑤ 글 보기 + 댓글 페이징 ────────────────────────────
//   ★ 매개변수 자리에 Post 타입을 적으면 '라우트 모델 바인딩'이 걸린다.
//     /posts/91 로 들어오면 Post::find(91) 을 대신 해서 넣어 준다.
//     · 못 찾으면 자동으로 404 — 우리가 쓰던 "없으면 홈으로" if 문이 사라진다
//     · 소프트삭제된 글은 애초에 조회되지 않으므로 역시 404가 된다
Route::get('/posts/{post}', function (Post $post) {
    $comments = $post->comments()
        // ★ 지운 댓글도 함께 가져온다.
        //   우리 프로젝트는 지운 댓글을 지우지 않고 "삭제된 댓글입니다" 자리로 남긴다.
        //   (답글이 달려 있으면 고아가 되기 때문) SoftDeletes 는 기본으로 빼버리므로
        //   여기서는 일부러 되돌린다.
        ->withTrashed()
        // ★ 화면 순서 = 원댓글 바로 밑에 그 답글.
        //   COALESCE(parent_id, id) = '내가 속한 묶음의 번호'. 그 안에서는 id 순.
        ->orderByRaw('COALESCE(parent_id, id), id')
        // ★ 답글까지 합쳐 20줄씩 자른다 (묶음 기준이 아니라 줄 기준).
        //   네 번째 인자로 주소 파라미터 이름을 cpage 로 바꾼다 — 글 목록의 page 와 겹치지 않게.
        ->paginate(20, ['*'], 'cpage');

    return view('posts.show', ['post' => $post, 'comments' => $comments]);
});

// ── 2단계 인증 ──────────────────────────────────────────────
//   ★ 여기서부터는 함수가 아니라 '컨트롤러의 메서드'를 지정한다.
//     [클래스::class, '메서드이름'] 형태.
//
//   throttle:5,1 = 1분에 5번까지만 허용. 넘으면 429 로 막는다.
//     ★ 우리 includes/login_guard.php (116줄) 가 하던 무차별 대입 방어가 이 한 마디다.
//       (다만 기준이 다르다 — 우리는 아이디별, 내장 제한은 IP별이다)
Route::get('/login',  [LoginController::class, 'create'])->name('login');
Route::post('/login', [LoginController::class, 'store'])->middleware('throttle:5,1');
Route::post('/logout', [LoginController::class, 'destroy']);

//   회원가입도 같은 방식. 자동 가입 스크립트를 막으려고 요청 제한을 함께 건다.
Route::get('/register',  [RegisterController::class, 'create']);
Route::post('/register', [RegisterController::class, 'store'])->middleware('throttle:5,1');
