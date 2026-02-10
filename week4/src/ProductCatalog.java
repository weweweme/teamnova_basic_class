import java.util.HashMap;
import java.util.Map;

/// <summary>
/// 상품 카탈로그 (물류 관리 시스템)
/// 게임에서 사용하는 모든 상품과 카테고리를 생성하고 조회 기능 제공
/// </summary>
public class ProductCatalog {

    // ========== 상품 객체 ==========

    // 음료
    public Product cola;
    public Product cider;
    public Product water;
    public Product pocari;
    public Product ipro;
    public Product fanta;
    public Product milkis;

    // 맥주
    public Product cass;
    public Product terra;
    public Product hite;
    public Product kloud;
    public Product filgood;

    // 소주
    public Product chamisul;
    public Product cheumcherum;
    public Product jinro;
    public Product goodday;
    public Product saero;

    // 간식/안주
    public Product driedSquid;
    public Product peanut;
    public Product chip;
    public Product jerky;
    public Product sausageSnack;

    // 고기
    public Product samgyupsal;
    public Product moksal;
    public Product sausage;
    public Product galbi;
    public Product hangjeongsal;

    // 해수욕 용품
    public Product tube;
    public Product sunscreen;
    public Product beachBall;
    public Product goggles;
    public Product waterGun;

    // 식재료
    public Product ssamjang;
    public Product lettuce;
    public Product kimchi;
    public Product onion;
    public Product salt;

    // 라면
    public Product shinRamen;
    public Product jinRamen;
    public Product neoguri;
    public Product buldak;
    public Product chapagetti;

    // 아이스크림
    public Product melona;
    public Product screwBar;
    public Product fishBread;
    public Product jewelBar;
    public Product watermelonBar;

    // 기타 (폭죽)
    public Product sparkler;      // 불꽃막대
    public Product romanCandle;   // 로만캔들
    public Product fountain;      // 분수폭죽
    public Product fireworkSet;   // 폭죽세트
    public Product smokeBomb;     // 연막탄

    // ========== 카테고리 ==========

    public Category categoryDrink;      // 음료
    public Category categoryBeer;       // 맥주
    public Category categorySoju;       // 소주
    public Category categorySnack;      // 안주
    public Category categoryMeat;       // 고기
    public Category categoryBeach;      // 해수욕용품
    public Category categoryGrocery;    // 식재료
    public Category categoryRamen;      // 라면
    public Category categoryIcecream;   // 아이스크림
    public Category categoryFirework;   // 폭죽

    // 전체 카테고리 배열 (순회용)
    public Category[] allCategories;

    // 전체 상품 배열 (순회용, 중복 없음)
    public Product[] allProducts;

    // 상품 이름 → 상품 객체 맵 (O(1) 조회)
    private Map<String, Product> productMap;

    // ========== 생성자 ==========

    /// <summary>
    /// ProductCatalog 생성자
    /// 배율을 적용하여 모든 상품과 카테고리를 초기화
    /// </summary>
    public ProductCatalog(int priceMultiplier) {
        initProducts(priceMultiplier);
        initProductMap();
    }

    // ========== 조회 메서드 ==========

    /// <summary>
    /// 상품명(또는 별칭)으로 상품 조회
    /// 없으면 null 반환
    /// </summary>
    public Product getProductByName(String name) {
        return productMap.get(name);
    }

    /// <summary>
    /// 카테고리 선택 메뉴 출력
    /// allCategories의 이름을 사용하여 동적으로 메뉴 생성
    /// </summary>
    public void printCategoryMenu() {
        for (int i = 0; i < allCategories.length; i++) {
            // 3개씩 한 줄에 출력
            System.out.printf("[%d] %-8s", i + 1, allCategories[i].name);
            if ((i + 1) % 3 == 0) {
                System.out.println();
            }
        }
        // 3의 배수가 아닌 경우 줄바꿈
        if (allCategories.length % 3 != 0) {
            System.out.println();
        }
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");
    }

