import java.util.Map;

/// <summary>
/// 계산대 클래스
/// 손님이 바구니에 담아온 상품의 결제만 처리
/// 바구니에는 이미 같은 상품이 합산되어 있으므로 금액 계산만 수행
/// </summary>
public class Cashier {

    // ========== 결제 ==========

    /// <summary>
    /// 손님이 가져온 바구니 결제 처리
    /// 바구니의 상품별 금액 계산 → 결과 반환
    /// verbose=true: 결제 내역을 한 줄씩 출력 (직접 영업용)
    /// verbose=false: 출력 없이 처리 (빠른 영업용)
    /// </summary>
    /// <returns>int[2] = {매출, 이익}</returns>
    // 결제 처리 흐름:
    // 1단계: 바구니의 상품별로 매출(판매가 × 수량), 이익((판매가 - 매입가) × 수량) 계산
    // 2단계: 결제 총액 출력 후 결과 반환
    public int[] checkout(Map<Product, Integer> basket, boolean verbose) {
        int totalSales = 0;
        int totalProfit = 0;

        // ========== 1단계: 상품별 금액 계산 ==========
        // 바구니를 순회하면서 상품별 매출과 이익을 계산
        //
        // 계산 방식:
        // - 매출(saleAmount) = 판매가 × 수량 (손님이 낸 금액)
        // - 이익(profitAmount) = (판매가 - 매입가) × 수량 (실제로 남는 돈)
        //
        // 예시: 삼겹살 3개 (매입 8,000원, 판매 15,000원)
        // → 매출: 15,000 × 3 = 45,000원
        // → 이익: (15,000 - 8,000) × 3 = 21,000원
        if (verbose) {
            System.out.println();
            System.out.println("결제 내역:");
        }

        int customerSales = 0;

        for (Map.Entry<Product, Integer> entry : basket.entrySet()) {
            Product product = entry.getKey();
            int amount = entry.getValue();

            int saleAmount = product.sellPrice * amount;
            int profitAmount = (product.sellPrice - product.buyPrice) * amount;

            totalSales = totalSales + saleAmount;
            totalProfit = totalProfit + profitAmount;
            customerSales = customerSales + saleAmount;

            if (verbose) {
                System.out.printf(" - %s %d개 (+%,d원)%n", product.name, amount, saleAmount);
            }
        }

        // ========== 2단계: 결제 총액 출력 ==========
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
