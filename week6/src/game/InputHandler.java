package game;

import unit.colonist.Colonist;
import unit.colonist.ColonistSpawner;
import unit.colonist.ColonistType;
import gun.Gun;
import gun.Pistol;
import gun.Shotgun;
import gun.Rifle;
import gun.Minigun;
import unit.enemy.Enemy;
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
    private final GameWorld gameWorld;

    /// <summary>
    /// 렌더러 (정착민 선택 전달용)
    /// </summary>
    private final Renderer renderer;

    /// <summary>
    /// 낮/밤 주기
    /// </summary>
    private final DayNightCycle dayNightCycle;

    /// <summary>
    /// 정착민 생성 스포너
    /// </summary>
    private final ColonistSpawner colonistSpawner;

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
    /// 승격 모드 여부
    /// </summary>
    private boolean promoteMode;

    /// <summary>
    /// 치트 모드 여부
    /// </summary>
    private boolean cheatMode;

    /// <summary>
    /// 배치 모드 여부 (커서로 구조물 설치 위치 선택 중)
    /// </summary>
    private boolean placementMode;

    /// <summary>
    /// 배치할 구조물 종류 (1=가시덫, 2=지뢰, 3=탄약상자)
    /// </summary>
    private int placementType;

    /// <summary>
    /// 배치 커서의 행 위치
    /// </summary>
    private int cursorRow;

    /// <summary>
    /// 배치 커서의 열 위치
    /// </summary>
    private int cursorCol;

    /// <summary>
    /// 입력 처리기 생성
    /// </summary>
    public InputHandler(GameWorld gameWorld, Renderer renderer, DayNightCycle dayNightCycle, ColonistSpawner colonistSpawner) {
        this.gameWorld = gameWorld;
        this.renderer = renderer;
        this.dayNightCycle = dayNightCycle;
        this.colonistSpawner = colonistSpawner;
        this.running = true;
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
    /// 승격 모드 여부 반환
    /// </summary>
    public boolean isPromoteMode() {
        return promoteMode;
    }

    /// <summary>
    /// 치트 모드 여부 반환
    /// </summary>
    public boolean isCheatMode() {
        return cheatMode;
    }

    /// <summary>
    /// 배치 모드 여부 반환
    /// </summary>
    public boolean isPlacementMode() {
        return placementMode;
    }

    /// <summary>
    /// 배치할 구조물 종류 반환 (1=가시덫, 2=지뢰, 3=탄약상자)
    /// </summary>
    public int getPlacementType() {
        return placementType;
    }

    /// <summary>
    /// 배치 커서 행 반환
    /// </summary>
    public int getCursorRow() {
        return cursorRow;
    }

    /// <summary>
    /// 배치 커서 열 반환
    /// </summary>
    public int getCursorCol() {
        return cursorCol;
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
        } else if (promoteMode) {
            handlePromoteMode(key);
        } else if (placementMode) {
            handlePlacementMode(key);
        } else if (buildMode) {
            handleBuildMode(key);
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
            Colonist selected = gameWorld.getColonists().get(renderer.getSelectedIndex());
            boolean alive = selected.isLiving();
            boolean affordable = gameWorld.getSupply().spend(purchased.getCost());

            if (alive && affordable) {
                selected.setGun(purchased);
                gameWorld.addLog(">> " + selected.getColonistName() + " → " + purchased.getName() + " 장착");
            }

            shopMode = false;
        }
    }

    /// <summary>
    /// 건설 모드: 구조물 종류 선택 → 배치 모드 진입, 바리케이드 강화는 즉시 처리
    /// </summary>
    private void handleBuildMode(int key) {
        final int KEY_SPIKE = '1';
        final int KEY_LANDMINE = '2';
        final int KEY_AMMOBOX = '3';
        final int KEY_UPGRADE = '4';

        // 구조물별 비용 (배치 확정 시 차감)
        final int SPIKE_COST = 20;
        final int LANDMINE_COST = 25;
        final int AMMOBOX_COST = 20;

        switch (key) {
            case KEY_SPIKE:
                if (gameWorld.getSupply().getAmount() >= SPIKE_COST) {
                    enterPlacementMode(1);
                }
                buildMode = false;
                break;
            case KEY_LANDMINE:
                if (gameWorld.getSupply().getAmount() >= LANDMINE_COST) {
                    enterPlacementMode(2);
                }
                buildMode = false;
                break;
            case KEY_AMMOBOX:
                if (gameWorld.getSupply().getAmount() >= AMMOBOX_COST) {
                    enterPlacementMode(3);
                }
                buildMode = false;
                break;
            case KEY_UPGRADE:
                Barricade barricade = gameWorld.getBarricade();
                if (barricade.canUpgrade() && gameWorld.getSupply().spend(barricade.getUpgradeCost())) {
                    barricade.upgrade();
                    gameWorld.addLog(">> 바리케이드 강화 Lv" + barricade.getLevel() + " (최대HP " + barricade.getMaxHp() + ")");
                }
                buildMode = false;
                break;
            case KEY_QUIT:
                buildMode = false;
                break;
        }
    }

    /// <summary>
    /// 배치 모드 진입 (커서 초기 위치 설정)
    /// 함정은 전장(바리케이드 오른쪽), 탄약상자는 안전지대(바리케이드 왼쪽)에서 시작
    /// </summary>
    private void enterPlacementMode(int type) {
        placementMode = true;
        placementType = type;
        cursorRow = GameWorld.HEIGHT / 2;

        // 탄약 상자 기본 커서 열 (안전지대 중앙)
        final int AMMOBOX_DEFAULT_COL = 7;
        // 함정 기본 커서 열 (바리케이드 오른쪽 전장)
        final int TRAP_DEFAULT_COL = 20;

        // 탄약 상자는 안전지대, 함정은 전장에서 시작
        final int TYPE_AMMOBOX = 3;
        if (type == TYPE_AMMOBOX) {
            cursorCol = AMMOBOX_DEFAULT_COL;
        } else {
            cursorCol = TRAP_DEFAULT_COL;
        }
    }

    /// <summary>
    /// 배치 모드: 화살표로 커서 이동, Enter로 설치 확정, q로 취소
    /// 커서는 유효 영역 내에서만 이동 가능
    /// </summary>
    private void handlePlacementMode(int key) {
        // 전장 시작 열 (바리케이드 다음 칸부터)
        final int BATTLEFIELD_START = Barricade.COLUMN + 2;
        // 안전지대 끝 열 (바리케이드 직전)
        final int SAFE_ZONE_END = Barricade.COLUMN - 1;
        // 탄약 상자 타입 번호
        final int TYPE_AMMOBOX = 3;

        switch (key) {
            case KEY_UP:
                if (cursorRow > 0) {
                    cursorRow--;
                }
                break;
            case KEY_DOWN:
                if (cursorRow < GameWorld.HEIGHT - 1) {
                    cursorRow++;
                }
                break;
            case KEY_LEFT:
                if (placementType == TYPE_AMMOBOX) {
                    // 탄약 상자: 안전지대 내에서만 이동
                    if (cursorCol > 0) {
                        cursorCol--;
                    }
                } else {
                    // 함정: 전장 내에서만 이동
                    if (cursorCol > BATTLEFIELD_START) {
                        cursorCol--;
                    }
                }
                break;
            case KEY_RIGHT:
                if (placementType == TYPE_AMMOBOX) {
                    // 탄약 상자: 안전지대 끝까지
                    if (cursorCol < SAFE_ZONE_END) {
                        cursorCol++;
                    }
                } else {
                    // 함정: 맵 오른쪽 끝까지
                    if (cursorCol < GameWorld.WIDTH - 1) {
                        cursorCol++;
                    }
                }
                break;
            case KEY_ENTER:
                confirmPlacement();
                break;
            case KEY_QUIT:
                placementMode = false;
                break;
        }
    }

    /// <summary>
    /// 커서 위치에 구조물 설치 확정 (비용 차감 + 구조물 생성)
    /// </summary>
    private void confirmPlacement() {
        final int TYPE_SPIKE = 1;
        final int TYPE_LANDMINE = 2;
        final int TYPE_AMMOBOX = 3;

        switch (placementType) {
            case TYPE_SPIKE: {
                Spike spike = new Spike(cursorRow, cursorCol);
                if (gameWorld.getSupply().spend(spike.getCost())) {
                    gameWorld.addSpike(spike);
                    gameWorld.addLog(">> 가시덫 설치 (" + cursorRow + "," + cursorCol + ")");
                }
                break;
            }
            case TYPE_LANDMINE: {
                Landmine landmine = new Landmine(cursorRow, cursorCol);
                if (gameWorld.getSupply().spend(landmine.getCost())) {
                    gameWorld.addLandmine(landmine);
                    gameWorld.addLog(">> 지뢰 설치 (" + cursorRow + "," + cursorCol + ")");
                }
                break;
            }
            case TYPE_AMMOBOX: {
                AmmoBox ammoBox = new AmmoBox(cursorRow, cursorCol);
                if (gameWorld.getSupply().spend(ammoBox.getCost())) {
                    gameWorld.addAmmoBox(ammoBox);
                    gameWorld.addLog(">> 탄약 상자 설치 (" + cursorRow + "," + cursorCol + ")");
                }
                break;
            }
        }
        placementMode = false;
    }

    /// <summary>
    /// BASIC 정착민 모집 (유형 선택 없이 즉시 모집)
    /// </summary>
    private void recruit() {
        // 모집 비용 (보급품)
        final int RECRUIT_COST = 40;
        if (gameWorld.getSupply().spend(RECRUIT_COST)) {
            // 왼쪽 바깥에서 등장하여 안전지대로 걸어 들어옴
            int row = GameWorld.HEIGHT / 2;
            int col = 0;
            Colonist recruit = colonistSpawner.spawn(gameWorld, new Position(row, col));
            gameWorld.addLog(">> " + recruit.getColonistName() + " 합류!");
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
        final int KEY_PROMOTE = '6';
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
                if (!dayNightCycle.isNight() && gameWorld.getSupply().spend(REPAIR_COST)) {

                    // 수리량 (HP)
                    final int REPAIR_AMOUNT = 30;
                    gameWorld.getBarricade().repair(REPAIR_AMOUNT);
                    gameWorld.addLog(">> 바리케이드 수리 (+" + REPAIR_AMOUNT + ")");
                }
                break;
            case KEY_WEAPON:
                // 낮에만 무기 상점 진입
                if (!dayNightCycle.isNight()) {
                    shopMode = true;
                }
                break;
            case KEY_HEAL:
                if (!dayNightCycle.isNight() && gameWorld.getSupply().spend(HEAL_COST)) {
                    Colonist selected = gameWorld.getColonists().get(renderer.getSelectedIndex());
                    if (selected.isLiving()) {

                        // 치료량 (HP)
                        final int HEAL_AMOUNT = 30;
                        selected.heal(HEAL_AMOUNT);
                        gameWorld.addLog(">> " + selected.getColonistName() + " 치료 (+" + HEAL_AMOUNT + ")");
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
                // 낮에만 모집 (최대 인원 미만일 때만)
                boolean canRecruit = !dayNightCycle.isNight() && gameWorld.getColonists().size() < MAX_COLONISTS;
                if (canRecruit) {
                    recruit();
                }
                break;
            case KEY_PROMOTE:
                // 낮에만 승격 모드 진입 (선택된 정착민이 BASIC일 때만)
                if (!dayNightCycle.isNight()) {
                    Colonist target = gameWorld.getColonists().get(renderer.getSelectedIndex());
                    boolean canPromote = target.isLiving() && target.getType() == ColonistType.BASIC;
                    if (canPromote) {
                        promoteMode = true;
                    }
                }
                break;
            case KEY_CHEAT:
                cheatMode = true;
                break;
        }
    }

    /// <summary>
    /// 승격 모드: 유형 선택 또는 취소
    /// </summary>
    private void handlePromoteMode(int key) {
        final int KEY_GUNNER = '1';
        final int KEY_SNIPER = '2';
        final int KEY_ASSAULT = '3';

        ColonistType promoteType = null;
        switch (key) {
            case KEY_GUNNER:
                promoteType = ColonistType.GUNNER;
                break;
            case KEY_SNIPER:
                promoteType = ColonistType.SNIPER;
                break;
            case KEY_ASSAULT:
                promoteType = ColonistType.ASSAULT;
                break;
            case KEY_QUIT:
                promoteMode = false;
                break;
        }

        if (promoteType != null) {
            // 승격 비용 (보급품)
            final int PROMOTE_COST = 30;
            if (gameWorld.getSupply().spend(PROMOTE_COST)) {
                Colonist target = gameWorld.getColonists().get(renderer.getSelectedIndex());
                Colonist promoted = colonistSpawner.promote(target, promoteType, gameWorld);
                gameWorld.addLog(">> " + promoted.getColonistName() + " → " + promoted.getSpec().getDisplayName() + " 승격!");
            }
            promoteMode = false;
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
        final int KEY_ENEMY_BUFF = '7';
        final int KEY_ENEMY_DEBUFF = '8';
        final int KEY_KILL_COLONIST = '9';

        switch (key) {
            case KEY_SUPPLY:
                gameWorld.getSupply().add(999);
                gameWorld.addLog(">> [치트] 보급품 +999");
                cheatMode = false;
                break;
            case KEY_HEAL_ALL:
                for (Colonist colonist : gameWorld.getColonists()) {
                    if (colonist.isLiving()) {
                        colonist.heal(9999);
                    }
                }
                gameWorld.getBarricade().repair(9999);
                gameWorld.addLog(">> [치트] 전원 회복 완료");
                cheatMode = false;
                break;
            case KEY_KILL_ALL:
                for (Enemy enemy : gameWorld.getEnemies()) {
                    if (enemy.isLiving()) {
                        enemy.takeDamage(99999);
                    }
                }
                dayNightCycle.clearPendingSpawns();
                gameWorld.addLog(">> [치트] 적 전멸");
                cheatMode = false;
                break;
            case KEY_INVINCIBLE:
                boolean nowInvincible = !gameWorld.isInvincible();
                gameWorld.setInvincible(nowInvincible);
                gameWorld.getBarricade().setInvincible(nowInvincible);
                String status = nowInvincible ? "ON" : "OFF";
                gameWorld.addLog(">> [치트] 무적 모드 " + status);
                cheatMode = false;
                break;
            case KEY_MAX_BARRICADE:
                Barricade barricade = gameWorld.getBarricade();
                while (barricade.canUpgrade()) {
                    barricade.upgrade();
                }
                barricade.repair(9999);
                gameWorld.addLog(">> [치트] 바리케이드 MAX (Lv" + barricade.getLevel() + ")");
                cheatMode = false;
                break;
            case KEY_ALL_MINIGUN:
                for (Colonist colonist : gameWorld.getColonists()) {
                    if (colonist.isLiving()) {
                        colonist.setGun(new Minigun());
                    }
                }
                gameWorld.addLog(">> [치트] 전원 미니건 장착");
                cheatMode = false;
                break;
            case KEY_ENEMY_BUFF:
                gameWorld.adjustEnemyBuff(1);
                int buffLv = gameWorld.getEnemyBuffLevel();
                gameWorld.addLog(">> [치트] 적 강화 Lv" + buffLv
                        + " (x" + String.format("%.1f", gameWorld.getEnemyBuffMultiplier()) + ")");
                cheatMode = false;
                break;
            case KEY_ENEMY_DEBUFF:
                gameWorld.adjustEnemyBuff(-1);
                int debuffLv = gameWorld.getEnemyBuffLevel();
                gameWorld.addLog(">> [치트] 적 약화 Lv" + debuffLv
                        + " (x" + String.format("%.1f", gameWorld.getEnemyBuffMultiplier()) + ")");
                cheatMode = false;
                break;
            case KEY_KILL_COLONIST:
                // 살아있는 정착민 중 랜덤 1명 즉사
                java.util.ArrayList<Colonist> alive = new java.util.ArrayList<>();
                for (Colonist colonist : gameWorld.getColonists()) {
                    if (colonist.isLiving()) {
                        alive.add(colonist);
                    }
                }
                if (!alive.isEmpty()) {
                    Colonist victim = alive.get(Util.rand(alive.size()));
                    victim.takeDamage(99999);
                    gameWorld.addLog(">> [치트] " + victim.getColonistName() + " 즉사!");
                }
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
    /// 한글 자모(ㅂ 등)는 대응하는 영문 키로 변환
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
        // 멀티바이트 UTF-8 시작 바이트의 최솟값 (2바이트 시작: 110xxxxx)
        final int UTF8_MULTIBYTE_START = 0xC0;

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

            // 멀티바이트 UTF-8 처리 (한글 IME 활성 시 ㅂ→q 등 변환)
            if (ch >= UTF8_MULTIBYTE_START) {
                ch = decodeKoreanKey(ch);
                if (ch == INVALID_INPUT) {
                    return INVALID_INPUT;
                }
                // 매핑된 영문 키를 아래 로직에서 계속 처리
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

    /// <summary>
    /// 멀티바이트 UTF-8 문자를 읽어 한글 자모이면 대응하는 영문 키로 변환
    /// 한글 자모가 아니면 INVALID_INPUT 반환
    /// </summary>
    private static int decodeKoreanKey(int firstByte) {
        // 주의: System.in.read()는 checked exception이라 try-catch 필수 (컴파일러 요구)
        try {
            // UTF-8 후속 바이트 수 결정 (첫 바이트의 상위 비트 패턴으로 판단)
            // 4바이트 시작 (11110xxx)
            final int UTF8_4BYTE = 0xF0;
            // 3바이트 시작 (1110xxxx) — 한글 자모가 여기 해당
            final int UTF8_3BYTE = 0xE0;

            int bytesRemaining;
            int codePoint;
            if (firstByte >= UTF8_4BYTE) {
                bytesRemaining = 3;
                codePoint = firstByte & 0x07;
            } else if (firstByte >= UTF8_3BYTE) {
                bytesRemaining = 2;
                codePoint = firstByte & 0x0F;
            } else {
                bytesRemaining = 1;
                codePoint = firstByte & 0x1F;
            }

            // 후속 바이트 읽기 (각 바이트의 하위 6비트를 코드포인트에 결합)
            for (int i = 0; i < bytesRemaining; i++) {
                int next = System.in.read();
                codePoint = (codePoint << 6) | (next & 0x3F);
            }

            return mapKoreanToEnglish(codePoint);
        } catch (Exception e) {
            // IOException (컴파일러 요구사항)
            return INVALID_INPUT;
        }
    }

    /// <summary>
    /// 한글 자모 유니코드를 2벌식 자판 기준 영문 키로 변환
    /// 한글 자모가 아니면 INVALID_INPUT 반환
    /// </summary>
    private static int mapKoreanToEnglish(int codePoint) {
        switch (codePoint) {
            // 기본 자음 (ㄱ~ㅎ)
            case 0x3131: return 'r';  // ㄱ
            case 0x3134: return 's';  // ㄴ
            case 0x3137: return 'e';  // ㄷ
            case 0x3139: return 'f';  // ㄹ
            case 0x3141: return 'a';  // ㅁ
            case 0x3142: return 'q';  // ㅂ
            case 0x3145: return 't';  // ㅅ
            case 0x3147: return 'd';  // ㅇ
            case 0x3148: return 'w';  // ㅈ
            case 0x314A: return 'c';  // ㅊ
            case 0x314B: return 'z';  // ㅋ
            case 0x314C: return 'x';  // ㅌ
            case 0x314D: return 'v';  // ㅍ
            case 0x314E: return 'g';  // ㅎ

            // 쌍자음 (Shift 입력)
            case 0x3132: return 'R';  // ㄲ
            case 0x3138: return 'E';  // ㄸ
            case 0x3143: return 'Q';  // ㅃ
            case 0x3146: return 'T';  // ㅆ
            case 0x3149: return 'W';  // ㅉ

            // 모음 (ㅏ~ㅣ)
            case 0x314F: return 'k';  // ㅏ
            case 0x3150: return 'o';  // ㅐ
            case 0x3151: return 'i';  // ㅑ
            case 0x3152: return 'O';  // ㅒ
            case 0x3153: return 'j';  // ㅓ
            case 0x3154: return 'p';  // ㅔ
            case 0x3155: return 'u';  // ㅕ
            case 0x3156: return 'P';  // ㅖ
            case 0x3157: return 'h';  // ㅗ
            case 0x315B: return 'y';  // ㅛ
            case 0x315C: return 'n';  // ㅜ
            case 0x3160: return 'b';  // ㅠ
            case 0x3161: return 'm';  // ㅡ
            case 0x3163: return 'l';  // ㅣ

            default: return INVALID_INPUT;
        }
    }
}
