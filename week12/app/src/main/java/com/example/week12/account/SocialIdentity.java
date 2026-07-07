package com.example.week12.account;

/// <summary>
/// 소셜 로그인으로 확인된 "신원" — provider가 토큰을 검증한 뒤 돌려주는 사용자 정보 묶음
///
/// ──── 왜 필요한가 ────
/// 카카오와 네이버는 사용자 정보를 담는 클래스(User / NidProfileDetail)가 서로 다르다.
/// 그 서로 다른 것을 우리 앱이 쓰기 쉬운 하나의 공통 모양으로 옮겨 담은 것이 이 클래스.
/// → AuthRepository는 카카오/네이버를 구분하지 않고 이 SocialIdentity만 보고 세션을 만든다.
///
/// Unity 비유: 서로 다른 서버 응답을 우리 게임이 쓰는 하나의 UserData 구조체로 정규화하는 것.
/// </summary>
public class SocialIdentity {

    /// <summary>
    /// provider가 발급한 사용자 고유 id (카카오: 숫자 id를 문자열로, 네이버: 문자열 id)
    /// 이 값 앞에 provider 접두사(kakao_/naver_)를 붙여 우리 계정 id를 만든다
    /// </summary>
    private final String id;

    /// <summary>
    /// 표시용 별명 (동의 안 했거나 없으면 provider가 기본값을 채워 넘김)
    /// </summary>
    private final String nickname;

    /// <summary>
    /// 프로필 사진 주소(https). 없으면 빈 문자열 → 색깔 원 아바타 유지
    /// </summary>
    private final String imageUrl;

    /// <summary>
    /// 신원 생성 (세 값 모두 provider가 검증 후 채워서 넘김)
    /// </summary>
    public SocialIdentity(String id, String nickname, String imageUrl) {
        this.id = id;
        this.nickname = nickname;
        this.imageUrl = imageUrl;
    }

    /// <summary>
    /// provider 사용자 고유 id 반환
    /// </summary>
    public String getId() {
        return id;
    }

    /// <summary>
    /// 별명 반환
    /// </summary>
    public String getNickname() {
        return nickname;
    }

    /// <summary>
    /// 프로필 사진 주소 반환 (없으면 빈 문자열)
    /// </summary>
    public String getImageUrl() {
        return imageUrl;
    }
}