    /// <summary>
    /// 카테고리에서 랜덤 상품 1개 선택
    /// </summary>
    public Product getRandomFromCategory(Category category) {
        int index = Util.rand(category.products.length);
        return category.products[index];
    }

    // ========== 초기화 메서드 ==========

    /// <summary>
    /// 상품 초기화
    /// 배율을 적용하여 상품 객체 생성 후 카테고리에 묶음
    /// </summary>
    private void initProducts(int priceMultiplier) {
        // 음료 (1박스 = 24개)
        cola = new Product("코카콜라", 800, 1500 * priceMultiplier, 7, 24);
        cider = new Product("칠성사이다", 800, 1500 * priceMultiplier, 6, 24);
        water = new Product("삼다수", 400, 1000 * priceMultiplier, 5, 24);
        pocari = new Product("포카리스웨트", 1200, 2000 * priceMultiplier, 6, 24);
        ipro = new Product("이프로", 1000, 1800 * priceMultiplier, 5, 24);
        fanta = new Product("환타", 900, 1600 * priceMultiplier, 6, 24);
        milkis = new Product("밀키스", 900, 1600 * priceMultiplier, 5, 24);

        // 맥주 (1박스 = 24개)
        cass = new Product("카스", 1500, 3000 * priceMultiplier, 8, 24);
        terra = new Product("테라", 1600, 3200 * priceMultiplier, 8, 24);
        hite = new Product("하이트", 1400, 2800 * priceMultiplier, 7, 24);
        kloud = new Product("클라우드", 1700, 3400 * priceMultiplier, 7, 24);
        filgood = new Product("필굿", 1300, 2600 * priceMultiplier, 6, 24);

        // 소주 (1박스 = 20병)
        chamisul = new Product("참이슬", 1200, 2500 * priceMultiplier, 9, 20);
        cheumcherum = new Product("처음처럼", 1200, 2500 * priceMultiplier, 8, 20);
        jinro = new Product("진로", 1300, 2600 * priceMultiplier, 7, 20);
        goodday = new Product("좋은데이", 1100, 2400 * priceMultiplier, 7, 20);
        saero = new Product("새로", 1200, 2500 * priceMultiplier, 8, 20);

        // 간식/안주 (1박스 = 20개)
        driedSquid = new Product("마른오징어", 3000, 6000 * priceMultiplier, 6, 20);
        peanut = new Product("땅콩", 2000, 4000 * priceMultiplier, 5, 20);
        chip = new Product("감자칩", 1500, 3000 * priceMultiplier, 6, 20);
        jerky = new Product("육포", 4000, 8000 * priceMultiplier, 7, 20);
        sausageSnack = new Product("소시지안주", 2500, 5000 * priceMultiplier, 5, 20);

        // 고기 (1판 = 10팩)
        samgyupsal = new Product("삼겹살", 8000, 15000 * priceMultiplier, 10, 10);
        moksal = new Product("목살", 9000, 16000 * priceMultiplier, 9, 10);
        sausage = new Product("소세지", 3000, 6000 * priceMultiplier, 7, 10);
        galbi = new Product("갈비", 12000, 22000 * priceMultiplier, 9, 10);
        hangjeongsal = new Product("항정살", 10000, 18000 * priceMultiplier, 8, 10);

        // 해수욕 용품 (1묶음 = 5개)
        tube = new Product("튜브", 5000, 15000 * priceMultiplier, 7, 5);
        sunscreen = new Product("선크림", 8000, 20000 * priceMultiplier, 8, 5);
        beachBall = new Product("비치볼", 3000, 8000 * priceMultiplier, 5, 5);
        goggles = new Product("수경", 4000, 12000 * priceMultiplier, 6, 5);
        waterGun = new Product("물총", 3000, 10000 * priceMultiplier, 7, 5);

        // 식재료 (1박스 = 10개)
        ssamjang = new Product("쌈장", 2000, 4000 * priceMultiplier, 6, 10);
        lettuce = new Product("상추", 2000, 4000 * priceMultiplier, 7, 10);
        kimchi = new Product("김치", 3000, 6000 * priceMultiplier, 5, 10);
        onion = new Product("양파", 1500, 3000 * priceMultiplier, 5, 10);
        salt = new Product("소금", 1000, 2000 * priceMultiplier, 4, 10);

        // 라면 (1박스 = 40개)
        shinRamen = new Product("신라면", 800, 1500 * priceMultiplier, 8, 40);
        jinRamen = new Product("진라면", 700, 1400 * priceMultiplier, 7, 40);
        neoguri = new Product("너구리", 800, 1500 * priceMultiplier, 6, 40);
        buldak = new Product("불닭볶음면", 900, 1700 * priceMultiplier, 8, 40);
        chapagetti = new Product("짜파게티", 800, 1500 * priceMultiplier, 7, 40);

        // 아이스크림 (1박스 = 24개)
        melona = new Product("메로나", 500, 1200 * priceMultiplier, 7, 24);
        screwBar = new Product("스크류바", 600, 1300 * priceMultiplier, 6, 24);
        fishBread = new Product("붕어싸만코", 800, 1500 * priceMultiplier, 6, 24);
        jewelBar = new Product("보석바", 500, 1200 * priceMultiplier, 6, 24);
        watermelonBar = new Product("수박바", 500, 1200 * priceMultiplier, 7, 24);

        // 기타 - 폭죽 (1박스 = 10개)
        sparkler = new Product("불꽃막대", 3000, 8000 * priceMultiplier, 8, 10);
        romanCandle = new Product("로만캔들", 5000, 15000 * priceMultiplier, 9, 10);
        fountain = new Product("분수폭죽", 7000, 20000 * priceMultiplier, 8, 10);
        fireworkSet = new Product("폭죽세트", 10000, 25000 * priceMultiplier, 9, 10);
        smokeBomb = new Product("연막탄", 4000, 10000 * priceMultiplier, 7, 10);

        // 카테고리 초기화 (이름, 박스단위, 상품배열, 인덱스)
        categoryDrink = new Category("음료", "1박스=24개", new Product[]{cola, cider, water, pocari, ipro, fanta, milkis}, 0);
        categoryBeer = new Category("맥주", "1박스=24개", new Product[]{cass, terra, hite, kloud, filgood}, 1);
        categorySoju = new Category("소주", "1박스=20병", new Product[]{chamisul, cheumcherum, jinro, goodday, saero}, 2);
        categorySnack = new Category("간식/안주", "1박스=20개", new Product[]{driedSquid, peanut, chip, jerky, sausageSnack}, 3);
        categoryMeat = new Category("고기", "1판=10팩", new Product[]{samgyupsal, moksal, sausage, galbi, hangjeongsal}, 4);
        categoryBeach = new Category("해수욕용품", "1묶음=5개", new Product[]{tube, sunscreen, beachBall, goggles, waterGun}, 5);
        categoryGrocery = new Category("식재료", "1박스=10개", new Product[]{ssamjang, lettuce, kimchi, onion, salt}, 6);
        categoryRamen = new Category("라면", "1박스=40개", new Product[]{shinRamen, jinRamen, neoguri, buldak, chapagetti}, 7);
        categoryIcecream = new Category("아이스크림", "1박스=24개", new Product[]{melona, screwBar, fishBread, jewelBar, watermelonBar}, 8);
        categoryFirework = new Category("폭죽", "1박스=10개", new Product[]{sparkler, romanCandle, fountain, fireworkSet, smokeBomb}, 9);

        // 전체 카테고리 배열 초기화 (순회용)
        allCategories = new Category[]{
            categoryDrink, categoryBeer, categorySoju, categorySnack, categoryMeat,
            categoryBeach, categoryGrocery, categoryRamen, categoryIcecream, categoryFirework
        };

        // 전체 상품 배열 초기화 (순회용)
        allProducts = new Product[]{
            cola, cider, water, pocari, ipro, fanta, milkis,
            cass, terra, hite, kloud, filgood,
            chamisul, cheumcherum, jinro, goodday, saero,
            driedSquid, peanut, chip, jerky, sausageSnack,
            samgyupsal, moksal, sausage, galbi, hangjeongsal,
            tube, sunscreen, beachBall, goggles, waterGun,
            ssamjang, lettuce, kimchi, onion, salt,
            shinRamen, jinRamen, neoguri, buldak, chapagetti,
            melona, screwBar, fishBread, jewelBar, watermelonBar,
            sparkler, romanCandle, fountain, fireworkSet, smokeBomb
        };
    }

