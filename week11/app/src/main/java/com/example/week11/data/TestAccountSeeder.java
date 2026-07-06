package com.example.week11.data;

import android.content.Context;

import com.example.week11.account.AccountManager;
import com.example.week11.account.UserPrefs;

import java.util.Random;

/// <summary>
/// 테스트/시연용 계정을 "항상 존재하도록" 심어두는 도우미 (미니 커뮤니티 만들기)
///
/// ──── 무엇을 하나 ────
/// 미리 정해둔 테스트 유저 여러 명(프로필 + 샘플 리뷰)을 만든다.
/// 커뮤니티("다른 사람들의 평가")를 바로 보여주려면 리뷰가 있는 다른 계정이 많아야 하는데,
/// 매번 손으로 만들기 번거로우니 앱이 시작할 때 자동으로 심어둔다.
/// 여러 유저의 리뷰를 같은 게임에 겹쳐 두어, 게임 상세에서 "여러 명의 평가/평균"이 보이게 한다.
///
/// ──── 삭제되지 않게 ────
/// - 앱 시작(App.onCreate) 때 seedIfMissing() → 없으면 다시 만든다
/// - 전체 초기화(resetAll) 뒤에도 다시 심으므로 "초기화해도 사라지지 않는" 계정이 된다
///
/// ──── 구분 표시 ────
/// 테스트 계정 아이디는 모두 "tester"로 시작 → isTestAccount로 판별해 로그인 목록 뒤에 "(test)" 표시
/// (영문이라 숫자 아이디들 뒤, 즉 목록 맨 끝에 정렬돼 보인다)
/// </summary>
public class TestAccountSeeder {

    /// <summary>테스트 계정 아이디 공통 접두사 ("(test)" 표시 판별용)</summary>
    private static final String TEST_ID_PREFIX = "tester";

    /// <summary>테스트 계정 공통 비밀번호(PIN) — 시연 편의상 0000</summary>
    private static final String TEST_PIN = "0000";

    /// <summary>1시간(ms) — 리뷰 작성 시각을 과거로 흩뿌릴 때 쓰는 단위</summary>
    private static final long HOUR_MS = 60L * 60L * 1000L;

    /// <summary>시드 리뷰의 최대 나이(ms) — 최대 이만큼 과거까지 흩뿌린다 (2주)</summary>
    private static final long MAX_REVIEW_AGE_MS = 14L * 24L * HOUR_MS;

    /// <summary>
    /// 리뷰 작성 시각을 무작위 과거로 흩뿌리기 위한 난수 생성기
    /// (모두 같은 시각이면 피드가 계정별로 뭉쳐 보이므로, 리뷰마다 다른 과거 시각을 준다)
    /// </summary>
    private final Random random = new Random();

    /// <summary>
    /// 계정 파일(user_<id>)을 열기 위한 앱 컨텍스트
    /// </summary>
    private final Context appContext;

    /// <summary>
    /// 가입(등록)을 맡는 전역 계정 관리자
    /// </summary>
    private final AccountManager accountManager;

    /// <summary>
    /// 시더 생성 (App이 하나 만들어 보유)
    /// </summary>
    public TestAccountSeeder(Context context, AccountManager accountManager) {
        this.appContext = context.getApplicationContext();
        this.accountManager = accountManager;
    }

    /// <summary>
    /// 해당 아이디가 테스트 계정인지 (로그인 목록에서 "(test)" 표시에 사용)
    /// 테스트 계정 아이디는 모두 "tester"로 시작하도록 만든다
    /// </summary>
    public static boolean isTestAccount(String id) {
        return id != null && id.startsWith(TEST_ID_PREFIX);
    }

