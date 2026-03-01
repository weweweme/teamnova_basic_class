package entity.colonist;

/// <summary>
/// 정착민 유형을 정의하는 열거형
/// 유형마다 패시브 능력이 다름 (속사/치명타/넉백)
/// 전투 능력은 장착한 무기(Gun)가 결정
/// </summary>
public enum ColonistType {

    /// <summary>
    /// 사격수 — 패시브: 속사 (발사 간격 20% 감소)
    /// </summary>
    GUNNER("사격수", 100, 0.8, 0.0, 0),

    /// <summary>
    /// 저격수 — 패시브: 치명타 (30% 확률로 2배 데미지)
    /// </summary>
    SNIPER("저격수", 100, 1.0, 0.3, 0),

    /// <summary>
    /// 돌격수 — 패시브: 넉백 (명중 시 적 1칸 밀어냄)
    /// </summary>
    ASSAULT("돌격수", 100, 1.0, 0.0, 1);

    /// <summary>
    /// 화면에 표시할 유형 이름
    /// </summary>
    private final String displayName;

    /// <summary>
    /// 최대 체력
    /// </summary>
    private final int maxHp;

    /// <summary>
    /// 발사 간격 배율 (1.0 = 보통, 0.8 = 20% 빠름)
    /// </summary>
    private final double fireRateBonus;

    /// <summary>
    /// 치명타 확률 (0.0 ~ 1.0, 발동 시 데미지 2배)
    /// </summary>
    private final double critChance;

    /// <summary>
    /// 넉백 거리 (명중 시 적을 밀어내는 칸 수, 0이면 없음)
    /// </summary>
    private final int knockback;

    ColonistType(String displayName, int maxHp, double fireRateBonus, double critChance, int knockback) {
        this.displayName = displayName;
        this.maxHp = maxHp;
        this.fireRateBonus = fireRateBonus;
        this.critChance = critChance;
        this.knockback = knockback;
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
    /// 발사 간격 배율 반환
    /// </summary>
    public double getFireRateBonus() {
        return fireRateBonus;
    }

    /// <summary>
    /// 치명타 확률 반환
    /// </summary>
    public double getCritChance() {
        return critChance;
    }

    /// <summary>
    /// 넉백 거리 반환
    /// </summary>
    public int getKnockback() {
        return knockback;
    }
}
