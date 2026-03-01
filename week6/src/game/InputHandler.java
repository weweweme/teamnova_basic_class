package game;

import entity.colonist.Colonist;
import entity.colonist.ColonistFactory;
import entity.colonist.ColonistType;
import gun.Gun;
import gun.Pistol;
import gun.Shotgun;
import gun.Rifle;
import gun.Minigun;
import entity.enemy.Enemy;
import structure.AmmoBox;
import structure.Barricade;
import structure.Landmine;
import structure.Spike;

/// <summary>
/// 사용자 키 입력을 처리하는 클래스
/// 메뉴 모드 관리 + 키 입력에 따른 게임 명령 실행
/// </summary>
public class InputHandler {

    // ========== 키 상수 ==========
    // readKey()는 InputHandler 생성 전(난이도 선택 등)에도 호출되므로 static 메서드
    // static 메서드에서 사용하는 상수도 static이어야 함

    /// <summary>
    /// 잘못된 입력을 나타내는 값
    /// </summary>
    private static final int INVALID_INPUT = -1;

    /// <summary>
    /// 화살표 위 키
    /// </summary>
    private static final int KEY_UP = 1000;

    /// <summary>
    /// 화살표 아래 키
    /// </summary>
    private static final int KEY_DOWN = 1001;

    /// <summary>
    /// 화살표 왼쪽 키
    /// </summary>
    private static final int KEY_LEFT = 1002;

    /// <summary>
    /// 화살표 오른쪽 키
    /// </summary>
    private static final int KEY_RIGHT = 1003;

    /// <summary>
    /// Enter 키
    /// </summary>
    private static final int KEY_ENTER = 1004;

    /// <summary>
    /// 종료/취소 키 (q)
    /// </summary>
    private static final int KEY_QUIT = 1005;

    // ========== 의존 객체 ==========

    /// <summary>
    /// 게임 맵
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 렌더러 (정착민 선택 전달용)
    /// </summary>
    private final Renderer renderer;

    /// <summary>
    /// 낮/밤 주기
    /// </summary>
    private final DayNightCycle dayNightCycle;

    /// <summary>
    /// 정착민 생성 팩토리
    /// </summary>
    private final ColonistFactory colonistFactory;

    /// <summary>
    /// 게임 계속 여부
    /// </summary>
    private boolean running;

    /// <summary>
    /// 무기 상점 모드 여부
    /// </summary>
    private boolean shopMode;

    /// <summary>
    /// 건설 모드 여부
    /// </summary>
    private boolean buildMode;

    /// <summary>
    /// 모집 모드 여부
    /// </summary>
    private boolean recruitMode;

    /// <summary>
    /// 치트 모드 여부
    /// </summary>
    private boolean cheatMode;

    /// <summary>
    /// 모집 이름 카운터
    /// </summary>
    private int recruitCount;

    /// <summary>
    /// 입력 처리기 생성
    /// </summary>
    public InputHandler(GameMap gameMap, Renderer renderer, DayNightCycle dayNightCycle, ColonistFactory colonistFactory) {
        this.gameMap = gameMap;
        this.renderer = renderer;
        this.dayNightCycle = dayNightCycle;
        this.colonistFactory = colonistFactory;
        this.running = true;
        this.recruitCount = 0;
    }

    /// <summary>
    /// 게임 계속 여부 반환
    /// </summary>
    public boolean isRunning() {
        return running;
    }

    /// <summary>
    /// 무기 상점 모드 여부 반환
    /// </summary>
    public boolean isShopMode() {
        return shopMode;
    }

    /// <summary>
    /// 건설 모드 여부 반환
    /// </summary>
    public boolean isBuildMode() {
        return buildMode;
    }

    /// <summary>
    /// 모집 모드 여부 반환
    /// </summary>
    public boolean isRecruitMode() {
        return recruitMode;
    }

    /// <summary>
    /// 치트 모드 여부 반환
    /// </summary>
    public boolean isCheatMode() {
        return cheatMode;
    }

    /// <summary>
    /// 키 입력에 따라 게임 명령 실행
    /// </summary>
    public void handleInput(int key) {
        if (renderer.isVictory() || renderer.isGameOver()) {
            handleEndScreen(key);
        } else if (cheatMode) {
            handleCheatMode(key);
        } else if (shopMode) {
            handleShopMode(key);
        } else if (buildMode) {
            handleBuildMode(key);
        } else if (recruitMode) {
            handleRecruitMode(key);
        } else {
            handleDayCommand(key);
        }
    }

