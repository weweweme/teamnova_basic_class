package game;

import gun.Bullet;
import unit.enemy.Enemy;
import game.HitEffect;

import java.util.ArrayList;

/// <summary>
/// 총알의 이동과 충돌 처리를 담당하는 시스템
/// 충돌은 총알과 적, 두 객체 사이의 판정이므로
/// Bullet 내부가 아닌 이 시스템에서 처리
/// 스레드 안전은 GameWorld이 보장 (이 클래스의 메서드는 동기화하지 않음)
/// </summary>
public class BulletSystem {

    /// <summary>
    /// 날아가는 총알 목록
    /// </summary>
    private final ArrayList<Bullet> bullets = new ArrayList<>();

    /// <summary>
    /// 총알 복사본 재사용 버퍼 (렌더링용)
    /// </summary>
    private final ArrayList<Bullet> bulletsCopy = new ArrayList<>();

    /// <summary>
    /// 제거 대상 총알 재사용 버퍼
    /// </summary>
    private final ArrayList<Bullet> bulletsToRemove = new ArrayList<>();

    /// <summary>
    /// 총알 추가
    /// </summary>
    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    /// <summary>
    /// 현재 총알 목록 복사본 반환 (렌더링용)
    /// </summary>
    public ArrayList<Bullet> getBullets() {
        bulletsCopy.clear();
        bulletsCopy.addAll(bullets);
        return bulletsCopy;
    }

    /// <summary>
    /// 모든 총알을 전진시키고, 적과 충돌 검사
    /// 적에 명중하면 피해를 주고 총알 제거, 화면 밖이면 제거
    /// </summary>
    public void advanceBullets(ArrayList<Enemy> enemies, ArrayList<HitEffect> effects, GameWorld gameWorld) {
        bulletsToRemove.clear();

        for (Bullet bullet : bullets) {
            bullet.advance();

            // 화면 밖으로 나간 총알 제거
            if (bullet.isOffScreen()) {
                bulletsToRemove.add(bullet);
                continue;
            }

            // 적과 충돌 검사
            for (Enemy enemy : enemies) {
                if (!enemy.isLiving()) {
                    continue;
                }

                // 적 블록 범위 계산
                String[] block = enemy.getSpec().getBlock();
                int enemyRow = enemy.getPosition().getRow();
                int enemyCol = enemy.getPosition().getCol();
                int blockHeight = block.length;
                int blockWidth = block[0].length();

                // 총알이 적 블록 범위 안에 있으면 명중
                boolean hitRow = bullet.getRow() >= enemyRow && bullet.getRow() < enemyRow + blockHeight;
                boolean hitCol = bullet.getCol() >= enemyCol && bullet.getCol() < enemyCol + blockWidth;

                if (hitRow && hitCol) {
                    enemy.takeDamage(bullet.getDamage());

                    // 넉백 적용 (명중 시 적을 오른쪽으로 밀어냄)
                    if (bullet.getKnockback() > 0 && enemy.isLiving()) {
                        int newCol = enemy.getPosition().getCol() + bullet.getKnockback();
                        if (newCol < GameWorld.WIDTH) {
                            enemy.getPosition().setCol(newCol);
                        }
                    }

                    // 명중 효과음
                    SfxPlayer sfx = gameWorld.getSfxPlayer();
                    if (bullet.isCrit()) {
                        sfx.playCrit();
                    } else {
                        sfx.playHit();
                    }

                    // 명중 이펙트 (치명타면 밝은 빨강 ★, 일반이면 무기별 문자/색상)
                    long now = System.currentTimeMillis();
                    if (bullet.isCrit()) {
                        // 치명타: 중심 ★ + 상하 * 로 강조
                        final int COLOR_BRIGHT_RED = 91;
                        effects.add(new HitEffect(bullet.getRow(), bullet.getCol(), now, '*', COLOR_BRIGHT_RED));
                        // 위쪽 이펙트
                        boolean aboveValid = bullet.getRow() - 1 >= 0;
                        if (aboveValid) {
                            effects.add(new HitEffect(bullet.getRow() - 1, bullet.getCol(), now, '*', COLOR_BRIGHT_RED));
                        }
                        // 아래쪽 이펙트
                        boolean belowValid = bullet.getRow() + 1 < GameWorld.HEIGHT;
                        if (belowValid) {
                            effects.add(new HitEffect(bullet.getRow() + 1, bullet.getCol(), now, '*', COLOR_BRIGHT_RED));
                        }
                    } else {
                        char hitChar = bullet.getBulletChar() == '*' ? '!' : bullet.getBulletChar();
                        int hitColor = bullet.getBulletColor() != 0 ? bullet.getBulletColor() : 33;
                        effects.add(new HitEffect(bullet.getRow(), bullet.getCol(), now, hitChar, hitColor));
                    }

                    // 적 처치 시 로그 + 사망음 (치명타 처치면 강조)
                    if (!enemy.isLiving()) {
                        sfx.playDeath();
                        String enemyName = enemy.getSpec().getDisplayName();
                        String critMark = bullet.isCrit() ? " 치명타!" : "";
                        gameWorld.addLog("[" + bullet.getShooterLabel() + "] " + enemyName + " 처치!" + critMark);
                    }

                    // 관통 총알은 제거하지 않고 계속 전진
                    if (!bullet.isPiercing()) {
                        bulletsToRemove.add(bullet);
                    }
                    break;
                }
            }
        }

        bullets.removeAll(bulletsToRemove);
    }
}
