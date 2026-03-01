package structure;

/// <summary>
/// 적에게 피해를 주는 구조물의 공통 상위 클래스
/// 설치 위치, 내구도(Structure), 피해량을 가짐
/// </summary>
public abstract class Trap extends Structure {

    /// <summary>
    /// 적에게 주는 피해량
    /// </summary>
    private final int damage;

    /// <summary>
    /// 지정한 열에 함정 생성, 내구도와 피해량 설정
    /// </summary>
    protected Trap(int column, int maxHp, int damage) {
        super(column, maxHp);
        this.damage = damage;
    }

    /// <summary>
    /// 적에게 주는 피해량 반환
    /// </summary>
    public int getDamage() {
        return damage;
    }
}
