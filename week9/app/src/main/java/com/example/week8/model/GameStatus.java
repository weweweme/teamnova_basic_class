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
    // 두 번째 값은 상태 배지 색상 (ARGB 정수)
    // 파랑=진행중, 초록=완료, 회색=중단, 주황=백로그 — 의미가 직관적으로 연상되는 색
    PLAYING("플레이중", 0xFF1565C0),
    COMPLETED("완료", 0xFF2E7D32),
    DROPPED("중단", 0xFF757575),
    BACKLOG("찜 목록", 0xFFEF6C00);

    /// <summary>
    /// 화면에 표시할 한국어 이름
    /// </summary>
    private final String displayName;

    /// <summary>
    /// 상태 배지 배경색 (ARGB 정수)
    /// 안드로이드 color 리소스(R.color.*) 대신 순수 정수로 보관 → 모델이 R에 의존하지 않음
    /// 사용: View.setBackgroundTintList(ColorStateList.valueOf(status.getColorArgb()))
    /// </summary>
    private final int colorArgb;

    GameStatus(String displayName, int colorArgb) {
        this.displayName = displayName;
        this.colorArgb = colorArgb;
    }

    /// <summary>
    /// 화면 표시용 한국어 이름 반환
    /// </summary>
    public String getDisplayName() {
        return this.displayName;
    }

    /// <summary>
    /// 상태 배지 색상(ARGB 정수) 반환
    /// </summary>
    public int getColorArgb() {
        return this.colorArgb;
    }
}
