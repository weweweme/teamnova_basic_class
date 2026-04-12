package com.example.week8;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.week8.databinding.ActivitySplashBinding;

/// <summary>
/// 스플래시 화면 (앱 진입점)
/// 1.5초간 로고를 보여준 뒤 OnboardingActivity로 이동
/// Unity로 비유하면 게임 시작 시 로고 Scene → Invoke("GoToNext", 1.5f)
///
/// ──── Lifecycle 학습 ────
/// onCreate: ViewBinding + Handler로 1.5초 후 이동 예약
/// onDestroy: Handler 콜백 제거 (메모리 누수 방지)
///   → 1.5초 안에 뒤로가기로 나가면, Activity는 파괴되는데
///     Handler는 살아있어서 존재하지 않는 Activity를 호출하려 함 → 크래시
///   → Unity에서 OnDestroy()에 CancelInvoke() 넣는 것과 같은 이유
///
/// ──── Intent 학습 ────
/// Intent Filter: Manifest에 MAIN + LAUNCHER 등록 (앱 아이콘으로 실행되는 진입점)
/// 명시적 Intent: OnboardingActivity로 이동
/// Flags: FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
///   → Splash를 백스택에서 아예 제거 (뒤로가기로 돌아올 수 없게)
///   → Unity에서 로고 Scene을 LoadSceneMode.Single로 날려버리는 것과 동일
///
/// TODO: 10주차에서 SharedPreferences 학습 후 온보딩 완료 여부 분기 추가
/// </summary>
public class SplashActivity extends AppCompatActivity {

    /// <summary>
    /// 스플래시 화면 유지 시간 (밀리초)
    /// Unity의 Invoke 지연 시간에 해당
    /// </summary>
    private static final int SPLASH_DELAY_MS = 1500;

    /// <summary>
    /// 지연 실행을 담당하는 핸들러
    /// Unity의 Invoke/CancelInvoke 시스템에 해당
    /// Looper.getMainLooper()는 "메인(UI) 스레드에서 실행하라"는 뜻
    /// Unity에서 코루틴이 메인 스레드에서 돌아가는 것과 같은 개념
    /// </summary>
    private final Handler handler = new Handler(Looper.getMainLooper());

    /// <summary>
    /// Handler에 등록할 지연 실행 작업
    /// 필드로 저장해야 onDestroy에서 removeCallbacks로 취소 가능
    /// Unity로 비유하면 CancelInvoke에 넘길 메서드 참조
    /// </summary>
    private final Runnable navigateRunnable = new Runnable() {
        @Override
        public void run() {
            navigateToNextScreen();
        }
    };

    // ========== Lifecycle ==========

    /// <summary>
    /// 스플래시 화면 생성
    /// ViewBinding으로 레이아웃 연결 후, 1.5초 뒤 다음 화면으로 이동 예약
    /// </summary>
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding으로 레이아웃 연결
        // Unity로 비유하면 Prefab을 Instantiate하고 참조를 잡는 것
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1.5초 후 다음 화면으로 이동 예약
        // Unity의 Invoke("NavigateToNextScreen", 1.5f)와 동일
        handler.postDelayed(navigateRunnable, SPLASH_DELAY_MS);
    }

    /// <summary>
    /// 스플래시 화면 파괴 시 Handler 콜백 제거
    /// 1.5초 안에 뒤로가기로 나갈 경우를 대비한 안전장치
    /// Unity에서 OnDestroy()에 CancelInvoke() 넣는 것과 같은 이유
    /// </summary>
    @Override
    protected void onDestroy() {
        // Handler에 예약된 콜백 제거 (메모리 누수 방지)
        handler.removeCallbacks(navigateRunnable);
        super.onDestroy();
    }

    // ========== 화면 이동 ==========

    /// <summary>
    /// OnboardingActivity로 이동
    /// TODO: 10주차에서 SharedPreferences 학습 후
    ///       온보딩 완료 여부에 따라 Main/Onboarding 분기 추가
    /// </summary>
    private void navigateToNextScreen() {
        // 명시적 Intent로 OnboardingActivity 이동
        Intent intent = new Intent(this, OnboardingActivity.class);

        // FLAG_ACTIVITY_NEW_TASK: 새 태스크에서 시작
        // FLAG_ACTIVITY_CLEAR_TASK: 기존 태스크(Splash 포함) 전부 제거
        // → 뒤로가기로 Splash에 돌아올 수 없게 함
        // Unity에서 로고 Scene을 LoadSceneMode.Single로 날리는 것과 동일
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
    }
}
