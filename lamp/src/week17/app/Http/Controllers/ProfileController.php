<?php

namespace App\Http\Controllers;

use App\Models\Post;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

// ============================================================
// ProfileController — 프로필 보기 · 아바타 올리기
//   지금 profile/index.php · profile/avatar.php 에 해당한다.
// ============================================================
class ProfileController extends Controller
{
    // ── 프로필 (GET /users/{username}) ──────────────────────
    //   ★ {user:username} = id 가 아니라 username 칸으로 찾는다.
    public function show(Request $request, User $user)
    {
        // 탭: 작성한 글 | 좋아요한 글
        $tab = $request->query('tab') === 'liked' ? 'liked' : 'posts';

        $list = $tab === 'liked'
            ? $user->likedPosts()->with(['media', 'author' => fn ($q) => $q->withCount('posts')])
            : $user->posts()->with('media');

        $posts = $list->withCount(['comments', 'likers'])->latest('posts.id')->paginate(15)->withQueryString();

        // 활동 통계 — 글 수 · 총 조회 · 받은 추천.
        //   ★ 목록과 달리 '전체'를 세야 하므로 페이지네이터가 아니라 따로 묻는다.
        $stats = $user->posts()
            ->selectRaw('COUNT(*) AS posts_count, COALESCE(SUM(views), 0) AS views_sum')
            ->first();

        $likesReceived = DB::table('likes')
            ->join('posts', 'posts.id', '=', 'likes.post_id')
            ->where('posts.author_id', $user->id)
            ->whereNull('posts.deleted_at')
            ->count();

        return view('profile.show', [
            'user'          => $user,
            'posts'         => $posts,
            'tab'           => $tab,
            'isMe'          => $request->user()?->is($user) ?? false,
            'postCount'     => (int) $stats->posts_count,
            'totalViews'    => (int) $stats->views_sum,
            'likesReceived' => $likesReceived,
        ]);
    }

    // ── 아바타 올리기 (POST /settings/avatar) ───────────────
    public function updateAvatar(Request $request)
    {
        $request->validate([
            // ★ 'image' 는 실제로 이미지인지 확인한다(내용을 열어 본다).
            //   'evil.php' 를 'photo.jpg' 로 이름만 바꿔 올려도 여기서 걸린다.
            //   mimes 는 우리가 허용한 형식만, max 는 KB 단위(2048KB = 2MB).
            'avatar' => ['required', 'image', 'mimes:jpeg,png,webp', 'max:2048'],
        ], [
            'avatar.image' => '이미지 파일만 올릴 수 있습니다.',
            'avatar.mimes' => 'JPG · PNG · WEBP 만 올릴 수 있습니다.',
            'avatar.max'   => '2MB 이하만 올릴 수 있습니다.',
        ]);

        $user = $request->user();
        $file = $request->file('avatar');

        // ★ 파일 이름을 사용자 것에서 가져오지 않는다. 확장자도 우리가 정한 것만 쓴다.
        //   사용자가 준 이름을 그대로 쓰면 경로를 거슬러 올라가거나(../) 실행 가능한 이름이 될 수 있다.
        $name = 'u' . $user->id . '_' . bin2hex(random_bytes(8)) . '.' . $file->extension();

        $file->move(public_path('uploads/avatars'), $name);

        // 예전 파일은 지운다 (계속 쌓이지 않게)
        if ($user->avatar && str_starts_with($user->avatar, '/uploads/avatars/')) {
            @unlink(public_path(ltrim($user->avatar, '/')));
        }

        $user->update(['avatar' => '/uploads/avatars/' . $name]);

        return back()->with('status', '프로필 사진을 변경했습니다.');
    }
}
