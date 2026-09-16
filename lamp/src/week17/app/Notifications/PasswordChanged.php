<?php

namespace App\Notifications;

use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Notifications\Notification;

// ============================================================
// PasswordChanged — 비밀번호가 바뀌었을 때 보내는 보안 알림
//
//   ★ 이것도 끌 수 없다. 계정을 빼앗겼을 때 본인이 알아챌 마지막 통로다.
// ============================================================
class PasswordChanged extends Notification
{
    public function via(object $notifiable): array
    {
        return ['mail'];
    }

    public function toMail(object $notifiable): MailMessage
    {
        return (new MailMessage)
            ->subject('[리뷰 커뮤니티] 비밀번호가 변경되었습니다')
            ->greeting("{$notifiable->nickname}님,")
            ->line('방금 비밀번호가 변경되었습니다. 다른 기기의 로그인도 함께 해제되었습니다.')
            ->line('본인이 바꾼 것이 아니라면 바로 비밀번호 찾기로 계정을 되찾아 주세요.')
            ->action('비밀번호 찾기', url('/forgot-password'))
            ->salutation('리뷰 커뮤니티 드림');
    }
}
