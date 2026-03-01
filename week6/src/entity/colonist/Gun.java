package entity.colonist;

import entity.enemy.Enemy;
import world.GameMap;

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
    /// 정착민의 치명타 패시브 적용 (확률에 따라 데미지 2배)
    /// </summary>
    protected int applyCrit(int baseDamage, Colonist colonist) {
        double critChance = colonist.getType().getCritChance();
        boolean isCrit = critChance > 0 && Math.random() < critChance;
        if (isCrit) {
            return baseDamage * 2;
        }
        return baseDamage;
    }

    /// <summary>
    /// 정착민의 넉백 패시브 값 반환
    /// </summary>
    protected int getKnockback(Colonist colonist) {
        return colonist.getType().getKnockback();
    }
}
