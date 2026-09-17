<?php

namespace App\Http\Controllers\Auth;

use App\Http\Controllers\Controller;
use App\Http\Controllers\AccountController;
use App\Models\User;
use App\Services\Consent;
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
    // 구글에서 받아 온 정보를 '가입 마무리' 화면까지 잠깐 들고 있는 자리.
    //   ★ 세션에 둔다 — 서버에만 있고 사용자가 고칠 수 없다.
    private const PENDING_KEY = 'google_pending_signup';

    // ── 구글로 보내기 (GET /auth/google) ────────────────────
    public function redirect(Request $request, GoogleOAuth $google)
    {
        if (! $google->configured()) {
            return redirect('/login')->with('error', '구글 로그인이 준비되지 않았습니다.');
        }

        return redirect()->away($google->redirectUrl($request));
    }

    // ── 본인 확인용으로 구글에 다녀오기 (GET /auth/google/confirm) ──
    //   ★ 구글로 가입한 사람은 비밀번호를 모른다(난수로 채워 둔다).
    //     그래서 '지금 본인이 맞나'를 비밀번호로 물을 수 없다.
    //     로그인할 때 쓴 그 수단으로 다시 확인받는다 — GitHub 이 쓰는 방식과 같다.
    public function confirm(Request $request, GoogleOAuth $google)
    {
        if (! $google->configured() || ! $request->user()->google_id) {
            return redirect('/confirm-password')->with('error', '구글로 확인할 수 없는 계정입니다.');
        }

        return redirect()->away($google->redirectUrl($request, 'confirm'));
    }

    // ── 구글에서 돌아왔을 때 (GET /auth/google/callback) ────
    public function callback(Request $request, GoogleOAuth $google, DeviceTracker $devices)
    {
        $purpose = $google->pullPurpose($request);
        $back    = $purpose === 'confirm' ? '/confirm-password' : '/login';

        try {
            $profile = $google->userFromCallback($request);
        } catch (Throwable $e) {
            return redirect($back)->with('error', $e->getMessage());
        }

        // ── 본인 확인이었다면 여기서 끝난다 ─────────────────
        //   ★ 돌아온 구글 계정이 '지금 로그인한 그 사람'인지 본다.
        //     이것을 빼면 아무 구글 계정이나 남의 설정을 여는 열쇠가 된다.
        if ($purpose === 'confirm') {
            if (! $request->user() || $request->user()->google_id !== $profile['id']) {
                return redirect('/confirm-password')->with('error', '로그인한 계정과 다른 구글 계정입니다.');
            }

            // 비밀번호를 맞힌 것과 같게 기록한다 — password.confirm 미들웨어가 이 값을 본다.
            $request->session()->put('auth.password_confirmed_at', time());

            return redirect()->intended('/settings');
        }

        // 구글이 '확인되지 않은 주소'라고 하면 받지 않는다.
        //   ★ 확인 안 된 주소를 믿으면, 남의 주소를 등록해 둔 구글 계정으로
        //     그 주소의 주인 행세를 할 수 있다.
        if (! $profile['verified'] || ! $profile['email']) {
            return redirect('/login')->with('error', '구글 계정의 이메일이 확인되지 않았습니다.');
        }

        // ★ 처음 오는 사람이면 여기서 계정을 만들지 않는다.
        //   구글에서 받은 동의는 '구글이 우리에게 정보를 넘겨도 되는가'에 대한 것이고,
        //   우리가 그 정보를 무슨 목적으로 쓰는가는 우리가 따로 받아야 하는 동의다.
        //   아이디로 가입하는 사람에게는 체크를 받으면서 구글로 오는 사람은 그냥 만들면,
        //   같은 서비스가 사람에 따라 다른 기준을 쓰게 된다.
        $user = User::withTrashed()->where('google_id', $profile['id'])->first();

        if (! $user) {
            // 받아 온 정보를 잠깐 세션에 두고 동의 화면으로 보낸다.
            //   ★ 주소에 실어 보내지 않는다 — 주소는 사용자가 고칠 수 있다.
            $request->session()->put(self::PENDING_KEY, $profile);

            return redirect()->route('google.complete');
        }

        // ★ 탈퇴 대기 중이면 바로 들여보내지 않는다. 복구할지 지울지 본인이 고른다.
        //   구글 버튼을 무심코 눌러 계정이 말없이 살아나는 일을 막는다.
        if ($user->trashed() && ! $user->anonymized_at) {
            $request->session()->put(AccountController::PENDING_KEY, $user->id);

            return redirect('/account/restore');
        }

        // ★ remember: true 를 쓰면 안 된다 — 이 프로젝트는 users 에 remember_token 칸이 없다.
        //   자동 로그인을 의도적으로 빼면서 칸까지 지웠기 때문이다(User 모델 맨 아래 참고).
        //   아이디·비밀번호 로그인도 Auth::attempt() 만 쓰고 자동 로그인은 쓰지 않는다.
        Auth::login($user);

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

    // ── 가입 마무리 화면 (GET /auth/google/complete) ────────
    //   ★ 구글을 다녀왔지만 아직 계정은 없는 상태다. 여기서 동의를 받는다.
    public function complete(Request $request)
    {
        $profile = $request->session()->get(self::PENDING_KEY);

        if (! $profile) {
            return redirect('/login')->with('error', '가입 절차가 만료되었습니다. 다시 시도해 주세요.');
        }

        return view('auth.google-complete', [
            'profile'  => $profile,
            'username' => $this->makeUsername($profile['email']),
            'nickname' => $this->makeNickname($profile),
        ]);
    }

    // ── 동의하고 가입 (POST /auth/google/complete) ──────────
    public function store(Request $request, Consent $consent, DeviceTracker $devices)
    {
        $profile = $request->session()->get(self::PENDING_KEY);

        if (! $profile) {
            return redirect('/login')->with('error', '가입 절차가 만료되었습니다. 다시 시도해 주세요.');
        }

        // 동의하지 않으면 계정을 만들지 않는다.
        //   ★ 검사가 먼저다. 세션을 먼저 비우면, 체크를 깜빡했을 때 되돌아온 화면에서
        //     '절차가 만료되었습니다'가 떠서 구글 로그인부터 다시 해야 한다.
        //     실수 한 번에 처음부터 다시 시키는 것은 안내가 아니라 벌이다.
        $request->validate(
            ['agree' => ['accepted']],
            ['agree.accepted' => '이용약관과 개인정보처리방침에 동의해 주세요.']
        );

        // 여기까지 왔으면 만든다. 이제 비운다 — 뒤로가기로 두 번 가입되지 않게.
        $request->session()->forget(self::PENDING_KEY);

        $user = $this->create($profile);

        // ★ 아이디로 가입할 때와 같은 표에 같은 모양으로 남긴다. source 만 다르다.
        //   이것이 없으면 '구글로 가입한 사람에게는 동의를 받았나'에 답할 수 없다.
        $consent->recordTerms($request, $user->id, 'google');

        Auth::login($user);
        $request->session()->regenerate();
        app(DeviceKey::class)->openEnrollWindow($request);
        $devices->remember($request, $user->id);
        $devices->takeNewDevices($request, $user->id);   // 방금 만든 계정이라 알릴 '새 기기'가 없다

        return redirect()->intended('/')->with('status', $user->nickname . '님, 환영합니다!');
    }

    // ── 계정 만들기 ────────────────────────────────────────
    //   ★ 동의를 받은 뒤에만 불린다.
    private function create(array $profile): User
    {
        //
        //   ★ 비밀번호는 아무도 모르는 난수를 넣는다. 빈 값으로 두면 빈 비밀번호로
        //     로그인이 될 여지가 생긴다. 이 사람은 구글로만 들어온다.
        //
        //   ★ 이메일은 '자리가 비어 있을 때만' 채운다 —
        //     users.email 은 메일 알림 기능의 칸이고 unique 다. 로그인에 필요한 값이 아니다.
        //     그 주소를 이미 다른 계정이 쓰고 있다고 해서 로그인이 막혀서는 안 된다.
        //     비워 두면 이 사람은 그냥 '메일 주소를 아직 안 적은 회원'이 된다.
        //     나중에 설정에서 직접 정하면 되고, 그 흐름은 아이디로 가입한 사람과 똑같다.
        // withTrashed() — 탈퇴한 계정이 쥐고 있는 주소도 '쓰는 중'으로 본다(유예 기간이라 되돌아올 수 있다)
        $emailIsFree = ! User::withTrashed()->where('email', $profile['email'])->exists();

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
        for ($n = 2; User::withTrashed()->where('username', $name)->exists(); $n++) {
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
