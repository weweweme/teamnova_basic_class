<?php

use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;
use Illuminate\Http\Request;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        web: __DIR__.'/../routes/web.php',
        commands: __DIR__.'/../routes/console.php',
        health: '/up',
    )
    ->withMiddleware(function (Middleware $middleware): void {
        // 로그인한 사람의 유휴 시간을 모든 화면 요청에서 확인한다.
        //   ★ 라우트마다 붙이면 새 라우트에서 빠뜨린다 → web 그룹 전체에 건다.
        $middleware->web(append: [
            \App\Http\Middleware\RememberPublicUrl::class,
            \App\Http\Middleware\ApplyCookieConsent::class,
            \App\Http\Middleware\EnsureNotIdle::class,
            \App\Http\Middleware\RequireDeviceKeyProof::class,
        ]);

        //
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        $exceptions->shouldRenderJsonWhen(
            fn (Request $request) => $request->is('api/*') || $request->expectsJson(),
        );
    })->create();
