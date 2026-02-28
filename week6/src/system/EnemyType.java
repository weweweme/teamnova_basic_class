package system;

/// <summary>
/// 적의 종류를 정의하는 열거형
/// 각 종류마다 이름, 체력, 공격력, 이동 속도, 아스키아트가 다름
/// </summary>
public enum EnemyType {

    /// <summary>
    /// 늑대 — 보통 속도, 보통 체력 (6x3)
    /// </summary>
    WOLF("늑대", 30, 3, 400, new String[]{
        " /\\/\\ ",
        " (oo) ",
        "  \\/  "
    }),

    /// <summary>
    /// 거미 — 빠르지만 약함 (6x3)
    /// </summary>
    SPIDER("거미", 20, 2, 250, new String[]{
        "\\(oo)/",
        " \\\\// ",
        "/(  )\\"
    }),

    /// <summary>
    /// 곰 — 느리지만 체력과 공격력이 높음 (8x4)
    /// </summary>
    BEAR("곰", 80, 8, 600, new String[]{
        " /\\  /\\ ",
        "( O  O )",
        " |VVVV| ",
        " /____\\ "
    });

    /// <summary>
    /// 화면에 표시할 이름
    /// </summary>
    private final String displayName;

    /// <summary>
    /// 최대 체력
    /// </summary>
    private final int maxHp;

    /// <summary>
    /// 틱당 공격력
    /// </summary>
    private final int damage;

    /// <summary>
    /// 행동 틱 간격 (밀리초, 작을수록 빠름)
    /// </summary>
    private final int tickDelay;

    /// <summary>
    /// 화면에 그릴 아스키아트 블록
    /// </summary>
    private final String[] block;

    EnemyType(String displayName, int maxHp, int damage, int tickDelay, String[] block) {
        this.displayName = displayName;
        this.maxHp = maxHp;
        this.damage = damage;
        this.tickDelay = tickDelay;
        this.block = block;
    }

    /// <summary>
    /// 표시 이름 반환
    /// </summary>
    public String getDisplayName() {
        return displayName;
    }

    /// <summary>
    /// 최대 체력 반환
    /// </summary>
    public int getMaxHp() {
        return maxHp;
    }

    /// <summary>
    /// 공격력 반환
    /// </summary>
    public int getDamage() {
        return damage;
    }

    /// <summary>
    /// 틱 간격 반환
    /// </summary>
    public int getTickDelay() {
        return tickDelay;
    }

    /// <summary>
    /// 아스키아트 블록 반환
    /// </summary>
    public String[] getBlock() {
        return block;
    }
}
