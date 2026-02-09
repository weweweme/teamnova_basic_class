/// <summary>
/// 상품 클래스
/// 슈퍼마켓에서 판매하는 모든 상품의 공통 클래스
/// </summary>
public class Product {

    // ========== 필드 ==========

    String name;
    int buyPrice;
    int sellPrice;
    int stock;
    int popularity;
    int boxSize;              // 박스당 수량 (자동주문 시 이 단위로 구매)
    boolean autoOrderEnabled; // 개별 자동주문 활성화 여부
    int autoOrderThreshold;   // 개별 자동주문 임계값 (재고가 이 값 이하면 주문)

    // ========== 생성자 ==========

    // 기존 생성자 (박스 사이즈 기본값 10)
    Product(String name, int buyPrice, int sellPrice, int popularity) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stock = 0;
        this.popularity = popularity;
        this.boxSize = 10;              // 기본 박스 사이즈
        this.autoOrderEnabled = false;  // 기본 비활성화
        this.autoOrderThreshold = 0;    // 기본 임계값 0
    }

    // 박스 사이즈 지정 생성자
    Product(String name, int buyPrice, int sellPrice, int popularity, int boxSize) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stock = 0;
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
        System.out.println("재고: " + stock + "개");
        System.out.println("인기도: " + popularity);
    }

    /// <summary>
    /// 재고 추가 (입고)
    /// </summary>
    void addStock(int amount) {
        stock = stock + amount;
    }

    /// <summary>
    /// 재고 감소 (판매)
    /// </summary>
    void removeStock(int amount) {
        stock = stock - amount;
    }

    /// <summary>
    /// 재고 확인
    /// </summary>
    int getStock() {
        return stock;
    }

    /// <summary>
    /// 판매 가능 여부 확인
    /// </summary>
    boolean isAvailable() {
        return stock > 0;
    }
}
