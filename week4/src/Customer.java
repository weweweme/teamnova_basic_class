/// <summary>
/// 손님 클래스
/// 유형(가족, 커플, 친구, 혼자)에 따라 구매 패턴이 다름
/// 생성 시 유형별 쇼핑 패턴(카테고리+수량)을 자동 설정
/// </summary>
public class Customer {

    // ========== 상수 ==========

    // 손님 유형
    public static final int TYPE_FAMILY = 0;   // 가족 - 고기, 음료 많이 구매
    public static final int TYPE_COUPLE = 1;   // 커플 - 술, 안주 위주
    public static final int TYPE_FRIENDS = 2;  // 친구들 - 다양하게 조금씩

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
    public int[] wantCategories;     // 원하는 카테고리 인덱스 (Category.INDEX_MEAT 등)
    public Product[] wantProducts;   // 실제 선택된 상품 (Cashier가 매대에서 골라 채움)
    public int[] wantAmounts;        // 각 상품별 희망 수량
    public int wantCount;            // 실제 구매 품목 수

    // ========== 생성자 ==========

    /// <summary>
    /// 손님 생성
    /// 유형에 따라 이름, 쇼핑 패턴(카테고리+수량)을 자동 설정
    /// </summary>
    public Customer(int type) {
        this.type = type;
        this.typeName = getTypeName(type);

        // 유형별 배열 크기 할당
        int maxItems = getMaxItems(type);
        this.wantCategories = new int[maxItems];
        this.wantProducts = new Product[maxItems];
        this.wantAmounts = new int[maxItems];
        this.wantCount = maxItems;

        // 유형별 쇼핑 패턴 설정
        initShoppingList();
    }

    // ========== 멘트 상수 ==========

    // 손님 유형별 인사말 [유형][멘트]
    public static final String[][] TYPE_GREETINGS = {
        // 가족 손님 (0)
        {
            "바베큐 하려고 왔어요~",
            "애들이랑 고기 구워 먹으려고요!",
            "가족 나들이 왔다가 들렀어요~",
            "오늘 저녁은 삼겹살이에요!",
            "아이들 간식도 좀 사려고요~"
        },
        // 커플 손님 (1)
        {
            "오늘 달 보면서 한잔 하려고요~",
            "둘이서 조용히 마시려고요~",
            "데이트하다가 들렀어요!",
            "와인 대신 소주로 할래요~",
            "안주 좀 추천해주세요~"
        },
        // 친구들 (2)
        {
            "우리 오늘 펜션에서 파티해요!!",
            "MT 왔어요! 술 많이 주세요~",
            "불꽃놀이 할 건데 폭죽 있어요?",
            "다같이 모여서 놀려고요!",
            "친구들이랑 바베큐 파티에요~"
        },
        // 혼자 온 손님 (3)
        {
            "라면이랑 맥주 좀 주세요.",
            "혼자 조용히 먹으려고요...",
            "야식 사러 왔어요~",
            "간단하게 먹을 거 찾고 있어요.",
            "편하게 혼술하려고요~"
        }
    };

    // 시간대별 멘트
    public static final String[] MORNING_GREETINGS = {
        "아침부터 열일하시네요!",
        "아침 일찍 오셨네요~",
        "오전에 미리 사두려고요!",
        "아침밥 준비하러 왔어요~",
        "일찍 나왔더니 기분 좋네요!"
    };

    public static final String[] AFTERNOON_GREETINGS = {
        "점심 준비하러 왔어요~",
        "오후에 먹으려고요!",
        "낮이라 사람 많네요~",
        "한낮에 장보러 왔어요!",
        "오후에 뭐 좀 사려고요~"
    };

    public static final String[] NIGHT_GREETINGS = {
        "저녁에 다 같이 먹을 거예요~",
        "밤에 야식으로 먹을 거예요!",
        "저녁 준비하러 왔어요~",
        "밤바람 쐬러 나왔다가 들렀어요~",
        "야식 사러 왔어요!"
    };

    // ========== 메서드 ==========

    /// <summary>
    /// 유형별 이름 반환
    /// </summary>
    private static String getTypeName(int type) {
        switch (type) {
            case TYPE_FAMILY:
                return "가족 손님";
            case TYPE_COUPLE:
                return "커플 손님";
            case TYPE_FRIENDS:
                return "친구들";
            default:
                return "혼자 온 손님";
        }
    }

