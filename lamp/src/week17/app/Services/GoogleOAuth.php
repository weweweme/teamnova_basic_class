<?php

namespace App\Services;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;
use RuntimeException;

// ============================================================
// GoogleOAuth — 구글 로그인의 바깥쪽 절차
//
//   ★ 왜 Socialite 를 쓰지 않았나 —
//     Socialite 는 이 프로젝트와 의존성이 충돌한다(Guzzle 8 고정 · php-jwt 보안 권고).
//     구글 로그인은 OAuth 2.0 표준이고 실제로는 HTTP 요청 두 번이라,
//     Laravel 의 HTTP 클라이언트로 규격대로 붙였다.
//
//   ★ 흐름 (인증 코드 방식) —
//     ① 우리가 구글 로그인 주소를 만들어 사용자를 그리로 보낸다
//     ② 사용자가 구글에서 로그인하고 '허용'을 누른다
//     ③ 구글이 사용자를 우리 주소로 되돌려보내며 1회용 코드를 붙여 준다
//     ④ 우리가 그 코드를 구글에 직접 보내(브라우저를 거치지 않고) 신원을 받아 온다
//
//     ④ 를 서버끼리 하는 이유 — 브라우저를 거치면 값이 주소창에 남고 가로챌 수 있다.
//     코드만 브라우저를 지나가고, 그 코드는 한 번 쓰면 죽는다.
//
//   ★ state — ③ 에서 돌아온 것이 '우리가 보낸 그 사람'이 맞는지 확인하는 값.
//     우리가 난수를 만들어 세션에 넣고 같은 값을 구글에 보낸다. 돌아온 값이 다르면 버린다.
//     이것이 없으면 공격자가 자기 코드로 남을 자기 계정에 로그인시킬 수 있다(CSRF).
// ============================================================
class GoogleOAuth
{
    private const AUTH_URL     = 'https://accounts.google.com/o/oauth2/v2/auth';
    private const TOKEN_URL    = 'https://oauth2.googleapis.com/token';
    private const USERINFO_URL = 'https://www.googleapis.com/oauth2/v3/userinfo';

    private const STATE_KEY = 'google_oauth_state';

    public function configured(): bool
    {
        return (bool) config('services.google.client_id') && (bool) config('services.google.client_secret');
    }

    // ── ① 보낼 주소 만들기 ──────────────────────────────────
    public function redirectUrl(Request $request): string
    {
        $state = bin2hex(random_bytes(16));
        $request->session()->put(self::STATE_KEY, $state);

        return self::AUTH_URL . '?' . http_build_query([
            'client_id'     => config('services.google.client_id'),
            'redirect_uri'  => config('services.google.redirect'),
            'response_type' => 'code',
            // 필요한 것만 받는다 — 이름과 이메일. 연락처·캘린더 같은 건 요구하지 않는다.
            'scope'         => 'openid email profile',
            'state'         => $state,
            'prompt'        => 'select_account',
        ]);
    }

    // ── ③④ 돌아온 코드로 신원 받아 오기 ────────────────────
    public function userFromCallback(Request $request): array
    {
        // 보낸 값과 돌아온 값이 같은지 먼저 본다. 한 번 쓰면 버린다.
        $expected = $request->session()->pull(self::STATE_KEY);

        if (! $expected || ! is_string($request->query('state')) || ! hash_equals($expected, $request->query('state'))) {
            throw new RuntimeException('요청이 올바르지 않습니다. 다시 시도해 주세요.');
        }

        if (! $request->filled('code')) {
            throw new RuntimeException('구글 로그인이 취소되었습니다.');
        }

        // ④-1 코드를 토큰으로 바꾼다 (서버 ↔ 구글)
        $token = Http::asForm()->timeout(15)->post(self::TOKEN_URL, [
            'code'          => $request->query('code'),
            'client_id'     => config('services.google.client_id'),
            'client_secret' => config('services.google.client_secret'),
            'redirect_uri'  => config('services.google.redirect'),
            'grant_type'    => 'authorization_code',
        ]);

        if ($token->failed()) {
            throw new RuntimeException('구글에서 신원을 받아오지 못했습니다.');
        }

        // ④-2 토큰으로 사용자 정보를 받는다
        $profile = Http::withToken($token->json('access_token'))->timeout(15)->get(self::USERINFO_URL);

        if ($profile->failed() || ! $profile->json('sub')) {
            throw new RuntimeException('구글에서 사용자 정보를 받아오지 못했습니다.');
        }

        return [
            // sub = 구글이 계정마다 붙인 번호. 이메일을 바꿔도 이 값은 그대로다.
            'id'       => (string) $profile->json('sub'),
            'email'    => $profile->json('email'),
            // 구글이 '이 주소는 확인된 주소'라고 알려 준다 → 우리 인증 절차를 건너뛸 근거가 된다.
            'verified' => (bool) $profile->json('email_verified'),
            'name'     => $profile->json('name') ?: $profile->json('given_name'),
        ];
    }
}
