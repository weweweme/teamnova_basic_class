import java.util.Scanner;

/// <summary>
/// 계산대 클래스
/// 손님이 매대에서 직접 고른 상품의 결제 처리를 담당
/// 직접 영업, 빠른 영업, 빅 이벤트 처리
/// </summary>
public class Cashier {

    // ========== 의존 객체 ==========

    private final GameManager game;           // game.money, game.day, game.timeOfDay 접근용
    private final Market market;              // market.display (판매), market.sellBulk (빅이벤트)
    private final Scanner scanner;

    // ========== 생성자 ==========

    /// <summary>
    /// Cashier 생성자
    /// GameManager, Market, Scanner를 전달받음
    /// </summary>
    public Cashier(GameManager game, Market market, Scanner scanner) {
        this.game = game;
        this.market = market;
        this.scanner = scanner;
    }

    // ========== 영업 메서드 ==========

    /// <summary>
    /// 영업 시작 (손님 응대)
    /// </summary>
    public void startBusiness() {
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

            // 손님 생성 + 매대에서 직접 상품 선택
            Customer customer = createCustomer(customerType);

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

                int currentStock = market.display.getDisplayed(product);
                if (currentStock >= wantAmount) {
                    // 전부 판매 가능
                    int saleAmount = product.sellPrice * wantAmount;
                    int profitAmount = (product.sellPrice - product.buyPrice) * wantAmount;

                    market.display.sell(product, wantAmount);

                    game.money = game.money + saleAmount;
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

                    market.display.sell(product, currentStock);

                    game.money = game.money + saleAmount;
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
                    // 남은 손님 자동 처리 (Customer 객체로 간소화)
                    System.out.println();
                    System.out.println("남은 손님을 빠르게 처리합니다...");
                    Util.delay(500);

                    for (int skipNum = customerNum + 1; skipNum <= todayCustomers; skipNum++) {
                        // 손님 생성 + 매대에서 직접 상품 선택
                        Customer skipCustomer = createCustomer(Util.rand(4));

                        // 손님이 고른 상품 판매 처리
                        for (int i = 0; i < skipCustomer.wantCount; i++) {
                            Product skipProduct = skipCustomer.wantProducts[i];
                            int skipWantAmount = skipCustomer.wantAmounts[i];

                            if (skipWantAmount <= 0 || skipProduct == null) {
                                continue;
                            }

                            int[] result = sellProduct(skipProduct, skipWantAmount);
                            int saleAmount = result[0];
                            int profitAmount = result[1];
                            int soldAmount = result[2];

                            if (soldAmount > 0) {
                                game.money = game.money + saleAmount;
                                todaySales = todaySales + saleAmount;
                                todayProfit = todayProfit + profitAmount;
                                successCount++;

                                if (soldAmount < skipWantAmount) {
                                    failCount++;
                                }
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
        printDailySettlement(game.day, todayCustomers, successCount, failCount,
                todaySales, todayProfit, bigEventOccurred);

        System.out.println();
        System.out.println("아무 키나 입력하면 계속...");
        scanner.next();
    }

    /// <summary>
    /// 빠른 영업 (하루 요약)
    /// 손님 상세 없이 결과만 출력
    /// </summary>
    public void startQuickBusiness() {
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("         [ 빠른 영업 - " + game.day + "일차 ]");
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

        // 손님별 간략 처리 (Customer 객체 사용)
        for (int customerNum = 0; customerNum < todayCustomers; customerNum++) {
            // 손님 생성 + 매대에서 직접 상품 선택
            Customer customer = createCustomer(Util.rand(4));

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
                    game.money = game.money + saleAmount;
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
        printDailySettlement(game.day, todayCustomers, successCount, failCount,
                             todaySales, todayProfit, eventOccurred);

        System.out.println();
        System.out.println("아무 키나 입력하면 계속...");
        scanner.next();
    }

    /// <summary>
    /// 손님 객체 생성 + 매대에서 상품 선택까지 처리
    /// Customer가 유형별 쇼핑 패턴(카테고리+수량)을 자동 설정하고,
    /// pickProducts()로 매대에서 직접 상품을 고름
    /// </summary>
    private Customer createCustomer(int type) {
        Customer c = new Customer(type);

        // 손님이 매대에서 직접 상품을 고름
        c.pickProducts(market.display, market.catalog.allCategories);

        // 멘트 조합: [손님 인사] + [시간대 멘트]
        String greeting = Customer.TYPE_GREETINGS[type][Util.rand(5)];
        // 현재 시간대에 맞는 멘트 선택
        String timeMsg = switch (game.timeOfDay) {
            case GameManager.TIME_MORNING -> Customer.MORNING_GREETINGS[Util.rand(5)];
            case GameManager.TIME_NIGHT -> Customer.NIGHT_GREETINGS[Util.rand(5)];
            default -> Customer.AFTERNOON_GREETINGS[Util.rand(5)];
        };
        c.greeting = greeting + " " + timeMsg;

        return c;
    }

    /// <summary>
    /// 상품 판매 처리
    /// 매대 재고에서 판매하고 결과 반환
    /// </summary>
    /// <returns>int[3] = {판매액, 이익, 실제판매수량} (재고 없으면 모두 0)</returns>
    private int[] sellProduct(Product product, int wantAmount) {
        int[] result = new int[3];  // [판매액, 이익, 실제판매수량]

        int stock = market.display.getDisplayed(product);

        if (stock >= wantAmount) {
            // 전부 판매 가능
            result[0] = product.sellPrice * wantAmount;
            result[1] = (product.sellPrice - product.buyPrice) * wantAmount;
            result[2] = wantAmount;
            market.display.sell(product, wantAmount);
        } else if (stock > 0) {
            // 일부만 판매
            result[0] = product.sellPrice * stock;
            result[1] = (product.sellPrice - product.buyPrice) * stock;
            result[2] = stock;
            market.display.sell(product, stock);
        }
        // else: 재고 없음 - result는 이미 0으로 초기화됨

        return result;
    }

    /// <summary>
    /// 빅 이벤트 체크 및 처리
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
            int bonus = market.sellBulk(Category.INDEX_DRINK, 10 + Util.rand(10));
            bonus = bonus + market.sellBulk(Category.INDEX_SNACK, 5 + Util.rand(5));
            game.money = game.money + bonus;
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
            int bonus = market.sellBulk(Category.INDEX_MEAT, 5 + Util.rand(5));
            bonus = bonus + market.sellBulk(Category.INDEX_DRINK, 5 + Util.rand(5));
            bonus = bonus + market.sellBulk(Category.INDEX_GROCERY, 3 + Util.rand(3));
            game.money = game.money + bonus;
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
            int bonus = market.sellBulk(Category.INDEX_FIREWORK, 5 + Util.rand(10));
            bonus = bonus + market.sellBulk(Category.INDEX_BEER, 10 + Util.rand(10));
            game.money = game.money + bonus;
            if (bonus > 0) {
                System.out.printf(">> 축제 시즌 매출: %,d원%n", bonus);
            } else {
                System.out.println(">> 재고 부족으로 판매 실패...");
            }
            return bonus > 0;
        }
    }

    /// <summary>
    /// 일일 정산 출력
    /// </summary>
    private void printDailySettlement(int dayNum, int customers, int success, int fail,
                                      int sales, int profit, boolean bigEvent) {
        // 시간대 문자열
        String timeName;
        switch (game.timeOfDay) {
            case GameManager.TIME_MORNING:
                timeName = "아침";
                break;
            case GameManager.TIME_NIGHT:
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
        System.out.printf("  현재 총 자본: %,d원%n", game.money);
        System.out.printf("  목표까지:     %,d원%n", game.goalMoney - game.money);
        System.out.println("========================================");
    }
}
