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

    private final int maxDisplayPerSlot;  // 슬롯당 최대 진열 수량

    // ========== 재사용 배열 (메서드 내 반복 할당 방지) ==========

    // autoArrangeDisplay()용 - 라운드 로빈 진열 시 각 카테고리의 현재 탐색 위치
    private int[] categoryIndex = new int[10];

    // getAvailableFromCategory()용 - 재고 있는 상품 필터링
    private final Product[] availableProducts = new Product[7];  // 카테고리 내 재고 있는 상품 임시 저장 (최대 7개 - 음료)

    // ========== 생성자 ==========

    /// <summary>
    /// Inventory 생성자
    /// 창고와 매대를 초기화하고, 매대 설정값과 스캐너를 저장
    /// </summary>
    public Inventory(int maxSlot, int maxDisplayPerSlot) {
        this.warehouse = new Warehouse();
        this.display = new Display(maxSlot, maxDisplayPerSlot);
        this.maxSlot = maxSlot;
        this.maxDisplayPerSlot = maxDisplayPerSlot;
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
        // 0 입력 전까지 반복 (여러 작업을 연속으로 할 수 있도록)
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
                    // 창고에서 상품을 골라 매대에 올림
                    displayProduct(scanner, catalog);
                    break;
                case 2:
                    // 매대에서 상품을 골라 창고로 되돌림
                    returnProduct(scanner, catalog);
                    break;
                case 3:
                    // 창고에 남아있는 재고 목록 확인
                    showWarehouse(scanner, catalog);
                    break;
                case 4:
                    // 기존 상품 보충 + 빈 슬롯에 카테고리 균형 배정
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
                        num, p.name, stock, displayed, maxDisplayPerSlot);
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
            System.out.println("[!!] 매대 공간이 부족합니다. (" + display.getUsedSlots() + "/" + maxSlot + "칸)");
            return;
        }

        // 슬롯당 최대 수량에서 현재 진열량을 뺀 만큼만 추가 가능
        int maxCanDisplay = maxDisplayPerSlot - currentDisplay;
        if (maxCanDisplay <= 0) {
            System.out.printf("[!!] 매대가 가득 찼습니다. (최대 %d개)%n", maxDisplayPerSlot);
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
            System.out.printf("[!!] 매대 공간 제한으로 %d개만 진열합니다. (최대 %d개)%n", maxCanDisplay, maxDisplayPerSlot);
            amount = maxCanDisplay;
        }

        // ========== 진열 처리 ==========
        // Display 클래스가 창고 차감 + 매대 추가 + 슬롯 관리를 자동으로 처리
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
                            int displayed = display.displayFromWarehouse(product, warehouse, maxDisplayPerSlot);

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
        System.out.printf("매대: %d / %d칸%n", display.getUsedSlots(), maxSlot);
        System.out.println("========================================");
        System.out.println();
        System.out.println("아무 키나 입력하면 돌아갑니다...");
        scanner.next();
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
    private void showWarehouse(Scanner scanner, ProductCatalog catalog) {
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
}
