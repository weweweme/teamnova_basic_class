import system.Colonist;
import system.DayNightCycle;
import system.GameMap;
import system.Position;
import system.Renderer;
import system.Util;

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
        title.append("\n\n\n\n\n\n");
        title.append("             ╔═════════════════════════════════╗\n");
        title.append("             ║                                 ║\n");
        title.append("             ║        표  류  자  들           ║\n");
        title.append("             ║       - The Castaways -         ║\n");
        title.append("             ║                                 ║\n");
        title.append("             ╚═════════════════════════════════╝\n");
        title.append("\n");
        title.append("            바리케이드 너머에서 밀려오는 적을 막아라\n");
        title.append("\n\n");
        title.append("              [조작법]\n");
        title.append("               ↑↓ : 정착민 선택\n");
        title.append("               q  : 종료\n");
        title.append("\n\n");
        title.append("              1번을 눌러 시작하세요...\n");
        System.out.print(title);
        System.out.flush();

        // 1번 키 입력 대기
        final int KEY_START = '1';
        int startKey;
        do {
            startKey = Util.readKey();
        } while (startKey != KEY_START);

        // ===== 게임 초기화 =====
        GameMap gameMap = new GameMap();
        Renderer renderer = new Renderer(gameMap);

        // 초기 보급품
        gameMap.getSupply().add(30);

        // 정착민 3명 배치 (안전지대 내)
        int centerRow = GameMap.HEIGHT / 2;
        Colonist chulsoo = new Colonist("김철수", new Position(centerRow, 3), gameMap);
        Colonist younghee = new Colonist("이영희", new Position(centerRow, 7), gameMap);
        Colonist minsoo = new Colonist("박민수", new Position(centerRow, 11), gameMap);
        gameMap.addColonist(chulsoo);
        gameMap.addColonist(younghee);
        gameMap.addColonist(minsoo);

        // 낮/밤 주기 생성 및 렌더러에 연결
        DayNightCycle dayNightCycle = new DayNightCycle(gameMap);
        renderer.setDayNightCycle(dayNightCycle);

        Util.clearScreen();

        // 스레드 시작
        dayNightCycle.start();
        chulsoo.start();
        younghee.start();
        minsoo.start();

        // 렌더링 간격 (밀리초)
        final int RENDER_INTERVAL = 100;
        // 입력 체크 간격 (밀리초)
        final int INPUT_CHECK_INTERVAL = 16;

        long lastRenderTime = 0;

        // ===== 메인 루프 =====
        boolean running = true;
        while (running) {
            // 입력 체크
            // 주의: System.in.available()은 checked exception이라 try-catch 필수 (컴파일러 요구)
            try {
                if (System.in.available() > 0) {
                    int key = Util.readKey();

                    if (renderer.isGameOver()) {
                        // 게임오버 시 q키만 허용
                        if (key == Util.KEY_QUIT) {
                            running = false;
                        }
                    } else {
                        // 밤 건너뛰기 키
                        final int KEY_SKIP_NIGHT = 'n';

                        switch (key) {
                            case Util.KEY_QUIT:
                                running = false;
                                break;
                            case Util.KEY_UP:
                                renderer.selectPrevious();
                                break;
                            case Util.KEY_DOWN:
                                renderer.selectNext();
                                break;
                            case KEY_SKIP_NIGHT:
                                dayNightCycle.skipToNight();
                                break;
                        }
                    }

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
                gameMap.advanceBullets();
                renderer.render();
                lastRenderTime = now;
            }

            Util.delay(INPUT_CHECK_INTERVAL);
        }

        // ===== 종료 =====
        dayNightCycle.stopRunning();
        gameMap.clearEnemies();
        for (Colonist colonist : gameMap.getColonists()) {
            colonist.stopRunning();
        }

        Util.disableRawMode();
        Util.clearScreen();

        // 게임 통계 출력
        int survivors = 0;
        for (Colonist colonist : gameMap.getColonists()) {
            if (colonist.isLiving()) {
                survivors++;
            }
        }

        System.out.println("=== 게임 통계 ===");
        System.out.println("생존 일수: " + dayNightCycle.getDay() + "일");
        System.out.println("생존자: " + survivors + "/" + gameMap.getColonists().size() + "명");
        System.out.println("처치한 적: " + gameMap.getEnemiesKilled() + "마리");
    }
}
