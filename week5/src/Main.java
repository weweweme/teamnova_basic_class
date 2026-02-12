/// <summary>
/// 메인 클래스
/// 타이틀 화면 출력, 게임 모드 선택, 게임 시작
/// </summary>
public class Main {

    // 실행 명령어 java -cp out Main

    // ========== 진입점 ==========

    public static void main(String[] args) {
        // 터미널 raw 모드 활성화 (키 입력을 즉시 읽기 위해)
        Util.enableRawMode();

        // 타이틀 화면 출력
        printTitleScreen();

        // 게임 모드 선택 루프
        boolean running = true;
        while (running) {
            int mode = selectMode();

            switch (mode) {
                case 1:
                    // 2인 대전
                    Util.clearScreen();
                    startGame();
                    running = false;
                    break;
                case 2:
                    // AI 대전 - 색상 선택
                    int color = selectColor();
                    if (color == 0) {
                        break;
                    }
                    // 난이도 선택 (-1이면 돌아가기)
                    int difficulty = selectDifficulty();
                    if (difficulty == -1) {
                        break;
                    }
                    Util.clearScreen();
                    startAiGame(color, difficulty);
                    running = false;
                    break;
                default:
                    // 0 → 종료
                    System.out.println("\n게임을 종료합니다.");
                    running = false;
                    break;
            }
        }

        // 터미널 원래 모드로 복원
        Util.disableRawMode();
    }

    // ========== 게임 시작 ==========

    /// <summary>
    /// 2인 대전 게임 시작
    /// 빨간팀, 파란팀 모두 사람 플레이어
    /// </summary>
    private static void startGame() {
        Player red = new HumanPlayer(Piece.RED, "플레이어 1");
        Player blue = new HumanPlayer(Piece.BLUE, "플레이어 2");
        Game game = new Game(red, blue);
        game.run();
    }

    /// <summary>
    /// AI 대전 게임 시작
    /// 선택한 색상에 따라 사람/AI 배치
    /// 1: 플레이어가 빨간팀(선공), 2: 플레이어가 파란팀(후공)
    /// </summary>
    private static void startAiGame(int playerColor, int difficulty) {
        Player red;
        Player blue;

        if (playerColor == 1) {
            // 플레이어가 빨간팀(선공), AI가 파란팀
            red = new HumanPlayer(Piece.RED, "플레이어");
            blue = new AiPlayer(Piece.BLUE, "AI", difficulty);
        } else {
            // AI가 빨간팀(선공), 플레이어가 파란팀
            red = new AiPlayer(Piece.RED, "AI", difficulty);
            blue = new HumanPlayer(Piece.BLUE, "플레이어");
        }

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
    /// 유효한 키가 눌릴 때까지 대기
    /// </summary>
    private static int selectMode() {
        System.out.println();
        System.out.println("[1] 2인 대전");
        System.out.println("[2] AI 대전");
        System.out.println("[0] 종료");

        // 유효한 키가 입력될 때까지 반복
        while (true) {
            int key = Util.readInt();
            if (key == 0 || key == 1 || key == 2) {
                return key;
            }
        }
    }

    /// <summary>
    /// AI 난이도 선택
    /// 1: 쉬움(랜덤), 2: 보통(전략), 0: 돌아가기
    /// 유효한 키가 눌릴 때까지 대기
    /// </summary>
    private static int selectDifficulty() {
        System.out.println();
        System.out.println("난이도를 선택하세요:");
        System.out.println("[1] 쉬움");
        System.out.println("[2] 보통");
        System.out.println("[0] 돌아가기");

        // 유효한 키가 입력될 때까지 반복
        while (true) {
            int key = Util.readInt();
            if (key == 0) {
                return -1;  // 돌아가기
            }
            if (key == 1) {
                return AiPlayer.EASY;
            }
            if (key == 2) {
                return AiPlayer.NORMAL;
            }
        }
    }

    /// <summary>
    /// AI 대전 시 플레이어 색상 선택
    /// 1: 빨간팀(선공), 2: 파란팀(후공), 0: 돌아가기
    /// 유효한 키가 눌릴 때까지 대기
    /// </summary>
    private static int selectColor() {
        System.out.println();
        System.out.println("팀을 선택하세요:");
        System.out.println("[1] " + Util.RED + "빨간팀" + Util.RESET + " (선공)");
        System.out.println("[2] " + Util.BLUE + "파란팀" + Util.RESET + " (후공)");
        System.out.println("[0] 돌아가기");

        // 유효한 키가 입력될 때까지 반복
        while (true) {
            int key = Util.readInt();
            if (key == 0 || key == 1 || key == 2) {
                return key;
            }
        }
    }
}
