import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

/// <summary>
/// 마켓(슈퍼마켓) 클래스
/// 게임의 모든 진행 로직을 담당 (도매상, 영업, 정산, 재고 관리 등)
/// Main에서 생성하여 run()으로 게임 시작
/// </summary>
public class Market {

    // ========== 상수 ==========

    static final int MAX_SLOT = 30;                   // 매대 최대 슬롯 (상품 50개 중 30개만 진열 가능)
    static final int MAX_DISPLAY_PER_SLOT = 15;      // 슬롯당 최대 진열 수량
    static final int MIN_SLOT_FOR_BUSINESS = 15;     // 영업 시작 최소 슬롯 (50%)
    static final int AUTO_ORDER_BOX_COUNT = 3;       // 자동주문 시 박스 수 (약 1주일치)

    // 시간대 상수
    static final int TIME_MORNING = 0;    // 아침 (도매상 이용 가능)
    static final int TIME_AFTERNOON = 1;  // 낮 (영업 전반부)
    static final int TIME_NIGHT = 2;      // 밤 (영업 후반부)

    // ========== 게임 변수 ==========

    int money;
    int goalMoney;
    int priceMultiplier;
    int day = 1;
    int timeOfDay = TIME_MORNING;  // 현재 시간대

    Scanner scanner;

    // ========== 창고와 매대 ==========

    Warehouse warehouse;  // 창고 (상품 재고 관리)
    Display display;      // 매대 (상품 진열 관리)

    // ========== 카테고리별 자동주문 정책 ==========
    // Category 클래스의 autoOrderEnabled, autoOrderThreshold로 관리

    // ========== 상품 객체 ==========
    // initProducts()에서 초기화

    // 음료
    Product cola;
    Product cider;
    Product water;
    Product pocari;
    Product ipro;
    Product fanta;
    Product milkis;

    // 맥주
    Product cass;
    Product terra;
    Product hite;
    Product kloud;
    Product filgood;

    // 소주
    Product chamisul;
    Product cheumcherum;
    Product jinro;
    Product goodday;
    Product saero;

    // 간식/안주
    Product driedSquid;
    Product peanut;
    Product chip;
    Product jerky;
    Product sausageSnack;

    // 고기
    Product samgyupsal;
    Product moksal;
    Product sausage;
    Product galbi;
    Product hangjeongsal;

    // 해수욕 용품
    Product tube;
    Product sunscreen;
    Product beachBall;
    Product goggles;
    Product waterGun;

    // 식재료
    Product ssamjang;
    Product lettuce;
    Product kimchi;
    Product onion;
    Product salt;

    // 라면
    Product shinRamen;
    Product jinRamen;
    Product neoguri;
    Product buldak;
    Product chapagetti;

    // 아이스크림
    Product melona;
    Product screwBar;
    Product fishBread;
    Product jewelBar;
    Product watermelonBar;

    // 기타 (폭죽)
    Product sparkler;      // 불꽃막대
    Product romanCandle;   // 로만캔들
    Product fountain;      // 분수폭죽
    Product fireworkSet;   // 폭죽세트
    Product smokeBomb;     // 연막탄

    // ========== 카테고리별 상품 배열 ==========
    // initProducts() 이후에 초기화됨

    Category categoryDrink;      // 음료
    Category categoryBeer;       // 맥주
    Category categorySoju;       // 소주
    Category categorySnack;      // 안주
    Category categoryMeat;       // 고기
    Category categoryBeach;      // 해수욕용품
    Category categoryGrocery;    // 식재료
    Category categoryRamen;      // 라면
    Category categoryIcecream;   // 아이스크림
    Category categoryFirework;   // 폭죽

    // 전체 카테고리 배열 (순회용)
    Category[] allCategories;

    // ========== 상품 이름 → 상품 객체 맵 ==========
    // productMap.get(name)으로 O(1) 조회
    Map<String, Product> productMap;

    // ========== 전체 상품 배열 ==========
    // 모든 상품을 순회할 때 사용 (중복 없음)
    Product[] allProducts;

    // ========== 재사용 배열 (메서드 내 반복 할당 방지) ==========

    // autoArrangeDisplay()용 - 카테고리별 진열 대기 상품 버퍼
    Product[][] arrangeCategoriesBuffer = new Product[10][7];  // [카테고리][상품] - 창고→매대 진열 대기 버퍼
    int[] arrangeCategoryCounts = new int[10];                 // 각 카테고리별 버퍼 내 상품 수
    int[] arrangeCategoryIndex = new int[10];                  // 라운드 로빈 진열 시 현재 인덱스

    // 스킵 영업용 - 직접 영업 중 "남은 손님 스킵" 선택 시
    Product[] skipList = new Product[2];       // 스킵 처리 시 간략화된 구매 목록
    int[] skipAmounts = new int[2];            // 스킵 처리 시 구매 수량

    // getAvailableFromCategory()용 - 재고 있는 상품 필터링
    Product[] availableProducts = new Product[7];  // 카테고리 내 재고 있는 상품 임시 저장 (최대 7개 - 음료)

    // ========== 손님 멘트 배열 ==========
    // [손님유형][다양한 멘트] - 4종류 × 5개

