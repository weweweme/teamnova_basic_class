package system;

/// <summary>
/// 건설 가능한 건물 종류
/// 각 건물은 건설 비용(자재, 광석), 건설 시간, 효과가 다름
/// </summary>
public enum BuildingType {

    /// <summary>
    /// 벽 — 이동 불가 타일, 적 침입 차단
    /// </summary>
    WALL(5, 0, 0, 3),

    /// <summary>
    /// 저장소 — 자원 보관 용량 증가
    /// </summary>
    STORAGE(15, 0, 0, 5),

    /// <summary>
    /// 침실 — 근처에서 휴식 시 회복 속도 2배
    /// </summary>
    BEDROOM(10, 0, 0, 4),

    /// <summary>
    /// 방어탑 — 범위 내 적에게 자동 공격
    /// </summary>
    TOWER(8, 5, 0, 6);

    /// <summary>
    /// 건설에 필요한 자재
    /// </summary>
    private final int materialCost;

    /// <summary>
    /// 건설에 필요한 광석
    /// </summary>
    private final int oreCost;

    /// <summary>
    /// 건설에 필요한 식량
    /// </summary>
    private final int foodCost;

    /// <summary>
    /// 건설에 걸리는 시간 (초)
    /// </summary>
    private final int buildTime;

    BuildingType(int materialCost, int oreCost, int foodCost, int buildTime) {
        this.materialCost = materialCost;
        this.oreCost = oreCost;
        this.foodCost = foodCost;
        this.buildTime = buildTime;
    }

    /// <summary>
    /// 필요 자재 반환
    /// </summary>
    public int getMaterialCost() {
        return materialCost;
    }

    /// <summary>
    /// 필요 광석 반환
    /// </summary>
    public int getOreCost() {
        return oreCost;
    }

    /// <summary>
    /// 필요 식량 반환
    /// </summary>
    public int getFoodCost() {
        return foodCost;
    }

    /// <summary>
    /// 건설 시간(초) 반환
    /// </summary>
    public int getBuildTime() {
        return buildTime;
    }
}
