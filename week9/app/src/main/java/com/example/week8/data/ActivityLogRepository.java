package com.example.week8.data;

import com.example.week8.model.ActivityLog;
import com.example.week8.model.ActivityLogType;

import java.util.ArrayList;

/// <summary>
/// 활동 로그 저장소
/// 타임라인(TimelineActivity)에 표시할 더미 활동 로그를 제공
/// Unity로 비유하면 테스트용 로그 데이터를 하드코딩해둔 매니저
///
/// 현재(9주차)는 하드코딩 더미 데이터만 사용 (4종 타입을 섞어 멀티 뷰타입 시연용)
/// gameId는 GameRepository의 더미 게임 id(1~4)와 매칭됨
/// </summary>
public class ActivityLogRepository {

    /// <summary>
    /// 시간 계산용 상수 (밀리초 단위)
    /// 더미 로그의 timestamp를 "지금으로부터 n시간/일 전"으로 만들 때 사용
    /// </summary>
    private static final long HOUR_MS = 60L * 60L * 1000L;
    private static final long DAY_MS = 24L * HOUR_MS;

    /// <summary>
    /// 더미 활동 로그 목록 (최신순으로 하드코딩)
    /// </summary>
    private final ArrayList<ActivityLog> logs;

    // ========== 생성자 ==========

    /// <summary>
    /// 저장소 생성 및 더미 데이터 초기화
    /// 4종 타입(ADDED/COMPLETED/REVIEWED/PLAYED)을 섞어 10개 생성
    /// timestamp는 앱 실행 시점(now) 기준으로 과거로 거슬러 배치
    /// </summary>
    public ActivityLogRepository() {
        this.logs = new ArrayList<>();

        // 기준 시각: 앱(저장소)이 만들어진 현재 시각
        // 각 로그는 여기서 일정 시간 전으로 배치 (최신 → 과거 순)
        long now = System.currentTimeMillis();

        // gameId 매핑: 1=엘든링, 2=발더스게이트3, 3=할로우나이트, 4=셀레스테
        this.logs.add(new ActivityLog(
                ActivityLogType.PLAYED, 1, now - 2 * HOUR_MS, "2시간 30분"));
        this.logs.add(new ActivityLog(
                ActivityLogType.REVIEWED, 3, now - 5 * HOUR_MS,
                "인디 메트로배니아의 정점. 아트와 음악이 압도적"));
        this.logs.add(new ActivityLog(
                ActivityLogType.COMPLETED, 3, now - 6 * HOUR_MS, ""));
        this.logs.add(new ActivityLog(
                ActivityLogType.PLAYED, 2, now - 1 * DAY_MS, "4시간 10분"));
        this.logs.add(new ActivityLog(
                ActivityLogType.REVIEWED, 1, now - 2 * DAY_MS,
                "프롬소프트의 정점. 죽고 배우는 쾌감이 끝내준다"));
        this.logs.add(new ActivityLog(
                ActivityLogType.PLAYED, 1, now - 2 * DAY_MS - 3 * HOUR_MS, "1시간 45분"));
        this.logs.add(new ActivityLog(
                ActivityLogType.COMPLETED, 1, now - 3 * DAY_MS, ""));
        this.logs.add(new ActivityLog(
                ActivityLogType.ADDED, 4, now - 4 * DAY_MS, ""));
        this.logs.add(new ActivityLog(
                ActivityLogType.ADDED, 2, now - 5 * DAY_MS, ""));
        this.logs.add(new ActivityLog(
                ActivityLogType.ADDED, 1, now - 6 * DAY_MS, ""));
    }

    // ========== 조회 ==========

    /// <summary>
    /// 전체 활동 로그 목록 반환
    /// TimelineActivity에서 타임라인을 만들 때 사용
    /// </summary>
    public ArrayList<ActivityLog> getAllLogs() {
        return this.logs;
    }
}
