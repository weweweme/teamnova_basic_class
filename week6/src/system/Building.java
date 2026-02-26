package system;

/// <summary>
/// 맵 위에 건설된 건물 하나를 나타내는 클래스
/// 4x2 블록으로 렌더링되며, 종류에 따라 다른 효과를 가짐
/// </summary>
public class Building {

    /// <summary>
    /// 맵 위의 위치 (블록 좌상단 기준)
    /// </summary>
    private final Position position;

    /// <summary>
    /// 건물 종류
    /// </summary>
    private final BuildingType type;

    /// <summary>
    /// 지정한 위치와 종류로 건물 생성
    /// </summary>
    public Building(Position position, BuildingType type) {
        this.position = position;
        this.type = type;
    }

    /// <summary>
    /// 위치 반환
    /// </summary>
    public Position getPosition() {
        return position;
    }

    /// <summary>
    /// 건물 종류 반환
    /// </summary>
    public BuildingType getType() {
        return type;
    }
}
