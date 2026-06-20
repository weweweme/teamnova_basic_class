package com.example.week8;

import android.app.Application;

import com.example.week8.data.ActivityLogRepository;
import com.example.week8.data.GameRepository;

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
    /// 앱 프로세스 시작 시 단 한 번 호출
    /// 여기서 만든 객체들은 앱이 살아있는 동안 계속 같은 인스턴스로 유지됨
    /// </summary>
    @Override
    public void onCreate() {
        super.onCreate();
        gameRepository = new GameRepository();
        activityLogRepository = new ActivityLogRepository();
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
}
