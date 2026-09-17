<?php

use App\Http\Controllers\Api\MediaFeedController;
use App\Http\Controllers\Auth\ConfirmPasswordController;
use App\Http\Controllers\Auth\LoginController;
use App\Http\Controllers\Auth\GoogleLoginController;
use App\Http\Controllers\Auth\EmailVerificationController;
use App\Http\Controllers\Auth\RecoveryQuestionController;
use App\Http\Controllers\Auth\PasswordResetController;
use App\Http\Controllers\Auth\RegisterController;
use App\Http\Controllers\CommentController;
use App\Http\Controllers\LikeController;
use App\Http\Controllers\NotificationController;
use App\Http\Controllers\DeviceKeyController;
use App\Http\Controllers\DraftController;
use App\Http\Controllers\AccountController;
use App\Http\Controllers\ConsentController;
use App\Http\Controllers\HomeController;
use App\Http\Controllers\PostController;
use App\Http\Controllers\PostImageController;
use App\Http\Controllers\ProfileController;
use App\Http\Controllers\SessionPingController;
use App\Http\Controllers\SettingsController;
use App\Http\Controllers\UnsubscribeController;
use App\Http\Controllers\RankController;
use App\Http\Controllers\SearchController;
use App\Http\Controllers\VoteController;
use App\Http\Controllers\WorkController;
use App\Http\Controllers\ReportController;
use App\Http\Controllers\TrashController;
use Illuminate\Support\Facades\Route;

// ── 홈 ────────────────────────────────────────────────────
Route::get('/', [HomeController::class, 'index']);

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

//   본문에 넣을 사진 올리기 — 글쓰기 화면의 JS 가 부른다 (주소를 JSON 으로 돌려준다)
Route::post('/posts/images', [PostImageController::class, 'store'])->middleware(['auth', 'throttle:20,1']);

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
// ── 비밀번호 찾기 ─────────────────────────────────────────
//   ★ 레거시에 없던 기능. 토큰 발급·메일 발송·만료 확인은 프레임워크가 한다.
//     라우트 이름 'password.reset' 은 메일 본문의 링크를 만들 때 쓰이므로 이름을 맞춰야 한다.
Route::get('/forgot-password',  [PasswordResetController::class, 'request'])->name('password.request');
Route::post('/forgot-password', [PasswordResetController::class, 'send'])->middleware('throttle:5,1');
Route::get('/reset-password/{token}', [PasswordResetController::class, 'edit'])->name('password.reset');
Route::post('/reset-password',        [PasswordResetController::class, 'update'])->middleware('throttle:5,1');

//   ★ 질문으로 찾기 — 이메일을 넣지 않은 회원을 위한 두 번째 통로.
//     메일보다 약한 방식이라 시도 횟수를 더 좁게 잡는다.
//     답을 하나씩 넣어 보거나 아이디를 훑는 데 쓰이지 못하게 하는 장치다.
Route::get('/forgot-password/question',  [RecoveryQuestionController::class, 'ask']);
Route::post('/forgot-password/question', [RecoveryQuestionController::class, 'show'])->middleware('throttle:5,1');
Route::post('/forgot-password/question/verify', [RecoveryQuestionController::class, 'update'])
    ->middleware('throttle:5,60');

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

// ── 추천 ──────────────────────────────────────────────────
Route::post('/posts/{post}/like', [LikeController::class, 'toggle'])->middleware('auth');

// ── 알림 ──────────────────────────────────────────────────
Route::get('/notifications', [NotificationController::class, 'index'])->middleware('auth');

//   메일 속 '알림 그만 받기' — 로그인 없이 눌러야 하므로 서명된 주소를 쓴다.
//   ★ signed 미들웨어가 서명을 확인한다. 주소를 고치면 403 으로 막힌다.
Route::get('/notifications/unsubscribe/{user}', UnsubscribeController::class)
    ->name('notify.unsubscribe')->middleware('signed');

// ── 신고 ──────────────────────────────────────────────────
Route::post('/posts/{post}/report', [ReportController::class, 'store'])->middleware('auth');

// ── 작품 ──────────────────────────────────────────────────
//   ★ {media:slug} = id 가 아니라 slug 칸으로 찾는다. 주소가 /works/parasite 처럼 읽힌다.
Route::get('/works', [WorkController::class, 'index']);
Route::get('/works/{media:slug}', [WorkController::class, 'show']);
Route::post('/works/{media:slug}/vote', [VoteController::class, 'store'])->middleware('auth');

// ── 검색 ──────────────────────────────────────────────────
Route::get('/search',        [SearchController::class, 'index']);
Route::get('/search/posts',  [SearchController::class, 'posts']);
Route::get('/search/users',  [SearchController::class, 'users']);
Route::get('/search/works',  [SearchController::class, 'works']);

// ── 랭킹 ──────────────────────────────────────────────────
Route::get('/rank', [RankController::class, 'index']);

