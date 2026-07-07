package com.example.week12.account;

import android.content.Context;

import com.example.week12.data.TestAccountSeeder;
import com.example.week12.model.Account;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.util.NidOAuthCallback;
import com.navercorp.nid.profile.domain.vo.NidProfile;
import com.navercorp.nid.profile.domain.vo.NidProfileDetail;
import com.navercorp.nid.profile.util.NidProfileCallback;

import kotlin.Unit;

/// <summary>
/// "서버 역할"을 흉내 낸 인증 계층 — 로그인 토큰을 검증하고, 통과하면 우리 앱 세션을 발급한다.
///
/// ──── 왜 이렇게 나눴나 (구조 학습) ────
/// 원래 LoginActivity(화면)가 "카카오 로그인 → 토큰 → 프로필 조회 → 계정 연결 → 세션"을 전부 했다.
/// 그런데 이 중 "토큰 검증 + 세션 발급"은 원래 **서버가 할 일**이다.
/// 그래서 그 부분을 이 클래스로 떼어냈다. → "진짜 서버가 생기면 이 클래스가 곧 서버가 되는" 경계선.
///
/// ⚠️ 보안 주의: 지금은 이 계층이 클라이언트와 "같은 기기"에 있어 진짜 방어가 되진 않는다.
///    (공격자가 앱도, 이 계층도 다 조작 가능). 진짜 보안은 서버가 "다른 신뢰된 하드웨어"에 있어야 성립.
///    여기서는 "서버라면 이런 모양이다"라는 구조만 보여준다.
/// </summary>
public class AuthRepository {

    /// <summary>
    /// 카카오 계정을 우리 계정 id로 만들 때 붙이는 접두사 (예: kakao_1234567890)
    /// "이 계정이 카카오 계정인지" 판단할 때도 이 접두사를 쓴다 (로그아웃 등)
    /// </summary>
    public static final String KAKAO_ACCOUNT_PREFIX = "kakao_";

    /// <summary>
    /// 네이버 계정을 우리 계정 id로 만들 때 붙이는 접두사 (예: naver_abcd1234)
    /// "이 계정이 네이버 계정인지" 판단할 때도 이 접두사를 쓴다 (로그아웃/자동로그인 등)
    /// </summary>
    public static final String NAVER_ACCOUNT_PREFIX = "naver_";

    /// <summary>
    /// 계정 파일(user_<id>) 접근용 앱 컨텍스트
    /// </summary>
    private final Context appContext;

    /// <summary>
    /// 계정 등록/세션(현재 계정) 관리를 맡는 전역 계정 관리자
    /// </summary>
    private final AccountManager accountManager;

    /// <summary>
    /// 인증 계층 생성
    /// </summary>
    public AuthRepository(Context context, AccountManager accountManager) {
        this.appContext = context.getApplicationContext();
        this.accountManager = accountManager;
    }

    /// <summary>
    /// 카카오 토큰으로 로그인 처리 — 검증 → 세션 발급 (결과는 콜백, 메인 스레드)
    ///
    /// ──── "진짜 서버라면" 이 지점 ────
    /// 클라이언트(화면)가 로그인 증표인 token을 이 계층("서버")에 건넨 셈이다.
    /// 진짜 서버라면 이 token을 카카오에 보내 "정말 유효한 토큰인지" 검증한다.
    /// 지금은 SDK me()가 토큰으로 카카오에 유저를 물어보는 것이 그 검증에 해당한다
    ///   → 카카오가 확인해준 유저만 돌아오므로, 클라이언트가 신원을 지어낼 수 없다.
    /// </summary>
    public void loginWithKakao(OAuthToken token, boolean keepLogin, AuthResultCallback callback) {
        // 클라이언트가 건넨 로그인 증표(토큰)가 없으면 진행 불가
        if (token == null) {
            callback.onError("로그인 정보가 없어요");
            return;
        }

        // ① 검증: 토큰으로 카카오에 유저를 물어본다 (카카오가 확인해준 신원만 돌아옴)
        UserApiClient.getInstance().me((user, error) -> {
            boolean failed = error != null || user == null || user.getId() == null;
            if (failed) {
                callback.onError("카카오 인증 실패");
                return Unit.INSTANCE;
            }

            // ② 검증 통과 → 우리 앱 세션 발급 (계정 연결 + 현재 계정 지정 + 프로필 저장)
            long kakaoId = user.getId();
            String nickname = extractNickname(user);
            String imageUrl = extractImageUrl(user);
            boolean isNew = issueSession(KAKAO_ACCOUNT_PREFIX + kakaoId, nickname, imageUrl, keepLogin);

            callback.onSuccess(nickname, isNew);
            return Unit.INSTANCE;
        });
    }

