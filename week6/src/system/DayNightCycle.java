package system;

/// <summary>
/// 낮/밤 주기를 관리하는 스레드
/// 낮(40초)과 밤(20초)을 반복하며, 현재 시간 상태를 제공
/// </summary>
public class DayNightCycle extends Thread {

    /// <summary>
    /// 낮 지속 시간 (밀리초)
    /// </summary>
    private static final int DAY_DURATION = 40000;

    /// <summary>
    /// 밤 지속 시간 (밀리초)
    /// </summary>
    private static final int NIGHT_DURATION = 20000;

    /// <summary>
    /// 틱 간격 (밀리초)
    /// </summary>
    private static final int TICK_DELAY = 500;

    /// <summary>
    /// 밤마다 출현하는 적 수
    /// </summary>
    private static final int ENEMIES_PER_NIGHT = 5;

    /// <summary>
    /// 새 정착민 합류 확률 (퍼센트, 0~100)
    /// </summary>
    private static final int NEW_COLONIST_CHANCE = 30;

    /// <summary>
    /// 자원 드랍 확률 (퍼센트, 0~100)
    /// </summary>
    private static final int RESOURCE_DROP_CHANCE = 50;

    /// <summary>
    /// 최대 정착민 수
    /// </summary>
    private static final int MAX_COLONISTS = 6;

    /// <summary>
    /// 자원 드랍 시 추가되는 자원 수
    /// </summary>
    private static final int RESOURCE_DROP_COUNT = 3;

    /// <summary>
    /// 새 정착민 이름 후보
    /// </summary>
    private static final String[] COLONIST_NAMES = {
        "정하늘", "최은수", "강도윤", "윤서진",
        "한지민", "송우진", "임수빈", "오태현"
    };

    /// <summary>
    /// 이 주기가 관리하는 맵 (적 스폰/제거용)
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 맵 생성기 (자원 드랍용)
    /// </summary>
    private final MapGenerator mapGenerator;

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
    /// 현재 이벤트 메시지 (없으면 null)
    /// </summary>
    private volatile String eventMessage;

    /// <summary>
    /// 이벤트 메시지 남은 표시 틱 수
    /// </summary>
    private int eventMessageTicks;

    /// <summary>
    /// 지정한 맵으로 낮/밤 주기 생성, 1일차 낮부터 시작
    /// </summary>
    public DayNightCycle(GameMap gameMap, MapGenerator mapGenerator) {
        this.gameMap = gameMap;
        this.mapGenerator = mapGenerator;
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

            // 현재 페이즈 지속 시간 초과 시 전환
            int currentDuration = night ? NIGHT_DURATION : DAY_DURATION;
            if (elapsedInPhase >= currentDuration) {
                elapsedInPhase = 0;

                if (night) {
                    // 밤 → 낮 (새로운 일차), 남은 적 제거
                    gameMap.clearEnemies();
                    night = false;
                    day++;
                    mapGenerator.respawnResources();
                    triggerDayEvents();
                    gameMap.addLog("── " + day + "일차 낮 시작 ──");
                } else {
                    // 낮 → 밤, 적 출현
                    night = true;
                    spawnEnemies();
                    gameMap.addLog("── 밤이 찾아왔습니다 ──");
                }
            }

            // 밤이면 방어탑 공격 + 죽은 적 제거
            if (night) {
                gameMap.towerAttack();
                gameMap.removeDeadEnemies();
            }

            // 이벤트 메시지 표시 시간 차감
            if (eventMessageTicks > 0) {
                eventMessageTicks--;
                if (eventMessageTicks <= 0) {
                    eventMessage = null;
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
    /// </summary>
    public int getRemainingSeconds() {
        int currentDuration = night ? NIGHT_DURATION : DAY_DURATION;
        long remaining = currentDuration - elapsedInPhase;
        if (remaining < 0) {
            remaining = 0;
        }
        return (int) (remaining / 1000);
    }

    /// <summary>
    /// 현재 이벤트 메시지 반환 (없으면 null)
    /// </summary>
    public String getEventMessage() {
        return eventMessage;
    }

    /// <summary>
    /// 새로운 날이 시작될 때 랜덤 이벤트 발생
    /// 새 정착민 합류 또는 자원 드랍 중 하나만 발생
    /// </summary>
    private void triggerDayEvents() {
        // 새 정착민 합류 판정
        int livingCount = 0;
        for (Colonist colonist : gameMap.getColonists()) {
            if (colonist.isLiving()) {
                livingCount++;
            }
        }

        boolean canAddColonist = livingCount < MAX_COLONISTS;
        boolean colonistRoll = Util.rand(100) < NEW_COLONIST_CHANCE;

        if (canAddColonist && colonistRoll) {
            spawnNewColonist();
            return;
        }

        // 자원 드랍 판정
        boolean resourceRoll = Util.rand(100) < RESOURCE_DROP_CHANCE;
        if (resourceRoll) {
            dropResources();
        }
    }

    /// <summary>
    /// 새 정착민을 맵 가장자리에 합류시키고 스레드 시작
    /// </summary>
    private void spawnNewColonist() {
        String name = COLONIST_NAMES[Util.rand(COLONIST_NAMES.length)];
        Position spawnPos = randomEdgePosition();
        Colonist newColonist = new Colonist(name, spawnPos, gameMap);
        gameMap.addColonist(newColonist);
        newColonist.start();

        eventMessage = name + " 합류!";
        eventMessageTicks = 10;
        gameMap.addLog(">> " + name + " 합류!");
    }

    /// <summary>
    /// 맵에 랜덤 자원을 추가로 배치
    /// </summary>
    private void dropResources() {
        ResourceType[] types = {ResourceType.FOOD, ResourceType.TREE, ResourceType.ROCK, ResourceType.IRON};

        for (int i = 0; i < RESOURCE_DROP_COUNT; i++) {
            ResourceType type = types[Util.rand(types.length)];
            mapGenerator.respawnResource(type);
        }

        eventMessage = "자원 발견!";
        eventMessageTicks = 10;
        gameMap.addLog(">> 자원 발견!");
    }

    /// <summary>
    /// 맵 가장자리 랜덤 위치에 적을 출현시키고 스레드 시작
    /// </summary>
    private void spawnEnemies() {
        for (int i = 0; i < ENEMIES_PER_NIGHT; i++) {
            Position spawnPos = randomEdgePosition();
            Enemy enemy = new Enemy(spawnPos, gameMap);
            gameMap.addEnemy(enemy);
            enemy.start();
        }
    }

    /// <summary>
    /// 맵 가장자리(상/하/좌/우)에서 랜덤 위치 생성
    /// </summary>
    private Position randomEdgePosition() {
        // 4방향 중 랜덤 선택 (0=상, 1=하, 2=좌, 3=우)
        int side = Util.rand(4);

        switch (side) {
            case 0:
                // 상단
                return new Position(0, Util.rand(GameMap.WIDTH));
            case 1:
                // 하단
                return new Position(GameMap.HEIGHT - 1, Util.rand(GameMap.WIDTH));
            case 2:
                // 좌측
                return new Position(Util.rand(GameMap.HEIGHT), 0);
            default:
                // 우측
                return new Position(Util.rand(GameMap.HEIGHT), GameMap.WIDTH - 1);
        }
    }
}
