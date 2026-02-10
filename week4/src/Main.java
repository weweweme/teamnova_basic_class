import java.util.Scanner;

/// <summary>
/// 메인 클래스
/// 게임 시작 화면과 모드 선택만 담당
/// 실제 게임 로직은 GameManager 클래스에서 처리
/// </summary>
public class Main {

    // ========== 상수 ==========

    static final int INIT_MONEY = 50000000;           // 초기 자본 5000만원
    static final int GOAL_MONEY = 300000000;          // 목표 금액 3억원
    static final int DEFAULT_PRICE_MULTIPLIER = 3;   // 기본 가격 배율

    // ========== 진입점 ==========

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 게임 시작 화면 출력
        printStartScreen();

        int startChoice = Util.readInt(scanner);  // 잘못된 입력 시 종료

        int money;
        int goalMoney;
        int priceMultiplier;

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
            int inputMoney = Util.readInt(scanner);
            money = inputMoney * 10000;

            System.out.print("목표 금액 입력 (만원 단위): ");
            int inputGoal = Util.readInt(scanner);
            goalMoney = inputGoal * 10000;

            System.out.print("가격 배율 입력 (1~100, 판매가에만 적용): ");
            priceMultiplier = Util.readInt(scanner);

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

        // 게임 매니저 생성 및 게임 시작
        GameManager gameManager = new GameManager(money, goalMoney, priceMultiplier, scanner);
        gameManager.run();

        scanner.close();
    }

    /// <summary>
    /// 게임 시작 화면 출력
    /// </summary>
    private static void printStartScreen() {
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
    }
}
