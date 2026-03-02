package gun;

import unit.colonist.Colonist;

import unit.enemy.Enemy;
import game.GameWorld;

/// <summary>
/// 피스톨 — 기본 무기
/// 단발 사격, 균형 잡힌 데미지와 발사 간격
/// </summary>
public class Pistol extends Gun {

    public Pistol() {
        super("피스톨", 0, 4, '*', 0, 5, 3);
    }

    /// <summary>
    /// 단발 총알 1개를 적 중앙을 향해 발사
    /// </summary>
    @Override
    public void fire(Colonist colonist, Enemy target, GameWorld gameWorld) {
        fireBullet(colonist, target, gameWorld, false, 0);
    }
}
