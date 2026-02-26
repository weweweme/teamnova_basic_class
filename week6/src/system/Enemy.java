package system;

/// <summary>
/// 밤에 맵 가장자리에서 출현하는 적
/// 자기 스레드에서 가장 가까운 정착민을 향해 이동
/// </summary>
public class Enemy extends Thread {

    /// <summary>
    /// 최대 체력
    /// </summary>
    private static final int MAX_HP = 50;

    /// <summary>
    /// 행동 틱 간격 (밀리초, 정착민보다 빠름)
    /// </summary>
    private static final int TICK_DELAY = 300;

    /// <summary>
    /// 맵 위의 위치
    /// </summary>
    private final Position position;

    /// <summary>
    /// 이 적이 속한 맵
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 현재 체력
    /// </summary>
    private int hp;

    /// <summary>
    /// 스레드 실행 여부
    /// </summary>
    private volatile boolean running;

    /// <summary>
    /// 지정한 위치와 맵으로 적 생성
    /// </summary>
    public Enemy(Position position, GameMap gameMap) {
        this.position = position;
        this.gameMap = gameMap;
        this.hp = MAX_HP;
        this.running = true;
    }

    @Override
    public void run() {
        while (running && isLiving()) {
            moveTowardClosestColonist();
            Util.delay(TICK_DELAY);
        }
    }

    /// <summary>
    /// 가장 가까운 정착민을 향해 한 칸 이동
    /// </summary>
    private void moveTowardClosestColonist() {
        Colonist closest = findClosestColonist();
        if (closest == null) {
            return;
        }

        Position targetPos = closest.getPosition();
        int rowDiff = targetPos.getRow() - position.getRow();
        int colDiff = targetPos.getCol() - position.getCol();

        int nextRow = position.getRow();
        int nextCol = position.getCol();

        if (rowDiff > 0) {
            nextRow++;
        } else if (rowDiff < 0) {
            nextRow--;
        }

        if (colDiff > 0) {
            nextCol++;
        } else if (colDiff < 0) {
            nextCol--;
        }

        if (gameMap.isWalkable(nextRow, nextCol)) {
            position.moveTo(nextRow, nextCol);
        }
    }

    /// <summary>
    /// 맵의 정착민 중 가장 가까운 정착민 찾기
    /// 정착민이 없으면 null 반환
    /// </summary>
    private Colonist findClosestColonist() {
        Colonist closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Colonist colonist : gameMap.getColonists()) {
            if (!colonist.isLiving()) {
                continue;
            }

            int distance = position.distanceTo(colonist.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                closest = colonist;
            }
        }
        return closest;
    }

    /// <summary>
    /// 스레드 안전하게 종료
    /// </summary>
    public void stopRunning() {
        running = false;
    }

    /// <summary>
    /// 위치 반환
    /// </summary>
    public Position getPosition() {
        return position;
    }

    /// <summary>
    /// 현재 체력 반환
    /// </summary>
    public int getHp() {
        return hp;
    }

    /// <summary>
    /// 최대 체력 반환
    /// </summary>
    public int getMaxHp() {
        return MAX_HP;
    }

    /// <summary>
    /// 체력이 남아있는지 확인
    /// </summary>
    public boolean isLiving() {
        return hp > 0;
    }

    /// <summary>
    /// 피해를 받아 체력 감소
    /// </summary>
    public void takeDamage(int damage) {
        hp = Math.max(hp - damage, 0);
    }
}
