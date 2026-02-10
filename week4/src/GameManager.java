import java.util.Scanner;

/// <summary>
/// 게임 매니저 클래스
/// 게임 루프, 승리/패배 판정, 시간 관리, 게임 상태(자본금/날짜)를 담당
/// </summary>
public class GameManager {

    // ========== 시간 상수 ==========

    public static final int TIME_MORNING = 0;    // 아침 (도매상 이용 가능)
    public static final int TIME_AFTERNOON = 1;  // 낮 (영업 전반부)
    public static final int TIME_NIGHT = 2;      // 밤 (영업 후반부)

    // ========== 게임 상태 ==========
    // Wholesaler에서 game.money로 직접 접근, Market은 영업 매출을 반환

    public int money;
    public int goalMoney;
    public int day = 1;
    public int timeOfDay = TIME_MORNING;

    private final int priceMultiplier;
    private final Scanner scanner;

    /// <summary>
    /// GameManager 생성자
    /// Main에서 설정한 게임 모드 값을 전달받음
    /// </summary>
    public GameManager(int money, int goalMoney, int priceMultiplier, Scanner scanner) {
        this.money = money;
        this.goalMoney = goalMoney;
        this.priceMultiplier = priceMultiplier;
        this.scanner = scanner;
    }

    // ========== 게임 시작 ==========

    /// <summary>
    /// 게임 메인 루프
    /// 협력 객체 초기화 -> 게임 루프
    /// </summary>
    public void run() {
        // 협력 객체 초기화
        // 상품/카테고리 데이터
        ProductCatalog catalog = new ProductCatalog(priceMultiplier);
        // 가게 (메뉴 UI + 창고 + 매대 + 캐셔 + 영업)
        Market market = new Market(this, catalog, scanner);
        // 도매상/구매/자동주문
        Wholesaler wholesaler = new Wholesaler(this, market, catalog, scanner);

        // ========== 게임 루프 ==========

        boolean playing = true;

        while (playing) {

            // 승리/패배 조건 체크 (게임 종료 시 true 반환)
            if (checkWinOrLose()) {
                break;
            }

            // 하루 시작 메뉴 출력
            market.printDailyMenu();

            int choice = Util.readInt(scanner);

            switch (choice) {
                case 1:
                    // 도매상
                    if (timeOfDay == TIME_MORNING) {
                        wholesaler.goWholesaler();
                        timeOfDay = TIME_AFTERNOON;  // 도매상 갔다오면 낮으로 전환
                    } else {
                        System.out.println("[!!] 도매상은 오전에만 이용 가능합니다.");
                    }
                    break;

                case 2:
                    // 영업 시작 전 매대 체크
                    if (!market.isBusinessReady()) {
                        break;
                    }

                    // 영업 시작 (서브메뉴)
                    int businessResult = market.showBusinessMenu();
                    switch (businessResult) {
                        case 1:
                            // 직접 영업 → 마켓이 영업 후 매출 반환
                            money = money + market.startBusiness();
                            advanceTime();  // 아침->낮, 낮->밤
                            break;
                        case 2:
                            // 빠른 영업 → 마켓이 영업 후 매출 반환
                            money = money + market.startQuickBusiness();
                            advanceTime();
                            break;
                        // case 0: 돌아가기 (아무것도 안 함)
                    }
                    break;

                case 3:
                    // 재고/매대 관리 (서브메뉴)
                    market.showInventoryMenu();
                    break;

                case 4:
                    // 다음 날로 (밤에만 가능)
                    if (timeOfDay == TIME_NIGHT) {
                        day++;
                        timeOfDay = TIME_MORNING;
                    } else {
                        System.out.println("[!!] 아직 하루가 끝나지 않았습니다.");
                    }
                    break;

                case 0:
                    playing = false;
                    break;

                default:
                    System.out.println("잘못된 입력입니다.");
                    break;
            }
        }

        System.out.println("게임을 종료합니다.");
    }

    // ========== 시간 관리 ==========

    /// <summary>
    /// 시간대를 한 단계 전진
    /// 아침 -> 낮, 낮 -> 밤, 밤 -> 아침(다음 날)
    /// </summary>
    private void advanceTime() {
        switch (timeOfDay) {
            case TIME_MORNING:
                timeOfDay = TIME_AFTERNOON;
                break;
            case TIME_AFTERNOON:
                timeOfDay = TIME_NIGHT;
                break;
            case TIME_NIGHT:
                // 밤 영업 끝 -> 다음 날 아침
                day++;
                timeOfDay = TIME_MORNING;
                break;
        }
    }

    // ========== 승리/패배 ==========

    /// <summary>
    /// 승리/패배 조건 체크
    /// 게임 종료 시 true 반환
    /// </summary>
    private boolean checkWinOrLose() {
        // 승리 조건 체크
        if (money >= goalMoney) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("         *** 축하합니다! ***");
            System.out.println("========================================");
            System.out.println("목표 금액 " + String.format("%,d", goalMoney) + "원을 달성했습니다!");
            System.out.println("이제 펜션 주인이 되셨습니다!");
            System.out.println("총 " + day + "일 만에 달성!");
            System.out.println("========================================");
            return true;
        }

        // 패배 조건 체크
        if (money <= 0) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("          *** 파산 ***");
            System.out.println("========================================");
            System.out.println("자본금이 바닥났습니다...");
            System.out.println(day + "일차에 파산했습니다.");
            System.out.println("========================================");
            return true;
        }

        return false;
    }
}
