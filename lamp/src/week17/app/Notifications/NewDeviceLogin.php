<?php

namespace App\Notifications;

use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Notifications\Notification;

// ============================================================
// NewDeviceLogin — 처음 보는 기기에서 로그인되었을 때 보내는 보안 알림
//
//   ★ 설정으로 끌 수 없다. 본인이 하지 않은 로그인을 알리는 것이라,
//     끌 수 있으면 알림의 목적이 사라진다.
//   ★ 화면에도 같은 안내가 뜨지만 그때 못 보면 놓친다. 메일은 남는다.
//   ★ ShouldQueue — '이 알림은 지금 보내지 말고 할 일 목록에 적어 두라'는 표시다.
//     붙이기 전: 메일 서버에 연결해 한 통 보내는 3.8초 동안 사용자의 화면이 멈춰 있었다.
//     붙인 뒤  : jobs 표에 한 줄 적고(0.01초) 화면은 곧바로 넘어간다.
//                실제 발송은 뒤에서 도는 워커가 한다. 실패하면 정해진 횟수만큼 다시 시도한다.
//     우리가 고친 것은 이 한 줄(implements ShouldQueue)과 use Queueable 뿐이고,
//     '무엇을 보낼지' 적어 둔 아래 코드는 하나도 바뀌지 않았다.
// ============================================================
class NewDeviceLogin extends Notification implements ShouldQueue
{
    use Queueable;

    /** @param array<int, string> $devices 기기 설명 (예: 'Windows · Chrome') */
    public function __construct(private array $devices) {}

    public function via(object $notifiable): array
    {
        return ['mail'];
    }

    public function toMail(object $notifiable): MailMessage
    {
        return (new MailMessage)
            ->subject('[리뷰 커뮤니티] 새 기기에서 로그인되었습니다')
            ->greeting("{$notifiable->nickname}님,")
            ->line('처음 보는 기기에서 로그인되었습니다.')
            ->line('― ' . implode(' · ', $this->devices))
            ->line('본인이 맞다면 아무것도 하지 않으셔도 됩니다.')
            ->line('모르는 기기라면 비밀번호를 바꾸고, 설정에서 그 기기를 끊어 주세요.')
            ->action('설정 열기', url('/settings'))
            ->salutation('리뷰 커뮤니티 드림');
    }
}
