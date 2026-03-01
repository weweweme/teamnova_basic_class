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
public class Enemy extends GameEntity {

    /// <summary>
    /// 이 적의 종류 (체력/공격력/속도/외형 결정)
    /// </summary>
    private final EnemyType type;

    /// <summary>
    /// 사망 시각 (0이면 살아있음, 양수면 사망 시점의 밀리초)
    /// </summary>
    private long deathTime;

    /// <summary>
    /// 지정한 종류, 위치, 맵으로 적 생성
    /// </summary>
    public Enemy(EnemyType type, Position position, GameMap gameMap) {
        super(position, gameMap, type.getMaxHp());
        this.type = type;
    }

    /// <summary>
    /// 스레드 실행 루프
    /// 매 틱마다 왼쪽으로 이동, 바리케이드 도달 시 공격
    /// </summary>
    @Override
    public void run() {
        // 바리케이드 바로 오른쪽에서 멈춤
        int stopCol = Barricade.COLUMN + 2;

        while (isRunning() && isLiving()) {
            int currentCol = getPosition().getCol();

            if (currentCol > stopCol) {
                // 아직 바리케이드에 도달하지 않음 — 왼쪽으로 이동
                getPosition().setCol(currentCol - 1);
            } else {
                // 바리케이드에 도달 — 공격
                Barricade barricade = getGameMap().getBarricade();
                if (!barricade.isDestroyed()) {
                    barricade.takeDamage(type.getDamage());
                }
            }

            Util.delay(type.getTickDelay());
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
    /// 적 종류 반환
    /// </summary>
    public EnemyType getType() {
        return type;
    }

    /// <summary>
    /// 사망 시각 반환 (0이면 살아있음)
    /// </summary>
    public long getDeathTime() {
        return deathTime;
    }
}
