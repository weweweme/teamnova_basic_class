import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/// <summary>
/// 마켓(슈퍼마켓) 클래스
/// 가게 전체를 관리: 메뉴 UI + 창고/매대 + 캐셔 + 영업(손님 응대) + 재고 관리
/// 게임 루프와 게임 상태는 GameManager가 관리
/// </summary>
public class Market {

    // ========== 매대 상수 ==========

    public static final int MAX_SLOT = 30;                   // 매대 최대 슬롯 (상품 50개 중 30개만 진열 가능)
    public static final int MAX_DISPLAY_PER_SLOT = 15;      // 슬롯당 최대 진열 수량
    private static final int MIN_SLOT_FOR_BUSINESS = 15;    // 영업 시작 최소 슬롯 (50%)

    // ========== 가게 시설 ==========

    public Warehouse warehouse;      // 창고 (Wholesaler에서 market.warehouse로 접근)
    public Display display;          // 매대 (Wholesaler에서 접근)

    private final Cashier cashier;         // 계산대 (결제 처리 전용)

    // ========== 협력 객체 ==========

    private final GameManager game;            // 게임 상태 접근용 (day, timeOfDay, goalMoney)
    public final ProductCatalog catalog;       // 상품/카테고리 데이터 (Wholesaler에서 접근)
    private final Scanner scanner;

    // ========== 재사용 배열 (메서드 내 반복 할당 방지) ==========

    // autoArrangeDisplay()용 - 라운드 로빈 진열 시 각 카테고리의 현재 탐색 위치
    private final int[] categoryIndex = new int[ProductCatalog.CATEGORY_COUNT];

    // 손님이 매대에서 물건을 담는 바구니 (매 손님마다 비우고 재사용)
    private final Map<Product, Integer> basket = new HashMap<>();

    // ========== 생성자 ==========

    /// <summary>
    /// Market 생성자
    /// 창고와 매대를 직접 생성하고, 게임 상태와 협력 객체를 전달받음
    /// </summary>
    public Market(GameManager game, ProductCatalog catalog, Scanner scanner) {
        this.game = game;
        this.catalog = catalog;
        this.scanner = scanner;
        this.warehouse = new Warehouse();
        this.display = new Display(MAX_SLOT, MAX_DISPLAY_PER_SLOT);
        this.cashier = new Cashier();
    }

    // ========== 재고 조회 ==========

    /// <summary>
    /// 총 재고 조회 (창고 + 매대)
    /// </summary>
    public int getTotalStock(Product p) {
        return warehouse.getStock(p) + display.getDisplayed(p);
    }

    // ========== 매대 판매 ==========

