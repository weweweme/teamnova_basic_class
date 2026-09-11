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

    // 새 댓글에는 수정 시각을 넣지 않는다 (Post 와 같은 이유)
    protected static function booted(): void
    {
        static::creating(function (self $comment) {
            $comment->edited_at = null;
        });
    }

    // 한 페이지에 보여줄 댓글 줄 수 (답글도 한 줄로 센다)
    public const PER_PAGE = 20;

    // ── 이 댓글이 몇 페이지에 있는가 ────────────────────────
    //   ★ 이건 프레임워크가 대신 해주지 않는 '우리 규칙'이다.
    //     댓글을 쓰거나 고친 뒤 '그 댓글이 실제로 보이는 페이지'로 돌려보내는 데 쓴다.
    //
    //   화면 순서가 COALESCE(parent_id, id), id 이므로 '앞'의 기준도 두 단계다.
    //     ① 내 묶음보다 앞선 묶음의 줄 전부
    //     ② 같은 묶음 안에서 나보다 앞이거나 나 자신
    //   지운 댓글도 화면에 자리가 남으므로 withTrashed() 로 함께 센다.
    //
    //   ★ 컨트롤러가 아니라 모델에 둔다 — 이 규칙을 쓰는 곳이 작성·수정·삭제·알림 네 군데라
    //     한 곳에 있어야 어긋나지 않는다.
    public function pageNumber(): int
    {
        $rootId = $this->parent_id ?? $this->id;

        $position = static::withTrashed()
            ->where('post_id', $this->post_id)
            ->where(function ($q) use ($rootId) {
                $q->whereRaw('COALESCE(parent_id, id) < ?', [$rootId])
                  ->orWhere(function ($q2) use ($rootId) {
                      $q2->whereRaw('COALESCE(parent_id, id) = ?', [$rootId])
                         ->where('id', '<=', $this->id);
                  });
            })
            ->count();

        return max(1, (int) ceil($position / self::PER_PAGE));
    }

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
