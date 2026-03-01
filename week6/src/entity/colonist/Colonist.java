package entity.colonist;

import gun.Gun;
import gun.Pistol;

import entity.GameEntity;
import game.Position;
import game.Util;
import game.GameMap;

/// <summary>
/// 정착민 한 명을 나타내는 클래스
/// 각 정착민은 자기 스레드에서 자율적으로 행동
/// 상태 패턴으로 행동을 관리 (WanderingState, ShootingState 등)
/// </summary>
public class Colonist extends GameEntity {

    /// <summary>
    /// 행동 틱 간격 (밀리초)
    /// </summary>
    private final int TICK_DELAY = 500;

    /// <summary>
    /// 정착민 유형 (타입 식별용)
    /// </summary>
    private final ColonistType type;

    /// <summary>
    /// 정착민 속성 데이터 (체력/패시브 등)
    /// </summary>
    private final ColonistSpec spec;

    /// <summary>
    /// 정착민 이름 (한글, 패널에서만 표시)
    /// </summary>
    private final String name;

    /// <summary>
    /// 맵에 표시할 알파벳 라벨 (A, B, C 순서)
    /// </summary>
    private final char label;

    /// <summary>
    /// 장착한 무기 (발사 패턴과 데미지 결정)
    /// </summary>
    private Gun gun;

    /// <summary>
    /// 현재 행동 상태
    /// </summary>
    private ColonistState currentState;

    /// <summary>
    /// 사망 시각 (0이면 살아있음, 양수면 사망 시점의 밀리초)
    /// </summary>
    private long deathTime;

    /// <summary>
    /// 지정한 유형, 이름, 위치, 맵으로 정착민 생성
    /// 체력은 유형의 최대 체력, 배회 상태로 시작
    /// </summary>
    public Colonist(ColonistType type, ColonistSpec spec, String name, char label, Position position, GameMap gameMap) {
        super(position, gameMap, spec.getMaxHp());
        this.type = type;
        this.spec = spec;
        this.name = name;
        this.label = label;
        this.gun = new Pistol();
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
            Util.beep();
            getGameMap().addLog("[" + label + "] " + name + " 사망");
        }
    }

    /// <summary>
    /// 최대 체력 반환
    /// </summary>
    @Override
    public int getMaxHp() {
        return spec.getMaxHp();
    }

    /// <summary>
    /// 피해를 받아 체력 감소, 사망 시 시각 기록
    /// </summary>
    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);

        if (getHp() == 0 && deathTime == 0) {
            deathTime = System.currentTimeMillis();
        }
    }

    /// <summary>
    /// 사망 시각 반환 (0이면 살아있음)
    /// </summary>
    public long getDeathTime() {
        return deathTime;
    }

    /// <summary>
    /// 정착민 유형 반환
    /// </summary>
    public ColonistType getType() {
        return type;
    }

    /// <summary>
    /// 정착민 속성 데이터 반환
    /// </summary>
    public ColonistSpec getSpec() {
        return spec;
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
    /// 장착한 무기 반환
    /// </summary>
    public Gun getGun() {
        return gun;
    }

    /// <summary>
    /// 무기 교체
    /// </summary>
    public void setGun(Gun gun) {
        this.gun = gun;
    }

    /// <summary>
    /// 현재 상태 반환
    /// </summary>
    public ColonistState getCurrentState() {
        return currentState;
    }
}
