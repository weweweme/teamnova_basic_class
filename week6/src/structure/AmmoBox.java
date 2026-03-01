package structure;

/// <summary>
/// 안전지대에 설치하는 탄약 보급 구조물
/// 설치되어 있는 동안 모든 정착민의 공격 속도를 증가시킴
/// </summary>
public class AmmoBox extends Structure {

    /// <summary>
    /// 최대 내구도 (안전지대에 있으므로 파괴되지 않지만 구조상 필요)
    /// </summary>
    // super() 호출에 필요하여 static 유지
    private static final int MAX_HP = 1;

    /// <summary>
    /// 발사 간격 배율 (0.7 = 30% 빨라짐)
    /// </summary>
    private final double FIRE_RATE_MULTIPLIER = 0.7;

    /// <summary>
    /// 설치 비용
    /// </summary>
    public static final int COST = 20;

    /// <summary>
    /// 지정한 열에 탄약 상자 설치
    /// </summary>
    public AmmoBox(int column) {
        super(column, MAX_HP);
    }

    /// <summary>
    /// 발사 간격 배율 반환
    /// </summary>
    public double getFireRateMultiplier() {
        return FIRE_RATE_MULTIPLIER;
    }
}
