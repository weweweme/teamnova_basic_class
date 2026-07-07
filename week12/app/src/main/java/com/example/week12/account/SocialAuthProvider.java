package com.example.week12.account;

/// <summary>
/// 소셜 로그인 provider(카카오/네이버/…)가 지켜야 할 공통 규격(인터페이스)
///
/// ──── 왜 이렇게 나눴나 ────
/// provider마다 SDK 호출 방법이 다르다(카카오 me() vs 네이버 callProfileApi() 등).
/// 그 "다른 부분"을 각 provider 클래스 안에 가두고, AuthRepository는 이 공통 규격만 보고
/// 카카오/네이버를 구분하지 않은 채 로그인·로그아웃·연동해제를 처리한다.
/// → 새 provider(구글 등)를 추가할 때 이 규격을 구현한 클래스 하나만 만들면 되고,
///   AuthRepository의 로그아웃/삭제 코드는 손대지 않아도 된다 (개방-폐쇄 원칙).
///
/// Unity 비유: IWeapon 인터페이스를 두고 Sword/Bow가 각자 Attack()을 구현하면,
/// 플레이어는 IWeapon만 알고 무기 종류를 구분하지 않고 attack()을 부르는 것과 같다.
/// </summary>
public interface SocialAuthProvider {

    /// <summary>
    /// 이 provider가 만드는 계정 id의 접두사 (예: "kakao_", "naver_")
    /// provider 사용자 고유 id 앞에 붙여 우리 계정 id를 만든다
    /// </summary>
    String accountPrefix();

    /// <summary>
    /// 주어진 계정 id가 이 provider의 소관인지 (접두사로 판별)
    /// 로그아웃/연동해제 때 "누가 처리할 provider인지" 고르는 데 쓴다
    /// </summary>
    boolean owns(String accountId);

    /// <summary>
    /// 지금 로그인된 토큰으로 SDK에 신원을 물어본다 (검증) → 결과를 콜백으로 전달
    /// (성공하면 확인된 SocialIdentity, 실패하면 사유)
    /// </summary>
    void verify(SocialAuthCallback callback);

    /// <summary>
    /// 로그아웃 — 이 provider의 SDK 토큰을 지운다 (연동·동의는 남김)
    /// </summary>
    void clearSession();

    /// <summary>
    /// 연동 해제 — 앱↔provider 연결·동의까지 완전히 끊는다 (다음 로그인 때 동의부터 다시)
    /// </summary>
    void unlink();
}
