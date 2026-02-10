/// <summary>
/// 카테고리 클래스
/// 상품 카테고리의 이름, 상품 목록, 자동주문 정책을 묶어 관리
/// </summary>
public class Category {

    // ========== 카테고리 인덱스 상수 ==========

    public static final int INDEX_DRINK = 0;      // 음료
    public static final int INDEX_BEER = 1;       // 맥주
    public static final int INDEX_SOJU = 2;       // 소주
    public static final int INDEX_SNACK = 3;      // 간식/안주
    public static final int INDEX_MEAT = 4;       // 고기
    public static final int INDEX_BEACH = 5;      // 해수욕용품
    public static final int INDEX_GROCERY = 6;    // 식재료
    public static final int INDEX_RAMEN = 7;      // 라면
    public static final int INDEX_ICECREAM = 8;   // 아이스크림
    public static final int INDEX_FIREWORK = 9;   // 폭죽

    // ========== 필드 ==========

    public String name;               // 카테고리명 ("음료", "맥주" 등)
    public String boxUnit;            // 박스 단위 표시 ("1박스=24개" 등, 도매상 UI용)
    public Product[] products;        // 카테고리 내 상품 배열
    public int index;                 // 0-based 카테고리 인덱스

    // 자동주문 정책
    public boolean autoOrderEnabled;  // 자동주문 활성화 여부 (기본 true)
    public int autoOrderThreshold;    // 자동주문 임계값 (재고가 이 값 이하면 주문, 기본 10)
    public int autoOrderBoxCount;     // 자동주문 박스 수 (기본 3)

    // ========== 생성자 ==========

    /// <summary>
    /// Category 생성자
    /// 자동주문은 기본 활성화 (임계값 10)
    /// </summary>
    public Category(String name, String boxUnit, Product[] products, int index) {
        this.name = name;
        this.boxUnit = boxUnit;
        this.products = products;
        this.index = index;
        this.autoOrderEnabled = true;
        this.autoOrderThreshold = 10;
        this.autoOrderBoxCount = 3;
    }

    // ========== 메서드 ==========

    /// <summary>
    /// 번호(1-based)로 상품 찾기
    /// 유효하지 않은 번호면 null 반환
    /// </summary>
    public Product getProductByNum(int num) {
        if (num < 1 || num > products.length) {
            return null;
        }
        return products[num - 1];
    }
}
