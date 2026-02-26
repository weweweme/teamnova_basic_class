package system;

/// <summary>
/// 대기 상태 — 명령이 없을 때 랜덤으로 배회
/// </summary>
public class IdleState extends ColonistState {

    /// <summary>
    /// 8방향 중 이동할 수 있는 방향 목록
    /// </summary>
    private static final Direction[] DIRECTIONS = Direction.values();

    @Override
    public void enter(Colonist colonist) {
        // 대기 상태 진입 시 별도 초기화 없음
    }

    @Override
    public void update(Colonist colonist) {
        // 랜덤 방향 선택
        int randomIndex = Util.rand(DIRECTIONS.length);
        Direction direction = DIRECTIONS[randomIndex];

        // 이동할 좌표 계산
        int newRow = colonist.getPosition().getRow() + direction.getDeltaRow();
        int newCol = colonist.getPosition().getCol() + direction.getDeltaCol();

        // 맵 범위 안이고 이동 가능한 타일이면 이동
        GameMap gameMap = colonist.getGameMap();
        if (gameMap.isWalkable(newRow, newCol)) {
            colonist.getPosition().moveTo(newRow, newCol);
        }
    }

    @Override
    public void exit(Colonist colonist) {
        // 대기 상태 퇴장 시 별도 정리 없음
    }

    @Override
    public String getDisplayName() {
        return "대기";
    }
}
