package entity;

import core.Position;
import world.GameMap;

/// <summary>
/// 정착민과 적의 공통 상위 클래스
/// 맵 위에서 체력과 위치를 가지며, 자기 스레드로 행동
/// </summary>
public abstract class GameEntity extends Thread {

    /// <summary>
    /// 맵 위의 위치
    /// </summary>
    private final Position position;

    /// <summary>
    /// 이 개체가 속한 맵
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 현재 체력
    /// </summary>
    private int hp;

    /// <summary>
    /// 스레드 실행 여부
    /// </summary>
    private volatile boolean running;

    /// <summary>
    /// 지정한 위치와 맵으로 개체 생성, 체력은 최대로 시작
    /// </summary>
    protected GameEntity(Position position, GameMap gameMap, int maxHp) {
        this.position = position;
        this.gameMap = gameMap;
        this.hp = maxHp;
        this.running = true;
    }

    /// <summary>
    /// 위치 반환
    /// </summary>
    public Position getPosition() {
        return position;
    }

    /// <summary>
    /// 이 개체가 속한 맵 반환
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
    public abstract int getMaxHp();

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
        hp = Math.min(hp + amount, getMaxHp());
    }

    /// <summary>
    /// 스레드를 안전하게 종료
    /// </summary>
    public void stopRunning() {
        running = false;
    }

    /// <summary>
    /// 스레드가 실행 중인지 확인 (하위 클래스의 run()에서 사용)
    /// </summary>
    protected boolean isRunning() {
        return running;
    }
}
