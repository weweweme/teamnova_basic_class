package entity.colonist;

/// <summary>
/// 정착민 유형을 정의하는 열거형
/// 유형마다 최대 체력, 발사 간격, 기본 피해량이 다름
/// </summary>
public enum ColonistType {

    /// <summary>
    /// 사격수 — 균형형 (체력 100, 발사간격 4틱, 피해 5)
    /// </summary>
    GUNNER("사격수", 100, 4, 5),

    /// <summary>
    /// 저격수 — 느리지만 강함 (체력 80, 발사간격 6틱, 피해 10)
    /// </summary>
    SNIPER("저격수", 80, 6, 10),

    /// <summary>
    /// 돌격수 — 빠르고 단단함 (체력 120, 발사간격 3틱, 피해 3)
    /// </summary>
    ASSAULT("돌격수", 120, 3, 3);

    /// <summary>
    /// 화면에 표시할 유형 이름
    /// </summary>
    private final String displayName;

    /// <summary>
    /// 최대 체력
    /// </summary>
    private final int maxHp;

    /// <summary>
    /// 발사 간격 (틱 수, 1틱 = 500ms)
    /// </summary>
    private final int shootInterval;

    /// <summary>
    /// 무기 레벨 1당 기본 피해량
    /// </summary>
    private final int baseDamage;

    ColonistType(String displayName, int maxHp, int shootInterval, int baseDamage) {
        this.displayName = displayName;
        this.maxHp = maxHp;
        this.shootInterval = shootInterval;
        this.baseDamage = baseDamage;
    }

    /// <summary>
    /// 유형 이름 반환
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
    /// 발사 간격 반환 (틱 수)
    /// </summary>
    public int getShootInterval() {
        return shootInterval;
    }

    /// <summary>
    /// 기본 피해량 반환
    /// </summary>
    public int getBaseDamage() {
        return baseDamage;
    }
}
