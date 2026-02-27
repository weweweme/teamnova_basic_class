package system;

/// <summary>
/// 식민지 전체가 공유하는 자원 보유량 관리
/// 채집으로 얻은 자원이 여기에 저장됨
/// 여러 정착민 스레드에서 동시에 접근하므로 동기화 필요
/// </summary>
public class Supply {

    /// <summary>
    /// 보유 식량
    /// </summary>
    private int food;

    /// <summary>
    /// 보유 자재
    /// </summary>
    private int material;

    /// <summary>
    /// 보유 광석
    /// </summary>
    private int ore;

    /// <summary>
    /// 모든 자원 0으로 시작
    /// </summary>
    public Supply() {
        this.food = 0;
        this.material = 0;
        this.ore = 0;
    }

    /// <summary>
    /// 자원 종류에 따라 1만큼 추가
    /// 여러 스레드에서 동시 호출될 수 있어 동기화 처리
    /// </summary>
    public synchronized void add(ResourceType type) {
        switch (type) {
            case FOOD:
                food++;
                break;
            case MATERIAL:
                material++;
                break;
            case ORE:
                ore++;
                break;
        }
    }

    /// <summary>
    /// 식량 1 소비, 식량이 있으면 차감 후 true, 없으면 false
    /// </summary>
    public synchronized boolean consumeFood() {
        if (food > 0) {
            food--;
            return true;
        }
        return false;
    }

    /// <summary>
    /// 보유 식량 반환
    /// </summary>
    public synchronized int getFood() {
        return food;
    }

    /// <summary>
    /// 보유 자재 반환
    /// </summary>
    public synchronized int getMaterial() {
        return material;
    }

    /// <summary>
    /// 보유 광석 반환
    /// </summary>
    public synchronized int getOre() {
        return ore;
    }

    /// <summary>
    /// 건물 건설 비용을 감당할 수 있는지 확인
    /// </summary>
    public synchronized boolean canAfford(BuildingType buildingType) {
        boolean enoughMaterial = material >= buildingType.getMaterialCost();
        boolean enoughOre = ore >= buildingType.getOreCost();
        boolean enoughFood = food >= buildingType.getFoodCost();
        return enoughMaterial && enoughOre && enoughFood;
    }

    /// <summary>
    /// 건물 건설 비용만큼 자원 차감
    /// 비용이 부족하면 차감하지 않고 false 반환
    /// </summary>
    public synchronized boolean spend(BuildingType buildingType) {
        if (!canAfford(buildingType)) {
            return false;
        }
        material -= buildingType.getMaterialCost();
        ore -= buildingType.getOreCost();
        food -= buildingType.getFoodCost();
        return true;
    }
}
