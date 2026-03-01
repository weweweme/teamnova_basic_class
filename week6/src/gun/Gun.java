package gun;

import entity.colonist.Colonist;

import entity.enemy.Enemy;
import structure.Barricade;
import game.GameMap;

/// <summary>
/// 무기 추상 클래스
/// 모든 무기의 공통 인터페이스를 정의
/// 서브클래스가 발사 패턴, 데미지, 총알 외형을 결정
/// </summary>
public abstract class Gun {

    /// <summary>
    /// 무기 이름 반환 (패널 표시용)
    /// </summary>
    public abstract String getName();

    /// <summary>
    /// 구매 비용 반환 (0이면 기본 무기)
    /// </summary>
    public abstract int getCost();

    /// <summary>
    /// 발사 간격 반환 (틱 수, 1틱 = 500ms)
    /// </summary>
    public abstract int getFireInterval();

    /// <summary>
    /// 총알을 생성하여 맵에 추가
    /// 서브클래스마다 발사 패턴이 다름 (단발, 산탄, 관통 등)
    /// </summary>
    public abstract void fire(Colonist colonist, Enemy target, GameMap gameMap);

    /// <summary>
    /// 총알 표시 문자 반환 (렌더링용)
    /// </summary>
    public abstract char getBulletChar();

    /// <summary>
    /// 총알 ANSI 색상 코드 반환 (0이면 기본색)
    /// </summary>
    public abstract int getBulletColor();

    /// <summary>
    /// 공통 발사 로직: 적 중앙 조준 → 크리티컬 → 넉백 → 총알 생성
    /// aimRowOffset으로 조준점을 상하로 이동 가능 (샷건 산탄용)
    /// </summary>
    protected void fireBullet(Colonist colonist, Enemy target, GameMap gameMap,
                              int damage, int speed, boolean piercing, int aimRowOffset) {
        int bulletRow = colonist.getPosition().getRow();
        int bulletCol = Barricade.COLUMN + 2;

        // 적 블록 중앙을 조준
        String[] block = target.getSpec().getBlock();
        int aimRow = target.getPosition().getRow() + block.length / 2 + aimRowOffset;
        int aimCol = target.getPosition().getCol() + block[0].length() / 2;

        int finalDamage = applyCrit(damage, colonist);
        int kb = getKnockback(colonist);
        Bullet bullet = new Bullet(
            bulletRow, bulletCol, aimRow, aimCol, finalDamage,
            colonist.getLabel(), speed, getBulletChar(), getBulletColor(), piercing, kb
        );
        gameMap.addBullet(bullet);
    }

    /// <summary>
    /// 정착민의 치명타 패시브 적용 (확률에 따라 데미지 2배)
    /// </summary>
    private int applyCrit(int baseDamage, Colonist colonist) {
        double critChance = colonist.getSpec().getCritChance();
        boolean isCrit = critChance > 0 && Math.random() < critChance;
        if (isCrit) {
            return baseDamage * 2;
        }
        return baseDamage;
    }

    /// <summary>
    /// 정착민의 넉백 패시브 값 반환
    /// </summary>
    private int getKnockback(Colonist colonist) {
        return colonist.getSpec().getKnockback();
    }
}
