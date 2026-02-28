package system;

/// <summary>
/// 적의 종류를 정의하는 열거형
/// 각 종류마다 이름, 체력, 공격력, 이동 속도, 아스키아트가 다름
/// 일반(6x3), 강한(8x4), 보스(10~12x5) 세 등급으로 나뉨
/// </summary>
public enum EnemyType {

    // ── 일반 (6x3) ──

    /// <summary>
    /// 늑대 — 보통 속도, 보통 체력
    /// </summary>
    WOLF("늑대", 30, 3, 400, new String[]{
        " /\\/\\ ",
        " (oo) ",
        "  \\/  "
    }),

    /// <summary>
    /// 거미 — 빠르지만 약함
    /// </summary>
    SPIDER("거미", 20, 2, 250, new String[]{
        "\\(oo)/",
        " \\\\// ",
        "/(  )\\"
    }),

    /// <summary>
    /// 해골 — 보통 속도, 보통 체력
    /// </summary>
    SKELETON("해골", 25, 3, 350, new String[]{
        " (^^) ",
        " /||\\ ",
        "  /\\  "
    }),

    /// <summary>
    /// 좀비 — 느리지만 약간 단단함
    /// </summary>
    ZOMBIE("좀비", 35, 4, 500, new String[]{
        " (00) ",
        " /||\\ ",
        " _/\\_ "
    }),

    /// <summary>
    /// 쥐 — 매우 빠르지만 매우 약함
    /// </summary>
    RAT("쥐", 15, 1, 200, new String[]{
        "/\\_/| ",
        "(o.o) ",
        " \\_|  "
    }),

    /// <summary>
    /// 슬라임 — 느리고 약하지만 체력이 높음
    /// </summary>
    SLIME("슬라임", 40, 2, 450, new String[]{
        " .--. ",
        "(o  o)",
        " '--' "
    }),

    // ── 강한 (8x4) ──

    /// <summary>
    /// 곰 — 느리지만 체력과 공격력이 높음
    /// </summary>
    BEAR("곰", 80, 8, 600, new String[]{
        " /\\  /\\ ",
        "( O  O )",
        " |VVVV| ",
        " /____\\ "
    }),

    /// <summary>
    /// 도적 — 강한 몬스터 중 가장 빠름
    /// </summary>
    BANDIT("도적", 60, 6, 350, new String[]{
        " _/~~\\_ ",
        "|(-_-)| ",
        "/|    |\\",
        " /\\  /\\ "
    }),

    /// <summary>
    /// 전갈 — 보통 속도의 강한 몬스터
    /// </summary>
    SCORPION("전갈", 70, 7, 400, new String[]{
        "___  /\\ ",
        "<(oo)/_>",
        " /|| \\/ ",
        " /||\\ \\ "
    }),

    /// <summary>
    /// 오크 — 강한 몬스터 중 가장 단단함
    /// </summary>
    ORC("오크", 90, 9, 500, new String[]{
        " /~~~~\\ ",
        "|(o  o)|",
        "-|/VV\\|-",
        " /|  |\\ "
    }),

    // ── 보스 (12x5 / 10x5) ──

    /// <summary>
    /// 드래곤 — 빠르고 강한 보스 (12x5)
    /// </summary>
    DRAGON("드래곤", 200, 15, 300, new String[]{
        "  /\\    /\\  ",
        " / O\\__/O \\ ",
        "<   VVVV   >",
        " \\ /\\  /\\ / ",
        "  V  \\/  V  "
    }),

    /// <summary>
    /// 골렘 — 매우 단단하고 느린 보스 (10x5)
    /// </summary>
    GOLEM("골렘", 300, 10, 700, new String[]{
        " .------. ",
        "| O    O |",
        "|==|==|==|",
        "|  |  |  |",
        "|__|__|__|"
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
