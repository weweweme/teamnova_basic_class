package entity.colonist;

import entity.Bullet;
import entity.enemy.Enemy;
import world.Barricade;
import world.GameMap;

/// <summary>
/// 피스톨 — 기본 무기
/// 단발 사격, 균형 잡힌 데미지와 발사 간격
/// </summary>
public class Pistol extends Gun {

    /// <summary>
    /// 발사 간격 (틱 수)
    /// </summary>
    private static final int FIRE_INTERVAL = 4;

    /// <summary>
    /// 기본 피해량
    /// </summary>
    private static final int DAMAGE = 5;

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
        int bulletRow = colonist.getPosition().getRow();
        int bulletCol = Barricade.COLUMN + 2;

        // 적 블록 중앙을 조준
        String[] block = target.getType().getBlock();
        int aimRow = target.getPosition().getRow() + block.length / 2;
        int aimCol = target.getPosition().getCol() + block[0].length() / 2;

        Bullet bullet = new Bullet(bulletRow, bulletCol, aimRow, aimCol, DAMAGE, colonist.getLabel());
        gameMap.addBullet(bullet);
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
