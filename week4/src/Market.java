import java.util.Scanner;

/// <summary>
/// 마켓(슈퍼마켓) 클래스
/// 가게 운영 메뉴 표시, 영업 준비 확인, 재고 관리 메뉴를 담당
/// 게임 루프와 게임 상태는 GameManager가 관리
/// </summary>
public class Market {

    // ========== 매대 상수 ==========

    public static final int MAX_SLOT = 30;                   // 매대 최대 슬롯 (상품 50개 중 30개만 진열 가능)
    public static final int MAX_DISPLAY_PER_SLOT = 15;      // 슬롯당 최대 진열 수량
    private static final int MIN_SLOT_FOR_BUSINESS = 15;    // 영업 시작 최소 슬롯 (50%)

    // ========== 협력 객체 ==========

    private final GameManager game;            // 게임 상태 접근용 (money, day, timeOfDay)
    private final Inventory inventory;         // 재고 관리 (창고+매대)
    private final ProductCatalog catalog;      // 상품/카테고리 데이터
    private final Scanner scanner;

    // ========== 생성자 ==========

    /// <summary>
    /// Market 생성자
    /// GameManager에서 생성하며, 게임 상태와 협력 객체를 전달받음
    /// </summary>
    public Market(GameManager game, Inventory inventory, ProductCatalog catalog, Scanner scanner) {
        this.game = game;
        this.inventory = inventory;
        this.catalog = catalog;
        this.scanner = scanner;
    }

    // ========== 메뉴 ==========

    /// <summary>
    /// 하루 시작 메뉴 출력
    /// </summary>
    public void printDailyMenu() {
        Util.clearScreen();

        // ========== 헤더: 날짜 + 시간대 표시 ==========
        System.out.println("========================================");
        switch (game.timeOfDay) {
            case GameManager.TIME_MORNING:
                System.out.println("          [  " + game.day + "일차 - 아침  ]");
                break;
            case GameManager.TIME_AFTERNOON:
                System.out.println("          [  " + game.day + "일차 - 낮  ]");
                break;
            case GameManager.TIME_NIGHT:
                System.out.println("          [  " + game.day + "일차 - 밤  ]");
                break;
        }
        System.out.println("========================================");

        // 현재 자본과 매대 사용 현황
        System.out.println("현재 자본: " + String.format("%,d", game.money) + "원");
        System.out.println("매대 현황: " + inventory.display.getUsedSlots() + " / " + MAX_SLOT + "칸");
        System.out.println();

        // ========== 메뉴 항목: 시간대별로 이용 가능한 메뉴가 다름 ==========
        switch (game.timeOfDay) {
            case GameManager.TIME_MORNING:
                // 아침: 도매상, 영업, 재고/매대 모두 가능
                System.out.println("[1] 도매상 가기 (오전 소비)");
                System.out.println("[2] 영업 시작");
                System.out.println("[3] 재고/매대 관리");
                break;
            case GameManager.TIME_AFTERNOON:
                // 낮: 도매상 마감, 영업과 재고/매대만 가능
                System.out.println("[1] (도매상 마감)");
                System.out.println("[2] 영업 시작");
                System.out.println("[3] 재고/매대 관리");
                break;
            case GameManager.TIME_NIGHT:
                // 밤: 도매상 마감, 영업/재고/매대 + 다음 날 넘기기 가능
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
    /// 영업 준비 확인
    /// 매대 슬롯이 최소 기준 미달이면 경고 출력 후 false 반환
    /// </summary>
    public boolean isBusinessReady() {
        // 매대에 진열된 슬롯 수가 최소 기준(50%)에 미달하면 영업 불가
        if (inventory.display.getUsedSlots() < MIN_SLOT_FOR_BUSINESS) {
            // 경고 메시지: 현재 슬롯 수와 최소 기준 안내
            System.out.println();
            System.out.println("[!!] 매대가 부족합니다!");
            System.out.printf("    현재: %d칸 / 최소: %d칸 (50%%)%n", inventory.display.getUsedSlots(), MIN_SLOT_FOR_BUSINESS);
            System.out.println("    도매상에서 상품을 구매하고 매대에 진열하세요.");
            System.out.println();
            System.out.println("아무 키나 입력하면 돌아갑니다...");
            scanner.next();
            return false;
        }
        // 기준 충족 -> 영업 가능
        return true;
    }

    /// <summary>
    /// 영업 시작 서브메뉴
    /// 선택한 영업 타입 반환 (1: 직접, 2: 빠른, 0: 취소)
    /// </summary>
    public int showBusinessMenu() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("           [ 영업 시작 ]");
        System.out.println("========================================");
        System.out.println();

        // 영업 방식 선택: 직접(손님 한 명씩) vs 빠른(결과 요약)
        System.out.println("[1] 직접 영업 (손님 한 명씩 응대)");
        System.out.println("[2] 빠른 영업 (결과만 요약)");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        // 사용자 입력 반환 (잘못된 입력 시 INVALID_INPUT -> GameManager에서 무시됨)
        return Util.readInt(scanner);
    }

    /// <summary>
    /// 재고/매대 관리 서브메뉴
    /// </summary>
    public void showInventoryMenu() {
        // 0 혹은 아무거나 입력 전까지 반복 (여러 작업을 연속으로 할 수 있도록)
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

            int choice = Util.readInt(scanner);

            switch (choice) {
                case 1:
                    // 매대에 진열된 상품과 수량 확인
                    inventory.showInventory(scanner, catalog);
                    break;
                case 2:
                    // 창고->매대 진열, 매대->창고 회수, 자동배정 등
                    inventory.manageDisplay(scanner, catalog);
                    break;
                default:
                    managing = false;
                    break;
            }
        }
    }
}
