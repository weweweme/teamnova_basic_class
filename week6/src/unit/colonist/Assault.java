package unit.colonist;

import game.GameWorld;
import game.HitEffect;
import structure.Barricade;
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

        long now = System.currentTimeMillis();
        final int COLOR_CYAN = 96;
        final int COLOR_BRIGHT_WHITE = 97;

        // 모든 살아있는 적에게 데미지 + 넉백 + 충격 이펙트
        for (Enemy enemy : gameWorld.getEnemies()) {
            if (!enemy.isLiving()) {
                continue;
            }

            // 적 위치에 충격 이펙트 (밀려나기 전)
            int enemyRow = enemy.getPosition().getRow();
            int enemyCol = enemy.getPosition().getCol();
            String[] block = enemy.getSpec().getBlock();
            int centerRow = enemyRow + block.length / 2;
            gameWorld.addEffect(new HitEffect(centerRow, enemyCol, now, '!', COLOR_BRIGHT_WHITE));

            final int SHOCKWAVE_DAMAGE = 5;
            enemy.takeDamage(SHOCKWAVE_DAMAGE);

            // 넉백 (오른쪽으로 밀어냄, 맵 밖으로 나가지 않도록 제한)
            if (enemy.isLiving()) {
                final int KNOCKBACK_DISTANCE = 5;
                int newCol = enemyCol + KNOCKBACK_DISTANCE;
                if (newCol < GameWorld.WIDTH) {
                    enemy.getPosition().setCol(newCol);
                }
            }
        }

        // 바리케이드에서 전장까지 수평 충격파 라인 (3줄)
        int waveStart = Barricade.COLUMN + 3;
        int centerRow = GameWorld.HEIGHT / 2;
        final int WAVE_SPACING = 3;
        for (int c = waveStart; c < GameWorld.WIDTH; c += WAVE_SPACING) {
            gameWorld.addEffect(new HitEffect(centerRow, c, now, '~', COLOR_CYAN));
            if (centerRow - 3 >= 0) {
                gameWorld.addEffect(new HitEffect(centerRow - 3, c, now, '~', COLOR_CYAN));
            }
            if (centerRow + 3 < GameWorld.HEIGHT) {
                gameWorld.addEffect(new HitEffect(centerRow + 3, c, now, '~', COLOR_CYAN));
            }
        }

        // 화면 흔들림 발동
        final int SHAKE_DURATION = 500;
        final int SHAKE_INTENSITY = 2;
        gameWorld.getScreenEffects().triggerScreenShake(SHAKE_DURATION, SHAKE_INTENSITY);

        gameWorld.getSfxPlayer().playShockwave();
        gameWorld.addLog("[" + getLabel() + ": " + getColonistName() + "] 충격파!");
    }
}
