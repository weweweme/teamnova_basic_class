import core.Util;
import piece.Piece;
import player.*;
import game.*;

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
                    // 기본 체스 2인 대전
                    Util.clearScreen();
                    startSimpleGame();
                    running = false;
                    break;
                case 2:
                    // 기본 체스 AI 대전
                    int color2 = selectColor();
                    if (color2 == 0) {
                        break;
                    }
                    int diff2 = selectDifficulty();
                    if (diff2 == Util.NONE) {
                        break;
                    }
                    Util.clearScreen();
                    startSimpleAiGame(color2, diff2);
                    running = false;
                    break;
                case 3:
                    // 공식 체스 2인 대전
                    Util.clearScreen();
                    startClassicGame();
                    running = false;
                    break;
                case 4:
                    // 공식 체스 AI 대전
                    int color4 = selectColor();
                    if (color4 == 0) {
                        break;
                    }
                    int diff4 = selectDifficulty();
                    if (diff4 == Util.NONE) {
                        break;
                    }
                    Util.clearScreen();
                    startClassicAiGame(color4, diff4);
                    running = false;
                    break;
                case 5:
                    // 스킬 모드 2인 대전
                    Util.clearScreen();
                    startSkillGame();
                    running = false;
                    break;
                case 6:
                    // 스킬 모드 AI 대전
                    int color6 = selectColor();
                    if (color6 == 0) {
                        break;
                    }
                    int diff6 = selectDifficulty();
                    if (diff6 == Util.NONE) {
                        break;
                    }
                    Util.clearScreen();
                    startSkillAiGame(color6, diff6);
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
    /// 기본 체스 2인 대전 시작
    /// 기물의 기본 이동만 사용 (캐슬링/앙파상/프로모션 없음)
    /// </summary>
    private static void startSimpleGame() {
        Player red = new HumanPlayer(Piece.RED, "플레이어 1");
        Player blue = new HumanPlayer(Piece.BLUE, "플레이어 2");
        Game game = new SimpleGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 기본 체스 AI 대전 시작
    /// </summary>
    private static void startSimpleAiGame(int playerColor, int difficulty) {
        Player red;
        Player blue;

        if (playerColor == 1) {
            red = new HumanPlayer(Piece.RED, "플레이어");
            blue = new AiPlayer(Piece.BLUE, "AI", difficulty);
        } else {
            red = new AiPlayer(Piece.RED, "AI", difficulty);
            blue = new HumanPlayer(Piece.BLUE, "플레이어");
        }

        Game game = new SimpleGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 공식 체스 2인 대전 시작
    /// 캐슬링, 앙파상, 프로모션 포함
    /// </summary>
    private static void startClassicGame() {
        Player red = new HumanPlayer(Piece.RED, "플레이어 1");
        Player blue = new HumanPlayer(Piece.BLUE, "플레이어 2");
        Game game = new ClassicGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 공식 체스 AI 대전 시작
    /// </summary>
    private static void startClassicAiGame(int playerColor, int difficulty) {
        Player red;
        Player blue;

        if (playerColor == 1) {
            red = new HumanPlayer(Piece.RED, "플레이어");
            blue = new AiPlayer(Piece.BLUE, "AI", difficulty);
        } else {
            red = new AiPlayer(Piece.RED, "AI", difficulty);
            blue = new HumanPlayer(Piece.BLUE, "플레이어");
        }

        Game game = new ClassicGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 스킬 모드 2인 대전 시작
    /// 스킬과 아이템을 사용할 수 있는 모드
    /// </summary>
    private static void startSkillGame() {
        Player red = new HumanPlayer(Piece.RED, "플레이어 1");
        Player blue = new HumanPlayer(Piece.BLUE, "플레이어 2");
        Game game = new SkillGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 스킬 모드 AI 대전 시작
    /// </summary>
    private static void startSkillAiGame(int playerColor, int difficulty) {
        Player red;
        Player blue;

        if (playerColor == 1) {
            red = new HumanPlayer(Piece.RED, "플레이어");
            blue = new AiPlayer(Piece.BLUE, "AI", difficulty);
        } else {
            red = new AiPlayer(Piece.RED, "AI", difficulty);
            blue = new HumanPlayer(Piece.BLUE, "플레이어");
        }

        Game game = new SkillGame(red, blue);
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
    /// 유효한 키가 눌릴 때까지 대기
    /// </summary>
    private static int selectMode() {
        System.out.println();
        System.out.println("[1] 기본 체스 (2인)");
        System.out.println("[2] 기본 체스 (AI)");
        System.out.println("[3] 공식 체스 (2인)");
        System.out.println("[4] 공식 체스 (AI)");
        System.out.println("[5] 스킬 모드 (2인)");
        System.out.println("[6] 스킬 모드 (AI)");
        System.out.println("[0] 종료");

        // 유효한 키가 입력될 때까지 반복
        while (true) {
            int key = Util.readInt();
            if (key >= 0 && key <= 6) {
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
                return Util.NONE;  // 돌아가기
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
