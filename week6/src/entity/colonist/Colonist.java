package entity.colonist;

import entity.GameEntity;
import system.Position;
import system.Util;
import world.GameMap;

/// <summary>
/// 정착민 한 명을 나타내는 클래스
/// 각 정착민은 자기 스레드에서 자율적으로 행동
/// 상태 패턴으로 행동을 관리 (WanderingState, ShootingState 등)
/// </summary>
public class Colonist extends GameEntity {

    /// <summary>
    /// 행동 틱 간격 (밀리초)
    /// </summary>
    private static final int TICK_DELAY = 500;

    /// <summary>
    /// 다음에 부여할 알파벳 (A부터 순서대로)
    /// </summary>
    private static char nextLabel = 'A';

    /// <summary>
    /// 정착민 유형 (체력/발사간격/피해량 결정)
    /// </summary>
    private final ColonistType type;

    /// <summary>
    /// 정착민 이름 (한글, 패널에서만 표시)
    /// </summary>
    private final String name;

    /// <summary>
    /// 맵에 표시할 알파벳 라벨 (A, B, C 순서)
    /// </summary>
    private final char label;

    /// <summary>
    /// 무기 레벨 (높을수록 공격력 증가, 기본 1)
    /// </summary>
    private int weaponLevel;

    /// <summary>
    /// 현재 행동 상태
    /// </summary>
    private ColonistState currentState;

    /// <summary>
    /// 지정한 유형, 이름, 위치, 맵으로 정착민 생성
    /// 체력은 유형의 최대 체력, 배회 상태로 시작
    /// </summary>
    public Colonist(ColonistType type, String name, Position position, GameMap gameMap) {
        super(position, gameMap, type.getMaxHp());
        this.type = type;
        this.name = name;
        this.label = nextLabel;
        nextLabel++;
        this.weaponLevel = 1;
        this.currentState = new WanderingState();
    }

    /// <summary>
    /// 스레드 실행 루프
    /// 매 틱마다 현재 상태의 update를 호출
    /// </summary>
    @Override
    public void run() {
        currentState.enter(this);

        while (isRunning() && isLiving()) {
            currentState.update(this);
            Util.delay(TICK_DELAY);
        }

        if (!isLiving()) {
            getGameMap().addLog("[" + label + "] " + name + " 사망");
        }
    }

    /// <summary>
    /// 최대 체력 반환
    /// </summary>
    @Override
    public int getMaxHp() {
        return type.getMaxHp();
    }

    /// <summary>
    /// 정착민 유형 반환
    /// </summary>
    public ColonistType getType() {
        return type;
    }

    /// <summary>
    /// 상태 전환
    /// 현재 상태의 exit → 새 상태의 enter 순서로 호출
    /// </summary>
    public void changeState(ColonistState newState) {
        currentState.exit(this);
        currentState = newState;
        currentState.enter(this);
        getGameMap().addLog("[" + label + "] " + name + ": " + newState.getDisplayName());
    }

    /// <summary>
    /// 정착민 이름 반환
    /// </summary>
    public String getColonistName() {
        return name;
    }

    /// <summary>
    /// 맵 표시용 알파벳 라벨 반환
    /// </summary>
    public char getLabel() {
        return label;
    }

    /// <summary>
    /// 무기 레벨 반환
    /// </summary>
    public int getWeaponLevel() {
        return weaponLevel;
    }

    /// <summary>
    /// 무기 레벨 1 증가
    /// </summary>
    public void upgradeWeapon() {
        weaponLevel++;
    }

    /// <summary>
    /// 현재 상태 반환
    /// </summary>
    public ColonistState getCurrentState() {
        return currentState;
    }
}
