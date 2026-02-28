package system;

/// <summary>
/// 사격 상태 — 밤에 바리케이드에 붙어서 가장 가까운 적을 조준 사격
/// 자연 전환 시 바리케이드까지 걸어서 이동, 건너뛰기 시 즉시 배치
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
    /// 즉시 배치 여부 (밤 건너뛰기 시 true)
    /// </summary>
    private final boolean instant;

    /// <summary>
    /// 목표 행 (사격 위치)
    /// </summary>
    private int targetRow;

    /// <summary>
    /// 목표에 도착했는지 여부
    /// </summary>
    private boolean arrived;

    /// <summary>
    /// 발사 카운터 (SHOOT_INTERVAL에 도달하면 발사)
    /// </summary>
    private int tickCount;

    /// <summary>
    /// 즉시 배치 여부를 지정하여 생성
    /// </summary>
    public ShootingState(boolean instant) {
        this.instant = instant;
    }

    @Override
    public void enter(Colonist colonist) {
        tickCount = 0;
        arrived = false;

        // 정착민 목록에서 자기 순번으로 목표 행 계산 (겹침 방지)
        GameMap gameMap = colonist.getGameMap();
        int index = gameMap.getColonists().indexOf(colonist);
        int count = gameMap.getColonists().size();
        int spacing = GameMap.HEIGHT / (count + 1);
        targetRow = spacing * (index + 1);

        if (instant) {
            // 밤 건너뛰기: 즉시 배치
            colonist.getPosition().setRow(targetRow);
            colonist.getPosition().setCol(SHOOT_COL);
            arrived = true;
        }
    }

    @Override
    public void update(Colonist colonist) {
        // 아직 목표에 도착하지 않았으면 이동
        if (!arrived) {
            moveToward(colonist);
            return;
        }

        // 사격 로직
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
        int aimRow = target.getPosition().getRow() + block.length / 2;
        int aimCol = target.getPosition().getCol() + block[0].length() / 2;

        int damage = colonist.getWeaponLevel() * BASE_DAMAGE;
        Bullet bullet = new Bullet(bulletRow, bulletCol, aimRow, aimCol, damage);
        colonist.getGameMap().addBullet(bullet);
    }

    /// <summary>
    /// 목표 위치를 향해 한 칸씩 이동
    /// 행과 열을 동시에 움직여 대각선 이동
    /// </summary>
    private void moveToward(Colonist colonist) {
        int currentRow = colonist.getPosition().getRow();
        int currentCol = colonist.getPosition().getCol();

        // 행 이동
        if (currentRow < targetRow) {
            colonist.getPosition().setRow(currentRow + 1);
        } else if (currentRow > targetRow) {
            colonist.getPosition().setRow(currentRow - 1);
        }

        // 열 이동
        if (currentCol < SHOOT_COL) {
            colonist.getPosition().setCol(currentCol + 1);
        } else if (currentCol > SHOOT_COL) {
            colonist.getPosition().setCol(currentCol - 1);
        }

        // 도착 확인
        boolean rowArrived = colonist.getPosition().getRow() == targetRow;
        boolean colArrived = colonist.getPosition().getCol() == SHOOT_COL;
        if (rowArrived && colArrived) {
            arrived = true;
        }
    }

    @Override
    public void exit(Colonist colonist) {
        // 사격 상태 퇴장 시 별도 정리 없음
    }

    @Override
    public String getDisplayName() {
        if (!arrived) {
            return "배치 중";
        }
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
