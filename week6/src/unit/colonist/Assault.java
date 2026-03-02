package unit.colonist;

import game.GameWorld;
import unit.enemy.Enemy;

/// <summary>
/// 돌격수 — Colonist 서브클래스
/// 특수공격 "충격파": 전체 적 넉백 + 소량 데미지 + 화면 흔들림
/// </summary>
public class Assault extends Colonist {

    /// <summary>
    /// 돌격수 생성
    /// </summary>
    public Assault(ColonistSpec spec, String name, char label,
                   game.Position position, GameWorld gameWorld) {
        super(ColonistType.ASSAULT, spec, name, label, position, gameWorld);
    }

    /// <summary>
    /// 특수공격 — "충격파"
    /// 모든 적에게 데미지 + 넉백 + 화면 흔들림
    /// </summary>
    @Override
    protected void specialAttack(Enemy target) {
        GameWorld gameWorld = getGameWorld();

        // 모든 살아있는 적에게 데미지 + 넉백
        for (Enemy enemy : gameWorld.getEnemies()) {
            if (!enemy.isLiving()) {
                continue;
            }

            final int SHOCKWAVE_DAMAGE = 5;
            enemy.takeDamage(SHOCKWAVE_DAMAGE);

            // 넉백 (오른쪽으로 밀어냄, 맵 밖으로 나가지 않도록 제한)
            if (enemy.isLiving()) {
                final int KNOCKBACK_DISTANCE = 5;
                int newCol = enemy.getPosition().getCol() + KNOCKBACK_DISTANCE;
                if (newCol < GameWorld.WIDTH) {
                    enemy.getPosition().setCol(newCol);
                }
            }
        }

        // 화면 흔들림 발동
        final int SHAKE_DURATION = 500;
        final int SHAKE_INTENSITY = 2;
        gameWorld.getScreenEffects().triggerScreenShake(SHAKE_DURATION, SHAKE_INTENSITY);

        gameWorld.getSfxPlayer().playShockwave();
        gameWorld.addLog("[" + getLabel() + "] 충격파!");
    }
}