    /// <summary>
    /// 카테고리에서 대량 판매 처리
    /// 카테고리 인덱스로 카테고리를 찾아 매대에서 판매
    /// 판매된 총 금액 반환 (money 처리는 호출자가 담당)
    /// </summary>
    public int sellBulk(int categoryIndex, int amount) {
        Category category = catalog.allCategories[categoryIndex];
        int totalSale = 0;
        // 총 요청 수량을 카테고리 내 상품 수로 균등 분배
        int sellAmount = amount / category.products.length;

        for (Product p : category.products) {
            int stock = display.getDisplayed(p);
            if (stock >= sellAmount) {
                int sale = p.sellPrice * sellAmount;
                display.sell(p, sellAmount);
                totalSale = totalSale + sale;
            } else if (stock > 0) {
                int sale = p.sellPrice * stock;
                display.sell(p, stock);
                totalSale = totalSale + sale;
            }
        }

        return totalSale;
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
        System.out.println("매대 현황: " + display.getUsedSlots() + " / " + MAX_SLOT + "칸");
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
        if (display.getUsedSlots() < MIN_SLOT_FOR_BUSINESS) {
            // 경고 메시지: 현재 슬롯 수와 최소 기준 안내
            System.out.println();
            System.out.println("[!!] 매대가 부족합니다!");
            System.out.printf("    현재: %d칸 / 최소: %d칸 (50%%)%n", display.getUsedSlots(), MIN_SLOT_FOR_BUSINESS);
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
                    showDisplayStock();
                    break;
                case 2:
                    // 창고->매대 진열, 매대->창고 회수, 자동배정 등
                    manageDisplay();
                    break;
                default:
                    managing = false;
                    break;
            }
        }
    }

    // ========== 매대 관리 ==========

    /// <summary>
    /// 매대 관리
    /// </summary>
    private void manageDisplay() {
        // 0 입력 전까지 반복 (여러 작업을 연속으로 할 수 있도록)
        boolean managing = true;

        while (managing) {
            Util.clearScreen();
            System.out.println("========================================");
            System.out.println("            [ 매대 관리 ]");
            System.out.println("========================================");
            System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
            System.out.println();
            System.out.println("[1] 상품 진열 (창고 -> 매대)");
            System.out.println("[2] 상품 회수 (매대 -> 창고)");
            System.out.println("[3] 창고 재고 확인");
            System.out.println("[4] 자동 배정 (카테고리 균형)");
            System.out.println("[0] 돌아가기");
            System.out.print(">> ");

            int choice = Util.readInt(scanner);

            switch (choice) {
                case 1:
                    // 창고에서 상품을 골라 매대에 올림
                    displayProduct();
                    break;
                case 2:
                    // 매대에서 상품을 골라 창고로 되돌림
                    returnProduct();
                    break;
                case 3:
                    // 창고에 남아있는 재고 목록 확인
                    showWarehouse();
                    break;
                case 4:
                    // 기존 상품 보충 + 빈 슬롯에 카테고리 균형 배정
                    autoArrangeDisplay();
                    break;
                case 0:
                    managing = false;
                    break;
            }
        }
    }

    /// <summary>
    /// 상품 진열 (창고 -> 매대)
    /// </summary>
    private void displayProduct() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("        [ 상품 진열 ] 창고 -> 매대");
        System.out.println("========================================");
        System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
        System.out.println();

        // 매대가 꽉 찼으면 경고 (이미 진열 중인 상품에 수량 추가만 가능)
        if (!display.hasEmptySlot()) {
            System.out.println("[!!] 매대가 꽉 찼습니다! (이미 진열된 상품만 추가 가능)");
            System.out.println();
        }

        // ========== 창고 재고 목록 출력 ==========
        // stockList: 번호로 상품을 선택하기 위한 매핑 배열
        // 출력 시 이미 매대에 진열 중인 상품은 [진열중 n/15] 표시
        Product[] stockList = new Product[catalog.allProducts.length];
        System.out.println("--- 창고 재고 ---");
        int num = 0;
        for (Product p : catalog.allProducts) {
            int stock = warehouse.getStock(p);
            if (stock > 0) {
                stockList[num] = p;
                num++;
                int displayed = display.getDisplayed(p);
                if (displayed > 0) {
                    System.out.printf("%d. %s (창고: %d개) [진열중 %d/%d]%n",
                        num, p.name, stock, displayed, MAX_DISPLAY_PER_SLOT);
                } else {
                    System.out.printf("%d. %s (창고: %d개)%n", num, p.name, stock);
                }
            }
        }

        // 창고가 비어있으면 진열 불가
        if (num == 0) {
            System.out.println("  창고가 비어있습니다.");
            System.out.println();
            System.out.println("아무 키나 입력하면 돌아갑니다...");
            scanner.next();
            return;
        }

        // ========== 상품 선택 ==========
        System.out.println();
        System.out.print("진열할 상품 번호 입력 (취소: 0): ");
        int productNum = Util.readInt(scanner);

        if (productNum == 0 || productNum == Util.INVALID_INPUT) {
            return;
        }

        if (productNum < 1 || productNum > num) {
            System.out.println("[!!] 잘못된 번호입니다.");
            return;
        }

        // 사용자 입력은 1번부터, 배열은 0번부터이므로 -1
        Product product = stockList[productNum - 1];
        int warehouseStock = warehouse.getStock(product);

        // ========== 슬롯/수량 체크 ==========
        // 매대에 처음 올리는 상품이면 빈 슬롯이 필요
        int currentDisplay = display.getDisplayed(product);
        boolean isNewOnDisplay = (currentDisplay == 0);
        if (isNewOnDisplay && !display.hasEmptySlot()) {
            System.out.println("[!!] 매대 공간이 부족합니다. (" + display.getUsedSlots() + "/" + MAX_SLOT + "칸)");
            return;
        }

        // 슬롯당 최대 수량에서 현재 진열량을 뺀 만큼만 추가 가능
        int maxCanDisplay = MAX_DISPLAY_PER_SLOT - currentDisplay;
        if (maxCanDisplay <= 0) {
            System.out.printf("[!!] 매대가 가득 찼습니다. (최대 %d개)%n", MAX_DISPLAY_PER_SLOT);
            return;
        }

        // ========== 수량 입력 ==========
        // 'a' 입력 시 가능한 최대 수량으로 자동 설정
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

        // 입력 수량이 진열 가능량을 초과하면 자동 조정
        if (amount > maxCanDisplay) {
            System.out.printf("[!!] 매대 공간 제한으로 %d개만 진열합니다. (최대 %d개)%n", maxCanDisplay, MAX_DISPLAY_PER_SLOT);
            amount = maxCanDisplay;
        }

        // ========== 진열 처리 ==========
        // Display 클래스가 창고 차감 + 매대 추가 + 슬롯 관리를 자동으로 처리
        int displayed = display.displayFromWarehouse(product, warehouse, amount);

        System.out.printf("[OK] %s %d개 매대에 진열! (매대: %d/%d개)%n", product.name, displayed, display.getDisplayed(product), MAX_DISPLAY_PER_SLOT);
    }

    /// <summary>
    /// 상품 회수 (매대 -> 창고)
    /// </summary>
    private void returnProduct() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("        [ 상품 회수 ] 매대 -> 창고");
        System.out.println("========================================");
        System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
        System.out.println();

        // ========== 매대 재고 목록 출력 ==========
        System.out.println("--- 매대 재고 ---");
        int num = 1;
        for (Product p : catalog.allProducts) {
            int stock = display.getDisplayed(p);
            if (stock > 0) {
                System.out.printf("%d. %s (%d개)%n", num++, p.name, stock);
            }
        }

        // 매대가 비어있으면 회수 불가
        if (num == 1) {
            System.out.println("  매대가 비어있습니다.");
            System.out.println();
            System.out.println("아무 키나 입력하면 돌아갑니다...");
            scanner.next();
            return;
        }

        // ========== 상품 선택 (이름으로 검색) ==========
        System.out.println();
        System.out.print("회수할 상품명 입력 (취소: 0): ");
        String productName = scanner.next();

        if (productName.equals("0")) {
            return;
        }

        // productMap에서 이름으로 상품 객체 조회
        Product product = catalog.getProductByName(productName);

        if (product == null) {
            System.out.println("[!!] 상품을 찾을 수 없습니다.");
            return;
        }

        // 매대에 해당 상품이 없으면 회수 불가
        int currentDisplay = display.getDisplayed(product);
        if (currentDisplay == 0) {
            System.out.println("[!!] 매대에 재고가 없습니다.");
            return;
        }

        // ========== 수량 입력 ==========
        // 'a' 입력 시 매대의 전체 수량을 회수
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

        // ========== 회수 처리 ==========
        // Display 클래스가 매대 차감 + 창고 추가 + 슬롯 관리를 자동으로 처리
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
        for (Product p : catalog.allProducts) {
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
            // categoryIndex[cat]: 각 카테고리의 products 배열에서 다음에 확인할 위치
            // category.products를 직접 순회하면서 조건(창고O, 매대X)에 맞는 상품만 진열
            for (int i = 0; i < 10; i++) {
                categoryIndex[i] = 0;
            }

            // 한 카테고리에 몰아서 진열하지 않고, 돌아가며 1개씩 진열한다
            // -> 매대 30칸을 특정 카테고리가 독점하는 것을 방지
            //
            // 동작 예시 (매대 빈 슬롯이 5칸인 경우):
            //   1바퀴: 음료(환타) -> 맥주(클라우드) -> 간식(육포) -> 라면(불닭) -> 아이스크림(보석바) -> 5칸 다 참 -> 종료
            boolean hasMore = true;

            while (hasMore && display.hasEmptySlot()) {
                hasMore = false;  // 이번 바퀴에서 진열한 게 없으면 종료

                // 10개 카테고리를 순서대로 순회 (1바퀴)
                for (int cat = 0; cat < 10; cat++) {
                    Category category = catalog.allCategories[cat];

                    // 매대가 중간에 꽉 찰 수 있으므로 매번 체크
                    if (!display.hasEmptySlot()) {
                        break;
                    }

                    // 이 카테고리에서 조건에 맞는 다음 상품 찾기
                    // 조건: 창고에 재고가 있고(O) + 매대에 아직 없는(X)
                    // (이미 매대에 있는 상품은 1단계에서 보충됨)
                    while (categoryIndex[cat] < category.products.length) {
                        Product product = category.products[categoryIndex[cat]];
                        categoryIndex[cat]++;

                        // 조건에 맞으면 진열하고 다음 카테고리로
                        if (warehouse.getStock(product) > 0 && display.getDisplayed(product) == 0) {
                            int displayed = display.displayFromWarehouse(product, warehouse, MAX_DISPLAY_PER_SLOT);

                            if (displayed > 0) {
                                newDisplayCount++;

                                int remainInWarehouse = warehouse.getStock(product);
                                if (remainInWarehouse > 0) {
                                    System.out.printf(" - [%s] %s %d개 진열 (창고: %d개)%n", category.name, product.name, displayed, remainInWarehouse);
                                } else {
                                    System.out.printf(" - [%s] %s %d개 진열%n", category.name, product.name, displayed);
                                }
                            }

                            hasMore = true;
                            break;  // 라운드 로빈: 1개 진열했으면 다음 카테고리로
                        }
                    }
                }
            }

            // 아무것도 진열하지 못한 경우
            if (newDisplayCount == 0) {
                System.out.println(" (창고에 새로 진열할 상품 없음)");
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

    // ========== 재고 출력 ==========

    /// <summary>
    /// 매대 재고 확인
    /// </summary>
    private void showDisplayStock() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.printf("       [ 매대 현황 ] %d / %d칸%n", display.getUsedSlots(), MAX_SLOT);
        System.out.println("========================================");
        System.out.println();

        // 매대에 진열된 상품을 순회하며 이름과 수량 출력
        boolean hasStock = false;
        for (Product p : catalog.allProducts) {
            int stock = display.getDisplayed(p);
            if (stock > 0) {
                // 상품명을 정렬하여 한 줄로 출력
                printStockBar(p.name, stock);
                hasStock = true;
            }
        }

        // 매대가 비어있으면 안내 메시지
        if (!hasStock) {
            System.out.println("  매대가 비어있습니다.");
            System.out.println("  도매상에서 상품을 입고하세요!");
        }

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
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

        // 전체 상품을 순회하며 창고에 재고가 있는 것만 출력
        for (Product p : catalog.allProducts) {
            int stock = warehouse.getStock(p);
            if (stock > 0) {
                System.out.printf("%-16s %d개%n", p.name, stock);
                hasStock = true;
            }
        }

        // 창고가 비어있으면 안내 메시지
        if (!hasStock) {
            System.out.println("  창고가 비어있습니다.");
        }

        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    /// <summary>
    /// 재고바 출력
    /// 상품명과 수량을 정렬하여 한 줄로 출력
    /// </summary>
    private void printStockBar(String name, int stock) {
        // 한글은 2칸, 영문은 1칸 차지하므로 getDisplayWidth()로 실제 폭 계산
        // maxWidth(16칸)에서 이름 폭을 빼서 나머지를 공백으로 채움 -> 수량이 정렬됨
        int maxWidth = 16;
        int nameWidth = Util.getDisplayWidth(name);
        int padding = maxWidth - nameWidth;

        System.out.print(name);
        for (int i = 0; i < padding; i++) {
            System.out.print(" ");
        }

        System.out.printf("%d개%n", stock);
    }

    // ========== 영업 ==========

    /// <summary>
    /// 직접 영업 (손님 한 명씩 응대)
    /// 손님 생성 → 매대에서 상품 선택 → 캐셔에게 결제 → 정산
    /// 총 매출 반환 (GameManager가 money에 합산)
    /// </summary>
    public int startBusiness() {
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
        int bigEventBonus = 0;   // 빅 이벤트 매출
        boolean bigEventOccurred = false;

        // 손님 응대 루프
        for (int customerNum = 1; customerNum <= todayCustomers; customerNum++) {

            // 손님 절반쯤 지났을 때 빅 이벤트 체크 (20% 확률)
            if (!bigEventOccurred && customerNum == todayCustomers / 2) {
                int bonus = checkBigEvent(20);
                if (bonus > 0) {
                    bigEventOccurred = true;
                    bigEventBonus = bigEventBonus + bonus;
                    Util.delay(1000);
                }
            }

            // 랜덤 손님 유형 (0: 가족, 1: 커플, 2: 친구들, 3: 혼자)
            int customerType = Util.rand(4);

            // 손님이 가게에 들어옴
            Customer customer = customerEntered(customerType);

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.printf("[ 손님 %d/%d - %s ]%n", customerNum, todayCustomers, customer.typeName);
            Util.delay(500);
            customer.sayGreeting();
            System.out.println();

            // 손님이 바구니를 집고 매대에서 직접 상품을 담음
            basket.clear();
            int[] pickResult = customer.pickProducts(display, catalog.allCategories, basket, true);
            successCount = successCount + pickResult[0];
            failCount = failCount + pickResult[1];
            Util.delay(500);

            // 손님이 담은 바구니를 캐셔에게 전달하여 결제
            int[] checkoutResult = cashier.checkout(basket, true);
            todaySales = todaySales + checkoutResult[0];
            todayProfit = todayProfit + checkoutResult[1];

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
                int choice = Util.readInt(scanner);

                if (choice == 2) {
                    // 남은 손님 자동 처리
                    System.out.println();
                    System.out.println("남은 손님을 빠르게 처리합니다...");
                    Util.delay(500);

                    for (int skipNum = customerNum + 1; skipNum <= todayCustomers; skipNum++) {
                        Customer skipCustomer = customerEntered(Util.rand(4));
                        basket.clear();
                        int[] skipPick = skipCustomer.pickProducts(display, catalog.allCategories, basket, false);
                        successCount = successCount + skipPick[0];
                        failCount = failCount + skipPick[1];

                        int[] skipCheckout = cashier.checkout(basket, false);
                        todaySales = todaySales + skipCheckout[0];
                        todayProfit = todayProfit + skipCheckout[1];
                    }
                    System.out.printf("손님 %d명 처리 완료!%n", todayCustomers - customerNum);
                    break;

                } else if (choice == 0) {
                    // 영업 중단
                    System.out.println();
                    System.out.println("영업을 중단합니다.");
                    todayCustomers = customerNum;
                    break;
                }
            }
        }

        // 총 매출 (일반 판매 + 빅 이벤트)
        int totalEarnings = todaySales + bigEventBonus;

        // 하루 정산
        Util.delay(800);
        printDailySettlement(game.day, todayCustomers, successCount, failCount, todaySales, todayProfit, bigEventOccurred, bigEventBonus, totalEarnings);

        System.out.println();
        System.out.println("아무 키나 입력하면 계속...");
        scanner.next();

        return totalEarnings;
    }

    /// <summary>
    /// 빠른 영업 (하루 요약)
    /// 손님 상세 없이 결과만 출력
    /// 총 매출 반환 (GameManager가 money에 합산)
    /// </summary>
    public int startQuickBusiness() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("         [ 빠른 영업 - " + game.day + "일차 ]");
        System.out.println("========================================");
        System.out.println();
        System.out.println("영업 중...");
        Util.delay(1000);

        int todayCustomers = 10 + Util.rand(11);
        int todaySales = 0;
        int todayProfit = 0;
        int successCount = 0;
        int failCount = 0;

        // 빅 이벤트 체크 (10% 확률)
        int bigEventBonus = checkBigEvent(10);
        boolean bigEventOccurred = bigEventBonus > 0;

        // 손님별 처리 (출력 없이)
        for (int customerNum = 0; customerNum < todayCustomers; customerNum++) {
            Customer customer = customerEntered(Util.rand(4));
            basket.clear();
            int[] pickResult = customer.pickProducts(display, catalog.allCategories, basket, false);
            successCount = successCount + pickResult[0];
            failCount = failCount + pickResult[1];

            int[] checkoutResult = cashier.checkout(basket, false);
            todaySales = todaySales + checkoutResult[0];
            todayProfit = todayProfit + checkoutResult[1];
        }

        // 총 매출 (일반 판매 + 빅 이벤트)
        int totalEarnings = todaySales + bigEventBonus;

        // 결과 출력
        printDailySettlement(game.day, todayCustomers, successCount, failCount,
                             todaySales, todayProfit, bigEventOccurred, bigEventBonus, totalEarnings);

        System.out.println();
        System.out.println("아무 키나 입력하면 계속...");
        scanner.next();

        return totalEarnings;
    }

    // ========== 영업 헬퍼 ==========

    /// <summary>
    /// 손님이 가게에 들어옴
    /// 유형별 쇼핑 패턴(카테고리+수량)을 자동 설정하고 인사말 생성
    /// 상품 선택(pickProducts)은 호출하는 쪽에서 별도 수행
    /// </summary>
    private Customer customerEntered(int type) {
        Customer c = new Customer(type);

        // 멘트 조합: [손님 인사] + [시간대 멘트]
        String greeting = Customer.TYPE_GREETINGS[type][Util.rand(5)];
        String timeMsg = switch (game.timeOfDay) {
            case GameManager.TIME_MORNING -> Customer.MORNING_GREETINGS[Util.rand(5)];
            case GameManager.TIME_NIGHT -> Customer.NIGHT_GREETINGS[Util.rand(5)];
            default -> Customer.AFTERNOON_GREETINGS[Util.rand(5)];
        };
        c.greeting = greeting + " " + timeMsg;

        return c;
    }

    /// <summary>
    /// 빅 이벤트 체크 및 처리
    /// 단체 주문, 펜션 배달, 축제 시즌 중 하나 발생
    /// 발생한 이벤트의 매출 반환 (미발생 시 0)
    /// </summary>
    private int checkBigEvent(int chance) {
        // chance% 확률로 이벤트 발생
        if (Util.rand(100) >= chance) {
            return 0;
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
            int bonus = sellBulk(Category.INDEX_DRINK, 10 + Util.rand(10));
            bonus = bonus + sellBulk(Category.INDEX_SNACK, 5 + Util.rand(5));
            if (bonus > 0) {
                System.out.printf(">> 단체 주문 매출: %,d원%n", bonus);
            } else {
                System.out.println(">> 재고 부족으로 주문 처리 실패...");
            }
            return bonus;
        } else if (eventType == 1) {
            // 펜션 배달: 고기, 음료, 식재료 판매
            System.out.println();
            System.out.println("========================================");
            System.out.println("      *** 전화가 왔습니다! ***");
            System.out.println("========================================");
            System.out.println("\"펜션에서 바베큐 세트 배달 부탁드려요!\"");
            System.out.println();
            int bonus = sellBulk(Category.INDEX_MEAT, 5 + Util.rand(5));
            bonus = bonus + sellBulk(Category.INDEX_DRINK, 5 + Util.rand(5));
            bonus = bonus + sellBulk(Category.INDEX_GROCERY, 3 + Util.rand(3));
            if (bonus > 0) {
                System.out.printf(">> 펜션 배달 매출: %,d원%n", bonus);
            } else {
                System.out.println(">> 재고 부족으로 배달 실패...");
            }
            return bonus;
        } else {
            // 축제 시즌: 폭죽, 맥주 대량 판매
            System.out.println();
            System.out.println("========================================");
            System.out.println("    *** 불꽃축제 시즌입니다! ***");
            System.out.println("========================================");
            System.out.println("\"축제 준비물 사러 왔어요!\"");
            System.out.println();
            int bonus = sellBulk(Category.INDEX_FIREWORK, 5 + Util.rand(10));
            bonus = bonus + sellBulk(Category.INDEX_BEER, 10 + Util.rand(10));
            if (bonus > 0) {
                System.out.printf(">> 축제 시즌 매출: %,d원%n", bonus);
            } else {
                System.out.println(">> 재고 부족으로 판매 실패...");
            }
            return bonus;
        }
    }

    /// <summary>
    /// 일일 정산 출력
    /// </summary>
    private void printDailySettlement(int dayNum, int customers, int success, int fail,
                                      int sales, int profit, boolean bigEvent,
                                      int bigEventBonus, int totalEarnings) {
        // 시간대 문자열
        String timeName = switch (game.timeOfDay) {
            case GameManager.TIME_MORNING -> "아침";
            case GameManager.TIME_NIGHT -> "밤";
            default -> "낮";
        };

        // 영업 후 예상 잔액 (GameManager가 실제 합산하기 전이므로 계산)
        int newBalance = game.money + totalEarnings;

        System.out.println();
        System.out.println("========================================");
        System.out.printf("       [ %d일차 %s 영업 정산 ]%n", dayNum, timeName);
        System.out.println("========================================");
        if (bigEvent) {
            System.out.printf("★ 빅 이벤트 매출: %,d원%n", bigEventBonus);
        }
        System.out.printf("오늘 방문 손님: %d명%n", customers);
        System.out.printf("판매 성공: %d건%n", success);
        System.out.printf("판매 실패: %d건%n", fail);
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.printf("  오늘 매출:    %,d원%n", sales + bigEventBonus);
        System.out.printf("  순이익:      +%,d원%n", profit);
        System.out.println("----------------------------------------");
        System.out.printf("  현재 총 자본: %,d원%n", newBalance);
        System.out.printf("  목표까지:     %,d원%n", game.goalMoney - newBalance);
        System.out.println("========================================");
    }
}
