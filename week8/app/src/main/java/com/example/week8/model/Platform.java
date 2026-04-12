package com.example.week8.model;

/// <summary>
/// 게임 플랫폼 열거형
/// 메이저 플랫폼 5종 + 기타(ETC)
/// Unity로 비유하면 BuildTarget 열거형과 같은 개념
/// </summary>
public enum Platform {
    STEAM("Steam"),
    PLAYSTATION("PlayStation"),
    XBOX("Xbox"),
    NINTENDO_SWITCH("Nintendo Switch"),
    MOBILE("모바일"),
    ETC("기타");

    /// <summary>
    /// 화면에 표시할 플랫폼 이름
    /// </summary>
    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

    /// <summary>
    /// 화면 표시용 이름 반환
    /// </summary>
    public String getDisplayName() {
        return this.displayName;
    }
}
