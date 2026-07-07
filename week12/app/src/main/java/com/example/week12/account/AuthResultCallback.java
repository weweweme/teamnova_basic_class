package com.example.week12.account;

/// <summary>
/// 로그인(인증) 결과를 돌려받는 통로 — "서버 역할"인 AuthRepository가 결과를 알려줄 때 쓴다
///
/// 화면(LoginActivity)은 이 콜백으로 결과만 받아 화면 전환을 하고,
/// 검증·세션 발급 같은 "서버 몫"은 AuthRepository 안에서 처리된다 (역할 분리).
/// </summary>
public interface AuthResultCallback {

    /// <summary>
    /// 로그인 성공 — 닉네임과 "새로 만든 계정인지" 여부를 전달
    /// </summary>
    void onSuccess(String nickname, boolean isNewAccount);

    /// <summary>
    /// 로그인 실패 — 사람이 읽을 실패 사유
    /// </summary>
    void onError(String message);
}
