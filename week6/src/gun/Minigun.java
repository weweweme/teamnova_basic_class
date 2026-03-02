package gun;

import unit.colonist.Colonist;

import unit.enemy.Enemy;
import game.GameWorld;

/// <summary>
/// 미니건 — 초고속 연사 무기
/// 매 틱마다 발사, 데미지는 낮지만 물량으로 제압
/// </summary>
public class Minigun extends Gun {

    public Minigun() {
        super("미니건", 30, 1, '.', 0, 2, 4);
    }

    /// <summary>
    /// 단발 총알 1개를 적 중앙을 향해 발사 (매 틱마다 반복)
    /// </summary>
    @Override
    public void fire(Colonist colonist, Enemy target, GameWorld gameWorld) {
        fireBullet(colonist, target, gameWorld, false, 0);
    }
}
