import java.util.HashMap;
import java.util.Map;

/// <summary>
/// 창고 클래스
/// 상품의 창고 재고를 관리하는 책임
/// </summary>
public class Warehouse {

    // ========== 필드 ==========

    // 상품별 재고 수량
    Map<Product, Integer> stock;

    // ========== 생성자 ==========

    Warehouse() {
        stock = new HashMap<>();
    }

    // ========== 메서드 ==========

    /// <summary>
    /// 상품 재고 확인
    /// </summary>
    int getStock(Product product) {
        return stock.getOrDefault(product, 0);
    }

    /// <summary>
    /// 재고가 있는지 확인
    /// </summary>
    boolean hasStock(Product product) {
        return getStock(product) > 0;
    }

    /// <summary>
    /// 재고 추가 (도매상에서 구매)
    /// </summary>
    void addStock(Product product, int amount) {
        int current = getStock(product);
        stock.put(product, current + amount);
    }

    /// <summary>
    /// 재고 감소 (매대로 이동)
    /// 요청량보다 적으면 있는 만큼만 감소
    /// 실제 감소량 반환
    /// </summary>
    int removeStock(Product product, int amount) {
        int current = getStock(product);
        int actual = Math.min(current, amount);
        stock.put(product, current - actual);
        return actual;
    }

    /// <summary>
    /// 전체 재고 출력 (디버그용)
    /// </summary>
    void printAll() {
        System.out.println("[창고 재고]");
        for (Map.Entry<Product, Integer> entry : stock.entrySet()) {
            if (entry.getValue() > 0) {
                System.out.printf(" - %s: %d개%n", entry.getKey().name, entry.getValue());
            }
        }
    }
}
