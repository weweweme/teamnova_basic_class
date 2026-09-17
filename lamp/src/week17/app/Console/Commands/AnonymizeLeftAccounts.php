<?php

namespace App\Console\Commands;

use App\Services\Accounts;
use App\Models\User;
use Illuminate\Console\Command;

// ============================================================
// accounts:anonymize — 유예 기간이 지난 탈퇴 계정의 개인정보를 실제로 지운다
//
//   ★ 왜 필요한가 —
//     deleted_at 만 찍는 것은 '화면에서 감추기'다. 아이디·이메일·구글 연결은
//     DB 에 그대로 읽을 수 있게 남아 있다. 그건 지운 것이 아니다.
//     유예 기간이 지나면 그 값들을 실제로 비워야 한다.
//
//   ★ 줄은 왜 남기나 —
//     글과 댓글을 남기기로 했고(대화가 끊기지 않게), 글은 작성자를 가리켜야 한다.
//     개인정보를 비운 줄은 더 이상 누구의 것도 아니다 —
//     '누가 썼는지 알 수 없는 글'이 되는 것이고, 그것이 목적이다.
//
//   ★ 사람이 기억해서 하는 일로 두지 않는다. 예약 작업이 매일 돈다.
// ============================================================
class AnonymizeLeftAccounts extends Command
{
    protected $signature = 'accounts:anonymize {--dry-run : 지우지 않고 대상만 보여준다}';

    protected $description = '유예 기간이 지난 탈퇴 계정의 개인정보를 비운다';

    public function handle(Accounts $accounts): int
    {
        $cutoff = now()->subDays(Accounts::GRACE_DAYS);

        $targets = User::onlyTrashed()
            ->whereNull('anonymized_at')
            ->where('deleted_at', '<=', $cutoff)
            ->get();

        if ($targets->isEmpty()) {
            $this->info('대상 없음 (' . Accounts::GRACE_DAYS . '일이 지난 탈퇴 계정이 없습니다).');

            return self::SUCCESS;
        }

        foreach ($targets as $user) {
            $this->line('  ' . $user->username . ' (탈퇴 ' . $user->deleted_at->format('Y-m-d') . ')');

            if ($this->option('dry-run')) {
                continue;
            }

            $accounts->erase($user);
        }

        $this->newLine();
        $this->info(($this->option('dry-run') ? '[시험] ' : '') . $targets->count() . '개 계정 처리');

        return self::SUCCESS;
    }
}
