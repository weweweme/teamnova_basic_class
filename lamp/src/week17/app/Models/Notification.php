<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

// ============================================================
// Notification — notifications 표
//   "누가(actor) 내 글·내 댓글에 말을 걸었다"를 한 줄로 남긴다.
//
//   ★ Laravel에도 자체 알림 기능이 있지만 쓰지 않는다.
//     그쪽은 표 모양(notifications.type = 클래스 이름, data = JSON)이 정해져 있어
//     우리 표(actor_id·post_id·comment_id 를 각각 칸으로 둔 구조)와 맞지 않는다.
//     우리 표를 그대로 쓰는 쪽이 이관 비용이 적다.
// ============================================================
class Notification extends Model
{
    // updated_at 이 없는 표라 '만든 시각'만 쓴다
    const UPDATED_AT = null;

    protected $fillable = ['user_id', 'actor_id', 'type', 'post_id', 'comment_id'];

    protected function casts(): array
    {
        return ['is_read' => 'boolean'];
    }

    // 받는 사람
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class, 'user_id');
    }

    // 말을 건 사람
    public function actor(): BelongsTo
    {
        return $this->belongsTo(User::class, 'actor_id');
    }

    public function post(): BelongsTo
    {
        return $this->belongsTo(Post::class, 'post_id');
    }

    // 알림이 가리키는 댓글. 지워졌어도 '몇 페이지인지' 계산에 필요하므로 함께 가져온다.
    public function comment(): BelongsTo
    {
        return $this->belongsTo(Comment::class, 'comment_id')->withTrashed();
    }

    // ── 알림 한 줄 만들기 ───────────────────────────────────
    //   ★ 자기 자신에게 보내는 알림은 만들지 않는다 (내 글에 내가 댓글을 달 때).
    //     지금 create_notification() 안에서 걸러내던 것과 같다.
    public static function notify(?int $recipientId, int $actorId, Comment $comment, string $type): void
    {
        if (! $recipientId || $recipientId === $actorId) {
            return;
        }

        static::create([
            'user_id'    => $recipientId,
            'actor_id'   => $actorId,
            'type'       => $type,
            'post_id'    => $comment->post_id,
            'comment_id' => $comment->id,
        ]);
    }
}
