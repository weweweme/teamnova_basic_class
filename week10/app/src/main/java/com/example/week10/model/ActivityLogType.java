package com.example.week10.model;

/// <summary>
/// 활동 로그 종류 열거형
/// 타임라인(TimelineActivity)에 표시할 활동 4종
///
/// 각 종류는 화면에서 서로 다른 레이아웃(뷰타입)으로 그려짐:
///   ADDED     → 게임을 라이브러리에 추가
///   COMPLETED → 게임 완료
///   REVIEWED  → 리뷰 작성 (본문 + 별점)
///   PLAYED    → 플레이 세션 기록 (플레이 시간)
///
/// 종류가 4종(5개 이하) + 필드도 적어서 enum에 데이터를 직접 포함
/// </summary>
public enum ActivityLogType {
    ADDED("추가함"),
    COMPLETED("완료함"),
    REVIEWED("리뷰함"),
    PLAYED("플레이함");

    /// <summary>
    /// 화면에 표시할 한국어 이름
    /// </summary>
    private final String displayName;

    ActivityLogType(String displayName) {
        this.displayName = displayName;
    }

    /// <summary>
    /// 화면 표시용 한국어 이름 반환
    /// </summary>
    public String getDisplayName() {
        return this.displayName;
    }
}
