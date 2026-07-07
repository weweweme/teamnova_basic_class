package com.example.week12;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

import com.example.week12.account.AccountManager;
import com.example.week12.account.UserPrefs;
import com.example.week12.data.ActivityLogRepository;
import com.example.week12.data.CommunityRepository;
import com.example.week12.data.GameRepository;
import com.example.week12.data.TestAccountSeeder;
import com.example.week12.util.CoverImageLoader;

/// <summary>
/// 앱 전역 Application 클래스
///
/// ──── 왜 필요한가 ────
/// 지금까지는 MainActivity가 onCreate에서 new GameRepository()로 직접 생성했음.
/// 그런데 다른 Activity로 이동하면 Game을 Parcelable로 복사해서 주고받기 때문에,
/// 받는 쪽 Activity에서 Game을 수정해도 원본(MainActivity가 들고 있던 저장소)에는
/// 반영되지 않음. → 별점/리뷰/스크린샷을 바꿔도 MainActivity로 돌아오면 변경이 사라져 보이는 문제
///
/// 해결: GameRepository를 Application 레벨에 단 하나만 두고, 모든 Activity가
/// 이 단일 인스턴스를 공유하도록 만든다. (Unity의 DontDestroyOnLoad 매니저 싱글톤과 비슷한 개념)
///
/// ──── 호출 시점 ────
/// 앱 프로세스가 만들어질 때 단 한 번, onCreate가 호출됨
/// 어떤 Activity의 onCreate보다도 먼저 실행됨 → 이후 어디서든 안전하게 접근 가능
/// </summary>
public class App extends Application {

    /// <summary>
    /// 앱 전역 게임 저장소 (모든 Activity가 공유)
    /// 사용처: ((App) getApplication()).getGameRepository()
    /// </summary>
    private GameRepository gameRepository;

    /// <summary>
    /// 앱 전역 활동 로그 저장소 (타임라인 표시용)
    /// 사용처: ((App) getApplication()).getActivityLogRepository()
    /// </summary>
    private ActivityLogRepository activityLogRepository;

    /// <summary>
    /// 앱 전역 계정 관리자 (로그인/회원가입/자동로그인 — app_global 파일 담당)
    /// 사용처: ((App) getApplication()).getAccountManager()
    /// </summary>
    private AccountManager accountManager;

    /// <summary>
    /// 현재 로그인된 계정의 개인 설정 저장소 (user_<id> 파일 담당)
    /// 로그인한 계정이 바뀌면 다른 파일을 봐야 하므로 그때마다 새로 만든다 → getUserPrefs() 참고
    /// </summary>
    private UserPrefs userPrefs;

    /// <summary>
    /// userPrefs가 지금 어느 계정을 담당하고 있는지 기억하는 값
    /// 현재 로그인 계정과 이 값이 다르면 "계정이 바뀐 것" → userPrefs를 새로 만든다
    /// </summary>
    private String userPrefsAccountId;

    /// <summary>
    /// 커뮤니티 저장소 (이 기기의 여러 계정을 가로질러 읽음 — "이 기기의 유저들" 등)
    /// 사용처: ((App) getApplication()).getCommunityRepository()
    /// </summary>
    private CommunityRepository communityRepository;

    /// <summary>
    /// 테스트 계정 시더 (시연용 계정을 항상 존재하게 유지)
    /// </summary>
    private TestAccountSeeder testAccountSeeder;

    /// <summary>
    /// 앱 전역 표지 이미지 로더 (백그라운드 디코딩 + 메모리 캐시)
    /// App이 하나만 보유해야 모든 화면이 같은 캐시를 공유함 → 한 번 로드한 표지는 재사용
    /// 사용처: ((App) getApplication()).getCoverImageLoader()
    /// </summary>
    private CoverImageLoader coverImageLoader;

