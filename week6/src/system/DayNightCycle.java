package system;

/// <summary>
/// 낮/밤 주기를 관리하는 스레드
/// 낮(30초)과 밤(적 섬멸까지)을 반복
/// </summary>
public class DayNightCycle extends Thread {

    /// <summary>
    /// 낮 지속 시간 (밀리초)
    /// </summary>
    private static final int DAY_DURATION = 30000;

    /// <summary>
    /// 틱 간격 (밀리초)
    /// </summary>
    private static final int TICK_DELAY = 500;

    /// <summary>
    /// 밤마다 출현하는 적 수
    /// </summary>
    private static final int ENEMIES_PER_NIGHT = 5;

    /// <summary>
    /// 매일 자동 지급되는 보급품 양
    /// </summary>
    private static final int DAILY_SUPPLY = 20;

    /// <summary>
    /// 이 주기가 관리하는 맵 (적 스폰/제거용)
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 현재 일차 (1부터 시작)
    /// </summary>
    private int day;

    /// <summary>
    /// 현재 밤인지 여부 (false면 낮)
    /// </summary>
    private boolean night;

    /// <summary>
    /// 현재 페이즈(낮 또는 밤)에서 경과한 시간 (밀리초)
    /// </summary>
    private long elapsedInPhase;

    /// <summary>
    /// 스레드 실행 여부
    /// </summary>
    private volatile boolean running;

    /// <summary>
    /// 지정한 맵으로 낮/밤 주기 생성, 1일차 낮부터 시작
    /// </summary>
    public DayNightCycle(GameMap gameMap) {
        this.gameMap = gameMap;
        this.day = 1;
        this.night = false;
        this.elapsedInPhase = 0;
        this.running = true;
    }

    @Override
    public void run() {
        long lastTime = System.currentTimeMillis();

        while (running) {
            long now = System.currentTimeMillis();
            long delta = now - lastTime;
            lastTime = now;

            elapsedInPhase += delta;

            if (night) {
                // 밤: 죽은 적 제거, 적이 전멸하면 낮으로 전환
                gameMap.removeDeadEnemies();

                boolean allEnemiesDead = gameMap.getEnemies().isEmpty();
                if (allEnemiesDead) {
                    night = false;
                    day++;
                    elapsedInPhase = 0;
                    gameMap.getSupply().add(DAILY_SUPPLY);
                    gameMap.addLog("── " + day + "일차 낮 시작 (보급 +" + DAILY_SUPPLY + ") ──");
                }
            } else {
                // 낮: 시간이 다 되면 밤으로 전환
                if (elapsedInPhase >= DAY_DURATION) {
                    night = true;
                    elapsedInPhase = 0;
                    spawnEnemies();
                    gameMap.addLog("── 밤이 찾아왔습니다 ──");
                }
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
    /// 현재 일차 반환
    /// </summary>
    public int getDay() {
        return day;
    }

    /// <summary>
    /// 현재 밤인지 확인
    /// </summary>
    public boolean isNight() {
        return night;
    }

    /// <summary>
    /// 현재 페이즈의 남은 시간(초) 반환
    /// 밤에는 시간제가 아니므로 0 반환
    /// </summary>
    public int getRemainingSeconds() {
        if (night) {
            return 0;
        }
        long remaining = DAY_DURATION - elapsedInPhase;
        if (remaining < 0) {
            remaining = 0;
        }
        return (int) (remaining / 1000);
    }

    /// <summary>
    /// 맵 오른쪽 가장자리에 적을 출현시키고 스레드 시작
    /// 지면(HEIGHT-2) 위쪽 랜덤 행에 배치
    /// </summary>
    private void spawnEnemies() {
        // 지면 위 영역에서 랜덤 행 선택 (row 0 ~ HEIGHT-3)
        int maxRow = GameMap.HEIGHT - 3;

        for (int i = 0; i < ENEMIES_PER_NIGHT; i++) {
            int row = Util.rand(maxRow + 1);
            int col = GameMap.WIDTH - 1;
            Enemy enemy = new Enemy(new Position(row, col), gameMap);
            gameMap.addEnemy(enemy);
            enemy.start();
        }
    }
}
