package com.example.week11.model;

/// <summary>
/// 휴지통에 들어간 게임 하나 + "언제 버려졌는지" 시각
/// 30일 자동 삭제(오래된 것 정리)와 "N일 후 삭제" 표시에 이 시각을 사용
/// </summary>
public class TrashEntry {

    /// <summary>
    /// 버려진 게임
    /// </summary>
    private final Game game;

    /// <summary>
    /// 휴지통에 넣은 시각 (1970년 기준 밀리초 = System.currentTimeMillis 값)
    /// </summary>
    private final long trashedAt;

    /// <summary>
    /// 생성 (게임 + 버린 시각)
    /// </summary>
    public TrashEntry(Game game, long trashedAt) {
        this.game = game;
        this.trashedAt = trashedAt;
    }

    /// <summary>
    /// 버려진 게임 반환
    /// </summary>
    public Game getGame() {
        return game;
    }

    /// <summary>
    /// 휴지통에 넣은 시각(ms) 반환
    /// </summary>
    public long getTrashedAt() {
        return trashedAt;
    }
}
