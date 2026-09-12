<?php

namespace App\Services;

use Illuminate\Support\Facades\DB;

// ============================================================
// DraftBox — 글쓰기 초안 저장·불러오기
//   지금 includes/drafts.php 를 옮긴 것이다.
//
//   ★ 초안은 '작품별로 하나'다. 같은 작품에 다시 쓰면 덮어쓴다.
// ============================================================
class DraftBox
{
    // 한 사람이 갖고 있을 수 있는 초안 수. 넘으면 오래된 것부터 버린다.
    private const MAX = 5;

    public function save(int $userId, string $workSlug, array $values): void
    {
        // 셋 다 비어 있으면 저장할 이유가 없다 (빈 초안이 쌓이는 걸 막는다)
        if (trim($values['title'] ?? '') === '' && trim($values['content'] ?? '') === '') {
            $this->forget($userId, $workSlug);

            return;
        }

        DB::table('drafts')->upsert([[
            'user_id'    => $userId,
            'work_slug'  => $workSlug,
            'title'      => mb_substr((string) ($values['title'] ?? ''), 0, 200),
            'content'    => (string) ($values['content'] ?? ''),
            'sentiment'  => (string) ($values['sentiment'] ?? ''),
            'updated_at' => now(),
        ]], ['user_id', 'work_slug'], ['title', 'content', 'sentiment', 'updated_at']);

        $this->trim($userId);
    }

    public function get(int $userId, string $workSlug): ?object
    {
        return DB::table('drafts')->where('user_id', $userId)->where('work_slug', $workSlug)->first();
    }

    public function forget(int $userId, string $workSlug): void
    {
        DB::table('drafts')->where('user_id', $userId)->where('work_slug', $workSlug)->delete();
    }

    // ── 개수 제한 ───────────────────────────────────────────
    //   ★ '지울 것'을 고르는 게 아니라 '남길 것'을 먼저 고르고 그 밖을 지운다.
    //     LIMIT 을 DELETE 에 직접 못 쓰는 경우가 있어 이 방식이 안전하다.
    private function trim(int $userId): void
    {
        $keep = DB::table('drafts')->where('user_id', $userId)
            ->orderByDesc('updated_at')->limit(self::MAX)->pluck('work_slug');

        if ($keep->count() < self::MAX) {
            return;
        }

        DB::table('drafts')->where('user_id', $userId)
            ->whereNotIn('work_slug', $keep)->delete();
    }
}
