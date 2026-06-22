package com.example.week10.account;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;

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
    /// key: 화면 테마 (ThemeMode의 이름 SYSTEM/LIGHT/DARK 문자열로 저장)
    /// enum 이름을 문자열로 저장해두면 나중에 다시 ThemeMode로 되돌리기 쉬움
    /// </summary>
    private static final String KEY_THEME = "theme";

    /// <summary>
    /// key: 누적 방문 횟수 (하루에 한 번만 올라감)
    /// </summary>
    private static final String KEY_VISIT_COUNT = "visit_count";

    /// <summary>
    /// key: 마지막으로 방문한 날짜 ("2026-06-23" 같은 yyyy-MM-dd 문자열)
    /// "오늘 이미 방문했는지" / "어제 방문했는지"를 이 값과 비교해 판단
    /// </summary>
    private static final String KEY_LAST_VISIT_DATE = "last_visit_date";

    /// <summary>
    /// key: 연속 방문 일수 (어제도 왔으면 +1, 하루라도 건너뛰면 1로 리셋)
    /// </summary>
    private static final String KEY_STREAK = "streak";

    /// <summary>
    /// key 앞부분: 작성 중인 리뷰 초안 (게임마다 따로) → 실제 key는 "draft_review_<게임id>"
    /// 게임 12번 리뷰를 쓰다 말면 "draft_review_12"에 저장됨
    /// </summary>
    private static final String KEY_DRAFT_REVIEW_PREFIX = "draft_review_";

    /// <summary>
    /// key: 보관함에서 마지막으로 보던 필터 탭 위치 (0=전체, 1부터 상태별)
    /// </summary>
    private static final String KEY_LAST_FILTER_TAB = "last_filter_tab";

    /// <summary>
    /// key: 보관함에서 마지막으로 고른 정렬 기준 (GameSortOrder의 이름 문자열로 저장)
    /// GameSortOrder는 library 쪽 enum이라, UserPrefs는 그 타입을 모른 채 "이름(문자열)"만 보관한다
    /// (enum ↔ 이름 변환은 이 값을 쓰는 LibraryActivity가 담당)
    /// </summary>
    private static final String KEY_LAST_SORT = "last_sort";

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

    // ========== 테마 (theme) ==========

    /// <summary>
    /// 이 계정의 화면 테마를 반환 (아직 고른 적 없으면 시스템 설정 따라감)
    ///
    /// 저장은 enum 이름(문자열)으로 해두었으므로 ThemeMode.valueOf로 되돌린다.
    /// 혹시 저장값이 깨져 있으면(없거나 알 수 없는 이름) 안전하게 SYSTEM으로 처리한다.
    /// </summary>
    public ThemeMode getThemeMode() {
        String saved = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name());
        // 알 수 없는 값이 들어와도 앱이 죽지 않도록 하나씩 비교해 찾는다
        for (ThemeMode mode : ThemeMode.values()) {
            if (mode.name().equals(saved)) {
                return mode;
            }
        }
        return ThemeMode.SYSTEM;
    }

    /// <summary>
    /// 이 계정의 화면 테마를 저장 (enum 이름 문자열로)
    /// </summary>
    public void setThemeMode(ThemeMode mode) {
        prefs.edit()
                .putString(KEY_THEME, mode.name())
                .apply();
    }

    // ========== 출석/방문 (visit_count / last_visit_date / streak) ==========

    /// <summary>
    /// 누적 방문 횟수를 반환 (없으면 0)
    /// </summary>
    public int getVisitCount() {
        return prefs.getInt(KEY_VISIT_COUNT, 0);
    }

    /// <summary>
    /// 연속 방문 일수를 반환 (없으면 0)
    /// </summary>
    public int getStreak() {
        return prefs.getInt(KEY_STREAK, 0);
    }

    /// <summary>
    /// 오늘 방문을 기록한다
    ///   - 오늘 이미 방문했으면 → 아무것도 하지 않고 false (하루 한 번만 집계)
    ///   - 오늘 첫 방문이면 → 누적 +1, 연속 일수 갱신, 마지막 방문일을 오늘로 저장하고 true
    ///     · 어제도 방문했으면 연속 +1
    ///     · 하루라도 건너뛰었으면(또는 첫 방문) 연속을 1로 리셋
    ///
    /// 오늘 날짜를 인자로 받는 이유: UserPrefs가 직접 "지금 시각"을 읽지 않게 하기 위함.
    /// (시계에 직접 의존하지 않으면 동작을 예측·확인하기 쉬워짐)
    /// </summary>
    /// <param name="today">호출하는 쪽에서 구한 오늘 날짜 (LocalDate.now())</param>
    /// <returns>오늘 첫 방문으로 새로 집계되었으면 true</returns>
    public boolean recordVisit(LocalDate today) {
        // LocalDate를 "2026-06-23" 형태 문자열로 (toString 기본 형식이 yyyy-MM-dd)
        String todayStr = today.toString();
        String lastStr = prefs.getString(KEY_LAST_VISIT_DATE, null);

        // 오늘 이미 방문했으면 중복 집계하지 않음
        boolean alreadyVisitedToday = todayStr.equals(lastStr);
        if (alreadyVisitedToday) {
            return false;
        }

        int newVisitCount = getVisitCount() + 1;

        // 어제 날짜 문자열과 비교해 "연속"인지 판단
        String yesterdayStr = today.minusDays(1).toString();
        boolean visitedYesterday = yesterdayStr.equals(lastStr);
        int newStreak = visitedYesterday ? getStreak() + 1 : 1;

        prefs.edit()
                .putInt(KEY_VISIT_COUNT, newVisitCount)
                .putInt(KEY_STREAK, newStreak)
                .putString(KEY_LAST_VISIT_DATE, todayStr)
                .apply();
        return true;
    }

    // ========== 리뷰 초안 (draft_review_<게임id>) ==========

    /// <summary>
    /// 게임 id로 초안 key를 만든다 (예: 12 → "draft_review_12")
    /// 게임마다 다른 key를 써야 서로 다른 게임의 초안이 섞이지 않음
    /// </summary>
    private String draftKey(int gameId) {
        return KEY_DRAFT_REVIEW_PREFIX + gameId;
    }

    /// <summary>
    /// 이 게임에 저장된 리뷰 초안이 있는지 확인 (Tester)
    /// </summary>
    public boolean hasDraftReview(int gameId) {
        return prefs.contains(draftKey(gameId));
    }

    /// <summary>
    /// 이 게임의 리뷰 초안을 반환 (없으면 빈 문자열)
    /// </summary>
    public String getDraftReview(int gameId) {
        return prefs.getString(draftKey(gameId), "");
    }

    /// <summary>
    /// 이 게임의 리뷰 초안을 저장 (작성 중 실시간 호출 → 앱이 꺼져도 내용이 남음)
    /// </summary>
    public void saveDraftReview(int gameId, String text) {
        prefs.edit()
                .putString(draftKey(gameId), text)
                .apply();
    }

    /// <summary>
    /// 이 게임의 리뷰 초안을 지운다 (정식 저장이 끝나 더 이상 초안이 필요 없을 때)
    /// </summary>
    public void clearDraftReview(int gameId) {
        prefs.edit()
                .remove(draftKey(gameId))
                .apply();
    }

    // ========== 보관함 마지막 상태 (last_filter_tab / last_sort) ==========

    /// <summary>
    /// 마지막으로 보던 필터 탭 위치를 반환 (없으면 0 = 전체 탭)
    /// </summary>
    public int getLastFilterTab() {
        return prefs.getInt(KEY_LAST_FILTER_TAB, 0);
    }

    /// <summary>
    /// 마지막으로 보던 필터 탭 위치를 저장
    /// </summary>
    public void setLastFilterTab(int tabPosition) {
        prefs.edit()
                .putInt(KEY_LAST_FILTER_TAB, tabPosition)
                .apply();
    }

    /// <summary>
    /// 마지막으로 고른 정렬 기준 이름을 반환 (없으면 넘겨준 기본값 그대로)
    /// </summary>
    /// <param name="fallback">저장된 값이 없을 때 돌려줄 기본 정렬 이름</param>
    public String getLastSort(String fallback) {
        return prefs.getString(KEY_LAST_SORT, fallback);
    }

    /// <summary>
    /// 마지막으로 고른 정렬 기준 이름을 저장 (GameSortOrder.name() 문자열)
    /// </summary>
    public void setLastSort(String sortName) {
        prefs.edit()
                .putString(KEY_LAST_SORT, sortName)
                .apply();
    }
}
