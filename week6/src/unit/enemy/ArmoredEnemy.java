package unit.enemy;

import game.GameWorld;
import game.HitEffect;
import game.Position;
import structure.Barricade;

/// <summary>
/// 방어 특성 적 — Enemy 서브클래스
/// 패시브: 받는 피해 50% 감소
/// 특수 스킬 "방패 충돌": 바리케이드 앞이면 2배 추가 공격, 아니면 다음 1회 피해 무효
/// </summary>
public class ArmoredEnemy extends Enemy {

    /// <summary>
    /// 다음 피해를 완전 무효화할지 여부 (방어 강화 스킬)
    /// </summary>
    private boolean shieldActive;

    /// <summary>
    /// 방어 적 생성
    /// </summary>
    public ArmoredEnemy(EnemyType type, EnemySpec spec, Position position, GameWorld gameWorld) {
        super(type, spec, position, gameWorld);
    }

    /// <summary>
    /// 피해를 받아 체력 감소 — 방어 특성으로 피해 50% 감소
    /// 방어 강화 스킬 활성화 시 1회 피해 완전 무효
    /// </summary>
    @Override
    public void takeDamage(int damage) {
        // 방어 강화 스킬: 1회 피해 완전 무효
        if (shieldActive) {
            shieldActive = false;
            return;
        }

        // 방어 특성: 피해 절반으로 감소 (최소 1)
        int actualDamage = damage;
        if (damage > 1) {
            actualDamage = damage / 2;
        }
        super.takeDamage(actualDamage);
    }

    /// <summary>
    /// 특수 스킬 — "방패 충돌"
    /// 바리케이드 앞이면 2배 추가 공격, 아니면 방어 강화 (다음 1회 피해 무효)
    /// </summary>
    @Override
    protected void specialAbility() {
        GameWorld gameWorld = getGameWorld();
        int currentCol = getPosition().getCol();
        int row = getPosition().getRow();
        long now = System.currentTimeMillis();
        final int COLOR_BLUE = 34;
        String name = getSpec().getDisplayName();

        // 바리케이드 바로 옆에 있는지 확인
        final int BARRICADE_STOP = Barricade.COLUMN + 2;
        boolean atBarricade = currentCol <= BARRICADE_STOP;

        if (atBarricade && !gameWorld.getBarricade().isDestroyed()) {
            // 바리케이드 앞: 2배 데미지 추가 공격
            final int BASH_MULTIPLIER = 2;
            gameWorld.getBarricade().takeDamage(getBuffedDamage() * BASH_MULTIPLIER);
            gameWorld.addEffect(new HitEffect(row, currentCol, now, '#', COLOR_BLUE));
            gameWorld.addLog(name + " — 방패 충돌!");
        } else {
            // 바리케이드에 도달하지 않음: 방어 강화
            shieldActive = true;
            gameWorld.addEffect(new HitEffect(row, currentCol, now, '#', COLOR_BLUE));
            gameWorld.addLog(name + " — 방어 강화!");
        }

        gameWorld.getSfxPlayer().playShieldBash();
    }
}
