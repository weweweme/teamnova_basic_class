package structure;

import game.GameMap;

import entity.enemy.Enemy;

import java.util.ArrayList;

/// <summary>
/// 일회용 범위 폭발 구조물
/// 적이 밟으면 주변 적에게 피해를 주고 파괴됨
/// </summary>
public class Landmine extends Structure {

    /// <summary>
    /// 내구도 (일회용이므로 1)
    /// </summary>
    // super() 호출에 필요하여 static 유지
    private static final int MAX_HP = 1;

    /// <summary>
    /// 폭발 데미지
    /// </summary>
    private final int BLAST_DAMAGE = 15;

    /// <summary>
    /// 폭발 범위 (좌우 칸 수)
    /// </summary>
    private final int BLAST_RANGE = 3;

    /// <summary>
    /// 설치 비용
    /// </summary>
    public static final int COST = 25;

    /// <summary>
    /// 지정한 열에 지뢰 설치
    /// </summary>
    public Landmine(int column) {
        super(column, MAX_HP);
    }


    /// <summary>
    /// 폭발 처리: 범위 내 모든 적에게 데미지
    /// 폭발 후 자동 파괴됨
    /// </summary>
    public void explode(ArrayList<Enemy> enemies, GameMap gameMap) {
        int mineCol = getColumn();

        for (Enemy enemy : enemies) {
            if (!enemy.isLiving()) {
                continue;
            }

            int enemyCol = enemy.getPosition().getCol();
            int distance = Math.abs(enemyCol - mineCol);
            boolean inRange = distance <= BLAST_RANGE;

            if (inRange) {
                enemy.takeDamage(BLAST_DAMAGE);

                if (!enemy.isLiving()) {
                    gameMap.addLog("[지뢰] " + enemy.getSpec().getDisplayName() + " 처치!");
                }
            }
        }

        // 폭발 이펙트 추가
        int groundRow = GameMap.HEIGHT - 3;
        long now = System.currentTimeMillis();
        for (int col = mineCol - BLAST_RANGE; col <= mineCol + BLAST_RANGE; col++) {
            boolean validCol = col >= 0 && col < GameMap.WIDTH;
            if (validCol) {
                gameMap.getEffects().add(new GameMap.HitEffect(groundRow, col, now, '*', 31));
            }
        }

        // 지뢰 파괴
        takeDamage(MAX_HP);
    }

    /// <summary>
    /// 폭발 데미지 반환
    /// </summary>
    public int getBlastDamage() {
        return BLAST_DAMAGE;
    }

    /// <summary>
    /// 폭발 범위 반환
    /// </summary>
    public int getBlastRange() {
        return BLAST_RANGE;
    }
}
