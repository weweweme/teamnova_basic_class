package structure;

/// <summary>
/// 플레이어가 비용을 지불하여 건설할 수 있는 구조물의 공통 상위 클래스
/// 설치 비용을 가지며, Structure의 위치/내구도를 상속받음
/// </summary>
public abstract class Buildable extends Structure {

    /// <summary>
    /// 설치 비용
    /// </summary>
    private final int cost;

    /// <summary>
    /// 지정한 열에 건설 가능 구조물 생성, 내구도와 비용 설정
    /// </summary>
    protected Buildable(int column, int maxHp, int cost) {
        super(column, maxHp);
        this.cost = cost;
    }

    /// <summary>
    /// 설치 비용 반환
    /// </summary>
    public int getCost() {
        return cost;
    }
}
