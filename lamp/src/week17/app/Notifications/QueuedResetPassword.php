<?php

namespace App\Notifications;

use Illuminate\Auth\Notifications\ResetPassword;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;

// ============================================================
// QueuedResetPassword — 비밀번호 찾기 메일을 큐에 태우기 위한 껍데기
//   내용·토큰·만료는 그대로 두고 '언제 보내는가'만 바꾼다.
// ============================================================
class QueuedResetPassword extends ResetPassword implements ShouldQueue
{
    use Queueable;
}
