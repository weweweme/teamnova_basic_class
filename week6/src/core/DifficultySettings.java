package core;

/// <summary>
/// 난이도에 따른 게임 밸런스 수치를 관리하는 클래스
/// 적 수 배율, 보급 배율, 승리 일차를 제공
/// </summary>
public class DifficultySettings {

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

    /// <summary>
    /// 선택된 난이도
    /// </summary>
    private final Difficulty difficulty;

    /// <summary>
    /// 난이도에 맞는 밸런스 수치로 생성
    /// </summary>
    public DifficultySettings(Difficulty difficulty) {
        this.difficulty = difficulty;

        switch (difficulty) {
            case EASY:
                enemyMultiplier = 0.7;
                supplyMultiplier = 1.5;
                winDay = 7;
                break;
            case HARD:
                enemyMultiplier = 1.3;
                supplyMultiplier = 0.7;
                winDay = 15;
                break;
            default:
                enemyMultiplier = 1.0;
                supplyMultiplier = 1.0;
                winDay = 10;
                break;
        }
    }

    /// <summary>
    /// 선택된 난이도 반환
    /// </summary>
    public Difficulty getDifficulty() {
        return difficulty;
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
