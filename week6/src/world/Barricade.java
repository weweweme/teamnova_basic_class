package world;

/// <summary>
/// 안전지대와 전장을 나누는 세로 방벽
/// 적이 도달하면 공격 대상이 되며, 보급품으로 수리 가능
/// </summary>
public class Barricade extends Structure {

    /// <summary>
    /// 바리케이드가 위치한 열 (이 열과 다음 열에 ## 표시)
    /// </summary>
    public static final int COLUMN = 15;

    /// <summary>
    /// 최대 내구도
    /// </summary>
    private static final int MAX_HP = 100;

    /// <summary>
    /// 피격 깜빡임 지속 시간 (밀리초)
    /// </summary>
    private static final int FLASH_DURATION = 300;

    /// <summary>
    /// 마지막으로 피격당한 시각 (0이면 아직 안 맞음)
    /// </summary>
    private long lastHitTime;

    /// <summary>
    /// 마지막으로 수리한 시각 (0이면 아직 안 함)
    /// </summary>
    private long lastRepairTime;

    /// <summary>
    /// 최대 내구도로 바리케이드 생성
    /// </summary>
    public Barricade() {
        super(COLUMN, MAX_HP);
    }

    /// <summary>
    /// 피해를 받아 내구도 감소, 피격 시각 기록
    /// </summary>
    @Override
    public synchronized void takeDamage(int damage) {
        super.takeDamage(damage);
        lastHitTime = System.currentTimeMillis();
    }

    /// <summary>
    /// 수리하여 내구도 회복, 수리 시각 기록
    /// </summary>
    @Override
    public synchronized void repair(int amount) {
        super.repair(amount);
        lastRepairTime = System.currentTimeMillis();
    }

    /// <summary>
    /// 최근에 피격당했는지 확인 (깜빡임 표시용)
    /// </summary>
    public synchronized boolean isRecentlyHit() {
        return System.currentTimeMillis() - lastHitTime < FLASH_DURATION;
    }

    /// <summary>
    /// 최근에 수리했는지 확인 (깜빡임 표시용)
    /// </summary>
    public synchronized boolean isRecentlyRepaired() {
        return System.currentTimeMillis() - lastRepairTime < FLASH_DURATION;
    }
}
