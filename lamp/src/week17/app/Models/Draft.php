<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

// ============================================================
// Draft — drafts 표 (글쓰기 도중 자동 저장되는 초안)
//   기본키가 (user_id, work_slug) 복합키라 id 가 없다.
// ============================================================
class Draft extends Model
{
    // 사람이 붙인 번호(id)가 없는 표라 Eloquent 의 가정 두 개를 꺼야 한다
    protected $primaryKey = null;
    public $incrementing = false;

    // 만든 시각은 없고 고친 시각만 있다
    const CREATED_AT = null;

    protected $fillable = ['user_id', 'work_slug', 'title', 'content', 'sentiment'];
}
