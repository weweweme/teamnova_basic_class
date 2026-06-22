package com.example.week10.library;

/// <summary>
/// 보관함 게임 정렬 기준
/// FAB(정렬 버튼) → 다이얼로그에서 선택하는 정렬 옵션 4종
///
/// 종류가 적고(4종) 필드도 displayName 하나뿐이라 enum에 직접 포함
/// 실제 정렬 로직(Comparator)은 LibraryActivity에서 이 값으로 분기
/// (Comparator를 enum에 넣으면 무거워지고 Game 모델 의존이 생겨 분리)
/// </summary>
public enum GameSortOrder {
    RECENT("최근 추가순"),
    NAME("이름순"),
    RATING_HIGH("별점 높은순"),
    RATING_LOW("별점 낮은순");

    /// <summary>
    /// 다이얼로그에 표시할 한국어 이름
    /// </summary>
    private final String displayName;

    GameSortOrder(String displayName) {
        this.displayName = displayName;
    }

    /// <summary>
    /// 표시용 이름 반환
    /// </summary>
    public String getDisplayName() {
        return this.displayName;
    }
}
