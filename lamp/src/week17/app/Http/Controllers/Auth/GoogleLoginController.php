<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Notifications\NewDeviceLogin;
use App\Services\DeviceKey;
use App\Services\DeviceTracker;
use App\Services\GoogleOAuth;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Str;
use Throwable;

// ============================================================
// GoogleLoginController — 구글 계정으로 로그인 · 가입
//
//   ★ 우리 회원가입은 아이디와 비밀번호를 받는다. 구글로 들어온 사람에게는 둘 다 없다.
//     그래서 처음 들어오면 계정을 대신 만들어 준다(자동 가입).
//     아이디는 이메일 앞부분에서 만들고, 겹치면 뒤에 숫자를 붙인다.
//     화면에 보이는 이름(닉네임)은 설정에서 바꿀 수 있으므로 여기서 정해도 괜찮다.
//
//   ★ 구글 계정과 기존 계정은 별개로 둔다 —
//     이메일이 같다고 자동으로 이어 붙이지 않는다. 안전한지와는 별개로,
//     구글 버튼을 눌렀는데 아이디로 만든 계정에 로그인되는 것은 예상 밖의 일이다.
//     '연결하기'를 따로 만들 수도 있지만, 그러면 연결 해제 시 잠기지 않게 막는 일까지
//     딸려 온다. 이 규모에서 치를 비용이 아니라고 보아 넣지 않았다.
//
//   ★ 그래서 찾는 순서는 둘뿐이다 —
//     ① google_id 가 같은 사람 — 전에 구글로 들어온 적이 있다
//     ② 없으면 새로 만든다
//
//   ★ 로그인에 필요한 값은 google_id 하나다. 이메일은 메일 알림 기능의 값이라
//     여기서 채우지 못해도 로그인은 그대로 된다.
// ============================================================
class GoogleLoginController extends Controller
{
    // ── 구글로 보내기 (GET /auth/google) ────────────────────
    public function redirect(Request $request, GoogleOAuth $google)
    {
        if (! $google->configured()) {
            return redirect('/login')->with('error', '구글 로그인이 준비되지 않았습니다.');
        }

        return redirect()->away($google->redirectUrl($request));
    }

    // ── 구글에서 돌아왔을 때 (GET /auth/google/callback) ────
    public function callback(Request $request, GoogleOAuth $google, DeviceTracker $devices)
    {
        try {
            $profile = $google->userFromCallback($request);
        } catch (Throwable $e) {
            return redirect('/login')->with('error', $e->getMessage());
        }

        // 구글이 '확인되지 않은 주소'라고 하면 받지 않는다.
        //   ★ 확인 안 된 주소를 믿으면, 남의 주소를 등록해 둔 구글 계정으로
        //     그 주소의 주인 행세를 할 수 있다.
        if (! $profile['verified'] || ! $profile['email']) {
            return redirect('/login')->with('error', '구글 계정의 이메일이 확인되지 않았습니다.');
        }

        $user = $this->findOrCreate($profile);

        Auth::login($user, remember: true);

        // 아이디·비밀번호 로그인과 같은 뒷정리를 한다 — 여기만 빠뜨리면 기기 목록이 어긋난다.
        $request->session()->regenerate();
        app(DeviceKey::class)->openEnrollWindow($request);
        $devices->remember($request, $user->id);

        $new = $devices->takeNewDevices($request, $user->id);

        if ($new->isNotEmpty() && $user->email) {
            $user->notify(new NewDeviceLogin($new->map->describe()->all()));
        }

        return redirect()->intended('/')->with('status', $user->nickname . '님, 환영합니다!');
    }

    // ── 사람 찾기 · 없으면 만들기 ───────────────────────────
    private function findOrCreate(array $profile): User
    {
        // ① 전에 구글로 들어온 적이 있다
        if ($user = User::where('google_id', $profile['id'])->first()) {
            return $user;
        }

        // ② 새로 만든다
        //   ★ 비밀번호는 아무도 모르는 난수를 넣는다. 빈 값으로 두면 빈 비밀번호로
        //     로그인이 될 여지가 생긴다. 이 사람은 구글로만 들어온다.
        //
        //   ★ 이메일은 '자리가 비어 있을 때만' 채운다 —
        //     users.email 은 메일 알림 기능의 칸이고 unique 다. 로그인에 필요한 값이 아니다.
        //     그 주소를 이미 다른 계정이 쓰고 있다고 해서 로그인이 막혀서는 안 된다.
        //     비워 두면 이 사람은 그냥 '메일 주소를 아직 안 적은 회원'이 된다.
        //     나중에 설정에서 직접 정하면 되고, 그 흐름은 아이디로 가입한 사람과 똑같다.
        $emailIsFree = ! User::where('email', $profile['email'])->exists();

        return User::create([
            'username' => $this->makeUsername($profile['email']),
            'nickname' => $this->makeNickname($profile),
            'google_id' => $profile['id'],
            'password' => Str::random(40),

            // 채우는 경우에는 인증도 끝난 것으로 본다 — 구글이 이미 확인해 준 주소다.
            //   우리가 인증 메일을 한 번 더 보내는 것은 같은 사실을 두 번 확인하는 일이다.
            'email'             => $emailIsFree ? $profile['email'] : null,
            'email_verified_at' => $emailIsFree ? now() : null,
        ]);
    }

    // 이메일 앞부분으로 아이디를 만든다. 겹치면 뒤에 숫자를 붙인다.
    //   ★ 아이디는 주소(/users/아이디)에 들어가므로 영문·숫자만 남긴다.
    private function makeUsername(string $email): string
    {
        $base = Str::of($email)->before('@')->lower()->replaceMatches('/[^a-z0-9_]/', '')->limit(14, '')->value();

        if ($base === '' || is_numeric($base)) {
            $base = 'user' . $base;                     // 빈 값이나 숫자뿐이면 앞을 붙여 준다
        }

        $name = $base;

        // 겹치면 user, user2, user3 … 로 늘려 간다 (users.username 은 unique 다)
        for ($n = 2; User::where('username', $name)->exists(); $n++) {
            $name = Str::limit($base, 20 - strlen((string) $n), '') . $n;
        }

        return $name;
    }

    private function makeNickname(array $profile): string
    {
        $name = trim((string) ($profile['name'] ?? ''));

        return mb_substr($name !== '' ? $name : Str::before($profile['email'], '@'), 0, 20);
    }
}
