<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

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
}
