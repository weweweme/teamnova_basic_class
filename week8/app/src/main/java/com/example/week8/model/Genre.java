package com.example.week8.model;

/// <summary>
/// 게임 장르 열거형
/// 일반적인 게임 분류 기준 10종 + 기타(ETC)
/// Unity로 비유하면 Inspector의 드롭다운에 표시할 고정 선택지 목록
/// </summary>
public enum Genre {
    ACTION("액션"),
    ADVENTURE("어드벤처"),
    RPG("RPG"),
    STRATEGY("전략"),
    SIMULATION("시뮬레이션"),
    SPORTS("스포츠"),
    RACING("레이싱"),
    PUZZLE("퍼즐"),
    HORROR("호러"),
    PLATFORMER("플랫포머"),
    ETC("기타");

    /// <summary>
    /// 화면에 표시할 한국어 이름
    /// </summary>
    private final String displayName;

    Genre(String displayName) {
        this.displayName = displayName;
    }

    /// <summary>
    /// 화면 표시용 한국어 이름 반환
    /// </summary>
    public String getDisplayName() {
        return this.displayName;
    }
}
