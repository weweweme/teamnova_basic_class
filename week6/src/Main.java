import entity.Colonist;
import entity.ColonistType;
import world.Barricade;
import world.DayNightCycle;
import world.GameMap;
import world.Spike;
import core.Difficulty;
import core.DifficultySettings;
import core.Position;
import core.Renderer;
import core.Util;

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

        // 정착민 3명 배치 (안전지대 내, 각기 다른 유형)
        int centerRow = GameMap.HEIGHT / 2;
        Colonist chulsoo = new Colonist(ColonistType.GUNNER, "김철수", new Position(centerRow, 3), gameMap);
        Colonist younghee = new Colonist(ColonistType.SNIPER, "이영희", new Position(centerRow, 7), gameMap);
        Colonist minsoo = new Colonist(ColonistType.ASSAULT, "박민수", new Position(centerRow, 11), gameMap);
        gameMap.addColonist(chulsoo);
        gameMap.addColonist(younghee);
        gameMap.addColonist(minsoo);

        // 낮/밤 주기 생성 및 렌더러에 연결 (임시 NORMAL)
        DifficultySettings settings = new DifficultySettings(Difficulty.NORMAL);
        DayNightCycle dayNightCycle = new DayNightCycle(gameMap, settings);
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

                    if (renderer.isVictory() || renderer.isGameOver()) {
                        // 승리 또는 게임오버 시 q키만 허용
                        if (key == Util.KEY_QUIT) {
                            running = false;
                        }
                    } else {
                        // 명령 키
                        final int KEY_REPAIR = '1';
                        final int KEY_UPGRADE = '2';
                        final int KEY_HEAL = '3';
                        final int KEY_SPIKE = '4';
                        final int KEY_SKIP_NIGHT = 'n';

                        // 명령 비용
                        final int REPAIR_COST = 10;
                        final int REPAIR_AMOUNT = 30;
                        final int UPGRADE_COST = 15;
                        final int HEAL_COST = 10;
                        final int HEAL_AMOUNT = 30;
                        final int SPIKE_COST = 20;
                        final int SPIKE_SPACING = 5;

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
                            case KEY_REPAIR:
                                // 낮에만 사용 가능
                                if (!dayNightCycle.isNight() && gameMap.getSupply().spend(REPAIR_COST)) {
                                    gameMap.getBarricade().repair(REPAIR_AMOUNT);
                                    gameMap.addLog(">> 바리케이드 수리 (+" + REPAIR_AMOUNT + ")");
                                }
                                break;
                            case KEY_UPGRADE:
                                if (!dayNightCycle.isNight() && gameMap.getSupply().spend(UPGRADE_COST)) {
                                    Colonist selected = gameMap.getColonists().get(renderer.getSelectedIndex());
                                    if (selected.isLiving()) {
                                        selected.upgradeWeapon();
                                        gameMap.addLog(">> " + selected.getColonistName() + " 무기 강화 (Lv" + selected.getWeaponLevel() + ")");
                                    }
                                }
                                break;
                            case KEY_HEAL:
                                if (!dayNightCycle.isNight() && gameMap.getSupply().spend(HEAL_COST)) {
                                    Colonist selected = gameMap.getColonists().get(renderer.getSelectedIndex());
                                    if (selected.isLiving()) {
                                        selected.heal(HEAL_AMOUNT);
                                        gameMap.addLog(">> " + selected.getColonistName() + " 치료 (+" + HEAL_AMOUNT + ")");
                                    }
                                }
                                break;
                            case KEY_SPIKE:
                                if (!dayNightCycle.isNight() && gameMap.getSupply().spend(SPIKE_COST)) {
                                    int spikeCol = Barricade.COLUMN + SPIKE_SPACING * (gameMap.getSpikes().size() + 1);
                                    gameMap.addSpike(new Spike(spikeCol));
                                    gameMap.addLog(">> 가시덫 설치 (열 " + spikeCol + ")");
                                }
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
