package entity;

import core.Position;
import core.Util;
import world.GameMap;

/// <summary>
/// 정착민 한 명을 나타내는 클래스
/// 각 정착민은 자기 스레드에서 자율적으로 행동
/// 상태 패턴으로 행동을 관리 (IdleState 등)
/// </summary>
public class Colonist extends Thread {

    /// <summary>
    /// 최대 체력
    /// </summary>
    private static final int MAX_HP = 100;

    /// <summary>
    /// 행동 틱 간격 (밀리초)
    /// </summary>
    private static final int TICK_DELAY = 500;

    /// <summary>
    /// 다음에 부여할 알파벳 (A부터 순서대로)
    /// </summary>
    private static char nextLabel = 'A';

    /// <summary>
    /// 정착민 이름 (한글, 패널에서만 표시)
    /// </summary>
    private final String name;

    /// <summary>
    /// 맵에 표시할 알파벳 라벨 (A, B, C 순서)
    /// </summary>
    private final char label;

    /// <summary>
    /// 맵 위의 위치
    /// </summary>
    private final Position position;

    /// <summary>
    /// 이 정착민이 속한 맵 (이동 시 범위 체크용)
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 현재 체력
    /// </summary>
    private int hp;

    /// <summary>
    /// 무기 레벨 (높을수록 공격력 증가, 기본 1)
    /// </summary>
    private int weaponLevel;

    /// <summary>
    /// 현재 행동 상태
    /// </summary>
    private ColonistState currentState;

    /// <summary>
    /// 스레드 실행 여부 (false가 되면 스레드 종료)
    /// </summary>
    private volatile boolean running;

    /// <summary>
    /// 지정한 이름, 위치, 맵으로 정착민 생성
    /// 체력은 최대, 대기 상태로 시작
    /// </summary>
    public Colonist(String name, Position position, GameMap gameMap) {
        this.name = name;
        this.label = nextLabel;
        nextLabel++;
        this.position = position;
        this.gameMap = gameMap;
        this.hp = MAX_HP;
        this.weaponLevel = 1;
        this.currentState = new WanderingState();
        this.running = true;
    }

    /// <summary>
    /// 스레드 실행 루프
    /// 매 틱마다 현재 상태의 update를 호출
    /// </summary>
    @Override
    public void run() {
        currentState.enter(this);

        while (running && isLiving()) {
            currentState.update(this);
            Util.delay(TICK_DELAY);
        }

        if (!isLiving()) {
            gameMap.addLog("[" + label + "] " + name + " 사망");
        }
    }

    /// <summary>
    /// 상태 전환
    /// 현재 상태의 exit → 새 상태의 enter 순서로 호출
    /// </summary>
    public void changeState(ColonistState newState) {
        currentState.exit(this);
        currentState = newState;
        currentState.enter(this);
        gameMap.addLog("[" + label + "] " + name + ": " + newState.getDisplayName());
    }

    /// <summary>
    /// 스레드를 안전하게 종료
    /// </summary>
    public void stopRunning() {
        running = false;
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
    /// 위치 반환
    /// </summary>
    public Position getPosition() {
        return position;
    }

    /// <summary>
    /// 이 정착민이 속한 맵 반환
    /// </summary>
    public GameMap getGameMap() {
        return gameMap;
    }

    /// <summary>
    /// 현재 체력 반환
    /// </summary>
    public int getHp() {
        return hp;
    }

    /// <summary>
    /// 최대 체력 반환
    /// </summary>
    public int getMaxHp() {
        return MAX_HP;
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

    /// <summary>
    /// 체력이 남아있는지 확인
    /// </summary>
    public boolean isLiving() {
        return hp > 0;
    }

    /// <summary>
    /// 피해를 받아 체력 감소
    /// </summary>
    public void takeDamage(int damage) {
        hp = Math.max(hp - damage, 0);
    }

    /// <summary>
    /// 체력 회복 (최대 체력 초과 방지)
    /// </summary>
    public void heal(int amount) {
        hp = Math.min(hp + amount, MAX_HP);
    }
}