    /// <summary>
    /// 상품 이름 맵 초기화
    /// 상품명과 별칭을 모두 등록하여 productMap.get()으로 O(1) 조회 가능
    /// </summary>
    private void initProductMap() {
        productMap = new HashMap<>();

        // 음료
        productMap.put("코카콜라", cola);
        productMap.put("콜라", cola);
        productMap.put("칠성사이다", cider);
        productMap.put("사이다", cider);
        productMap.put("삼다수", water);
        productMap.put("물", water);
        productMap.put("포카리스웨트", pocari);
        productMap.put("포카리", pocari);
        productMap.put("이프로", ipro);
        productMap.put("환타", fanta);
        productMap.put("밀키스", milkis);

        // 맥주
        productMap.put("카스", cass);
        productMap.put("테라", terra);
        productMap.put("하이트", hite);
        productMap.put("클라우드", kloud);
        productMap.put("필굿", filgood);

        // 소주
        productMap.put("참이슬", chamisul);
        productMap.put("처음처럼", cheumcherum);
        productMap.put("진로", jinro);
        productMap.put("좋은데이", goodday);
        productMap.put("새로", saero);

        // 간식/안주
        productMap.put("마른오징어", driedSquid);
        productMap.put("오징어", driedSquid);
        productMap.put("땅콩", peanut);
        productMap.put("감자칩", chip);
        productMap.put("과자", chip);
        productMap.put("칩", chip);
        productMap.put("육포", jerky);
        productMap.put("소시지안주", sausageSnack);

        // 고기
        productMap.put("삼겹살", samgyupsal);
        productMap.put("목살", moksal);
        productMap.put("소세지", sausage);
        productMap.put("갈비", galbi);
        productMap.put("항정살", hangjeongsal);

        // 해수욕 용품
        productMap.put("튜브", tube);
        productMap.put("선크림", sunscreen);
        productMap.put("비치볼", beachBall);
        productMap.put("수경", goggles);
        productMap.put("물총", waterGun);

        // 식재료
        productMap.put("쌈장", ssamjang);
        productMap.put("상추", lettuce);
        productMap.put("김치", kimchi);
        productMap.put("양파", onion);
        productMap.put("소금", salt);

        // 라면
        productMap.put("신라면", shinRamen);
        productMap.put("진라면", jinRamen);
        productMap.put("너구리", neoguri);
        productMap.put("불닭볶음면", buldak);
        productMap.put("불닭", buldak);
        productMap.put("짜파게티", chapagetti);

        // 아이스크림
        productMap.put("메로나", melona);
        productMap.put("스크류바", screwBar);
        productMap.put("붕어싸만코", fishBread);
        productMap.put("붕어", fishBread);
        productMap.put("보석바", jewelBar);
        productMap.put("수박바", watermelonBar);

        // 폭죽
        productMap.put("불꽃막대", sparkler);
        productMap.put("로만캔들", romanCandle);
        productMap.put("분수폭죽", fountain);
        productMap.put("폭죽세트", fireworkSet);
        productMap.put("연막탄", smokeBomb);
    }
}
