package gun;

import unit.colonist.Colonist;

import unit.enemy.Enemy;
import game.GameWorld;

/// <summary>
/// 라이플 — 관통 단발 무기
/// 느리지만 높은 데미지, 총알이 적을 뚫고 지나감
/// </summary>
public class Rifle extends Gun {

    public Rifle() {
        super("라이플", 20, 5, '-', 36, 8, 6);
    }

    /// <summary>
    /// 관통 총알 1발을 적 중앙을 향해 발사
    /// 적에 명중해도 사라지지 않고 계속 직진
    /// </summary>
    @Override
    public void fire(Colonist colonist, Enemy target, GameWorld gameWorld) {
        fireBullet(colonist, target, gameWorld, true, 0);
    }
}
