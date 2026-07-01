package com.example.week10.account;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    /// key 앞부분: 작성 중인 별점 초안 (게임마다 따로) → 실제 key는 "draft_rating_<게임id>"
    /// 한줄평 초안과 짝을 이뤄, 별점도 쓰다 말면 그대로 남아 복원됨
    /// </summary>
    private static final String KEY_DRAFT_RATING_PREFIX = "draft_rating_";

    /// <summary>
    /// key 앞부분: 정식 저장된 별점 (게임마다 따로) → 실제 key는 "rating_<게임id>"
    /// draft_(작성 중 임시)와 달리, "저장" 버튼을 눌러 확정한 값. 계정마다 따로 보관됨
    /// </summary>
    private static final String KEY_RATING_PREFIX = "rating_";

    /// <summary>
    /// key 앞부분: 정식 저장된 한줄평 → 실제 key는 "review_<게임id>"
    /// </summary>
    private static final String KEY_REVIEW_PREFIX = "review_";

    /// <summary>
    /// key 앞부분: 리뷰를 저장한 시각(밀리초) → 실제 key는 "reviewed_at_<게임id>"
    /// 팔로잉 피드를 최신순으로 정렬하는 데 사용
    /// (주의: "reviewed_at_"는 "review_"로 시작하지 않으므로 리뷰 개수 집계와 안 섞임)
    /// </summary>
    private static final String KEY_REVIEWED_AT_PREFIX = "reviewed_at_";

    /// <summary>
    /// key 앞부분: 이 계정이 "누른 좋아요" → 실제 key는 "like_<게임id>_<작성자id>"
    /// 이 계정이 (그 게임에 대한, 작성자 X의) 리뷰에 좋아요를 눌렀으면 그 key가 존재
    /// (좋아요는 "누가 눌렀나"를 누른 사람 파일에 저장 → 개수는 계정들을 훑어서 셈)
    /// </summary>
    private static final String KEY_LIKE_PREFIX = "like_";

    /// <summary>
    /// key 앞부분: 이 계정이 "팔로우한 상대" → 실제 key는 "follow_<상대id>"
    /// 내가 X를 팔로우하면 내 파일에 "follow_X" 가 생김
    /// (팔로잉 수 = 내 follow_ key 개수 / 팔로워 수 = 나를 follow_한 계정 수)
    /// </summary>
    private static final String KEY_FOLLOW_PREFIX = "follow_";

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
    /// 아직 색을 고르지 않은 계정에 쓸 기본 아바타 색 (파랑) — 기본색의 "주인"은 여기 한 곳
    /// 0xFF... 형태의 ARGB 값 (맨 앞 FF = 불투명)
    /// 프로필 편집 화면도 팔레트에서 이 값을 그대로 가져다 써서 항상 일치한다
    ///   (public static: ProfileEditActivity가 UserPrefs.DEFAULT_AVATAR_COLOR로 참조)
    /// </summary>
    public static final int DEFAULT_AVATAR_COLOR = 0xFF1E88E5;

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
        // 계정 파일 이름 접두사는 AccountManager가 소유한 공용 상수를 그대로 사용
        // → 두 클래스가 항상 같은 user_<id> 파일을 가리킴 (값 중복/불일치 가능성 제거)
        this.prefs = context.getSharedPreferences(
                AccountManager.FILE_USER_PREFIX + accountId, Context.MODE_PRIVATE);
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

    // ========== 별점 초안 (draft_rating_<게임id>) ==========

    /// <summary>
    /// 게임 id로 별점 초안 key를 만든다 (예: 12 → "draft_rating_12")
    /// </summary>
    private String draftRatingKey(int gameId) {
        return KEY_DRAFT_RATING_PREFIX + gameId;
    }

    /// <summary>
    /// 이 게임에 저장된 별점 초안이 있는지 확인 (Tester)
    /// </summary>
    public boolean hasDraftRating(int gameId) {
        return prefs.contains(draftRatingKey(gameId));
    }

    /// <summary>
    /// 이 게임의 별점 초안을 반환 (없으면 0)
    /// </summary>
    public float getDraftRating(int gameId) {
        return prefs.getFloat(draftRatingKey(gameId), 0f);
    }

    /// <summary>
    /// 이 게임의 별점 초안을 저장 (별점을 바꿀 때 실시간 호출)
    /// </summary>
    public void saveDraftRating(int gameId, float rating) {
        prefs.edit()
                .putFloat(draftRatingKey(gameId), rating)
                .apply();
    }

    /// <summary>
    /// 이 게임의 별점 초안을 지운다 (정식 저장이 끝났을 때)
    /// </summary>
    public void clearDraftRating(int gameId) {
        prefs.edit()
                .remove(draftRatingKey(gameId))
                .apply();
    }

    // ========== 정식 리뷰 (rating_<게임id> / review_<게임id>) ==========
    // 이 계정이 그 게임에 "저장" 버튼으로 확정한 별점/한줄평.
    // 계정마다 별도 파일(user_<id>)에 있으므로, 같은 게임이라도 계정마다 리뷰가 다르다.
    // (CommunityRepository가 다른 계정의 UserPrefs를 열어 "다른 사람들의 평가"를 모을 때도 이 값을 읽음)

    /// <summary>게임 id로 별점 key를 만든다 (예: 12 → "rating_12")</summary>
    private String ratingKey(int gameId) {
        return KEY_RATING_PREFIX + gameId;
    }

    /// <summary>게임 id로 한줄평 key를 만든다 (예: 12 → "review_12")</summary>
    private String reviewKey(int gameId) {
        return KEY_REVIEW_PREFIX + gameId;
    }

    /// <summary>
    /// 이 계정이 그 게임에 정식 리뷰(별점/한줄평)를 남긴 적 있는지
    /// </summary>
    public boolean hasReview(int gameId) {
        return prefs.contains(ratingKey(gameId)) || prefs.contains(reviewKey(gameId));
    }

    /// <summary>
    /// 이 계정의 그 게임 별점을 반환 (없으면 0)
    /// </summary>
    public float getRating(int gameId) {
        return prefs.getFloat(ratingKey(gameId), 0f);
    }

    /// <summary>
    /// 이 계정의 그 게임 한줄평을 반환 (없으면 빈 문자열)
    /// </summary>
    public String getReview(int gameId) {
        return prefs.getString(reviewKey(gameId), "");
    }

    /// <summary>게임 id로 리뷰 작성 시각 key를 만든다 (예: 12 → "reviewed_at_12")</summary>
    private String reviewedAtKey(int gameId) {
        return KEY_REVIEWED_AT_PREFIX + gameId;
    }

    /// <summary>
    /// 이 계정의 그 게임 별점/한줄평을 정식 저장 ("저장" 버튼을 눌렀을 때)
    /// 저장 시각(현재 시간)도 함께 기록 → 팔로잉 피드 정렬에 사용
    /// </summary>
    public void saveReview(int gameId, float rating, String review) {
        prefs.edit()
                .putFloat(ratingKey(gameId), rating)
                .putString(reviewKey(gameId), review)
                .putLong(reviewedAtKey(gameId), System.currentTimeMillis())
                .apply();
    }

    /// <summary>
    /// 이 계정이 그 게임 리뷰를 저장한 시각(밀리초) 반환 (없으면 0)
    /// </summary>
    public long getReviewedAt(int gameId) {
        return prefs.getLong(reviewedAtKey(gameId), 0L);
    }

    /// <summary>
    /// 이 계정이 리뷰를 남긴 게임 id들을 반환 (피드에서 "이 사람이 무슨 게임을 리뷰했나" 수집용)
    /// 저장된 key 중 "review_"로 시작하는 것에서 뒤의 숫자(게임 id)를 꺼낸다
    /// </summary>
    public List<Integer> getReviewedGameIds() {
        List<Integer> gameIds = new ArrayList<>();
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(KEY_REVIEW_PREFIX)) {
                gameIds.add(Integer.parseInt(key.substring(KEY_REVIEW_PREFIX.length())));
            }
        }
        return gameIds;
    }

    // ========== 리뷰 좋아요 (like_<게임id>_<작성자id>) ==========

    /// <summary>
    /// 좋아요 key를 만든다 (예: 게임 1, 작성자 tester1 → "like_1_tester1")
    /// </summary>
    private String likeKey(int gameId, String reviewerId) {
        return KEY_LIKE_PREFIX + gameId + "_" + reviewerId;
    }

    /// <summary>
    /// 이 계정이 (게임 gameId, 작성자 reviewerId)의 리뷰에 좋아요를 눌렀는지
    /// </summary>
    public boolean hasLiked(int gameId, String reviewerId) {
        return prefs.contains(likeKey(gameId, reviewerId));
    }

    /// <summary>
    /// 이 계정의 좋아요를 켜거나(true) 끈다(false) — 하트 토글에 사용
    /// 끌 때는 key를 아예 지운다(remove) → 개수 집계에서 자연히 빠짐
    /// </summary>
    public void setLiked(int gameId, String reviewerId, boolean liked) {
        if (liked) {
            prefs.edit().putBoolean(likeKey(gameId, reviewerId), true).apply();
        } else {
            prefs.edit().remove(likeKey(gameId, reviewerId)).apply();
        }
    }

    // ========== 팔로우 (follow_<상대id>) ==========

    /// <summary>상대 id로 팔로우 key를 만든다 (예: bob → "follow_bob")</summary>
    private String followKey(String targetId) {
        return KEY_FOLLOW_PREFIX + targetId;
    }

    /// <summary>
    /// 이 계정이 상대(targetId)를 팔로우하고 있는지
    /// </summary>
    public boolean isFollowing(String targetId) {
        return prefs.contains(followKey(targetId));
    }

    /// <summary>
    /// 팔로우를 켜거나(true) 끈다(false) — 팔로우/언팔로우 버튼에 사용
    /// </summary>
    public void setFollowing(String targetId, boolean follow) {
        if (follow) {
            prefs.edit().putBoolean(followKey(targetId), true).apply();
        } else {
            prefs.edit().remove(followKey(targetId)).apply();
        }
    }

    /// <summary>
    /// 이 계정이 팔로우한 사람 수 ("follow_"로 시작하는 key 개수)
    /// </summary>
    public int getFollowingCount() {
        int count = 0;
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(KEY_FOLLOW_PREFIX)) {
                count++;
            }
        }
        return count;
    }

    /// <summary>
    /// 이 계정이 정식으로 남긴 리뷰 개수 (랭킹 "리뷰왕" 집계에 사용)
    ///
    /// 저장된 key 중 "review_"로 시작하는 것의 개수를 센다.
    /// (작성 중 초안은 "draft_review_"라 접두사가 달라 자동으로 제외됨)
    /// </summary>
    public int getReviewCount() {
        int count = 0;
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(KEY_REVIEW_PREFIX)) {
                count++;
            }
        }
        return count;
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
