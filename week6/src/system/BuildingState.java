package system;

/// <summary>
/// 건설 상태 — 건설 위치로 이동한 뒤 건물 건설 수행
/// 1. 건설 위치 옆까지 이동
/// 2. 건설 시간만큼 대기
/// 3. 건물 완성 후 맵에 등록, 대기 상태로 전환
/// </summary>
public class BuildingState extends ColonistState {

    /// <summary>
    /// 건설할 건물 종류
    /// </summary>
    private final BuildingType buildingType;

    /// <summary>
    /// 건설 위치
    /// </summary>
    private final Position buildPosition;

    /// <summary>
    /// 건설 위치 근처에 도착했는지 여부
    /// </summary>
    private boolean arrived;

    /// <summary>
    /// 현재 건설 경과 틱 수
    /// </summary>
    private int buildTicks;

    /// <summary>
    /// 건설 완료에 필요한 틱 수
    /// </summary>
    private int requiredTicks;

    /// <summary>
    /// 지정한 종류의 건물을 지정한 위치에 건설하는 상태 생성
    /// </summary>
    public BuildingState(BuildingType buildingType, Position buildPosition) {
        this.buildingType = buildingType;
        this.buildPosition = buildPosition;
    }

    /// <summary>
    /// 건설 위치 반환 (목표 마커 표시용)
    /// </summary>
    public Position getBuildPosition() {
        return buildPosition;
    }

    @Override
    public void enter(Colonist colonist) {
        this.arrived = false;
        this.buildTicks = 0;
        // 건설 시간(초)을 틱 수로 변환 (틱 간격 500ms = 0.5초)
        this.requiredTicks = buildingType.getBuildTime() * 2;
    }

    @Override
    public void update(Colonist colonist) {
        // 피로 증가 — 한계 도달 시 강제 휴식
        colonist.addFatigue(1);
        if (colonist.isExhausted()) {
            colonist.changeState(new RestingState());
            return;
        }

        // 건설 위치 근처에 도착하지 않았으면 이동
        if (!arrived) {
            moveToBuildSite(colonist);
            return;
        }

        // 건설 진행
        buildTicks++;

        if (buildTicks >= requiredTicks) {
            // 건설 완료 — 건물을 맵에 등록
            GameMap gameMap = colonist.getGameMap();
            Building building = new Building(new Position(buildPosition.getRow(), buildPosition.getCol()), buildingType);
            gameMap.addBuilding(building);
            colonist.changeState(new IdleState());
        }
    }

    /// <summary>
    /// 건설 위치 근처(맨해튼 거리 2 이내)까지 이동
    /// </summary>
    private void moveToBuildSite(Colonist colonist) {
        Position current = colonist.getPosition();

        // 건설 위치까지의 거리가 2 이하면 도착으로 판정
        int distance = current.distanceTo(buildPosition);
        if (distance <= 2) {
            arrived = true;
            return;
        }

        // 건설 위치를 향해 한 칸 이동
        int rowDiff = buildPosition.getRow() - current.getRow();
        int colDiff = buildPosition.getCol() - current.getCol();

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

        GameMap gameMap = colonist.getGameMap();
        if (gameMap.isWalkable(nextRow, nextCol)) {
            current.moveTo(nextRow, nextCol);
        }
    }

    @Override
    public void exit(Colonist colonist) {
        // 건설 종료 시 별도 정리 없음
    }

    @Override
    public String getDisplayName() {
        return "건설중";
    }
}
