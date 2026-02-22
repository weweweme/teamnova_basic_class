import core.Util;
import piece.Piece;
import player.*;
import game.*;

/// <summary>
/// 메인 클래스
/// 타이틀 화면 출력, 게임 모드 선택, 게임 시작
/// </summary>
public class Main {

    // 실행 명령어: java -cp out Main

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

        // 게임 모드 선택 루프
        boolean running = true;
        while (running) {
            int mode = selectMode();

            switch (mode) {
                case MODE_SIMPLE_2P:
                    Util.clearScreen();
                    startSimpleGame();
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
                    break;
                case MODE_CLASSIC_2P:
                    Util.clearScreen();
                    startClassicGame();
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
                    break;
                case MODE_SKILL_2P:
                    Util.clearScreen();
                    startSkillGame();
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
                    break;
                case MODE_DEMO:
                    startDemo();
                    // 시연 종료 후 메인 메뉴로 복귀 (selectMode가 화면을 다시 그림)
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
        ClassicPlayer red = new ClassicHumanPlayer(Piece.RED, "플레이어 1");
        ClassicPlayer blue = new ClassicHumanPlayer(Piece.BLUE, "플레이어 2");
        Game game = new ClassicGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 공식 체스 AI 대전 시작
    /// </summary>
    private static void startClassicAiGame(int playerColor, int difficulty) {
        ClassicPlayer red;
        ClassicPlayer blue;

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
        SkillPlayer red = new SkillHumanPlayer(Piece.RED, "플레이어 1");
        SkillPlayer blue = new SkillHumanPlayer(Piece.BLUE, "플레이어 2");
        Game game = new SkillGame(red, blue);
        game.run();
    }

    /// <summary>
    /// 스킬 모드 AI 대전 시작
    /// </summary>
    private static void startSkillAiGame(int playerColor, int difficulty) {
        SkillPlayer red;
        SkillPlayer blue;

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
    /// 커서로 기본/공식/스킬 시연을 선택하여 진행
    /// q를 누르면 메인 메뉴로 돌아감
    /// </summary>
    private static void startDemo() {
        String[] labels = {"기본 체스", "공식 체스", "스킬 모드"};
        String[] descriptions = {
                "기물의 기본 이동 방법을 배웁니다.",
                "캐슬링, 앙파상, 프로모션을 배웁니다.",
                "스킬과 아이템 사용법을 배웁니다."
        };

        while (true) {
            int index = selectMenu("튜토리얼", labels, descriptions, true);
            if (index == Util.NONE) {
                return;
            }
            switch (index) {
                case 0:
                    new DemoSimpleGame().run();
                    break;
                case 1:
                    new DemoClassicGame().run();
                    break;
                case 2:
                    new DemoSkillGame().run();
                    break;
            }
        }
    }

    // ========== 화면 ==========

    /// <summary>
    /// 타이틀 헤더 문자열 생성
    /// 체스 아스키 아트와 팀 표시를 포함한 문자열 반환
    /// </summary>
    private static String getTitleHeader() {
        return "========================================\n"
                + "      _____ _\n"
                + "     / ____| |\n"
                + "    | |    | |__   ___  ___ ___\n"
                + "    | |    | '_ \\ / _ \\/ __/ __|\n"
                + "    | |____| | | |  __/\\__ \\__ \\\n"
                + "     \\_____|_| |_|\\___||___/___/\n"
                + "\n"
                + "        " + Util.RED + "RED" + Util.RESET
                + " vs " + Util.BLUE + "BLUE" + Util.RESET + "\n"
                + "========================================";
    }

    // ========== 메뉴 ==========

    /// <summary>
    /// 커서 기반 메뉴 선택
    /// 화살표(↑↓) 또는 W/S로 커서 이동, Enter로 확정
    /// header: 메뉴 상단에 표시할 텍스트 (null이면 생략)
    /// labels: 옵션 이름 배열
    /// descriptions: 옵션 설명 배열 (labels와 같은 길이)
    /// canGoBack: true이면 q키로 뒤로가기 가능
    /// 반환: 선택한 인덱스 (0부터) 또는 Util.NONE (뒤로가기)
    /// </summary>
    private static int selectMenu(String header, String[] labels, String[] descriptions, boolean canGoBack) {
        int cursor = 0;

        while (true) {
            Util.clearScreen();

            // 헤더 출력 (있으면)
            if (header != null) {
                System.out.println(header);
            }

            // 옵션 목록 출력
            System.out.println();
            for (int i = 0; i < labels.length; i++) {
                if (i == cursor) {
                    System.out.println("  > " + labels[i]);
                } else {
                    System.out.println("    " + labels[i]);
                }
            }

            // 현재 커서 위치의 설명 출력
            System.out.println();
            System.out.println("  " + descriptions[cursor]);

            // 조작 안내
            System.out.println();
            if (canGoBack) {
                System.out.println("  ↑↓/WS: 이동  Enter: 선택  q: 뒤로");
            } else {
                System.out.println("  ↑↓/WS: 이동  Enter: 선택");
            }

            // 키 입력 처리
            int key = Util.readKey();

            switch (key) {
                case Util.KEY_UP:
                case 'w': case 'W':
                    if (cursor > 0) {
                        cursor--;
                    }
                    break;
                case Util.KEY_DOWN:
                case 's': case 'S':
                    if (cursor < labels.length - 1) {
                        cursor++;
                    }
                    break;
                case Util.KEY_ENTER:
                    return cursor;
                case Util.KEY_QUIT:
                    if (canGoBack) {
                        return Util.NONE;
                    }
                    break;
            }
        }
    }

    /// <summary>
    /// 게임 모드 선택
    /// 커서로 모드를 선택하면 대응하는 MODE 상수 반환
    /// </summary>
    private static int selectMode() {
        String header = getTitleHeader();

        String[] labels = {
                "기본 체스 (2인)",
                "기본 체스 (AI)",
                "공식 체스 (2인)",
                "공식 체스 (AI)",
                "스킬 모드 (2인)",
                "스킬 모드 (AI)",
                "튜토리얼",
                "종료"
        };

        String[] descriptions = {
                "기물의 기본 이동만 사용합니다. (캐슬링/앙파상/프로모션 없음)",
                "기본 규칙으로 AI와 대전합니다.",
                "캐슬링, 앙파상, 프로모션이 포함된 공식 규칙입니다.",
                "공식 규칙으로 AI와 대전합니다.",
                "스킬과 아이템을 사용하는 특별한 체스입니다.",
                "스킬 모드로 AI와 대전합니다.",
                "각 모드의 규칙을 단계별로 배웁니다.",
                "게임을 종료합니다."
        };

        int index = selectMenu(header, labels, descriptions, false);

        // 인덱스 → MODE 상수 변환
        // 0~6 → MODE_SIMPLE_2P(1) ~ MODE_DEMO(7)
        // 7 → MODE_QUIT(0)
        if (index == labels.length - 1) {
            return MODE_QUIT;
        }
        return index + 1;
    }

    /// <summary>
    /// AI 난이도 선택
    /// 커서로 쉬움/보통 선택, q로 뒤로가기
    /// </summary>
    private static int selectDifficulty() {
        String[] labels = {"쉬움", "보통"};
        String[] descriptions = {
                "AI가 랜덤으로 수를 선택합니다.",
                "AI가 전략적으로 수를 선택합니다."
        };

        int index = selectMenu("난이도를 선택하세요:", labels, descriptions, true);
        if (index == Util.NONE) {
            return Util.NONE;
        }
        return (index == 0) ? AiPlayer.EASY : AiPlayer.NORMAL;
    }

    /// <summary>
    /// AI 대전 시 플레이어 색상 선택
    /// 커서로 빨간팀/파란팀 선택, q로 뒤로가기
    /// </summary>
    private static int selectColor() {
        String[] labels = {
                Util.RED + "빨간팀" + Util.RESET + " (선공)",
                Util.BLUE + "파란팀" + Util.RESET + " (후공)"
        };
        String[] descriptions = {
                "먼저 움직입니다.",
                "나중에 움직입니다."
        };

        int index = selectMenu("팀을 선택하세요:", labels, descriptions, true);
        if (index == Util.NONE) {
            return Util.NONE;
        }
        return (index == 0) ? Piece.RED : Piece.BLUE;
    }
}
