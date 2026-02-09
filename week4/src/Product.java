/// <summary>
/// 상품 클래스
/// 슈퍼마켓에서 판매하는 모든 상품의 공통 클래스
/// </summary>
public class Product {

    // ========== 필드 ==========

    String name;
    int buyPrice;
    int sellPrice;
    int warehouseStock;           // 창고 재고
    int displayStock;             // 매대 재고 (진열된 수량)
    int popularity;
    int boxSize;                  // 박스당 수량 (자동주문 시 이 단위로 구매)
    boolean autoOrderEnabled;     // 개별 자동주문 활성화 여부
    int autoOrderThreshold;       // 개별 자동주문 임계값 (재고가 이 값 이하면 주문)

    // ========== 생성자 ==========

    /// <summary>
    /// 기존 생성자 (박스 사이즈 기본값 10)
    /// </summary>
    Product(String name, int buyPrice, int sellPrice, int popularity) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.warehouseStock = 0;
        this.displayStock = 0;
        this.popularity = popularity;
        this.boxSize = 10;              // 기본 박스 사이즈
        this.autoOrderEnabled = false;  // 기본 비활성화
        this.autoOrderThreshold = 0;    // 기본 임계값 0
    }

    /// <summary>
    /// 박스 사이즈 지정 생성자
    /// </summary>
    Product(String name, int buyPrice, int sellPrice, int popularity, int boxSize) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.warehouseStock = 0;
        this.displayStock = 0;
        this.popularity = popularity;
        this.boxSize = boxSize;
        this.autoOrderEnabled = false;
        this.autoOrderThreshold = 0;
    }

    // ========== 메서드 ==========

    /// <summary>
    /// 상품 정보 출력
    /// </summary>
    void printInfo() {
        System.out.println("상품명: " + name);
        System.out.println("매입가: " + buyPrice + "원");
        System.out.println("판매가: " + sellPrice + "원");
        System.out.println("창고 재고: " + warehouseStock + "개");
        System.out.println("매대 재고: " + displayStock + "개");
        System.out.println("인기도: " + popularity);
    }

    /// <summary>
    /// 창고에 입고 (도매상에서 구매)
    /// </summary>
    void addToWarehouse(int amount) {
        warehouseStock = warehouseStock + amount;
    }

    /// <summary>
    /// 창고에서 매대로 진열
    /// </summary>
    void displayFromWarehouse(int amount) {
        if (amount > warehouseStock) {
            amount = warehouseStock;  // 창고 재고보다 많이 진열 불가
        }
        warehouseStock = warehouseStock - amount;
        displayStock = displayStock + amount;
    }

    /// <summary>
    /// 매대에서 창고로 회수
    /// </summary>
    void returnToWarehouse(int amount) {
        if (amount > displayStock) {
            amount = displayStock;  // 매대 재고보다 많이 회수 불가
        }
        displayStock = displayStock - amount;
        warehouseStock = warehouseStock + amount;
    }

    /// <summary>
    /// 매대에서 판매 (재고 감소)
    /// </summary>
    void sell(int amount) {
        displayStock = displayStock - amount;
    }

    /// <summary>
    /// 창고 재고 확인
    /// </summary>
    int getWarehouseStock() {
        return warehouseStock;
    }

    /// <summary>
    /// 매대 재고 확인
    /// </summary>
    int getDisplayStock() {
        return displayStock;
    }

    /// <summary>
    /// 총 재고 확인 (창고 + 매대)
    /// </summary>
    int getTotalStock() {
        return warehouseStock + displayStock;
    }

    /// <summary>
    /// 판매 가능 여부 확인 (매대에 재고가 있어야 판매 가능)
    /// </summary>
    boolean isAvailable() {
        return displayStock > 0;
    }

    /// <summary>
    /// 매대에 진열 중인지 확인 (슬롯 사용 여부)
    /// </summary>
    boolean isOnDisplay() {
        return displayStock > 0;
    }
}
