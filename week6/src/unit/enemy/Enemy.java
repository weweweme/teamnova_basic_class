package unit.enemy;

import unit.GameEntity;
import unit.colonist.Colonist;
import game.Position;
import game.Util;
import structure.Barricade;
import game.GameWorld;
import structure.Spike;

/// <summary>
/// 밤에 오른쪽에서 출현하여 좌측으로 이동하는 적
/// 종류(EnemyType)에 따라 체력, 공격력, 속도, 외형이 다름
/// 자기 스레드에서 매 틱마다 왼쪽으로 한 칸씩 이동
/// 서브클래스(ChargerEnemy, ArmoredEnemy, RegeneratingEnemy)가 특성별 행동과 스킬을 구현
/// </summary>
public class Enemy extends GameEntity {

    /// <summary>
    /// 이 적의 종류 (타입 식별용)
    /// </summary>
    private final EnemyType type;

    /// <summary>
    /// 이 적의 속성 데이터 (체력/공격력/속도/외형 등)
    /// </summary>
    private final EnemySpec spec;

    /// <summary>
    /// 사망 시각 (0이면 살아있음, 양수면 사망 시점의 밀리초)
    /// </summary>
    private long deathTime;

    /// <summary>
    /// 특수 스킬 발동용 틱 카운터
    /// </summary>
    private int abilityTick;

    /// <summary>
    /// 지정한 종류, 속성, 위치, 맵으로 적 생성
    /// </summary>
    public Enemy(EnemyType type, EnemySpec spec, Position position, GameWorld gameWorld) {
        super(position, gameWorld, spec.getMaxHp());
        this.type = type;
        this.spec = spec;
    }

    /// <summary>
    /// 스레드 실행 루프
    /// 매 틱마다 왼쪽으로 이동, 바리케이드 도달 시 공격
    /// 바리케이드 파괴 시 정착민에게 돌진
    /// </summary>
    @Override
    public void run() {
        while (isRunning() && isLiving()) {
            // 서브클래스별 매 틱 처리 (재생 등)
            onTick();

            // 특수 스킬 카운터
            tickAbility();

            int currentCol = getPosition().getCol();
            Barricade barricade = getGameWorld().getBarricade();

            // 이동량 결정 (서브클래스에서 오버라이드 가능)
            int moveAmount = getMoveAmount();

            // 바리케이드 바로 오른쪽에서 멈추는 열
            final int BARRICADE_STOP = Barricade.COLUMN + 2;

            if (!barricade.isDestroyed()) {
                // 바리케이드 건재: 바리케이드까지 이동 후 공격
                if (currentCol > BARRICADE_STOP) {
                    int newCol = currentCol - moveAmount;
                    if (newCol < BARRICADE_STOP) {
                        newCol = BARRICADE_STOP;
                    }
                    getPosition().setCol(newCol);
                    checkSpikes();
                } else {
                    barricade.takeDamage(spec.getDamage());
                }
            } else {
                // 바리케이드 파괴: 정착민에게 돌진
                Colonist target = findNearestColonist();

                if (target != null) {
                    // 정착민 블록 오른쪽 바로 옆에서 멈춤
                    int colonistStop = target.getPosition().getCol() + target.getBlock()[0].length();

                    if (currentCol > colonistStop) {
                        int newCol = currentCol - moveAmount;
                        if (newCol < colonistStop) {
                            newCol = colonistStop;
                        }
                        getPosition().setCol(newCol);
                        checkSpikes();
                    } else {
                        target.takeDamage(spec.getDamage());
                    }

                } else if (currentCol > 0) {
                    // 살아있는 정착민 없음: 왼쪽으로 계속 이동
                    int newCol = currentCol - moveAmount;
                    if (newCol < 0) {
                        newCol = 0;
                    }
                    getPosition().setCol(newCol);
                    checkSpikes();
                }
            }

            Util.delay(spec.getTickDelay());
        }
    }

    /// <summary>
    /// 매 틱마다 호출되는 훅 메서드
    /// 서브클래스에서 오버라이드하여 패시브 효과(재생 등) 구현
    /// </summary>
    protected void onTick() {
        // 기본 적은 매 틱 추가 행동 없음
    }

    /// <summary>
    /// 이동량 반환 (기본 1칸)
    /// 서브클래스에서 오버라이드하여 돌진 등 구현
    /// </summary>
    protected int getMoveAmount() {
        return 1;
    }

    /// <summary>
    /// 특수 스킬 카운터를 증가시키고, 간격에 도달하면 specialAbility() 발동
    /// </summary>
    private void tickAbility() {
        abilityTick++;
        final int ABILITY_INTERVAL = 8;
        if (abilityTick < ABILITY_INTERVAL) {
            return;
        }
        abilityTick = 0;
        specialAbility();
    }

    /// <summary>
    /// 주기적으로 발동하는 특수 스킬
    /// 서브클래스에서 역할별 고유 효과를 구현
    /// </summary>
    protected void specialAbility() {
        // 기본 적은 특수 스킬 없음
    }

    /// <summary>
    /// 가장 가까운 살아있는 정착민 찾기 (열 기준)
    /// </summary>
    private Colonist findNearestColonist() {
        Colonist nearest = null;
        int minDist = Integer.MAX_VALUE;

        for (Colonist colonist : getGameWorld().getColonists()) {
            if (!colonist.isLiving()) {
                continue;
            }
            int dist = Math.abs(colonist.getPosition().getCol() - getPosition().getCol());
            if (dist < minDist) {
                minDist = dist;
                nearest = colonist;
            }
        }
        return nearest;
    }

    /// <summary>
    /// 현재 위치의 적 블록과 가시덫이 겹치면 피해를 받고, 가시덫 내구도 감소
    /// </summary>
    private void checkSpikes() {
        int enemyRow = getPosition().getRow();
        int enemyCol = getPosition().getCol();
        String[] block = getSpec().getBlock();
        int blockHeight = block.length;
        int blockWidth = block[0].length();

        for (Spike spike : getGameWorld().getSpikes()) {
            if (spike.isDestroyed()) {
                continue;
            }

            // 적 블록의 행/열 범위와 가시덫 위치가 겹치는지 확인
            boolean colOverlap = spike.getColumn() >= enemyCol && spike.getColumn() < enemyCol + blockWidth;
            boolean rowOverlap = spike.getRow() >= enemyRow && spike.getRow() < enemyRow + blockHeight;

            if (colOverlap && rowOverlap) {
                takeDamage(spike.getDamage());
                spike.takeDamage(1);
                break;
            }
        }
    }

    /// <summary>
    /// 최대 체력 반환
    /// </summary>
    @Override
    public int getMaxHp() {
        return spec.getMaxHp();
    }

    /// <summary>
    /// 피해를 받아 체력 감소, 사망 시 시각 기록
    /// </summary>
    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);

        if (getHp() == 0 && deathTime == 0) {
            deathTime = System.currentTimeMillis();
        }
    }

    /// <summary>
    /// 적 종류 반환
    /// </summary>
    public EnemyType getType() {
        return type;
    }

    /// <summary>
    /// 적 속성 데이터 반환
    /// </summary>
    public EnemySpec getSpec() {
        return spec;
    }

    /// <summary>
    /// 사망 시각 반환 (0이면 살아있음)
    /// </summary>
    public long getDeathTime() {
        return deathTime;
    }
}
