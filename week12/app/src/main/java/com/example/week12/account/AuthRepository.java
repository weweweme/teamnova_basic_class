package com.example.week12.account;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.week12.data.TestAccountSeeder;
import com.example.week12.model.Account;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// "서버 역할"을 흉내 낸 인증 계층 — 로그인 신원을 검증하고, 통과하면 우리 앱 세션을 발급한다.
///
/// ──── 왜 이렇게 나눴나 (구조 학습) ────
/// 원래 LoginActivity(화면)가 "소셜 로그인 → 토큰 → 프로필 조회 → 계정 연결 → 세션"을 전부 했다.
/// 그중 "신원 검증 + 세션 발급"은 원래 **서버가 할 일**이라 이 클래스로 떼어냈다.
/// → "진짜 서버가 생기면 이 클래스가 곧 서버가 되는" 경계선.
///
/// ──── provider로 한 번 더 쪼갬 ────
/// 카카오/네이버는 SDK 호출법이 서로 다르다. 그 "다른 부분"은 각 SocialAuthProvider 구현체
/// (KakaoAuthProvider/NaverAuthProvider)에 가두고, 이 클래스는 provider를 규격(SocialAuthProvider)
/// 으로만 다룬다. → 새 provider(구글 등)는 구현체 하나 + 아래 목록에 한 줄 추가로 끝난다.
///
/// ⚠️ 보안 주의: 지금은 이 계층이 클라이언트와 "같은 기기"에 있어 진짜 방어가 되진 않는다.
///    (공격자가 앱도, 이 계층도 다 조작 가능). 진짜 보안은 서버가 "다른 신뢰된 하드웨어"에 있어야 성립.
///    여기서는 "서버라면 이런 모양이다"라는 구조만 보여준다.
/// </summary>
public class AuthRepository {

    /// <summary>
    /// Logcat 태그 — 발표 때 "AuthApi"로 필터하면 로그인 전 과정(시작→검증→세션)이 보인다
    /// </summary>
    private static final String TAG = "AuthApi";

    /// <summary>
    /// 카카오 계정 접두사 — 접두사의 소유자는 provider이고, 여기선 다른 화면(Login/Splash)이
    /// 참조하기 편하게 그대로 다시 노출한다 (값의 출처는 KakaoAuthProvider 하나뿐)
    /// </summary>
    public static final String KAKAO_ACCOUNT_PREFIX = KakaoAuthProvider.ACCOUNT_PREFIX;

    /// <summary>
    /// 네이버 계정 접두사 (출처는 NaverAuthProvider 하나뿐 — 위와 같은 이유로 재노출)
    /// </summary>
    public static final String NAVER_ACCOUNT_PREFIX = NaverAuthProvider.ACCOUNT_PREFIX;

    /// <summary>
    /// 계정 파일(user_<id>) 접근용 앱 컨텍스트
    /// </summary>
    private final Context appContext;

    /// <summary>
    /// 계정 등록/세션(현재 계정) 관리를 맡는 전역 계정 관리자
    /// </summary>
    private final AccountManager accountManager;

    /// <summary>
    /// 카카오 로그인 provider (로그인 진입점에서 직접 지목)
    /// </summary>
    private final SocialAuthProvider kakaoProvider = new KakaoAuthProvider();

    /// <summary>
    /// 네이버 로그인 provider
    /// </summary>
    private final SocialAuthProvider naverProvider = new NaverAuthProvider();

    /// <summary>
    /// 구글 로그인 provider — 컨텍스트가 필요해 생성자에서 만든다 (카카오/네이버는 무인자)
    /// </summary>
    private final SocialAuthProvider googleProvider;

    /// <summary>
    /// 등록된 소셜 provider 목록 — 로그아웃/삭제 때 "이 계정이 누구 소관인지" 찾는 데 쓴다
    /// 새 provider는 생성자에서 이 목록에 add 한 줄만 추가하면 된다
    /// </summary>
    private final List<SocialAuthProvider> socialProviders;

    /// <summary>
    /// 인증 계층 생성 (provider들을 만들어 목록에 담아둔다)
    /// </summary>
    public AuthRepository(Context context, AccountManager accountManager) {
        this.appContext = context.getApplicationContext();
        this.accountManager = accountManager;
        this.googleProvider = new GoogleAuthProvider(appContext);
        this.socialProviders = new ArrayList<>();
        socialProviders.add(kakaoProvider);
        socialProviders.add(naverProvider);
        socialProviders.add(googleProvider);
    }

    // ========== 로그인 ==========

