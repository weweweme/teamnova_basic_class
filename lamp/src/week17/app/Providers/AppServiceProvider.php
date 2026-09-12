<?php

namespace App\Providers;

use App\Session\DbSessionHandler;
use App\Support\Highlight;
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

        //
    }
}
