<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use App\Services\DeviceTracker;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Validation\ValidationException;

// ============================================================
// LoginController — 로그인 화면 · 로그인 처리 · 로그아웃
//   지금까지 auth/login.php(화면) · authenticate.php(처리) · logout.php(처리)
//   세 파일로 나뉘어 있던 것이 한 클래스의 메서드 세 개가 된다.
//
//   ★ 컨트롤러는 '요청 하나를 처리하는 메서드들의 모음'이다.
//     어떤 주소가 어느 메서드로 오는지는 routes/web.php 가 정한다.
// ============================================================
class LoginController extends Controller
{
    // ── 로그인 화면 보여주기 (GET /login) ───────────────────
    public function create()
    {
        return view('auth.login');
    }

    // ── 로그인 처리 (POST /login) ───────────────────────────
    public function store(Request $request, DeviceTracker $devices)
    {
        // ① 입력값 검사
        //   ★ 조건만 적으면 검사·오류 문구·폼 되돌리기를 프레임워크가 한다.
        //     지금 우리 코드가 if 문으로 하던 일이다.
        //     통과하면 검사된 값만 담긴 배열이 돌아온다.
        $credentials = $request->validate([
            'username' => ['required', 'string', 'max:20'],
            'password' => ['required', 'string'],
        ]);

        // ② 아이디·비밀번호 확인
        //   Auth::attempt() 가 하는 일
        //     1) users 표에서 username 이 맞는 행을 찾고
        //     2) password 를 해시 비교하고 (기존 bcrypt 해시를 그대로 검증한다)
        //     3) 맞으면 세션에 '이 사람이 로그인했다'를 기록한다
        //   ★ 우리 verify_login() + login() 이 하던 일이 이 한 줄이다.
        if (! Auth::attempt($credentials)) {
            // 실패하면 검증 오류처럼 되돌려보낸다 → 폼 위에 메시지가 뜨고 입력값이 남는다
            throw ValidationException::withMessages([
                'username' => '아이디 또는 비밀번호가 올바르지 않습니다.',
            ]);
        }

        // ③ 세션 번호표 새로 발급
        //   ★ 로그인 직후 반드시 해야 한다. 공격자가 미리 심어 둔 번호표를 그대로 쓰면
        //     그 번호표로 남의 로그인 상태를 훔칠 수 있다(세션 고정 공격).
        $request->session()->regenerate();

        // ★ 비밀번호를 방금 확인했으므로 '도장 등록 창'을 연다 (3분).
        //   아무 때나 등록을 받으면 훔친 세션으로 자기 도장을 심을 수 있다.
        app(\App\Services\DeviceKey::class)->openEnrollWindow($request);

        // ④ 이 기기를 내 기기 목록에 올린다 (처음이면 새로 추가, 이미 있으면 시각만 갱신)
        $devices->remember($request, $request->user()->id);

        // ⑤ 아직 안 알린 '새 기기'가 있으면 이번 한 번만 알린다
        //   ★ 대상은 '나보다 나중에 나타난 기기'뿐이다. 지금 기기를 뺀 전부로 잡으면
        //     새 기기에서 로그인했을 때 예전 기기들이 전부 '새 기기'로 보고된다.
        $new = $devices->takeNewDevices($request, $request->user()->id);

        $status = $new->isEmpty()
            ? '환영합니다!'
            : '환영합니다! 새로운 기기에서 로그인한 기록이 있습니다 — ' . $new->map->describe()->implode(', ');

        // intended() = 로그인 때문에 막혔던 원래 주소로 돌려보낸다. 없으면 두 번째 인자로.
        //   ★ 그 주소는 auth 미들웨어가 막으면서 세션에 적어 둔 것이다.
        //     (지금 remember_intended() 가 쿠키에 적고 take_intended() 가 꺼내던 일이다.
        //      GET 이면 지금 주소를, POST 면 직전 화면을 적는다 — 빈 POST 로 되돌려 보내면
        //      또 튕기기 때문이다. 우리가 손으로 갈라 두었던 조건과 같다.)
        //   그냥 상단 메뉴의 '로그인'을 눌러 들어온 경우에는 적힌 주소가 없다 → 홈으로.
        return redirect()->intended('/')->with('status', $status);
    }

    // ── 로그아웃 (POST /logout) ─────────────────────────────
    //   ★ 상태를 바꾸는 동작이라 링크(GET)가 아니라 폼(POST)이다.
    //     GET이면 남의 사이트에 <img src="/logout"> 만 박아도 로그아웃된다.
    public function destroy(Request $request)
    {
        Auth::logout();

        $request->session()->invalidate();      // 세션에 든 내용 전부 버림
        $request->session()->regenerateToken(); // CSRF 토큰도 새로 발급

        return redirect('/')->with('status', '로그아웃했습니다.');
    }
}
