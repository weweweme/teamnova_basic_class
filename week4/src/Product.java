/// <summary>
/// 상품 클래스
/// 상품의 기본 정보만 담당 (재고는 Warehouse/Display가 관리)
/// </summary>
public class Product {

    // ========== 필드 ==========

    public String name;          // 상품명
    public int buyPrice;         // 매입가
    public int sellPrice;        // 판매가
    public int popularity;       // 인기도 (높을수록 손님이 많이 찾음)
    public int quantityPerBox;          // 박스당 수량 (도매상에서 이 단위로 구매)

    // 자동주문 설정 (상품별 개별 설정)
    public boolean autoOrderEnabled;   // 자동주문 활성화 여부
    public int autoOrderThreshold;     // 자동주문 임계값 (총 재고가 이 값 이하면 주문)

    // ========== 생성자 ==========

    /// <summary>
    /// 상품 생성자
    /// </summary>
    public Product(String name, int buyPrice, int sellPrice, int popularity, int quantityPerBox) {
        this.name = name;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.popularity = popularity;
        this.quantityPerBox = quantityPerBox;
    }
}
