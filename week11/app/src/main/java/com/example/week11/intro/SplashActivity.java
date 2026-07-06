package com.example.week11.intro;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.week11.App;
import com.example.week11.account.AccountManager;
import com.example.week11.account.LoginActivity;
import com.example.week11.databinding.ActivitySplashBinding;
import com.example.week11.main.MainActivity;
import com.example.week11.model.Game;
import com.example.week11.util.CoverImageLoader;

/// <summary>
/// 스플래시 화면 (앱 진입점)
/// 로고를 잠깐 보여준 뒤(지연 없이), 자동 로그인 여부에 따라 바로 Home 또는 Login으로 이동
///
/// ──── Intent 학습 ────
/// Intent Filter: Manifest에 MAIN + LAUNCHER 등록 (앱 아이콘으로 실행되는 진입점)
/// 명시적 Intent: Home 또는 Login으로 이동 (자동 로그인 분기)
/// Flags: FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
///   → Splash를 백스택에서 아예 제거 (뒤로가기로 돌아올 수 없게)
///   → Unity에서 로고 Scene을 LoadSceneMode.Single로 날려버리는 것과 동일
/// </summary>
///
/// ──── @SuppressLint("CustomSplashScreen") 이 왜 필요한가 ────
/// 공식 문서: https://developer.android.com/develop/ui/views/launch/splash-screen
/// 마이그레이션 가이드: https://developer.android.com/develop/ui/views/launch/splash-screen/migrate
/// 테마/아트(로고) 설정 가이드: https://developer.android.com/develop/ui/views/launch/splash-screen#elements
///
/// Android 12(API 31)부터는 시스템이 앱 아이콘 기반의 SplashScreen을 자동으로 띄워줌
///   → androidx.core.splashscreen.SplashScreen API + Theme.SplashScreen 사용이 표준
///   → 별도의 SplashActivity를 만들 필요가 없어짐
/// 그런데 우리는 예전 방식(SplashActivity 직접 구현)으로 만들었기 때문에
/// Lint가 "CustomSplashScreen" 이라는 경고를 띄움
///   → "요즘은 이렇게 안 만들어도 되는데?" 라는 뜻
///
/// 구식 방식을 사용한 이유 (학습 목적):
///   이 화면 하나에서 다음 개념들을 한꺼번에 체험할 수 있기 때문
///     - Intent Filter (Manifest에 MAIN + LAUNCHER)
///     - 명시적 Intent + Flags (CLEAR_TASK)
///
/// SuppressLint 어노테이션 의미:
///   "이 Lint 경고(CustomSplashScreen)는 의도적으로 무시하겠다"
///   괄호 안 문자열이 Lint 경고 ID
///   경고를 그냥 끄는 게 아니라, "이유를 알고 끈다"는 표시
///
/// ──── 로고/스플래시 아트 관련 ────
/// 공식 리소스 가이드: https://developer.android.com/develop/ui/views/launch/splash-screen#splash_screen_dimensions
///   - windowSplashScreenAnimatedIcon: 중앙 아이콘 (adaptive icon 권장)
///   - windowSplashScreenBackground: 배경색
/// Material Design 가이드: https://m3.material.io/styles/motion/transitions/splash-screen
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    // ========== Lifecycle ==========

    /// <summary>
    /// 스플래시 화면 생성
    /// ViewBinding으로 레이아웃 연결 후, 표지를 미리 캐시에 담고 곧바로 다음 화면으로 이동
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding으로 레이아웃 연결
        // Unity로 비유하면 Prefab을 Instantiate하고 참조를 잡는 것
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 표지를 미리 디코딩해 캐시에 담아둔다 (백그라운드 스레드 풀에서 처리 → 화면 안 멈춤)
        // → 로그인 후 보관함/상세에 들어갔을 때 회색 로딩 없이 바로 표지가 뜸
        preloadCovers();

        // 지연 없이 곧바로 다음 화면으로 이동 (자동 로그인 분기)
        navigateToNextScreen();
    }

    /// <summary>
    /// 게임 표지들을 미리 디코딩해 공용 캐시에 담아둠 (진입 시 미리 준비)
    /// 실제 디코딩은 로더가 백그라운드 스레드 풀에서 처리 → 스플래시 화면은 안 멈춤
    /// </summary>
    private void preloadCovers() {
        App app = (App) getApplication();
        CoverImageLoader loader = app.getCoverImageLoader();
        String packageName = getPackageName();

        for (Game game : app.getGameRepository().getAllGames()) {
            // 표지 이름(문자열)으로 drawable 리소스 id를 찾아 미리 로드
            int coverResId = getResources().getIdentifier(
                    game.getCoverAssetName(), "drawable", packageName);
            loader.preload(getResources(), coverResId);
        }
    }

    // ========== 화면 이동 ==========

    /// <summary>
    /// 자동 로그인 여부에 따라 다음 화면을 정해 이동
    ///   "로그인 유지"가 켜져 있고(auto_login) + 로그인된 계정이 남아 있으면(current_account) → 바로 Home
    ///   그 외에는 → Login (로그인 화면)
    /// 9주차까지는 Onboarding으로 갔으나, 10주차에서 로그인 화면을 진입점으로 도입.
    /// </summary>
    private void navigateToNextScreen() {
        AccountManager accountManager = ((App) getApplication()).getAccountManager();

        // 두 조건이 모두 참일 때만 자동 로그인 (이름을 붙여 의도를 분명히)
        boolean keepLogin = accountManager.isAutoLogin();
        boolean hasAccount = accountManager.hasCurrentAccount();
        boolean canAutoLogin = keepLogin && hasAccount;

        // 갈 화면을 먼저 고른다 (Home 또는 Login)
        Class<?> target = canAutoLogin ? MainActivity.class : LoginActivity.class;
        Intent intent = new Intent(this, target);

        // FLAG_ACTIVITY_NEW_TASK: 새 태스크에서 시작
        // FLAG_ACTIVITY_CLEAR_TASK: 기존 태스크(Splash 포함) 전부 제거
        // → 뒤로가기로 Splash에 돌아올 수 없게 함
        // Unity에서 로고 Scene을 LoadSceneMode.Single로 날리는 것과 동일
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }
}