    /// <summary>
    /// 승리 또는 게임오버 화면: q키만 허용
    /// </summary>
    private void handleEndScreen(int key) {
        if (key == KEY_QUIT) {
            running = false;
        }
    }

    /// <summary>
    /// 무기 상점 모드: 무기 선택 또는 취소
    /// </summary>
    private void handleShopMode(int key) {
        final int KEY_PISTOL = '1';
        final int KEY_SHOTGUN = '2';
        final int KEY_RIFLE = '3';
        final int KEY_MINIGUN = '4';

        Gun purchased = null;
        switch (key) {
            case KEY_PISTOL:
                purchased = new Pistol();
                break;
            case KEY_SHOTGUN:
                purchased = new Shotgun();
                break;
            case KEY_RIFLE:
                purchased = new Rifle();
                break;
            case KEY_MINIGUN:
                purchased = new Minigun();
                break;
            case KEY_QUIT:
                shopMode = false;
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
        }
    }

    /// <summary>
    /// 건설 모드: 구조물 설치 또는 취소
    /// </summary>
    private void handleBuildMode(int key) {
        final int KEY_SPIKE = '1';
        final int KEY_LANDMINE = '2';
        final int KEY_AMMOBOX = '3';
        final int KEY_UPGRADE = '4';

        // 구조물 간격 (바리케이드 기준 오른쪽으로)
        final int STRUCTURE_SPACING = 5;
        switch (key) {
            case KEY_SPIKE: {
                int totalStructures = gameMap.getSpikes().size() + gameMap.getLandmines().size();
                int spikeCol = Barricade.COLUMN + STRUCTURE_SPACING * (totalStructures + 1);
                Spike spike = new Spike(spikeCol);
                if (gameMap.getSupply().spend(spike.getCost())) {
                    gameMap.addSpike(spike);
                    gameMap.addLog(">> 가시덫 설치 (열 " + spikeCol + ")");
                }
                buildMode = false;
                break;
            }
            case KEY_LANDMINE: {
                int totalStructures = gameMap.getSpikes().size() + gameMap.getLandmines().size();
                int mineCol = Barricade.COLUMN + STRUCTURE_SPACING * (totalStructures + 1);
                Landmine landmine = new Landmine(mineCol);
                if (gameMap.getSupply().spend(landmine.getCost())) {
                    gameMap.addLandmine(landmine);
                    gameMap.addLog(">> 지뢰 설치 (열 " + mineCol + ")");
                }
                buildMode = false;
                break;
            }
            case KEY_AMMOBOX: {
                // 탄약 상자 설치 열 (바리케이드 왼쪽 안전지대)
                final int AMMOBOX_COL = 1;
                int ammoCol = AMMOBOX_COL + gameMap.getAmmoBoxes().size() * 2;
                AmmoBox ammoBox = new AmmoBox(ammoCol);
                if (gameMap.getSupply().spend(ammoBox.getCost())) {
                    gameMap.addAmmoBox(ammoBox);
                    gameMap.addLog(">> 탄약 상자 설치");
                }
                buildMode = false;
                break;
            }
            case KEY_UPGRADE:
                Barricade barricade = gameMap.getBarricade();
                if (barricade.canUpgrade() && gameMap.getSupply().spend(barricade.getUpgradeCost())) {
                    barricade.upgrade();
                    gameMap.addLog(">> 바리케이드 강화 Lv" + barricade.getLevel() + " (최대HP " + barricade.getMaxHp() + ")");
                }
                buildMode = false;
                break;
            case KEY_QUIT:
                buildMode = false;
                break;
        }
    }

