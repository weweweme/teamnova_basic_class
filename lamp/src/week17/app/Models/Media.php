<?php

namespace App\Models;

use App\Services\Tmdb;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;

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

    // TMDB 에서 받아온 값을 그대로 넣어 만들 수 있게 열어 둔다
    protected $fillable = ['slug', 'title', 'genre', 'year', 'tmdb_id', 'poster_url', 'overview'];

    // ── 주소의 slug 로 작품 찾기 ───────────────────────────
    //   ★ 라우트에 /works/{media:slug} 라고 적으면 Laravel 이 이 함수를 부른다.
    //     기본 동작은 '표에서 찾고 없으면 404'인데, 우리 게시판은 그러면 안 된다.
    //     아직 아무도 글을 쓰지 않은 작품은 media 표에 없기 때문이다.
    //     그래서 표에 없으면 TMDB 에서 받아와 '저장하지 않은 채' 돌려준다
    //     — 화면은 열리고, 글이 처음 올라올 때 비로소 표에 들어간다.
    public function resolveRouteBinding($value, $field = null)
    {
        return self::bySlug($value);
    }

    // 표에서 찾고, 없으면 TMDB 에서 받아 '저장하지 않은 채' 돌려준다.
    //   ★ new 로만 만들고 save() 는 하지 않는다 → id 가 없는 '읽기용' 작품이다.
    //     글이 처음 올라올 때 ensureBySlug() 가 비로소 표에 넣는다.
    public static function bySlug(string $slug): ?self
    {
        $found = self::where('slug', $slug)->first();
        if ($found) {
            return $found;
        }

        $info = self::infoFromTmdb($slug);

        return $info ? new self($info) : null;
    }

    // ── 글을 쓰기 직전에 표에 들어와 있게 한다 ──────────────
    //   ★ 글의 media_id 외래키가 걸리려면 작품이 먼저 표에 있어야 한다.
    //     이미 있으면 그대로, 없으면 TMDB 에서 받아 저장하고 돌려준다.
    public static function ensureBySlug(string $slug): ?self
    {
        $found = self::where('slug', $slug)->first();
        if ($found) {
            return $found;
        }

        $info = self::infoFromTmdb($slug);

        return $info ? self::create($info) : null;
    }

    // 'tmdb-496243' 처럼 생긴 slug 를 TMDB 에 물어본다 (아니면 null)
    private static function infoFromTmdb(string $slug): ?array
    {
        if (! str_starts_with($slug, 'tmdb-')) {
            return null;
        }

        return app(Tmdb::class)->findById((int) substr($slug, 5));
    }

    // 이 작품에 달린 글들
    public function posts(): HasMany
    {
        return $this->hasMany(Post::class, 'media_id');
    }

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
