package unit.colonist;

import game.GameWorld;
import game.HitEffect;
import game.Util;
import structure.Barricade;
import unit.enemy.Enemy;

/// <summary>
/// 사격수 — Colonist 서브클래스
/// 특수공격 "속사 탄막": 전체 적에 소량 데미지 + 탄흔 이펙트
/// </summary>
public class Gunner extends Colonist {

    /// <summary>
    /// 사격수 생성
    /// </summary>
    public Gunner(ColonistSpec spec, String name, char label,
                  game.Position position, GameWorld gameWorld) {
        super(ColonistType.GUNNER, spec, name, label, position, gameWorld);
    }

    /// <summary>
    /// 특수공격 — "속사 탄막"
    /// 전체 적에 소량 데미지 + 전장에 무작위 탄흔 이펙트
    /// </summary>
    @Override
    protected void specialAttack(Enemy target) {
        GameWorld gameWorld = getGameWorld();

        long now = System.currentTimeMillis();
        final int COLOR_YELLOW = 33;
        final int COLOR_BRIGHT_YELLOW = 93;

        // 모든 살아있는 적에게 탄막 데미지 + 적 위치에 피격 이펙트
        final int BARRAGE_DAMAGE = 3;
        for (Enemy enemy : gameWorld.getEnemies()) {
            if (!enemy.isLiving()) {
                continue;
            }
            enemy.takeDamage(BARRAGE_DAMAGE);

            // 적 블록 중앙에 밝은 노란 피격 이펙트
            int enemyRow = enemy.getPosition().getRow();
            int enemyCol = enemy.getPosition().getCol();
            String[] block = enemy.getSpec().getBlock();
            int centerRow = enemyRow + block.length / 2;
            int centerCol = enemyCol + block[0].length() / 2;
            gameWorld.addEffect(new HitEffect(centerRow, centerCol, now, 'X', COLOR_BRIGHT_YELLOW));
        }

        // 바리케이드 오른쪽 전장에 무작위 탄흔 이펙트 (넓게 퍼뜨림)
        int minCol = Barricade.COLUMN + 3;
        int colRange = GameWorld.WIDTH - minCol;

        final int EFFECT_COUNT = 12;
        for (int i = 0; i < EFFECT_COUNT; i++) {
            int row = Util.rand(GameWorld.HEIGHT);
            int col = minCol + Util.rand(colRange);
            gameWorld.addEffect(new HitEffect(row, col, now, '*', COLOR_YELLOW));
        }

        gameWorld.getSfxPlayer().playBarrage();
        gameWorld.addLog("[" + getLabel() + "] 속사 탄막!");
    }
}
