<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Attributes\Hidden;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Foundation\Auth\User as Authenticatable;

// ============================================================
// User — users 표
//   기본 생성 파일은 Laravel 표준 스키마(name·email·remember_token)를 전제로 한다.
//   우리 표는 id · username · nickname · password · avatar · joined_at 뿐이라 맞춰 고쳤다.
//
//   ★ Authenticatable 을 상속한다 — '로그인할 수 있는 대상'이라는 뜻이다.
//     실제 로그인 처리는 2단계에서 붙인다.
// ============================================================
#[Fillable(['username', 'nickname', 'password', 'avatar'])]
#[Hidden(['password'])]
class User extends Authenticatable
{
    // ── 시각 컬럼이 하나뿐 ──────────────────────────────────
    //   Laravel 기본은 created_at + updated_at 두 개인데 우리는 joined_at 하나다.
    //     CREATED_AT = 'joined_at' → 가입 시각은 이 칸에 넣는다
    //     UPDATED_AT = null        → 수정 시각은 쓰지 않는다
    const CREATED_AT = 'joined_at';
    const UPDATED_AT = null;

    // ── 비밀번호 자동 해시 ──────────────────────────────────
    //   'hashed' 를 걸어 두면 $user->password = '평문' 만 해도 저장 시 해시된다.
    //   ★ 기존 회원 25명의 해시가 bcrypt cost 12 라 Laravel 기본값과 같다.
    //     그래서 비밀번호를 바꾸지 않아도 그대로 로그인된다.
    protected function casts(): array
    {
        return ['password' => 'hashed'];
    }

    // 이 사람이 쓴 글
    public function posts(): HasMany
    {
        return $this->hasMany(Post::class, 'author_id');
    }

    // ── 안 읽은 알림 ───────────────────────────────────────
    //   상단바 🔔 뱃지에 쓴다.
    //   ★ 화면에서 모델 클래스를 직접 부르지 않으려고 관계로 만들어 둔다.
    //     화면은 auth()->user()->unreadNotifications()->count() 만 쓰면 된다.
    public function unreadNotifications(): HasMany
    {
        return $this->hasMany(Notification::class, 'user_id')->where('is_read', false);
    }

    // ※ 기본 파일에 있던 HasFactory·Notifiable 은 뺐다.
    //    · HasFactory  — 테스트용 더미 생성기. 우리 표 모양과 맞지 않는다
    //    · Notifiable  — Laravel 방식 알림용. 우리는 notifications 표를 따로 쓰고 있다 (4단계에서 결정)
    //
    // ⚠ remember_token 컬럼이 우리 표에 없다.
    //    Laravel의 '로그인 유지'를 켜면 그 칸을 찾다가 실패한다.
    //    우리는 remember_tokens 표를 따로 쓰고 있으므로 2단계에서 어느 쪽을 쓸지 정한다.
}
