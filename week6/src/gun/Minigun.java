package gun;

import entity.colonist.Colonist;

import entity.enemy.Enemy;
import game.GameMap;

/// <summary>
/// 미니건 — 초고속 연사 무기
/// 매 틱마다 발사, 데미지는 낮지만 물량으로 제압
/// </summary>
public class Minigun extends Gun {

    /// <summary>
    /// 발사 간격 (틱 수, 매 틱마다 발사)
    /// </summary>
    private final int FIRE_INTERVAL = 1;

    /// <summary>
    /// 기본 피해량
    /// </summary>
    private final int DAMAGE = 2;

    /// <summary>
    /// 총알 이동 속도
    /// </summary>
    private final int BULLET_SPEED = 4;

    @Override
    public String getName() {
        return "미니건";
    }

    @Override
    public int getCost() {
        return 30;
    }

    @Override
    public int getFireInterval() {
        return FIRE_INTERVAL;
    }

    /// <summary>
    /// 단발 총알 1개를 적 중앙을 향해 발사 (매 틱마다 반복)
    /// </summary>
    @Override
    public void fire(Colonist colonist, Enemy target, GameMap gameMap) {
        fireBullet(colonist, target, gameMap, DAMAGE, BULLET_SPEED, false, 0);
    }

    @Override
    public char getBulletChar() {
        return '.';
    }

    @Override
    public int getBulletColor() {
        return 0;
    }
}
