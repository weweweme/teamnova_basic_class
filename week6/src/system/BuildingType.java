package system;

/// <summary>
/// 건설 가능한 건물 종류
/// 각 건물은 건설 비용, 건설 시간, 효과가 다름
/// </summary>
public enum BuildingType {

    /// <summary>
    /// 벽 — 이동 불가 타일, 적 침입 차단
    /// </summary>
    WALL(5, 0, 0, 0, 3),

    /// <summary>
    /// 저장소 — 자원 보관 용량 증가
    /// </summary>
    STORAGE(10, 10, 0, 0, 5),

    /// <summary>
    /// 침실 — 근처에서 휴식 시 회복 속도 2배
    /// </summary>
    BEDROOM(8, 5, 0, 0, 4);

    /// <summary>
    /// 건설에 필요한 목재
    /// </summary>
    private final int woodCost;

    /// <summary>
    /// 건설에 필요한 석재
    /// </summary>
    private final int stoneCost;

    /// <summary>
    /// 건설에 필요한 식량
    /// </summary>
    private final int foodCost;

    /// <summary>
    /// 건설에 필요한 철
    /// </summary>
    private final int ironCost;

    /// <summary>
    /// 건설에 걸리는 시간 (초)
    /// </summary>
    private final int buildTime;

    BuildingType(int woodCost, int stoneCost, int foodCost, int ironCost, int buildTime) {
        this.woodCost = woodCost;
        this.stoneCost = stoneCost;
        this.foodCost = foodCost;
        this.ironCost = ironCost;
        this.buildTime = buildTime;
    }

    /// <summary>
    /// 필요 목재 반환
    /// </summary>
    public int getWoodCost() {
        return woodCost;
    }

    /// <summary>
    /// 필요 석재 반환
    /// </summary>
    public int getStoneCost() {
        return stoneCost;
    }

    /// <summary>
    /// 필요 식량 반환
    /// </summary>
    public int getFoodCost() {
        return foodCost;
    }

    /// <summary>
    /// 필요 철 반환
    /// </summary>
    public int getIronCost() {
        return ironCost;
    }

    /// <summary>
    /// 건설 시간(초) 반환
    /// </summary>
    public int getBuildTime() {
        return buildTime;
    }
}
