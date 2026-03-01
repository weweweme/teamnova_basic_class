package structure;

/// <summary>
/// 바리케이드 앞에 설치하는 가시덫
/// 적이 이 열을 지나가면 피해를 받음
/// 내구도가 있어서 일정 횟수 사용하면 파괴됨
/// </summary>
public class Spike extends Structure {

    /// <summary>
    /// 최대 내구도 (적이 밟을 때마다 1씩 감소)
    /// </summary>
    private static final int MAX_HP = 10;

    /// <summary>
    /// 적이 밟을 때 주는 피해량
    /// </summary>
    private static final int SPIKE_DAMAGE = 3;

    /// <summary>
    /// 설치 비용
    /// </summary>
    public static final int COST = 20;

    /// <summary>
    /// 지정한 열에 가시덫 설치
    /// </summary>
    public Spike(int column) {
        super(column, MAX_HP);
    }


    /// <summary>
    /// 적이 밟을 때 주는 피해량 반환
    /// </summary>
    public int getSpikeDamage() {
        return SPIKE_DAMAGE;
    }
}
