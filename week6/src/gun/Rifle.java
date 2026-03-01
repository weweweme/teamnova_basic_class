package gun;

import entity.colonist.Colonist;

import gun.Bullet;
import entity.enemy.Enemy;
import structure.Barricade;
import game.GameMap;

/// <summary>
/// 라이플 — 관통 단발 무기
/// 느리지만 높은 데미지, 총알이 적을 뚫고 지나감
/// </summary>
public class Rifle extends Gun {

    /// <summary>
    /// 발사 간격 (틱 수)
    /// </summary>
    private static final int FIRE_INTERVAL = 5;

    /// <summary>
    /// 기본 피해량
    /// </summary>
    private static final int DAMAGE = 8;

    /// <summary>
    /// 총알 이동 속도 (관통이라 빠르게 설정)
    /// </summary>
    private static final int BULLET_SPEED = 6;

    /// <summary>
    /// 총알 ANSI 색상 (시안)
    /// </summary>
    private static final int BULLET_COLOR = 36;

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
        return FIRE_INTERVAL;
    }

    /// <summary>
    /// 관통 총알 1발을 적 중앙을 향해 발사
    /// 적에 명중해도 사라지지 않고 계속 직진
    /// </summary>
    @Override
    public void fire(Colonist colonist, Enemy target, GameMap gameMap) {
        int bulletRow = colonist.getPosition().getRow();
        int bulletCol = Barricade.COLUMN + 2;

        // 적 블록 중앙을 조준
        String[] block = target.getSpec().getBlock();
        int aimRow = target.getPosition().getRow() + block.length / 2;
        int aimCol = target.getPosition().getCol() + block[0].length() / 2;

        int finalDamage = applyCrit(DAMAGE, colonist);
        int kb = getKnockback(colonist);
        Bullet bullet = new Bullet(
            bulletRow, bulletCol, aimRow, aimCol, finalDamage,
            colonist.getLabel(), BULLET_SPEED, getBulletChar(), BULLET_COLOR, true, kb
        );
        gameMap.addBullet(bullet);
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
