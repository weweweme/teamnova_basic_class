<?php

use App\Http\Controllers\Auth\LoginController;
use App\Http\Controllers\Auth\RegisterController;
use App\Http\Controllers\CommentController;
use App\Http\Controllers\PostController;
use App\Http\Controllers\TrashController;
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

// ── 글 ────────────────────────────────────────────────────
//   ★ 순서가 중요하다. /posts/create 를 /posts/{post} 보다 먼저 적어야 한다.
//     라우트는 위에서부터 맞춰 보므로, 반대로 두면 'create' 가 글 번호로 해석돼 404가 난다.
//
//   middleware('auth') = 로그인 안 한 요청은 컨트롤러에 닿기 전에 로그인 화면으로 보낸다.
//     지금 화면 첫 줄에서 require_login() 을 부르던 27곳이 이 한 마디로 대체된다.
Route::get('/posts',         [PostController::class, 'index']);
Route::get('/posts/create',  [PostController::class, 'create'])->middleware('auth');
Route::post('/posts',        [PostController::class, 'store'])->middleware('auth');
Route::get('/posts/{post}',  [PostController::class, 'show']);

//   수정·삭제는 로그인 + 소유권 둘 다 필요하다.
//     · 로그인 여부  → auth 미들웨어 (컨트롤러에 닿기 전)
//     · 소유권       → PostPolicy (컨트롤러 안에서 authorize)
//   ★ HTML 폼은 GET·POST 만 보낼 수 있다. PUT·DELETE 는 폼에 @method('PUT') 을 넣어
//     '사실은 PUT 이다'라고 알려주는 방식으로 쓴다(메서드 위장).
Route::get('/posts/{post}/edit', [PostController::class, 'edit'])->middleware('auth');
Route::put('/posts/{post}',      [PostController::class, 'update'])->middleware('auth');
Route::delete('/posts/{post}',   [PostController::class, 'destroy'])->middleware('auth');

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

// ── 댓글 ──────────────────────────────────────────────────
//   작성은 '어느 글에 다는지'가 주소에 드러나게 /posts/{post}/comments 로 둔다.
//   수정·삭제는 댓글 번호만 있으면 되므로 /comments/{comment}.
Route::post('/posts/{post}/comments', [CommentController::class, 'store'])->middleware('auth');
Route::put('/comments/{comment}',     [CommentController::class, 'update'])->middleware('auth');
Route::delete('/comments/{comment}',  [CommentController::class, 'destroy'])->middleware('auth');

// ── 휴지통 ────────────────────────────────────────────────
//   ★ withTrashed() 를 붙여야 한다.
//     라우트 모델 바인딩은 기본적으로 '지워지지 않은 것'만 찾으므로,
//     그냥 두면 휴지통 글을 되돌리려 할 때 404가 난다.
Route::get('/trash', [TrashController::class, 'index'])->middleware('auth');
Route::patch('/posts/{post}/restore', [TrashController::class, 'restore'])
    ->middleware('auth')->withTrashed();
Route::delete('/posts/{post}/force', [TrashController::class, 'forceDelete'])
    ->middleware('auth')->withTrashed();
