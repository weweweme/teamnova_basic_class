<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

// ============================================================
// RememberPublicUrl — '로그인 없이도 볼 수 있던 마지막 화면'을 기억한다
//
//   [무엇에 쓰나] 로그아웃한 뒤 돌아갈 자리.
//     로그아웃 버튼은 상단바에 있어서 글쓰기·설정처럼 로그인 전용 화면에서도 눌린다.
//     거기서 '방금 있던 화면'으로 돌려보내면 곧바로 로그인 화면으로 튕겨서
//     로그아웃이 실패한 것처럼 보인다.
//     그래서 '방금 있던 화면'이 아니라 '로그인 없이도 볼 수 있던 마지막 화면'을 기억한다.
//
//   [예] 작품 게시판 → 글쓰기 → 로그아웃  ⇒  작품 게시판으로 돌아간다
//        (글쓰기는 로그인 전용이라 애초에 기억하지 않았다)
//
//   ★ 지금 있는 화면이 공개 화면이면 그 화면이 곧 마지막으로 기억된 값이 된다.
//     그래서 "공개 화면에서 로그아웃하면 제자리"와 "전용 화면에서 로그아웃하면 직전 공개 화면"이
//     규칙 하나로 같이 처리된다.
//
//   ※ 로그인 때 돌아가는 자리(intended)와는 다른 값이다.
//     intended 는 '막혀서 못 본 화면'이고, 이건 '보고 있던 화면'이다.
// ============================================================
class RememberPublicUrl
{
    public const KEY = 'last_public_url';

    // 공개 화면이지만 돌아가 봐야 의미가 없는 곳
    private const SKIP = ['login', 'register', 'confirm-password', 'logout'];

    public function handle(Request $request, Closure $next)
    {
        $response = $next($request);

        if ($this->isScreen($request, $response) && $this->isPublic($request)) {
            $request->session()->put(self::KEY, $request->fullUrl());
        }

        return $response;
    }

    // 이게 '사람이 보고 있던 화면'인가.
    //   ★ 응답을 보고 판단한다. 주소 목록을 따로 적어 두면 새 주소를 추가할 때마다 여기도 고쳐야 하고,
    //     한 번 빠뜨리면 엉뚱한 자리로 돌아가는 버그가 된다.
    //     · GET      — POST 주소를 기억하면 돌아갔을 때 값 없는 빈 요청이 된다
    //     · 200대    — 리다이렉트나 오류 화면은 머물던 자리가 아니다
    //     · text/html — JS가 배경에서 받아 가는 JSON(/api/row 등)은 화면이 아니다
    private function isScreen(Request $request, Response $response): bool
    {
        return $request->isMethod('GET')
            && $response->isSuccessful()
            && str_contains((string) $response->headers->get('Content-Type'), 'text/html');
    }

    // 이 주소가 로그인 없이도 볼 수 있는 화면인가.
    //   ★ 여기도 화면 목록을 적지 않는다. 라우트에 붙은 미들웨어를 직접 본다
    //     — 그래야 새 화면에 auth 를 붙일 때 이 파일을 같이 고치는 걸 잊어도 틀리지 않는다.
    private function isPublic(Request $request): bool
    {
        $route = $request->route();
        if ($route === null || in_array($route->uri(), self::SKIP, true)) {
            return false;
        }

        foreach ($route->gatherMiddleware() as $middleware) {
            if (is_string($middleware) && str_starts_with($middleware, 'auth')) {
                return false;
            }
        }

        return true;
    }
}
