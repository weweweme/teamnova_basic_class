<?php

namespace App\Notifications;

use Illuminate\Auth\Notifications\VerifyEmail;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;

// ============================================================
// QueuedVerifyEmail — 이메일 인증 메일을 큐에 태우기 위한 껍데기
//
//   ★ 인증 메일의 내용과 링크(서명·만료)를 만드는 일은 프레임워크의 VerifyEmail 이
//     이미 하고 있다. 우리가 바꾸려는 것은 '언제 보내는가' 하나뿐이다.
//     그래서 그 클래스를 물려받아 ShouldQueue 만 붙인다. 내용은 한 줄도 건드리지 않는다.
//
//   ★ 같은 이유로 ResetPassword 도 QueuedResetPassword 로 감싼다.
// ============================================================
class QueuedVerifyEmail extends VerifyEmail implements ShouldQueue
{
    use Queueable;
}
