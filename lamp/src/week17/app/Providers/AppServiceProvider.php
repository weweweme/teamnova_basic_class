<?php

namespace App\Providers;

use App\Session\DbSessionHandler;
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

        //
    }
}
