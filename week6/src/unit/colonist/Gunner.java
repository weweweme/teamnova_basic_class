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
    /// 탄막 데미지 (적당 피해량)
    /// </summary>
    private static final int BARRAGE_DAMAGE = 3;

    /// <summary>
    /// 화면에 뿌릴 이펙트 수
    /// </summary>
    private static final int EFFECT_COUNT = 5;

    /// <summary>
    /// 이펙트 색상 (노란색)
    /// </summary>
    private static final int COLOR_YELLOW = 33;

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

        // 모든 살아있는 적에게 탄막 데미지
        for (Enemy enemy : gameWorld.getEnemies()) {
            if (enemy.isLiving()) {
                enemy.takeDamage(BARRAGE_DAMAGE);
            }
        }

        // 바리케이드 오른쪽 전장에 무작위 탄흔 이펙트
        int minCol = Barricade.COLUMN + 3;
        int colRange = GameWorld.WIDTH - minCol;
        long now = System.currentTimeMillis();
        for (int i = 0; i < EFFECT_COUNT; i++) {
            int row = Util.rand(GameWorld.HEIGHT);
            int col = minCol + Util.rand(colRange);
            gameWorld.addEffect(new HitEffect(row, col, now, '*', COLOR_YELLOW));
        }

        gameWorld.getSfxPlayer().playBarrage();
        gameWorld.addLog("[" + getLabel() + "] 속사 탄막!");
    }
}
