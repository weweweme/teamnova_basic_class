import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class Main {

    // ========== 상수 ==========

    static final int INIT_MONEY = 50000000;           // 초기 자본 5000만원
    static final int GOAL_MONEY = 300000000;          // 목표 금액 3억원
    static final int MAX_SLOT = 20;                   // 매대 최대 슬롯 (상품 30개 중 20개만 진열 가능)
    static final int MAX_DISPLAY_PER_SLOT = 15;      // 슬롯당 최대 진열 수량
    static final int MIN_SLOT_FOR_BUSINESS = 10;     // 영업 시작 최소 슬롯 (50%)
    static final int DEFAULT_PRICE_MULTIPLIER = 3;   // 기본 가격 배율
    static final int AUTO_ORDER_BOX_COUNT = 3;       // 자동주문 시 박스 수 (약 1주일치)

    // ========== 게임 변수 ==========

    static int money;
    static int goalMoney;
    static int priceMultiplier;
    static int day = 1;
    static boolean isMorning = true;  // true: 아침, false: 오후

    static Scanner scanner = new Scanner(System.in);

    // ========== 창고와 매대 ==========

    static Warehouse warehouse;  // 창고 (상품 재고 관리)
    static Display display;      // 매대 (상품 진열 관리)

    // ========== 카테고리별 자동주문 정책 ==========
    // 각 카테고리의 자동주문 활성화 여부와 임계값

    // 활성화 여부 (기본 true - 모든 카테고리 자동주문 활성화)
    static boolean autoOrderDrink = true;       // 음료
    static boolean autoOrderBeer = true;        // 맥주
    static boolean autoOrderSoju = true;        // 소주
    static boolean autoOrderSnack = true;       // 간식/안주
    static boolean autoOrderMeat = true;        // 고기
    static boolean autoOrderBeach = true;       // 해수욕 용품
    static boolean autoOrderGrocery = true;     // 식재료
    static boolean autoOrderRamen = true;       // 라면
    static boolean autoOrderIcecream = true;    // 아이스크림
    static boolean autoOrderEtc = true;         // 폭죽

    // 임계값 (재고가 이 값 이하면 자동주문, 기본 10개)
    static int thresholdDrink = 10;
    static int thresholdBeer = 10;
    static int thresholdSoju = 10;
    static int thresholdSnack = 10;
    static int thresholdMeat = 10;
    static int thresholdBeach = 10;
    static int thresholdGrocery = 10;
    static int thresholdRamen = 10;
    static int thresholdIcecream = 10;
    static int thresholdEtc = 10;

    // ========== 상품 객체 ==========
    // initProducts()에서 초기화

    // 음료
    static Product cola;
    static Product cider;
    static Product water;
    static Product pocari;
    static Product ipro;

    // 맥주
    static Product cass;
    static Product terra;
    static Product hite;

    // 소주
    static Product chamisul;
    static Product cheumcherum;
    static Product jinro;

    // 간식/안주
    static Product driedSquid;
    static Product peanut;
    static Product chip;

    // 고기
    static Product samgyupsal;
    static Product moksal;
    static Product sausage;

    // 해수욕 용품
    static Product tube;
    static Product sunscreen;
    static Product beachBall;

    // 식재료
    static Product ssamjang;
    static Product lettuce;
    static Product kimchi;

    // 라면
    static Product shinRamen;
    static Product jinRamen;
    static Product neoguri;

    // 아이스크림
    static Product melona;
    static Product screwBar;
    static Product fishBread;

    // 기타 (폭죽)
    static Product sparkler;      // 불꽃막대
    static Product romanCandle;   // 로만캔들
    static Product fountain;      // 분수폭죽

    // ========== 카테고리별 상품 배열 ==========
    // initProducts() 이후에 초기화됨

    static Product[] categoryDrink;      // 음료
    static Product[] categoryBeer;       // 맥주
    static Product[] categorySoju;       // 소주
    static Product[] categorySnack;      // 안주
    static Product[] categoryMeat;       // 고기
    static Product[] categoryBeach;      // 해수욕용품
    static Product[] categoryGrocery;    // 식재료
    static Product[] categoryRamen;      // 라면
    static Product[] categoryIcecream;   // 아이스크림
    static Product[] categoryFirework;   // 폭죽

    // 전체 카테고리 배열 (순회용)
    static Product[][] allCategories;

    // ========== 상품 이름 → 상품 객체 맵 ==========
    // productMap.get(name)으로 O(1) 조회
    static Map<String, Product> productMap;

    // ========== 전체 상품 배열 ==========
    // 모든 상품을 순회할 때 사용 (중복 없음)
    static Product[] allProducts;

    // ========== 재사용 배열 (메서드 내 반복 할당 방지) ==========

    // autoArrangeDisplay()용 - 카테고리별 진열 대기 상품 버퍼
    static Product[][] arrangeCategoriesBuffer = new Product[10][5];  // [카테고리][상품] - 창고→매대 진열 대기 버퍼
    static int[] arrangeCategoryCounts = new int[10];                 // 각 카테고리별 버퍼 내 상품 수
    static int[] arrangeCategoryIndex = new int[10];                  // 라운드 로빈 진열 시 현재 인덱스
    static String[] categoryNames = {"음료", "맥주", "소주", "간식", "고기", "해수욕", "식재료", "라면", "아이스크림", "폭죽"};

    // 스킵 영업용 - 직접 영업 중 "남은 손님 스킵" 선택 시
    static Product[] skipList = new Product[2];       // 스킵 처리 시 간략화된 구매 목록
    static int[] skipAmounts = new int[2];            // 스킵 처리 시 구매 수량

    // 판매 대상용 - 빠른 영업 / 1주일 스킵 시 손님별 구매 대상
    static Product[] targets = new Product[8];        // 자동 영업 시 판매 대상 상품
    static int targetsCount = 0;                      // 실제 사용 개수 (현재 3개 고정)

    // getAvailableFromCategory()용 - 재고 있는 상품 필터링
    static Product[] availableProducts = new Product[5];  // 카테고리 내 재고 있는 상품 임시 저장 (최대 5개 - 음료)

    // ========== 손님 멘트 배열 ==========
    // [손님유형][다양한 멘트] - 4종류 × 5개

    static String[][] customerGreetings = {
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

    // 시간대별 멘트 - 5개

    static String[] timeGreetings = {
        "아침부터 열일하시네요!",
        "점심 준비하러 왔어요~",
        "오후에 먹으려고요!",
        "저녁에 다 같이 먹을 거예요~",
        "밤에 야식으로 먹을 거예요!"
    };

    public static void main(String[] args) {

        // 게임 시작 화면 출력
        printStartScreen();

        int startChoice = Util.readInt(scanner);  // 잘못된 입력 시 종료

        if (startChoice == 1) {
            // 기본 모드
            money = INIT_MONEY;
            goalMoney = GOAL_MONEY;
            priceMultiplier = DEFAULT_PRICE_MULTIPLIER;

        } else if (startChoice == 2) {
            // 커스텀 모드
            System.out.println();
            System.out.println("========================================");
            System.out.println("         [ 커스텀 게임 설정 ]");
            System.out.println("========================================");

            System.out.print("시작 자본 입력 (만원 단위): ");
            int inputMoney = Util.readInt(scanner);
            money = inputMoney * 10000;

            System.out.print("목표 금액 입력 (만원 단위): ");
            int inputGoal = Util.readInt(scanner);
            goalMoney = inputGoal * 10000;

            System.out.print("가격 배율 입력 (1~100, 판매가에만 적용): ");
            priceMultiplier = Util.readInt(scanner);

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.println("시작 자본: " + String.format("%,d", money) + "원");
            System.out.println("목표 금액: " + String.format("%,d", goalMoney) + "원");
            System.out.println("가격 배율: " + priceMultiplier + "배");
            System.out.println("----------------------------------------");

        } else {
            // 종료
            System.out.println("게임을 종료합니다.");
            scanner.close();
            return;
        }

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
                    if (isMorning) {
                        goWholesaler();
                        isMorning = false;  // 도매상 갔다오면 오후로 전환
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
                            day++;
                            isMorning = true;
                            break;
                        case 2:
                            // 빠른 영업
                            startQuickBusiness();
                            day++;
                            isMorning = true;
                            break;
                        case 3:
                            // 1주일 스킵
                            skipWeek();
                            isMorning = true;
                            break;
                        // case 0: 돌아가기 (아무것도 안 함)
                    }
                    break;

                case 3:
                    // 재고/매대 관리 (서브메뉴)
                    showInventoryMenu();
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
        scanner.close();
    }

    /// <summary>
    /// 총 재고 조회 (창고 + 매대)
    /// </summary>
    static int getTotalStock(Product p) {
        return warehouse.getStock(p) + display.getDisplayed(p);
    }

    /// <summary>
    /// 50% 확률로 구매 (선택 카테고리용)
    /// 50% 확률로 원래 수량 반환, 50% 확률로 0 반환
    /// </summary>
    static int maybeBuy(int amount) {
        if (Util.rand(2) == 0) {
            return 0;       // 안 삼
        }
        return amount;      // 삼
    }

    /// <summary>
    /// 게임 시작 화면 출력
    /// </summary>
    private static void printStartScreen() {
        System.out.println("========================================");
        System.out.println("     _____                      ");
        System.out.println("    / ____|                     ");
        System.out.println("   | (___  _   _ _ __   ___ _ __ ");
        System.out.println("    \\___ \\| | | | '_ \\ / _ \\ '__|");
        System.out.println("    ____) | |_| | |_) |  __/ |   ");
        System.out.println("   |_____/ \\__,_| .__/ \\___|_|   ");
        System.out.println("                | |              ");
        System.out.println("                |_|   @ Gangneung");
        System.out.println("========================================");
        System.out.println("목표: 돈을 모아서 펜션 주인이 되자!");
        System.out.println();
        System.out.println("[1] 게임 시작 (기본: 5000만원 -> 3억원, 배율 3배)");
        System.out.println("[2] 커스텀 게임 (자본/목표/배율 직접 설정)");
        System.out.println("[그 외] 종료");
        System.out.print(">> ");
    }

    /// <summary>
    /// 하루 시작 메뉴 출력
    /// </summary>
    private static void printDailyMenu() {
        Util.clearScreen();
        System.out.println("========================================");
        if (isMorning) {
            System.out.println("          [  " + day + "일차 - 아침  ]");
        } else {
            System.out.println("          [  " + day + "일차 - 오후  ]");
        }
        System.out.println("========================================");
        System.out.println("현재 자본: " + String.format("%,d", money) + "원");
        System.out.println("매대 현황: " + display.getUsedSlots() + " / " + MAX_SLOT + "칸");
        System.out.println();

        if (isMorning) {
            // 아침: 도매상, 영업, 재고/매대 모두 가능
            System.out.println("[1] 도매상 가기 (오전 소비)");
            System.out.println("[2] 영업 시작");
            System.out.println("[3] 재고/매대 관리");
        } else {
            // 오후: 영업, 재고/매대만 가능
            System.out.println("[1] (도매상 마감)");
            System.out.println("[2] 영업 시작");
            System.out.println("[3] 재고/매대 관리");
        }
        System.out.println("[0] 게임 종료");
        System.out.print(">> ");
    }

    /// <summary>
    /// 영업 시작 서브메뉴
    /// 선택한 영업 타입 반환 (1: 직접, 2: 빠른, 3: 1주일 스킵, 0: 취소)
    /// </summary>
    private static int showBusinessMenu() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("           [ 영업 시작 ]");
        System.out.println("========================================");
        System.out.println();
        System.out.println("[1] 직접 영업 (손님 한 명씩 응대)");
        System.out.println("[2] 빠른 영업 (하루 결과만 요약)");
        System.out.println("[3] 1주일 스킵 (7일 자동 영업)");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        // 잘못된 입력 시 돌아가기
        return Util.readInt(scanner);
    }

    /// <summary>
    /// 재고/매대 관리 서브메뉴
    /// </summary>
    private static void showInventoryMenu() {
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
    private static boolean checkWinOrLose() {
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
    private static void showInventory() {
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
    private static void manageDisplay() {
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
    private static void showWarehouse() {
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
    private static void displayProduct() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("        [ 상품 진열 ] 창고 → 매대");
        System.out.println("========================================");
        System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
        System.out.println();

        // 창고에 재고가 있는 상품 목록 출력
        System.out.println("--- 창고 재고 ---");
        int num = 1;
        for (Product p : allProducts) {
            int stock = warehouse.getStock(p);
            if (stock > 0) {
                System.out.printf("%d. %s (%d개)%n", num++, p.name, stock);
            }
        }

        if (num == 1) {
            System.out.println("  창고가 비어있습니다.");
            System.out.println();
            System.out.println("아무 키나 입력하면 돌아갑니다...");
            scanner.next();
            return;
        }

        System.out.println();
        System.out.print("진열할 상품명 입력 (취소: 0): ");
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

        int warehouseStock = warehouse.getStock(product);
        if (warehouseStock == 0) {
            System.out.println("[!!] 창고에 재고가 없습니다.");
            return;
        }

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
    private static void returnProduct() {
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
    private static void autoArrangeDisplay() {
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
            // 매대에 진열할 상품 목록 준비
            // 창고에는 있지만 아직 매대에 없는 상품들을 카테고리별로 모아둔다
            // 예: 창고에 콜라 20개 있고 매대에 0개 → 진열 대기 목록에 추가
            int totalAvailable = 0;
            for (int i = 0; i < 10; i++) {
                arrangeCategoryCounts[i] = 0;
                arrangeCategoryIndex[i] = 0;
                fillArrangeBuffer(i, allCategories[i]);
                totalAvailable = totalAvailable + arrangeCategoryCounts[i];
            }

            if (totalAvailable == 0) {
                System.out.println(" (창고에 새로 진열할 상품 없음)");
            } else {
                // 라운드 로빈 방식으로 진열
                boolean hasMore = true;

                while (hasMore && display.hasEmptySlot()) {
                    hasMore = false;

                    for (int cat = 0; cat < 10; cat++) {
                        if (arrangeCategoryIndex[cat] < arrangeCategoryCounts[cat]) {
                            if (!display.hasEmptySlot()) {
                                break;
                            }

                            Product product = arrangeCategoriesBuffer[cat][arrangeCategoryIndex[cat]];
                            int displayed = display.displayFromWarehouse(product, warehouse, MAX_DISPLAY_PER_SLOT);

                            if (displayed > 0) {
                                newDisplayCount++;

                                int remainInWarehouse = warehouse.getStock(product);
                                if (remainInWarehouse > 0) {
                                    System.out.printf(" - [%s] %s %d개 진열 (창고: %d개)%n", categoryNames[cat], product.name, displayed, remainInWarehouse);
                                } else {
                                    System.out.printf(" - [%s] %s %d개 진열%n", categoryNames[cat], product.name, displayed);
                                }
                            }

                            arrangeCategoryIndex[cat]++;
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

    private static void printStockBar(String name, int stock) {
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
    private static void goWholesaler() {
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
    private static void buyByCategory() {
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
    private static void buyCategoryProducts(int category) {
        boolean buying = true;

        while (buying) {
            Util.clearScreen();

            // 카테고리별 상품 목록 출력
            if (category == 1) {
                // 음료
                System.out.println("========================================");
                System.out.println("            [ 음료 ] (1박스=24개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cola.name, cola.buyPrice, cola.sellPrice, getTotalStock(cola));
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cider.name, cider.buyPrice, cider.sellPrice, getTotalStock(cider));
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", water.name, water.buyPrice, water.sellPrice, getTotalStock(water));
                System.out.printf("4. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", pocari.name, pocari.buyPrice, pocari.sellPrice, getTotalStock(pocari));
                System.out.printf("5. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", ipro.name, ipro.buyPrice, ipro.sellPrice, getTotalStock(ipro));
            } else if (category == 2) {
                // 맥주
                System.out.println("========================================");
                System.out.println("            [ 맥주 ] (1박스=24개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cass.name, cass.buyPrice, cass.sellPrice, getTotalStock(cass));
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", terra.name, terra.buyPrice, terra.sellPrice, getTotalStock(terra));
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", hite.name, hite.buyPrice, hite.sellPrice, getTotalStock(hite));
            } else if (category == 3) {
                // 소주
                System.out.println("========================================");
                System.out.println("            [ 소주 ] (1박스=20병)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", chamisul.name, chamisul.buyPrice, chamisul.sellPrice, getTotalStock(chamisul));
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", cheumcherum.name, cheumcherum.buyPrice, cheumcherum.sellPrice, getTotalStock(cheumcherum));
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", jinro.name, jinro.buyPrice, jinro.sellPrice, getTotalStock(jinro));
            } else if (category == 4) {
                // 간식/안주
                System.out.println("========================================");
                System.out.println("         [ 간식/안주 ] (1박스=20개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", driedSquid.name, driedSquid.buyPrice, driedSquid.sellPrice, getTotalStock(driedSquid));
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", peanut.name, peanut.buyPrice, peanut.sellPrice, getTotalStock(peanut));
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", chip.name, chip.buyPrice, chip.sellPrice, getTotalStock(chip));
            } else if (category == 5) {
                // 고기
                System.out.println("========================================");
                System.out.println("            [ 고기 ] (1판=10팩)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", samgyupsal.name, samgyupsal.buyPrice, samgyupsal.sellPrice, getTotalStock(samgyupsal));
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", moksal.name, moksal.buyPrice, moksal.sellPrice, getTotalStock(moksal));
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", sausage.name, sausage.buyPrice, sausage.sellPrice, getTotalStock(sausage));
            } else if (category == 6) {
                // 해수욕 용품
                System.out.println("========================================");
                System.out.println("        [ 해수욕용품 ] (1묶음=5개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", tube.name, tube.buyPrice, tube.sellPrice, getTotalStock(tube));
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", sunscreen.name, sunscreen.buyPrice, sunscreen.sellPrice, getTotalStock(sunscreen));
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", beachBall.name, beachBall.buyPrice, beachBall.sellPrice, getTotalStock(beachBall));
            } else if (category == 7) {
                // 식재료
                System.out.println("========================================");
                System.out.println("          [ 식재료 ] (1박스=10개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", ssamjang.name, ssamjang.buyPrice, ssamjang.sellPrice, getTotalStock(ssamjang));
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", lettuce.name, lettuce.buyPrice, lettuce.sellPrice, getTotalStock(lettuce));
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", kimchi.name, kimchi.buyPrice, kimchi.sellPrice, getTotalStock(kimchi));
            } else if (category == 8) {
                // 라면
                System.out.println("========================================");
                System.out.println("            [ 라면 ] (1박스=40개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", shinRamen.name, shinRamen.buyPrice, shinRamen.sellPrice, getTotalStock(shinRamen));
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", jinRamen.name, jinRamen.buyPrice, jinRamen.sellPrice, getTotalStock(jinRamen));
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", neoguri.name, neoguri.buyPrice, neoguri.sellPrice, getTotalStock(neoguri));
            } else if (category == 9) {
                // 아이스크림
                System.out.println("========================================");
                System.out.println("        [ 아이스크림 ] (1박스=24개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", melona.name, melona.buyPrice, melona.sellPrice, getTotalStock(melona));
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", screwBar.name, screwBar.buyPrice, screwBar.sellPrice, getTotalStock(screwBar));
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", fishBread.name, fishBread.buyPrice, fishBread.sellPrice, getTotalStock(fishBread));
            } else if (category == 10) {
                // 폭죽
                System.out.println("========================================");
                System.out.println("           [ 폭죽 ] (1박스=10개)");
                System.out.println("========================================");
                System.out.printf("1. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", sparkler.name, sparkler.buyPrice, sparkler.sellPrice, getTotalStock(sparkler));
                System.out.printf("2. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", romanCandle.name, romanCandle.buyPrice, romanCandle.sellPrice, getTotalStock(romanCandle));
                System.out.printf("3. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n", fountain.name, fountain.buyPrice, fountain.sellPrice, getTotalStock(fountain));
            }

            System.out.println();
            System.out.println("구매할 상품 번호 (0: 돌아가기)");
            System.out.print(">> ");

            int productChoice = Util.readInt(scanner);

            if (productChoice == 0) {
                buying = false;
            } else {
                // 수량 입력
                System.out.print("수량 입력 >> ");
                int quantity = Util.readInt(scanner);

                // 상품 구매 처리
                purchaseProduct(category, productChoice, quantity);
            }
        }
    }

    /// <summary>
    /// 상품 구매 처리
    /// </summary>
    private static void purchaseProduct(int category, int productNum, int quantity) {
        // 카테고리와 상품 번호로 상품 찾기
        Product product = getProductByCategoryAndNum(category, productNum);

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
    /// 카테고리와 번호로 상품 찾기
    /// </summary>
    private static Product getProductByCategoryAndNum(int category, int num) {
        if (category == 1) {
            // 음료
            if (num == 1) return cola;
            if (num == 2) return cider;
            if (num == 3) return water;
            if (num == 4) return pocari;
            if (num == 5) return ipro;
        } else if (category == 2) {
            // 맥주
            if (num == 1) return cass;
            if (num == 2) return terra;
            if (num == 3) return hite;
        } else if (category == 3) {
            // 소주
            if (num == 1) return chamisul;
            if (num == 2) return cheumcherum;
            if (num == 3) return jinro;
        } else if (category == 4) {
            // 간식/안주
            if (num == 1) return driedSquid;
            if (num == 2) return peanut;
            if (num == 3) return chip;
        } else if (category == 5) {
            // 고기
            if (num == 1) return samgyupsal;
            if (num == 2) return moksal;
            if (num == 3) return sausage;
        } else if (category == 6) {
            // 해수욕 용품
            if (num == 1) return tube;
            if (num == 2) return sunscreen;
            if (num == 3) return beachBall;
        } else if (category == 7) {
            // 식재료
            if (num == 1) return ssamjang;
            if (num == 2) return lettuce;
            if (num == 3) return kimchi;
        } else if (category == 8) {
            // 라면
            if (num == 1) return shinRamen;
            if (num == 2) return jinRamen;
            if (num == 3) return neoguri;
        } else if (category == 9) {
            // 아이스크림
            if (num == 1) return melona;
            if (num == 2) return screwBar;
            if (num == 3) return fishBread;
        } else if (category == 10) {
            // 폭죽
            if (num == 1) return sparkler;
            if (num == 2) return romanCandle;
            if (num == 3) return fountain;
        }
        return null;
    }

    /// <summary>
    /// 정책 설정
    /// </summary>
    private static void setPolicies() {
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
    private static void setCategoryPolicy() {
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
        String categoryName = getCategoryName(categoryChoice);

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
            switch (categoryChoice) {
                case 1:
                    autoOrderDrink = true;
                    thresholdDrink = threshold;
                    break;
                case 2:
                    autoOrderBeer = true;
                    thresholdBeer = threshold;
                    break;
                case 3:
                    autoOrderSoju = true;
                    thresholdSoju = threshold;
                    break;
                case 4:
                    autoOrderSnack = true;
                    thresholdSnack = threshold;
                    break;
                case 5:
                    autoOrderMeat = true;
                    thresholdMeat = threshold;
                    break;
                case 6:
                    autoOrderBeach = true;
                    thresholdBeach = threshold;
                    break;
                case 7:
                    autoOrderGrocery = true;
                    thresholdGrocery = threshold;
                    break;
                case 8:
                    autoOrderRamen = true;
                    thresholdRamen = threshold;
                    break;
                case 9:
                    autoOrderIcecream = true;
                    thresholdIcecream = threshold;
                    break;
                case 10:
                    autoOrderEtc = true;
                    thresholdEtc = threshold;
                    break;
            }

            System.out.println("[OK] " + categoryName + " 카테고리 등록 완료 (임계값: " + threshold + "개)");

        } else if (actionChoice == 2) {
            // 자동주문 해제
            switch (categoryChoice) {
                case 1:
                    autoOrderDrink = false;
                    break;
                case 2:
                    autoOrderBeer = false;
                    break;
                case 3:
                    autoOrderSoju = false;
                    break;
                case 4:
                    autoOrderSnack = false;
                    break;
                case 5:
                    autoOrderMeat = false;
                    break;
                case 6:
                    autoOrderBeach = false;
                    break;
                case 7:
                    autoOrderGrocery = false;
                    break;
                case 8:
                    autoOrderRamen = false;
                    break;
                case 9:
                    autoOrderIcecream = false;
                    break;
                case 10:
                    autoOrderEtc = false;
                    break;
            }

            System.out.println("[OK] " + categoryName + " 카테고리 자동주문 해제됨");
        }
    }

    /// <summary>
    /// 카테고리명 가져오기
    /// </summary>
    private static String getCategoryName(int category) {
        return switch (category) {
            case 1 -> "음료";
            case 2 -> "맥주";
            case 3 -> "소주";
            case 4 -> "간식/안주";
            case 5 -> "고기";
            case 6 -> "해수욕용품";
            case 7 -> "식재료";
            case 8 -> "라면";
            case 9 -> "아이스크림";
            case 10 -> "폭죽";
            default -> "";
        };
    }

    /// <summary>
    /// 개별 상품 정책 설정
    /// </summary>
    private static void setIndividualPolicy() {
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

        Product product = getProductByCategoryAndNum(categoryChoice, productNum);

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
    private static void printCategoryProductsForPolicy(int category) {
        switch (category) {
            case 1:
                System.out.println("[ 음료 ]");
                System.out.printf("1. %s (재고: %d)%n", cola.name, getTotalStock(cola));
                System.out.printf("2. %s (재고: %d)%n", cider.name, getTotalStock(cider));
                System.out.printf("3. %s (재고: %d)%n", water.name, getTotalStock(water));
                System.out.printf("4. %s (재고: %d)%n", pocari.name, getTotalStock(pocari));
                System.out.printf("5. %s (재고: %d)%n", ipro.name, getTotalStock(ipro));
                break;
            case 2:
                System.out.println("[ 맥주 ]");
                System.out.printf("1. %s (재고: %d)%n", cass.name, getTotalStock(cass));
                System.out.printf("2. %s (재고: %d)%n", terra.name, getTotalStock(terra));
                System.out.printf("3. %s (재고: %d)%n", hite.name, getTotalStock(hite));
                break;
            case 3:
                System.out.println("[ 소주 ]");
                System.out.printf("1. %s (재고: %d)%n", chamisul.name, getTotalStock(chamisul));
                System.out.printf("2. %s (재고: %d)%n", cheumcherum.name, getTotalStock(cheumcherum));
                System.out.printf("3. %s (재고: %d)%n", jinro.name, getTotalStock(jinro));
                break;
            case 4:
                System.out.println("[ 간식/안주 ]");
                System.out.printf("1. %s (재고: %d)%n", driedSquid.name, getTotalStock(driedSquid));
                System.out.printf("2. %s (재고: %d)%n", peanut.name, getTotalStock(peanut));
                System.out.printf("3. %s (재고: %d)%n", chip.name, getTotalStock(chip));
                break;
            case 5:
                System.out.println("[ 고기 ]");
                System.out.printf("1. %s (재고: %d)%n", samgyupsal.name, getTotalStock(samgyupsal));
                System.out.printf("2. %s (재고: %d)%n", moksal.name, getTotalStock(moksal));
                System.out.printf("3. %s (재고: %d)%n", sausage.name, getTotalStock(sausage));
                break;
            case 6:
                System.out.println("[ 해수욕용품 ]");
                System.out.printf("1. %s (재고: %d)%n", tube.name, getTotalStock(tube));
                System.out.printf("2. %s (재고: %d)%n", sunscreen.name, getTotalStock(sunscreen));
                System.out.printf("3. %s (재고: %d)%n", beachBall.name, getTotalStock(beachBall));
                break;
            case 7:
                System.out.println("[ 식재료 ]");
                System.out.printf("1. %s (재고: %d)%n", ssamjang.name, getTotalStock(ssamjang));
                System.out.printf("2. %s (재고: %d)%n", lettuce.name, getTotalStock(lettuce));
                System.out.printf("3. %s (재고: %d)%n", kimchi.name, getTotalStock(kimchi));
                break;
            case 8:
                System.out.println("[ 라면 ]");
                System.out.printf("1. %s (재고: %d)%n", shinRamen.name, getTotalStock(shinRamen));
                System.out.printf("2. %s (재고: %d)%n", jinRamen.name, getTotalStock(jinRamen));
                System.out.printf("3. %s (재고: %d)%n", neoguri.name, getTotalStock(neoguri));
                break;
            case 9:
                System.out.println("[ 아이스크림 ]");
                System.out.printf("1. %s (재고: %d)%n", melona.name, getTotalStock(melona));
                System.out.printf("2. %s (재고: %d)%n", screwBar.name, getTotalStock(screwBar));
                System.out.printf("3. %s (재고: %d)%n", fishBread.name, getTotalStock(fishBread));
                break;
            case 10:
                System.out.println("[ 폭죽 ]");
                System.out.printf("1. %s (재고: %d)%n", sparkler.name, getTotalStock(sparkler));
                System.out.printf("2. %s (재고: %d)%n", romanCandle.name, getTotalStock(romanCandle));
                System.out.printf("3. %s (재고: %d)%n", fountain.name, getTotalStock(fountain));
                break;
        }
    }

    /// <summary>
    /// 현재 정책 확인
    /// </summary>
    private static void showCurrentPolicies() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("         [ 현재 정책 확인 ]");
        System.out.println("========================================");
        System.out.println();

        // 카테고리 정책 출력
        System.out.println("[ 카테고리 정책 ]");
        boolean hasCategoryPolicy = false;

        if (autoOrderDrink) {
            System.out.println(" - 음료: 임계값 " + thresholdDrink + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderBeer) {
            System.out.println(" - 맥주: 임계값 " + thresholdBeer + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderSoju) {
            System.out.println(" - 소주: 임계값 " + thresholdSoju + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderSnack) {
            System.out.println(" - 간식/안주: 임계값 " + thresholdSnack + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderMeat) {
            System.out.println(" - 고기: 임계값 " + thresholdMeat + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderBeach) {
            System.out.println(" - 해수욕용품: 임계값 " + thresholdBeach + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderGrocery) {
            System.out.println(" - 식재료: 임계값 " + thresholdGrocery + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderRamen) {
            System.out.println(" - 라면: 임계값 " + thresholdRamen + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderIcecream) {
            System.out.println(" - 아이스크림: 임계값 " + thresholdIcecream + "개");
            hasCategoryPolicy = true;
        }
        if (autoOrderEtc) {
            System.out.println(" - 폭죽: 임계값 " + thresholdEtc + "개");
            hasCategoryPolicy = true;
        }

        if (!hasCategoryPolicy) {
            System.out.println(" (없음)");
        }

        System.out.println();

        // 개별 상품 정책 출력
        System.out.println("[ 개별 상품 정책 ]");
        boolean hasIndividualPolicy = false;

        // 모든 상품 체크
        if (cola.autoOrderEnabled) {
            System.out.println(" - " + cola.name + ": 임계값 " + cola.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (cider.autoOrderEnabled) {
            System.out.println(" - " + cider.name + ": 임계값 " + cider.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (water.autoOrderEnabled) {
            System.out.println(" - " + water.name + ": 임계값 " + water.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (pocari.autoOrderEnabled) {
            System.out.println(" - " + pocari.name + ": 임계값 " + pocari.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (ipro.autoOrderEnabled) {
            System.out.println(" - " + ipro.name + ": 임계값 " + ipro.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (cass.autoOrderEnabled) {
            System.out.println(" - " + cass.name + ": 임계값 " + cass.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (terra.autoOrderEnabled) {
            System.out.println(" - " + terra.name + ": 임계값 " + terra.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (hite.autoOrderEnabled) {
            System.out.println(" - " + hite.name + ": 임계값 " + hite.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (chamisul.autoOrderEnabled) {
            System.out.println(" - " + chamisul.name + ": 임계값 " + chamisul.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (cheumcherum.autoOrderEnabled) {
            System.out.println(" - " + cheumcherum.name + ": 임계값 " + cheumcherum.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (jinro.autoOrderEnabled) {
            System.out.println(" - " + jinro.name + ": 임계값 " + jinro.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (driedSquid.autoOrderEnabled) {
            System.out.println(" - " + driedSquid.name + ": 임계값 " + driedSquid.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (peanut.autoOrderEnabled) {
            System.out.println(" - " + peanut.name + ": 임계값 " + peanut.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (chip.autoOrderEnabled) {
            System.out.println(" - " + chip.name + ": 임계값 " + chip.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (samgyupsal.autoOrderEnabled) {
            System.out.println(" - " + samgyupsal.name + ": 임계값 " + samgyupsal.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (moksal.autoOrderEnabled) {
            System.out.println(" - " + moksal.name + ": 임계값 " + moksal.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (sausage.autoOrderEnabled) {
            System.out.println(" - " + sausage.name + ": 임계값 " + sausage.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (tube.autoOrderEnabled) {
            System.out.println(" - " + tube.name + ": 임계값 " + tube.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (sunscreen.autoOrderEnabled) {
            System.out.println(" - " + sunscreen.name + ": 임계값 " + sunscreen.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (beachBall.autoOrderEnabled) {
            System.out.println(" - " + beachBall.name + ": 임계값 " + beachBall.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (ssamjang.autoOrderEnabled) {
            System.out.println(" - " + ssamjang.name + ": 임계값 " + ssamjang.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (lettuce.autoOrderEnabled) {
            System.out.println(" - " + lettuce.name + ": 임계값 " + lettuce.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (kimchi.autoOrderEnabled) {
            System.out.println(" - " + kimchi.name + ": 임계값 " + kimchi.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (shinRamen.autoOrderEnabled) {
            System.out.println(" - " + shinRamen.name + ": 임계값 " + shinRamen.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (jinRamen.autoOrderEnabled) {
            System.out.println(" - " + jinRamen.name + ": 임계값 " + jinRamen.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (neoguri.autoOrderEnabled) {
            System.out.println(" - " + neoguri.name + ": 임계값 " + neoguri.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (melona.autoOrderEnabled) {
            System.out.println(" - " + melona.name + ": 임계값 " + melona.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (screwBar.autoOrderEnabled) {
            System.out.println(" - " + screwBar.name + ": 임계값 " + screwBar.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (fishBread.autoOrderEnabled) {
            System.out.println(" - " + fishBread.name + ": 임계값 " + fishBread.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (sparkler.autoOrderEnabled) {
            System.out.println(" - " + sparkler.name + ": 임계값 " + sparkler.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (romanCandle.autoOrderEnabled) {
            System.out.println(" - " + romanCandle.name + ": 임계값 " + romanCandle.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
        }
        if (fountain.autoOrderEnabled) {
            System.out.println(" - " + fountain.name + ": 임계값 " + fountain.autoOrderThreshold + "개");
            hasIndividualPolicy = true;
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
    private static void executeAutoOrder() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("         [ 자동주문 실행 ]");
        System.out.println("========================================");
        System.out.println();

        int totalCost = 0;
        int orderCount = 0;

        // 카테고리 정책 기반 주문 (음료)
        if (autoOrderDrink) {
            totalCost = totalCost + autoOrderProduct(cola, thresholdDrink);
            totalCost = totalCost + autoOrderProduct(cider, thresholdDrink);
            totalCost = totalCost + autoOrderProduct(water, thresholdDrink);
            totalCost = totalCost + autoOrderProduct(pocari, thresholdDrink);
            totalCost = totalCost + autoOrderProduct(ipro, thresholdDrink);
        }

        // 맥주
        if (autoOrderBeer) {
            totalCost = totalCost + autoOrderProduct(cass, thresholdBeer);
            totalCost = totalCost + autoOrderProduct(terra, thresholdBeer);
            totalCost = totalCost + autoOrderProduct(hite, thresholdBeer);
        }

        // 소주
        if (autoOrderSoju) {
            totalCost = totalCost + autoOrderProduct(chamisul, thresholdSoju);
            totalCost = totalCost + autoOrderProduct(cheumcherum, thresholdSoju);
            totalCost = totalCost + autoOrderProduct(jinro, thresholdSoju);
        }

        // 간식/안주
        if (autoOrderSnack) {
            totalCost = totalCost + autoOrderProduct(driedSquid, thresholdSnack);
            totalCost = totalCost + autoOrderProduct(peanut, thresholdSnack);
            totalCost = totalCost + autoOrderProduct(chip, thresholdSnack);
        }

        // 고기
        if (autoOrderMeat) {
            totalCost = totalCost + autoOrderProduct(samgyupsal, thresholdMeat);
            totalCost = totalCost + autoOrderProduct(moksal, thresholdMeat);
            totalCost = totalCost + autoOrderProduct(sausage, thresholdMeat);
        }

        // 해수욕용품
        if (autoOrderBeach) {
            totalCost = totalCost + autoOrderProduct(tube, thresholdBeach);
            totalCost = totalCost + autoOrderProduct(sunscreen, thresholdBeach);
            totalCost = totalCost + autoOrderProduct(beachBall, thresholdBeach);
        }

        // 식재료
        if (autoOrderGrocery) {
            totalCost = totalCost + autoOrderProduct(ssamjang, thresholdGrocery);
            totalCost = totalCost + autoOrderProduct(lettuce, thresholdGrocery);
            totalCost = totalCost + autoOrderProduct(kimchi, thresholdGrocery);
        }

        // 라면
        if (autoOrderRamen) {
            totalCost = totalCost + autoOrderProduct(shinRamen, thresholdRamen);
            totalCost = totalCost + autoOrderProduct(jinRamen, thresholdRamen);
            totalCost = totalCost + autoOrderProduct(neoguri, thresholdRamen);
        }

        // 아이스크림
        if (autoOrderIcecream) {
            totalCost = totalCost + autoOrderProduct(melona, thresholdIcecream);
            totalCost = totalCost + autoOrderProduct(screwBar, thresholdIcecream);
            totalCost = totalCost + autoOrderProduct(fishBread, thresholdIcecream);
        }

        // 폭죽
        if (autoOrderEtc) {
            totalCost = totalCost + autoOrderProduct(sparkler, thresholdEtc);
            totalCost = totalCost + autoOrderProduct(romanCandle, thresholdEtc);
            totalCost = totalCost + autoOrderProduct(fountain, thresholdEtc);
        }

        // 개별 상품 정책 기반 주문 (카테고리에 없는 것만)
        if (!autoOrderDrink) {
            if (cola.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(cola, cola.autoOrderThreshold);
            if (cider.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(cider, cider.autoOrderThreshold);
            if (water.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(water, water.autoOrderThreshold);
            if (pocari.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(pocari, pocari.autoOrderThreshold);
            if (ipro.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(ipro, ipro.autoOrderThreshold);
        }
        if (!autoOrderBeer) {
            if (cass.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(cass, cass.autoOrderThreshold);
            if (terra.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(terra, terra.autoOrderThreshold);
            if (hite.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(hite, hite.autoOrderThreshold);
        }
        if (!autoOrderSoju) {
            if (chamisul.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(chamisul, chamisul.autoOrderThreshold);
            if (cheumcherum.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(cheumcherum, cheumcherum.autoOrderThreshold);
            if (jinro.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(jinro, jinro.autoOrderThreshold);
        }
        if (!autoOrderSnack) {
            if (driedSquid.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(driedSquid, driedSquid.autoOrderThreshold);
            if (peanut.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(peanut, peanut.autoOrderThreshold);
            if (chip.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(chip, chip.autoOrderThreshold);
        }
        if (!autoOrderMeat) {
            if (samgyupsal.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(samgyupsal, samgyupsal.autoOrderThreshold);
            if (moksal.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(moksal, moksal.autoOrderThreshold);
            if (sausage.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(sausage, sausage.autoOrderThreshold);
        }
        if (!autoOrderBeach) {
            if (tube.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(tube, tube.autoOrderThreshold);
            if (sunscreen.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(sunscreen, sunscreen.autoOrderThreshold);
            if (beachBall.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(beachBall, beachBall.autoOrderThreshold);
        }
        if (!autoOrderGrocery) {
            if (ssamjang.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(ssamjang, ssamjang.autoOrderThreshold);
            if (lettuce.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(lettuce, lettuce.autoOrderThreshold);
            if (kimchi.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(kimchi, kimchi.autoOrderThreshold);
        }
        if (!autoOrderRamen) {
            if (shinRamen.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(shinRamen, shinRamen.autoOrderThreshold);
            if (jinRamen.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(jinRamen, jinRamen.autoOrderThreshold);
            if (neoguri.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(neoguri, neoguri.autoOrderThreshold);
        }
        if (!autoOrderIcecream) {
            if (melona.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(melona, melona.autoOrderThreshold);
            if (screwBar.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(screwBar, screwBar.autoOrderThreshold);
            if (fishBread.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(fishBread, fishBread.autoOrderThreshold);
        }
        if (!autoOrderEtc) {
            if (sparkler.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(sparkler, sparkler.autoOrderThreshold);
            if (romanCandle.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(romanCandle, romanCandle.autoOrderThreshold);
            if (fountain.autoOrderEnabled) totalCost = totalCost + autoOrderProduct(fountain, fountain.autoOrderThreshold);
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
    private static int autoOrderProduct(Product product, int threshold) {
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
    private static void startBusiness() {
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

        // 빅 이벤트 체크 (직접 영업: 20% 확률)
        if (checkBigEvent(20)) {
            bigEventOccurred = true;
            System.out.println();
            System.out.println("★★★ 빅 이벤트 발생! ★★★");
            System.out.println("대량 주문이 들어왔습니다!");
            Util.delay(1000);
        }

        // 손님 응대 루프
        for (int i = 1; i <= todayCustomers; i++) {

            // 랜덤 손님 유형 (0: 가족, 1: 커플, 2: 친구들, 3: 혼자)
            int customerType = Util.rand(4);

            // 손님 객체 생성
            Customer customer = createCustomer(customerType);

            // 멘트 조합: [손님 인사] + [시간대 멘트]
            String greeting = customerGreetings[customerType][Util.rand(5)];
            String timeMsg = timeGreetings[Util.rand(5)];
            customer.greeting = greeting + " " + timeMsg;

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.printf("[ 손님 %d/%d - %s ]%n", i, todayCustomers, customer.typeName);
            Util.delay(500);
            customer.sayGreeting();
            System.out.println();

            // 쇼핑 리스트 먼저 한번에 출력
            customer.sayWant();

            Util.delay(500);  // 리스트 확인 후 처리
            System.out.println();
            System.out.println("판매 결과:");

            // 손님의 쇼핑 리스트 처리
            int customerSales = 0;
            int customerProfit = 0;

            for (int j = 0; j < customer.wantCount; j++) {
                Product product = customer.wantProducts[j];
                int wantAmount = customer.wantAmounts[j];

                // 수량 0이면 스킵
                if (wantAmount <= 0) {
                    continue;
                }

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

                    System.out.printf(" - %s: OK (+%,d원)%n", product.name, saleAmount);

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

                    System.out.printf(" - %s: %d개만 (+%,d원)%n", product.name, currentStock, saleAmount);

                } else {
                    // 재고 없음
                    failCount++;
                    System.out.printf(" - %s: 재고 없음!%n", product.name);
                }
            }

            // 손님 총액 표시
            if (customerSales > 0) {
                System.out.printf(">> 손님 결제: %,d원%n", customerSales);
            } else {
                System.out.println(">> 아무것도 못 사고 갔습니다...");
            }

            // 다음 손님 또는 스킵 선택 (마지막 손님이 아닌 경우)
            if (i < todayCustomers) {
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

                    for (int k = i + 1; k <= todayCustomers; k++) {
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
                        for (int m = 0; m < 2; m++) {
                            Product p = skipList[m];
                            int want = skipAmounts[m];
                            int stock = display.getDisplayed(p);

                            if (stock >= want) {
                                int sale = p.sellPrice * want;
                                int profit = (p.sellPrice - p.buyPrice) * want;
                                display.sell(p, want);
                                money += sale;
                                todaySales += sale;
                                todayProfit += profit;
                                successCount++;
                            } else if (stock > 0) {
                                int actual = stock;
                                int sale = p.sellPrice * actual;
                                int profit = (p.sellPrice - p.buyPrice) * actual;
                                display.sell(p, actual);
                                money += sale;
                                todaySales += sale;
                                todayProfit += profit;
                                successCount++;
                                failCount++;
                            } else {
                                failCount++;
                            }
                        }
                    }
                    System.out.printf("손님 %d명 처리 완료!%n", todayCustomers - i);
                    break;  // for 루프 종료

                } else if (choice == 0) {
                    // 영업 중단
                    System.out.println();
                    System.out.println("영업을 중단합니다.");
                    todayCustomers = i;  // 정산용 손님 수 조정
                    break;  // for 루프 종료
                }
                // choice == 1 또는 다른 값: 다음 손님 (루프 계속)
            }
        }

        // 하루 정산
        Util.delay(800);  // 정산 준비 연출
        System.out.println();
        System.out.println("========================================");
        System.out.printf("          [ %d일차 정산 ]%n", day);
        System.out.println("========================================");
        if (bigEventOccurred) {
            System.out.println("★ 빅 이벤트 발생!");
        }
        System.out.printf("오늘 방문 손님: %d명%n", todayCustomers);
        System.out.printf("판매 성공: %d건%n", successCount);
        System.out.printf("판매 실패: %d건%n", failCount);
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.printf("  오늘 매출:    %,d원%n", todaySales);
        System.out.printf("  순이익:      +%,d원%n", todayProfit);
        System.out.println("----------------------------------------");
        System.out.printf("  현재 총 자본: %,d원%n", money);
        System.out.printf("  목표까지:     %,d원%n", goalMoney - money);
        System.out.println("========================================");

        System.out.println();
        System.out.println("아무 키나 입력하면 계속...");
        scanner.next();
    }

    /// <summary>
    /// 빠른 영업 (하루 요약)
    /// 손님 상세 없이 결과만 출력
    /// </summary>
    private static void startQuickBusiness() {
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

        // 손님별 간략 처리 (재사용 배열 사용)
        for (int i = 0; i < todayCustomers; i++) {
            int customerType = Util.rand(4);

            // 카테고리별 랜덤 판매 시뮬레이션
            targetsCount = 3;
            if (customerType == 0) {
                targets[0] = getRandomFromCategory(categoryMeat);
                targets[1] = getRandomFromCategory(categoryGrocery);
                targets[2] = getRandomFromCategory(categoryDrink);
            } else if (customerType == 1) {
                targets[0] = getRandomFromCategory(categorySoju);
                targets[1] = getRandomFromCategory(categoryBeer);
                targets[2] = getRandomFromCategory(categorySnack);
            } else if (customerType == 2) {
                targets[0] = getRandomFromCategory(categoryBeer);
                targets[1] = getRandomFromCategory(categorySoju);
                targets[2] = getRandomFromCategory(categorySnack);
            } else {
                targets[0] = getRandomFromCategory(categoryRamen);
                targets[1] = getRandomFromCategory(categoryBeer);
                targets[2] = getRandomFromCategory(categoryIcecream);
            }

            // 각 상품 판매 시도
            for (int j = 0; j < targetsCount; j++) {
                Product p = targets[j];
                int want = 1 + Util.rand(3);
                int stock = display.getDisplayed(p);

                if (stock >= want) {
                    int sale = p.sellPrice * want;
                    int profit = (p.sellPrice - p.buyPrice) * want;
                    display.sell(p, want);

                    money = money + sale;
                    todaySales = todaySales + sale;
                    todayProfit = todayProfit + profit;
                    successCount++;
                } else if (stock > 0) {
                    int actual = stock;
                    int sale = p.sellPrice * actual;
                    int profit = (p.sellPrice - p.buyPrice) * actual;
                    display.sell(p, actual);

                    money = money + sale;
                    todaySales = todaySales + sale;
                    todayProfit = todayProfit + profit;
                    successCount++;
                    failCount++;
                } else {
                    failCount++;
                }
            }
        }

        // 결과 출력
        System.out.println();
        System.out.println("========================================");
        System.out.printf("          [ %d일차 정산 ]%n", day);
        System.out.println("========================================");
        System.out.printf("오늘 방문 손님: %d명%n", todayCustomers);
        System.out.printf("판매 성공: %d건%n", successCount);
        System.out.printf("판매 실패: %d건%n", failCount);
        if (eventOccurred) {
            System.out.println(">> 빅 이벤트 발생!");
        }
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.printf("  오늘 매출:    %,d원%n", todaySales);
        System.out.printf("  순이익:      +%,d원%n", todayProfit);
        System.out.println("----------------------------------------");
        System.out.printf("  현재 총 자본: %,d원%n", money);
        System.out.printf("  목표까지:     %,d원%n", goalMoney - money);
        System.out.println("========================================");

        System.out.println();
        System.out.println("아무 키나 입력하면 계속...");
        scanner.next();
    }

    /// <summary>
    /// 1주일 스킵 (7일 자동 영업)
    /// </summary>
    private static void skipWeek() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("         [ 1주일 스킵 ]");
        System.out.println("========================================");
        System.out.printf("%d일차 ~ %d일차 자동 영업 시작...%n", day, day + 6);
        System.out.println();

        int weekSales = 0;
        int weekProfit = 0;
        int eventCount = 0;

        // 7일 반복
        for (int d = 0; d < 7; d++) {
            System.out.printf("%d일차 영업 중...", day);
            Util.delay(300);

            int todayCustomers = 10 + Util.rand(11);
            int todaySales = 0;
            int todayProfit = 0;

            // 빅 이벤트 체크 (10% 확률)
            if (checkBigEvent(10)) {
                eventCount++;
                System.out.print(" [이벤트!]");
            }

            // 손님별 간략 처리 (재사용 배열 사용)
            for (int i = 0; i < todayCustomers; i++) {
                int customerType = Util.rand(4);
                targetsCount = 3;

                if (customerType == 0) {
                    targets[0] = getRandomFromCategory(categoryMeat);
                    targets[1] = getRandomFromCategory(categoryGrocery);
                    targets[2] = getRandomFromCategory(categoryDrink);
                } else if (customerType == 1) {
                    targets[0] = getRandomFromCategory(categorySoju);
                    targets[1] = getRandomFromCategory(categoryBeer);
                    targets[2] = getRandomFromCategory(categorySnack);
                } else if (customerType == 2) {
                    targets[0] = getRandomFromCategory(categoryBeer);
                    targets[1] = getRandomFromCategory(categorySoju);
                    targets[2] = getRandomFromCategory(categorySnack);
                } else {
                    targets[0] = getRandomFromCategory(categoryRamen);
                    targets[1] = getRandomFromCategory(categoryBeer);
                    targets[2] = getRandomFromCategory(categoryIcecream);
                }

                for (int j = 0; j < targetsCount; j++) {
                    Product p = targets[j];
                    int want = 1 + Util.rand(3);
                    int stock = display.getDisplayed(p);

                    if (stock >= want) {
                        int sale = p.sellPrice * want;
                        int profit = (p.sellPrice - p.buyPrice) * want;
                        display.sell(p, want);

                        money = money + sale;
                        todaySales = todaySales + sale;
                        todayProfit = todayProfit + profit;
                    } else if (stock > 0) {
                        int actual = stock;
                        int sale = p.sellPrice * actual;
                        int profit = (p.sellPrice - p.buyPrice) * actual;
                        display.sell(p, actual);

                        money = money + sale;
                        todaySales = todaySales + sale;
                        todayProfit = todayProfit + profit;
                    }
                }
            }

            weekSales = weekSales + todaySales;
            weekProfit = weekProfit + todayProfit;

            System.out.printf(" 매출 %,d원%n", todaySales);
            day++;
        }

        // 주간 요약
        System.out.println();
        System.out.println("========================================");
        System.out.println("         [ 주간 요약 ]");
        System.out.println("========================================");
        System.out.printf("기간: %d일 영업%n", 7);
        if (eventCount > 0) {
            System.out.printf("빅 이벤트: %d회 발생%n", eventCount);
        }
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.printf("  주간 매출:    %,d원%n", weekSales);
        System.out.printf("  주간 순이익: +%,d원%n", weekProfit);
        System.out.println("----------------------------------------");
        System.out.printf("  현재 총 자본: %,d원%n", money);
        System.out.printf("  목표까지:     %,d원%n", goalMoney - money);
        System.out.println("========================================");

        System.out.println();
        System.out.println("아무 키나 입력하면 계속...");
        scanner.next();
    }

    /// <summary>
    /// 빅 이벤트 체크 및 처리 (10% 확률)
    /// 단체 주문, 펜션 배달, 축제 시즌 중 하나 발생
    /// </summary>
    private static boolean checkBigEvent(int chance) {
        // chance% 확률로 이벤트 발생
        if (Util.rand(100) >= chance) {
            return false;
        }

        int eventType = Util.rand(3);

        if (eventType == 0) {
            // 단체 주문: 음료, 안주 대량 판매
            int bonus = sellBulk(categoryDrink, 10 + Util.rand(10));
            bonus = bonus + sellBulk(categorySnack, 5 + Util.rand(5));
            if (bonus > 0) {
                return true;
            }
        } else if (eventType == 1) {
            // 펜션 배달: 고기, 음료, 식재료 판매
            int bonus = sellBulk(categoryMeat, 5 + Util.rand(5));
            bonus = bonus + sellBulk(categoryDrink, 5 + Util.rand(5));
            bonus = bonus + sellBulk(categoryGrocery, 3 + Util.rand(3));
            if (bonus > 0) {
                return true;
            }
        } else {
            // 축제 시즌: 폭죽, 맥주 대량 판매
            int bonus = sellBulk(categoryFirework, 5 + Util.rand(10));
            bonus = bonus + sellBulk(categoryBeer, 10 + Util.rand(10));
            if (bonus > 0) {
                return true;
            }
        }

        return false;
    }

    /// <summary>
    /// 카테고리에서 대량 판매 처리
    /// 판매된 금액 반환
    /// </summary>
    private static int sellBulk(Product[] category, int amount) {
        int totalSale = 0;
        int sellAmount = amount / category.length;

        for (Product p : category) {
            int stock = display.getDisplayed(p);
            if (stock >= sellAmount) {
                int sale = p.sellPrice * sellAmount;
                display.sell(p, sellAmount);

                money = money + sale;
                totalSale = totalSale + sale;
            } else if (stock > 0) {
                int actual = stock;
                int sale = p.sellPrice * actual;
                display.sell(p, actual);

                money = money + sale;
                totalSale = totalSale + sale;
            }
        }

        return totalSale;
    }

    /// <summary>
    /// 상품 초기화 메서드
    /// 배율을 적용하여 상품 객체 생성
    /// </summary>
    private static void initProducts() {
        // 음료 (1박스 = 24개)
        cola = new Product("코카콜라", 800, 1500 * priceMultiplier, 7, 24);
        cider = new Product("칠성사이다", 800, 1500 * priceMultiplier, 6, 24);
        water = new Product("삼다수", 400, 1000 * priceMultiplier, 5, 24);
        pocari = new Product("포카리스웨트", 1200, 2000 * priceMultiplier, 6, 24);
        ipro = new Product("이프로", 1000, 1800 * priceMultiplier, 5, 24);

        // 맥주 (1박스 = 24개)
        cass = new Product("카스", 1500, 3000 * priceMultiplier, 8, 24);
        terra = new Product("테라", 1600, 3200 * priceMultiplier, 8, 24);
        hite = new Product("하이트", 1400, 2800 * priceMultiplier, 7, 24);

        // 소주 (1박스 = 20병)
        chamisul = new Product("참이슬", 1200, 2500 * priceMultiplier, 9, 20);
        cheumcherum = new Product("처음처럼", 1200, 2500 * priceMultiplier, 8, 20);
        jinro = new Product("진로", 1300, 2600 * priceMultiplier, 7, 20);

        // 간식/안주 (1박스 = 20개)
        driedSquid = new Product("마른오징어", 3000, 6000 * priceMultiplier, 6, 20);
        peanut = new Product("땅콩", 2000, 4000 * priceMultiplier, 5, 20);
        chip = new Product("감자칩", 1500, 3000 * priceMultiplier, 6, 20);

        // 고기 (1판 = 10팩)
        samgyupsal = new Product("삼겹살", 8000, 15000 * priceMultiplier, 10, 10);
        moksal = new Product("목살", 9000, 16000 * priceMultiplier, 9, 10);
        sausage = new Product("소세지", 3000, 6000 * priceMultiplier, 7, 10);

        // 해수욕 용품 (1묶음 = 5개)
        tube = new Product("튜브", 5000, 15000 * priceMultiplier, 7, 5);
        sunscreen = new Product("선크림", 8000, 20000 * priceMultiplier, 8, 5);
        beachBall = new Product("비치볼", 3000, 8000 * priceMultiplier, 5, 5);

        // 식재료 (1박스 = 10개)
        ssamjang = new Product("쌈장", 2000, 4000 * priceMultiplier, 6, 10);
        lettuce = new Product("상추", 2000, 4000 * priceMultiplier, 7, 10);
        kimchi = new Product("김치", 3000, 6000 * priceMultiplier, 5, 10);

        // 라면 (1박스 = 40개)
        shinRamen = new Product("신라면", 800, 1500 * priceMultiplier, 8, 40);
        jinRamen = new Product("진라면", 700, 1400 * priceMultiplier, 7, 40);
        neoguri = new Product("너구리", 800, 1500 * priceMultiplier, 6, 40);

        // 아이스크림 (1박스 = 24개)
        melona = new Product("메로나", 500, 1200 * priceMultiplier, 7, 24);
        screwBar = new Product("스크류바", 600, 1300 * priceMultiplier, 6, 24);
        fishBread = new Product("붕어싸만코", 800, 1500 * priceMultiplier, 6, 24);

        // 기타 - 폭죽 (1박스 = 10개)
        sparkler = new Product("불꽃막대", 3000, 8000 * priceMultiplier, 8, 10);
        romanCandle = new Product("로만캔들", 5000, 15000 * priceMultiplier, 9, 10);
        fountain = new Product("분수폭죽", 7000, 20000 * priceMultiplier, 8, 10);

        // 카테고리별 상품 배열 초기화
        categoryDrink = new Product[]{cola, cider, water, pocari, ipro};
        categoryBeer = new Product[]{cass, terra, hite};
        categorySoju = new Product[]{chamisul, cheumcherum, jinro};
        categorySnack = new Product[]{driedSquid, peanut, chip};
        categoryMeat = new Product[]{samgyupsal, moksal, sausage};
        categoryBeach = new Product[]{tube, sunscreen, beachBall};
        categoryGrocery = new Product[]{ssamjang, lettuce, kimchi};
        categoryRamen = new Product[]{shinRamen, jinRamen, neoguri};
        categoryIcecream = new Product[]{melona, screwBar, fishBread};
        categoryFirework = new Product[]{sparkler, romanCandle, fountain};

        // 전체 카테고리 배열 초기화 (순회용)
        allCategories = new Product[][]{
            categoryDrink, categoryBeer, categorySoju, categorySnack, categoryMeat,
            categoryBeach, categoryGrocery, categoryRamen, categoryIcecream, categoryFirework
        };

        // 전체 상품 배열 초기화 (순회용)
        allProducts = new Product[]{
            cola, cider, water, pocari, ipro,
            cass, terra, hite,
            chamisul, cheumcherum, jinro,
            driedSquid, peanut, chip,
            samgyupsal, moksal, sausage,
            tube, sunscreen, beachBall,
            ssamjang, lettuce, kimchi,
            shinRamen, jinRamen, neoguri,
            melona, screwBar, fishBread,
            sparkler, romanCandle, fountain
        };

        // 상품 이름 맵 초기화 (O(1) 조회용)
        initProductMap();
    }

    /// <summary>
    /// 상품 이름 맵 초기화
    /// 상품명과 별칭을 모두 등록하여 productMap.get()으로 O(1) 조회 가능
    /// </summary>
    private static void initProductMap() {
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

        // 맥주
        productMap.put("카스", cass);
        productMap.put("테라", terra);
        productMap.put("하이트", hite);

        // 소주
        productMap.put("참이슬", chamisul);
        productMap.put("처음처럼", cheumcherum);
        productMap.put("진로", jinro);

        // 간식/안주
        productMap.put("마른오징어", driedSquid);
        productMap.put("오징어", driedSquid);
        productMap.put("땅콩", peanut);
        productMap.put("감자칩", chip);
        productMap.put("과자", chip);
        productMap.put("칩", chip);

        // 고기
        productMap.put("삼겹살", samgyupsal);
        productMap.put("목살", moksal);
        productMap.put("소세지", sausage);

        // 해수욕 용품
        productMap.put("튜브", tube);
        productMap.put("선크림", sunscreen);
        productMap.put("비치볼", beachBall);

        // 식재료
        productMap.put("쌈장", ssamjang);
        productMap.put("상추", lettuce);
        productMap.put("김치", kimchi);

        // 라면
        productMap.put("신라면", shinRamen);
        productMap.put("진라면", jinRamen);
        productMap.put("너구리", neoguri);

        // 아이스크림
        productMap.put("메로나", melona);
        productMap.put("스크류바", screwBar);
        productMap.put("붕어싸만코", fishBread);
        productMap.put("붕어", fishBread);

        // 폭죽
        productMap.put("불꽃막대", sparkler);
        productMap.put("로만캔들", romanCandle);
        productMap.put("분수폭죽", fountain);
    }

    /// <summary>
    /// 카테고리에서 랜덤 상품 1개 선택
    /// </summary>
    private static Product getRandomFromCategory(Product[] category) {
        int index = Util.rand(category.length);
        return category[index];
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
    private static void fillArrangeBuffer(int categoryIndex, Product[] category) {
        for (Product p : category) {
            // 창고에 재고가 있고 매대에 진열되지 않은 상품
            if (warehouse.getStock(p) > 0 && display.getDisplayed(p) == 0) {
                arrangeCategoriesBuffer[categoryIndex][arrangeCategoryCounts[categoryIndex]++] = p;
            }
        }
    }

    /// <summary>
    /// 손님 객체 생성 (유형별 구매 목록 설정)
    /// </summary>
    static Customer createCustomer(int type) {
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

            c.wantAmounts[0] = 2 + Util.rand(2);           // 소주 (필수)
            c.wantAmounts[1] = 2 + Util.rand(3);           // 맥주 (필수)
            c.wantAmounts[2] = 1 + Util.rand(2);           // 안주1 (필수)
            c.wantAmounts[3] = 1 + Util.rand(2);           // 안주2 (필수)
            c.wantAmounts[4] = maybeBuy(1 + Util.rand(2)); // 음료 (선택 50%)
            c.wantAmounts[5] = maybeBuy(1 + Util.rand(2)); // 아이스크림 (선택 50%)

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

            c.wantAmounts[0] = 2 + Util.rand(3);           // 라면 (필수)
            c.wantAmounts[1] = 2 + Util.rand(2);           // 맥주 (필수)
            c.wantAmounts[2] = maybeBuy(1 + Util.rand(2)); // 음료 (선택 50%)
            c.wantAmounts[3] = maybeBuy(1 + Util.rand(2)); // 아이스크림 (선택 50%)
            c.wantAmounts[4] = maybeBuy(1 + Util.rand(2)); // 안주 (선택 50%)
        }

        return c;
    }

    /// <summary>
    /// 카테고리에서 재고 있는 상품 우선 선택
    /// 재고 있는 상품이 없으면 랜덤 선택 (재고 없음 처리)
    /// </summary>
    static Product getAvailableFromCategory(Product[] category) {
        // 재고 있는 상품들 먼저 모음 (재사용 배열 사용)
        int count = 0;

        for (Product p : category) {
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
        return category[Util.rand(category.length)];
    }
}
