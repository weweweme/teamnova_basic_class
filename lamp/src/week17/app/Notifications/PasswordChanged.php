<?php

namespace App\Notifications;

use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Notifications\Notification;

// ============================================================
// PasswordChanged — 비밀번호가 바뀌었을 때 보내는 보안 알림
//
//   ★ 이것도 끌 수 없다. 계정을 빼앗겼을 때 본인이 알아챌 마지막 통로다.
//   ★ ShouldQueue — '이 알림은 지금 보내지 말고 할 일 목록에 적어 두라'는 표시다.
//     붙이기 전: 메일 서버에 연결해 한 통 보내는 3.8초 동안 사용자의 화면이 멈춰 있었다.
//     붙인 뒤  : jobs 표에 한 줄 적고(0.01초) 화면은 곧바로 넘어간다.
//                실제 발송은 뒤에서 도는 워커가 한다. 실패하면 정해진 횟수만큼 다시 시도한다.
//     우리가 고친 것은 이 한 줄(implements ShouldQueue)과 use Queueable 뿐이고,
//     '무엇을 보낼지' 적어 둔 아래 코드는 하나도 바뀌지 않았다.
// ============================================================
class PasswordChanged extends Notification implements ShouldQueue
{
    use Queueable;

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
