package system;

/// <summary>
/// 맵 위에 배치된 자원 하나를 나타내는 클래스
/// 열매 덤불, 자재 더미, 광산이 해당하며, 정착민이 채집할 수 있음
/// </summary>
public class Resource {

    /// <summary>
    /// 맵 위의 위치
    /// </summary>
    private final Position position;

    /// <summary>
    /// 자원 종류 (식량, 자재, 광석)
    /// </summary>
    private final ResourceType type;

    /// <summary>
    /// 남은 채집 횟수 (무한 자원은 이 값을 사용하지 않음)
    /// </summary>
    private int harvestRemaining;

    /// <summary>
    /// 지정한 위치와 종류로 자원 생성
    /// 남은 채집 횟수는 종류별 최대값으로 초기화
    /// </summary>
    public Resource(Position position, ResourceType type) {
        this.position = position;
        this.type = type;
        this.harvestRemaining = type.getMaxHarvest();
    }

    /// <summary>
    /// 위치 반환
    /// </summary>
    public Position getPosition() {
        return position;
    }

    /// <summary>
    /// 자원 종류 반환
    /// </summary>
    public ResourceType getType() {
        return type;
    }

    /// <summary>
    /// 남은 채집 횟수 반환
    /// </summary>
    public int getHarvestRemaining() {
        return harvestRemaining;
    }

    /// <summary>
    /// 채집 가능한지 확인
    /// 무한 자원(광산)은 항상 채집 가능
    /// </summary>
    public boolean isHarvestable() {
        if (type.isInfinite()) {
            return true;
        }
        return harvestRemaining > 0;
    }

    /// <summary>
    /// 채집 1회 수행, 남은 횟수를 1 줄임
    /// 무한 자원(광산)은 횟수가 줄지 않음
    /// </summary>
    public void harvest() {
        if (type.isInfinite()) {
            return;
        }
        if (harvestRemaining > 0) {
            harvestRemaining--;
        }
    }
}
