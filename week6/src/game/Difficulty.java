package game;

/// <summary>
/// 게임 난이도 종류
/// 난이도별 밸런스 수치(적 배율, 보급 배율, 승리 일차)를 포함
/// </summary>
public enum Difficulty {

    /// <summary>
    /// 쉬움
    /// </summary>
    EASY("쉬움", 0.7, 1.5, 7),

    /// <summary>
    /// 보통
    /// </summary>
    NORMAL("보통", 1.0, 1.0, 10),

    /// <summary>
    /// 어려움
    /// </summary>
    HARD("어려움", 1.3, 0.7, 15);

    /// <summary>
    /// 화면에 표시할 난이도 이름
    /// </summary>
    private final String displayName;

    /// <summary>
    /// 적 수 배율 (1.0이 기본)
    /// </summary>
    private final double enemyMultiplier;

    /// <summary>
    /// 보급 배율 (1.0이 기본)
    /// </summary>
    private final double supplyMultiplier;

    /// <summary>
    /// 승리에 필요한 생존 일차
    /// </summary>
    private final int winDay;

    Difficulty(String displayName, double enemyMultiplier, double supplyMultiplier, int winDay) {
        this.displayName = displayName;
        this.enemyMultiplier = enemyMultiplier;
        this.supplyMultiplier = supplyMultiplier;
        this.winDay = winDay;
    }

    /// <summary>
    /// 난이도 이름 반환
    /// </summary>
    public String getDisplayName() {
        return displayName;
    }

    /// <summary>
    /// 적 수에 배율을 적용한 값 반환 (최소 1)
    /// </summary>
    public int applyEnemyCount(int baseCount) {
        return Math.max(1, (int) (baseCount * enemyMultiplier));
    }

    /// <summary>
    /// 보급량에 배율을 적용한 값 반환 (최소 1)
    /// </summary>
    public int applySupply(int baseAmount) {
        return Math.max(1, (int) (baseAmount * supplyMultiplier));
    }

    /// <summary>
    /// 승리 일차 반환
    /// </summary>
    public int getWinDay() {
        return winDay;
    }
}
