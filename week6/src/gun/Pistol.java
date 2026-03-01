package gun;

import entity.colonist.Colonist;

import entity.enemy.Enemy;
import game.GameMap;

/// <summary>
/// 피스톨 — 기본 무기
/// 단발 사격, 균형 잡힌 데미지와 발사 간격
/// </summary>
public class Pistol extends Gun {

    /// <summary>
    /// 발사 간격 (틱 수)
    /// </summary>
    private final int FIRE_INTERVAL = 4;

    /// <summary>
    /// 기본 피해량
    /// </summary>
    private final int DAMAGE = 5;

    @Override
    public String getName() {
        return "피스톨";
    }

    @Override
    public int getCost() {
        return 0;
    }

    @Override
    public int getFireInterval() {
        return FIRE_INTERVAL;
    }

    /// <summary>
    /// 단발 총알 1개를 적 중앙을 향해 발사
    /// </summary>
    @Override
    public void fire(Colonist colonist, Enemy target, GameMap gameMap) {
        final int BULLET_SPEED = 3;
        fireBullet(colonist, target, gameMap, DAMAGE, BULLET_SPEED, false, 0);
    }

    @Override
    public char getBulletChar() {
        return '*';
    }

    @Override
    public int getBulletColor() {
        return 0;
    }
}