    /// <summary>
    /// 유형별 최대 품목 수 반환
    /// </summary>
    public static int getMaxItems(int type) {
        switch (type) {
            case TYPE_FAMILY:
                return MAX_ITEMS_FAMILY;
            case TYPE_COUPLE:
                return MAX_ITEMS_COUPLE;
            case TYPE_FRIENDS:
                return MAX_ITEMS_FRIENDS;
            default:
                return MAX_ITEMS_SOLO;
        }
    }

    /// <summary>
    /// 유형별 쇼핑 패턴 설정
    /// 어떤 카테고리에서 몇 개를 살지 결정
    /// </summary>
    private void initShoppingList() {
        switch (type) {
            case TYPE_FAMILY:
                // 가족: 고기 + 식재료 + 음료 (필수) / 안주, 아이스크림 (선택 50%)
                wantCategories[0] = Category.INDEX_MEAT;
                wantCategories[1] = Category.INDEX_MEAT;
                wantCategories[2] = Category.INDEX_GROCERY;
                wantCategories[3] = Category.INDEX_GROCERY;
                wantCategories[4] = Category.INDEX_DRINK;
                wantCategories[5] = Category.INDEX_DRINK;
                wantCategories[6] = Category.INDEX_SNACK;
                wantCategories[7] = Category.INDEX_ICECREAM;

                wantAmounts[0] = 2 + Util.rand(2);           // 고기1 (필수)
                wantAmounts[1] = 1 + Util.rand(2);           // 고기2 (필수)
                wantAmounts[2] = 1 + Util.rand(2);           // 식재료1 (필수)
                wantAmounts[3] = 1 + Util.rand(2);           // 식재료2 (필수)
                wantAmounts[4] = 2 + Util.rand(3);           // 음료1 (필수)
                wantAmounts[5] = 1 + Util.rand(2);           // 음료2 (필수)
                wantAmounts[6] = maybeBuy(2 + Util.rand(2)); // 안주 (선택 50%)
                wantAmounts[7] = maybeBuy(2 + Util.rand(3)); // 아이스크림 (선택 50%)
                break;

            case TYPE_COUPLE:
                // 커플: 소주 + 맥주 + 안주 (필수) / 음료, 아이스크림 (선택 50%)
                wantCategories[0] = Category.INDEX_SOJU;
                wantCategories[1] = Category.INDEX_BEER;
                wantCategories[2] = Category.INDEX_SNACK;
                wantCategories[3] = Category.INDEX_SNACK;
                wantCategories[4] = Category.INDEX_DRINK;
                wantCategories[5] = Category.INDEX_ICECREAM;

                wantAmounts[0] = 1 + Util.rand(2);           // 소주 (필수) 1~2개
                wantAmounts[1] = 1 + Util.rand(2);           // 맥주 (필수) 1~2개
                wantAmounts[2] = 1 + Util.rand(2);           // 안주1 (필수) 1~2개
                wantAmounts[3] = maybeBuy(1 + Util.rand(2)); // 안주2 (선택 50%) 0~2개
                wantAmounts[4] = maybeBuy(1);                 // 음료 (선택 50%) 0~1개
                wantAmounts[5] = maybeBuy(1);                 // 아이스크림 (선택 50%) 0~1개
                break;

            case TYPE_FRIENDS:
                // 친구들: 맥주 + 소주 + 안주 (필수) / 아이스크림, 폭죽 (선택 50%)
                wantCategories[0] = Category.INDEX_BEER;
                wantCategories[1] = Category.INDEX_SOJU;
                wantCategories[2] = Category.INDEX_SNACK;
                wantCategories[3] = Category.INDEX_SNACK;
                wantCategories[4] = Category.INDEX_ICECREAM;
                wantCategories[5] = Category.INDEX_ICECREAM;
                wantCategories[6] = Category.INDEX_FIREWORK;

                wantAmounts[0] = 6 + Util.rand(5);           // 맥주 (필수) - 많이
                wantAmounts[1] = 3 + Util.rand(3);           // 소주 (필수)
                wantAmounts[2] = 2 + Util.rand(2);           // 안주1 (필수)
                wantAmounts[3] = 1 + Util.rand(2);           // 안주2 (필수)
                wantAmounts[4] = maybeBuy(2 + Util.rand(2)); // 아이스크림1 (선택 50%)
                wantAmounts[5] = maybeBuy(1 + Util.rand(2)); // 아이스크림2 (선택 50%)
                wantAmounts[6] = maybeBuy(2 + Util.rand(3)); // 폭죽 (선택 50%)
                break;

            default:
                // 혼자: 라면 + 맥주 (필수) / 음료, 아이스크림, 안주 (선택 50%)
                wantCategories[0] = Category.INDEX_RAMEN;
                wantCategories[1] = Category.INDEX_BEER;
                wantCategories[2] = Category.INDEX_DRINK;
                wantCategories[3] = Category.INDEX_ICECREAM;
                wantCategories[4] = Category.INDEX_SNACK;

                wantAmounts[0] = 1 + Util.rand(2);           // 라면 (필수) 1~2개
                wantAmounts[1] = 1 + Util.rand(2);           // 맥주 (필수) 1~2개
                wantAmounts[2] = maybeBuy(1);                 // 음료 (선택 50%) 0~1개
                wantAmounts[3] = maybeBuy(1);                 // 아이스크림 (선택 50%) 0~1개
                wantAmounts[4] = maybeBuy(1);                 // 안주 (선택 50%) 0~1개
                break;
        }
    }

