<?php

namespace App\Providers;

use App\Session\DbSessionHandler;
use App\Support\Highlight;
use Illuminate\Auth\Notifications\VerifyEmail;
use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Support\Facades\Blade;
use Illuminate\Pagination\Paginator;
use Illuminate\Support\Facades\Session;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        //
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        // ── 세션을 우리 sessions 표에 담는 드라이버 등록 ────────
        //   ★ Laravel 공식 확장 지점이다. 이렇게 등록해 두면
        //     .env 의 SESSION_DRIVER=db 로 골라 쓸 수 있다.
        Session::extend('db', fn () => new DbSessionHandler());

        // ── 페이지 번호를 우리 화면으로 ─────────────────────
        //   ★ Laravel 기본 출력은 Tailwind 용 HTML 이라 우리 style.css 와 맞지 않는다.
        //     기존 board/index.php 의 마크업(.pagination > .page-nav / .page-num)을 그대로 옮긴
        //     화면을 쓰게 지정한다.
        Paginator::defaultView('vendor.pagination.default');

        // ── 검색 결과 형광펜 ───────────────────────────────
        //   ★ @highlight($글, $검색어) 로 쓴다. Blade 에 우리 표현을 하나 더하는 것이다.
        //     결과는 이미 안전 처리된 HTML 이라 {{ }} 가 아니라 그대로 출력한다.
        Blade::directive('highlight', fn ($args) => "<?php echo \App\Support\Highlight::of({$args}); ?>");
        Blade::directive('snippet',   fn ($args) => "<?php echo \App\Support\Highlight::snippet({$args}); ?>");

        // ── 이메일 확인 메일 문구 ───────────────────────────
        //   ★ 공식 확장 지점이다. 링크를 만드는 일(서명·만료)은 그대로 두고,
        //     메일에 적히는 말만 우리 것으로 바꾼다.
        //     기본 문구는 '가입하지 않았다면 무시하세요'인데, 우리는 가입이 아니라
        //     설정에서 주소를 넣을 때 보내는 메일이라 맞지 않는다.
        VerifyEmail::toMailUsing(function (object $notifiable, string $url) {
            return (new MailMessage)
                ->subject('[리뷰 커뮤니티] 이메일 인증을 완료해 주세요')
                ->greeting($notifiable->nickname . '님, 안녕하세요.')
                ->line('리뷰 커뮤니티 계정에 이 주소를 등록하려고 합니다.')
                ->line('아래 버튼을 누르면 인증이 끝나고, 그때부터 댓글 알림 메일을 켤 수 있습니다.')
                ->action('이메일 인증하기', $url)
                ->line('이 링크는 ' . config('auth.verification.expire', 60) . '분 뒤에 만료됩니다.')
                ->line('주소를 등록한 적이 없다면 이 메일을 무시하셔도 됩니다. 아무 일도 일어나지 않습니다.')
                ->salutation('리뷰 커뮤니티 드림');
        });
    }
}
