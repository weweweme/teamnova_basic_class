<?php

namespace App\Services;

use App\Models\Post;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cookie;
use Illuminate\Support\Facades\DB;

// ============================================================
// ViewCounter — 조회수 올리기 판정과 집계
//   지금 includes/view_session.php(192줄) + count_post_view() 가 하던 일이다.
//
//   ★ 여기는 '옮기는' 쪽이다. 프레임워크에 대응 기능이 없다 — 규칙 자체가 우리 것이기 때문이다.
// ============================================================
class ViewCounter
{
    // 같은 사람이 같은 글을 다시 열어도 안 올리는 기간(분). 그 글을 본 시각부터 24시간.
    private const WINDOW_MINUTES = 60 * 24;

    // 판정 기록을 담는 쿠키 이름.
    //   ★ 로그인·CSRF 가 든 세션에 조회 기록을 섞지 않는다.
    //     조회 판정은 글 보기 화면에서만 필요하고 수명도 다르다(하루).
    //   ★ DB 표로 만들지 않는 이유 — '누가 무엇을 봤는지'를 서버가 한자리에 모으게 되기 때문이다.
    //     판정에 필요한 건 "이 사람이 이 글을 최근에 봤나" 하나뿐이다.
    private const COOKIE = 'viewed_posts';

    public function count(Request $request, Post $post): bool
    {
        // ① 봇은 조회수에 넣지 않는다. 사람이 안 본 만큼 부풀려질 이유가 없다.
        if ($this->isBot((string) $request->userAgent())) {
            return false;
        }

        // ② 글쓴이가 자기 글을 여는 것은 조회가 아니다.
        //   ⚠ 이 숫자가 '지금 뜨는 글'·인기 탭·랭킹을 움직인다. 자기 글을 매일 열면 그게 순위가 된다.
        if ($request->user()?->id === $post->author_id) {
            return false;
        }

        // ③ 최근에 본 글이면 올리지 않는다
        $seen = $this->seen($request);
        $now  = time();
        if (isset($seen[$post->id]) && $seen[$post->id] > $now) {
            return false;
        }

        // ④ 올린다 — 글의 누적 조회수 + 그날의 집계
        //   ★ 집계는 '달력 날짜'로 쌓는다. 판정과 기준이 다른 게 맞다.
        //     판정은 "이 사람이 최근에 봤나", 집계는 "그날 몇 번 조회됐나"이다.
        //   ⚠ 날짜를 DB의 CURDATE() 가 아니라 앱 쪽에서 만든다.
        //     DB는 UTC, 앱은 Asia/Seoul 이라 섞으면 하루의 경계가 자정이 아니게 된다.
        $post->increment('views');

        DB::table('post_view_daily')->upsert(
            [['post_id' => $post->id, 'viewed_on' => now()->toDateString(), 'views' => 1]],
            ['post_id', 'viewed_on'],           // 이 조합이 이미 있으면
            ['views' => DB::raw('views + 1')]   // 값을 더한다
        );

        // ⑤ 판정 기록 갱신 — 이 글은 24시간 동안 다시 세지 않는다
        $seen[$post->id] = $now + self::WINDOW_MINUTES * 60;
        $this->remember($seen);

        return true;
    }

    // 쿠키에 담긴 '최근에 본 글' 목록. 만료된 항목은 버린다.
    private function seen(Request $request): array
    {
        $raw = json_decode((string) $request->cookie(self::COOKIE), true);

        return is_array($raw)
            ? array_filter($raw, fn ($until) => is_int($until) && $until > time())
            : [];
    }

    private function remember(array $seen): void
    {
        // 쿠키는 4KB뿐이라 무한정 담을 수 없다. 최근 것부터 200개만 남긴다.
        $seen = array_slice($seen, -200, null, true);

        // Cookie::queue = 이번 응답에 쿠키를 실어 보낸다. 값은 자동으로 암호화된다.
        Cookie::queue(self::COOKIE, json_encode($seen), self::WINDOW_MINUTES);
    }

    // 아주 단순한 봇 판별 — 이름에 흔한 크롤러 표시가 있으면 봇으로 본다.
    private function isBot(string $userAgent): bool
    {
        if ($userAgent === '') {
            return true;   // 브라우저는 보통 비워 두지 않는다
        }

        return (bool) preg_match('/bot|crawl|spider|slurp|curl|wget|python-requests/i', $userAgent);
    }
}
