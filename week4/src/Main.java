import java.util.Scanner;

/// <summary>
/// 강릉 펜션촌 슈퍼마켓 게임
/// 목표: 3억원 모아서 펜션 주인 되기!
/// </summary>
public class Main {

    public static void main(String[] args) {

        // ========== 상수 ==========

        final int INIT_MONEY = 50000000;   // 초기 자본 5000만원
        final int GOAL_MONEY = 300000000;  // 목표 금액 3억원
        final int MAX_SLOT = 30;           // 매대 최대 슬롯

        // ========== 게임 변수 ==========

        int money;       // 현재 보유 금액
        int goalMoney;   // 목표 금액 (커스텀 가능)
        int usedSlot = 0;             // 사용 중인 슬롯
        int day = 1;                  // 현재 날짜

        Scanner scanner = new Scanner(System.in);

        // ========== 상품 객체 생성 ==========

        // 음료
        Cola cola = new Cola();
        Cider cider = new Cider();
        Water water = new Water();
        Pocari pocari = new Pocari();
        Ipro ipro = new Ipro();

        // 맥주
        Cass cass = new Cass();
        Terra terra = new Terra();
        Hite hite = new Hite();

        // 소주
        Chamisul chamisul = new Chamisul();
        Cheumcherum cheumcherum = new Cheumcherum();
        Jinro jinro = new Jinro();

        // 간식/안주
        DriedSquid driedSquid = new DriedSquid();
        Peanut peanut = new Peanut();
        Chip chip = new Chip();

        // 고기
        Samgyupsal samgyupsal = new Samgyupsal();
        Moksal moksal = new Moksal();
        Sausage sausage = new Sausage();

        // 해수욕 용품
        Tube tube = new Tube();
        Sunscreen sunscreen = new Sunscreen();
        BeachBall beachBall = new BeachBall();

        // 식재료
        Ssamjang ssamjang = new Ssamjang();
        Lettuce lettuce = new Lettuce();
        Kimchi kimchi = new Kimchi();

        // 라면
        ShinRamen shinRamen = new ShinRamen();
        JinRamen jinRamen = new JinRamen();
        Neoguri neoguri = new Neoguri();

        // 아이스크림
        Melona melona = new Melona();
        ScrewBar screwBar = new ScrewBar();
        FishBread fishBread = new FishBread();

        // 기타
        Firework firework = new Firework();

        // ========== 상품 초기화 ==========

        // TODO: 각 상품의 name, buyPrice, sellPrice, popularity 설정

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
        System.out.println("[1] 게임 시작 (기본: 5000만원 -> 3억원)");
        System.out.println("[2] 커스텀 게임 (자본/목표 직접 설정)");
        System.out.println("[그 외] 종료");
        System.out.print(">> ");

        int startChoice = scanner.nextInt();

        if (startChoice == 1) {
            // 기본 모드
            money = INIT_MONEY;
            goalMoney = GOAL_MONEY;

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

            System.out.println();
            System.out.println("시작 자본: " + String.format("%,d", money) + "원");
            System.out.println("목표 금액: " + String.format("%,d", goalMoney) + "원으로 설정!");

        } else {
            // 종료
            System.out.println("게임을 종료합니다.");
            scanner.close();
            return;
        }

        // ========== 게임 루프 ==========

        boolean playing = true;

        while (playing) {

            // 승리 조건 체크
            if (money >= goalMoney) {
                System.out.println();
                System.out.println("========================================");
                System.out.println("         *** 축하합니다! ***");
                System.out.println("========================================");
                System.out.println("목표 금액 3억원을 달성했습니다!");
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
