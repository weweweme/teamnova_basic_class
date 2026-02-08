/// <summary>
/// 가족 손님 클래스
/// 고기, 음료를 많이 구매하는 손님 유형
/// </summary>
public class Family {

    // ========== 필드 ==========

    String name;
    int money;
    String[] wantItems;
    int[] wantAmounts;

    // ========== 메서드 ==========

    /// <summary>
    /// 손님 정보 출력
    /// </summary>
    void printInfo() {
        System.out.println("손님 유형: " + name);
        System.out.println("보유 금액: " + money + "원");
    }

    /// <summary>
    /// 원하는 상품 목록 출력
    /// </summary>
    void sayWant() {
        System.out.println("원하는 상품:");
        for (int i = 0; i < wantItems.length; i++) {
            System.out.println(" - " + wantItems[i] + " " + wantAmounts[i] + "개");
        }
    }

    /// <summary>
    /// 상품 구매 (금액 차감)
    /// </summary>
    void buy(int price) {
        money = money - price;
    }

    /// <summary>
    /// 보유 금액 확인
    /// </summary>
    int getMoney() {
        return money;
    }
}