    /// <summary>
    /// 카카오로 로그인 — 카카오 provider에게 로그인+신원확인을 맡기고, 통과하면 세션 발급
    /// (로그인 창을 띄우려면 activity가 필요하므로 화면에서 받아 넘긴다)
    /// </summary>
    public void loginWithKakao(Activity activity, boolean keepLogin, AuthResultCallback callback) {
        loginWithProvider(kakaoProvider, activity, keepLogin, callback);
    }

    /// <summary>
    /// 네이버로 로그인 — 네이버 provider에게 로그인+신원확인을 맡기고, 통과하면 세션 발급
    /// </summary>
    public void loginWithNaver(Activity activity, boolean keepLogin, AuthResultCallback callback) {
        loginWithProvider(naverProvider, activity, keepLogin, callback);
    }

    /// <summary>
    /// 구글로 로그인 — 구글 provider에게 로그인+신원확인을 맡기고, 통과하면 세션 발급
    /// </summary>
    public void loginWithGoogle(Activity activity, boolean keepLogin, AuthResultCallback callback) {
        loginWithProvider(googleProvider, activity, keepLogin, callback);
    }

    /// <summary>
    /// 소셜 로그인 공통 처리 — provider가 어느 소셜이든 흐름은 똑같다
    ///   provider.login(로그인+신원확인) → 통과하면 issueSession(세션 발급) → 화면에 결과 콜백
    /// "카카오냐 네이버냐 구글이냐"는 provider 안에 숨겨져 있어, 여기선 구분하지 않는다.
    /// </summary>
    private void loginWithProvider(SocialAuthProvider provider, Activity activity, boolean keepLogin,
                                   AuthResultCallback callback) {
        Log.d(TAG, "🔐 소셜 로그인 시작 (" + provider.accountPrefix() + ")");
        provider.login(activity, new SocialAuthCallback() {
            @Override
            public void onVerified(SocialIdentity identity) {
                Log.d(TAG, "✅ 신원 확인 ← 닉네임=" + identity.getNickname() + ", id=" + identity.getId());
                // provider 접두사 + provider가 준 고유 id = 우리 계정 id
                String accountId = provider.accountPrefix() + identity.getId();
                boolean isNew = issueSession(accountId, identity.getNickname(),
                        identity.getImageUrl(), keepLogin);
                callback.onSuccess(identity.getNickname(), isNew);
            }

            @Override
            public void onFailed(String message) {
                Log.w(TAG, "❌ 인증 실패 ← " + message);
                callback.onError(message);
            }
        });
    }

    /// <summary>
    /// PIN(가상 계정)으로 로그인 처리 — 검증 → 세션 발급 (소셜과 같은 "서버 역할" 경계)
    ///
    /// PIN 검증은 로컬(파일)이라 결과가 바로 나오므로, 소셜과 달리 콜백 없이 즉시 true/false를 돌려준다.
    /// SDK도 토큰도 없어 성격이 완전히 달라, 소셜 provider 규격에 넣지 않고 별도 메서드로 둔다.
    /// (진짜 서버라면 비밀번호 로그인도, 소셜 로그인도 모두 이 서버 계층을 통과한다)
    /// </summary>
    public boolean loginWithPin(String id, String pin, boolean keepLogin) {
        boolean ok = accountManager.login(id, pin);   // 검증(verifyPin) + 현재 계정 지정
        if (ok) {
            accountManager.setAutoLogin(keepLogin);
        }
        Log.d(TAG, "🔐 PIN 로그인: " + id + " → " + (ok ? "성공" : "실패"));
        return ok;
    }

    // ========== 로그아웃 / 계정 삭제 ==========

    /// <summary>
    /// 로그아웃 — 로그인의 짝. 현재 계정이 소셜이면 그 provider가 SDK 세션(토큰)을 지우고,
    /// 그다음 우리 세션(current_account)을 비운다.
    ///
    /// 소셜 토큰을 안 지우면, 로그아웃 후 다시 소셜 로그인할 때 로그인 창 없이 곧바로 같은 계정으로
    /// 들어가버린다. "어떤 SDK를 어떻게 정리하나"는 provider가 알고, 여기선 그저 위임한다.
    /// (accountManager.logout()이 현재 계정을 비우기 전에 provider를 먼저 찾아야 판별 가능)
    /// </summary>
    public void logout() {
        String currentId = accountManager.getCurrentAccountId();
        Log.d(TAG, "🚪 로그아웃 (계정=" + currentId + ")");
        SocialAuthProvider provider = findProvider(currentId);
        if (provider != null) {
            provider.clearSession();
        }
        accountManager.logout();
    }

