import java.util.Scanner;

/// <summary>
/// 도매상 클래스
/// 도매상 구매 + 자동주문 정책 설정/실행을 담당
/// </summary>
public class Wholesaler {

    // ========== 필드 ==========

    private final GameManager game;           // 게임 상태(money) 접근용
    private final Market market;              // 가게 (창고, 매대, 총 재고 조회)
    private final ProductCatalog catalog;     // 상품/카테고리 조회
    private final Scanner scanner;

    // ========== 상수 ==========

    private static final int DEFAULT_BOX_COUNT = 3;  // 자동주문 기본 박스 수

    // ========== 생성자 ==========

    /// <summary>
    /// Wholesaler 생성자
    /// </summary>
    public Wholesaler(GameManager game, Market market, ProductCatalog catalog, Scanner scanner) {
        this.game = game;
        this.market = market;
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
            System.out.printf("매대: %d / %d칸%n", market.display.getUsedSlots(), Market.MAX_SLOT);
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
            System.out.printf("현재 자본: %,d원 | 매대: %d / %d칸%n", game.money, market.display.getUsedSlots(), Market.MAX_SLOT);
            System.out.println();
            catalog.printCategoryMenu();

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
    /// 선택한 카테고리의 상품 목록(매입가/판매가/재고)을 출력하고,
    /// 사용자가 상품 번호와 수량을 입력하면 purchaseProduct()로 구매 처리
    /// 0 입력 시 카테고리 선택 화면으로 복귀
    /// </summary>
    private void buyCategoryProducts(int category) {
        boolean buying = true;
        // 사용자 입력은 1번부터, 배열은 0번부터이므로 -1
        Category cat = catalog.allCategories[category - 1];

        while (buying) {
            Util.clearScreen();

            // 카테고리 헤더 (카테고리명 + 박스 단위 표시)
            System.out.println("========================================");
            System.out.printf("        [ %s ] (%s)%n", cat.name, cat.getPackageInfo());
            System.out.println("========================================");

            // 카테고리 내 상품 목록 출력 (번호, 이름, 매입가, 판매가, 총 재고)
            for (int i = 0; i < cat.products.length; i++) {
                Product p = cat.products[i];
                System.out.printf("%d. %-8s | 매입 %,6d원 | 판매 %,6d원 | 재고: %d개%n",
                    i + 1, p.name, p.buyPrice, p.sellPrice, market.getTotalStock(p));
            }

            System.out.println();
            System.out.println("구매할 상품 번호 (0: 돌아가기)");
            System.out.print(">> ");

            int productChoice = Util.readInt(scanner);

            if (productChoice == 0) {
                // 카테고리 선택 화면으로 복귀
                buying = false;
            } else {
                // 상품 번호와 수량을 받아 구매 처리
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
        market.warehouse.addStock(product, quantity);

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
    /// 카테고리를 선택한 뒤, 해당 카테고리 전체 상품에 대해
    /// 자동주문 등록(임계값 설정) 또는 해제를 수행
    /// 등록 시 카테고리의 autoOrderEnabled/autoOrderThreshold가 갱신됨
    /// </summary>
    private void setCategoryPolicy() {
        // ========== 1단계: 카테고리 선택 ==========

        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("      [ 카테고리 단위 설정 ]");
        System.out.println("========================================");
        System.out.println();
        catalog.printCategoryMenu();

        int categoryChoice = Util.readInt(scanner);

        if (categoryChoice == 0) {
            return;
        }

        // 범위 밖 입력 방어
        if (categoryChoice < 1 || categoryChoice > 10) {
            System.out.println("잘못된 입력입니다.");
            return;
        }

        // 사용자 입력은 1번부터, 배열은 0번부터이므로 -1
        String categoryName = catalog.allCategories[categoryChoice - 1].name;

        // ========== 2단계: 등록/해제 선택 ==========

        System.out.println();
        System.out.println("[ " + categoryName + " 카테고리 설정 ]");
        System.out.println("[1] 자동주문 등록 (임계값 입력)");
        System.out.println("[2] 자동주문 해제");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        int actionChoice = Util.readInt(scanner);

        if (actionChoice == 1) {
            // 자동주문 등록: 임계값과 박스 수를 입력받아 카테고리에 설정
            // -> executeAutoOrder() 실행 시 이 카테고리의 모든 상품이 임계값 기준으로 자동 주문됨
            System.out.print("임계값 입력 (재고 몇 개 이하면 주문?) >> ");
            int threshold = Util.readInt(scanner);

            System.out.print("주문 박스 수 입력 (기본 3) >> ");
            int boxCount = Util.readInt(scanner);
            if (boxCount <= 0) {
                boxCount = DEFAULT_BOX_COUNT;
            }

            Category cat = catalog.allCategories[categoryChoice - 1];
            cat.autoOrderEnabled = true;
            cat.autoOrderThreshold = threshold;
            cat.autoOrderBoxCount = boxCount;

            System.out.println("[OK] " + categoryName + " 카테고리 등록 완료 (임계값: " + threshold + "개, " + boxCount + "박스)");

        } else if (actionChoice == 2) {
            // 자동주문 해제: 카테고리의 자동주문 플래그를 끔
            Category cat = catalog.allCategories[categoryChoice - 1];
            cat.autoOrderEnabled = false;

            System.out.println("[OK] " + categoryName + " 카테고리 자동주문 해제됨");
        }

        // actionChoice == 0 또는 그 외: 아무것도 안 하고 메서드 종료
    }

    /// <summary>
    /// 개별 상품 정책 설정
    /// </summary>
    private void setIndividualPolicy() {
        // ========== 1단계: 카테고리 선택 ==========
        // 개별 상품을 설정하려면 먼저 어떤 카테고리인지 좁혀야 함

        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("       [ 개별 상품 설정 ]");
        System.out.println("========================================");
        System.out.println("먼저 카테고리를 선택하세요.");
        System.out.println();
        catalog.printCategoryMenu();

        int categoryChoice = Util.readInt(scanner);

        if (categoryChoice == 0) {
            return;
        }

        // 범위 밖 입력 방어
        if (categoryChoice < 1 || categoryChoice > 10) {
            System.out.println("잘못된 입력입니다.");
            return;
        }

        // ========== 2단계: 상품 선택 ==========
        // 카테고리 내 상품 목록을 보여주고, 번호로 선택

        System.out.println();
        printCategoryProductsForPolicy(categoryChoice);

        System.out.print("상품 번호 선택 >> ");
        int productNum = Util.readInt(scanner);

        // 사용자 입력은 1번부터, 배열은 0번부터이므로 -1
        Product product = catalog.allCategories[categoryChoice - 1].getProductByNum(productNum);

        if (product == null) {
            System.out.println("[!!] 잘못된 상품 번호입니다.");
            return;
        }

        // ========== 3단계: 등록/해제 선택 ==========

        System.out.println();
        System.out.println("[ " + product.name + " 설정 ]");
        System.out.println("[1] 자동주문 등록 (임계값 입력)");
        System.out.println("[2] 자동주문 해제");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");

        int actionChoice = Util.readInt(scanner);

        if (actionChoice == 1) {
            // 자동주문 등록: 임계값과 박스 수를 입력받아 개별 상품에 설정
            // -> executeAutoOrder() 실행 시 이 상품이 임계값 기준으로 자동 주문됨
            //    (단, 카테고리 정책이 등록된 경우 카테고리 정책이 우선)
            System.out.print("임계값 입력 (재고 몇 개 이하면 주문?) >> ");
            int threshold = Util.readInt(scanner);

            System.out.print("주문 박스 수 입력 (기본 3) >> ");
            int boxCount = Util.readInt(scanner);
            if (boxCount <= 0) {
                boxCount = DEFAULT_BOX_COUNT;
            }

            product.autoOrderEnabled = true;
            product.autoOrderThreshold = threshold;
            product.autoOrderBoxCount = boxCount;

            System.out.println("[OK] " + product.name + " 등록 완료 (임계값: " + threshold + "개, " + boxCount + "박스)");

        } else if (actionChoice == 2) {
            // 자동주문 해제: 상품의 자동주문 플래그를 끔
            product.autoOrderEnabled = false;
            System.out.println("[OK] " + product.name + " 자동주문 해제됨");
        }
        // actionChoice == 0 또는 그 외: 아무것도 안 하고 메서드 종료
    }

    /// <summary>
    /// 정책 설정용 카테고리 상품 출력
    /// 카테고리 내 상품을 번호와 함께 나열하여 선택할 수 있게 함
    /// </summary>
    private void printCategoryProductsForPolicy(int category) {
        // 사용자 입력은 1번부터, 배열은 0번부터이므로 -1
        Category cat = catalog.allCategories[category - 1];
        System.out.println("[ " + cat.name + " ]");

        // 상품 번호(1부터), 이름, 총 재고(창고+매대) 출력
        for (int i = 0; i < cat.products.length; i++) {
            Product p = cat.products[i];
            System.out.printf("%d. %s (재고: %d)%n", i + 1, p.name, market.getTotalStock(p));
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
                System.out.println(" - " + cat.name + ": 임계값 " + cat.autoOrderThreshold + "개, " + cat.autoOrderBoxCount + "박스");
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
                System.out.println(" - " + p.name + ": 임계값 " + p.autoOrderThreshold + "개, " + p.autoOrderBoxCount + "박스");
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

        // 이번 자동주문에서 사용한 총 금액 (정산 출력용)
        int totalCost = 0;

        // ========== 1순위: 카테고리 정책 기반 주문 ==========
        // 카테고리에 자동주문이 등록되어 있으면, 해당 카테고리의 모든 상품을
        // 카테고리 임계값 기준으로 주문
        for (Category cat : catalog.allCategories) {
            if (cat.autoOrderEnabled) {
                for (Product p : cat.products) {
                    totalCost = totalCost + autoOrderProduct(p, cat.autoOrderThreshold, cat.autoOrderBoxCount);
                }
            }
        }

        // ========== 2순위: 개별 상품 정책 기반 주문 ==========
        // 카테고리 정책이 없는 카테고리에서만, 개별 상품 정책이 등록된 상품을
        // 상품별 임계값 기준으로 주문
        // (카테고리 정책이 있으면 이미 1순위에서 처리됐으므로 건너뜀)
        for (Category cat : catalog.allCategories) {
            if (!cat.autoOrderEnabled) {
                for (Product p : cat.products) {
                    if (p.autoOrderEnabled) {
                        totalCost = totalCost + autoOrderProduct(p, p.autoOrderThreshold, p.autoOrderBoxCount);
                    }
                }
            }
        }

        // 정산 결과 출력
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
    /// 재고가 임계값 이하면 지정된 박스 수만큼 주문, 주문 금액 반환
    /// </summary>
    private int autoOrderProduct(Product product, int threshold, int boxCount) {
        // 재고 확인: 총 재고(창고+매대)가 임계값 이하인지 체크
        int totalStock = market.getTotalStock(product);
        if (totalStock > threshold) {
            // 재고 충분 -> 주문 불필요
            return 0;
        }

        // 주문 수량 계산: 박스당 수량 × 박스 수
        int orderAmount = product.quantityPerBox * boxCount;
        int cost = product.buyPrice * orderAmount;

        // 자본 부족 시 주문 불가
        if (cost > game.money) {
            System.out.printf(" - %s: 자본 부족 (필요: %,d원)%n", product.name, cost);
            return 0;
        }

        // 구매 처리: 자본 차감 + 창고 입고
        game.money = game.money - cost;
        market.warehouse.addStock(product, orderAmount);

        System.out.printf(" - %s %d박스(%d개) 창고 입고 (-%,d원)%n", product.name, boxCount, orderAmount, cost);

        return cost;
    }
}
