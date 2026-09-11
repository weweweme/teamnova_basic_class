<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

// ============================================================
// Report — reports 표 (글 신고)
//   (reporter_id, post_id) 가 UNIQUE 라 같은 사람이 같은 글을 두 번 신고할 수 없다.
// ============================================================
class Report extends Model
{
    const UPDATED_AT = null;   // 만든 시각만 있는 표

    protected $fillable = ['post_id', 'reporter_id', 'reason'];

    // 신고 사유 — 표의 enum 과 같은 값이어야 한다
    public const REASONS = ['스팸/광고', '욕설/비방', '스포일러', '기타'];

    public function post(): BelongsTo
    {
        return $this->belongsTo(Post::class, 'post_id');
    }
}
