import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // ========== 상수 ==========

        final int INIT_MONEY = 50000000;           // 초기 자본 5000만원
        final int GOAL_MONEY = 300000000;          // 목표 금액 3억원
        final int MAX_SLOT = 30;                   // 매대 최대 슬롯
        final int DEFAULT_PRICE_MULTIPLIER = 3;   // 기본 가격 배율

        // ========== 게임 변수 ==========

        int money;
        int goalMoney;
        int priceMultiplier;
        int usedSlot = 0;
        int day = 1;

        Scanner scanner = new Scanner(System.in);

        // ========== 게임 시작 화면 ==========

        System.out.println("========================================");
        System.out.println("     _____                      ");
        System.out.println("    / ____|                     ");
        System.out.println("   | (___  _   _ _ __   ___ _ __ ");
        System.out.println("    \\___ \\| | | | '_ \\ / _ \\ '__|");
        System.out.println("    ____) | |_| | |_) |  __/ |   ");
        System.out.println("   |_____/ \\__,_| .__/ \\___|_|   ");
        System.out.println("                | |              ");
        System.out.println("                |_|   @ Gangneung");
        System.out.println("========================================");
        System.out.println("목표: 돈을 모아서 펜션 주인이 되자!");
        System.out.println();
        System.out.println("[1] 게임 시작 (기본: 5000만원 -> 3억원, 배율 3배)");
        System.out.println("[2] 커스텀 게임 (자본/목표/배율 직접 설정)");
        System.out.println("[그 외] 종료");
        System.out.print(">> ");

        int startChoice = scanner.nextInt();

        if (startChoice == 1) {
            // 기본 모드
            money = INIT_MONEY;
            goalMoney = GOAL_MONEY;
            priceMultiplier = DEFAULT_PRICE_MULTIPLIER;

        } else if (startChoice == 2) {
            // 커스텀 모드
            System.out.println();
            System.out.println("========================================");
            System.out.println("         [ 커스텀 게임 설정 ]");
            System.out.println("========================================");

            System.out.print("시작 자본 입력 (만원 단위): ");
            int inputMoney = scanner.nextInt();
            money = inputMoney * 10000;

            System.out.print("목표 금액 입력 (만원 단위): ");
            int inputGoal = scanner.nextInt();
            goalMoney = inputGoal * 10000;

            System.out.print("가격 배율 입력 (1~100, 판매가에만 적용): ");
            priceMultiplier = scanner.nextInt();

            System.out.println();
            System.out.println("----------------------------------------");
            System.out.println("시작 자본: " + String.format("%,d", money) + "원");
            System.out.println("목표 금액: " + String.format("%,d", goalMoney) + "원");
            System.out.println("가격 배율: " + priceMultiplier + "배");
            System.out.println("----------------------------------------");

        } else {
            // 종료
            System.out.println("게임을 종료합니다.");
            scanner.close();
            return;
        }

        // ========== 상품 객체 생성 (배율 적용) ==========
        // sellPrice에만 배율 적용 (매입가는 현실 가격, 판매가는 배율 적용)

        // 음료
        Cola cola = new Cola("코카콜라", 800, 1500 * priceMultiplier, 7);
        Cider cider = new Cider("칠성사이다", 800, 1500 * priceMultiplier, 6);
        Water water = new Water("삼다수", 400, 1000 * priceMultiplier, 5);
        Pocari pocari = new Pocari("포카리스웨트", 1200, 2000 * priceMultiplier, 6);
        Ipro ipro = new Ipro("이프로", 1000, 1800 * priceMultiplier, 5);

        // 맥주
        Cass cass = new Cass("카스", 1500, 3000 * priceMultiplier, 8);
        Terra terra = new Terra("테라", 1600, 3200 * priceMultiplier, 8);
        Hite hite = new Hite("하이트", 1400, 2800 * priceMultiplier, 7);

        // 소주
        Chamisul chamisul = new Chamisul("참이슬", 1200, 2500 * priceMultiplier, 9);
        Cheumcherum cheumcherum = new Cheumcherum("처음처럼", 1200, 2500 * priceMultiplier, 8);
        Jinro jinro = new Jinro("진로", 1300, 2600 * priceMultiplier, 7);

        // 간식/안주
        DriedSquid driedSquid = new DriedSquid("마른오징어", 3000, 6000 * priceMultiplier, 6);
        Peanut peanut = new Peanut("땅콩", 2000, 4000 * priceMultiplier, 5);
        Chip chip = new Chip("감자칩", 1500, 3000 * priceMultiplier, 6);

        // 고기
        Samgyupsal samgyupsal = new Samgyupsal("삼겹살", 8000, 15000 * priceMultiplier, 10);
        Moksal moksal = new Moksal("목살", 9000, 16000 * priceMultiplier, 9);
        Sausage sausage = new Sausage("소세지", 3000, 6000 * priceMultiplier, 7);

        // 해수욕 용품
        Tube tube = new Tube("튜브", 5000, 15000 * priceMultiplier, 7);
        Sunscreen sunscreen = new Sunscreen("선크림", 8000, 20000 * priceMultiplier, 8);
        BeachBall beachBall = new BeachBall("비치볼", 3000, 8000 * priceMultiplier, 5);

        // 식재료
        Ssamjang ssamjang = new Ssamjang("쌈장", 2000, 4000 * priceMultiplier, 6);
        Lettuce lettuce = new Lettuce("상추", 2000, 4000 * priceMultiplier, 7);
        Kimchi kimchi = new Kimchi("김치", 3000, 6000 * priceMultiplier, 5);

        // 라면
        ShinRamen shinRamen = new ShinRamen("신라면", 800, 1500 * priceMultiplier, 8);
        JinRamen jinRamen = new JinRamen("진라면", 700, 1400 * priceMultiplier, 7);
        Neoguri neoguri = new Neoguri("너구리", 800, 1500 * priceMultiplier, 6);

        // 아이스크림
        Melona melona = new Melona("메로나", 500, 1200 * priceMultiplier, 7);
        ScrewBar screwBar = new ScrewBar("스크류바", 600, 1300 * priceMultiplier, 6);
        FishBread fishBread = new FishBread("붕어싸만코", 800, 1500 * priceMultiplier, 6);

        // 기타
        Firework firework = new Firework("폭죽", 5000, 15000 * priceMultiplier, 9);

        // ========== 게임 루프 ==========

        boolean playing = true;

        while (playing) {

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
                break;
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
                break;
            }

            // 하루 시작 메뉴
            System.out.println();
            System.out.println("========================================");
            System.out.println("          [  " + day + "일차 - 아침  ]");
            System.out.println("========================================");
            System.out.println("현재 자본: " + String.format("%,d", money) + "원");
            System.out.println("매대 현황: " + usedSlot + " / " + MAX_SLOT + "칸");
            System.out.println();
            System.out.println("[1] 도매상 가기 (상품 입고)");
            System.out.println("[2] 영업 시작");
            System.out.println("[3] 현재 재고 확인");
            System.out.println("[0] 게임 종료");
            System.out.print(">> ");

            int choice = scanner.nextInt();

            if (choice == 1) {
                // 도매상
                System.out.println();
                System.out.println("========================================");
                System.out.println("            [ 도매상 ]");
                System.out.println("========================================");
                // TODO: 도매상 구현

            } else if (choice == 2) {
                // 영업 시작
                System.out.println();
                System.out.println("========================================");
                System.out.println("           [ 영업 시작 ]");
                System.out.println("========================================");
                // TODO: 영업 구현

                day++;

            } else if (choice == 3) {
                // 재고 확인
                System.out.println();
                System.out.println("========================================");
                System.out.println("           [ 현재 재고 ]");
                System.out.println("========================================");
                // TODO: 재고 확인 구현

            } else if (choice == 0) {
                playing = false;

            } else {
                System.out.println("잘못된 입력입니다.");
            }
        }

        System.out.println("게임을 종료합니다.");
        scanner.close();
    }
}
