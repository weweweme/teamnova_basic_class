package system;

/// <summary>
/// 사격 상태 — 밤에 바리케이드에 붙어서 가장 가까운 적을 조준 사격
/// </summary>
public class ShootingState extends ColonistState {

    /// <summary>
    /// 발사 간격 (틱 수, 4틱 = 2초)
    /// </summary>
    private static final int SHOOT_INTERVAL = 4;

    /// <summary>
    /// 무기 레벨 1당 기본 피해량
    /// </summary>
    private static final int BASE_DAMAGE = 5;

    /// <summary>
    /// 사격 위치 (바리케이드 바로 왼쪽, 블록이 겹치지 않는 열)
    /// </summary>
    private static final int SHOOT_COL = Barricade.COLUMN - 3;

    /// <summary>
    /// 발사 카운터 (SHOOT_INTERVAL에 도달하면 발사)
    /// </summary>
    private int tickCount;

    @Override
    public void enter(Colonist colonist) {
        tickCount = 0;

        // 바리케이드 옆으로 이동 (행은 유지)
        colonist.getPosition().setCol(SHOOT_COL);
    }

    @Override
    public void update(Colonist colonist) {
        tickCount++;

        if (tickCount < SHOOT_INTERVAL) {
            return;
        }

        tickCount = 0;

        // 가장 가까운 적 찾기
        Enemy target = findNearestEnemy(colonist);
        if (target == null) {
            return;
        }

        // 총알 발사 위치 (바리케이드 오른쪽)
        int bulletRow = colonist.getPosition().getRow();
        int bulletCol = Barricade.COLUMN + 2;

        // 적 블록 중앙을 조준
        String[] block = target.getType().getBlock();
        int targetRow = target.getPosition().getRow() + block.length / 2;
        int targetCol = target.getPosition().getCol() + block[0].length() / 2;

        int damage = colonist.getWeaponLevel() * BASE_DAMAGE;
        Bullet bullet = new Bullet(bulletRow, bulletCol, targetRow, targetCol, damage);
        colonist.getGameMap().addBullet(bullet);
    }

    @Override
    public void exit(Colonist colonist) {
        // 사격 상태 퇴장 시 별도 정리 없음
    }

    @Override
    public String getDisplayName() {
        return "사격";
    }

    /// <summary>
    /// 살아있는 적 중 가장 가까운 적 찾기
    /// 없으면 null 반환
    /// </summary>
    private Enemy findNearestEnemy(Colonist colonist) {
        GameMap gameMap = colonist.getGameMap();
        Enemy nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Enemy enemy : gameMap.getEnemies()) {
            if (!enemy.isLiving()) {
                continue;
            }

            int distance = colonist.getPosition().distanceTo(enemy.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = enemy;
            }
        }

        return nearest;
    }
}