    String[][] customerGreetings = {
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

    // 시간대별 멘트 - timeOfDay에 따라 다른 배열 사용

    String[] morningGreetings = {
        "아침부터 열일하시네요!",
        "아침 일찍 오셨네요~",
        "오전에 미리 사두려고요!",
        "아침밥 준비하러 왔어요~",
        "일찍 나왔더니 기분 좋네요!"
    };

    String[] afternoonGreetings = {
        "점심 준비하러 왔어요~",
        "오후에 먹으려고요!",
        "낮이라 사람 많네요~",
        "한낮에 장보러 왔어요!",
        "오후에 뭐 좀 사려고요~"
    };

    String[] nightGreetings = {
        "저녁에 다 같이 먹을 거예요~",
        "밤에 야식으로 먹을 거예요!",
        "저녁 준비하러 왔어요~",
        "밤바람 쐬러 나왔다가 들렀어요~",
        "야식 사러 왔어요!"
    };

    // ========== 생성자 ==========

    /// <summary>
    /// Market 생성자
    /// Main에서 설정한 게임 모드 값을 전달받음
    /// </summary>
    public Market(int money, int goalMoney, int priceMultiplier, Scanner scanner) {
        this.money = money;
        this.goalMoney = goalMoney;
        this.priceMultiplier = priceMultiplier;
        this.scanner = scanner;
    }

    // ========== 게임 시작 ==========

    /// <summary>
    /// 게임 메인 루프
    /// 상품 초기화 → 창고/매대 생성 → 게임 루프
    /// </summary>
    public void run() {
        // 상품 초기화 (배율 적용)
        initProducts();

        // 창고와 매대 초기화
        warehouse = new Warehouse();
        display = new Display(MAX_SLOT, MAX_DISPLAY_PER_SLOT);

        // ========== 게임 루프 ==========

        boolean playing = true;

        while (playing) {

            // 승리/패배 조건 체크 (게임 종료 시 true 반환)
            if (checkWinOrLose()) {
                break;
            }

            // 하루 시작 메뉴 출력
            printDailyMenu();

            int choice = Util.readInt(scanner);  // 잘못된 입력 시 게임 종료

            switch (choice) {
                case 1:
                    // 도매상
                    if (timeOfDay == TIME_MORNING) {
                        goWholesaler();
                        timeOfDay = TIME_AFTERNOON;  // 도매상 갔다오면 낮으로 전환
                    } else {
                        System.out.println("[!!] 도매상은 오전에만 이용 가능합니다.");
                    }
                    break;

                case 2:
                    // 영업 시작 전 매대 체크
                    if (display.getUsedSlots() < MIN_SLOT_FOR_BUSINESS) {
                        System.out.println();
                        System.out.println("[!!] 매대가 부족합니다!");
                        System.out.printf("    현재: %d칸 / 최소: %d칸 (50%%)%n", display.getUsedSlots(), MIN_SLOT_FOR_BUSINESS);
                        System.out.println("    도매상에서 상품을 구매하고 매대에 진열하세요.");
                        System.out.println();
                        System.out.println("아무 키나 입력하면 돌아갑니다...");
                        scanner.next();
                        break;
                    }

                    // 영업 시작 (서브메뉴)
                    int businessResult = showBusinessMenu();
                    switch (businessResult) {
                        case 1:
                            // 직접 영업
                            startBusiness();
                            advanceTime();  // 아침→낮, 낮→밤
                            break;
                        case 2:
                            // 빠른 영업
                            startQuickBusiness();
                            advanceTime();
                            break;
                        // case 0: 돌아가기 (아무것도 안 함)
                    }
                    break;

                case 3:
                    // 재고/매대 관리 (서브메뉴)
                    showInventoryMenu();
                    break;

                case 4:
                    // 다음 날로 (밤에만 가능)
                    if (timeOfDay == TIME_NIGHT) {
                        day++;
                        timeOfDay = TIME_MORNING;
                    } else {
                        System.out.println("[!!] 아직 하루가 끝나지 않았습니다.");
                    }
                    break;

                case 0:
                    playing = false;
                    break;

                default:
                    System.out.println("잘못된 입력입니다.");
                    break;
            }
        }

        System.out.println("게임을 종료합니다.");
    }

    /// <summary>
    /// 시간대를 한 단계 전진
    /// 아침 → 낮, 낮 → 밤, 밤 → 아침(다음 날)
    /// </summary>
    private void advanceTime() {
        switch (timeOfDay) {
            case TIME_MORNING:
                timeOfDay = TIME_AFTERNOON;
                break;
            case TIME_AFTERNOON:
                timeOfDay = TIME_NIGHT;
                break;
            case TIME_NIGHT:
                // 밤 영업 끝 → 다음 날 아침
                day++;
                timeOfDay = TIME_MORNING;
                break;
        }
    }

    /// <summary>
    /// 총 재고 조회 (창고 + 매대)
    /// </summary>
    int getTotalStock(Product p) {
        return warehouse.getStock(p) + display.getDisplayed(p);
    }

    /// <summary>
    /// 50% 확률로 구매 (선택 카테고리용)
    /// 50% 확률로 원래 수량 반환, 50% 확률로 0 반환
    /// </summary>
    int maybeBuy(int amount) {
        if (Util.rand(2) == 0) {
            return 0;       // 안 삼
        }
        return amount;      // 삼
    }

    /// <summary>
    /// 하루 시작 메뉴 출력
    /// </summary>
    private void printDailyMenu() {
        Util.clearScreen();
        System.out.println("========================================");
        // 시간대에 따른 표시
        switch (timeOfDay) {
            case TIME_MORNING:
                System.out.println("          [  " + day + "일차 - 아침  ]");
                break;
            case TIME_AFTERNOON:
                System.out.println("          [  " + day + "일차 - 낮  ]");
                break;
            case TIME_NIGHT:
                System.out.println("          [  " + day + "일차 - 밤  ]");
                break;
        }
        System.out.println("========================================");
        System.out.println("현재 자본: " + String.format("%,d", money) + "원");
        System.out.println("매대 현황: " + display.getUsedSlots() + " / " + MAX_SLOT + "칸");
        System.out.println();

        switch (timeOfDay) {
            case TIME_MORNING:
                // 아침: 도매상, 영업, 재고/매대 모두 가능
                System.out.println("[1] 도매상 가기 (오전 소비)");
                System.out.println("[2] 영업 시작");
                System.out.println("[3] 재고/매대 관리");
                break;
            case TIME_AFTERNOON:
                // 낮: 영업, 재고/매대만 가능
                System.out.println("[1] (도매상 마감)");
                System.out.println("[2] 영업 시작");
                System.out.println("[3] 재고/매대 관리");
                break;
            case TIME_NIGHT:
                // 밤: 영업, 재고/매대, 다음 날 가능
                System.out.println("[1] (도매상 마감)");
                System.out.println("[2] 영업 시작");
                System.out.println("[3] 재고/매대 관리");
                System.out.println("[4] 다음 날로");
                break;
        }
        System.out.println("[0] 게임 종료");
        System.out.print(">> ");
    }

    /// <summary>
    /// 영업 시작 서브메뉴
    /// 선택한 영업 타입 반환 (1: 직접, 2: 빠른, 3: 1주일 스킵, 0: 취소)
    /// </summary>
    private int showBusinessMenu() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("           [ 영업 시작 ]");
        System.out.println("========================================");
        System.out.println();
        System.out.println("[1] 직접 영업 (손님 한 명씩 응대)");
        System.out.println("[2] 빠른 영업 (결과만 요약)");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        // 잘못된 입력 시 돌아가기
        return Util.readInt(scanner);
    }

    /// <summary>
    /// 재고/매대 관리 서브메뉴
    /// </summary>
    private void showInventoryMenu() {
        boolean managing = true;

        while (managing) {
            Util.clearScreen();
            System.out.println("========================================");
            System.out.println("         [ 재고/매대 관리 ]");
            System.out.println("========================================");
            System.out.println();
            System.out.println("[1] 현재 재고 확인");
            System.out.println("[2] 매대 관리 (진열/회수)");
            System.out.println("[0] 돌아가기");
            System.out.print(">> ");

            int choice = Util.readInt(scanner);  // 잘못된 입력 시 돌아가기

            switch (choice) {
                case 1:
                    showInventory();
                    break;
                case 2:
                    manageDisplay();
                    break;
                case 0:
                    managing = false;
                    break;
            }
        }
    }

    /// <summary>
    /// 승리/패배 조건 체크
    /// 게임 종료 시 true 반환
    /// </summary>
    private boolean checkWinOrLose() {
        // 승리 조건 체크
        if (money >= goalMoney) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("         *** 축하합니다! ***");
            System.out.println("========================================");
            System.out.println("목표 금액 " + String.format("%,d", goalMoney) + "원을 달성했습니다!");
            System.out.println("이제 펜션 주인이 되셨습니다!");
            System.out.println("총 " + day + "일 만에 달성!");
            System.out.println("========================================");
            return true;
        }

