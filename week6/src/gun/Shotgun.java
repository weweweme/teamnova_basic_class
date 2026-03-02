package gun;

import unit.colonist.Colonist;

import unit.enemy.Enemy;
import game.GameWorld;

/// <summary>
/// 샷건 — 부채꼴 3발 동시 발사
/// 발사 간격이 길지만 넓은 범위를 커버
/// </summary>
public class Shotgun extends Gun {

    /// <summary>
    /// 부채꼴 조준 상하 오프셋 (적 중앙 기준 ± 행)
    /// </summary>
    private static final int SPREAD = 2;

    /// <summary>
    /// 중앙 + 위 + 아래 조준 오프셋 배열
    /// </summary>
    private static final int[] OFFSETS = {0, -SPREAD, SPREAD};

    public Shotgun() {
        super("샷건", 25, 6, '*', 33, 3, 3);
    }

    /// <summary>
    /// 적 중앙 + 위/아래로 3발을 부채꼴 발사
    /// </summary>
    @Override
    public void fire(Colonist colonist, Enemy target, GameWorld gameWorld) {
        for (int offset : OFFSETS) {
            fireBullet(colonist, target, gameWorld, false, offset);
        }
    }
}
