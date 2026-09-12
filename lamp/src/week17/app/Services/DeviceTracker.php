<?php

namespace App\Services;

use App\Models\Device;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cookie;
use Illuminate\Support\Facades\DB;

// ============================================================
// DeviceTracker — '이 브라우저'를 구분하는 표시와 기기 목록 관리
//   지금 includes/devices.php 를 옮긴 것이다.
//
//   ★ 여기는 '옮기는' 쪽이다. 프레임워크에 대응 기능이 없다.
// ============================================================
class DeviceTracker
{
    private const COOKIE = 'device';
    private const DAYS   = 90;

    // ── 이 브라우저의 표시 ──────────────────────────────────
    //   ★ 신원이 아니라 '구분표'다. 로그인 여부와 무관하게 이 브라우저를 가리킨다.
    //     값이 없거나 모양이 이상하면 새로 만든다(남이 심어 둔 값을 그대로 쓰지 않는다).
    public function deviceId(Request $request): string
    {
        $id = (string) $request->cookie(self::COOKIE);

        if (! preg_match('/^[a-f0-9]{32}$/', $id)) {
            $id = bin2hex(random_bytes(16));
            Cookie::queue(self::COOKIE, $id, self::DAYS * 24 * 60);
        }

        return $id;
    }

    // ── 로그인할 때 이 기기를 목록에 올린다 ─────────────────
    //   ★ 처음 보는 기기면 announced = false 로 남는다 → 주인에게 한 번 알린다.
    //     이미 있던 기기면 announced 는 건드리지 않는다(다시 알릴 이유가 없다).
    public function remember(Request $request, int $userId): void
    {
        $now = now();

        DB::table('user_devices')->upsert([[
            'user_id'       => $userId,
            'device_id'     => $this->deviceId($request),
            'user_agent'    => substr((string) $request->userAgent(), 0, 255),
            'first_seen_at' => $now,
            'last_login_at' => $now,
            'announced'     => 0,
        ]], ['user_id', 'device_id'], ['last_login_at', 'user_agent']);
    }

    // ── 아직 안 알린 '새 기기' 목록 (읽으면서 표시를 바꾼다) ─
    //   ★ 알림 대상은 '나보다 나중에 나타난 기기'뿐이다.
    //     지금 기기를 뺀 전부로 잡으면, 새 기기에서 로그인했을 때
    //     예전 기기들이 전부 '새 기기'로 보고돼 방향이 거꾸로 간다.
    public function takeNewDevices(Request $request, int $userId)
    {
        $me = Device::where('user_id', $userId)->where('device_id', $this->deviceId($request))->first();

        if (! $me) {
            return collect();
        }

        $new = Device::where('user_id', $userId)
            ->where('announced', false)
            ->where('device_id', '!=', $me->device_id)
            ->where('first_seen_at', '>=', $me->first_seen_at)   // 나보다 나중에 나타난 것만
            ->get();

        if ($new->isNotEmpty()) {
            Device::whereIn('id', $new->pluck('id'))->update(['announced' => true]);
        }

        return $new;
    }

    // ── 기기 하나 끊기 ──────────────────────────────────────
    //   ★ 목록에서 지우는 것만으로는 부족하다. 그 기기의 세션까지 함께 끊어야
    //     '끊었다'는 말이 사실이 된다.
    public function revoke(int $userId, string $deviceId): bool
    {
        if (! preg_match('/^[a-f0-9]{32}$/', $deviceId)) {
            return false;
        }

        $deleted = Device::where('user_id', $userId)->where('device_id', $deviceId)->delete();

        if ($deleted === 0) {
            return false;   // 내 기기가 아니거나 이미 없다
        }

        DB::table('sessions')->where('user_id', $userId)->where('device_id', $deviceId)->delete();

        return true;
    }
}
