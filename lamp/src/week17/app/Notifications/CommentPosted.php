<?php

namespace App\Notifications;

use App\Models\Comment;
use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Support\Facades\URL;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Notifications\Notification;

// ============================================================
// CommentPosted — 내 글에 댓글이 달렸을 때 보내는 메일
//
//   ★ 화면에 뜨는 알림(우리 notifications 표)은 그대로 두고, 메일 한 통을 더 보낸다.
//     보내는 일 자체는 우리가 하지 않는다 — 어떤 경로로, 무슨 내용을 보낼지만 적는다.
//
//   ★ 지금은 메일 서버가 없어 .env 의 MAIL_MAILER=log 로 되어 있다.
//     실제로 나가는 대신 storage/logs/laravel.log 에 본문이 남는다.
//     서버가 생기면 .env 한 줄만 smtp 로 바꾸면 되고 이 파일은 그대로다.
//   ★ ShouldQueue — '이 알림은 지금 보내지 말고 할 일 목록에 적어 두라'는 표시다.
//     붙이기 전: 메일 서버에 연결해 한 통 보내는 3.8초 동안 사용자의 화면이 멈춰 있었다.
//     붙인 뒤  : jobs 표에 한 줄 적고(0.01초) 화면은 곧바로 넘어간다.
//                실제 발송은 뒤에서 도는 워커가 한다. 실패하면 정해진 횟수만큼 다시 시도한다.
//     우리가 고친 것은 이 한 줄(implements ShouldQueue)과 use Queueable 뿐이고,
//     '무엇을 보낼지' 적어 둔 아래 코드는 하나도 바뀌지 않았다.
// ============================================================
class CommentPosted extends Notification implements ShouldQueue
{
    use Queueable;

    public function __construct(
        private Comment $comment,
        private string $type,          // comment = 내 글에 댓글 / reply = 내 댓글에 답글
    ) {}

    // 어떤 경로로 보낼지. 여러 개를 적으면 동시에 나간다 (메일·SMS·슬랙 …)
    public function via(object $notifiable): array
    {
        return ['mail'];
    }

    public function toMail(object $notifiable): MailMessage
    {
        $post   = $this->comment->post;
        $writer = $this->comment->author->nickname;

        // 그 댓글이 몇 페이지에 있는지까지 계산해서 링크를 만든다.
        //   ★ 안 그러면 메일 링크를 눌러도 1페이지가 열려 정작 그 댓글이 안 보인다.
        $page = $this->comment->pageNumber();
        $url  = url("/posts/{$post->id}" . ($page > 1 ? "?cpage={$page}" : '') . "#c{$this->comment->id}");

        $headline = $this->type === 'reply'
            ? "{$writer}님이 회원님의 댓글에 답글을 남겼습니다."
            : "{$writer}님이 회원님의 글에 댓글을 남겼습니다.";

        return (new MailMessage)
            ->subject("[리뷰 커뮤니티] {$post->title}")
            ->greeting("{$notifiable->nickname}님,")
            ->line($headline)
            ->line('― ' . \Illuminate\Support\Str::limit($this->comment->content, 80))
            ->action('댓글 보러 가기', $url)
            // ★ 알림 그만 받기 — 로그인 없이 눌러야 해서 서명된 주소를 쓴다.
            //   주소를 고치면 서명이 깨져 403 이 된다. 만료는 두지 않는다 (언제 열어 볼지 모르므로).
            ->line('이 알림을 그만 받으시려면 [여기](' . URL::signedRoute('notify.unsubscribe', ['user' => $notifiable->id]) . ')를 눌러 주세요.')
            ->salutation('리뷰 커뮤니티 드림');
    }
}
