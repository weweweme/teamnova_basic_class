/// <summary>
/// 상추 상품 클래스
/// 슈퍼마켓에서 판매하는 바베큐용 식재료
/// </summary>
public class Lettuce {

    // ========== 필드 ==========

    String name;
    int buyPrice;
    int sellPrice;
    int stock;
    int popularity;

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
