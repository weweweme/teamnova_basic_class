package game;

/// <summary>
/// 게임 난이도 종류
/// </summary>
public enum Difficulty {

    /// <summary>
    /// 쉬움
    /// </summary>
    EASY("쉬움"),

    /// <summary>
    /// 보통
    /// </summary>
    NORMAL("보통"),

    /// <summary>
    /// 어려움
    /// </summary>
    HARD("어려움");

    /// <summary>
    /// 화면에 표시할 난이도 이름
    /// </summary>
    private final String displayName;

    Difficulty(String displayName) {
        this.displayName = displayName;
    }

    /// <summary>
    /// 난이도 이름 반환
    /// </summary>
    public String getDisplayName() {
        return displayName;
    }
}
