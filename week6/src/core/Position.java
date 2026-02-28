package core;

/// <summary>
/// 맵 위의 행/열 좌표를 담는 객체
/// 정착민, 적, 건물 등 모든 위치 표현에 사용
/// 인스턴스를 재사용하며, 값 변경 가능
/// </summary>
public class Position {

    /// <summary>
    /// 행 (세로 위치)
    /// </summary>
    private int row;

    /// <summary>
    /// 열 (가로 위치)
    /// </summary>
    private int col;

    /// <summary>
    /// 지정한 행/열 좌표로 생성
    /// </summary>
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /// <summary>
    /// 행 반환
    /// </summary>
    public int getRow() {
        return row;
    }

    /// <summary>
    /// 열 반환
    /// </summary>
    public int getCol() {
        return col;
    }

    /// <summary>
    /// 행 변경
    /// </summary>
    public void setRow(int row) {
        this.row = row;
    }

    /// <summary>
    /// 열 변경
    /// </summary>
    public void setCol(int col) {
        this.col = col;
    }

    /// <summary>
    /// 행과 열을 한번에 변경
    /// </summary>
    public void moveTo(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /// <summary>
    /// 다른 좌표와 같은 위치인지 비교
    /// </summary>
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Position)) {
            return false;
        }
        Position other = (Position) obj;
        boolean sameRow = this.row == other.row;
        boolean sameCol = this.col == other.col;
        return sameRow && sameCol;
    }

    /// <summary>
    /// equals와 쌍으로 구현 (같은 좌표면 같은 해시값)
    /// </summary>
    public int hashCode() {
        return row * 31 + col;
    }

    /// <summary>
    /// 다른 좌표까지의 맨해튼 거리 (격자 이동 거리)
    /// 행 차이의 절대값 + 열 차이의 절대값
    /// </summary>
    public int distanceTo(Position other) {
        int rowDiff = Math.abs(this.row - other.row);
        int colDiff = Math.abs(this.col - other.col);
        return rowDiff + colDiff;
    }
}
