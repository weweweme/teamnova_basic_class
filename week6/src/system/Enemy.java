package system;

/// <summary>
/// 밤에 오른쪽에서 출현하여 좌측으로 이동하는 적
/// 자기 스레드에서 매 틱마다 왼쪽으로 한 칸씩 이동
/// </summary>
public class Enemy extends Thread {

    /// <summary>
    /// 최대 체력
    /// </summary>
    private static final int MAX_HP = 50;

    /// <summary>
    /// 행동 틱 간격 (밀리초)
    /// </summary>
    private static final int TICK_DELAY = 300;

    /// <summary>
    /// 틱당 공격력
    /// </summary>
    private static final int ATTACK_DAMAGE = 5;

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

    /// <summary>
    /// 스레드 실행 루프
    /// 매 틱마다 왼쪽으로 한 칸 이동
    /// </summary>
    @Override
    public void run() {
        while (running && isLiving()) {
            // 왼쪽으로 한 칸 이동
            int nextCol = position.getCol() - 1;
            if (nextCol >= 0) {
                position.setCol(nextCol);
            }
            Util.delay(TICK_DELAY);
        }
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
