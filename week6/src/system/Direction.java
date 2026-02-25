package system;

/// <summary>
/// 8방향 이동을 나타내는 열거형
/// 정착민, 적, 카메라 등 모든 이동에 사용
/// 각 방향은 행/열 변화량을 가짐
/// </summary>
public enum Direction {

    UP(-1, 0),
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1),
    UP_LEFT(-1, -1),
    UP_RIGHT(-1, 1),
    DOWN_LEFT(1, -1),
    DOWN_RIGHT(1, 1);

    /// <summary>
    /// 행 변화량 (위쪽이 음수, 아래쪽이 양수)
    /// </summary>
    private final int deltaRow;

    /// <summary>
    /// 열 변화량 (왼쪽이 음수, 오른쪽이 양수)
    /// </summary>
    private final int deltaCol;

    Direction(int deltaRow, int deltaCol) {
        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;
    }

    /// <summary>
    /// 행 변화량 반환
    /// </summary>
    public int getDeltaRow() {
        return deltaRow;
    }

    /// <summary>
    /// 열 변화량 반환
    /// </summary>
    public int getDeltaCol() {
        return deltaCol;
    }

    /// <summary>
    /// 주어진 좌표에 이 방향을 적용하여 이동시킴
    /// Position 인스턴스의 값을 직접 변경
    /// </summary>
    public void apply(Position position) {
        int newRow = position.getRow() + deltaRow;
        int newCol = position.getCol() + deltaCol;
        position.moveTo(newRow, newCol);
    }
}
