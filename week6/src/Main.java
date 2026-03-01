import entity.colonist.Colonist;
import entity.colonist.ColonistFactory;
import entity.colonist.ColonistSpec;
import entity.colonist.ColonistType;
import gun.Gun;
import gun.Pistol;
import gun.Shotgun;
import gun.Rifle;
import gun.Minigun;
import entity.enemy.EnemyFactory;
import entity.enemy.EnemyType;
import structure.AmmoBox;
import structure.Barricade;
import game.DayNightCycle;
import game.GameMap;
import structure.Landmine;
import structure.Spike;
import game.Cutscene;
import game.Difficulty;
import game.DifficultySettings;
import game.Position;
import game.Renderer;
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
        title.append("      │  [정착민] HP 100           [무기]           │\n");
        title.append("      │   사격수(A) 피스톨         피스톨  단발     │\n");
        title.append("      │   저격수(B) 라이플         샷건    산탄 3발 │\n");
        title.append("      │   돌격수(C) 샷건           라이플  관통     │\n");
        title.append("      │                            미니건  연사     │\n");
        title.append("      ├─────────────────────────────────────────────┤\n");
        title.append("      │  [조작]  ↑↓ 정착민 선택  /  q 종료         │\n");
        title.append("      │  [낮]    1 수리 / 2 무기 / 3 치료 / 4 덫   │\n");
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
            int key = Util.readKey();
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
        GameMap gameMap = new GameMap();
        Renderer renderer = new Renderer(gameMap);

        // 초기 보급품
        gameMap.getSupply().add(30);

        // 팩토리 생성
        ColonistFactory colonistFactory = new ColonistFactory();
        EnemyFactory enemyFactory = new EnemyFactory();

        // 정착민 3명 배치 (안전지대 내, 각기 다른 유형)
        int centerRow = GameMap.HEIGHT / 2;
        Colonist chulsoo = new Colonist(ColonistType.GUNNER, colonistFactory.getSpec(ColonistType.GUNNER), "김철수", gameMap.issueNextLabel(), new Position(centerRow, 3), gameMap);
        Colonist younghee = new Colonist(ColonistType.SNIPER, colonistFactory.getSpec(ColonistType.SNIPER), "이영희", gameMap.issueNextLabel(), new Position(centerRow, 7), gameMap);
        Colonist minsoo = new Colonist(ColonistType.ASSAULT, colonistFactory.getSpec(ColonistType.ASSAULT), "박민수", gameMap.issueNextLabel(), new Position(centerRow, 11), gameMap);
        // 유형별 기본 무기 배정
        chulsoo.setGun(new Pistol());
        younghee.setGun(new Rifle());
        minsoo.setGun(new Shotgun());

        gameMap.addColonist(chulsoo);
        gameMap.addColonist(younghee);
        gameMap.addColonist(minsoo);

        // 낮/밤 주기 생성 및 렌더러에 연결
        DifficultySettings settings = new DifficultySettings(selectedDifficulty);
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

        // 서브 메뉴 모드 여부
        boolean shopMode = false;
        boolean buildMode = false;
        boolean recruitMode = false;

        // 무기 상점 메뉴 키
        final int SHOP_PISTOL = '1';
        final int SHOP_SHOTGUN = '2';
        final int SHOP_RIFLE = '3';
        final int SHOP_MINIGUN = '4';

        // 건설 메뉴 키
        final int BUILD_SPIKE = '1';
        final int BUILD_LANDMINE = '2';
        final int BUILD_AMMOBOX = '3';
        final int BUILD_UPGRADE = '4';

        // 모집 메뉴 키
        final int RECRUIT_GUNNER = '1';
        final int RECRUIT_SNIPER = '2';
        final int RECRUIT_ASSAULT = '3';

        // 모집 비용
        final int RECRUIT_COST = 40;

        // 최대 정착민 수
        final int MAX_COLONISTS = 5;

        // 모집 이름 카운터
        int recruitCount = 0;

        // 구조물 간격 (바리케이드 기준 오른쪽으로)
        final int STRUCTURE_SPACING = 5;

        // 탄약 상자 설치 열 (바리케이드 왼쪽 안전지대)
        final int AMMOBOX_COL = 1;

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
                    } else if (shopMode) {
                        // 무기 상점 모드: 무기 선택 또는 취소
                        Gun purchased = null;
                        switch (key) {
                            case SHOP_PISTOL:
                                purchased = new Pistol();
                                break;
                            case SHOP_SHOTGUN:
                                purchased = new Shotgun();
                                break;
                            case SHOP_RIFLE:
                                purchased = new Rifle();
                                break;
                            case SHOP_MINIGUN:
                                purchased = new Minigun();
                                break;
                            case Util.KEY_QUIT:
                                shopMode = false;
                                renderer.setShopMode(false);
                                break;
                        }

                        if (purchased != null) {
                            Colonist selected = gameMap.getColonists().get(renderer.getSelectedIndex());
                            boolean alive = selected.isLiving();
                            boolean affordable = gameMap.getSupply().spend(purchased.getCost());

                            if (alive && affordable) {
                                selected.setGun(purchased);
                                gameMap.addLog(">> " + selected.getColonistName() + " → " + purchased.getName() + " 장착");
                            }

                            shopMode = false;
                            renderer.setShopMode(false);
                        }
                    } else if (buildMode) {
                        // 건설 모드: 구조물 설치 또는 취소
                        switch (key) {
                            case BUILD_SPIKE:
                                if (gameMap.getSupply().spend(Spike.COST)) {
                                    int totalStructures = gameMap.getSpikes().size() + gameMap.getLandmines().size();
                                    int spikeCol = Barricade.COLUMN + STRUCTURE_SPACING * (totalStructures + 1);
                                    gameMap.addSpike(new Spike(spikeCol));
                                    gameMap.addLog(">> 가시덫 설치 (열 " + spikeCol + ")");
                                }
                                buildMode = false;
                                renderer.setBuildMode(false);
                                break;
                            case BUILD_LANDMINE:
                                if (gameMap.getSupply().spend(Landmine.COST)) {
                                    int totalStructures = gameMap.getSpikes().size() + gameMap.getLandmines().size();
                                    int mineCol = Barricade.COLUMN + STRUCTURE_SPACING * (totalStructures + 1);
                                    gameMap.addLandmine(new Landmine(mineCol));
                                    gameMap.addLog(">> 지뢰 설치 (열 " + mineCol + ")");
                                }
                                buildMode = false;
                                renderer.setBuildMode(false);
                                break;
                            case BUILD_AMMOBOX:
                                if (gameMap.getSupply().spend(AmmoBox.COST)) {
                                    int ammoCol = AMMOBOX_COL + gameMap.getAmmoBoxes().size() * 2;
                                    gameMap.addAmmoBox(new AmmoBox(ammoCol));
                                    gameMap.addLog(">> 탄약 상자 설치");
                                }
                                buildMode = false;
                                renderer.setBuildMode(false);
                                break;
                            case BUILD_UPGRADE:
                                Barricade barricade = gameMap.getBarricade();
                                if (barricade.canUpgrade() && gameMap.getSupply().spend(barricade.getUpgradeCost())) {
                                    barricade.upgrade();
                                    gameMap.addLog(">> 바리케이드 강화 Lv" + barricade.getLevel() + " (최대HP " + barricade.getMaxHp() + ")");
                                }
                                buildMode = false;
                                renderer.setBuildMode(false);
                                break;
                            case Util.KEY_QUIT:
                                buildMode = false;
                                renderer.setBuildMode(false);
                                break;
                        }
                    } else if (recruitMode) {
                        // 모집 모드: 유형 선택 또는 취소
                        ColonistType recruitType = null;
                        switch (key) {
                            case RECRUIT_GUNNER:
                                recruitType = ColonistType.GUNNER;
                                break;
                            case RECRUIT_SNIPER:
                                recruitType = ColonistType.SNIPER;
                                break;
                            case RECRUIT_ASSAULT:
                                recruitType = ColonistType.ASSAULT;
                                break;
                            case Util.KEY_QUIT:
                                recruitMode = false;
                                renderer.setRecruitMode(false);
                                break;
                        }

                        if (recruitType != null) {
                            if (gameMap.getSupply().spend(RECRUIT_COST)) {
                                recruitCount++;
                                String recruitName = "신병" + recruitCount;
                                int row = GameMap.HEIGHT / 2;
                                int col = 3 + gameMap.getColonists().size() * 4;
                                Colonist recruit = new Colonist(recruitType, colonistFactory.getSpec(recruitType), recruitName, gameMap.issueNextLabel(), new Position(row, col), gameMap);

                                // 유형별 기본 무기 배정
                                switch (recruitType) {
                                    case GUNNER:
                                        recruit.setGun(new Pistol());
                                        break;
                                    case SNIPER:
                                        recruit.setGun(new Rifle());
                                        break;
                                    case ASSAULT:
                                        recruit.setGun(new Shotgun());
                                        break;
                                }

                                gameMap.addColonist(recruit);
                                recruit.start();
                                gameMap.addLog(">> " + recruitName + " (" + colonistFactory.getSpec(recruitType).getDisplayName() + ") 합류!");
                            }

                            recruitMode = false;
                            renderer.setRecruitMode(false);
                        }
                    } else {
                        // 일반 명령 키
                        final int KEY_REPAIR = '1';
                        final int KEY_WEAPON = '2';
                        final int KEY_HEAL = '3';
                        final int KEY_BUILD = '4';
                        final int KEY_RECRUIT = '5';
                        final int KEY_SKIP_NIGHT = 'n';

                        // 명령 비용
                        final int REPAIR_COST = 10;
                        final int REPAIR_AMOUNT = 30;
                        final int HEAL_COST = 10;
                        final int HEAL_AMOUNT = 30;

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
                            case KEY_WEAPON:
                                // 낮에만 무기 상점 진입
                                if (!dayNightCycle.isNight()) {
                                    shopMode = true;
                                    renderer.setShopMode(true);
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
                            case KEY_BUILD:
                                // 낮에만 건설 모드 진입
                                if (!dayNightCycle.isNight()) {
                                    buildMode = true;
                                    renderer.setBuildMode(true);
                                }
                                break;
                            case KEY_RECRUIT:
                                // 낮에만 모집 모드 진입 (최대 인원 미만일 때만)
                                boolean canRecruit = !dayNightCycle.isNight() && gameMap.getColonists().size() < MAX_COLONISTS;
                                if (canRecruit) {
                                    recruitMode = true;
                                    renderer.setRecruitMode(true);
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
                gameMap.checkLandmines();
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
        for (Colonist colonist : gameMap.getColonists()) {
            if (colonist.isLiving()) {
                survivors++;
            }
        }

        System.out.println("=== 게임 통계 ===");
        System.out.println("생존 일수: " + dayNightCycle.getDay() + "일");
        System.out.println("생존자: " + survivors + "/" + gameMap.getColonists().size() + "명");
        System.out.println("처치한 적: " + gameMap.getEnemiesKilled() + "마리");

        // 종류별 처치 수 출력
        java.util.HashMap<EnemyType, Integer> killsByType = gameMap.getKillsByType();
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
