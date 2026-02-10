import java.util.Scanner;

/// <summary>
/// 도매상 클래스
/// 도매상 구매 + 자동주문 정책 설정/실행을 담당
/// </summary>
public class Wholesaler {

    // ========== 필드 ==========

    private GameManager game;           // 게임 상태(money) 접근용
    private Inventory inventory;        // 재고 관리 (창고, 매대, 총 재고 조회)
    private ProductCatalog catalog;     // 상품/카테고리 조회
    private Scanner scanner;

    // ========== 상수 ==========

    private static final int AUTO_ORDER_BOX_COUNT = 3;  // 자동주문 시 박스 수 (약 1주일치)

    // ========== 생성자 ==========

    /// <summary>
    /// Wholesaler 생성자
    /// </summary>
    public Wholesaler(GameManager game, Inventory inventory, ProductCatalog catalog, Scanner scanner) {
        this.game = game;
        this.inventory = inventory;
        this.catalog = catalog;
        this.scanner = scanner;
    }

    // ========== 도매상 메서드 ==========

    /// <summary>
    /// 도매상 (메인 메뉴)
    /// </summary>
    public void goWholesaler() {
        boolean shopping = true;

        while (shopping) {
            Util.clearScreen();
            System.out.println("========================================");
            System.out.println("            [ 도매상 ]");
            System.out.println("========================================");
            System.out.printf("현재 자본: %,d원%n", game.money);
            System.out.printf("매대: %d / %d칸%n", inventory.display.getUsedSlots(), inventory.maxSlot);
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
            System.out.printf("현재 자본: %,d원 | 매대: %d / %d칸%n", game.money, inventory.display.getUsedSlots(), inventory.maxSlot);
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
        Category cat = catalog.allCategories[category - 1];

        while (buying) {
            Util.clearScreen();

            System.out.println("========================================");
            System.out.printf("        [ %s ] (%s)%n", cat.name, cat.boxUnit);
            System.out.println("========================================");

            for (int i = 0; i < cat.products.length; i++) {
                Product p = cat.products[i];
                System.out.printf("%d. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n",
                    i + 1, p.name, p.buyPrice, p.sellPrice, inventory.getTotalStock(p));
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
        Product product = catalog.allCategories[category - 1].getProductByNum(productNum);

        if (product == null) {
            System.out.println("[!!] 잘못된 상품 번호입니다.");
            return;
        }

        int totalCost = product.buyPrice * quantity;

        // 자본 체크
        if (totalCost > game.money) {
            System.out.println("[!!] 자본이 부족합니다. (필요: " + String.format("%,d", totalCost) + "원)");
            return;
        }

        // 구매 처리 (창고로 입고)
        game.money = game.money - totalCost;
        inventory.warehouse.addStock(product, quantity);

        System.out.println("[OK] " + product.name + " " + quantity + "개 창고로 입고! (-" + String.format("%,d", totalCost) + "원)");
    }

    // ========== 정책 메서드 ==========

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
        String categoryName = catalog.allCategories[categoryChoice - 1].name;

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
            Category cat = catalog.allCategories[categoryChoice - 1];
            cat.autoOrderEnabled = true;
            cat.autoOrderThreshold = threshold;

            System.out.println("[OK] " + categoryName + " 카테고리 등록 완료 (임계값: " + threshold + "개)");

        } else if (actionChoice == 2) {
            // 자동주문 해제
            Category cat = catalog.allCategories[categoryChoice - 1];
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

        Product product = catalog.allCategories[categoryChoice - 1].getProductByNum(productNum);

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
        Category cat = catalog.allCategories[category - 1];
        System.out.println("[ " + cat.name + " ]");
        for (int i = 0; i < cat.products.length; i++) {
            Product p = cat.products[i];
            System.out.printf("%d. %s (재고: %d)%n", i + 1, p.name, inventory.getTotalStock(p));
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

        for (Category cat : catalog.allCategories) {
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
        for (Product p : catalog.allProducts) {
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

    // ========== 자동주문 메서드 ==========

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
        for (Category cat : catalog.allCategories) {
            if (cat.autoOrderEnabled) {
                for (Product p : cat.products) {
                    totalCost = totalCost + autoOrderProduct(p, cat.autoOrderThreshold);
                }
            }
        }

        // 개별 상품 정책 기반 주문 (카테고리 미등록 시)
        for (Category cat : catalog.allCategories) {
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
        System.out.printf("남은 자본: %,d원%n", game.money);
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
        int totalStock = inventory.getTotalStock(product);
        if (totalStock > threshold) {
            return 0;
        }

        int boxSize = product.boxSize;
        int orderAmount = boxSize * AUTO_ORDER_BOX_COUNT;  // 3박스
        int cost = product.buyPrice * orderAmount;

        // 자본 체크
        if (cost > game.money) {
            System.out.printf(" - %s: 자본 부족 (필요: %,d원)%n", product.name, cost);
            return 0;
        }

        // 주문 처리 (창고로 입고)
        game.money = game.money - cost;
        inventory.warehouse.addStock(product, orderAmount);

        System.out.printf(" - %s %d박스(%d개) 창고 입고 (-%,d원)%n", product.name, AUTO_ORDER_BOX_COUNT, orderAmount, cost);

        return cost;
    }
}
