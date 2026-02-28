package world;

/// <summary>
/// 식민지 전체가 공유하는 보급품 관리
/// 적 처치 보상과 매일 보급으로 획득, 수리/강화/치료에 사용
/// 여러 스레드에서 동시에 접근하므로 동기화 필요
/// </summary>
public class Supply {

    /// <summary>
    /// 보유 보급품
    /// </summary>
    private int amount;

    /// <summary>
    /// 보급품 0으로 시작
    /// </summary>
    public Supply() {
        this.amount = 0;
    }

    /// <summary>
    /// 보급품 수량 반환
    /// </summary>
    public synchronized int getAmount() {
        return amount;
    }

    /// <summary>
    /// 보급품 추가
    /// </summary>
    public synchronized void add(int quantity) {
        amount += quantity;
    }

    /// <summary>
    /// 비용을 감당할 수 있는지 확인
    /// </summary>
    public synchronized boolean canAfford(int cost) {
        return amount >= cost;
    }

    /// <summary>
    /// 비용만큼 보급품 차감, 부족하면 false
    /// </summary>
    public synchronized boolean spend(int cost) {
        if (!canAfford(cost)) {
            return false;
        }
        amount -= cost;
        return true;
    }
}
