package com.example.week12.account;

import android.app.Activity;
import android.util.Log;

import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.util.NidOAuthCallback;
import com.navercorp.nid.profile.domain.vo.NidProfile;
import com.navercorp.nid.profile.domain.vo.NidProfileDetail;
import com.navercorp.nid.profile.util.NidProfileCallback;

/// <summary>
/// 네이버 로그인 provider — 네이버 SDK로 신원 검증 / 로그아웃 / 연동 해제를 담당
///
/// 네이버 SDK를 아는 코드는 (로그인 창을 띄우는 LoginActivity를 빼면) 전부 이 클래스에 모여 있다.
/// AuthRepository는 이 클래스를 SocialAuthProvider 규격으로만 바라본다 (네이버인 줄 모른다).
/// </summary>
public class NaverAuthProvider implements SocialAuthProvider {

    /// <summary>
    /// 네이버 계정을 우리 계정 id로 만들 때 붙이는 접두사 (예: naver_abcd1234)
    /// </summary>
    public static final String ACCOUNT_PREFIX = "naver_";

    /// <summary>
    /// 이 provider가 만드는 계정 id 접두사 반환
    /// </summary>
    @Override
    public String accountPrefix() {
        return ACCOUNT_PREFIX;
    }

    /// <summary>
    /// 주어진 계정 id가 네이버 계정인지 (접두사로 판별)
    /// </summary>
    @Override
    public boolean owns(String accountId) {
        return accountId != null && accountId.startsWith(ACCOUNT_PREFIX);
    }

    /// <summary>
    /// 로그인 — 네이버 로그인 창을 띄우고, 성공하면 신원을 확인해 콜백으로 전달한다.
    /// </summary>
    @Override
    public void login(Activity activity, SocialAuthCallback callback) {
        Log.d("AuthApi", "  ↳ 네이버 SDK 로그인 창 호출");
        NaverIdLoginSDK.INSTANCE.authenticate(activity, new NidOAuthCallback() {
            @Override
            public void onSuccess() {
                // 로그인 성공(토큰이 SDK에 저장됨) → 그 토큰으로 신원 확인
                fetchIdentity(callback);
            }

            @Override
            public void onFailure(String httpStatus, String message) {
                // 로그인 실패/취소
                callback.onFailed("네이버 로그인 실패");
            }
        });
    }

    /// <summary>
    /// 저장된 토큰으로 네이버에 프로필을 물어본다 → 확인된 신원을 콜백으로 전달
    /// (토큰이 없거나 유효하지 않으면 onFailure가 와서 실패로 처리된다)
    /// </summary>
    private void fetchIdentity(SocialAuthCallback callback) {
        Log.d("AuthApi", "  ↳ 네이버 프로필 API 조회 (OAuth 2.0)");
        new NidOAuthLogin().callProfileApi(new NidProfileCallback<NidProfile>() {
            @Override
            public void onSuccess(NidProfile result) {
                NidProfileDetail detail = result.getProfile();
                // 네이버 유저 고유 id가 있어야 계정 id를 만들 수 있음
                boolean failed = detail == null || detail.getId() == null || detail.getId().isEmpty();
                if (failed) {
                    callback.onFailed("네이버 인증 실패");
                    return;
                }
                String nickname = extractNickname(detail);
                String imageUrl = detail.getProfileImage() != null ? detail.getProfileImage() : "";
                callback.onVerified(new SocialIdentity(detail.getId(), nickname, imageUrl));
            }

            @Override
            public void onFailure(String httpStatus, String message) {
                callback.onFailed("네이버 인증 실패");
            }
        });
    }

    /// <summary>
    /// 로그아웃 — 네이버 토큰 제거 (연동·동의는 남김). 결과와 무관하게 진행하므로 콜백은 비워둔다.
    /// (네이버 logout은 콜백을 받으므로 빈 콜백을 넘긴다)
    /// </summary>
    @Override
    public void clearSession() {
        NaverIdLoginSDK.INSTANCE.logout(new NidOAuthCallback() {
            @Override
            public void onSuccess() {
                // 토큰 제거 성공 — 따로 할 일 없음
            }

            @Override
            public void onFailure(String httpStatus, String message) {
                // 실패해도 상위 로그아웃은 계속 진행
            }
        });
    }

    /// <summary>
    /// 연동 해제 — 서버에서 토큰 삭제(앱↔네이버 연동 끊기). 다음 로그인 때 동의부터 다시 뜬다.
    /// </summary>
    @Override
    public void unlink() {
        new NidOAuthLogin().callDeleteTokenApi(new NidOAuthCallback() {
            @Override
            public void onSuccess() {
                // 연동 해제 성공 — 따로 할 일 없음
            }

            @Override
            public void onFailure(String httpStatus, String message) {
                // 실패해도 상위 계정 삭제는 계속 진행
            }
        });
    }

    /// <summary>
    /// 네이버 프로필에서 별명을 안전하게 꺼낸다 (동의 안 했거나 없으면 기본값)
    /// </summary>
    private String extractNickname(NidProfileDetail detail) {
        String fallback = "네이버사용자";
        String nickname = detail.getNickname();
        boolean hasNickname = nickname != null && !nickname.isEmpty();
        return hasNickname ? nickname : fallback;
    }
}
