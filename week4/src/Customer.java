/// <summary>
/// 손님 클래스
/// 유형(가족, 커플, 친구, 혼자)에 따라 구매 패턴이 다름
/// </summary>
public class Customer {

    // ========== 상수 ==========

    // 손님 유형
    public static final int TYPE_FAMILY = 0;   // 가족 - 고기, 음료 많이 구매
    public static final int TYPE_COUPLE = 1;   // 커플 - 술, 안주 위주
    public static final int TYPE_FRIENDS = 2;  // 친구들 - 다양하게 조금씩
    public static final int TYPE_SOLO = 3;     // 혼자 - 라면, 맥주 간단히

    // 유형별 최대 구매 품목 수
    private static final int MAX_ITEMS_FAMILY = 8;
    private static final int MAX_ITEMS_COUPLE = 6;
    private static final int MAX_ITEMS_FRIENDS = 7;
    private static final int MAX_ITEMS_SOLO = 5;

    // ========== 필드 ==========

    public int type;              // 손님 유형 (0~3)
    public String typeName;       // 유형 이름 ("가족 손님" 등)
    public String greeting;       // 인사말

    // 구매 목록
    public Product[] wantProducts;   // 원하는 상품 목록
    public int[] wantAmounts;        // 각 상품별 희망 수량
    public int wantCount;            // 실제 구매 품목 수

    // ========== 생성자 ==========

    /// <summary>
    /// 손님 생성
    /// </summary>
    public Customer(int type, String typeName) {
        this.type = type;
        this.typeName = typeName;

        // 유형별 배열 크기 할당
        int maxItems = getMaxItems(type);
        this.wantProducts = new Product[maxItems];
        this.wantAmounts = new int[maxItems];
        this.wantCount = maxItems;
    }

    // ========== 메서드 ==========

    /// <summary>
    /// 유형별 최대 품목 수 반환
    /// </summary>
    public static int getMaxItems(int type) {
        if (type == TYPE_FAMILY) {
            return MAX_ITEMS_FAMILY;
        } else if (type == TYPE_COUPLE) {
            return MAX_ITEMS_COUPLE;
        } else if (type == TYPE_FRIENDS) {
            return MAX_ITEMS_FRIENDS;
        } else {
            return MAX_ITEMS_SOLO;
        }
    }

    /// <summary>
    /// 인사말 출력
    /// </summary>
    public void sayGreeting() {
        System.out.println("\"" + greeting + "\"");
    }

    /// <summary>
    /// 원하는 상품 목록 출력
    /// </summary>
    public void sayWant() {
        System.out.println("원하는 상품:");
        for (int i = 0; i < wantCount; i++) {
            if (wantAmounts[i] > 0 && wantProducts[i] != null) {
                System.out.println(" - " + wantProducts[i].name + " " + wantAmounts[i] + "개");
            }
        }
    }
}
