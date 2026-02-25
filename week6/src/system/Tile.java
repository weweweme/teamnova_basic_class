package system;

/// <summary>
/// 맵의 한 칸을 나타내는 클래스
/// 이동 가능 여부만 관리하며, 자원/건물/개체는 레이어에서 처리
/// </summary>
public class Tile {

    /// <summary>
    /// 이 칸을 밟을 수 있는지 여부
    /// </summary>
    private boolean walkable;

    /// <summary>
    /// 지정한 이동 가능 여부로 타일 생성
    /// </summary>
    public Tile(boolean walkable) {
        this.walkable = walkable;
    }

    /// <summary>
    /// 이 칸을 밟을 수 있는지 반환
    /// </summary>
    public boolean isWalkable() {
        return walkable;
    }

    /// <summary>
    /// 이동 가능 여부 변경
    /// 건물 건설 등으로 밟을 수 없게 되는 경우 사용
    /// </summary>
    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }
}
