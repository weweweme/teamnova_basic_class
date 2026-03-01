package gun;

import entity.colonist.Colonist;

import entity.enemy.Enemy;
import game.GameMap;

/// <summary>
/// 라이플 — 관통 단발 무기
/// 느리지만 높은 데미지, 총알이 적을 뚫고 지나감
/// </summary>
public class Rifle extends Gun {

    /// <summary>
    /// 총알 ANSI 색상 (시안)
    /// </summary>
    private final int BULLET_COLOR = 36;

    @Override
    public String getName() {
        return "라이플";
    }

    @Override
    public int getCost() {
        return 20;
    }

    @Override
    public int getFireInterval() {
        /// <summary>
        /// 발사 간격 (틱 수)
        /// </summary>
        return 5;
    }

    /// <summary>
    /// 관통 총알 1발을 적 중앙을 향해 발사
    /// 적에 명중해도 사라지지 않고 계속 직진
    /// </summary>
    @Override
    public void fire(Colonist colonist, Enemy target, GameMap gameMap) {
        final int DAMAGE = 8;
        final int BULLET_SPEED = 6;
        fireBullet(colonist, target, gameMap, DAMAGE, BULLET_SPEED, true, 0);
    }

    @Override
    public char getBulletChar() {
        return '-';
    }

    @Override
    public int getBulletColor() {
        return BULLET_COLOR;
    }
}
