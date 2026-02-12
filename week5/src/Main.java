import java.util.Scanner;

/// <summary>
/// 메인 클래스
/// 타이틀 화면 출력, 게임 모드 선택, 게임 시작
/// </summary>
public class Main {

    // ========== 진입점 ==========

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 타이틀 화면 출력
        printTitleScreen();

        // 게임 모드 선택 루프
        boolean running = true;
        while (running) {
            int mode = selectMode(scanner);

            switch (mode) {
                case 1:
                    // 2인 대전
                    Util.clearScreen();
                    startGame(scanner);
                    running = false;
                    break;
                case 2:
                    // AI 대전 - 색상 선택
                    int color = selectColor(scanner);
                    if (color != 0) {
                        Util.clearScreen();
                        startGame(scanner);
                        running = false;
                    }
                    break;
                default:
                    // 0 또는 잘못된 입력 → 종료
                    System.out.println("\n게임을 종료합니다.");
                    running = false;
                    break;
            }
        }

        scanner.close();
    }

    // ========== 게임 시작 ==========

    /// <summary>
    /// 2인 대전 게임 시작
    /// 빨간팀, 파란팀 모두 사람 플레이어
    /// </summary>
    private static void startGame(Scanner scanner) {
        Player red = new HumanPlayer(Piece.RED, "플레이어 1", scanner);
        Player blue = new HumanPlayer(Piece.BLUE, "플레이어 2", scanner);
        Game game = new Game(red, blue);
        game.run();
    }

    // ========== 화면 ==========

    /// <summary>
    /// 타이틀 화면 출력
    /// 체스 아스키 아트와 게임 소개 표시
    /// </summary>
    private static void printTitleScreen() {
        System.out.println("========================================");
        System.out.println("      _____ _                     ");
        System.out.println("     / ____| |                    ");
        System.out.println("    | |    | |__   ___  ___ ___   ");
        System.out.println("    | |    | '_ \\ / _ \\/ __/ __|  ");
        System.out.println("    | |____| | | |  __/\\__ \\__ \\  ");
        System.out.println("     \\_____|_| |_|\\___||___/___/  ");
        System.out.println();
        System.out.println("        " + Util.RED + "RED" + Util.RESET
                + " vs " + Util.BLUE + "BLUE" + Util.RESET);
        System.out.println("========================================");
    }

    // ========== 메뉴 ==========

    /// <summary>
    /// 게임 모드 선택 메뉴 출력 및 입력 받기
    /// 1: 2인 대전, 2: AI 대전, 0: 종료
    /// </summary>
    private static int selectMode(Scanner scanner) {
        System.out.println();
        System.out.println("[1] 2인 대전");
        System.out.println("[2] AI 대전");
        System.out.println("[0] 종료");
        System.out.print(">> ");
        return Util.readInt(scanner);
    }

    /// <summary>
    /// AI 대전 시 플레이어 색상 선택
    /// 1: 빨간팀(선공), 2: 파란팀(후공), 0: 돌아가기
    /// </summary>
    private static int selectColor(Scanner scanner) {
        System.out.println();
        System.out.println("팀을 선택하세요:");
        System.out.println("[1] " + Util.RED + "빨간팀" + Util.RESET + " (선공)");
        System.out.println("[2] " + Util.BLUE + "파란팀" + Util.RESET + " (후공)");
        System.out.println("[0] 돌아가기");
        System.out.print(">> ");
        return Util.readInt(scanner);
    }
}