    /// <summary>
    /// 모집 모드: 유형 선택 또는 취소
    /// </summary>
    private void handleRecruitMode(int key) {
        final int KEY_GUNNER = '1';
        final int KEY_SNIPER = '2';
        final int KEY_ASSAULT = '3';

        ColonistType recruitType = null;
        switch (key) {
            case KEY_GUNNER:
                recruitType = ColonistType.GUNNER;
                break;
            case KEY_SNIPER:
                recruitType = ColonistType.SNIPER;
                break;
            case KEY_ASSAULT:
                recruitType = ColonistType.ASSAULT;
                break;
            case KEY_QUIT:
                recruitMode = false;
                break;
        }

        if (recruitType != null) {

            // 모집 비용 (보급품)
            final int RECRUIT_COST = 40;
            if (gameMap.getSupply().spend(RECRUIT_COST)) {
                recruitCount++;
                String recruitName = "신병" + recruitCount;
                // 왼쪽 바깥에서 등장하여 안전지대로 걸어 들어옴
                int row = GameMap.HEIGHT / 2;
                int col = 0;
                Colonist recruit = new Colonist(recruitType, colonistFactory.getSpec(recruitType), recruitName, gameMap.issueNextLabel(), new Position(row, col), gameMap);

                // 모든 신병 피스톨로 시작
                recruit.setGun(new Pistol());

                gameMap.addColonist(recruit);
                recruit.start();
                gameMap.addLog(">> " + recruitName + " (" + colonistFactory.getSpec(recruitType).getDisplayName() + ") 합류!");
            }

            recruitMode = false;
        }
    }

    /// <summary>
    /// 일반 명령: 수리, 무기, 치료, 건설, 모집, 화살표 키
    /// </summary>
    private void handleDayCommand(int key) {
        final int KEY_REPAIR = '1';
        final int KEY_WEAPON = '2';
        final int KEY_HEAL = '3';
        final int KEY_BUILD = '4';
        final int KEY_RECRUIT = '5';
        final int KEY_SKIP_NIGHT = 'n';
        final int KEY_CHEAT = '0';

        // 수리 비용 (보급품)
        final int REPAIR_COST = 10;

        // 치료 비용 (보급품)
        final int HEAL_COST = 10;

        // 최대 정착민 수
        final int MAX_COLONISTS = 5;
        switch (key) {
            case KEY_QUIT:
                running = false;
                break;
            case KEY_UP:
                renderer.selectPrevious();
                break;
            case KEY_DOWN:
                renderer.selectNext();
                break;
            case KEY_SKIP_NIGHT:
                dayNightCycle.skipToNight();
                break;
            case KEY_REPAIR:
                // 낮에만 사용 가능
                if (!dayNightCycle.isNight() && gameMap.getSupply().spend(REPAIR_COST)) {

                    // 수리량 (HP)
                    final int REPAIR_AMOUNT = 30;
                    gameMap.getBarricade().repair(REPAIR_AMOUNT);
                    gameMap.addLog(">> 바리케이드 수리 (+" + REPAIR_AMOUNT + ")");
                }
                break;
            case KEY_WEAPON:
                // 낮에만 무기 상점 진입
                if (!dayNightCycle.isNight()) {
                    shopMode = true;
                }
                break;
            case KEY_HEAL:
                if (!dayNightCycle.isNight() && gameMap.getSupply().spend(HEAL_COST)) {
                    Colonist selected = gameMap.getColonists().get(renderer.getSelectedIndex());
                    if (selected.isLiving()) {

                        // 치료량 (HP)
                        final int HEAL_AMOUNT = 30;
                        selected.heal(HEAL_AMOUNT);
                        gameMap.addLog(">> " + selected.getColonistName() + " 치료 (+" + HEAL_AMOUNT + ")");
                    }
                }
                break;
            case KEY_BUILD:
                // 낮에만 건설 모드 진입
                if (!dayNightCycle.isNight()) {
                    buildMode = true;
                }
                break;
            case KEY_RECRUIT:
                // 낮에만 모집 모드 진입 (최대 인원 미만일 때만)
                boolean canRecruit = !dayNightCycle.isNight() && gameMap.getColonists().size() < MAX_COLONISTS;
                if (canRecruit) {
                    recruitMode = true;
                }
                break;
            case KEY_CHEAT:
                cheatMode = true;
                break;
        }
    }

