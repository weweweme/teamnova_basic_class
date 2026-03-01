package entity.colonist;

/// <summary>
/// 정착민 한 유형의 속성 데이터를 담는 클래스
/// 이름, 체력, 패시브 능력 등
/// ColonistFactory에서 생성하여 Colonist에 전달
/// </summary>
public class ColonistSpec {

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

    /// <summary>
    /// 모든 속성을 지정하여 생성
    /// </summary>
    public ColonistSpec(String displayName, int maxHp, double fireRateBonus, double critChance, int knockback) {
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
