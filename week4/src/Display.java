import java.util.HashMap;
import java.util.Map;

/// <summary>
/// 매대 클래스
/// 상품 진열과 판매를 관리하는 책임
/// </summary>
public class Display {

    // ========== 필드 ==========

    int maxSlots;        // 최대 슬롯 수 (진열 가능한 상품 종류 수)
    int maxPerSlot;      // 슬롯당 최대 수량
    int usedSlots;       // 현재 사용 중인 슬롯 수

    // 상품별 진열 수량
    Map<Product, Integer> displayed;

    // ========== 생성자 ==========

    Display(int maxSlots, int maxPerSlot) {
        this.maxSlots = maxSlots;
        this.maxPerSlot = maxPerSlot;
        this.usedSlots = 0;
        this.displayed = new HashMap<>();
    }

    // ========== 조회 메서드 ==========

    /// <summary>
    /// 상품의 진열 수량 확인
    /// </summary>
    public int getDisplayed(Product product) {
        return displayed.getOrDefault(product, 0);
    }

    /// <summary>
    /// 새 상품을 위한 빈 슬롯이 있는지 확인
    /// </summary>
    public boolean hasEmptySlot() {
        return usedSlots < maxSlots;
    }

    /// <summary>
    /// 사용 중인 슬롯 수 확인
    /// </summary>
    public int getUsedSlots() {
        return usedSlots;
    }

    // ========== 변경 메서드 ==========

    /// <summary>
    /// 창고에서 매대로 상품 진열
    /// 실제 진열된 수량 반환
    /// </summary>
    public int displayFromWarehouse(Product product, Warehouse warehouse, int amount) {
        // ========== 실제 진열 수량 계산 ==========
        // 요청량, 창고 재고, 슬롯 여유 중 가장 작은 값이 실제 진열량

        // 창고에 있는 만큼만 가져올 수 있음
        int warehouseStock = warehouse.getStock(product);
        int actualFromWarehouse = Math.min(amount, warehouseStock);

        // 슬롯당 최대 수량에서 현재 진열량을 뺀 만큼만 추가 가능
        int currentDisplay = getDisplayed(product);
        int roomInSlot = maxPerSlot - currentDisplay;
        int actualToDisplay = Math.min(actualFromWarehouse, roomInSlot);

        if (actualToDisplay <= 0) {
            return 0;
        }

        // 매대에 처음 올리는 상품이면 빈 슬롯이 필요
        boolean isNewProduct = currentDisplay == 0;
        if (isNewProduct && !hasEmptySlot()) {
            return 0;
        }

        // ========== 진열 처리 ==========
        // 창고 차감 -> 매대 추가 -> 슬롯 카운트 갱신

        warehouse.removeStock(product, actualToDisplay);
        displayed.put(product, currentDisplay + actualToDisplay);

        if (isNewProduct) {
            usedSlots++;
        }

        return actualToDisplay;
    }

    /// <summary>
    /// 매대에서 상품 판매
    /// 실제 판매된 수량 반환
    /// </summary>
    public void sell(Product product, int amount) {
        // 요청량과 현재 진열량 중 작은 쪽이 실제 판매량
        int current = getDisplayed(product);
        int actual = Math.min(current, amount);

        if (actual <= 0) {
            return;
        }

        // 매대 수량 차감
        int remaining = current - actual;
        displayed.put(product, remaining);

        // 해당 상품이 매대에서 완전히 소진되면 슬롯 반환
        if (remaining == 0) {
            usedSlots--;
        }
    }

    /// <summary>
    /// 매대에서 창고로 상품 회수
    /// </summary>
    public int returnToWarehouse(Product product, Warehouse warehouse, int amount) {
        // 요청량과 현재 진열량 중 작은 쪽이 실제 회수량
        int current = getDisplayed(product);
        int actual = Math.min(current, amount);

        if (actual <= 0) {
            return 0;
        }

        // ========== 회수 처리 ==========
        // 매대 차감 -> 슬롯 카운트 갱신 -> 창고 추가

        int remaining = current - actual;
        displayed.put(product, remaining);

        // 해당 상품이 매대에서 완전히 소진되면 슬롯 반환
        if (remaining == 0) {
            usedSlots--;
        }

        warehouse.addStock(product, actual);

        return actual;
    }
}
