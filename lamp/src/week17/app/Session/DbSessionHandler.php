<?php

namespace App\Session;

use App\Services\DeviceTracker;
use Illuminate\Support\Facades\DB;
use SessionHandlerInterface;
use SessionUpdateTimestampHandlerInterface;

// ============================================================
// DbSessionHandler — 세션을 우리 sessions 표에 담는다
//   지금 includes/session_db.php 의 DbSessionHandler 를 옮긴 것이다.
//
//   [왜 Laravel 기본 database 드라이버를 안 쓰나]
//     칸 이름이 다르다. 우리 표는 id_hash · last_active · expires_at · device_id 를 쓰고,
//     Laravel 기본은 id · last_activity 를 쓴다.
//     DB를 손대지 않는다는 원칙이 있으므로 우리 표에 맞춘 드라이버를 만든다.
//     ★ '직접 세션 드라이버 만들기'는 Laravel 공식 확장 지점이다.
//
//   [★ 번호표를 그대로 저장하지 않는다]
//     세션 ID 원본을 DB에 넣으면, DB를 읽을 수 있는 사람이 그 값을 쿠키에 넣어
//     남의 로그인 상태가 될 수 있다. 그래서 sha256 '지문'만 저장한다.
//     (지문으로는 원래 번호표를 되돌릴 수 없다)
// ============================================================
class DbSessionHandler implements SessionHandlerInterface, SessionUpdateTimestampHandlerInterface
{
    // 세션 수명(초). 기존 SESSION_TTL 과 같은 30분.
    private const TTL = 1800;

    public function __construct(private string $table = 'sessions') {}

    // 번호표(세션 ID) → DB에 넣을 지문.
    //   ★ 이 함수가 유일한 지문 발급처여야 한다. 두 곳에서 만들면
    //     한쪽만 고쳐졌을 때 조용히 못 찾게 된다.
    public static function fingerprint(string $sessionId): string
    {
        return hash('sha256', $sessionId);
    }

    public function open(string $path, string $name): bool
    {
        return true;
    }

    public function close(): bool
    {
        return true;
    }

    // ★ read 와 validateId 는 조건이 같아야 한다(expires_at > NOW()).
    //   다르면 "있다고 했는데 못 읽는" 상태가 생긴다.
    public function read(string $id): string|false
    {
        $payload = DB::table($this->table)
            ->where('id_hash', self::fingerprint($id))
            ->where('expires_at', '>', now())
            ->value('payload');

        return $payload ?? '';
    }

    public function write(string $id, string $data): bool
    {
        $now = now();

        DB::table($this->table)->upsert([[
            'id_hash'     => self::fingerprint($id),
            'user_id'     => auth()->id(),
            // ★ 어느 기기의 세션인지 함께 남긴다.
            //   기기 하나를 끊을 때 '그 기기의 세션만' 골라 지우려면 이 값이 있어야 한다.
            'device_id'   => app(DeviceTracker::class)->deviceId(request()),
            'payload'     => $data,
            'ip_address'  => request()->ip(),
            'user_agent'  => substr((string) request()->userAgent(), 0, 255),
            'last_active' => $now,
            'expires_at'  => $now->copy()->addSeconds(self::TTL),
        ]], ['id_hash'], ['user_id', 'device_id', 'payload', 'ip_address', 'user_agent', 'last_active', 'expires_at']);

        return true;
    }

    public function destroy(string $id): bool
    {
        DB::table($this->table)->where('id_hash', self::fingerprint($id))->delete();

        return true;
    }

    // 청소 — 만료된 행을 지운다. PHP가 가끔 호출한다.
    public function gc(int $maxLifetime): int|false
    {
        return DB::table($this->table)->where('expires_at', '<', now())->delete();
    }

    // ★ use_strict_mode 용. "이 번호표가 우리가 발급한 것이 맞나"를 확인한다.
    //   이게 없으면 공격자가 아무 번호표나 던져서 그 값으로 세션을 만들게 할 수 있다(세션 고정).
    public function validateId(string $id): bool
    {
        return DB::table($this->table)
            ->where('id_hash', self::fingerprint($id))
            ->where('expires_at', '>', now())
            ->exists();
    }

    // 내용은 그대로이고 '살아 있다'만 알릴 때 호출된다. payload 를 다시 쓰지 않아 가볍다.
    public function updateTimestamp(string $id, string $data): bool
    {
        $now = now();

        DB::table($this->table)
            ->where('id_hash', self::fingerprint($id))
            ->update([
                'last_active' => $now,
                'expires_at'  => $now->copy()->addSeconds(self::TTL),
            ]);

        return true;
    }
}
