<?php

namespace App\Models;

use App\Notifications\CommentPosted;
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

        // ── 메일도 한 통 보낸다 ─────────────────────────────
        //   ★ 화면 알림은 위 한 줄로 끝났고, 여기서는 '누구에게 무엇을 보낼지'만 적는다.
        //     보내는 일(연결·재시도·형식)은 프레임워크가 한다.
        //   ★ 두 가지를 확인한다 — 이메일을 적었는가, 활동 알림을 켰는가.
        //     활동 알림은 기본이 꺼짐이다. 묻지 않고 보내는 메일은 동의가 아니다.
        //     (새 기기 로그인 · 비밀번호 변경 같은 보안 알림은 이 설정과 무관하게 나간다)
        $recipient = User::find($recipientId);

        if ($recipient?->email && $recipient->notify_activity) {
            $recipient->notify(new CommentPosted($comment, $type));
        }
    }
}
