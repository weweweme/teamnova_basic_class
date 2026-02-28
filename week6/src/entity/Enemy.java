package entity;

import core.Position;
import core.Util;
import world.Barricade;
import world.GameMap;

/// <summary>
/// 밤에 오른쪽에서 출현하여 좌측으로 이동하는 적
/// 종류(EnemyType)에 따라 체력, 공격력, 속도, 외형이 다름
/// 자기 스레드에서 매 틱마다 왼쪽으로 한 칸씩 이동
/// </summary>
public class Enemy extends Thread {

    /// <summary>
    /// 이 적의 종류 (체력/공격력/속도/외형 결정)
    /// </summary>
    private final EnemyType type;

    /// <summary>
    /// 맵 위의 위치
    /// </summary>
    private final Position position;

    /// <summary>
    /// 이 적이 속한 맵
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
    /// 지정한 종류, 위치, 맵으로 적 생성
    /// </summary>
    public Enemy(EnemyType type, Position position, GameMap gameMap) {
        this.type = type;
        this.position = position;
        this.gameMap = gameMap;
        this.hp = type.getMaxHp();
        this.running = true;
    }

    /// <summary>
    /// 스레드 실행 루프
    /// 매 틱마다 왼쪽으로 이동, 바리케이드 도달 시 공격
    /// </summary>
    @Override
    public void run() {
        // 바리케이드 바로 오른쪽에서 멈춤
        int stopCol = Barricade.COLUMN + 2;

        while (running && isLiving()) {
            int currentCol = position.getCol();

            if (currentCol > stopCol) {
                // 아직 바리케이드에 도달하지 않음 — 왼쪽으로 이동
                position.setCol(currentCol - 1);
            } else {
                // 바리케이드에 도달 — 공격
                Barricade barricade = gameMap.getBarricade();
                if (!barricade.isDestroyed()) {
                    barricade.takeDamage(type.getDamage());
                }
            }

            Util.delay(type.getTickDelay());
        }
    }

    /// <summary>
    /// 스레드 안전하게 종료
    /// </summary>
    public void stopRunning() {
        running = false;
    }

    /// <summary>
    /// 적 종류 반환
    /// </summary>
    public EnemyType getType() {
        return type;
    }

    /// <summary>
    /// 위치 반환
    /// </summary>
    public Position getPosition() {
        return position;
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
        return type.getMaxHp();
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
}
