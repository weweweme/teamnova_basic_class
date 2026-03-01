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
    /// 최대 내구도로 바리케이드 생성
    /// </summary>
    public Barricade() {
        super(COLUMN, MAX_HP);
    }
}
