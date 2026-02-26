package system;

/// <summary>
/// 정착민 한 명을 나타내는 클래스
/// 각 정착민은 자기 스레드에서 자율적으로 행동
/// 상태 패턴으로 행동을 관리 (IdleState, MovingState 등)
/// </summary>
public class Colonist extends Thread {

    /// <summary>
    /// 최대 체력
    /// </summary>
    private static final int MAX_HP = 100;

    /// <summary>
    /// 최대 피로도 (이 값에 도달하면 쓰러짐)
    /// </summary>
    private static final int MAX_FATIGUE = 100;

    /// <summary>
    /// 반격 공격력
    /// </summary>
    private static final int ATTACK_DAMAGE = 3;

    /// <summary>
    /// 반격 범위 (맨해튼 거리)
    /// </summary>
    private static final int ATTACK_RANGE = 2;

    /// <summary>
    /// 행동 틱 간격 (밀리초)
    /// </summary>
    private static final int TICK_DELAY = 500;

    /// <summary>
    /// 식량 소비 주기 (틱 수, 10초 = 20틱)
    /// </summary>
    private static final int HUNGER_INTERVAL = 20;

    /// <summary>
    /// 굶주림 상태에서 틱당 HP 감소량
    /// </summary>
    private static final int STARVATION_DAMAGE = 1;

    /// <summary>
    /// 다음에 부여할 알파벳 (A부터 순서대로)
    /// </summary>
    private static char nextLabel = 'A';

    /// <summary>
    /// 정착민 이름 (한글, 패널에서만 표시)
    /// </summary>
    private final String name;

    /// <summary>
    /// 맵에 표시할 알파벳 라벨 (A, B, C 순서)
    /// </summary>
    private final char label;

    /// <summary>
    /// 맵 위의 위치
    /// </summary>
    private final Position position;

    /// <summary>
    /// 이 정착민이 속한 맵 (이동 시 범위/충돌 체크용)
    /// </summary>
    private final GameMap gameMap;

    /// <summary>
    /// 현재 체력
    /// </summary>
    private int hp;

    /// <summary>
    /// 현재 피로도 (높을수록 지친 상태)
    /// </summary>
    private int fatigue;

    /// <summary>
    /// 현재 행동 상태
    /// </summary>
    private ColonistState currentState;

    /// <summary>
    /// 식량 소비 카운터 (HUNGER_INTERVAL에 도달하면 식량 1 소비)
    /// </summary>
    private int hungerTicks;

    /// <summary>
    /// 굶주림 상태인지 여부 (식량 부족 시 true)
    /// </summary>
    private boolean starving;

    /// <summary>
    /// 스레드 실행 여부 (false가 되면 스레드 종료)
    /// </summary>
    private volatile boolean running;

    /// <summary>
    /// 지정한 이름, 위치, 맵으로 정착민 생성
    /// 체력은 최대, 피로도는 0, 대기 상태로 시작
    /// </summary>
    public Colonist(String name, Position position, GameMap gameMap) {
        this.name = name;
        this.label = nextLabel;
        nextLabel++;
        this.position = position;
        this.gameMap = gameMap;
        this.hp = MAX_HP;
        this.fatigue = 0;
        this.hungerTicks = 0;
        this.starving = false;
        this.currentState = new IdleState();
        this.running = true;
    }

    /// <summary>
    /// 스레드 실행 루프
    /// 매 틱마다 현재 상태의 update를 호출
    /// </summary>
    @Override
    public void run() {
        currentState.enter(this);

        while (running && isLiving()) {
            currentState.update(this);
            attackNearbyEnemy();
            consumeFood();
            Util.delay(TICK_DELAY);
        }
    }

    /// <summary>
    /// 범위 내 가장 가까운 적에게 자동 반격
    /// </summary>
    private void attackNearbyEnemy() {
        Enemy closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Enemy enemy : gameMap.getEnemies()) {
            if (!enemy.isLiving()) {
                continue;
            }

            int distance = position.distanceTo(enemy.getPosition());
            if (distance <= ATTACK_RANGE && distance < minDistance) {
                minDistance = distance;
                closest = enemy;
            }
        }

        if (closest != null) {
            closest.takeDamage(ATTACK_DAMAGE);
        }
    }

    /// <summary>
    /// 일정 주기마다 식량을 1 소비, 식량이 없으면 굶주림으로 HP 감소
    /// </summary>
    private void consumeFood() {
        hungerTicks++;

        if (hungerTicks >= HUNGER_INTERVAL) {
            hungerTicks = 0;

            Supply supply = gameMap.getSupply();
            if (supply.consumeFood()) {
                // 식량 소비 성공 — 굶주림 해제
                starving = false;
            } else {
                // 식량 부족 — 굶주림 상태
                starving = true;
            }
        }

        // 굶주림 상태면 틱마다 HP 감소
        if (starving) {
            hp = Math.max(hp - STARVATION_DAMAGE, 0);
        }
    }

    /// <summary>
    /// 상태 전환
    /// 현재 상태의 exit → 새 상태의 enter 순서로 호출
    /// </summary>
    public void changeState(ColonistState newState) {
        currentState.exit(this);
        currentState = newState;
        currentState.enter(this);
    }

    /// <summary>
    /// 스레드를 안전하게 종료
    /// </summary>
    public void stopRunning() {
        running = false;
    }

    /// <summary>
    /// 정착민 이름 반환
    /// </summary>
    public String getColonistName() {
        return name;
    }

    /// <summary>
    /// 맵 표시용 알파벳 라벨 반환
    /// </summary>
    public char getLabel() {
        return label;
    }

    /// <summary>
    /// 위치 반환
    /// </summary>
    public Position getPosition() {
        return position;
    }

    /// <summary>
    /// 이 정착민이 속한 맵 반환
    /// </summary>
    public GameMap getGameMap() {
        return gameMap;
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
    /// 현재 피로도 반환
    /// </summary>
    public int getFatigue() {
        return fatigue;
    }

    /// <summary>
    /// 최대 피로도 반환
    /// </summary>
    public int getMaxFatigue() {
        return MAX_FATIGUE;
    }

    /// <summary>
    /// 피로도를 지정한 양만큼 증가 (최대치 초과 방지)
    /// </summary>
    public void addFatigue(int amount) {
        fatigue = Math.min(fatigue + amount, MAX_FATIGUE);
    }

    /// <summary>
    /// 피로도를 지정한 양만큼 감소 (0 미만 방지)
    /// </summary>
    public void reduceFatigue(int amount) {
        fatigue = Math.max(fatigue - amount, 0);
    }

    /// <summary>
    /// 피로가 최대치에 도달했는지 확인
    /// </summary>
    public boolean isExhausted() {
        return fatigue >= MAX_FATIGUE;
    }

    /// <summary>
    /// 현재 상태 반환
    /// </summary>
    public ColonistState getCurrentState() {
        return currentState;
    }

    /// <summary>
    /// 체력이 남아있는지 확인
    /// </summary>
    public boolean isLiving() {
        return hp > 0;
    }

    /// <summary>
    /// 굶주림 상태인지 확인
    /// </summary>
    public boolean isStarving() {
        return starving;
    }

    /// <summary>
    /// 피해를 받아 체력 감소
    /// </summary>
    public void takeDamage(int damage) {
        hp = Math.max(hp - damage, 0);
    }
}
