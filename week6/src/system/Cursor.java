package system;

/// <summary>
/// 맵 위를 이동하는 선택 커서
/// 화살표 키로 이동하며, 정착민 명령/건물 건설 등에 사용
/// </summary>
public class Cursor {

    /// <summary>
    /// 커서의 현재 위치
    /// </summary>
    private final Position position;

    /// <summary>
    /// 맵 중앙에서 시작하는 커서 생성
    /// </summary>
    public Cursor() {
        int startRow = GameMap.HEIGHT / 2;
        int startCol = GameMap.WIDTH / 2;
        this.position = new Position(startRow, startCol);
    }

    /// <summary>
    /// 커서 위치 반환
    /// </summary>
    public Position getPosition() {
        return position;
    }

    /// <summary>
    /// 화살표 키 입력에 따라 커서 이동
    /// 맵 범위를 벗어나지 않도록 제한
    /// </summary>
    public void move(int key) {
        int newRow = position.getRow();
        int newCol = position.getCol();

        switch (key) {
            case Util.KEY_UP:
                newRow--;
                break;
            case Util.KEY_DOWN:
                newRow++;
                break;
            case Util.KEY_LEFT:
                newCol--;
                break;
            case Util.KEY_RIGHT:
                newCol++;
                break;
        }

        // 커서가 맵 안에 있는지 확인
        boolean rowInBounds = newRow >= 0 && newRow < GameMap.HEIGHT;
        boolean colInBounds = newCol >= 0 && newCol < GameMap.WIDTH;

        if (rowInBounds && colInBounds) {
            position.moveTo(newRow, newCol);
        }
    }
}
