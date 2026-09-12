<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Casts\Attribute;
use Illuminate\Support\HtmlString;
use Illuminate\Database\Eloquent\Attributes\Hidden;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
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

    // ── 등급 배지 ───────────────────────────────────────────
    //   ★ 표에 없는 값을 속성처럼 만든다(접근자). $user->level 로 읽는다.
    //     지금 includes/level.php 의 user_level() 이 하던 일이다.
    //     화면마다 계산식을 복붙하지 않아도 되고, 기준이 한 곳에만 있다.
    protected function level(): Attribute
    {
        return Attribute::get(function () {
            $count = $this->posts_count ?? $this->posts()->count();

            // [글 수 기준, 배지, 등급 이름] — 위에서부터 처음 걸리는 칸이 그 사람의 등급이다.
            //   ★ 기준을 여기 한 곳에만 두면 화면마다 계산식을 베끼지 않아도 된다.
            foreach ([[10, '👑', '시네필'], [6, '🎖️', '평론가'], [3, '✍️', '리뷰어'], [1, '🎬', '관람객']] as [$min, $badge, $name]) {
                if ($count >= $min) {
                    return ['badge' => $badge, 'name' => $name];
                }
            }

            return ['badge' => '🌱', 'name' => '새싹'];
        });
    }

    // 화면에 그대로 넣는 배지 한 조각.
    //   ★ 지금 level_badge_html() 이 하던 일이다. 마우스를 올리면 등급 이름이 뜬다.
    protected function levelBadge(): Attribute
    {
        return Attribute::get(fn () => new HtmlString(
            '<span class="lvl-badge" title="' . e($this->level['name']) . '">' . $this->level['badge'] . '</span>'
        ));
    }

    // 이 사람이 쓴 글
    public function posts(): HasMany
    {
        return $this->hasMany(Post::class, 'author_id');
    }

    // 이 사람이 쓴 댓글 (유저 검색에서 활동량으로 보여준다)
    public function comments(): HasMany
    {
        return $this->hasMany(Comment::class, 'author_id');
    }

    // ── 내가 추천한 글 (likes 표) ───────────────────────────
    //   ★ Post::likers() 의 반대 방향이다. 같은 이어주는 표를 양쪽에서 본다.
    public function likedPosts(): BelongsToMany
    {
        return $this->belongsToMany(Post::class, 'likes', 'user_id', 'post_id');
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
