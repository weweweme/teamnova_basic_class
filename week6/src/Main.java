import unit.colonist.Colonist;
import unit.colonist.ColonistFactory;
import unit.colonist.ColonistSpawner;
import unit.enemy.EnemyFactory;
import unit.enemy.EnemyType;
import game.DayNightCycle;
import game.GameWorld;
import game.BgmPlayer;
import game.Cutscene;
import game.Difficulty;
import game.DifficultySettings;
import game.InputHandler;
import game.Position;
import game.Renderer;
import game.SfxPlayer;
import game.Util;

/// <summary>
/// 게임 진입점
/// 타이틀 화면 → 게임 초기화 → 입력 루프 → 통계 출력
/// </summary>
public class Main {

    public static void main(String[] args) {
        Util.enableRawMode();

        // ===== 타이틀 화면 =====
        StringBuilder title = new StringBuilder();
        title.append("\033[H\033[2J");
        title.append("\n");
        title.append("      ████████  ██████  ██     ██  ████  ████  ██████\n");
        title.append("         ██    ██    ██ ███   ███ ██  ██  ██  ██    ██\n");
        title.append("         ██    ██    ██ ██ █ █ ██ ██  ██  ██  ██    ██\n");
        title.append("         ██    ██    ██ ██  █  ██ ██████  ██  ██████\n");
        title.append("         ██     ██████  ██     ██ ██  ██ ████  ██  ██\n");
        title.append("\n");
        title.append("        ╔══════════════════════════════════════════╗\n");
        title.append("        ║      표 류 자 들  - The Castaways -      ║\n");
        title.append("        ╚══════════════════════════════════════════╝\n");
        title.append("\n");
        title.append("         바리케이드 너머에서 밀려오는 적을 막아 생존하라\n");
        title.append("\n");
        title.append("      ┌─────────────────────────────────────────────┐\n");
        title.append("      │  [정착민] 피스톨 / HP 100 / 패시브 없음     │\n");
        title.append("      │  [모집] 사격수 / 저격수 / 돌격수            │\n");
        title.append("      │  [무기] 피스톨 / 샷건 / 라이플 / 미니건     │\n");
        title.append("      ├─────────────────────────────────────────────┤\n");
        title.append("      │  [조작]  ↑↓ 정착민 선택 / q 종료            │\n");
        title.append("      │  [낮] 1수리 2무기 3치료 4건설               │\n");
        title.append("      │       5모집 6승격 n밤                       │\n");
        title.append("      └─────────────────────────────────────────────┘\n");
        title.append("\n");
        title.append("              [난이도 선택]\n");
        title.append("               1 : 쉬움   (7일 생존)\n");
        title.append("               2 : 보통  (10일 생존)\n");
        title.append("               3 : 어려움 (15일 생존)\n");
        System.out.print(title);
        System.out.flush();

        // 난이도 선택 입력 대기
        final int KEY_EASY = '1';
        final int KEY_NORMAL = '2';
        final int KEY_HARD = '3';

        Difficulty selectedDifficulty = null;
        while (selectedDifficulty == null) {
            int key = InputHandler.readKey();
            switch (key) {
                case KEY_EASY:
                    selectedDifficulty = Difficulty.EASY;
                    break;
                case KEY_NORMAL:
                    selectedDifficulty = Difficulty.NORMAL;
                    break;
                case KEY_HARD:
                    selectedDifficulty = Difficulty.HARD;
                    break;
            }
        }

        // ===== 인트로 컷씬 =====
        Cutscene.intro().play();

        // ===== 게임 초기화 =====
        SfxPlayer sfxPlayer = new SfxPlayer();
        GameWorld gameWorld = new GameWorld(sfxPlayer);
        Renderer renderer = new Renderer(gameWorld);

        // 초기 보급품
        gameWorld.getSupply().add(30);

        // 팩토리 및 스포너 생성
        ColonistFactory colonistFactory = new ColonistFactory();
        ColonistSpawner colonistSpawner = new ColonistSpawner(colonistFactory);
        EnemyFactory enemyFactory = new EnemyFactory();

        // 정착민 3명 배치 (안전지대 내, 기본 유형)
        int centerRow = GameWorld.HEIGHT / 2;
        int[] startCols = {3, 7, 11};

        for (int startCol : startCols) {
            colonistSpawner.spawn(gameWorld, new Position(centerRow, startCol));
        }

        // 낮/밤 주기 생성 및 렌더러에 연결
        DifficultySettings settings = new DifficultySettings(selectedDifficulty);
        DayNightCycle dayNightCycle = new DayNightCycle(gameWorld, settings);
        renderer.setDayNightCycle(dayNightCycle);

        // 입력 처리기 생성 및 렌더러에 연결
        InputHandler inputHandler = new InputHandler(gameWorld, renderer, dayNightCycle, colonistSpawner);
        renderer.setInputHandler(inputHandler);

        // BGM 재생 시작
        BgmPlayer bgmPlayer = new BgmPlayer("bgm/Claim_To_Fame-The_Grey_Room_Clark_Sims.mp3");
        bgmPlayer.start();

        Util.clearScreen();

        // 스레드 시작 (정착민은 spawn()에서 이미 시작됨)
        dayNightCycle.start();

        // 렌더링 간격 (밀리초)
        final int RENDER_INTERVAL = 100;
        // 입력 체크 간격 (밀리초)
        final int INPUT_CHECK_INTERVAL = 16;

        long lastRenderTime = 0;

        // ===== 메인 루프 =====
        while (inputHandler.isRunning()) {
            // 입력 체크
            // 주의: System.in.available()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                if (System.in.available() > 0) {
                    int key = InputHandler.readKey();
                    inputHandler.handleInput(key);

                    // 입력 즉시 화면 갱신
                    renderer.render();
                    lastRenderTime = System.currentTimeMillis();
                }
            } catch (Exception e) {
                // IOException (컴파일러 요구사항)
            }

