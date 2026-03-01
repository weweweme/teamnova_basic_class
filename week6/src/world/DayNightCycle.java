package world;

import system.DifficultySettings;
import system.Position;
import system.Util;
import entity.colonist.Colonist;
import entity.enemy.Enemy;
import entity.enemy.EnemyType;
import entity.colonist.ShootingState;
import entity.colonist.WanderingState;

import java.util.ArrayList;
import java.util.Collections;

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
    /// <summary>
    /// 밤 전 사격 배치 시작 시점 (밀리초, 밤까지 남은 시간)
    /// </summary>
    private static final int PREPARE_DURATION = 5000;

    /// <summary>
    /// 적 시간차 스폰 최소 간격 (밀리초)
    /// </summary>
    private static final int MIN_SPAWN_DELAY = 300;

    /// <summary>
    /// 적 시간차 스폰 최대 간격 (밀리초)
    /// </summary>
    private static final int MAX_SPAWN_DELAY = 3000;

    /// <summary>
    /// 매일 자동 지급되는 보급품 양
    /// </summary>
    private static final int DAILY_SUPPLY = 20;

    /// <summary>
    /// 이 주기가 관리하는 맵 (적 스폰/제거용)
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 난이도 설정 (적 수/보급/승리 일차 조절)
    /// </summary>
    private final DifficultySettings settings;

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
    /// 밤 건너뛰기가 요청되었는지 여부 (즉시 배치용)
    /// </summary>
    private volatile boolean skipRequested;

    /// <summary>
    /// 밤 전 사격 배치가 시작되었는지 여부
    /// </summary>
    private boolean preparing;

    /// <summary>
    /// 이번 밤에 바리케이드가 파괴되었는지 여부 (중복 로그 방지)
    /// </summary>
    private boolean barricadeBroken;

    /// <summary>
    /// 승리 조건 달성 여부
    /// </summary>
    private volatile boolean victory;

    /// <summary>
    /// 아직 출현하지 않은 대기 중인 적 목록
    /// </summary>
    private final ArrayList<Enemy> pendingSpawns = new ArrayList<>();

    /// <summary>
    /// 이번 밤 웨이브의 총 적 수
    /// </summary>
    private int totalWaveSize;

    /// <summary>
    /// 다음 적이 출현할 시각 (밀리초)
    /// </summary>
    private long nextSpawnTime;

    /// <summary>
    /// 지정한 맵과 난이도로 낮/밤 주기 생성, 1일차 낮부터 시작
    /// </summary>
    public DayNightCycle(GameMap gameMap, DifficultySettings settings) {
        this.gameMap = gameMap;
        this.settings = settings;
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
                // 바리케이드 파괴 감지
                if (!barricadeBroken && gameMap.getBarricade().isDestroyed()) {
                    barricadeBroken = true;
                    gameMap.addLog("!! 바리케이드가 무너졌습니다 !!");
                }

                // 밤: 대기 중인 적 시간차 출현
                if (!pendingSpawns.isEmpty() && now >= nextSpawnTime) {
                    Enemy enemy = pendingSpawns.remove(0);
                    gameMap.addEnemy(enemy);
                    enemy.start();

                    // 다음 스폰까지 랜덤 딜레이 (MIN ~ MAX 사이)
                    int delay = MIN_SPAWN_DELAY + Util.rand(MAX_SPAWN_DELAY - MIN_SPAWN_DELAY);
                    nextSpawnTime = now + delay;
                }

                // 죽은 적 제거, 모두 출현하고 전멸하면 낮으로 전환
                gameMap.removeDeadEnemies();

                boolean allSpawned = pendingSpawns.isEmpty();
                boolean allEnemiesDead = gameMap.getEnemies().isEmpty();
                if (allSpawned && allEnemiesDead) {
                    night = false;
                    day++;
                    elapsedInPhase = 0;
                    barricadeBroken = false;
                    int dailySupply = settings.applySupply(DAILY_SUPPLY);
                    gameMap.getSupply().add(dailySupply);
                    gameMap.removeDestroyedSpikes();
                    switchToWandering();
                    if (day > settings.getWinDay()) {
                        victory = true;
                        gameMap.addLog("══ 승리! " + settings.getWinDay() + "일을 버텨냈습니다! ══");
                    } else {
                        gameMap.addLog("── " + day + "일차 낮 시작 (보급 +" + dailySupply + ") ──");
                    }
                }
            } else {
                // 낮: 시간이 다 되면 밤으로 전환
                if (elapsedInPhase >= DAY_DURATION) {
                    night = true;
                    elapsedInPhase = 0;

                    if (skipRequested || !preparing) {
                        // 건너뛰기 또는 준비 없이 밤이 된 경우: 즉시 배치
                        switchToShooting(true);
                    }

                    skipRequested = false;
                    preparing = false;
                    spawnEnemies();
                    gameMap.addLog("── 밤이 찾아왔습니다 ──");
                } else if (!preparing && DAY_DURATION - elapsedInPhase <= PREPARE_DURATION) {
                    // 밤 5초 전: 사격 위치로 이동 시작
                    preparing = true;
                    switchToShooting(false);
                    gameMap.addLog(">> 정착민들이 바리케이드로 이동합니다");
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
            skipRequested = true;
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
    /// 승리 조건을 달성했는지 확인
    /// </summary>
    public boolean isVictory() {
        return victory;
    }

    /// <summary>
    /// 이번 밤 웨이브의 총 적 수 반환
    /// </summary>
    public int getTotalWaveSize() {
        return totalWaveSize;
    }

    /// <summary>
    /// 아직 출현하지 않은 대기 중인 적 수 반환
    /// </summary>
    public int getPendingCount() {
        return pendingSpawns.size();
    }

    /// <summary>
    /// 선택된 난이도 이름 반환 (패널 표시용)
    /// </summary>
    public String getDifficultyName() {
        return settings.getDifficulty().getDisplayName();
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
    private void switchToShooting(boolean instant) {
        for (Colonist colonist : gameMap.getColonists()) {
            if (colonist.isLiving()) {
                colonist.changeState(new ShootingState(instant));
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
    /// buildWave()로 구성된 적 목록을 중앙 기준으로 배치
    /// 적끼리 위아래 1칸 이상 간격을 두고 배치
    /// </summary>
    private void spawnEnemies() {
        ArrayList<EnemyType> spawnList = buildWave();

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
            pendingSpawns.add(enemy);

            // 블록 높이 + 간격 1칸
            currentRow += blockHeight + 1;
        }

        // 총 마릿수 기록
        totalWaveSize = pendingSpawns.size();

        // 출현 순서 섞기 (위치는 고정, 등장 순서만 랜덤)
        Collections.shuffle(pendingSpawns);

        // 첫 적 출현 시간 설정
        nextSpawnTime = System.currentTimeMillis() + MIN_SPAWN_DELAY;
    }

    /// <summary>
    /// 일차에 따라 출현할 적 목록을 구성
    /// 큰 적부터 작은 적 순서로 추가 (배치 시 중앙 정렬용)
    /// 1~5일차는 고정 웨이브, 6일차부터 무작위 구성, 5일마다 보스
    /// </summary>
    private ArrayList<EnemyType> buildWave() {
        ArrayList<EnemyType> wave = new ArrayList<>();

        if (day <= 5) {
            // 1~5일차: 고정 웨이브
            switch (day) {
                case 1:
                    addToWave(wave, EnemyType.WOLF, 3);
                    break;
                case 2:
                    addToWave(wave, EnemyType.WOLF, 4);
                    addToWave(wave, EnemyType.SPIDER, 2);
                    break;
                case 3:
                    addToWave(wave, EnemyType.BEAR, 1);
                    addToWave(wave, EnemyType.WOLF, 3);
                    addToWave(wave, EnemyType.SKELETON, 3);
                    break;
                case 4:
                    addToWave(wave, EnemyType.BEAR, 1);
                    addToWave(wave, EnemyType.SCORPION, 2);
                    addToWave(wave, EnemyType.ZOMBIE, 2);
                    addToWave(wave, EnemyType.WOLF, 2);
                    break;
                case 5:
                    addToWave(wave, EnemyType.DRAGON, 1);
                    addToWave(wave, EnemyType.ORC, 1);
                    addToWave(wave, EnemyType.BANDIT, 2);
                    addToWave(wave, EnemyType.SPIDER, 2);
                    addToWave(wave, EnemyType.RAT, 2);
                    break;
            }
        } else {
            // 6일차 이후: 일차 기반 무작위 구성
            // 보스 → 강한 → 일반 순서로 추가 (큰 것부터)

            // 5일마다 보스 등장
            boolean isBossDay = day % 5 == 0;
            if (isBossDay) {
                EnemyType[] bosses = {EnemyType.DRAGON, EnemyType.GOLEM};
                int index = (int) (Math.random() * bosses.length);
                wave.add(bosses[index]);
            }

            // 강한 몬스터 (일차 / 2 마리, 난이도 배율 적용)
            EnemyType[] strongs = {EnemyType.BEAR, EnemyType.BANDIT, EnemyType.SCORPION, EnemyType.ORC};
            int strongCount = settings.applyEnemyCount(day / 2);
            for (int i = 0; i < strongCount; i++) {
                int index = (int) (Math.random() * strongs.length);
                wave.add(strongs[index]);
            }

            // 일반 몬스터 (3 + 일차 마리, 난이도 배율 적용)
            EnemyType[] normals = {EnemyType.WOLF, EnemyType.SPIDER, EnemyType.SKELETON,
                                   EnemyType.ZOMBIE, EnemyType.RAT, EnemyType.SLIME};
            int normalCount = settings.applyEnemyCount(3 + day);
            for (int i = 0; i < normalCount; i++) {
                int index = (int) (Math.random() * normals.length);
                wave.add(normals[index]);
            }
        }

        return wave;
    }

    /// <summary>
    /// 웨이브 목록에 지정한 적을 여러 마리 추가
    /// </summary>
    private void addToWave(ArrayList<EnemyType> wave, EnemyType type, int count) {
        for (int i = 0; i < count; i++) {
            wave.add(type);
        }
    }
}
