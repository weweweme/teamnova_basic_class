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

        // 적 블록 중앙 좌표 계산
        int targetRow = strongest.getPosition().getRow();
        int targetCol = strongest.getPosition().getCol();
        String[] block = strongest.getSpec().getBlock();
        int centerRow = targetRow + block.length / 2;
        int centerCol = targetCol + block[0].length() / 2;
        long now = System.currentTimeMillis();

        final int COLOR_BRIGHT_RED = 91;
        final int COLOR_RED = 31;

        // 적 중앙에 큰 십자 조준선 이펙트
        gameWorld.addEffect(new HitEffect(centerRow, centerCol, now, 'X', COLOR_BRIGHT_RED));

        // 상하좌우 조준선 (+ 모양)
        int[][] crossOffsets = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-2, 0}, {2, 0}};
        for (int[] offset : crossOffsets) {
            int r = centerRow + offset[0];
            int c = centerCol + offset[1];
            if (r >= 0 && r < GameWorld.HEIGHT && c >= 0 && c < GameWorld.WIDTH) {
                gameWorld.addEffect(new HitEffect(r, c, now, '+', COLOR_BRIGHT_RED));
            }
        }

        // 바리케이드에서 적까지 탄도선 (빨간 '-' 라인)
        int trailStart = structure.Barricade.COLUMN + 3;
        final int TRAIL_SPACING = 2;
        for (int c = trailStart; c < centerCol; c += TRAIL_SPACING) {
            gameWorld.addEffect(new HitEffect(centerRow, c, now, '-', COLOR_RED));
        }

        gameWorld.getSfxPlayer().playPrecisionShot();
        String enemyName = strongest.getSpec().getDisplayName();
        gameWorld.addLog("[" + getLabel() + ": " + getColonistName() + "] 정밀 저격! → " + enemyName);
    }
}
