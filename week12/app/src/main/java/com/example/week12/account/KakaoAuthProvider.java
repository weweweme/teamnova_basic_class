package com.example.week12.account;

import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;

/// <summary>
/// 카카오 로그인 provider — 카카오 SDK로 신원 검증 / 로그아웃 / 연동 해제를 담당
///
/// 카카오 SDK를 아는 코드는 (로그인 창을 띄우는 LoginActivity를 빼면) 전부 이 클래스에 모여 있다.
/// AuthRepository는 이 클래스를 SocialAuthProvider 규격으로만 바라본다 (카카오인 줄 모른다).
/// </summary>
public class KakaoAuthProvider implements SocialAuthProvider {

    /// <summary>
    /// 카카오 계정을 우리 계정 id로 만들 때 붙이는 접두사 (예: kakao_1234567890)
    /// </summary>
    public static final String ACCOUNT_PREFIX = "kakao_";

    /// <summary>
    /// 이 provider가 만드는 계정 id 접두사 반환
    /// </summary>
    @Override
    public String accountPrefix() {
        return ACCOUNT_PREFIX;
    }

    /// <summary>
    /// 주어진 계정 id가 카카오 계정인지 (접두사로 판별)
    /// </summary>
    @Override
    public boolean owns(String accountId) {
        return accountId != null && accountId.startsWith(ACCOUNT_PREFIX);
    }

    /// <summary>
    /// 지금 로그인된 토큰으로 카카오에 사용자를 물어본다(me) → 확인된 신원을 콜백으로 전달
    /// (토큰이 없거나 유효하지 않으면 error가 와서 실패로 처리된다)
    /// </summary>
    @Override
    public void verify(SocialAuthCallback callback) {
        UserApiClient.getInstance().me((user, error) -> {
            boolean failed = error != null || user == null || user.getId() == null;
            if (failed) {
                callback.onFailed("카카오 인증 실패");
            } else {
                // 카카오 사용자 id는 숫자(Long) → 계정 id에 쓰려고 문자열로 바꾼다
                String id = String.valueOf(user.getId());
                String nickname = extractNickname(user);
                String imageUrl = extractImageUrl(user);
                callback.onVerified(new SocialIdentity(id, nickname, imageUrl));
            }
            // 콜백이 코틀린 함수 타입(반환형 Unit)이라 자바에선 Unit.INSTANCE를 돌려줘야 함
            return Unit.INSTANCE;
        });
    }

    /// <summary>
    /// 로그아웃 — 카카오 토큰 만료 (연동·동의는 남김). 결과와 무관하게 진행하므로 콜백은 비워둔다.
    /// </summary>
    @Override
    public void clearSession() {
        UserApiClient.getInstance().logout(error -> Unit.INSTANCE);
    }

    /// <summary>
    /// 연동 해제 — 앱↔카카오 연결·동의까지 완전히 끊는다 (다음 로그인 때 동의부터 다시)
    /// </summary>
    @Override
    public void unlink() {
        UserApiClient.getInstance().unlink(error -> Unit.INSTANCE);
    }

    /// <summary>
    /// 카카오 유저에서 별명을 안전하게 꺼낸다 (동의 안 했거나 없으면 기본값)
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
