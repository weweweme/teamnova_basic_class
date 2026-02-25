package system;

/// <summary>
/// 맵에 배치되는 자원의 종류
/// 각 자원은 채집 시 얻는 재화, 채집 횟수, 채집 시간이 다름
/// </summary>
public enum ResourceType {

    /// <summary>
    /// 나무 — 채집하면 목재 획득
    /// </summary>
    TREE(3, 3),

    /// <summary>
    /// 돌 — 채집하면 석재 획득
    /// </summary>
    ROCK(3, 5),

    /// <summary>
    /// 철광석 — 채집하면 철 획득
    /// </summary>
    IRON(3, 5);

    /// <summary>
    /// 최대 채집 횟수 (다 소진하면 자원 사라짐)
    /// </summary>
    private final int maxHarvest;

    /// <summary>
    /// 1회 채집에 걸리는 시간 (초)
    /// </summary>
    private final int harvestTime;

    ResourceType(int maxHarvest, int harvestTime) {
        this.maxHarvest = maxHarvest;
        this.harvestTime = harvestTime;
    }

    /// <summary>
    /// 최대 채집 횟수 반환
    /// </summary>
    public int getMaxHarvest() {
        return maxHarvest;
    }

    /// <summary>
    /// 1회 채집 시간(초) 반환
    /// </summary>
    public int getHarvestTime() {
        return harvestTime;
    }
}
