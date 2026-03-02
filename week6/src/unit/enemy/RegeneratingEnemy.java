package unit.enemy;

import game.GameWorld;
import game.HitEffect;
import game.Position;
import game.Util;

/// <summary>
/// 재생 특성 적 — Enemy 서브클래스
/// 패시브: 3틱마다 체력 1 회복
/// 특수 스킬 "치유 포자": 모든 살아있는 적 HP 5 회복 + 초록 이펙트
/// </summary>
public class RegeneratingEnemy extends Enemy {

    /// <summary>
    /// 재생 패시브용 틱 카운터
    /// </summary>
    private int regenTick;

    /// <summary>
    /// 재생 적 생성
    /// </summary>
    public RegeneratingEnemy(EnemyType type, EnemySpec spec, Position position, GameWorld gameWorld) {
        super(type, spec, position, gameWorld);
    }

    /// <summary>
    /// 매 틱 패시브 — 3틱마다 체력 1 회복
    /// </summary>
    @Override
    protected void onTick() {
        regenTick++;

        // 재생 간격 (틱 수)
        final int REGEN_INTERVAL = 3;
        if (regenTick >= REGEN_INTERVAL) {
            regenTick = 0;
            if (getHp() < getMaxHp()) {
                heal(1);
            }
        }
    }

    /// <summary>
    /// 특수 스킬 — "치유 포자"
    /// 모든 살아있는 적 HP 5 회복 + 자신 위치에 초록색 이펙트
    /// </summary>
    @Override
    protected void specialAbility() {
        GameWorld gameWorld = getGameWorld();

        // 모든 살아있는 적 HP 회복
        final int HEAL_AMOUNT = 5;
        for (Enemy enemy : gameWorld.getEnemies()) {
            if (enemy.isLiving()) {
                enemy.heal(HEAL_AMOUNT);
            }
        }

        // 자신 위치에 초록색 '+' 이펙트 3개 (상하좌우 무작위)
        int row = getPosition().getRow();
        int col = getPosition().getCol();
        long now = System.currentTimeMillis();
        final int COLOR_GREEN = 32;
        final int EFFECT_COUNT = 3;

        for (int i = 0; i < EFFECT_COUNT; i++) {
            int offsetRow = row + Util.rand(3) - 1;
            int offsetCol = col + Util.rand(3) - 1;

            // 맵 범위 내로 제한
            if (offsetRow >= 0 && offsetRow < GameWorld.HEIGHT && offsetCol >= 0 && offsetCol < GameWorld.WIDTH) {
                gameWorld.addEffect(new HitEffect(offsetRow, offsetCol, now, '+', COLOR_GREEN));
            }
        }

        gameWorld.getSfxPlayer().playHealingSpore();
        String name = getSpec().getDisplayName();
        gameWorld.addLog(name + " — 치유 포자!");
    }
}