    /// <summary>
    /// 네이버 토큰으로 로그인 처리 — 검증 → 세션 발급 (카카오와 완전히 같은 "서버 역할" 경계)
    ///
    /// 카카오의 me()가 토큰으로 유저를 물어보듯, 여기서는 네이버 프로필 API(callProfileApi)가
    /// 토큰으로 네이버에 유저를 물어보는 것이 검증에 해당한다 (네이버가 확인해준 신원만 돌아옴).
    /// 결과는 콜백으로 돌려주며, 네이버 SDK가 콜백을 메인 스레드에서 불러준다.
    /// </summary>
    public void loginWithNaver(String accessToken, boolean keepLogin, AuthResultCallback callback) {
        // 클라이언트가 건넨 로그인 증표(토큰)가 없으면 진행 불가
        boolean noToken = accessToken == null || accessToken.isEmpty();
        if (noToken) {
            callback.onError("로그인 정보가 없어요");
            return;
        }

        // ① 검증: 토큰으로 네이버에 프로필을 물어본다 (네이버가 확인해준 신원만 돌아옴)
        new NidOAuthLogin().callProfileApi(new NidProfileCallback<NidProfile>() {
            @Override
            public void onSuccess(NidProfile result) {
                NidProfileDetail detail = result.getProfile();
                // 네이버 유저 고유 id가 있어야 우리 계정 id를 만들 수 있음
                boolean failed = detail == null || detail.getId() == null || detail.getId().isEmpty();
                if (failed) {
                    callback.onError("네이버 인증 실패");
                    return;
                }

                // ② 검증 통과 → 우리 앱 세션 발급
                String accountId = NAVER_ACCOUNT_PREFIX + detail.getId();
                String nickname = extractNaverNickname(detail);
                String imageUrl = detail.getProfileImage() != null ? detail.getProfileImage() : "";
                boolean isNew = issueSession(accountId, nickname, imageUrl, keepLogin);

                callback.onSuccess(nickname, isNew);
            }

            @Override
            public void onFailure(String httpStatus, String message) {
                callback.onError("네이버 인증 실패");
            }
        });
    }

    /// <summary>
    /// PIN(가상 계정)으로 로그인 처리 — 검증 → 세션 발급 (카카오와 같은 "서버 역할" 경계)
    ///
    /// PIN 검증은 로컬(파일)이라 결과가 바로 나오므로, 카카오와 달리 콜백 없이 즉시 true/false를 돌려준다.
    /// 성공하면 현재 계정 지정 + 로그인 유지 반영까지 여기서 끝낸다.
    /// (진짜 서버라면 비밀번호 로그인도, 소셜 로그인도 모두 이 서버 계층을 통과한다)
    /// </summary>
    public boolean loginWithPin(String id, String pin, boolean keepLogin) {
        boolean ok = accountManager.login(id, pin);   // 검증(verifyPin) + 현재 계정 지정
        if (ok) {
            accountManager.setAutoLogin(keepLogin);
        }
        return ok;
    }

    /// <summary>
    /// 로그아웃 — 로그인의 짝. 현재 계정이 소셜(카카오/네이버)이면 그 SDK 세션(토큰)도 지운 뒤,
    /// 우리 세션(current_account)을 비운다.
    ///
    /// 소셜 토큰을 안 지우면, 로그아웃 후 다시 소셜 로그인할 때 로그인 창 없이 곧바로 같은 계정으로
    /// 들어가버린다. "어떤 SDK를 어떻게 정리하나"는 화면이 아니라 이 인증 계층이 알아야 할 일이다.
    /// (accountManager.logout()이 현재 계정을 비우기 전에 소셜 정리를 먼저 해야 provider 판별 가능)
    /// </summary>
    public void logout() {
        String currentId = accountManager.getCurrentAccountId();
        clearSocialSession(currentId);
        accountManager.logout();
    }

    /// <summary>
    /// 현재 계정 삭제 — 현재 계정이 소셜이면 연동까지 완전히 해제한 뒤, 로컬 계정을 통째로 지운다.
    ///
    /// 로그아웃은 토큰만 지우지만, 삭제는 앱↔소셜 "연동·동의"까지 끊는다 → 다음 소셜 로그인 때
    /// 처음처럼 동의 화면부터 뜨고 새 계정으로 다시 만들어진다.
    /// (deleteAccount 전에 소셜 연동 해제를 먼저 해야 provider 판별 가능)
    /// </summary>
    public void deleteCurrentAccount() {
        String currentId = accountManager.getCurrentAccountId();
        if (currentId == null) {
            return;
        }
        unlinkSocial(currentId);
        accountManager.deleteAccount(currentId);
    }

