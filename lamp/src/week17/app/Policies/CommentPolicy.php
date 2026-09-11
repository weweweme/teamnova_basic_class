<?php

namespace App\Policies;

use App\Models\Comment;
use App\Models\User;

// ============================================================
// CommentPolicy — 댓글 수정·삭제 권한
//   지금 comment/update.php · delete.php 가 각각 손으로 하던 소유권 검사다.
// ============================================================
class CommentPolicy
{
    public function update(User $user, Comment $comment): bool
    {
        // 지운 댓글은 고칠 수 없다 (자리만 남아 있는 상태이므로)
        return $user->id === $comment->author_id && ! $comment->trashed();
    }

    public function delete(User $user, Comment $comment): bool
    {
        return $user->id === $comment->author_id && ! $comment->trashed();
    }
}
