package entity.enemy;

/// <summary>
/// 적 한 종류의 속성 데이터를 담는 클래스
/// 이름, 체력, 공격력, 이동 속도, 보상, 특성, 외형 등
/// EnemyFactory에서 생성하여 Enemy에 전달
/// </summary>
public class EnemySpec {

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
    /// 처치 시 보급품 보상
    /// </summary>
    private final int reward;

    /// <summary>
    /// 행동 특성 (돌진, 방어, 재생 등)
    /// </summary>
    private final EnemyTrait trait;

    /// <summary>
    /// 화면에 그릴 아스키아트 블록
    /// </summary>
    private final String[] block;

    /// <summary>
    /// 모든 속성을 지정하여 생성
    /// </summary>
    public EnemySpec(String displayName, int maxHp, int damage, int tickDelay, int reward, EnemyTrait trait, String[] block) {
        this.displayName = displayName;
        this.maxHp = maxHp;
        this.damage = damage;
        this.tickDelay = tickDelay;
        this.reward = reward;
        this.trait = trait;
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
    /// 처치 보상 반환
    /// </summary>
    public int getReward() {
        return reward;
    }

    /// <summary>
    /// 행동 특성 반환
    /// </summary>
    public EnemyTrait getTrait() {
        return trait;
    }

    /// <summary>
    /// 아스키아트 블록 반환
    /// </summary>
    public String[] getBlock() {
        return block;
    }
}