    /// <summary>
    /// 앱 프로세스 시작 시 단 한 번 호출
    /// 여기서 만든 객체들은 앱이 살아있는 동안 계속 같은 인스턴스로 유지됨
    /// </summary>
    @Override
    public void onCreate() {
        super.onCreate();

        // 카카오 로그인 SDK 초기화 (앱 시작 시 한 번) — 네이티브 앱 키로 SDK를 켠다
        // 이 키는 카카오 개발자 콘솔에서 발급한 "네이티브 앱 키" (RAWG 키처럼 앱 신분증)
        // 주의: 공개 저장소에 올릴 거면 숨기는 게 안전 (학습용이라 코드에 둠)
        KakaoSdk.init(this, "273f1b25dbcaf75b1264b6616d3f1187");

        gameRepository = new GameRepository(this);
        // 앱이 켜질 때 시스템이 스스로 휴지통을 점검해 30일 지난 항목을 자동 정리
        // (사용자가 휴지통 화면을 열지 않아도 처리됨 — 서버가 백그라운드로 청소하는 것과 같은 개념)
        // 휴지통 상태는 prefs에 저장되므로 앱을 껐다 켜도 유지됨 → 이 시작-시 점검이 실제로 동작함
        gameRepository.purgeExpiredTrash(System.currentTimeMillis());
        activityLogRepository = new ActivityLogRepository();
        // this(Application)도 Context의 일종이라 SharedPreferences 파일을 열 수 있다 → 그대로 넘김
        accountManager = new AccountManager(this);
        // 커뮤니티 저장소는 계정 관리자·게임 저장소를 통해 여러 계정/게임을 읽으므로 그 다음에 생성
        communityRepository = new CommunityRepository(this, accountManager, gameRepository);

        // 테스트 계정을 항상 존재하게 유지 (없으면 심는다 — 시연/커뮤니티 데이터용)
        testAccountSeeder = new TestAccountSeeder(this, accountManager);
        testAccountSeeder.seedIfMissing();

        // 표지 이미지 로더 (전역 하나 → 화면 간 캐시 공유)
        coverImageLoader = new CoverImageLoader();
    }

    /// <summary>
    /// 전체 초기화(테스트/시연용) — "새로 설치한" 상태로 되돌린다
    ///   ① 게임 저장소를 하드코딩 초기 데이터로 복구 (세션 중 바뀐 상태/별점/스크린샷/추가게임 원상복구)
    ///   ② 모든 계정·설정(prefs) 삭제
    ///   ③ 테스트 계정 다시 심기 → "초기화해도 테스트 계정은 사라지지 않음"
    ///
    /// 게임(①)은 메모리 데이터라 prefs clear(②)로는 안 지워지므로 따로 되돌려야 한다
    /// </summary>
    public void resetAllData() {
        gameRepository.resetToDefault();
        accountManager.resetAll();
        testAccountSeeder.seedIfMissing();
    }

    /// <summary>
    /// 공용 게임 저장소 반환
    /// Activity에서 ((App) getApplication()).getGameRepository() 형태로 접근
    /// </summary>
    public GameRepository getGameRepository() {
        return gameRepository;
    }

    /// <summary>
    /// 공용 활동 로그 저장소 반환
    /// Activity에서 ((App) getApplication()).getActivityLogRepository() 형태로 접근
    /// </summary>
    public ActivityLogRepository getActivityLogRepository() {
        return activityLogRepository;
    }

    /// <summary>
    /// 공용 계정 관리자 반환
    /// Activity에서 ((App) getApplication()).getAccountManager() 형태로 접근
    /// </summary>
    public AccountManager getAccountManager() {
        return accountManager;
    }

    /// <summary>
    /// 공용 커뮤니티 저장소 반환 (이 기기의 여러 계정 읽기)
    /// </summary>
    public CommunityRepository getCommunityRepository() {
        return communityRepository;
    }

    /// <summary>
    /// 공용 표지 이미지 로더 반환 (백그라운드 디코딩 + 캐시)
    /// Activity/Adapter에서 ((App) getApplication()).getCoverImageLoader() 형태로 접근
    /// </summary>
    public CoverImageLoader getCoverImageLoader() {
        return coverImageLoader;
    }

    /// <summary>
    /// 현재 로그인된 계정의 개인 설정 저장소를 반환
    ///
    /// 로그인한 계정이 바뀌면(다른 계정으로 로그인) 담당 파일도 달라져야 하므로,
    /// 지금 로그인된 계정 아이디와 보관 중인 것을 비교해 다르면 새로 만든다.
    /// (한 번 만든 뒤 같은 계정이면 그대로 재사용 → 매번 새로 만들지 않음)
    ///
    /// 주의: 로그인 전(현재 계정 없음)에 부르면 null을 돌려준다.
    ///       이 메서드는 로그인/가입 이후 화면에서만 사용한다.
    /// </summary>
    public UserPrefs getUserPrefs() {
        String currentId = accountManager.getCurrentAccountId();
        if (currentId == null) {
            return null;
        }

        // 보관 중인 게 없거나, 담당 계정이 바뀌었으면 새로 만든다
        boolean needsRebuild = userPrefs == null || !currentId.equals(userPrefsAccountId);
        if (needsRebuild) {
            userPrefs = new UserPrefs(this, currentId);
            userPrefsAccountId = currentId;
        }
        return userPrefs;
    }
}
