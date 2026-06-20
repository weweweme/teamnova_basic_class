package com.example.week8.model;

/// <summary>
/// 게임 진행 상태 열거형
/// 내 컬렉션에서 각 게임이 어떤 단계인지 분류
/// LibraryActivity의 상태별 필터 탭에서 사용
///
/// 종류가 4종(5개 이하) + 필드도 적어서 enum에 데이터를 직접 포함
/// "전체"는 상태가 아니라 필터 옵션이라 여기에 넣지 않고 탭에서 별도 처리
/// </summary>
public enum GameStatus {
    PLAYING("플레이중"),
    COMPLETED("완료"),
    DROPPED("중단"),
    BACKLOG("백로그");

    /// <summary>
    /// 화면에 표시할 한국어 이름
    /// </summary>
    private final String displayName;

    GameStatus(String displayName) {
        this.displayName = displayName;
    }

    /// <summary>
    /// 화면 표시용 한국어 이름 반환
    /// </summary>
    public String getDisplayName() {
        return this.displayName;
    }
}
