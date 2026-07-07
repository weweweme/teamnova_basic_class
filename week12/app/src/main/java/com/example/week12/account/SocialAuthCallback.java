package com.example.week12.account;

/// <summary>
/// 소셜 provider가 토큰 검증(신원 확인)을 마친 뒤 결과를 돌려주는 통로(콜백)
///
/// 검증은 SDK가 서버에 다녀오는 일이라 오래 걸린다 → 곧바로 return하지 못하고
/// "다 되면 알려줄게" 방식으로 이 통로로 결과를 넘긴다.
/// (provider가 메인 스레드에서 불러주므로, 이어지는 세션 발급/화면 처리도 안전하다)
/// </summary>
public interface SocialAuthCallback {

    /// <summary>
    /// 검증 성공 — 확인된 신원(id/별명/사진)을 전달
    /// </summary>
    void onVerified(SocialIdentity identity);

    /// <summary>
    /// 검증 실패 — 사람이 읽을 실패 사유 전달
    /// </summary>
    void onFailed(String message);
}
