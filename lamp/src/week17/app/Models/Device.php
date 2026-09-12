<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

// ============================================================
// Device — user_devices 표 (회원이 로그인한 적 있는 기기 목록)
//   지금 includes/devices.php 가 다루던 표다.
// ============================================================
class Device extends Model
{
    protected $table = 'user_devices';

    // 시각 칸 이름이 Laravel 관례와 다르다 (first_seen_at / last_login_at)
    public $timestamps = false;

    protected $fillable = ['user_id', 'device_id', 'user_agent', 'first_seen_at', 'last_login_at', 'announced'];

    protected function casts(): array
    {
        return [
            'first_seen_at' => 'datetime',
            'last_login_at' => 'datetime',
            'key_added_at'  => 'datetime',
            'announced'     => 'boolean',
        ];
    }

    // ★ 공개키 자체는 화면에 내보내지 않는다. 필요한 건 '있나 없나'뿐이다.
    //   필요 없는 값을 끌고 나오면 언젠가 그걸 어딘가에 찍게 된다.
    protected $hidden = ['public_key'];

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class, 'user_id');
    }

    // 도장(공개키)이 등록된 기기인가
    public function hasKey(): bool
    {
        return $this->public_key !== null;
    }

    // user_agent 문자열을 사람이 읽는 이름으로. ("Chrome · Windows")
    public function describe(): string
    {
        $ua = (string) $this->user_agent;

        $browser = match (true) {
            str_contains($ua, 'Edg/')     => 'Edge',
            str_contains($ua, 'Chrome/')  => 'Chrome',
            str_contains($ua, 'Firefox/') => 'Firefox',
            str_contains($ua, 'Safari/')  => 'Safari',
            default                       => '알 수 없는 브라우저',
        };

        $os = match (true) {
            str_contains($ua, 'Windows')  => 'Windows',
            str_contains($ua, 'Android')  => 'Android',
            str_contains($ua, 'iPhone'), str_contains($ua, 'iPad') => 'iOS',
            str_contains($ua, 'Mac OS')   => 'macOS',
            str_contains($ua, 'Linux')    => 'Linux',
            default                       => '알 수 없는 기기',
        };

        return "{$browser} · {$os}";
    }
}
