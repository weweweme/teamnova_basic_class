import java.util.Scanner;

/// <summary>
/// 인벤토리(재고) 클래스
/// 창고+매대 재고를 통합 관리 (진열/회수/자동배정/재고조회)
/// </summary>
public class Inventory {

    // ========== 필드 ==========

    public Warehouse warehouse;    // 창고 (Wholesaler에서 inventory.warehouse로 접근)
    public Display display;        // 매대 (Market, Wholesaler, Cashier에서 접근)
    public int maxSlot;            // 매대 최대 슬롯 수 (Wholesaler에서 접근)
    private int maxDisplayPerSlot;  // 슬롯당 최대 진열 수량
    private Scanner scanner;        // 사용자 입력용

    // ========== 재사용 배열 (메서드 내 반복 할당 방지) ==========

    // autoArrangeDisplay()용 - 카테고리별 진열 대기 상품 버퍼
    private Product[][] arrangeCategoriesBuffer = new Product[10][7];  // [카테고리][상품] - 창고->매대 진열 대기 버퍼
    private int[] arrangeCategoryCounts = new int[10];                 // 각 카테고리별 버퍼 내 상품 수
    private int[] arrangeCategoryIndex = new int[10];                  // 라운드 로빈 진열 시 현재 인덱스

    // getAvailableFromCategory()용 - 재고 있는 상품 필터링
    private Product[] availableProducts = new Product[7];  // 카테고리 내 재고 있는 상품 임시 저장 (최대 7개 - 음료)

    // ========== 생성자 ==========

    /// <summary>
    /// Inventory 생성자
    /// 창고와 매대를 초기화하고, 매대 설정값과 스캐너를 저장
    /// </summary>
    public Inventory(int maxSlot, int maxDisplayPerSlot, Scanner scanner) {
        this.warehouse = new Warehouse();
        this.display = new Display(maxSlot, maxDisplayPerSlot);
        this.maxSlot = maxSlot;
        this.maxDisplayPerSlot = maxDisplayPerSlot;
        this.scanner = scanner;
    }

    // ========== 재고 조회 ==========

    /// <summary>
    /// 총 재고 조회 (창고 + 매대)
    /// </summary>
    public int getTotalStock(Product p) {
        return warehouse.getStock(p) + display.getDisplayed(p);
    }

    /// <summary>
    /// 카테고리에서 재고 있는 상품 우선 선택
    /// 재고 있는 상품이 없으면 랜덤 선택 (재고 없음 처리)
    /// </summary>
    public Product getAvailableFromCategory(Category category) {
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

    // ========== 매대 관리 메뉴 ==========

    /// <summary>
    /// 매대 관리
    /// </summary>
    public void manageDisplay(Scanner scanner, ProductCatalog catalog) {
        boolean managing = true;

        while (managing) {
            Util.clearScreen();
            System.out.println("========================================");
            System.out.println("            [ 매대 관리 ]");
            System.out.println("========================================");
            System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), maxSlot);
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
                    displayProduct(scanner, catalog);
                    break;
                case 2:
                    returnProduct(scanner, catalog);
                    break;
                case 3:
                    showWarehouse(scanner, catalog);
                    break;
                case 4:
                    autoArrangeDisplay(scanner, catalog);
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
    private void displayProduct(Scanner scanner, ProductCatalog catalog) {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("        [ 상품 진열 ] 창고 -> 매대");
        System.out.println("========================================");
        System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), maxSlot);
        System.out.println();

        // 매대가 꽉 찼으면 경고
        if (!display.hasEmptySlot()) {
            System.out.println("[!!] 매대가 꽉 찼습니다! (이미 진열된 상품만 추가 가능)");
            System.out.println();
        }

        // 창고에 재고가 있는 상품 목록 출력
        // 이미 진열 중인 상품은 [진열중] 표시
        // 번호 -> 상품 매핑용 배열 (번호로 선택하기 위해)
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
                    // 이미 매대에 진열 중인 상품
                    System.out.printf("%d. %s (창고: %d개) [진열중 %d/%d]%n",
                        num, p.name, stock, displayed, maxDisplayPerSlot);
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

