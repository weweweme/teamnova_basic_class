package structure;

/// <summary>
/// 바리케이드 앞에 설치하는 가시덫
/// 적이 이 열을 지나가면 피해를 받음
/// 내구도가 있어서 일정 횟수 사용하면 파괴됨
/// </summary>
public class Spike extends Trap {

    /// <summary>
    /// 설치 비용
    /// </summary>
    public static final int COST = 20;

    /// <summary>
    /// 지정한 열에 가시덫 설치 (내구도 10, 피해량 3)
    /// </summary>
    public Spike(int column) {
        super(column, 10, 3);
    }
}
