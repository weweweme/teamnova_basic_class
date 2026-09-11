<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;

// ============================================================
// Media — media 표 (영화·드라마 등 '작품')
// ============================================================
class Media extends Model
{
    // ── 표 이름을 직접 적는 이유 ───────────────────────────
    //   Media 는 이미 복수형이라 자동 추측이 애매하다. 오해를 없애려고 못 박는다.
    protected $table = 'media';

    // 시각 컬럼이 아예 없는 표라 타임스탬프 기능을 끈다.
    //   (안 끄면 저장할 때 created_at/updated_at 을 찾다가 실패한다)
    public $timestamps = false;

    // ── 감상 투표 (votes 표) ────────────────────────────────
    //   votes 는 user_id·media_id 에 더해 choice('추천'/'비추천') 를 함께 들고 있다.
    //   ★ 이어주는 표에 '딸린 값'이 있으면 withPivot() 으로 가져온다.
    //     그러면 $media->voters->first()->pivot->choice 로 읽을 수 있다.
    public function voters(): BelongsToMany
    {
        return $this->belongsToMany(User::class, 'votes', 'media_id', 'user_id')
                    ->withPivot('choice');
    }
}
