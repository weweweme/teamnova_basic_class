/// <summary>
/// 계산대 클래스
/// 손님이 매대에서 직접 고른 상품의 결제만 처리
/// 상품 합산 → 매대 재고 차감 → 결과 반환
/// </summary>
public class Cashier {

    // ========== 필드 ==========

    private final Display display;  // 매대 (재고 확인 + 판매 처리)

    // ========== 생성자 ==========

    /// <summary>
    /// Cashier 생성자
    /// 매대(Display)만 전달받음
    /// </summary>
    public Cashier(Display display) {
        this.display = display;
    }

    // ========== 결제 ==========

    /// <summary>
    /// 손님 결제 처리
    /// 손님이 가져온 상품의 같은 상품 합산 → 매대 재고에서 판매 → 결과 반환
    /// verbose=true: 판매 결과를 한 줄씩 출력 (직접 영업용)
    /// verbose=false: 출력 없이 처리 (빠른 영업/스킵용)
    /// </summary>
    /// <returns>int[4] = {매출, 이익, 성공건수, 실패건수}</returns>
    public int[] checkout(Customer customer, boolean verbose) {
        int totalSales = 0;
        int totalProfit = 0;
        int successCount = 0;
        int failCount = 0;

        // ========== 같은 상품 합산 ==========
        // 손님이 같은 카테고리에서 같은 상품을 2번 골랐을 수 있음 (예: 고기1, 고기2 → 둘 다 삼겹살)
        Product[] mergedProducts = new Product[customer.wantCount];
        int[] mergedWants = new int[customer.wantCount];
        int mergedCount = 0;

        for (int i = 0; i < customer.wantCount; i++) {
            Product product = customer.wantProducts[i];
            int wantAmount = customer.wantAmounts[i];

            if (wantAmount <= 0 || product == null) {
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

        // ========== 판매 처리 ==========
        if (verbose) {
            System.out.println();
            System.out.println("판매 결과:");
        }

        int customerSales = 0;

        for (int i = 0; i < mergedCount; i++) {
            Product product = mergedProducts[i];
            int wantAmount = mergedWants[i];
            int stock = display.getDisplayed(product);

            if (stock >= wantAmount) {
                // 전부 판매 가능
                int saleAmount = product.sellPrice * wantAmount;
                int profitAmount = (product.sellPrice - product.buyPrice) * wantAmount;
                display.sell(product, wantAmount);

                totalSales = totalSales + saleAmount;
                totalProfit = totalProfit + profitAmount;
                customerSales = customerSales + saleAmount;
                successCount++;

                if (verbose) {
                    System.out.printf(" - %s %d개: OK (+%,d원)%n", product.name, wantAmount, saleAmount);
                }

            } else if (stock > 0) {
                // 일부만 판매
                int saleAmount = product.sellPrice * stock;
                int profitAmount = (product.sellPrice - product.buyPrice) * stock;
                display.sell(product, stock);

                totalSales = totalSales + saleAmount;
                totalProfit = totalProfit + profitAmount;
                customerSales = customerSales + saleAmount;
                successCount++;
                failCount++;  // 일부 실패로 카운트

                if (verbose) {
                    System.out.printf(" - %s: %d/%d개만 (+%,d원)%n", product.name, stock, wantAmount, saleAmount);
                }

            } else {
                // 재고 없음
                failCount++;

                if (verbose) {
                    System.out.printf(" - %s %d개: 재고 없음!%n", product.name, wantAmount);
                }
            }
        }

        // 손님 결제 총액 출력 (직접 영업용)
        if (verbose) {
            if (customerSales > 0) {
                System.out.printf(">> 손님 결제: %,d원%n", customerSales);
            } else {
                System.out.println(">> 아무것도 못 사고 갔습니다...");
            }
        }

        return new int[]{totalSales, totalProfit, successCount, failCount};
    }
}
