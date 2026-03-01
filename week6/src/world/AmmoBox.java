package world;

/// <summary>
/// 전장에 설치하는 탄약 보급 구조물
/// 살아있는 동안 모든 정착민의 공격 속도를 증가시킴
/// 적이 지나가면 피해를 입어 파괴될 수 있음
/// </summary>
public class AmmoBox extends Structure {

    /// <summary>
    /// 최대 내구도
    /// </summary>
    private static final int MAX_HP = 20;

    /// <summary>
    /// 발사 간격 배율 (0.7 = 30% 빨라짐)
    /// </summary>
    private static final double FIRE_RATE_MULTIPLIER = 0.7;

    /// <summary>
    /// 설치 비용
    /// </summary>
    private static final int COST = 20;

    /// <summary>
    /// 적이 지나갈 때 받는 피해
    /// </summary>
    private static final int TRAMPLE_DAMAGE = 3;

    /// <summary>
    /// 지정한 열에 탄약 상자 설치
    /// </summary>
    public AmmoBox(int column) {
        super(column, MAX_HP);
    }

    /// <summary>
    /// 설치 비용 반환
    /// </summary>
    public static int getCost() {
        return COST;
    }

    /// <summary>
    /// 발사 간격 배율 반환
    /// </summary>
    public static double getFireRateMultiplier() {
        return FIRE_RATE_MULTIPLIER;
    }

    /// <summary>
    /// 적에게 밟혔을 때 받는 피해량 반환
    /// </summary>
    public int getTrampleDamage() {
        return TRAMPLE_DAMAGE;
    }
}
