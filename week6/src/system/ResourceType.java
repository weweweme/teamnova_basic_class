package system;

/// <summary>
/// 맵에 배치되는 자원의 종류
/// 식량(생존용), 자재(건설 기본), 광석(건설 희귀) 3가지
/// </summary>
public enum ResourceType {

    /// <summary>
    /// 열매 덤불 — 채집하면 식량 획득 (정착민 생존용)
    /// </summary>
    FOOD(3, 2),

    /// <summary>
    /// 자재 더미 — 채집하면 자재 획득 (기본 건설 재료)
    /// </summary>
    MATERIAL(4, 3),

    /// <summary>
    /// 광산 — 채집하면 광석 획득 (희귀 건설 재료, 무한 채집)
    /// </summary>
    ORE(-1, 5);

    /// <summary>
    /// 최대 채집 횟수 (-1이면 무제한)
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

    /// <summary>
    /// 채집 횟수가 무제한인지 확인 (광산처럼 영구 자원인 경우)
    /// </summary>
    public boolean isInfinite() {
        return maxHarvest == -1;
    }
}