// ── 설정 ──────────────────────────────────────────────────
//   ★ auth.session 미들웨어 = '다른 기기 로그아웃'이 동작하기 위한 조건.
//     세션에 로그인 당시의 비밀번호 해시를 함께 담아 두고, 그 값이 달라지면 그 세션을 끊는다.
//   ★ password.confirm = '지금 본인이 맞나'를 한 번 더 묻는다(15분 유효).
//     로그인한 채로 자리를 비운 사이 누가 설정을 바꾸는 것을 막는다.
Route::middleware(['auth', 'auth.session', 'password.confirm'])->group(function () {
    Route::get('/settings', [SettingsController::class, 'index']);
    Route::patch('/settings/nickname', [SettingsController::class, 'nickname']);
    Route::patch('/settings/password', [SettingsController::class, 'password']);
    Route::patch('/settings/email', [SettingsController::class, 'email'])->middleware('throttle:5,1');
    Route::patch('/settings/notifications', [SettingsController::class, 'notifications']);
    Route::patch('/settings/recovery', [SettingsController::class, 'recovery']);
    Route::get('/settings/leave',    [AccountController::class, 'edit']);
    Route::delete('/settings/leave', [AccountController::class, 'destroy']);
    Route::post('/settings/logout-others', [SettingsController::class, 'logoutOtherDevices']);
    Route::delete('/settings/devices', [SettingsController::class, 'revokeDevice']);
});

// ── 세션 유지 신호 ────────────────────────────────────────
Route::post('/session/ping', SessionPingController::class)->middleware('auth');

// ── 기기 도장 ─────────────────────────────────────────────
//   브라우저 JS가 부르는 주소들. 모두 JSON 으로 답한다.
Route::middleware('auth')->group(function () {
    Route::post('/session/key',       [DeviceKeyController::class, 'enroll']);
    Route::post('/session/challenge', [DeviceKeyController::class, 'challenge']);
    Route::post('/session/verify',    [DeviceKeyController::class, 'verify']);
});

// ── 프로필 · 초안 ─────────────────────────────────────────
//   ★ {user:username} = id 가 아니라 username 칸으로 찾는다.
Route::get('/users/{user:username}', [ProfileController::class, 'show']);
Route::post('/settings/avatar', [ProfileController::class, 'updateAvatar'])->middleware('auth');
Route::post('/drafts', [DraftController::class, 'store'])->middleware('auth');

// ── 화면이 뜬 뒤 JS 가 받아 가는 작품 목록 ────────────────
//   지금 api/row.php · api/browse.php 에 해당한다.
Route::get('/api/row',    [MediaFeedController::class, 'row']);
Route::get('/api/browse', [MediaFeedController::class, 'browse']);

// ── 탈퇴 대기 중인 계정으로 로그인했을 때 ─────────────────
//   ★ 로그인은 아직 안 된 상태다. 복구할지 지울지 고른 뒤에야 들여보낸다.
Route::get('/account/restore',  [AccountController::class, 'restoreForm']);
Route::post('/account/restore', [AccountController::class, 'restoreSubmit'])->middleware('throttle:10,1');

// ── 약관 · 개인정보처리방침 ───────────────────────────────
//   ★ 로그인 없이 볼 수 있어야 한다. 가입 전에 읽고 판단하는 문서다.
Route::view('/terms', 'legal.terms');
Route::view('/privacy', 'legal.privacy');

// ── 쿠키 동의 ─────────────────────────────────────────────
Route::get('/cookies', [ConsentController::class, 'edit']);
Route::post('/consent', [ConsentController::class, 'store']);

// ── 구글 로그인 ───────────────────────────────────────────
//   ★ 두 주소가 한 쌍이다. 앞은 구글로 보내고, 뒤는 구글이 되돌려보내는 자리다.
//     뒤쪽 주소는 Google Cloud Console 에 등록한 값과 글자 하나까지 같아야 한다.
//   ★ throttle — 남이 콜백 주소를 마구 두드리는 것을 막는다.
Route::get('/auth/google', [GoogleLoginController::class, 'redirect'])->middleware('throttle:10,1');
Route::get('/auth/google/callback', [GoogleLoginController::class, 'callback'])->middleware('throttle:10,1');

//   ★ 가입 마무리 — 구글은 다녀왔지만 계정은 아직 없는 상태에서 동의를 받는다.
//     아이디로 가입하는 사람에게 체크를 받으면서 구글로 오는 사람만 그냥 만들 수는 없다.
Route::get('/auth/google/complete',  [GoogleLoginController::class, 'complete'])->name('google.complete');
Route::post('/auth/google/complete', [GoogleLoginController::class, 'store'])->middleware('throttle:10,1');

//   ★ 본인 확인용 — 로그인한 사람만. 비밀번호를 모르는 구글 회원이 설정에 들어갈 통로다.
Route::get('/auth/google/confirm', [GoogleLoginController::class, 'confirm'])
    ->middleware(['auth', 'throttle:10,1'])->name('google.confirm');

// ── 이메일 주소 확인 ──────────────────────────────────────
//   ★ 라우트 이름 'verification.verify' 는 바꿀 수 없다.
//     확인 메일의 링크를 프레임워크가 이 이름으로 만든다.
//   ★ signed = 서명 확인(주소를 고치면 403). 링크에는 만료 시각도 함께 서명되어 있다.
//   ★ auth  = 남이 주운 링크로 확인되지 않게, 본인 로그인 상태에서만 통과시킨다.
//   ★ throttle:6,1 = 1분에 6번까지. 링크를 마구 눌러 보는 시도를 막는다.
Route::get('/email/verify/{id}/{hash}', [EmailVerificationController::class, 'verify'])
    ->middleware(['auth', 'signed', 'throttle:6,1'])->name('verification.verify');

// ── 비밀번호 재확인 ───────────────────────────────────────
//   ★ 이름이 'password.confirm' 이어야 한다. 미들웨어가 이 이름으로 보낸다.
Route::get('/confirm-password', [ConfirmPasswordController::class, 'create'])
    ->middleware('auth')->name('password.confirm');
Route::post('/confirm-password', [ConfirmPasswordController::class, 'store'])
    ->middleware(['auth', 'throttle:6,1']);
