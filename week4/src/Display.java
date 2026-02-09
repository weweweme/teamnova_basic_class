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
        // 창고에서 가져올 수 있는 양 확인
        int warehouseStock = warehouse.getStock(product);
        int actualFromWarehouse = Math.min(amount, warehouseStock);

        // 매대에 놓을 수 있는 양 확인
        int currentDisplay = getDisplayed(product);
        int roomInSlot = maxPerSlot - currentDisplay;
        int actualToDisplay = Math.min(actualFromWarehouse, roomInSlot);

        if (actualToDisplay <= 0) {
            return 0;
        }

        // 새 상품인 경우 슬롯 확인
        boolean isNewProduct = currentDisplay == 0;
        if (isNewProduct && !hasEmptySlot()) {
            return 0;  // 빈 슬롯 없음
        }

        // 창고에서 제거
        warehouse.removeStock(product, actualToDisplay);

        // 매대에 추가
        displayed.put(product, currentDisplay + actualToDisplay);

        // 새 상품이면 슬롯 사용
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
        int current = getDisplayed(product);
        int actual = Math.min(current, amount);

        if (actual <= 0) {
            return;
        }

        int remaining = current - actual;
        displayed.put(product, remaining);

        // 재고가 0이 되면 슬롯 해제
        if (remaining == 0) {
            usedSlots--;
        }
    }

    /// <summary>
    /// 매대에서 창고로 상품 회수
    /// </summary>
    public int returnToWarehouse(Product product, Warehouse warehouse, int amount) {
        int current = getDisplayed(product);
        int actual = Math.min(current, amount);

        if (actual <= 0) {
            return 0;
        }

        // 매대에서 제거
        int remaining = current - actual;
        displayed.put(product, remaining);

        // 재고가 0이 되면 슬롯 해제
        if (remaining == 0) {
            usedSlots--;
        }

        // 창고에 추가
        warehouse.addStock(product, actual);

        return actual;
    }
}
