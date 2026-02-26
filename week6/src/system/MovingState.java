package system;

/// <summary>
/// 이동 상태 — 목표 지점을 향해 한 칸씩 이동
/// 도착하면 대기 상태로 전환
/// </summary>
public class MovingState extends ColonistState {

    /// <summary>
    /// 이동 목표 위치
    /// </summary>
    private final Position target;

    /// <summary>
    /// 지정한 목표 위치로 이동 상태 생성
    /// </summary>
    public MovingState(Position target) {
        this.target = target;
    }

    @Override
    public void enter(Colonist colonist) {
        // 이동 시작 시 별도 초기화 없음
    }

    @Override
    public void update(Colonist colonist) {
        Position current = colonist.getPosition();

        // 목표에 도착했으면 대기 상태로 전환
        int rowDiff = target.getRow() - current.getRow();
        int colDiff = target.getCol() - current.getCol();

        boolean arrived = rowDiff == 0 && colDiff == 0;
        if (arrived) {
            colonist.changeState(new IdleState());
            return;
        }

        // 목표를 향해 행/열 각각 1칸씩 이동
        int nextRow = current.getRow();
        int nextCol = current.getCol();

        if (rowDiff > 0) {
            nextRow++;
        } else if (rowDiff < 0) {
            nextRow--;
        }

        if (colDiff > 0) {
            nextCol++;
        } else if (colDiff < 0) {
            nextCol--;
        }

        // 이동 가능하면 이동, 불가능하면 제자리 대기
        GameMap gameMap = colonist.getGameMap();
        if (gameMap.isWalkable(nextRow, nextCol)) {
            current.moveTo(nextRow, nextCol);
        }
    }

    @Override
    public void exit(Colonist colonist) {
        // 이동 종료 시 별도 정리 없음
    }
}