    /// <summary>
    /// 치트 모드: 치트 선택 또는 취소
    /// </summary>
    private void handleCheatMode(int key) {
        final int KEY_SUPPLY = '1';
        final int KEY_HEAL_ALL = '2';
        final int KEY_KILL_ALL = '3';
        final int KEY_INVINCIBLE = '4';
        final int KEY_MAX_BARRICADE = '5';
        final int KEY_ALL_MINIGUN = '6';

        switch (key) {
            case KEY_SUPPLY:
                gameMap.getSupply().add(999);
                gameMap.addLog(">> [치트] 보급품 +999");
                cheatMode = false;
                break;
            case KEY_HEAL_ALL:
                for (Colonist colonist : gameMap.getColonists()) {
                    if (colonist.isLiving()) {
                        colonist.heal(9999);
                    }
                }
                gameMap.getBarricade().repair(9999);
                gameMap.addLog(">> [치트] 전원 회복 완료");
                cheatMode = false;
                break;
            case KEY_KILL_ALL:
                for (Enemy enemy : gameMap.getEnemies()) {
                    if (enemy.isLiving()) {
                        enemy.takeDamage(99999);
                    }
                }
                dayNightCycle.clearPendingSpawns();
                gameMap.addLog(">> [치트] 적 전멸");
                cheatMode = false;
                break;
            case KEY_INVINCIBLE:
                boolean nowInvincible = !gameMap.isInvincible();
                gameMap.setInvincible(nowInvincible);
                gameMap.getBarricade().setInvincible(nowInvincible);
                String status = nowInvincible ? "ON" : "OFF";
                gameMap.addLog(">> [치트] 무적 모드 " + status);
                cheatMode = false;
                break;
            case KEY_MAX_BARRICADE:
                Barricade barricade = gameMap.getBarricade();
                while (barricade.canUpgrade()) {
                    barricade.upgrade();
                }
                barricade.repair(9999);
                gameMap.addLog(">> [치트] 바리케이드 MAX (Lv" + barricade.getLevel() + ")");
                cheatMode = false;
                break;
            case KEY_ALL_MINIGUN:
                for (Colonist colonist : gameMap.getColonists()) {
                    if (colonist.isLiving()) {
                        colonist.setGun(new Minigun());
                    }
                }
                gameMap.addLog(">> [치트] 전원 미니건 장착");
                cheatMode = false;
                break;
            case KEY_QUIT:
                cheatMode = false;
                break;
        }
    }

    // ========== 키 입력 읽기 ==========

    /// <summary>
    /// 키 하나를 읽어서 반환
    /// 화살표 키는 KEY_UP/DOWN/LEFT/RIGHT 상수로 반환
    /// Enter는 KEY_ENTER, q는 KEY_QUIT 반환
    /// 그 외 키는 문자 코드 그대로 반환
    /// </summary>
    public static int readKey() {
        // ESC 키의 아스키 코드 (화살표 키가 이 코드로 시작됨)
        final int ESC_KEY = 27;
        // Enter 키의 아스키 코드 (운영체제마다 다른 코드를 보냄)
        final int ENTER_UNIX = 10;
        final int ENTER_WINDOWS = 13;
        // 화살표 키 후속 바이트 대기 시간 (밀리초)
        final int ARROW_KEY_DELAY = 10;

        // 주의: System.in.read()와 Thread.sleep()은 checked exception이라 try-catch 필수 (컴파일러 요구)
        try {
            int ch = System.in.read();

            // ESC (화살표 키의 시작 바이트)
            if (ch == ESC_KEY) {
                // 화살표 키는 ESC + [ + 방향 문자가 연속으로 전송됨
                // 잠깐 대기하여 후속 바이트가 도착하도록 함
                Thread.sleep(ARROW_KEY_DELAY);

                if (System.in.available() > 0) {
                    int next = System.in.read();
                    if (next == '[' && System.in.available() > 0) {
                        int arrow = System.in.read();
                        switch (arrow) {
                            case 'A':
                                return KEY_UP;
                            case 'B':
                                return KEY_DOWN;
                            case 'C':
                                return KEY_RIGHT;
                            case 'D':
                                return KEY_LEFT;
                        }
                    }
                }
                // ESC 단독 또는 인식할 수 없는 키 조합
                return INVALID_INPUT;
            }

            // Enter 키 (운영체제에 따라 다른 코드가 올 수 있음)
            if (ch == ENTER_UNIX || ch == ENTER_WINDOWS) {
                return KEY_ENTER;
            }

            // q 또는 Q → 종료/취소
            if (ch == 'q' || ch == 'Q') {
                return KEY_QUIT;
            }

            // 그 외 → 문자 코드 그대로 반환
            return ch;
        } catch (Exception e) {
            // IOException, InterruptedException 등 (컴파일러 요구사항)
            return INVALID_INPUT;
        }
    }
}
