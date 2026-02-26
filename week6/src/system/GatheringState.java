package system;

/// <summary>
/// 채집 상태 — 자원 근처로 이동한 뒤 채집 수행
/// 1. 자원 옆까지 이동
/// 2. 채집 시간만큼 대기
/// 3. 자원 횟수 차감
/// 4. 횟수가 남아있으면 다시 채집, 없으면 대기 상태로 전환
/// </summary>
public class GatheringState extends ColonistState {

    /// <summary>
    /// 채집할 자원
    /// </summary>
    private final Resource target;

    /// <summary>
    /// 자원 옆에 도착했는지 여부
    /// </summary>
    private boolean arrived;

    /// <summary>
    /// 현재 채집 경과 틱 수
    /// </summary>
    private int gatherTicks;

    /// <summary>
    /// 1회 채집에 필요한 틱 수 (채집 시간(초) * 1000 / 틱 간격(500ms))
    /// </summary>
    private int requiredTicks;

    /// <summary>
    /// 지정한 자원을 채집하는 상태 생성
    /// </summary>
    public GatheringState(Resource target) {
        this.target = target;
    }

    /// <summary>
    /// 채집 대상 자원 반환
    /// </summary>
    public Resource getTarget() {
        return target;
    }

    @Override
    public void enter(Colonist colonist) {
        this.arrived = false;
        this.gatherTicks = 0;
        // 채집 시간(초)을 틱 수로 변환 (틱 간격 500ms = 0.5초)
        this.requiredTicks = target.getType().getHarvestTime() * 2;
    }

    @Override
    public void update(Colonist colonist) {
        // 자원이 이미 소진되었으면 대기로 전환
        if (!target.isHarvestable()) {
            colonist.changeState(new IdleState());
            return;
        }

        // 자원 옆에 도착하지 않았으면 이동
        if (!arrived) {
            moveToResource(colonist);
            return;
        }

        // 채집 진행
        gatherTicks++;

        if (gatherTicks >= requiredTicks) {
            // 1회 채집 완료
            target.harvest();
            gatherTicks = 0;

            // 자원이 소진되었으면 맵에서 제거하고 대기로 전환
            if (!target.isHarvestable()) {
                colonist.getGameMap().removeResource(target);
                colonist.changeState(new IdleState());
            }
        }
    }

    /// <summary>
    /// 자원 근처(맨해튼 거리 2 이내)까지 이동
    /// </summary>
    private void moveToResource(Colonist colonist) {
        Position current = colonist.getPosition();
        Position resourcePos = target.getPosition();

        // 자원까지의 거리가 2 이하면 도착으로 판정
        int distance = current.distanceTo(resourcePos);
        if (distance <= 2) {
            arrived = true;
            return;
        }

        // 자원을 향해 한 칸 이동
        int rowDiff = resourcePos.getRow() - current.getRow();
        int colDiff = resourcePos.getCol() - current.getCol();

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
        // 채집 종료 시 별도 정리 없음
    }

    @Override
    public String getDisplayName() {
        return "채집중";
    }
}
