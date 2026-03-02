package gun;

import unit.colonist.Colonist;

import unit.enemy.Enemy;
import structure.Barricade;
import game.GameWorld;

/// <summary>
/// 무기 추상 클래스
/// 공통 속성(이름, 비용, 발사 간격, 총알 외형)은 생성자로 받고
/// 발사 패턴(fire)만 서브클래스에서 구현
/// </summary>
public abstract class Gun {

    /// <summary>
    /// 무기 이름 (패널 표시용)
    /// </summary>
    private final String name;

    /// <summary>
    /// 구매 비용 (0이면 기본 무기)
    /// </summary>
    private final int cost;

    /// <summary>
    /// 발사 간격 (틱 수, 1틱 = 500ms)
    /// </summary>
    private final int fireInterval;

    /// <summary>
    /// 총알 표시 문자 (렌더링용)
    /// </summary>
    private final char bulletChar;

    /// <summary>
    /// 총알 ANSI 색상 코드 (0이면 기본색)
    /// </summary>
    private final int bulletColor;

    /// <summary>
    /// 총알 1발당 피해량
    /// </summary>
    private final int damage;

    /// <summary>
    /// 총알 이동 속도
    /// </summary>
    private final int bulletSpeed;

    /// <summary>
    /// 공통 속성을 지정하여 무기 생성
    /// </summary>
    protected Gun(String name, int cost, int fireInterval, char bulletChar, int bulletColor,
                  int damage, int bulletSpeed) {
        this.name = name;
        this.cost = cost;
        this.fireInterval = fireInterval;
        this.bulletChar = bulletChar;
        this.bulletColor = bulletColor;
        this.damage = damage;
        this.bulletSpeed = bulletSpeed;
    }

    /// <summary>
    /// 무기 이름 반환
    /// </summary>
    public String getName() {
        return name;
    }

    /// <summary>
    /// 구매 비용 반환
    /// </summary>
    public int getCost() {
        return cost;
    }

    /// <summary>
    /// 발사 간격 반환
    /// </summary>
    public int getFireInterval() {
        return fireInterval;
    }

    /// <summary>
    /// 총알을 생성하여 맵에 추가
    /// 서브클래스마다 발사 패턴이 다름 (단발, 산탄, 관통 등)
    /// </summary>
    public abstract void fire(Colonist colonist, Enemy target, GameWorld gameWorld);

    /// <summary>
    /// 발사 효과음 재생
    /// 무기별로 오버라이드하여 고유 발사음 사용 가능
    /// </summary>
    public void playFireSound(game.SfxPlayer sfxPlayer) {
        sfxPlayer.playShoot();
    }

    /// <summary>
    /// 총알 표시 문자 반환
    /// </summary>
    public char getBulletChar() {
        return bulletChar;
    }

    /// <summary>
    /// 총알 ANSI 색상 코드 반환
    /// </summary>
    public int getBulletColor() {
        return bulletColor;
    }

    /// <summary>
    /// 피해량 반환
    /// </summary>
    public int getDamage() {
        return damage;
    }

    /// <summary>
    /// 총알 이동 속도 반환
    /// </summary>
    public int getBulletSpeed() {
        return bulletSpeed;
    }

    /// <summary>
    /// 공통 발사 로직: 적 중앙 조준 → 크리티컬 → 넉백 → 총알 생성
    /// aimRowOffset으로 조준점을 상하로 이동 가능 (샷건 산탄용)
    /// </summary>
    protected void fireBullet(Colonist colonist, Enemy target, GameWorld gameWorld,
                              boolean piercing, int aimRowOffset) {
        int bulletRow = colonist.getPosition().getRow();
        int bulletCol = Barricade.COLUMN + 2;

        // 적 블록 중앙을 조준
        String[] block = target.getSpec().getBlock();
        int aimRow = target.getPosition().getRow() + block.length / 2 + aimRowOffset;
        int aimCol = target.getPosition().getCol() + block[0].length() / 2;

        // 치명타 판정
        double critChance = colonist.getSpec().getCritChance();
        boolean isCrit = critChance > 0 && Math.random() < critChance;
        int finalDamage = isCrit ? damage * 2 : damage;

        int kb = getKnockback(colonist);
        Bullet bullet = new Bullet(
            bulletRow, bulletCol, aimRow, aimCol, finalDamage,
            colonist.getLabel(), colonist.getColonistName(), bulletSpeed, getBulletChar(), getBulletColor(), piercing, kb, isCrit
        );
        gameWorld.addBullet(bullet);
    }

    /// <summary>
    /// 정착민의 넉백 패시브 값 반환
    /// </summary>
    private int getKnockback(Colonist colonist) {
        return colonist.getSpec().getKnockback();
    }
}
