<?php

namespace App\Notifications;

use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Notifications\Notification;

// ============================================================
// NewDeviceLogin — 처음 보는 기기에서 로그인되었을 때 보내는 보안 알림
//
//   ★ 설정으로 끌 수 없다. 본인이 하지 않은 로그인을 알리는 것이라,
//     끌 수 있으면 알림의 목적이 사라진다.
//   ★ 화면에도 같은 안내가 뜨지만 그때 못 보면 놓친다. 메일은 남는다.
// ============================================================
class NewDeviceLogin extends Notification
{
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
