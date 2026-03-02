package structure;

/// <summary>
/// 안전지대에 설치하는 탄약 보급 구조물
/// 설치되어 있는 동안 모든 정착민의 공격 속도를 증가시킴
/// </summary>
public class AmmoBox extends Buildable {

    /// <summary>
    /// 지정한 위치에 탄약 상자 설치 (내구도 1, 비용 20)
    /// </summary>
    public AmmoBox(int row, int column) {
        super(row, column, 1, 20);
    }

    /// <summary>
    /// 발사 간격 배율 반환
    /// </summary>
    public double getFireRateMultiplier() {
        return 0.7;
    }
}
