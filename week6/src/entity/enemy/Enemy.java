package entity.enemy;

import entity.GameEntity;
import entity.colonist.Colonist;
import game.Position;
import game.Util;
import structure.Barricade;
import game.GameMap;
import structure.Spike;

/// <summary>
/// 밤에 오른쪽에서 출현하여 좌측으로 이동하는 적
/// 종류(EnemyType)에 따라 체력, 공격력, 속도, 외형이 다름
/// 자기 스레드에서 매 틱마다 왼쪽으로 한 칸씩 이동
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
    /// 재생 특성용 틱 카운터
    /// </summary>
    private int regenTick;

    /// <summary>
    /// 재생 간격 (틱 수)
    /// </summary>
    private static final int REGEN_INTERVAL = 3;

    /// <summary>
    /// 돌진 특성이 발동하는 바리케이드까지의 거리 (열 수)
    /// </summary>
    private static final int CHARGE_RANGE = 8;

    /// <summary>
    /// 지정한 종류, 속성, 위치, 맵으로 적 생성
    /// </summary>
    public Enemy(EnemyType type, EnemySpec spec, Position position, GameMap gameMap) {
        super(position, gameMap, spec.getMaxHp());
        this.type = type;
        this.spec = spec;
    }

    /// <summary>
    /// 바리케이드 바로 오른쪽에서 멈추는 열
    /// </summary>
    private static final int BARRICADE_STOP = Barricade.COLUMN + 2;

    /// <summary>
    /// 정착민 블록 가로 크기 (공격 멈춤 거리 계산용)
    /// </summary>
    private static final int COLONIST_BLOCK_WIDTH = 3;

    /// <summary>
    /// 스레드 실행 루프
    /// 매 틱마다 왼쪽으로 이동, 바리케이드 도달 시 공격
    /// 바리케이드 파괴 시 정착민에게 돌진
    /// </summary>
    @Override
    public void run() {
        while (isRunning() && isLiving()) {
            int currentCol = getPosition().getCol();
            Barricade barricade = getGameMap().getBarricade();

            // 재생 특성: 일정 틱마다 체력 1 회복
            if (spec.getTrait() == EnemyTrait.REGENERATING) {
                regenTick++;
                if (regenTick >= REGEN_INTERVAL) {
                    regenTick = 0;
                    if (getHp() < getMaxHp()) {
                        heal(1);
                    }
                }
            }

            // 이동량 결정 (돌진 특성: 바리케이드 근처에서 2칸 이동)
            int moveAmount = 1;
            if (spec.getTrait() == EnemyTrait.CHARGER) {
                boolean nearBarricade = currentCol - BARRICADE_STOP <= CHARGE_RANGE;
                if (nearBarricade) {
                    moveAmount = 2;
                }
            }

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
                    int colonistStop = target.getPosition().getCol() + COLONIST_BLOCK_WIDTH;

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
    /// 가장 가까운 살아있는 정착민 찾기 (열 기준)
    /// </summary>
    private Colonist findNearestColonist() {
        Colonist nearest = null;
        int minDist = Integer.MAX_VALUE;

        for (Colonist colonist : getGameMap().getColonists()) {
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
    /// 현재 위치에 가시덫이 있으면 피해를 받고, 가시덫 내구도 감소
    /// </summary>
    private void checkSpikes() {
        int col = getPosition().getCol();

        for (Spike spike : getGameMap().getSpikes()) {
            if (spike.isDestroyed()) {
                continue;
            }
            if (spike.getColumn() == col) {
                takeDamage(spike.getSpikeDamage());
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
    /// 방어 특성은 받는 피해가 50% 감소
    /// </summary>
    @Override
    public void takeDamage(int damage) {
        // 방어 특성: 피해 절반으로 감소 (최소 1)
        int actualDamage = damage;
        if (spec.getTrait() == EnemyTrait.ARMORED && damage > 1) {
            actualDamage = damage / 2;
        }

        super.takeDamage(actualDamage);

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
