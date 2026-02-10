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
