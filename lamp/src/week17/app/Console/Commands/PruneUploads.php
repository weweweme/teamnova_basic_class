<?php

namespace App\Console\Commands;

use App\Models\Post;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;

// ============================================================
// uploads:prune — 글에 쓰이지 않는 업로드 사진을 지운다
//
//   ★ 왜 필요한가 —
//     글쓰기에서 사진을 넣고 등록하지 않으면, 파일은 이미 서버에 올라가 있는데
//     그 파일을 가리키는 글이 없다. 이런 파일은 아무도 지우지 않아 계속 쌓인다.
//     사람이 기억해서 지우는 일은 결국 안 하게 된다 → 기계가 매일 하게 둔다.
//
//   ★ 지우면 안 되는 것 —
//     · 글에 쓰이는 사진 (휴지통에 있는 글도 포함 — 되돌릴 수 있어야 한다)
//     · 초안에 쓰이는 사진 (아직 등록 안 한 글)
//     · 방금 올린 사진 (글을 쓰는 중일 수 있다 → 기본 24시간은 건드리지 않는다)
// ============================================================
class PruneUploads extends Command
{
    protected $signature = 'uploads:prune
                            {--hours=24 : 이 시간보다 오래된 파일만 검사한다}
                            {--dry-run : 지우지 않고 목록만 보여준다}';

    protected $description = '어느 글에도 쓰이지 않는 업로드 사진을 지운다';

    public function handle(): int
    {
        $dir = public_path('uploads/posts');

        if (! is_dir($dir)) {
            $this->info('업로드 폴더가 없습니다. 할 일 없음.');

            return self::SUCCESS;
        }

        // ── ① 지금 쓰이고 있는 파일 이름을 모두 모은다 ──────────
        //   withTrashed() — 휴지통에 든 글의 사진도 '쓰이는 중'이다.
        $inUse = [];

        foreach (Post::withTrashed()->pluck('content') as $content) {
            $inUse += $this->filenamesIn($content);
        }

        foreach (DB::table('drafts')->pluck('content') as $content) {
            $inUse += $this->filenamesIn($content);
        }

        // ── ② 폴더를 훑으며 쓰이지 않는 것을 고른다 ─────────────
        $cutoff  = now()->subHours((int) $this->option('hours'))->getTimestamp();
        $deleted = 0;
        $freed   = 0;

        foreach (scandir($dir) as $name) {
            if ($name === '.' || $name === '..' || $name === '.htaccess') {
                continue;
            }

            $path = $dir . DIRECTORY_SEPARATOR . $name;

            if (! is_file($path) || isset($inUse[$name]) || filemtime($path) > $cutoff) {
                continue;
            }

            $size = filesize($path);

            if ($this->option('dry-run')) {
                $this->line('지울 것: ' . $name . ' (' . $this->mb($size) . ')');
            } else {
                @unlink($path);
            }

            $deleted++;
            $freed += $size;
        }

        $this->info(($this->option('dry-run') ? '[시험] ' : '')
            . '쓰이지 않는 사진 ' . $deleted . '장 · ' . $this->mb($freed));

        return self::SUCCESS;
    }

    // 본문에서 ![](/uploads/posts/이름.webp) 의 '이름.webp' 만 뽑는다.
    //   ★ 키로 담는다 — 장수가 많아도 '들어 있나' 확인이 빠르다.
    private function filenamesIn(?string $content): array
    {
        preg_match_all('#/uploads/posts/([A-Za-z0-9_.-]+)#', (string) $content, $m);

        return array_fill_keys($m[1], true);
    }

    private function mb(int $bytes): string
    {
        return round($bytes / 1024 / 1024, 2) . 'MB';
    }
}
