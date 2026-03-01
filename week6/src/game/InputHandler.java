package game;

import entity.colonist.Colonist;
import entity.colonist.ColonistFactory;
import entity.colonist.ColonistType;
import gun.Gun;
import gun.Pistol;
import gun.Shotgun;
import gun.Rifle;
import gun.Minigun;
import structure.AmmoBox;
import structure.Barricade;
import structure.Landmine;
import structure.Spike;

/// <summary>
/// 사용자 키 입력을 처리하는 클래스
/// 메뉴 모드 관리 + 키 입력에 따른 게임 명령 실행
/// </summary>
public class InputHandler {

    /// <summary>
    /// 수리 비용 (보급품)
    /// </summary>
    private final int REPAIR_COST = 10;

    /// <summary>
    /// 수리량 (HP)
    /// </summary>
    private final int REPAIR_AMOUNT = 30;

    /// <summary>
    /// 치료 비용 (보급품)
    /// </summary>
    private final int HEAL_COST = 10;

    /// <summary>
    /// 치료량 (HP)
    /// </summary>
    private final int HEAL_AMOUNT = 30;

    /// <summary>
    /// 모집 비용 (보급품)
    /// </summary>
    private final int RECRUIT_COST = 40;

    /// <summary>
    /// 최대 정착민 수
    /// </summary>
    private final int MAX_COLONISTS = 5;

    /// <summary>
    /// 구조물 간격 (바리케이드 기준 오른쪽으로)
    /// </summary>
    private final int STRUCTURE_SPACING = 5;

    /// <summary>
    /// 탄약 상자 설치 열 (바리케이드 왼쪽 안전지대)
    /// </summary>
    private final int AMMOBOX_COL = 1;

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
    /// 키 입력에 따라 게임 명령 실행
    /// </summary>
    public void handleInput(int key) {
        if (renderer.isVictory() || renderer.isGameOver()) {
            handleEndScreen(key);
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
        if (key == Util.KEY_QUIT) {
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
            case Util.KEY_QUIT:
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

        switch (key) {
            case KEY_SPIKE:
                if (gameMap.getSupply().spend(Spike.COST)) {
                    int totalStructures = gameMap.getSpikes().size() + gameMap.getLandmines().size();
                    int spikeCol = Barricade.COLUMN + STRUCTURE_SPACING * (totalStructures + 1);
                    gameMap.addSpike(new Spike(spikeCol));
                    gameMap.addLog(">> 가시덫 설치 (열 " + spikeCol + ")");
                }
                buildMode = false;
                break;
            case KEY_LANDMINE:
                if (gameMap.getSupply().spend(Landmine.COST)) {
                    int totalStructures = gameMap.getSpikes().size() + gameMap.getLandmines().size();
                    int mineCol = Barricade.COLUMN + STRUCTURE_SPACING * (totalStructures + 1);
                    gameMap.addLandmine(new Landmine(mineCol));
                    gameMap.addLog(">> 지뢰 설치 (열 " + mineCol + ")");
                }
                buildMode = false;
                break;
            case KEY_AMMOBOX:
                if (gameMap.getSupply().spend(AmmoBox.COST)) {
                    int ammoCol = AMMOBOX_COL + gameMap.getAmmoBoxes().size() * 2;
                    gameMap.addAmmoBox(new AmmoBox(ammoCol));
                    gameMap.addLog(">> 탄약 상자 설치");
                }
                buildMode = false;
                break;
            case KEY_UPGRADE:
                Barricade barricade = gameMap.getBarricade();
                if (barricade.canUpgrade() && gameMap.getSupply().spend(barricade.getUpgradeCost())) {
                    barricade.upgrade();
                    gameMap.addLog(">> 바리케이드 강화 Lv" + barricade.getLevel() + " (최대HP " + barricade.getMaxHp() + ")");
                }
                buildMode = false;
                break;
            case Util.KEY_QUIT:
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
            case Util.KEY_QUIT:
                recruitMode = false;
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
                }
                break;
            case KEY_RECRUIT:
                // 낮에만 모집 모드 진입 (최대 인원 미만일 때만)
                boolean canRecruit = !dayNightCycle.isNight() && gameMap.getColonists().size() < MAX_COLONISTS;
                if (canRecruit) {
                    recruitMode = true;
                }
                break;
        }
    }
}