    /// <summary>
    /// 50% 확률로 구매 (선택 카테고리용)
    /// 50% 확률로 원래 수량 반환, 50% 확률로 0 반환
    /// </summary>
    private static int maybeBuy(int amount) {
        if (Util.rand(2) == 0) {
            return 0;       // 안 삼
        }
        return amount;      // 삼
    }

    /// <summary>
    /// 매대에서 직접 상품을 골라 가져감 (매대 재고 차감)
    /// 재고가 부족하면 있는 만큼만 가져가고, 없으면 아쉬워함
    /// verbose=true: 쇼핑 과정 출력 (직접 영업용)
    /// verbose=false: 출력 없이 처리 (빠른 영업용)
    /// </summary>
    /// <returns>int[2] = {성공건수, 실패건수}</returns>
    public int[] pickProducts(Display display, Category[] allCategories, boolean verbose) {
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < wantCount; i++) {
            // 수량 0이면 선택 안 된 항목 (maybeBuy에서 탈락)
            if (wantAmounts[i] <= 0) {
                continue;
            }

            Category cat = allCategories[wantCategories[i]];
            Product product = pickFromDisplay(display, cat);

            // 카테고리에 재고 있는 상품이 하나도 없음
            if (product == null) {
                if (verbose) {
                    System.out.printf(" - %s 쪽은 다 떨어졌네...%n", cat.name);
                }
                wantAmounts[i] = 0;
                failCount++;
                continue;
            }

            wantProducts[i] = product;
            int stock = display.getDisplayed(product);
            int wanted = wantAmounts[i];

            if (stock >= wanted) {
                // 원하는 만큼 전부 가져감
                display.sell(product, wanted);
                successCount++;
            } else {
                // 있는 만큼만 가져감
                display.sell(product, stock);
                wantAmounts[i] = stock;
                successCount++;
                failCount++;
                if (verbose) {
                    System.out.printf(" - %s %d개 갖고 싶었는데 %d개밖에 없네...%n", product.name, wanted, stock);
                }
            }
        }

        // 고른 상품 목록 출력
        if (verbose) {
            showPickedProducts();
        }

        return new int[]{successCount, failCount};
    }

    /// <summary>
    /// 매대에서 재고 있는 상품 중 랜덤 선택
    /// 재고 있는 상품이 하나도 없으면 null 반환
    /// </summary>
    private static Product pickFromDisplay(Display display, Category cat) {
        // 매대에 재고 있는 상품 개수 세기
        int count = 0;
        for (Product p : cat.products) {
            if (display.getDisplayed(p) > 0) {
                count++;
            }
        }

        // 재고 있는 상품이 없음
        if (count == 0) {
            return null;
        }

        // 재고 있는 상품 중 랜덤 선택
        int target = Util.rand(count);
        int idx = 0;
        for (Product p : cat.products) {
            if (display.getDisplayed(p) > 0) {
                if (idx == target) {
                    return p;
                }
                idx++;
            }
        }

        return null;
    }

    /// <summary>
    /// 인사말 출력
    /// </summary>
    public void sayGreeting() {
        System.out.println("\"" + greeting + "\"");
    }

    /// <summary>
    /// 매대에서 고른 상품 목록 출력
    /// </summary>
    public void showPickedProducts() {
        System.out.println("고른 상품:");
        for (int i = 0; i < wantCount; i++) {
            if (wantAmounts[i] > 0 && wantProducts[i] != null) {
                System.out.println(" - " + wantProducts[i].name + " " + wantAmounts[i] + "개");
            }
        }
    }
}
