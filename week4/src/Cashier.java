/// <summary>
/// 계산대 클래스
/// 손님이 가져온 상품의 결제만 처리 (재고 확인/차감은 하지 않음)
/// 같은 상품 합산 → 금액 계산 → 결과 반환
/// </summary>
public class Cashier {

    // ========== 결제 ==========

    /// <summary>
    /// 손님 결제 처리
    /// 손님이 가져온 상품의 같은 상품 합산 → 금액 계산 → 결과 반환
    /// verbose=true: 결제 내역을 한 줄씩 출력 (직접 영업용)
    /// verbose=false: 출력 없이 처리 (빠른 영업용)
    /// </summary>
    /// <returns>int[2] = {매출, 이익}</returns>
    public int[] checkout(Customer customer, boolean verbose) {
        int totalSales = 0;
        int totalProfit = 0;

        // ========== 같은 상품 합산 ==========
        // 손님이 같은 카테고리에서 같은 상품을 2번 골랐을 수 있음 (예: 고기1, 고기2 → 둘 다 삼겹살)
        Product[] mergedProducts = new Product[customer.wantCount];
        int[] mergedAmounts = new int[customer.wantCount];
        int mergedCount = 0;

        for (int i = 0; i < customer.wantCount; i++) {
            Product product = customer.wantProducts[i];
            int amount = customer.wantAmounts[i];

            if (amount <= 0 || product == null) {
                continue;
            }

            // 이미 합산 목록에 있는지 확인
            boolean found = false;
            for (int m = 0; m < mergedCount; m++) {
                if (mergedProducts[m] == product) {
                    mergedAmounts[m] = mergedAmounts[m] + amount;
                    found = true;
                    break;
                }
            }

            // 없으면 새로 추가
            if (!found) {
                mergedProducts[mergedCount] = product;
                mergedAmounts[mergedCount] = amount;
                mergedCount++;
            }
        }

        // ========== 금액 계산 ==========
        if (verbose) {
            System.out.println();
            System.out.println("결제 내역:");
        }

        int customerSales = 0;

        for (int i = 0; i < mergedCount; i++) {
            Product product = mergedProducts[i];
            int amount = mergedAmounts[i];

            int saleAmount = product.sellPrice * amount;
            int profitAmount = (product.sellPrice - product.buyPrice) * amount;

            totalSales = totalSales + saleAmount;
            totalProfit = totalProfit + profitAmount;
            customerSales = customerSales + saleAmount;

            if (verbose) {
                System.out.printf(" - %s %d개 (+%,d원)%n", product.name, amount, saleAmount);
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

        return new int[]{totalSales, totalProfit};
    }
}