        // 번호로 상품 찾기 (1번 -> stockList[0])
        Product product = stockList[productNum - 1];
        int warehouseStock = warehouse.getStock(product);

        // 새 상품이면 슬롯 체크
        int currentDisplay = display.getDisplayed(product);
        boolean isNewOnDisplay = (currentDisplay == 0);
        if (isNewOnDisplay && !display.hasEmptySlot()) {
            System.out.println("[!!] 매대 공간이 부족합니다. (" + display.getUsedSlots() + "/" + maxSlot + "칸)");
            return;
        }

        // 매대에 진열 가능한 최대 수량 계산
        int maxCanDisplay = maxDisplayPerSlot - currentDisplay;
        if (maxCanDisplay <= 0) {
            System.out.printf("[!!] 매대가 가득 찼습니다. (최대 %d개)%n", maxDisplayPerSlot);
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
            System.out.printf("[!!] 매대 공간 제한으로 %d개만 진열합니다. (최대 %d개)%n", maxCanDisplay, maxDisplayPerSlot);
            amount = maxCanDisplay;
        }

        // 진열 처리 (Display 클래스가 슬롯과 재고를 자동 관리)
        int displayed = display.displayFromWarehouse(product, warehouse, amount);

        System.out.printf("[OK] %s %d개 매대에 진열! (매대: %d/%d개)%n", product.name, displayed, display.getDisplayed(product), maxDisplayPerSlot);
    }

    /// <summary>
    /// 상품 회수 (매대 -> 창고)
    /// </summary>
    private void returnProduct(Scanner scanner, ProductCatalog catalog) {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("        [ 상품 회수 ] 매대 -> 창고");
        System.out.println("========================================");
        System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), maxSlot);
        System.out.println();

        // 매대에 진열된 상품 목록 출력
        System.out.println("--- 매대 재고 ---");
        int num = 1;
        for (Product p : catalog.allProducts) {
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
        Product product = catalog.getProductByName(productName);

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
    private void autoArrangeDisplay(Scanner scanner, ProductCatalog catalog) {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("      [ 자동 배정 - 카테고리 균형 ]");
        System.out.println("========================================");
        System.out.printf("현재 매대: %d / %d칸%n", display.getUsedSlots(), maxSlot);
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
            boolean hasRoomOnDisplay = displayStock < maxDisplayPerSlot;     // 매대에 더 놓을 공간이 있는가?
            boolean hasWarehouseStock = warehouseStock > 0;                  // 창고에 재고가 있는가?

            if (isOnDisplay && hasRoomOnDisplay && hasWarehouseStock) {
                int displayed = display.displayFromWarehouse(p, warehouse, maxDisplayPerSlot);
                System.out.printf(" - %s +%d개 보충 (매대: %d/%d)%n", p.name, displayed, display.getDisplayed(p), maxDisplayPerSlot);
                refilledCount++;
            }
        }

        if (refilledCount == 0) {
            System.out.println(" (보충할 상품 없음)");
        }

        System.out.println();

        // ========== 2단계: 빈 슬롯에 새 상품 배정 ==========
        System.out.println("[ 2단계: 새 상품 배정 ]");

        int remainingSlots = maxSlot - display.getUsedSlots();
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
            //   arrangeCategoriesBuffer[0] = [환타, 밀키스]  <- 음료 중 매대에 없는 것
            //   arrangeCategoriesBuffer[1] = [클라우드]      <- 맥주 중 매대에 없는 것
            //   arrangeCategoriesBuffer[2] = []              <- 소주는 전부 매대에 있음
            //   ...
            //   arrangeCategoryCounts[0] = 2  <- 음료 대기 상품 2개
            //   arrangeCategoryCounts[1] = 1  <- 맥주 대기 상품 1개
            //   arrangeCategoryCounts[2] = 0  <- 소주 대기 상품 0개
            int totalAvailable = 0;
            for (int i = 0; i < 10; i++) {
                arrangeCategoryCounts[i] = 0;   // 버퍼 초기화
                arrangeCategoryIndex[i] = 0;    // 진열 순서 인덱스 초기화
                fillArrangeBuffer(i, catalog.allCategories[i]);
                totalAvailable = totalAvailable + arrangeCategoryCounts[i];
            }

            if (totalAvailable == 0) {
                System.out.println(" (창고에 새로 진열할 상품 없음)");
            } else {
                // 한 카테고리에 몰아서 진열하지 않고, 돌아가며 1개씩 진열한다
                // -> 매대 30칸을 특정 카테고리가 독점하는 것을 방지
                //
                // 동작 순서 (매대 빈 슬롯이 5칸인 경우):
                //   1바퀴: 음료(환타) -> 맥주(클라우드) -> 간식(육포) -> 라면(불닭) -> 아이스크림(보석바) -> 5칸 다 참 -> 종료
                //
                // arrangeCategoryIndex[cat]가 각 카테고리에서 "다음에 진열할 상품" 위치를 추적
                // 모든 카테고리의 인덱스가 끝까지 도달하면 hasMore = false -> while 종료
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
                            int displayed = display.displayFromWarehouse(product, warehouse, maxDisplayPerSlot);

                            if (displayed > 0) {
                                newDisplayCount++;

                                int remainInWarehouse = warehouse.getStock(product);
                                if (remainInWarehouse > 0) {
                                    System.out.printf(" - [%s] %s %d개 진열 (창고: %d개)%n", catalog.allCategories[cat].name, product.name, displayed, remainInWarehouse);
                                } else {
                                    System.out.printf(" - [%s] %s %d개 진열%n", catalog.allCategories[cat].name, product.name, displayed);
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
        System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), maxSlot);
        System.out.println("========================================");
        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
    }

    /// <summary>
    /// 자동배정 헬퍼 - 카테고리별 진열 대기 버퍼 채우기
    /// 조건: 창고에 재고가 있고(O) + 매대에 아직 없는(X) 상품
    ///
    /// 예시: 음료 카테고리(categoryIndex=0) 검사
    ///       - 콜라: 창고 20개, 매대 0개 -> 버퍼에 추가 (새로 진열 가능)
    ///       - 사이다: 창고 0개, 매대 5개 -> 스킵 (창고에 없음)
    ///       - 물: 창고 10개, 매대 3개 -> 스킵 (이미 매대에 있음, 1단계에서 보충됨)
    /// </summary>
    private void fillArrangeBuffer(int categoryIndex, Category category) {
        for (Product p : category.products) {
            // 창고에 재고가 있고 매대에 진열되지 않은 상품
            if (warehouse.getStock(p) > 0 && display.getDisplayed(p) == 0) {
                arrangeCategoriesBuffer[categoryIndex][arrangeCategoryCounts[categoryIndex]++] = p;
            }
        }
    }

    // ========== 재고 출력 ==========

    /// <summary>
    /// 재고 확인
    /// </summary>
    public void showInventory(Scanner scanner, ProductCatalog catalog) {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.printf("       [ 매대 현황 ] %d / %d칸%n", display.getUsedSlots(), maxSlot);
        System.out.println("========================================");
        System.out.println();

        boolean hasStock = false;
        for (Product p : catalog.allProducts) {
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
    /// 창고 재고 확인
    /// </summary>
    private void showWarehouse(Scanner scanner, ProductCatalog catalog) {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("           [ 창고 재고 ]");
        System.out.println("========================================");
        System.out.println();

        boolean hasStock = false;

        // 창고에 재고가 있는 상품만 출력
        for (Product p : catalog.allProducts) {
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
    /// 재고바 출력
    /// 상품명과 수량을 정렬하여 한 줄로 출력
    /// </summary>
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
}