    /// <summary>
    /// 현재 계정 삭제 — 현재 계정이 소셜이면 그 provider가 연동까지 해제하고, 로컬 계정을 통째로 지운다.
    ///
    /// 로그아웃은 토큰만 지우지만, 삭제는 앱↔소셜 "연동·동의"까지 끊는다 → 다음 소셜 로그인 때
    /// 처음처럼 동의 화면부터 뜨고 새 계정으로 다시 만들어진다.
    /// (deleteAccount 전에 provider를 먼저 찾아야 판별 가능)
    /// </summary>
    public void deleteCurrentAccount() {
        String currentId = accountManager.getCurrentAccountId();
        if (currentId == null) {
            return;
        }
        Log.d(TAG, "🗑 계정 삭제 (계정=" + currentId + ")");
        SocialAuthProvider provider = findProvider(currentId);
        if (provider != null) {
            provider.unlink();
        }
        accountManager.deleteAccount(currentId);
    }

    /// <summary>
    /// 이 계정이 소셜 로그인 계정(카카오/네이버/구글…)인지 여부.
    /// PIN 로그인 목록에서 소셜 계정을 걸러낼 때 쓴다 (provider가 늘어도 이 메서드는 그대로).
    /// </summary>
    public boolean isSocialAccount(String accountId) {
        return findProvider(accountId) != null;
    }

    /// <summary>
    /// 계정 id의 주인 provider를 찾는다 (접두사로 판별). 소셜이 아니면(PIN 계정) null.
    /// </summary>
    private SocialAuthProvider findProvider(String accountId) {
        if (accountId == null) {
            return null;
        }
        for (SocialAuthProvider provider : socialProviders) {
            if (provider.owns(accountId)) {
                return provider;
            }
        }
        return null;
    }

    // ========== 세션 발급 ==========

    /// <summary>
    /// 검증을 통과한 신원으로 우리 앱 세션을 발급한다 (원래 서버가 하는 "세션 만들기")
    ///   - 계정이 없으면 만들고(PIN 없음), 있으면 닉네임만 갱신
    ///   - 현재 로그인 계정으로 지정 + 로그인 유지 반영 + (새 계정이면) 프로필 사진 저장
    ///   - 새 계정이면 테스트 계정 자동 팔로우 (팔로잉 피드 시드)
    /// 반환값: 이번에 새로 만든 계정이면 true
    ///
    /// accountId: 이미 접두사가 붙은 우리 계정 id (카카오면 kakao_..., 네이버면 naver_...)
    ///   → 어느 소셜이든 이 메서드 하나로 세션을 발급한다 (provider별로 중복 코드 없음)
    /// </summary>
    private boolean issueSession(String accountId, String nickname, String imageUrl, boolean keepLogin) {
        boolean isNew = !accountManager.isRegistered(accountId);
        if (isNew) {
            accountManager.register(accountId, nickname, "");
        } else {
            accountManager.updateNickname(accountId, nickname);
        }

        accountManager.setCurrentAccount(accountId);
        accountManager.setAutoLogin(keepLogin);

        Log.d(TAG, "🎫 세션 발급 ← 계정=" + accountId + " (신규=" + isNew + ")");

        UserPrefs userPrefs = new UserPrefs(appContext, accountId);

        // 프로필 사진은 "처음 가입할 때만" 소셜 사진으로 채운다
        // (재로그인 때도 덮으면, 사용자가 프로필 편집에서 직접 바꾼 사진이 매번 소셜 사진으로 되돌아감)
        if (isNew) {
            // 현재 계정이 정해진 뒤라야 올바른 계정 파일에 씀
            userPrefs.setAvatarImageUrl(imageUrl);
            // 새 계정이면 커뮤니티가 비어 보이지 않게 테스트 계정 자동 팔로우
            seedNewAccountFollows(userPrefs);
        }

        return isNew;
    }

    /// <summary>
    /// 새 계정이 곧바로 커뮤니티를 즐길 수 있게, 이 기기의 테스트 계정들을 자동 팔로우한다
    /// (팔로잉 피드 빈 화면 방지 — 테스트 계정들은 이미 리뷰를 갖고 있어 피드가 바로 채워진다)
    /// </summary>
    private void seedNewAccountFollows(UserPrefs userPrefs) {
        for (Account account : accountManager.getAccounts()) {
            if (TestAccountSeeder.isTestAccount(account.getId())) {
                userPrefs.setFollowing(account.getId(), true);
            }
        }
    }
}
