package com.example.week10.account;

import androidx.appcompat.app.AppCompatDelegate;

/// <summary>
/// 화면 테마 종류 (계정별 설정)
///
/// ──── 무엇인가 ────
/// 앱을 밝게 볼지(라이트), 어둡게 볼지(다크), 아니면 폰 설정을 따라갈지(시스템)를 나타낸다.
/// 각 종류는 안드로이드가 쓰는 "야간 모드 값"(nightMode)을 하나씩 들고 있어,
/// 화면에 적용할 때 AppCompatDelegate.setDefaultNightMode(nightMode)로 넘긴다.
///
/// ──── 왜 enum에 값을 직접 담았나 ────
/// 종류가 3개, 가진 값도 2개(표시 이름 + 야간 모드)뿐이라 별도 Spec/Factory 없이
/// enum 안에 데이터를 바로 담는 게 간단하고 읽기 좋다.
///
/// Unity 비유: Quality 설정을 Low/Medium/High enum으로 두고
/// 각 항목이 실제 적용할 설정값을 들고 있는 것과 같음.
/// </summary>
public enum ThemeMode {

    /// <summary>폰의 시스템 설정을 따라감 (기본값)</summary>
    SYSTEM("시스템 설정", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),

    /// <summary>항상 밝게</summary>
    LIGHT("라이트", AppCompatDelegate.MODE_NIGHT_NO),

    /// <summary>항상 어둡게</summary>
    DARK("다크", AppCompatDelegate.MODE_NIGHT_YES);

    /// <summary>
    /// 화면(다이얼로그)에 보여줄 이름
    /// 종류마다 다른 값이라 생성자에서 받고, 바뀌지 않으므로 final
    /// </summary>
    private final String displayName;

    /// <summary>
    /// AppCompat에 넘길 야간 모드 값 (MODE_NIGHT_NO / YES / FOLLOW_SYSTEM)
    /// </summary>
    private final int nightMode;

    /// <summary>
    /// 테마 종류 생성
    /// </summary>
    /// <param name="displayName">화면에 표시할 이름</param>
    /// <param name="nightMode">AppCompatDelegate에 넘길 야간 모드 값</param>
    ThemeMode(String displayName, int nightMode) {
        this.displayName = displayName;
        this.nightMode = nightMode;
    }

    /// <summary>
    /// 화면에 표시할 이름 반환
    /// </summary>
    public String getDisplayName() {
        return displayName;
    }

    /// <summary>
    /// AppCompatDelegate.setDefaultNightMode()에 넘길 값 반환
    /// </summary>
    public int getNightMode() {
        return nightMode;
    }
}
