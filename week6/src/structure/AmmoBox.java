package structure;

/// <summary>
/// 안전지대에 설치하는 탄약 보급 구조물
/// 설치되어 있는 동안 모든 정착민의 공격 속도를 증가시킴
/// </summary>
public class AmmoBox extends Structure {

    /// <summary>
    /// 설치 비용
    /// </summary>
    public static final int COST = 20;

    /// <summary>
    /// 지정한 열에 탄약 상자 설치 (내구도 1)
    /// </summary>
    public AmmoBox(int column) {
        super(column, 1);
    }

    /// <summary>
    /// 발사 간격 배율 반환
    /// </summary>
    public double getFireRateMultiplier() {
        return 0.7;
    }
}
