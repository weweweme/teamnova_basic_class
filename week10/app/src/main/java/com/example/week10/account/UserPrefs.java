package com.example.week10.account;

import android.content.Context;
import android.content.SharedPreferences;

/// <summary>
/// 계정별 개인 설정 저장소 — "user_<id>" 파일 담당
///
/// ──── 무엇을 하나 ────
/// 로그인한 계정 한 명의 개인 데이터를 그 계정 전용 파일에 읽고 쓴다.
/// 같은 파일을 AccountManager도 열지만, 둘은 다루는 key가 겹치지 않게 역할을 나눈다.
///   - AccountManager : 자격증명(별명/PIN) — 로그인에 필요한 신원 정보
///   - UserPrefs(이 클래스) : 그 외 개인 설정 — 튜토리얼 봤는지, 테마, 출석, 작성 중 리뷰 등
///
/// ──── 계정마다 별도 파일 = 계정마다 별도 PlayerPrefs ────
/// 예: alice로 로그인하면 "user_alice", bob으로 로그인하면 "user_bob" 파일을 연다.
/// → 같은 앱이라도 로그인한 계정에 따라 완전히 다른 저장 공간을 쓰는 셈
/// (Unity로 비유하면 세이브 슬롯마다 PlayerPrefs를 따로 두는 것)
///
/// ──── 누가 보유하나 ────
/// App이 "현재 로그인된 계정"의 UserPrefs를 하나 들고 있고, 계정이 바뀌면 다시 만든다.
///   사용처: ((App) getApplication()).getUserPrefs()
///
/// 현재(Phase 6)는 튜토리얼 여부(tutorial_seen)만 다룬다.
/// 프로필(Phase 7)·테마(Phase 8)·출석(Phase 9)·리뷰 드래프트(Phase 10)·
/// 보관함 마지막 상태(Phase 11)는 이 클래스에 메서드를 더해 쌓는다.
/// </summary>
public class UserPrefs {

    /// <summary>
    /// 계정별 파일 이름 앞에 붙이는 접두사
    /// ★ 주의: AccountManager의 FILE_USER_PREFIX와 반드시 같은 값이어야 한다
    ///   (둘이 똑같은 "user_<id>" 파일을 가리켜야 별명/PIN과 개인설정이 한 파일에 모임)
    /// </summary>
    private static final String FILE_USER_PREFIX = "user_";

    /// <summary>
    /// key: 이 계정이 튜토리얼(온보딩)을 본 적 있는지
    /// 기본값 false → 처음 로그인한 계정은 "아직 안 봄"으로 시작
    /// </summary>
    private static final String KEY_TUTORIAL_SEEN = "tutorial_seen";

    /// <summary>
    /// key: 아바타 원의 색 (ARGB 정수 한 개로 저장)
    /// 색 자체를 숫자로 저장하므로 화면에서 그대로 꺼내 원에 칠하면 됨
    /// </summary>
    private static final String KEY_AVATAR_COLOR = "avatar_color";

    /// <summary>
    /// key: 한 줄 소개(bio)
    /// </summary>
    private static final String KEY_BIO = "bio";

    /// <summary>
    /// 아직 색을 고르지 않은 계정에 쓸 기본 아바타 색 (파랑)
    /// 0xFF... 형태의 ARGB 값 (맨 앞 FF = 불투명)
    /// ★ 프로필 편집 화면(ProfileEditActivity)의 색 팔레트에도 이 값이 들어 있어야
    ///   처음 들어갔을 때 "현재 색"이 팔레트에서 선택된 상태로 보인다
    /// </summary>
    private static final int DEFAULT_AVATAR_COLOR = 0xFF1E88E5;

    /// <summary>
    /// 이 UserPrefs가 담당하는 계정의 저장소 핸들 (user_<id> 파일)
    /// 생성 시점에 한 번 열어두고 계속 재사용
    /// </summary>
    private final SharedPreferences prefs;

    /// <summary>
    /// 특정 계정의 개인 설정 저장소를 연다
    /// </summary>
    /// <param name="context">파일을 열기 위한 안드로이드 컨텍스트</param>
    /// <param name="accountId">이 저장소가 담당할 계정 아이디 (파일 이름 user_<id>가 됨)</param>
    public UserPrefs(Context context, String accountId) {
        this.prefs = context.getSharedPreferences(FILE_USER_PREFIX + accountId, Context.MODE_PRIVATE);
    }

    // ========== 튜토리얼 (tutorial_seen) ==========

    /// <summary>
    /// 이 계정이 튜토리얼을 본 적 있는지 반환 (기본값: 아직 안 봄 = false)
    /// </summary>
    public boolean hasSeenTutorial() {
        return prefs.getBoolean(KEY_TUTORIAL_SEEN, false);
    }

    /// <summary>
    /// 튜토리얼을 봤는지 여부를 저장 (온보딩을 끝까지 본 뒤 true로 기록)
    /// </summary>
    public void setTutorialSeen(boolean seen) {
        prefs.edit()
                .putBoolean(KEY_TUTORIAL_SEEN, seen)
                .apply();
    }

    // ========== 프로필 (avatar_color / bio) ==========

    /// <summary>
    /// 아바타 색을 반환 (아직 고른 적 없으면 기본 파랑)
    /// </summary>
    public int getAvatarColor() {
        return prefs.getInt(KEY_AVATAR_COLOR, DEFAULT_AVATAR_COLOR);
    }

    /// <summary>
    /// 아바타 색을 저장 (ARGB 정수)
    /// </summary>
    public void setAvatarColor(int color) {
        prefs.edit()
                .putInt(KEY_AVATAR_COLOR, color)
                .apply();
    }

    /// <summary>
    /// 한 줄 소개를 반환 (아직 없으면 빈 문자열)
    /// </summary>
    public String getBio() {
        return prefs.getString(KEY_BIO, "");
    }

    /// <summary>
    /// 한 줄 소개를 저장
    /// </summary>
    public void setBio(String bio) {
        prefs.edit()
                .putString(KEY_BIO, bio)
                .apply();
    }
}
