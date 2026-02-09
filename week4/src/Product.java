/// <summary>
/// 상품 클래스
/// 상품의 기본 정보만 담당 (재고는 Warehouse/Display가 관리)
/// </summary>
public class Product {

    // ========== 필드 ==========

    String name;          // 상품명
    int buyPrice;         // 매입가
    int sellPrice;        // 판매가
    int popularity;       // 인기도 (높을수록 손님이 많이 찾음)
    int boxSize;          // 박스당 수량 (도매상에서 이 단위로 구매)

    // 자동주문 설정 (상품별 개별 설정)
    boolean autoOrderEnabled;   // 자동주문 활성화 여부
    int autoOrderThreshold;     // 자동주문 임계값 (총 재고가 이 값 이하면 주문)

    // ========== 생성자 ==========

    /// <summary>
    /// 기존 생성자 (박스 사이즈 기본값 10)
    /// </summary>
    Product(String name, int buyPrice, int sellPrice, int popularity) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.popularity = popularity;
        this.boxSize = 10;
    }

    /// <summary>
    /// 박스 사이즈 지정 생성자
    /// </summary>
    Product(String name, int buyPrice, int sellPrice, int popularity, int boxSize) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.popularity = popularity;
        this.boxSize = boxSize;
    }

    // ========== 메서드 ==========

    /// <summary>
    /// 상품 기본 정보 출력
    /// </summary>
    void printInfo() {
        System.out.println("상품명: " + name);
        System.out.println("매입가: " + buyPrice + "원");
        System.out.println("판매가: " + sellPrice + "원");
        System.out.println("인기도: " + popularity);
        System.out.println("박스 단위: " + boxSize + "개");
    }

    /// <summary>
    /// 상품 정보 출력 (재고 포함)
    /// Warehouse와 Display에서 재고 정보를 받아서 출력
    /// </summary>
    void printInfoWithStock(Warehouse warehouse, Display display) {
        System.out.println("상품명: " + name);
        System.out.println("매입가: " + buyPrice + "원");
        System.out.println("판매가: " + sellPrice + "원");
        System.out.println("창고 재고: " + warehouse.getStock(this) + "개");
        System.out.println("매대 재고: " + display.getDisplayed(this) + "개");
        System.out.println("인기도: " + popularity);
    }

    /// <summary>
    /// 마진 계산 (판매가 - 매입가)
    /// </summary>
    int getMargin() {
        return sellPrice - buyPrice;
    }
}
