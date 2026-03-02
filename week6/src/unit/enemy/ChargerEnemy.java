package unit.enemy;

import game.GameWorld;
import game.HitEffect;
import game.Position;
import structure.Barricade;

/// <summary>
/// 돌진 특성 적 — Enemy 서브클래스
/// 패시브: 바리케이드 근처에서 2칸 이동
/// 특수 스킬 "돌진 질주": 3칸 추가 전진 + 잔상 이펙트
/// </summary>
public class ChargerEnemy extends Enemy {

    /// <summary>
    /// 돌진 적 생성
    /// </summary>
    public ChargerEnemy(EnemyType type, EnemySpec spec, Position position, GameWorld gameWorld) {
        super(type, spec, position, gameWorld);
    }

    /// <summary>
    /// 이동량 반환 — 바리케이드 근처에서 2칸 이동
    /// </summary>
    @Override
    protected int getMoveAmount() {
        int currentCol = getPosition().getCol();

        // 바리케이드 바로 오른쪽에서 멈추는 열
        final int BARRICADE_STOP = Barricade.COLUMN + 2;

        // 돌진 특성이 발동하는 바리케이드까지의 거리 (열 수)
        final int CHARGE_RANGE = 8;
        boolean nearBarricade = currentCol - BARRICADE_STOP <= CHARGE_RANGE;
        return nearBarricade ? 2 : 1;
    }

    /// <summary>
    /// 특수 스킬 — "돌진 질주"
    /// 3칸 추가 전진 + 이동 경로에 노란색 잔상 이펙트
    /// </summary>
    @Override
    protected void specialAbility() {
        GameWorld gameWorld = getGameWorld();
        int currentCol = getPosition().getCol();
        int row = getPosition().getRow();

        // 3칸 추가 전진 (바리케이드 너머로 넘어가지 않도록 제한)
        final int RUSH_DISTANCE = 3;
        final int BARRICADE_STOP = Barricade.COLUMN + 2;
        int newCol = currentCol - RUSH_DISTANCE;
        if (newCol < BARRICADE_STOP) {
            newCol = BARRICADE_STOP;
        }
        getPosition().setCol(newCol);

        // 이동 경로에 노란색 '>' 잔상 이펙트
        long now = System.currentTimeMillis();
        final int COLOR_YELLOW = 33;
        for (int col = currentCol; col > newCol; col--) {
            gameWorld.addEffect(new HitEffect(row, col, now, '>', COLOR_YELLOW));
        }

        gameWorld.getSfxPlayer().playChargeRush();
        String name = getSpec().getDisplayName();
        gameWorld.addLog(name + " — 돌진 질주!");
    }
}
