package system;

import java.util.ArrayList;

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
                    switchToWandering();
                    gameMap.addLog("── " + day + "일차 낮 시작 (보급 +" + DAILY_SUPPLY + ") ──");
                }
            } else {
                // 낮: 시간이 다 되면 밤으로 전환
                if (elapsedInPhase >= DAY_DURATION) {
                    night = true;
                    elapsedInPhase = 0;
                    switchToShooting();
                    spawnEnemies();
                    gameMap.addLog("── 밤이 찾아왔습니다 ──");
                }
            }

            Util.delay(TICK_DELAY);
        }
    }

    /// <summary>
    /// 낮을 건너뛰고 즉시 밤으로 전환 (낮일 때만 동작)
    /// </summary>
    public void skipToNight() {
        if (!night) {
            elapsedInPhase = DAY_DURATION;
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
    /// 살아있는 모든 정착민을 사격 상태로 전환
    /// 각 정착민마다 별도 상태 객체 생성 (tickCount 등 개별 관리)
    /// </summary>
    private void switchToShooting() {
        for (Colonist colonist : gameMap.getColonists()) {
            if (colonist.isLiving()) {
                colonist.changeState(new ShootingState());
            }
        }
    }

    /// <summary>
    /// 살아있는 모든 정착민을 배회 상태로 전환
    /// </summary>
    private void switchToWandering() {
        for (Colonist colonist : gameMap.getColonists()) {
            if (colonist.isLiving()) {
                colonist.changeState(new WanderingState());
            }
        }
    }

    /// <summary>
    /// 맵 오른쪽 가장자리에 적을 출현시키고 스레드 시작
    /// 일차에 따라 등장하는 몬스터 종류와 수가 달라짐
    /// 적끼리 위아래 1칸 이상 간격을 두고 배치
    /// </summary>
    private void spawnEnemies() {
        // 일차별 스폰 구성: 초반엔 늑대만, 점차 거미/곰 추가
        int wolfCount = 3 + day;
        int spiderCount = Math.max(0, day - 1);
        int bearCount = Math.max(0, (day - 2) / 2);

        // 스폰할 적 목록 모으기 (곰 → 늑대 → 거미 순서로 큰 것부터 배치)
        ArrayList<EnemyType> spawnList = new ArrayList<>();
        for (int i = 0; i < bearCount; i++) {
            spawnList.add(EnemyType.BEAR);
        }
        for (int i = 0; i < wolfCount; i++) {
            spawnList.add(EnemyType.WOLF);
        }
        for (int i = 0; i < spiderCount; i++) {
            spawnList.add(EnemyType.SPIDER);
        }

        if (spawnList.isEmpty()) {
            return;
        }

        // 전체 필요 높이 계산 (블록 높이 합 + 간격)
        int totalHeight = 0;
        for (EnemyType type : spawnList) {
            totalHeight += type.getBlock().length;
        }
        // 적 사이 간격 (마지막 적 아래는 제외)
        totalHeight += spawnList.size() - 1;

        // 중앙 기준으로 시작 행 계산
        int startRow = (GameMap.HEIGHT - totalHeight) / 2;
        if (startRow < 0) {
            startRow = 0;
        }

        int currentRow = startRow;
        for (EnemyType type : spawnList) {
            int blockHeight = type.getBlock().length;

            // 맵 높이를 넘으면 맨 위부터 다시 배치
            int maxRow = GameMap.HEIGHT - blockHeight;
            if (currentRow > maxRow) {
                currentRow = 0;
            }

            int col = GameMap.WIDTH - 1;
            Enemy enemy = new Enemy(type, new Position(currentRow, col), gameMap);
            gameMap.addEnemy(enemy);
            enemy.start();

            // 블록 높이 + 간격 1칸
            currentRow += blockHeight + 1;
        }
    }
}
