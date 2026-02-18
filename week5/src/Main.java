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

    // ========== 모드 선택 상수 ==========

    // 기본 체스 2인 대전
    private static final int MODE_SIMPLE_2P = 1;

    // 기본 체스 AI 대전
    private static final int MODE_SIMPLE_AI = 2;

    // 공식 체스 2인 대전
    private static final int MODE_CLASSIC_2P = 3;

    // 공식 체스 AI 대전
    private static final int MODE_CLASSIC_AI = 4;

    // 스킬 모드 2인 대전
    private static final int MODE_SKILL_2P = 5;

    // 스킬 모드 AI 대전
    private static final int MODE_SKILL_AI = 6;

    // 시연 모드
    private static final int MODE_DEMO = 7;

    // 종료
    private static final int MODE_QUIT = 0;

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
                case MODE_SIMPLE_2P:
                    Util.clearScreen();
                    startSimpleGame();
                    running = false;
                    break;
                case MODE_SIMPLE_AI:
                    int color2 = selectColor();
                    if (color2 == Util.NONE) {
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
                case MODE_CLASSIC_2P:
                    Util.clearScreen();
                    startClassicGame();
                    running = false;
                    break;
                case MODE_CLASSIC_AI:
                    int color4 = selectColor();
                    if (color4 == Util.NONE) {
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
                case MODE_SKILL_2P:
                    Util.clearScreen();
                    startSkillGame();
                    running = false;
                    break;
                case MODE_SKILL_AI:
                    int color6 = selectColor();
                    if (color6 == Util.NONE) {
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
                case MODE_DEMO:
                    Util.clearScreen();
                    startDemo();
                    running = false;
                    break;
                default:
                    // MODE_QUIT → 종료
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

        if (playerColor == Piece.RED) {
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
        Player red = new ClassicHumanPlayer(Piece.RED, "플레이어 1");
        Player blue = new ClassicHumanPlayer(Piece.BLUE, "플레이어 2");
        Game game = new ClassicGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 공식 체스 AI 대전 시작
    /// </summary>
    private static void startClassicAiGame(int playerColor, int difficulty) {
        Player red;
        Player blue;

        if (playerColor == Piece.RED) {
            red = new ClassicHumanPlayer(Piece.RED, "플레이어");
            blue = new ClassicAiPlayer(Piece.BLUE, "AI", difficulty);
        } else {
            red = new ClassicAiPlayer(Piece.RED, "AI", difficulty);
            blue = new ClassicHumanPlayer(Piece.BLUE, "플레이어");
        }

        Game game = new ClassicGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 스킬 모드 2인 대전 시작
    /// 스킬과 아이템을 사용할 수 있는 모드
    /// </summary>
    private static void startSkillGame() {
        Player red = new SkillHumanPlayer(Piece.RED, "플레이어 1");
        Player blue = new SkillHumanPlayer(Piece.BLUE, "플레이어 2");
        Game game = new SkillGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 스킬 모드 AI 대전 시작
    /// </summary>
    private static void startSkillAiGame(int playerColor, int difficulty) {
        Player red;
        Player blue;

        if (playerColor == Piece.RED) {
            red = new SkillHumanPlayer(Piece.RED, "플레이어");
            blue = new SkillAiPlayer(Piece.BLUE, "AI", difficulty);
        } else {
            red = new SkillAiPlayer(Piece.RED, "AI", difficulty);
            blue = new SkillHumanPlayer(Piece.BLUE, "플레이어");
        }

        Game game = new SkillGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 시연 모드 시작
    /// 기본 → 공식 → 스킬 순서로 시연 게임을 진행
    /// 각 시연에서 q를 누르면 다음 시연으로 넘어감
    /// </summary>
    private static void startDemo() {
        // 1단계: 기본 모드 시연 (Piece 상속 구조)
        Game demo1 = new DemoSimpleGame();
        demo1.run();

        // 전환 화면
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("  기본 모드 시연 완료!");
        System.out.println("  다음: 공식 모드 시연");
        System.out.println("  (아무 키나 누르세요)");
        System.out.println("========================================");
        Util.readKey();

        // 2단계: 공식 모드 시연 (ClassicBoard 상속)
        Game demo2 = new DemoClassicGame();
        demo2.run();

        // 전환 화면
        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("  공식 모드 시연 완료!");
        System.out.println("  다음: 스킬 모드 시연");
        System.out.println("  (아무 키나 누르세요)");
        System.out.println("========================================");
        Util.readKey();

        // 3단계: 스킬 모드 시연 (SkillBoard 상속)
        Game demo3 = new DemoSkillGame();
        demo3.run();

        Util.clearScreen();
        System.out.println("========================================");
        System.out.println("  시연 완료!");
        System.out.println("========================================");
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
        System.out.println("[" + MODE_SIMPLE_2P + "] 기본 체스 (2인)");
        System.out.println("[" + MODE_SIMPLE_AI + "] 기본 체스 (AI)");
        System.out.println("[" + MODE_CLASSIC_2P + "] 공식 체스 (2인)");
        System.out.println("[" + MODE_CLASSIC_AI + "] 공식 체스 (AI)");
        System.out.println("[" + MODE_SKILL_2P + "] 스킬 모드 (2인)");
        System.out.println("[" + MODE_SKILL_AI + "] 스킬 모드 (AI)");
        System.out.println("[" + MODE_DEMO + "] 시연 모드");
        System.out.println("[" + MODE_QUIT + "] 종료");

        // 유효한 키가 입력될 때까지 반복
        while (true) {
            int key = Util.readInt();
            if (key >= MODE_QUIT && key <= MODE_DEMO) {
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
        final int KEY_BACK = 0;
        final int KEY_EASY = 1;
        final int KEY_NORMAL = 2;

        System.out.println();
        System.out.println("난이도를 선택하세요:");
        System.out.println("[" + KEY_EASY + "] 쉬움");
        System.out.println("[" + KEY_NORMAL + "] 보통");
        System.out.println("[" + KEY_BACK + "] 돌아가기");

        // 유효한 키가 입력될 때까지 반복
        while (true) {
            int key = Util.readInt();
            if (key == KEY_BACK) {
                return Util.NONE;
            }
            if (key == KEY_EASY) {
                return AiPlayer.EASY;
            }
            if (key == KEY_NORMAL) {
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
        final int KEY_BACK = 0;
        final int KEY_RED = 1;
        final int KEY_BLUE = 2;

        System.out.println();
        System.out.println("팀을 선택하세요:");
        System.out.println("[" + KEY_RED + "] " + Util.RED + "빨간팀" + Util.RESET + " (선공)");
        System.out.println("[" + KEY_BLUE + "] " + Util.BLUE + "파란팀" + Util.RESET + " (후공)");
        System.out.println("[" + KEY_BACK + "] 돌아가기");

        // 유효한 키가 입력될 때까지 반복
        while (true) {
            int key = Util.readInt();
            if (key == KEY_BACK) {
                return Util.NONE;
            }
            if (key == KEY_RED) {
                return Piece.RED;
            }
            if (key == KEY_BLUE) {
                return Piece.BLUE;
            }
        }
    }
}
