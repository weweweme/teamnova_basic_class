package unit.colonist;

import game.GameWorld;
import game.HitEffect;
import unit.enemy.Enemy;

/// <summary>
/// 저격수 — Colonist 서브클래스
/// 특수공격 "정밀 저격": HP 최고 적에 큰 데미지 + 강조 이펙트
/// </summary>
public class Sniper extends Colonist {

    /// <summary>
    /// 저격수 생성
    /// </summary>
    public Sniper(ColonistSpec spec, String name, char label,
                  game.Position position, GameWorld gameWorld) {
        super(ColonistType.SNIPER, spec, name, label, position, gameWorld);
    }

    /// <summary>
    /// 특수공격 — "정밀 저격"
    /// HP가 가장 높은 적에 큰 데미지 + 적 위치에 강조 이펙트
    /// </summary>
    @Override
    protected void specialAttack(Enemy target) {
        GameWorld gameWorld = getGameWorld();

        // HP가 가장 높은 살아있는 적 찾기
        Enemy strongest = null;
        int highestHp = 0;

        for (Enemy enemy : gameWorld.getEnemies()) {
            if (enemy.isLiving() && enemy.getHp() > highestHp) {
                highestHp = enemy.getHp();
                strongest = enemy;
            }
        }

        if (strongest == null) {
            return;
        }

        // 대상에게 큰 데미지
        final int PRECISION_DAMAGE = 50;
        strongest.takeDamage(PRECISION_DAMAGE);

        // 적 위치에 강조 이펙트 (중심 + 상하)
        int targetRow = strongest.getPosition().getRow();
        int targetCol = strongest.getPosition().getCol();
        long now = System.currentTimeMillis();

        final int COLOR_BRIGHT_RED = 91;
        gameWorld.addEffect(new HitEffect(targetRow, targetCol, now, '*', COLOR_BRIGHT_RED));
        if (targetRow - 1 >= 0) {
            gameWorld.addEffect(new HitEffect(targetRow - 1, targetCol, now, '*', COLOR_BRIGHT_RED));
        }
        if (targetRow + 1 < GameWorld.HEIGHT) {
            gameWorld.addEffect(new HitEffect(targetRow + 1, targetCol, now, '*', COLOR_BRIGHT_RED));
        }

        gameWorld.getSfxPlayer().playPrecisionShot();
        String enemyName = strongest.getSpec().getDisplayName();
        gameWorld.addLog("[" + getLabel() + "] 정밀 저격! → " + enemyName);
    }
}
