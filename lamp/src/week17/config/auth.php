<?php

use App\Models\User;

return [

    /*
    |--------------------------------------------------------------------------
    | Authentication Defaults
    |--------------------------------------------------------------------------
    |
    | This option defines the default authentication "guard" and password
    | reset "broker" for your application. You may change these values
    | as required, but they're a perfect start for most applications.
    |
    */

    'defaults' => [
        'guard' => env('AUTH_GUARD', 'web'),
        'passwords' => env('AUTH_PASSWORD_BROKER', 'users'),
    ],

    /*
    |--------------------------------------------------------------------------
    | Authentication Guards
    |--------------------------------------------------------------------------
    |
    | Next, you may define every authentication guard for your application.
    | Of course, a great default configuration has been defined for you
    | which utilizes session storage plus the Eloquent user provider.
    |
    | All authentication guards have a user provider, which defines how the
    | users are actually retrieved out of your database or other storage
    | system used by the application. Typically, Eloquent is utilized.
    |
    | Supported: "session"
    |
    */

    'guards' => [
        'web' => [
            'driver' => 'session',
            'provider' => 'users',
        ],
    ],

    /*
    |--------------------------------------------------------------------------
    | User Providers
    |--------------------------------------------------------------------------
    |
    | All authentication guards have a user provider, which defines how the
    | users are actually retrieved out of your database or other storage
    | system used by the application. Typically, Eloquent is utilized.
    |
    | If you have multiple user tables or models you may configure multiple
    | providers to represent the model / table. These providers may then
    | be assigned to any extra authentication guards you have defined.
    |
    | Supported: "database", "eloquent"
    |
    */

    'providers' => [
        'users' => [
            'driver' => 'eloquent',
            'model' => env('AUTH_MODEL', User::class),
        ],

        // 'users' => [
        //     'driver' => 'database',
        //     'table' => 'users',
        // ],
    ],

    /*
    |--------------------------------------------------------------------------
    | Resetting Passwords
    |--------------------------------------------------------------------------
    |
    | These configuration options specify the behavior of Laravel's password
    | reset functionality, including the table utilized for token storage
    | and the user provider that is invoked to actually retrieve users.
    |
    | The expiry time is the number of minutes that each reset token will be
    | considered valid. This security feature keeps tokens short-lived so
    | they have less time to be guessed. You may change this as needed.
    |
    | The throttle setting is the number of seconds a user must wait before
    | generating more password reset tokens. This prevents the user from
    | quickly generating a very large amount of password reset tokens.
    |
    */

    'passwords' => [
        'users' => [
            'provider' => 'users',
            'table' => env('AUTH_PASSWORD_RESET_TOKEN_TABLE', 'password_reset_tokens'),
            'expire' => 60,
            'throttle' => 60,
        ],
    ],

    /*
    |--------------------------------------------------------------------------
    | Password Confirmation Timeout
    |--------------------------------------------------------------------------
    |
    | Here you may define the number of seconds before a password confirmation
    | window expires and users are asked to re-enter their password via the
    | confirmation screen. By default, the timeout lasts for three hours.
    |
    */

    // ★ 비밀번호를 한 번 확인하면 이 시간 동안 다시 묻지 않는다(sudo 창).
    //   Laravel 기본은 3시간(10800초)인데, 우리 프로젝트는 15분(900초)으로 정해 두었다.
    //   settings/reauth.php + SUDO_WINDOW = 900 이 하던 일이 이 한 줄이다.
    'password_timeout' => env('AUTH_PASSWORD_TIMEOUT', 900),

    /*
    |--------------------------------------------------------------------------
    | Idle Timeout
    |--------------------------------------------------------------------------
    |
    | 아무 동작이 없을 때 자동 로그아웃까지의 시간(초).
    | ★ 세션 수명(SESSION_LIFETIME=30분)보다 반드시 짧아야 한다.
    |   반대면 세션이 먼저 끊겨 안내를 띄울 기회가 없다.
    |
    */

    'idle_timeout' => env('AUTH_IDLE_TIMEOUT', 1200),

    /*
    |--------------------------------------------------------------------------
    | Device Key
    |--------------------------------------------------------------------------
    |
    | 기기 '도장'(공개키 서명)을 필수로 할 것인가.
    | 켜면 브라우저 JS가 있어야 화면을 열 수 있다. JS를 붙이기 전에는 꺼 둔다.
    |
    */

    'device_key_required' => env('AUTH_DEVICE_KEY_REQUIRED', false),

    // 도장 확인이 유효한 시간(초).
    'device_key_proof_ttl' => env('AUTH_DEVICE_KEY_PROOF_TTL', 60),

];
