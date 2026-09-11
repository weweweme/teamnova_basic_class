<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\SoftDeletes;

// ============================================================
// Comment — comments 표
//   원댓글과 답글이 같은 표에 있고, 답글은 parent_id 로 부모를 가리킨다.
// ============================================================
class Comment extends Model
{
    use SoftDeletes;                      // 지운 댓글도 '자리'는 남긴다 (deleted_at)

    const UPDATED_AT = 'edited_at';       // 우리 표의 수정 시각 컬럼 이름

    protected $fillable = ['post_id', 'author_id', 'parent_id', 'content'];

    // ── 관계 ────────────────────────────────────────────────
    //   ★ 관계를 적어 두면 JOIN을 우리가 쓰지 않는다.
    //     $comment->author->nickname 처럼 점으로 타고 들어가면
    //     필요한 쿼리를 Eloquent 가 알아서 보낸다.
    //
    //   belongsTo = '나에게 남의 번호가 있다' (comments.author_id → users.id)
    //   hasMany   = '남에게 내 번호가 있다'   (comments.parent_id → 이 댓글의 id)
    //
    //   두 번째 인자는 '외래키 칸 이름'이다. 관례(author_id → Author 모델)와
    //   다른 이름을 쓸 때는 이렇게 직접 적어 준다.
    public function author(): BelongsTo
    {
        return $this->belongsTo(User::class, 'author_id');
    }

    public function post(): BelongsTo
    {
        return $this->belongsTo(Post::class, 'post_id');
    }

    public function parent(): BelongsTo
    {
        return $this->belongsTo(self::class, 'parent_id');   // 이 댓글이 답글이면 그 부모
    }

    public function replies(): HasMany
    {
        return $this->hasMany(self::class, 'parent_id');     // 이 댓글에 달린 답글들
    }
}