    /// <summary>
    /// 테스트 유저들이 없으면 심는다 (이미 있으면 각자 건드리지 않음)
    /// 앱 시작 시 + 전체 초기화 직후에 호출
    ///
    /// 유저마다 아바타 색·소개가 다르고, 리뷰를 여러 게임에 겹쳐 남겨
    /// 게임 상세의 "다른 사람들의 평가"가 풍성하게 채워진다.
    /// (게임 id: 1엘든링 2발더스 3할로우 4셀레스테 5스타듀 6하데스 9포탈2
    ///          10위쳐3 11사펑 12다크소울3 13림월드 15슬더스 16언더테일 18오리 19데드셀)
    /// </summary>
    public void seedIfMissing() {
        // 계정마다 리뷰 개수를 1~5개로 다르게 둔다 (실제 유저처럼 활동량이 제각각으로 보이게)
        // t1=5, t2=3, t3=2, t4=4, t5=1, t6=3, t7=5, t8=2, t9=4
        seedUser("tester1", "픽셀덕후", 0xFFFB8C00, "인디부터 AAA까지 잡식",
                new int[]{1, 6, 9, 3, 16},
                new float[]{4.5f, 5.0f, 4.0f, 4.5f, 4.0f},
                new String[]{"엘든 링 어렵지만 결국 명작", "하데스 갓겜 인정합니다", "포탈2 두뇌 풀가동",
                        "할로우 나이트 손맛이 좋아요", "언더테일 스토리에 뭉클"});

        seedUser("tester2", "인디게이머", 0xFF43A047, "숨은 명작 찾아다니는 중",
                new int[]{1, 3, 6},
                new float[]{3.5f, 5.0f, 4.5f},
                new String[]{"엘든 링은 제 취향은 좀 아니었어요", "할로우 나이트 분위기 최고", "하데스 계속 하게 되네요"});

        seedUser("tester3", "소울충", 0xFFE53935, "죽는 게 곧 재미",
                new int[]{1, 12},
                new float[]{5.0f, 4.5f},
                new String[]{"소울 시리즈 최고봉 엘든 링", "다크 소울3 소울의 완성형"});

        seedUser("tester4", "힐링러", 0xFF00897B, "잔잔한 게임을 좋아해요",
                new int[]{5, 13, 4, 18},
                new float[]{5.0f, 4.5f, 4.0f, 4.5f},
                new String[]{"스타듀 밸리 힐링 그 자체", "림월드 이야기가 끝이 없어요", "셀레스테 어렵지만 따뜻함",
                        "오리 풍경이 마음을 녹여요"});

        seedUser("tester5", "도트장인", 0xFF8E24AA, "픽셀 아트에 진심",
                new int[]{3},
                new float[]{5.0f},
                new String[]{"할로우 나이트 도트의 정점"});

        seedUser("tester6", "퍼즐러", 0xFF1E88E5, "머리 쓰는 게임 환영",
                new int[]{9, 3, 16},
                new float[]{5.0f, 4.0f, 3.5f},
                new String[]{"포탈2 퍼즐 게임의 교과서", "할로우 나이트 길 찾기 재밌음", "언더테일 퍼즐도 은근 좋아요"});

        seedUser("tester7", "로그러", 0xFFD81B60, "로그라이크만 판다",
                new int[]{6, 15, 19, 3, 12},
                new float[]{4.5f, 5.0f, 4.0f, 4.5f, 4.0f},
                new String[]{"하데스 로그라이크 입문 추천", "슬더스 덱빌딩 중독됨", "데드셀 죽어도 또 한판",
                        "할로우 나이트도 도전 욕구 자극", "다크 소울3 반복 학습이 은근 로그라이크"});

        seedUser("tester8", "오픈월드러", 0xFF6D4C41, "넓은 세계 탐험이 좋아",
                new int[]{2, 10},
                new float[]{5.0f, 4.5f},
                new String[]{"발더스 게이트3 선택의 무게", "위쳐3 방대한 세계"});

        seedUser("tester9", "레트로충", 0xFF757575, "고전 감성 좋아요",
                new int[]{16, 9, 1, 4},
                new float[]{5.0f, 4.5f, 4.0f, 4.5f},
                new String[]{"언더테일 감성 최고", "포탈2 시대를 앞선 클래식", "엘든 링 대작은 대작",
                        "셀레스테 도트 감성 최고"});
    }

    /// <summary>
    /// 테스트 유저 한 명을 심는다 (이미 있으면 건너뜀)
    /// 프로필(아바타 색·소개) + 게임별 정식 리뷰(별점·한줄평)를 함께 저장
    /// gameIds/ratings/reviews는 같은 길이의 짝 배열 (i번째끼리 한 리뷰)
    /// </summary>
    private void seedUser(String id, String nickname, int color, String bio,
                          int[] gameIds, float[] ratings, String[] reviews) {
        if (accountManager.isRegistered(id)) {
            return;
        }

        accountManager.register(id, nickname, TEST_PIN);

        UserPrefs prefs = new UserPrefs(appContext, id);
        prefs.setAvatarColor(color);
        prefs.setBio(bio);
        long now = System.currentTimeMillis();
        for (int i = 0; i < gameIds.length; i++) {
            // 리뷰마다 1시간 ~ 2주 전 사이의 무작위 시각 → 피드가 계정 구분 없이 자연스럽게 섞임
            long ageMs = HOUR_MS + (long) (random.nextDouble() * (MAX_REVIEW_AGE_MS - HOUR_MS));
            long reviewedAt = now - ageMs;
            prefs.saveReview(gameIds[i], ratings[i], reviews[i], reviewedAt);
        }
    }
}
