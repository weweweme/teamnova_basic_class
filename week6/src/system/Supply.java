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
    /// 보유 목재
    /// </summary>
    private int wood;

    /// <summary>
    /// 보유 석재
    /// </summary>
    private int stone;

    /// <summary>
    /// 보유 철
    /// </summary>
    private int iron;

    /// <summary>
    /// 모든 자원 0으로 시작
    /// </summary>
    public Supply() {
        this.food = 0;
        this.wood = 0;
        this.stone = 0;
        this.iron = 0;
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
            case TREE:
                wood++;
                break;
            case ROCK:
                stone++;
                break;
            case IRON:
                iron++;
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
    /// 보유 목재 반환
    /// </summary>
    public synchronized int getWood() {
        return wood;
    }

    /// <summary>
    /// 보유 석재 반환
    /// </summary>
    public synchronized int getStone() {
        return stone;
    }

    /// <summary>
    /// 보유 철 반환
    /// </summary>
    public synchronized int getIron() {
        return iron;
    }

    /// <summary>
    /// 건물 건설 비용을 감당할 수 있는지 확인
    /// </summary>
    public synchronized boolean canAfford(BuildingType buildingType) {
        boolean enoughWood = wood >= buildingType.getWoodCost();
        boolean enoughStone = stone >= buildingType.getStoneCost();
        boolean enoughFood = food >= buildingType.getFoodCost();
        boolean enoughIron = iron >= buildingType.getIronCost();
        return enoughWood && enoughStone && enoughFood && enoughIron;
    }

    /// <summary>
    /// 건물 건설 비용만큼 자원 차감
    /// 비용이 부족하면 차감하지 않고 false 반환
    /// </summary>
    public synchronized boolean spend(BuildingType buildingType) {
        if (!canAfford(buildingType)) {
            return false;
        }
        wood -= buildingType.getWoodCost();
        stone -= buildingType.getStoneCost();
        food -= buildingType.getFoodCost();
        iron -= buildingType.getIronCost();
        return true;
    }
}
