package gun;

import entity.colonist.Colonist;

import entity.enemy.Enemy;
import game.GameMap;

/// <summary>
/// 샷건 — 부채꼴 3발 동시 발사
/// 발사 간격이 길지만 넓은 범위를 커버
/// </summary>
public class Shotgun extends Gun {

    /// <summary>
    /// 발사 간격 (틱 수)
    /// </summary>
    private final int FIRE_INTERVAL = 6;

    /// <summary>
    /// 총알 1발당 피해량
    /// </summary>
    private final int DAMAGE = 3;

    /// <summary>
    /// 부채꼴 조준 상하 오프셋 (적 중앙 기준 ± 행)
    /// </summary>
    private final int SPREAD = 2;

    /// <summary>
    /// 총알 ANSI 색상 (노랑)
    /// </summary>
    private final int BULLET_COLOR = 33;

    @Override
    public String getName() {
        return "샷건";
    }

    @Override
    public int getCost() {
        return 25;
    }

    @Override
    public int getFireInterval() {
        return FIRE_INTERVAL;
    }

    /// <summary>
    /// 적 중앙 + 위/아래로 3발을 부채꼴 발사
    /// </summary>
    @Override
    public void fire(Colonist colonist, Enemy target, GameMap gameMap) {
        final int BULLET_SPEED = 3;
        // 중앙 + 위 + 아래 총 3발
        int[] offsets = {0, -SPREAD, SPREAD};
        for (int offset : offsets) {
            fireBullet(colonist, target, gameMap, DAMAGE, BULLET_SPEED, false, offset);
        }
    }

    @Override
    public char getBulletChar() {
        return '*';
    }

    @Override
    public int getBulletColor() {
        return BULLET_COLOR;
    }
}
