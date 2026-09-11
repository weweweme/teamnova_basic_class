<?php

namespace App\Policies;

use App\Models\Post;
use App\Models\User;

// ============================================================
// PostPolicy — "이 사람이 이 글에 이걸 해도 되는가"를 모아 두는 자리
//
//   ★ 지금은 이 판단이 화면마다 흩어져 있다.
//     post/update.php · delete.php · restore.php · purge.php 가 각각
//     if (!is_owner($target['author'])) { ... } 를 손으로 쓴다.
//     한 곳만 빠뜨리면 남의 글을 고칠 수 있게 된다.
//
//   ★ Policy 는 모델 이름으로 자동 연결된다 — App\Models\Post → App\Policies\PostPolicy.
//     등록 코드가 따로 필요 없다.
//
//   메서드 이름이 곧 '동작 이름'이다. 컨트롤러에서 authorize('update', $post) 처럼 부르고,
//   화면에서는 @can('update', $post) 로 버튼을 보일지 정한다.
// ============================================================
class PostPolicy
{
    // 수정 — 내가 쓴 글만
    public function update(User $user, Post $post): bool
    {
        return $user->id === $post->author_id;
    }

    // 삭제(휴지통으로) — 내가 쓴 글만
    public function delete(User $user, Post $post): bool
    {
        return $user->id === $post->author_id;
    }

    // 복구 — 내가 쓴 글만
    public function restore(User $user, Post $post): bool
    {
        return $user->id === $post->author_id;
    }

    // 영구 삭제 — 내가 쓴 글만
    public function forceDelete(User $user, Post $post): bool
    {
        return $user->id === $post->author_id;
    }
}
