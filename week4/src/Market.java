import java.util.Scanner;

/// <summary>
/// 마켓(슈퍼마켓) 클래스
/// 게임 루프, 시간 관리, 메뉴 표시를 담당
/// 실제 로직은 각 매니저 클래스에 위임
///   - ProductCatalog: 상품/카테고리 데이터
///   - Inventory: 재고 관리 (창고+매대)
///   - Wholesaler: 도매상/구매/자동주문
///   - Cashier: 영업/손님/판매
/// </summary>
public class Market {

    // ========== 상수 ==========

    private static final int MAX_SLOT = 30;                   // 매대 최대 슬롯 (상품 50개 중 30개만 진열 가능)
    private static final int MAX_DISPLAY_PER_SLOT = 15;      // 슬롯당 최대 진열 수량
    private static final int MIN_SLOT_FOR_BUSINESS = 15;     // 영업 시작 최소 슬롯 (50%)

    // 시간대 상수 (Cashier에서 Market.TIME_MORNING 등으로 참조)
    public static final int TIME_MORNING = 0;    // 아침 (도매상 이용 가능)
    public static final int TIME_AFTERNOON = 1;  // 낮 (영업 전반부)
    public static final int TIME_NIGHT = 2;      // 밤 (영업 후반부)

    // ========== 게임 상태 ==========
    // 협력 객체들이 market.money 등으로 직접 접근

    public int money;                              // Wholesaler, Cashier에서 읽기/쓰기
    public int goalMoney;                          // Cashier에서 읽기
    private int priceMultiplier;                   // run()에서만 사용
    public int day = 1;                            // Cashier에서 읽기
    public int timeOfDay = TIME_MORNING;           // Cashier에서 읽기

    private Scanner scanner;

    // ========== 협력 객체 ==========

    private ProductCatalog catalog;       // 상품/카테고리 데이터
    private Inventory inventory;          // 재고 관리 (창고+매대)
    private Wholesaler wholesaler;        // 도매상/구매/자동주문
    private Cashier cashier;              // 영업/손님/판매

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
    /// 협력 객체 초기화 → 게임 루프
    /// </summary>
    public void run() {
        // 협력 객체 초기화
        catalog = new ProductCatalog(priceMultiplier);
        inventory = new Inventory(MAX_SLOT, MAX_DISPLAY_PER_SLOT, scanner);
        wholesaler = new Wholesaler(this, inventory, catalog, scanner);
        cashier = new Cashier(this, inventory, catalog, scanner);

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
                        wholesaler.goWholesaler();
                        timeOfDay = TIME_AFTERNOON;  // 도매상 갔다오면 낮으로 전환
                    } else {
                        System.out.println("[!!] 도매상은 오전에만 이용 가능합니다.");
                    }
                    break;

                case 2:
                    // 영업 시작 전 매대 체크
                    if (inventory.display.getUsedSlots() < MIN_SLOT_FOR_BUSINESS) {
                        System.out.println();
                        System.out.println("[!!] 매대가 부족합니다!");
                        System.out.printf("    현재: %d칸 / 최소: %d칸 (50%%)%n", inventory.display.getUsedSlots(), MIN_SLOT_FOR_BUSINESS);
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
                            cashier.startBusiness();
                            advanceTime();  // 아침→낮, 낮→밤
                            break;
                        case 2:
                            // 빠른 영업
                            cashier.startQuickBusiness();
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

    // ========== 시간 관리 ==========

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

    // ========== 메뉴 ==========

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
        System.out.println("매대 현황: " + inventory.display.getUsedSlots() + " / " + MAX_SLOT + "칸");
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
    /// 선택한 영업 타입 반환 (1: 직접, 2: 빠른, 0: 취소)
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
                    inventory.showInventory(scanner, catalog);
                    break;
                case 2:
                    inventory.manageDisplay(scanner, catalog);
                    break;
                case 0:
                    managing = false;
                    break;
            }
        }
    }

    // ========== 승리/패배 ==========

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
}
