package com.example.week11.model;

/// <summary>
/// 한 계정의 "공개 프로필" 한 장 (커뮤니티 화면 표시용 데이터 묶음)
///
/// ──── 무엇인가 ────
/// 이 기기에 있는 계정 한 명을, 남들에게 보여줄 수 있는 정보만 모아 담은 그릇.
/// 비밀번호(PIN) 같은 건 넣지 않는다 — 화면에 뿌릴 안전한 값만.
///
/// ──── 어디서 오나 ────
/// CommunityRepository가 각 계정 파일(user_<id>)을 읽어 이 객체 하나로 묶어 만든다.
///   - 별명 → AccountManager
///   - 아바타 색 / 소개 / 출석(연속·누적) → 그 계정의 UserPrefs
///
/// 서버 없이 "이 기기의 유저들"을 보여줄 수 있는 이유:
/// 한 앱이 이 기기에 저장된 모든 계정 파일을 읽을 수 있기 때문.
/// </summary>
public class AccountProfile {

    /// <summary>계정 아이디 (식별자)</summary>
    private final String id;

    /// <summary>화면에 보여줄 별명</summary>
    private final String nickname;

    /// <summary>아바타 원 색 (ARGB 정수)</summary>
    private final int avatarColor;

    /// <summary>한 줄 소개 (없으면 빈 문자열)</summary>
    private final String bio;

    /// <summary>연속 방문 일수</summary>
    private final int streak;

    /// <summary>누적 방문 횟수</summary>
    private final int visitCount;

    /// <summary>이 계정이 남긴 리뷰 개수 (랭킹용)</summary>
    private final int reviewCount;

    /// <summary>
    /// 공개 프로필 생성 (CommunityRepository가 계정 파일을 읽어 채워 만든다)
    /// </summary>
    public AccountProfile(String id,
                          String nickname,
                          int avatarColor,
                          String bio,
                          int streak,
                          int visitCount,
                          int reviewCount) {
        this.id = id;
        this.nickname = nickname;
        this.avatarColor = avatarColor;
        this.bio = bio;
        this.streak = streak;
        this.visitCount = visitCount;
        this.reviewCount = reviewCount;
    }

    /// <summary>계정 아이디 반환</summary>
    public String getId() {
        return id;
    }

    /// <summary>별명 반환</summary>
    public String getNickname() {
        return nickname;
    }

    /// <summary>아바타 색 반환</summary>
    public int getAvatarColor() {
        return avatarColor;
    }

    /// <summary>한 줄 소개 반환</summary>
    public String getBio() {
        return bio;
    }

    /// <summary>연속 방문 일수 반환</summary>
    public int getStreak() {
        return streak;
    }

    /// <summary>누적 방문 횟수 반환</summary>
    public int getVisitCount() {
        return visitCount;
    }

    /// <summary>리뷰 개수 반환</summary>
    public int getReviewCount() {
        return reviewCount;
    }
}