        // 패배 조건 체크
        if (money <= 0) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("          *** 파산 ***");
            System.out.println("========================================");
            System.out.println("자본금이 바닥났습니다...");
            System.out.println(day + "일차에 파산했습니다.");
            System.out.println("========================================");
            return true;
        }

        return false;
    }

    /// <summary>
    /// 재고 확인
    /// </summary>
    private void showInventory() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.printf("       [ 매대 현황 ] %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
        System.out.println("========================================");
        System.out.println();

        boolean hasStock = false;
        for (Product p : allProducts) {
            int stock = display.getDisplayed(p);
            if (stock > 0) {
                printStockBar(p.name, stock);
                hasStock = true;
            }
        }

        // 매대가 비어있으면
        if (!hasStock) {
            System.out.println("  매대가 비어있습니다.");
            System.out.println("  도매상에서 상품을 입고하세요!");
        }

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    /// <summary>
    /// 매대 관리
    /// </summary>
    private void manageDisplay() {
        boolean managing = true;

        while (managing) {
            Util.clearScreen();
            System.out.println("========================================");
            System.out.println("            [ 매대 관리 ]");
            System.out.println("========================================");
            System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
            System.out.println();
            System.out.println("[1] 상품 진열 (창고 → 매대)");
            System.out.println("[2] 상품 회수 (매대 → 창고)");
            System.out.println("[3] 창고 재고 확인");
            System.out.println("[4] 자동 배정 (카테고리 균형)");
            System.out.println("[0] 돌아가기");
            System.out.print(">> ");

            int choice = Util.readInt(scanner);

            switch (choice) {
                case 1:
                    displayProduct();
                    break;
                case 2:
                    returnProduct();
                    break;
                case 3:
                    showWarehouse();
                    break;
                case 4:
                    autoArrangeDisplay();
                    break;
                case 0:
                    managing = false;
                    break;
            }
        }
    }

    /// <summary>
    /// 창고 재고 확인
    /// </summary>
    private void showWarehouse() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("           [ 창고 재고 ]");
        System.out.println("========================================");
        System.out.println();

        boolean hasStock = false;

        // 창고에 재고가 있는 상품만 출력
        for (Product p : allProducts) {
            int stock = warehouse.getStock(p);
            if (stock > 0) {
                System.out.printf("%-16s %d개%n", p.name, stock);
                hasStock = true;
            }
        }

        if (!hasStock) {
            System.out.println("  창고가 비어있습니다.");
        }

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    /// <summary>
    /// 상품 진열 (창고 → 매대)
    /// </summary>
    private void displayProduct() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("        [ 상품 진열 ] 창고 → 매대");
        System.out.println("========================================");
        System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
        System.out.println();

        // 매대가 꽉 찼으면 경고
        if (!display.hasEmptySlot()) {
            System.out.println("[!!] 매대가 꽉 찼습니다! (이미 진열된 상품만 추가 가능)");
            System.out.println();
        }

        // 창고에 재고가 있는 상품 목록 출력
        // 이미 진열 중인 상품은 [진열중] 표시
        // 번호 → 상품 매핑용 배열 (번호로 선택하기 위해)
        Product[] stockList = new Product[allProducts.length];
        System.out.println("--- 창고 재고 ---");
        int num = 0;
        for (Product p : allProducts) {
            int stock = warehouse.getStock(p);
            if (stock > 0) {
                stockList[num] = p;
                num++;
                int displayed = display.getDisplayed(p);
                if (displayed > 0) {
                    // 이미 매대에 진열 중인 상품
                    System.out.printf("%d. %s (창고: %d개) [진열중 %d/%d]%n",
                        num, p.name, stock, displayed, MAX_DISPLAY_PER_SLOT);
                } else {
                    System.out.printf("%d. %s (창고: %d개)%n", num, p.name, stock);
                }
            }
        }

        if (num == 0) {
            System.out.println("  창고가 비어있습니다.");
            System.out.println();
            System.out.println("아무 키나 입력하면 돌아갑니다...");
            scanner.next();
            return;
        }

        System.out.println();
        System.out.print("진열할 상품 번호 입력 (취소: 0): ");
        int productNum = Util.readInt(scanner);

        if (productNum == 0 || productNum == Util.INVALID_INPUT) {
            return;
        }

        // 번호 범위 체크
        if (productNum < 1 || productNum > num) {
            System.out.println("[!!] 잘못된 번호입니다.");
            return;
        }

        // 번호로 상품 찾기 (1번 → stockList[0])
        Product product = stockList[productNum - 1];
        int warehouseStock = warehouse.getStock(product);

        // 새 상품이면 슬롯 체크
        int currentDisplay = display.getDisplayed(product);
        boolean isNewOnDisplay = (currentDisplay == 0);
        if (isNewOnDisplay && !display.hasEmptySlot()) {
            System.out.println("[!!] 매대 공간이 부족합니다. (" + display.getUsedSlots() + "/" + MAX_SLOT + "칸)");
            return;
        }

        // 매대에 진열 가능한 최대 수량 계산
        int maxCanDisplay = MAX_DISPLAY_PER_SLOT - currentDisplay;
        if (maxCanDisplay <= 0) {
            System.out.printf("[!!] 매대가 가득 찼습니다. (최대 %d개)%n", MAX_DISPLAY_PER_SLOT);
            return;
        }

        System.out.printf("수량 입력 (창고: %d개, 진열가능: %d개, 전체: a): ", warehouseStock, maxCanDisplay);
        String amountStr = scanner.next();

        int amount;
        if (amountStr.equals("a") || amountStr.equals("A")) {
            amount = Math.min(warehouseStock, maxCanDisplay);
        } else {
            amount = Integer.parseInt(amountStr);
        }

        if (amount <= 0) {
            return;
        }

        // 슬롯당 최대 수량 제한 적용
        if (amount > maxCanDisplay) {
            System.out.printf("[!!] 매대 공간 제한으로 %d개만 진열합니다. (최대 %d개)%n", maxCanDisplay, MAX_DISPLAY_PER_SLOT);
            amount = maxCanDisplay;
        }

        // 진열 처리 (Display 클래스가 슬롯과 재고를 자동 관리)
        int displayed = display.displayFromWarehouse(product, warehouse, amount);

        System.out.printf("[OK] %s %d개 매대에 진열! (매대: %d/%d개)%n", product.name, displayed, display.getDisplayed(product), MAX_DISPLAY_PER_SLOT);
    }

    /// <summary>
    /// 상품 회수 (매대 → 창고)
    /// </summary>
    private void returnProduct() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("        [ 상품 회수 ] 매대 → 창고");
        System.out.println("========================================");
        System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
        System.out.println();

        // 매대에 진열된 상품 목록 출력
        System.out.println("--- 매대 재고 ---");
        int num = 1;
        for (Product p : allProducts) {
            int stock = display.getDisplayed(p);
            if (stock > 0) {
                System.out.printf("%d. %s (%d개)%n", num++, p.name, stock);
            }
        }

        if (num == 1) {
            System.out.println("  매대가 비어있습니다.");
            System.out.println();
            System.out.println("아무 키나 입력하면 돌아갑니다...");
            scanner.next();
            return;
        }

        System.out.println();
        System.out.print("회수할 상품명 입력 (취소: 0): ");
        String productName = scanner.next();

        if (productName.equals("0")) {
            return;
        }

        // 상품 찾기
        Product product = productMap.get(productName);

        if (product == null) {
            System.out.println("[!!] 상품을 찾을 수 없습니다.");
            return;
        }

        int currentDisplay = display.getDisplayed(product);
        if (currentDisplay == 0) {
            System.out.println("[!!] 매대에 재고가 없습니다.");
            return;
        }

        System.out.printf("수량 입력 (매대: %d개, 전체: a): ", currentDisplay);
        String amountStr = scanner.next();

        int amount;
        if (amountStr.equals("a") || amountStr.equals("A")) {
            amount = currentDisplay;
        } else {
            amount = Integer.parseInt(amountStr);
        }

        if (amount <= 0) {
            return;
        }

        // 회수 처리 (Display 클래스가 슬롯과 재고를 자동 관리)
        int returned = display.returnToWarehouse(product, warehouse, amount);

        System.out.printf("[OK] %s %d개 창고로 회수!%n", product.name, returned);
    }

    /// <summary>
    /// 자동 배정 (카테고리 균형)
    /// </summary>
    private void autoArrangeDisplay() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("      [ 자동 배정 - 카테고리 균형 ]");
        System.out.println("========================================");
        System.out.printf("현재 매대: %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
        System.out.println();

        int refilledCount = 0;   // 보충한 상품 수
        int newDisplayCount = 0; // 새로 진열한 상품 수

        // ========== 1단계: 이미 매대에 있는 상품 보충 ==========
        System.out.println("[ 1단계: 기존 상품 보충 ]");

        // 모든 상품을 순회하며 매대에 있고 창고에 재고가 있는 경우 보충
        for (Product p : allProducts) {
            int displayStock = display.getDisplayed(p);
            int warehouseStock = warehouse.getStock(p);

            boolean isOnDisplay = displayStock > 0;                          // 매대에 진열 중인가?
            boolean hasRoomOnDisplay = displayStock < MAX_DISPLAY_PER_SLOT;  // 매대에 더 놓을 공간이 있는가?
            boolean hasWarehouseStock = warehouseStock > 0;                  // 창고에 재고가 있는가?

            if (isOnDisplay && hasRoomOnDisplay && hasWarehouseStock) {
                int displayed = display.displayFromWarehouse(p, warehouse, MAX_DISPLAY_PER_SLOT);
                System.out.printf(" - %s +%d개 보충 (매대: %d/%d)%n", p.name, displayed, display.getDisplayed(p), MAX_DISPLAY_PER_SLOT);
                refilledCount++;
            }
        }

        if (refilledCount == 0) {
            System.out.println(" (보충할 상품 없음)");
        }

        System.out.println();

        // ========== 2단계: 빈 슬롯에 새 상품 배정 ==========
        System.out.println("[ 2단계: 새 상품 배정 ]");

        int remainingSlots = MAX_SLOT - display.getUsedSlots();
        if (remainingSlots <= 0) {
            System.out.println(" (매대가 꽉 찼습니다)");

        } else {
            // ──────────────────────────────────────────────
            // [준비] 카테고리별 진열 대기 목록 만들기
            // ──────────────────────────────────────────────
            // 조건: 창고에 재고가 있고(O) + 매대에 아직 없는(X) 상품
            //
            // fillArrangeBuffer()가 각 카테고리를 순회하며 조건에 맞는 상품을
            // arrangeCategoriesBuffer 2차원 배열에 채운다
            //
            // 결과 예시:
            //   arrangeCategoriesBuffer[0] = [환타, 밀키스]  ← 음료 중 매대에 없는 것
            //   arrangeCategoriesBuffer[1] = [클라우드]      ← 맥주 중 매대에 없는 것
            //   arrangeCategoriesBuffer[2] = []              ← 소주는 전부 매대에 있음
            //   ...
            //   arrangeCategoryCounts[0] = 2  ← 음료 대기 상품 2개
            //   arrangeCategoryCounts[1] = 1  ← 맥주 대기 상품 1개
            //   arrangeCategoryCounts[2] = 0  ← 소주 대기 상품 0개
            int totalAvailable = 0;
            for (int i = 0; i < 10; i++) {
                arrangeCategoryCounts[i] = 0;   // 버퍼 초기화
                arrangeCategoryIndex[i] = 0;    // 진열 순서 인덱스 초기화
                fillArrangeBuffer(i, allCategories[i]);
                totalAvailable = totalAvailable + arrangeCategoryCounts[i];
            }

            if (totalAvailable == 0) {
                System.out.println(" (창고에 새로 진열할 상품 없음)");
            } else {
                // 한 카테고리에 몰아서 진열하지 않고, 돌아가며 1개씩 진열한다
                // → 매대 30칸을 특정 카테고리가 독점하는 것을 방지
                //
                // 동작 순서 (매대 빈 슬롯이 5칸인 경우):
                //   1바퀴: 음료(환타) → 맥주(클라우드) → 간식(육포) → 라면(불닭) → 아이스크림(보석바) → 5칸 다 참 → 종료
                //
                // arrangeCategoryIndex[cat]가 각 카테고리에서 "다음에 진열할 상품" 위치를 추적
                // 모든 카테고리의 인덱스가 끝까지 도달하면 hasMore = false → while 종료
                boolean hasMore = true;

                while (hasMore && display.hasEmptySlot()) {
                    hasMore = false;  // 이번 바퀴에서 진열한 게 없으면 종료

                    // 10개 카테고리를 순서대로 순회 (1바퀴)
                    for (int cat = 0; cat < 10; cat++) {

                        // 이 카테고리에 아직 진열할 상품이 남아있는가?
                        if (arrangeCategoryIndex[cat] < arrangeCategoryCounts[cat]) {

                            // 매대가 중간에 꽉 찰 수 있으므로 매번 체크
                            if (!display.hasEmptySlot()) {
                                break;
                            }

                            // 현재 인덱스 위치의 상품을 매대에 진열
                            Product product = arrangeCategoriesBuffer[cat][arrangeCategoryIndex[cat]];
                            int displayed = display.displayFromWarehouse(product, warehouse, MAX_DISPLAY_PER_SLOT);

                            if (displayed > 0) {
                                newDisplayCount++;

                                int remainInWarehouse = warehouse.getStock(product);
                                if (remainInWarehouse > 0) {
                                    System.out.printf(" - [%s] %s %d개 진열 (창고: %d개)%n", allCategories[cat].name, product.name, displayed, remainInWarehouse);
                                } else {
                                    System.out.printf(" - [%s] %s %d개 진열%n", allCategories[cat].name, product.name, displayed);
                                }
                            }

                            // 다음 상품으로 인덱스 이동
                            arrangeCategoryIndex[cat]++;
                            // 이번 바퀴에서 진열했으므로 다음 바퀴도 시도
                            hasMore = true;
                        }
                    }
                }
            }
        }

        System.out.println();
        System.out.println("========================================");
        System.out.printf("보충: %d개 상품 / 신규: %d개 상품%n", refilledCount, newDisplayCount);
        System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
        System.out.println("========================================");
        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    private void printStockBar(String name, int stock) {
        // 상품명 출력 (한글 8글자 기준 = 화면 폭 16칸)
        int maxWidth = 16;
        int nameWidth = Util.getDisplayWidth(name);
        int padding = maxWidth - nameWidth;

        System.out.print(name);
        for (int i = 0; i < padding; i++) {
            System.out.print(" ");
        }

        // 수량 표시
        System.out.printf("%d개%n", stock);
    }

    /// <summary>
    /// 도매상 (메인 메뉴)
    /// </summary>
    private void goWholesaler() {
        boolean shopping = true;

        while (shopping) {
            Util.clearScreen();
            System.out.println("========================================");
            System.out.println("            [ 도매상 ]");
            System.out.println("========================================");
            System.out.printf("현재 자본: %,d원%n", money);
            System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
            System.out.println();
            System.out.println("[1] 카테고리별 구매");
            System.out.println("[2] 정책 설정");
            System.out.println("[3] 자동주문 실행");
            System.out.println("[0] 돌아가기");
            System.out.print(">> ");

            int choice = Util.readInt(scanner);

            switch (choice) {
                case 1:
                    buyByCategory();
                    break;
                case 2:
                    setPolicies();
                    break;
                case 3:
                    executeAutoOrder();
                    break;
                case 0:
                    shopping = false;
                    break;
                default:
                    System.out.println("잘못된 입력입니다.");
                    break;
            }
        }
    }

    /// <summary>
    /// 카테고리별 구매
    /// </summary>
    private void buyByCategory() {
        boolean browsing = true;

        while (browsing) {
            Util.clearScreen();
            System.out.println("========================================");
            System.out.println("        [ 카테고리 선택 ]");
            System.out.println("========================================");
            System.out.printf("현재 자본: %,d원 | 매대: %d / %d칸%n", money, display.getUsedSlots(), MAX_SLOT);
            System.out.println();
            System.out.println("[1] 음료        [2] 맥주        [3] 소주");
            System.out.println("[4] 간식/안주   [5] 고기        [6] 해수욕용품");
            System.out.println("[7] 식재료      [8] 라면        [9] 아이스크림");
            System.out.println("[10] 폭죽");
            System.out.println("[0] 돌아가기");
            System.out.print(">> ");

            int categoryChoice = Util.readInt(scanner);

            if (categoryChoice == 0) {
                browsing = false;
            } else if (categoryChoice >= 1 && categoryChoice <= 10) {
                buyCategoryProducts(categoryChoice);
            } else {
                System.out.println("잘못된 입력입니다.");
            }
        }
    }

    /// <summary>
    /// 카테고리 내 상품 구매
    /// </summary>
    private void buyCategoryProducts(int category) {
        boolean buying = true;
        Category cat = allCategories[category - 1];

        while (buying) {
            Util.clearScreen();

            System.out.println("========================================");
            System.out.printf("        [ %s ] (%s)%n", cat.name, cat.boxUnit);
            System.out.println("========================================");

            for (int i = 0; i < cat.products.length; i++) {
                Product p = cat.products[i];
                System.out.printf("%d. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n",
                    i + 1, p.name, p.buyPrice, p.sellPrice, getTotalStock(p));
            }

            System.out.println();
            System.out.println("구매할 상품 번호 (0: 돌아가기)");
            System.out.print(">> ");

            int productChoice = Util.readInt(scanner);

            if (productChoice == 0) {
                buying = false;
            } else {
                System.out.print("수량 입력 >> ");
                int quantity = Util.readInt(scanner);
                purchaseProduct(category, productChoice, quantity);
            }
        }
    }

    /// <summary>
    /// 상품 구매 처리
    /// </summary>
    private void purchaseProduct(int category, int productNum, int quantity) {
        // 카테고리와 상품 번호로 상품 찾기
        Product product = allCategories[category - 1].getProductByNum(productNum);

        if (product == null) {
            System.out.println("[!!] 잘못된 상품 번호입니다.");
            return;
        }

        int totalCost = product.buyPrice * quantity;

        // 자본 체크
        if (totalCost > money) {
            System.out.println("[!!] 자본이 부족합니다. (필요: " + String.format("%,d", totalCost) + "원)");
            return;
        }

        // 구매 처리 (창고로 입고)
        money = money - totalCost;
        warehouse.addStock(product, quantity);

        System.out.println("[OK] " + product.name + " " + quantity + "개 창고로 입고! (-" + String.format("%,d", totalCost) + "원)");
    }

    /// <summary>
    /// 정책 설정
    /// </summary>
    private void setPolicies() {
        boolean setting = true;

        while (setting) {
            Util.clearScreen();
            System.out.println("========================================");
            System.out.println("          [ 정책 설정 ]");
            System.out.println("========================================");
            System.out.println();
            System.out.println("[1] 카테고리 단위 설정");
            System.out.println("[2] 개별 상품 설정");
            System.out.println("[3] 현재 정책 확인");
            System.out.println("[0] 돌아가기");
            System.out.print(">> ");

            int choice = Util.readInt(scanner);

            switch (choice) {
                case 1:
                    setCategoryPolicy();
                    break;
                case 2:
                    setIndividualPolicy();
                    break;
                case 3:
                    showCurrentPolicies();
                    break;
                case 0:
                    setting = false;
                    break;
                default:
                    System.out.println("잘못된 입력입니다.");
                    break;
            }
        }
    }

    /// <summary>
    /// 카테고리 단위 정책 설정
    /// </summary>
    private void setCategoryPolicy() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("      [ 카테고리 단위 설정 ]");
        System.out.println("========================================");
        System.out.println();
        System.out.println("[1] 음료        [2] 맥주        [3] 소주");
        System.out.println("[4] 간식/안주   [5] 고기        [6] 해수욕용품");
        System.out.println("[7] 식재료      [8] 라면        [9] 아이스크림");
        System.out.println("[10] 폭죽");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        int categoryChoice = Util.readInt(scanner);

        if (categoryChoice == 0) {
            return;
        }

        if (categoryChoice < 1 || categoryChoice > 10) {
            System.out.println("잘못된 입력입니다.");
            return;
        }

        // 카테고리명 가져오기
        String categoryName = allCategories[categoryChoice - 1].name;

        System.out.println();
        System.out.println("[ " + categoryName + " 카테고리 설정 ]");
        System.out.println("[1] 자동주문 등록 (임계값 입력)");
        System.out.println("[2] 자동주문 해제");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        int actionChoice = Util.readInt(scanner);

        if (actionChoice == 0) {
            return;
        } else if (actionChoice == 1) {
            System.out.print("임계값 입력 (재고 몇 개 이하면 주문?) >> ");
            int threshold = Util.readInt(scanner);

            // 카테고리별 정책 설정
            Category cat = allCategories[categoryChoice - 1];
            cat.autoOrderEnabled = true;
            cat.autoOrderThreshold = threshold;

            System.out.println("[OK] " + categoryName + " 카테고리 등록 완료 (임계값: " + threshold + "개)");

        } else if (actionChoice == 2) {
            // 자동주문 해제
            Category cat = allCategories[categoryChoice - 1];
            cat.autoOrderEnabled = false;

            System.out.println("[OK] " + categoryName + " 카테고리 자동주문 해제됨");
        }
    }

    /// <summary>
    /// 개별 상품 정책 설정
    /// </summary>
    private void setIndividualPolicy() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("       [ 개별 상품 설정 ]");
        System.out.println("========================================");
        System.out.println("먼저 카테고리를 선택하세요.");
        System.out.println();
        System.out.println("[1] 음료        [2] 맥주        [3] 소주");
        System.out.println("[4] 간식/안주   [5] 고기        [6] 해수욕용품");
        System.out.println("[7] 식재료      [8] 라면        [9] 아이스크림");
        System.out.println("[10] 폭죽");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        int categoryChoice = Util.readInt(scanner);

        if (categoryChoice == 0) {
            return;
        }

        if (categoryChoice < 1 || categoryChoice > 10) {
            System.out.println("잘못된 입력입니다.");
            return;
        }

        // 해당 카테고리 상품 목록 출력
        System.out.println();
        printCategoryProductsForPolicy(categoryChoice);

        System.out.print("상품 번호 선택 >> ");
        int productNum = Util.readInt(scanner);

        Product product = allCategories[categoryChoice - 1].getProductByNum(productNum);

        if (product == null) {
            System.out.println("[!!] 잘못된 상품 번호입니다.");
            return;
        }

        System.out.println();
        System.out.println("[ " + product.name + " 설정 ]");
        System.out.println("[1] 자동주문 등록 (임계값 입력)");
        System.out.println("[2] 자동주문 해제");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        int actionChoice = Util.readInt(scanner);

        if (actionChoice == 0) {
            return;
        } else if (actionChoice == 1) {
            System.out.print("임계값 입력 (재고 몇 개 이하면 주문?) >> ");
            int threshold = Util.readInt(scanner);

            product.autoOrderEnabled = true;
            product.autoOrderThreshold = threshold;

            System.out.println("[OK] " + product.name + " 등록 완료 (임계값: " + threshold + "개)");

        } else if (actionChoice == 2) {
            product.autoOrderEnabled = false;
            System.out.println("[OK] " + product.name + " 자동주문 해제됨");
        }
    }

    /// <summary>
    /// 정책 설정용 카테고리 상품 출력
    /// </summary>
    private void printCategoryProductsForPolicy(int category) {
        Category cat = allCategories[category - 1];
        System.out.println("[ " + cat.name + " ]");
        for (int i = 0; i < cat.products.length; i++) {
            Product p = cat.products[i];
            System.out.printf("%d. %s (재고: %d)%n", i + 1, p.name, getTotalStock(p));
        }
    }

    /// <summary>
    /// 현재 정책 확인
    /// </summary>
    private void showCurrentPolicies() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("         [ 현재 정책 확인 ]");
        System.out.println("========================================");
        System.out.println();

        // 카테고리 정책 출력
        // 카테고리 정책: 음료, 맥주 등 카테고리 단위로 자동주문을 설정하는 것
        System.out.println("[ 카테고리 정책 ]");
        // 등록된 카테고리 정책이 하나라도 있는지 추적
        // - true: 정책 목록이 출력됨
        // - false: "(없음)" 출력
        boolean hasCategoryPolicy = false;

        for (Category cat : allCategories) {
            if (cat.autoOrderEnabled) {
                System.out.println(" - " + cat.name + ": 임계값 " + cat.autoOrderThreshold + "개");
                hasCategoryPolicy = true;
            }
        }

        if (!hasCategoryPolicy) {
            System.out.println(" (없음)");
        }

        System.out.println();

        // 개별 상품 정책 출력
        // 개별 상품 정책: 콜라, 사이다 등 개별 상품 단위로 자동주문을 설정하는 것
        System.out.println("[ 개별 상품 정책 ]");
        // 등록된 개별 상품 정책이 하나라도 있는지 추적
        // - true: 정책 목록이 출력됨
        // - false: "(없음)" 출력
        boolean hasIndividualPolicy = false;

        // 모든 상품 체크
        for (Product p : allProducts) {
            if (p.autoOrderEnabled) {
                System.out.println(" - " + p.name + ": 임계값 " + p.autoOrderThreshold + "개");
                hasIndividualPolicy = true;
            }
        }

        if (!hasIndividualPolicy) {
            System.out.println(" (없음)");
        }

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    /// <summary>
    /// 자동주문 실행
    /// </summary>
    private void executeAutoOrder() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("         [ 자동주문 실행 ]");
        System.out.println("========================================");
        System.out.println();

        int totalCost = 0;

        // 카테고리 정책 기반 주문
        for (Category cat : allCategories) {
            if (cat.autoOrderEnabled) {
                for (Product p : cat.products) {
                    totalCost = totalCost + autoOrderProduct(p, cat.autoOrderThreshold);
                }
            }
        }

        // 개별 상품 정책 기반 주문 (카테고리 미등록 시)
        for (Category cat : allCategories) {
            if (!cat.autoOrderEnabled) {
                for (Product p : cat.products) {
                    if (p.autoOrderEnabled) {
                        totalCost = totalCost + autoOrderProduct(p, p.autoOrderThreshold);
                    }
                }
            }
        }

        System.out.println();
        System.out.println("----------------------------------------");
        System.out.printf("총 주문 금액: -%,d원%n", totalCost);
        System.out.printf("남은 자본: %,d원%n", money);
        System.out.println("----------------------------------------");

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    /// <summary>
    /// 개별 상품 자동주문 처리
    /// 재고가 임계값 이하면 AUTO_ORDER_BOX_COUNT 박스 주문, 주문 금액 반환
    /// </summary>
    private int autoOrderProduct(Product product, int threshold) {
        // 총 재고(창고+매대)가 임계값보다 많으면 주문 안 함
        int totalStock = getTotalStock(product);
        if (totalStock > threshold) {
            return 0;
        }

        int boxSize = product.boxSize;
        int orderAmount = boxSize * AUTO_ORDER_BOX_COUNT;  // 3박스
        int cost = product.buyPrice * orderAmount;

        // 자본 체크
        if (cost > money) {
            System.out.printf(" - %s: 자본 부족 (필요: %,d원)%n", product.name, cost);
            return 0;
        }

        // 주문 처리 (창고로 입고)
        money = money - cost;
        warehouse.addStock(product, orderAmount);

        System.out.printf(" - %s %d박스(%d개) 창고 입고 (-%,d원)%n", product.name, AUTO_ORDER_BOX_COUNT, orderAmount, cost);

        return cost;
    }

    /// <summary>
    /// 영업 시작 (손님 응대)
    /// </summary>
    private void startBusiness() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("           [ 영업 시작 ]");
        System.out.println("========================================");

        // 하루 영업 변수
        int todayCustomers = 10 + (int)(Math.random() * 11);  // 10~20명
        int todaySales = 0;      // 오늘 매출
        int todayProfit = 0;     // 오늘 순이익
        int successCount = 0;    // 판매 성공 건수
        int failCount = 0;       // 판매 실패 건수
        boolean bigEventOccurred = false;  // 빅 이벤트 발생 여부

        // 손님 응대 루프
        for (int customerNum = 1; customerNum <= todayCustomers; customerNum++) {

            // 손님 절반쯤 지났을 때 빅 이벤트 체크 (20% 확률)
            if (!bigEventOccurred && customerNum == todayCustomers / 2) {
                if (checkBigEvent(20)) {
                    bigEventOccurred = true;
                    Util.delay(1000);
                }
            }

            // 랜덤 손님 유형 (0: 가족, 1: 커플, 2: 친구들, 3: 혼자)
            int customerType = Util.rand(4);

            // 손님 객체 생성
            Customer customer = createCustomer(customerType);

            // 멘트 조합: [손님 인사] + [시간대 멘트]
            String greeting = customerGreetings[customerType][Util.rand(5)];
            // 현재 시간대에 맞는 멘트 선택
            String timeMsg;
            switch (timeOfDay) {
                case TIME_MORNING:
                    timeMsg = morningGreetings[Util.rand(5)];
                    break;
                case TIME_NIGHT:
                    timeMsg = nightGreetings[Util.rand(5)];
                    break;
                default:
                    timeMsg = afternoonGreetings[Util.rand(5)];
                    break;
            }
            customer.greeting = greeting + " " + timeMsg;

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.printf("[ 손님 %d/%d - %s ]%n", customerNum, todayCustomers, customer.typeName);
            Util.delay(500);
            customer.sayGreeting();
            System.out.println();

            // 쇼핑 리스트 먼저 한번에 출력
            customer.sayWant();

            Util.delay(500);  // 리스트 확인 후 처리

            // 같은 상품 합산을 위한 배열
            // mergedProducts[i]: 상품, mergedWants[i]: 합산 수량
            Product[] mergedProducts = new Product[customer.wantCount];
            int[] mergedWants = new int[customer.wantCount];
            int mergedCount = 0;

            for (int itemIndex = 0; itemIndex < customer.wantCount; itemIndex++) {
                Product product = customer.wantProducts[itemIndex];
                int wantAmount = customer.wantAmounts[itemIndex];

                if (wantAmount <= 0) {
                    continue;
                }

                // 이미 합산 목록에 있는지 확인
                boolean found = false;
                for (int m = 0; m < mergedCount; m++) {
                    if (mergedProducts[m] == product) {
                        mergedWants[m] = mergedWants[m] + wantAmount;
                        found = true;
                        break;
                    }
                }

                // 없으면 새로 추가
                if (!found) {
                    mergedProducts[mergedCount] = product;
                    mergedWants[mergedCount] = wantAmount;
                    mergedCount++;
                }
            }

            // 합산된 목록으로 판매 처리
            System.out.println();
            System.out.println("판매 결과:");

            int customerSales = 0;
            int customerProfit = 0;

            for (int itemIndex = 0; itemIndex < mergedCount; itemIndex++) {
                Product product = mergedProducts[itemIndex];
                int wantAmount = mergedWants[itemIndex];

                int currentStock = display.getDisplayed(product);
                if (currentStock >= wantAmount) {
                    // 전부 판매 가능
                    int saleAmount = product.sellPrice * wantAmount;
                    int profitAmount = (product.sellPrice - product.buyPrice) * wantAmount;

                    display.sell(product, wantAmount);

                    money = money + saleAmount;
                    customerSales = customerSales + saleAmount;
                    customerProfit = customerProfit + profitAmount;
                    todaySales = todaySales + saleAmount;
                    todayProfit = todayProfit + profitAmount;
                    successCount++;

                    System.out.printf(" - %s %d개: OK (+%,d원)%n", product.name, wantAmount, saleAmount);

                } else if (currentStock > 0) {
                    // 일부만 판매
                    int saleAmount = product.sellPrice * currentStock;
                    int profitAmount = (product.sellPrice - product.buyPrice) * currentStock;

                    display.sell(product, currentStock);

                    money = money + saleAmount;
                    customerSales = customerSales + saleAmount;
                    customerProfit = customerProfit + profitAmount;
                    todaySales = todaySales + saleAmount;
                    todayProfit = todayProfit + profitAmount;
                    successCount++;
                    failCount++;  // 일부 실패로 카운트

                    System.out.printf(" - %s: %d/%d개만 (+%,d원)%n", product.name, currentStock, wantAmount, saleAmount);

                } else {
                    // 재고 없음
                    failCount++;
                    System.out.printf(" - %s %d개: 재고 없음!%n", product.name, wantAmount);
                }
            }

            // 손님 총액 표시
            if (customerSales > 0) {
                System.out.printf(">> 손님 결제: %,d원%n", customerSales);
            } else {
                System.out.println(">> 아무것도 못 사고 갔습니다...");
            }

            // 다음 손님 또는 스킵 선택 (마지막 손님이 아닌 경우)
            if (customerNum < todayCustomers) {
                System.out.println("(아무 키나 누르면 일시정지)");

                // 1.5초 동안 입력 감지 - 입력 있으면 메뉴 표시
                boolean interrupted = Util.waitForInput();

                if (!interrupted) {
                    // 입력 없음 -> 자동으로 다음 손님
                    continue;
                }

                // 입력 감지됨 -> 메뉴 표시
                System.out.println();
                System.out.println("[1] 다음 손님  [2] 남은 손님 스킵  [0] 영업 중단");
                System.out.print(">> ");
                int choice = Util.readInt(scanner);  // 기본값 1 (다음 손님)

                if (choice == 2) {
                    // 남은 손님 자동 처리
                    System.out.println();
                    System.out.println("남은 손님을 빠르게 처리합니다...");
                    Util.delay(500);

                    for (int skipCustomerNum = customerNum + 1; skipCustomerNum <= todayCustomers; skipCustomerNum++) {
                        // 간단히 랜덤 손님 처리 (재사용 배열 사용)
                        int skipType = Util.rand(4);

                        // 손님별 간단 쇼핑 리스트
                        if (skipType == 0) {
                            skipList[0] = getRandomFromCategory(categoryMeat);
                            skipList[1] = getRandomFromCategory(categoryDrink);
                            skipAmounts[0] = 2 + Util.rand(2);
                            skipAmounts[1] = 3 + Util.rand(3);
                        } else if (skipType == 1) {
                            skipList[0] = getRandomFromCategory(categorySoju);
                            skipList[1] = getRandomFromCategory(categorySnack);
                            skipAmounts[0] = 2 + Util.rand(2);
                            skipAmounts[1] = 2 + Util.rand(2);
                        } else if (skipType == 2) {
                            skipList[0] = getRandomFromCategory(categoryBeer);
                            skipList[1] = getRandomFromCategory(categorySoju);
                            skipAmounts[0] = 4 + Util.rand(3);
                            skipAmounts[1] = 2 + Util.rand(2);
                        } else {
                            skipList[0] = getRandomFromCategory(categoryRamen);
                            skipList[1] = getRandomFromCategory(categoryBeer);
                            skipAmounts[0] = 2 + Util.rand(2);
                            skipAmounts[1] = 2 + Util.rand(2);
                        }

                        // 판매 처리
                        for (int skipItemIndex = 0; skipItemIndex < 2; skipItemIndex++) {
                            Product skipProduct = skipList[skipItemIndex];
                            int skipWantAmount = skipAmounts[skipItemIndex];
                            int skipStock = display.getDisplayed(skipProduct);

                            if (skipStock >= skipWantAmount) {
                                int skipSaleAmount = skipProduct.sellPrice * skipWantAmount;
                                int skipProfitAmount = (skipProduct.sellPrice - skipProduct.buyPrice) * skipWantAmount;
                                display.sell(skipProduct, skipWantAmount);
                                money += skipSaleAmount;
                                todaySales += skipSaleAmount;
                                todayProfit += skipProfitAmount;
                                successCount++;
                            } else if (skipStock > 0) {
                                int skipSaleAmount = skipProduct.sellPrice * skipStock;
                                int skipProfitAmount = (skipProduct.sellPrice - skipProduct.buyPrice) * skipStock;
                                display.sell(skipProduct, skipStock);
                                money += skipSaleAmount;
                                todaySales += skipSaleAmount;
                                todayProfit += skipProfitAmount;
                                successCount++;
                                failCount++;
                            } else {
                                failCount++;
                            }
                        }
                    }
                    System.out.printf("손님 %d명 처리 완료!%n", todayCustomers - customerNum);
                    break;  // for 루프 종료

                } else if (choice == 0) {
                    // 영업 중단
                    System.out.println();
                    System.out.println("영업을 중단합니다.");
                    todayCustomers = customerNum;  // 정산용 손님 수 조정
                    break;  // for 루프 종료
                }

                // choice == 1 또는 다른 값: 다음 손님 (루프 계속)
            }
        }

        // 하루 정산
        Util.delay(800);  // 정산 준비 연출
        printDailySettlement(day, todayCustomers, successCount, failCount,
                todaySales, todayProfit, bigEventOccurred);

        System.out.println();
        System.out.println("아무 키나 입력하면 계속...");
        scanner.next();
    }

    /// <summary>
    /// 빠른 영업 (하루 요약)
    /// 손님 상세 없이 결과만 출력
    /// </summary>
    private void startQuickBusiness() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("         [ 빠른 영업 - " + day + "일차 ]");
        System.out.println("========================================");
        System.out.println();
        System.out.println("영업 중...");
        Util.delay(1000);

        // 하루 영업 시뮬레이션 (손님 상세 생략)
        int todayCustomers = 10 + Util.rand(11);
        int todaySales = 0;
        int todayProfit = 0;
        int successCount = 0;
        int failCount = 0;

        // 빅 이벤트 체크 (10% 확률)
        boolean eventOccurred = checkBigEvent(10);

        // 손님별 간략 처리 (직접 영업과 동일한 createCustomer 사용)
        for (int customerNum = 0; customerNum < todayCustomers; customerNum++) {
            int customerType = Util.rand(4);
            Customer customer = createCustomer(customerType);

            // 같은 상품 합산
            Product[] mergedProducts = new Product[customer.wantCount];
            int[] mergedWants = new int[customer.wantCount];
            int mergedCount = 0;

            for (int itemIndex = 0; itemIndex < customer.wantCount; itemIndex++) {
                Product product = customer.wantProducts[itemIndex];
                int wantAmount = customer.wantAmounts[itemIndex];

                if (wantAmount <= 0) {
                    continue;
                }

                boolean found = false;
                for (int m = 0; m < mergedCount; m++) {
                    if (mergedProducts[m] == product) {
                        mergedWants[m] = mergedWants[m] + wantAmount;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    mergedProducts[mergedCount] = product;
                    mergedWants[mergedCount] = wantAmount;
                    mergedCount++;
                }
            }

            // 합산된 목록으로 판매
            for (int itemIndex = 0; itemIndex < mergedCount; itemIndex++) {
                Product product = mergedProducts[itemIndex];
                int wantAmount = mergedWants[itemIndex];

                int[] result = sellProduct(product, wantAmount);
                int saleAmount = result[0];
                int profitAmount = result[1];
                int soldAmount = result[2];

                if (soldAmount > 0) {
                    money = money + saleAmount;
                    todaySales = todaySales + saleAmount;
                    todayProfit = todayProfit + profitAmount;
                    successCount++;

                    if (soldAmount < wantAmount) {
                        failCount++;
                    }
                } else {
                    failCount++;
                }
            }
        }

        // 결과 출력
        printDailySettlement(day, todayCustomers, successCount, failCount,
                             todaySales, todayProfit, eventOccurred);

        System.out.println();
        System.out.println("아무 키나 입력하면 계속...");
        scanner.next();
    }

    /// <summary>
    /// 빅 이벤트 체크 및 처리 (10% 확률)
    /// 단체 주문, 펜션 배달, 축제 시즌 중 하나 발생
    /// </summary>
    private boolean checkBigEvent(int chance) {
        // chance% 확률로 이벤트 발생
        if (Util.rand(100) >= chance) {
            return false;
        }

        int eventType = Util.rand(3);

        if (eventType == 0) {
            // 단체 주문: 음료, 안주 대량 판매
            System.out.println();
            System.out.println("========================================");
            System.out.println("      *** 전화가 왔습니다! ***");
            System.out.println("========================================");
            System.out.println("\"여기 수련회인데요, 대량 주문할게요!\"");
            System.out.println();
            int bonus = sellBulk(categoryDrink, 10 + Util.rand(10));
            bonus = bonus + sellBulk(categorySnack, 5 + Util.rand(5));
            if (bonus > 0) {
                System.out.printf(">> 단체 주문 매출: %,d원%n", bonus);
            } else {
                System.out.println(">> 재고 부족으로 주문 처리 실패...");
            }
            return bonus > 0;
        } else if (eventType == 1) {
            // 펜션 배달: 고기, 음료, 식재료 판매
            System.out.println();
            System.out.println("========================================");
            System.out.println("      *** 전화가 왔습니다! ***");
            System.out.println("========================================");
            System.out.println("\"펜션에서 바베큐 세트 배달 부탁드려요!\"");
            System.out.println();
            int bonus = sellBulk(categoryMeat, 5 + Util.rand(5));
            bonus = bonus + sellBulk(categoryDrink, 5 + Util.rand(5));
            bonus = bonus + sellBulk(categoryGrocery, 3 + Util.rand(3));
            if (bonus > 0) {
                System.out.printf(">> 펜션 배달 매출: %,d원%n", bonus);
            } else {
                System.out.println(">> 재고 부족으로 배달 실패...");
            }
            return bonus > 0;
        } else {
            // 축제 시즌: 폭죽, 맥주 대량 판매
            System.out.println();
            System.out.println("========================================");
            System.out.println("    *** 불꽃축제 시즌입니다! ***");
            System.out.println("========================================");
            System.out.println("\"축제 준비물 사러 왔어요!\"");
            System.out.println();
            int bonus = sellBulk(categoryFirework, 5 + Util.rand(10));
            bonus = bonus + sellBulk(categoryBeer, 10 + Util.rand(10));
            if (bonus > 0) {
                System.out.printf(">> 축제 시즌 매출: %,d원%n", bonus);
            } else {
                System.out.println(">> 재고 부족으로 판매 실패...");
            }
            return bonus > 0;
        }
    }

    /// <summary>
    /// 카테고리에서 대량 판매 처리
    /// 판매된 금액 반환
    /// </summary>
    private int sellBulk(Category category, int amount) {
        int totalSale = 0;
        int sellAmount = amount / category.products.length;

        for (Product p : category.products) {
            int stock = display.getDisplayed(p);
            if (stock >= sellAmount) {
                int sale = p.sellPrice * sellAmount;
                display.sell(p, sellAmount);

                money = money + sale;
                totalSale = totalSale + sale;
            } else if (stock > 0) {
                int sale = p.sellPrice * stock;
                display.sell(p, stock);

                money = money + sale;
                totalSale = totalSale + sale;
            }
        }

        return totalSale;
    }

    /// <summary>
    /// 상품 판매 처리
    /// 매대 재고에서 판매하고 결과 반환
    /// </summary>
    /// <returns>int[3] = {판매액, 이익, 실제판매수량} (재고 없으면 모두 0)</returns>
    private int[] sellProduct(Product product, int wantAmount) {
        int[] result = new int[3];  // [판매액, 이익, 실제판매수량]

        int stock = display.getDisplayed(product);

        if (stock >= wantAmount) {
            // 전부 판매 가능
            result[0] = product.sellPrice * wantAmount;
            result[1] = (product.sellPrice - product.buyPrice) * wantAmount;
            result[2] = wantAmount;
            display.sell(product, wantAmount);
        } else if (stock > 0) {
            // 일부만 판매
            result[0] = product.sellPrice * stock;
            result[1] = (product.sellPrice - product.buyPrice) * stock;
            result[2] = stock;
            display.sell(product, stock);
        }
        // else: 재고 없음 - result는 이미 0으로 초기화됨

        return result;
    }

    /// <summary>
    /// 일일 정산 출력
    /// </summary>
    private void printDailySettlement(int dayNum, int customers, int success, int fail,
                                              int sales, int profit, boolean bigEvent) {
        // 시간대 문자열
        String timeName;
        switch (timeOfDay) {
            case TIME_MORNING:
                timeName = "아침";
                break;
            case TIME_NIGHT:
                timeName = "밤";
                break;
            default:
                timeName = "낮";
                break;
        }

        System.out.println();
        System.out.println("========================================");
        System.out.printf("       [ %d일차 %s 영업 정산 ]%n", dayNum, timeName);
        System.out.println("========================================");
        if (bigEvent) {
            System.out.println("★ 빅 이벤트 발생!");
        }
        System.out.printf("오늘 방문 손님: %d명%n", customers);
        System.out.printf("판매 성공: %d건%n", success);
        System.out.printf("판매 실패: %d건%n", fail);
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.printf("  오늘 매출:    %,d원%n", sales);
        System.out.printf("  순이익:      +%,d원%n", profit);
        System.out.println("----------------------------------------");
        System.out.printf("  현재 총 자본: %,d원%n", money);
        System.out.printf("  목표까지:     %,d원%n", goalMoney - money);
        System.out.println("========================================");
    }

    /// <summary>
    /// 상품 초기화 메서드
    /// 배율을 적용하여 상품 객체 생성
    /// </summary>
    private void initProducts() {
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
        categoryDrink = new Category("음료", "1박스=24개",
            new Product[]{cola, cider, water, pocari, ipro, fanta, milkis}, 0);
        categoryBeer = new Category("맥주", "1박스=24개",
            new Product[]{cass, terra, hite, kloud, filgood}, 1);
        categorySoju = new Category("소주", "1박스=20병",
            new Product[]{chamisul, cheumcherum, jinro, goodday, saero}, 2);
        categorySnack = new Category("간식/안주", "1박스=20개",
            new Product[]{driedSquid, peanut, chip, jerky, sausageSnack}, 3);
        categoryMeat = new Category("고기", "1판=10팩",
            new Product[]{samgyupsal, moksal, sausage, galbi, hangjeongsal}, 4);
        categoryBeach = new Category("해수욕용품", "1묶음=5개",
            new Product[]{tube, sunscreen, beachBall, goggles, waterGun}, 5);
        categoryGrocery = new Category("식재료", "1박스=10개",
            new Product[]{ssamjang, lettuce, kimchi, onion, salt}, 6);
        categoryRamen = new Category("라면", "1박스=40개",
            new Product[]{shinRamen, jinRamen, neoguri, buldak, chapagetti}, 7);
        categoryIcecream = new Category("아이스크림", "1박스=24개",
            new Product[]{melona, screwBar, fishBread, jewelBar, watermelonBar}, 8);
        categoryFirework = new Category("폭죽", "1박스=10개",
            new Product[]{sparkler, romanCandle, fountain, fireworkSet, smokeBomb}, 9);

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

        // 상품 이름 맵 초기화 (O(1) 조회용)
        initProductMap();
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

    /// <summary>
    /// 카테고리에서 랜덤 상품 1개 선택
    /// </summary>
    private Product getRandomFromCategory(Category category) {
        int index = Util.rand(category.products.length);
        return category.products[index];
    }

    /// <summary>
    /// 자동 배정 시 진열 대기 목록 채우기
    ///
    /// 동작: 해당 카테고리의 상품들을 검사해서 "창고에는 있는데 매대에는 없는" 상품만 진열 대기 버퍼에 추가
    ///
    /// 예시: 음료 카테고리(categoryIndex=0) 검사
    ///       - 콜라: 창고 20개, 매대 0개 → 버퍼에 추가 (새로 진열 가능)
    ///       - 사이다: 창고 0개, 매대 5개 → 스킵 (창고에 없음)
    ///       - 물: 창고 10개, 매대 3개 → 스킵 (이미 매대에 있음, 1단계에서 보충됨)
    /// </summary>
    private void fillArrangeBuffer(int categoryIndex, Category category) {
        for (Product p : category.products) {
            // 창고에 재고가 있고 매대에 진열되지 않은 상품
            if (warehouse.getStock(p) > 0 && display.getDisplayed(p) == 0) {
                arrangeCategoriesBuffer[categoryIndex][arrangeCategoryCounts[categoryIndex]++] = p;
            }
        }
    }

    /// <summary>
    /// 손님 객체 생성 (유형별 구매 목록 설정)
    /// </summary>
    Customer createCustomer(int type) {
        Customer c;

        if (type == Customer.TYPE_FAMILY) {
            // 가족: 고기 + 식재료 + 음료 (필수) / 안주, 아이스크림 (선택 50%)
            c = new Customer(type, "가족 손님");

            c.wantProducts[0] = getAvailableFromCategory(categoryMeat);
            c.wantProducts[1] = getAvailableFromCategory(categoryMeat);
            c.wantProducts[2] = getAvailableFromCategory(categoryGrocery);
            c.wantProducts[3] = getAvailableFromCategory(categoryGrocery);
            c.wantProducts[4] = getAvailableFromCategory(categoryDrink);
            c.wantProducts[5] = getAvailableFromCategory(categoryDrink);
            c.wantProducts[6] = getAvailableFromCategory(categorySnack);
            c.wantProducts[7] = getAvailableFromCategory(categoryIcecream);

            c.wantAmounts[0] = 2 + Util.rand(2);           // 고기1 (필수)
            c.wantAmounts[1] = 1 + Util.rand(2);           // 고기2 (필수)
            c.wantAmounts[2] = 1 + Util.rand(2);           // 식재료1 (필수)
            c.wantAmounts[3] = 1 + Util.rand(2);           // 식재료2 (필수)
            c.wantAmounts[4] = 2 + Util.rand(3);           // 음료1 (필수)
            c.wantAmounts[5] = 1 + Util.rand(2);           // 음료2 (필수)
            c.wantAmounts[6] = maybeBuy(2 + Util.rand(2)); // 안주 (선택 50%)
            c.wantAmounts[7] = maybeBuy(2 + Util.rand(3)); // 아이스크림 (선택 50%)

        } else if (type == Customer.TYPE_COUPLE) {
            // 커플: 소주 + 맥주 + 안주 (필수) / 음료, 아이스크림 (선택 50%)
            c = new Customer(type, "커플 손님");

            c.wantProducts[0] = getAvailableFromCategory(categorySoju);
            c.wantProducts[1] = getAvailableFromCategory(categoryBeer);
            c.wantProducts[2] = getAvailableFromCategory(categorySnack);
            c.wantProducts[3] = getAvailableFromCategory(categorySnack);
            c.wantProducts[4] = getAvailableFromCategory(categoryDrink);
            c.wantProducts[5] = getAvailableFromCategory(categoryIcecream);

            c.wantAmounts[0] = 1 + Util.rand(2);           // 소주 (필수) 1~2개
            c.wantAmounts[1] = 1 + Util.rand(2);           // 맥주 (필수) 1~2개
            c.wantAmounts[2] = 1 + Util.rand(2);           // 안주1 (필수) 1~2개
            c.wantAmounts[3] = maybeBuy(1 + Util.rand(2)); // 안주2 (선택 50%) 0~2개
            c.wantAmounts[4] = maybeBuy(1);                 // 음료 (선택 50%) 0~1개
            c.wantAmounts[5] = maybeBuy(1);                 // 아이스크림 (선택 50%) 0~1개

        } else if (type == Customer.TYPE_FRIENDS) {
            // 친구들: 맥주 + 소주 + 안주 (필수) / 아이스크림, 폭죽 (선택 50%)
            c = new Customer(type, "친구들");

            c.wantProducts[0] = getAvailableFromCategory(categoryBeer);
            c.wantProducts[1] = getAvailableFromCategory(categorySoju);
            c.wantProducts[2] = getAvailableFromCategory(categorySnack);
            c.wantProducts[3] = getAvailableFromCategory(categorySnack);
            c.wantProducts[4] = getAvailableFromCategory(categoryIcecream);
            c.wantProducts[5] = getAvailableFromCategory(categoryIcecream);
            c.wantProducts[6] = getAvailableFromCategory(categoryFirework);

            c.wantAmounts[0] = 6 + Util.rand(5);           // 맥주 (필수) - 많이
            c.wantAmounts[1] = 3 + Util.rand(3);           // 소주 (필수)
            c.wantAmounts[2] = 2 + Util.rand(2);           // 안주1 (필수)
            c.wantAmounts[3] = 1 + Util.rand(2);           // 안주2 (필수)
            c.wantAmounts[4] = maybeBuy(2 + Util.rand(2)); // 아이스크림1 (선택 50%)
            c.wantAmounts[5] = maybeBuy(1 + Util.rand(2)); // 아이스크림2 (선택 50%)
            c.wantAmounts[6] = maybeBuy(2 + Util.rand(3)); // 폭죽 (선택 50%)

        } else {
            // 혼자: 라면 + 맥주 (필수) / 음료, 아이스크림, 안주 (선택 50%)
            c = new Customer(type, "혼자 온 손님");

            c.wantProducts[0] = getAvailableFromCategory(categoryRamen);
            c.wantProducts[1] = getAvailableFromCategory(categoryBeer);
            c.wantProducts[2] = getAvailableFromCategory(categoryDrink);
            c.wantProducts[3] = getAvailableFromCategory(categoryIcecream);
            c.wantProducts[4] = getAvailableFromCategory(categorySnack);

            c.wantAmounts[0] = 1 + Util.rand(2);           // 라면 (필수) 1~2개
            c.wantAmounts[1] = 1 + Util.rand(2);           // 맥주 (필수) 1~2개
            c.wantAmounts[2] = maybeBuy(1);                 // 음료 (선택 50%) 0~1개
            c.wantAmounts[3] = maybeBuy(1);                 // 아이스크림 (선택 50%) 0~1개
            c.wantAmounts[4] = maybeBuy(1);                 // 안주 (선택 50%) 0~1개
        }

        return c;
    }

    /// <summary>
    /// 카테고리에서 재고 있는 상품 우선 선택
    /// 재고 있는 상품이 없으면 랜덤 선택 (재고 없음 처리)
    /// </summary>
    Product getAvailableFromCategory(Category category) {
        // 재고 있는 상품들 먼저 모음 (재사용 배열 사용)
        int count = 0;

        for (Product p : category.products) {
            if (display.getDisplayed(p) > 0) {
                availableProducts[count] = p;
                count++;
            }
        }

        // 재고 있는 상품이 있으면 그 중에서 랜덤 선택
        if (count > 0) {
            return availableProducts[Util.rand(count)];
        }

        // 재고 있는 상품이 없으면 랜덤 선택 (재고 없음으로 처리됨)
        return category.products[Util.rand(category.products.length)];
    }
}
