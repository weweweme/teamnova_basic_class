package structure;

/// <summary>
/// 적에게 피해를 주는 구조물의 공통 상위 클래스
/// 설치 위치, 내구도(Structure), 피해량을 가짐
/// </summary>
public abstract class Trap extends Structure {

    /// <summary>
    /// 적에게 주는 피해량
    /// </summary>
    private int damage;

    /// <summary>
    /// 지정한 열에 함정 생성
    /// 서브클래스에서 setDamage()를 호출하여 피해량 설정
    /// </summary>
    protected Trap(int column) {
        super(column);
    }

    /// <summary>
    /// 피해량 설정 (서브클래스 생성자에서 호출)
    /// </summary>
    protected void setDamage(int damage) {
        this.damage = damage;
    }

    /// <summary>
    /// 적에게 주는 피해량 반환
    /// </summary>
    public int getDamage() {
        return damage;
    }
}