            // 일정 간격마다 총알 전진 + 화면 갱신
            long now = System.currentTimeMillis();
            if (now - lastRenderTime >= RENDER_INTERVAL) {
                gameWorld.advanceBullets();
                gameWorld.checkLandmines();
                renderer.render();
                lastRenderTime = now;
            }

            Util.delay(INPUT_CHECK_INTERVAL);
        }

        // ===== 종료 =====
        bgmPlayer.stopPlaying();
        dayNightCycle.stopRunning();
        gameWorld.clearEnemies();
        for (Colonist colonist : gameWorld.getColonists()) {
            colonist.stopRunning();
        }

        // 승리 또는 게임오버 컷씬 재생
        if (renderer.isVictory()) {
            Cutscene.victory().play();
        } else if (renderer.isGameOver()) {
            Cutscene.gameOver().play();
        }

        Util.disableRawMode();
        Util.clearScreen();

        // 게임 통계 출력
        int survivors = 0;
        for (Colonist colonist : gameWorld.getColonists()) {
            if (colonist.isLiving()) {
                survivors++;
            }
        }

        System.out.println("=== 게임 통계 ===");
        System.out.println("생존 일수: " + dayNightCycle.getDay() + "일");
        System.out.println("생존자: " + survivors + "/" + gameWorld.getColonists().size() + "명");
        System.out.println("처치한 적: " + gameWorld.getEnemiesKilled() + "마리");

        // 종류별 처치 수 출력
        java.util.HashMap<EnemyType, Integer> killsByType = gameWorld.getKillsByType();
        if (!killsByType.isEmpty()) {
            System.out.println();
            System.out.println("── 종류별 처치 ──");
            for (EnemyType type : EnemyType.values()) {
                int count = killsByType.getOrDefault(type, 0);
                if (count > 0) {
                    System.out.println("  " + enemyFactory.getSpec(type).getDisplayName() + ": " + count + "마리");
                }
            }
        }
    }
}
