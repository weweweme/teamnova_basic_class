package structure;

import game.GameWorld;
import game.HitEffect;

import entity.enemy.Enemy;

import java.util.ArrayList;

/// <summary>
/// 일회용 범위 폭발 구조물
/// 적이 밟으면 주변 적에게 피해를 주고 파괴됨
/// </summary>
public class Landmine extends Trap {

    /// <summary>
    /// 폭발 범위 (좌우 칸 수)
    /// </summary>
    private static final int BLAST_RANGE = 3;

    /// <summary>
    /// 지정한 열에 지뢰 설치 (일회용, 비용 25, 폭발 데미지 15)
    /// </summary>
    public Landmine(int column) {
        super(column, 1, 25, 15);
    }

    /// <summary>
    /// 폭발 처리: 범위 내 모든 적에게 데미지
    /// 폭발 후 자동 파괴됨
    /// </summary>
    public void explode(ArrayList<Enemy> enemies, GameWorld gameWorld) {
        int mineCol = getColumn();

        for (Enemy enemy : enemies) {
            if (!enemy.isLiving()) {
                continue;
            }

            int enemyCol = enemy.getPosition().getCol();
            int distance = Math.abs(enemyCol - mineCol);
            boolean inRange = distance <= BLAST_RANGE;

            if (inRange) {
                enemy.takeDamage(getDamage());

                if (!enemy.isLiving()) {
                    gameWorld.addLog("[지뢰] " + enemy.getSpec().getDisplayName() + " 처치!");
                }
            }
        }

        // 폭발 이펙트 추가 (다이아몬드 형태, 중심에서 바깥으로 갈수록 약해짐)
        int centerRow = GameWorld.HEIGHT / 2;
        long now = System.currentTimeMillis();

        // 밝은 빨간색 ANSI 코드
        final int COLOR_BRIGHT_RED = 91;
        // 노란색 ANSI 코드
        final int COLOR_YELLOW = 33;

        for (int dr = -BLAST_RANGE; dr <= BLAST_RANGE; dr++) {
            int row = centerRow + dr;
            boolean validRow = row >= 0 && row < GameWorld.HEIGHT;
            if (!validRow) {
                continue;
            }

            // 중심에서 멀어질수록 가로 범위 축소 (다이아몬드)
            int colRange = BLAST_RANGE - Math.abs(dr);
            // 중심 행은 밝은 빨강 + '*', 바깥 행은 노랑 + '·'
            boolean isCenter = dr == 0;
            char effectChar = isCenter ? '*' : '.';
            int effectColor = isCenter ? COLOR_BRIGHT_RED : COLOR_YELLOW;

            for (int dc = -colRange; dc <= colRange; dc++) {
                int col = mineCol + dc;
                boolean validCol = col >= 0 && col < GameWorld.WIDTH;
                if (validCol) {
                    gameWorld.getEffects().add(new HitEffect(row, col, now, effectChar, effectColor));
                }
            }
        }

        // 화면 흔들림 (200ms, 강도 2) + 폭발음
        gameWorld.getScreenEffects().triggerScreenShake(200, 2);
        gameWorld.getSfxPlayer().playExplosion();

        // 지뢰 파괴
        takeDamage(getMaxHp());
    }

    /// <summary>
    /// 폭발 범위 반환
    /// </summary>
    public int getBlastRange() {
        return BLAST_RANGE;
    }
}