    /// <summary>
    /// 계정 provider를 보고 소셜 세션(토큰)을 지운다 (로그아웃용). 소셜이 아니면 아무 일 안 함.
    ///   - 카카오: UserApiClient.logout (토큰 만료)
    ///   - 네이버: NaverIdLoginSDK.logout (토큰 제거)
    /// 결과와 무관하게 우리 로그아웃은 진행하므로, 콜백은 비워둔다.
    /// </summary>
    private void clearSocialSession(String accountId) {
        if (accountId == null) {
            return;
        }
        if (accountId.startsWith(KAKAO_ACCOUNT_PREFIX)) {
            // 콜백이 코틀린 함수 타입(반환형 Unit)이라 자바에선 Unit.INSTANCE를 돌려줘야 함
            UserApiClient.getInstance().logout(error -> Unit.INSTANCE);
        } else if (accountId.startsWith(NAVER_ACCOUNT_PREFIX)) {
            NaverIdLoginSDK.INSTANCE.logout(new NidOAuthCallback() {
                @Override
                public void onSuccess() {
                    // 토큰 제거 성공 — 따로 할 일 없음
                }

                @Override
                public void onFailure(String httpStatus, String message) {
                    // 실패해도 우리 로그아웃은 계속 진행
                }
            });
        }
    }

    /// <summary>
    /// 계정 provider를 보고 소셜 "연동 해제"까지 한다 (계정 삭제용). 소셜이 아니면 아무 일 안 함.
    ///   - 카카오: UserApiClient.unlink (앱↔카카오 완전 해제)
    ///   - 네이버: NidOAuthLogin.callDeleteTokenApi (서버에서 토큰 삭제 = 연동 해제)
    /// 결과와 무관하게 로컬 계정 삭제는 진행하므로, 콜백은 비워둔다.
    /// </summary>
    private void unlinkSocial(String accountId) {
        if (accountId == null) {
            return;
        }
        if (accountId.startsWith(KAKAO_ACCOUNT_PREFIX)) {
            UserApiClient.getInstance().unlink(error -> Unit.INSTANCE);
        } else if (accountId.startsWith(NAVER_ACCOUNT_PREFIX)) {
            new NidOAuthLogin().callDeleteTokenApi(new NidOAuthCallback() {
                @Override
                public void onSuccess() {
                    // 연동 해제 성공 — 따로 할 일 없음
                }

                @Override
                public void onFailure(String httpStatus, String message) {
                    // 실패해도 로컬 계정 삭제는 계속 진행
                }
            });
        }
    }

    /// <summary>
    /// 검증을 통과한 카카오 신원으로 우리 앱 세션을 발급한다 (원래 서버가 하는 "세션 만들기")
    ///   - 계정이 없으면 만들고(PIN 없음), 있으면 닉네임만 갱신
    ///   - 현재 로그인 계정으로 지정 + 로그인 유지 반영 + 프로필 사진 저장
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

        UserPrefs userPrefs = new UserPrefs(appContext, accountId);

        // 프로필 사진은 "처음 가입할 때만" 카카오 사진으로 채운다
        // (재로그인 때도 덮으면, 사용자가 프로필 편집에서 직접 바꾼 사진이 매번 카카오 사진으로 되돌아감)
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

    /// <summary>
    /// 카카오 유저에서 닉네임을 안전하게 꺼낸다 (동의 안 했거나 없으면 기본값)
    /// </summary>
    private String extractNickname(User user) {
        String fallback = "카카오사용자";
        boolean hasProfile = user.getKakaoAccount() != null
                && user.getKakaoAccount().getProfile() != null;
        if (!hasProfile) {
            return fallback;
        }
        String nickname = user.getKakaoAccount().getProfile().getNickname();
        boolean hasNickname = nickname != null && !nickname.isEmpty();
        return hasNickname ? nickname : fallback;
    }

    /// <summary>
    /// 네이버 프로필에서 별명을 안전하게 꺼낸다 (동의 안 했거나 없으면 기본값)
    /// </summary>
    private String extractNaverNickname(NidProfileDetail detail) {
        String fallback = "네이버사용자";
        String nickname = detail.getNickname();
        boolean hasNickname = nickname != null && !nickname.isEmpty();
        return hasNickname ? nickname : fallback;
    }

    /// <summary>
    /// 카카오 유저에서 프로필 사진 주소를 안전하게 꺼낸다 (없으면 빈 문자열 → 색깔 원 아바타 유지)
    /// </summary>
    private String extractImageUrl(User user) {
        boolean hasProfile = user.getKakaoAccount() != null
                && user.getKakaoAccount().getProfile() != null;
        if (!hasProfile) {
            return "";
        }
        String url = user.getKakaoAccount().getProfile().getProfileImageUrl();
        return url != null ? url : "";
    }
}
