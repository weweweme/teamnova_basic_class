package world;

/// <summary>
/// 안전지대와 전장을 나누는 세로 방벽
/// 적이 도달하면 공격 대상이 되며, 보급품으로 수리 가능
/// 여러 스레드에서 동시 접근 가능하므로 동기화 처리
/// </summary>
public class Barricade {

    /// <summary>
    /// 바리케이드가 위치한 열 (이 열과 다음 열에 ## 표시)
    /// </summary>
    public static final int COLUMN = 15;

    /// <summary>
    /// 최대 내구도
    /// </summary>
    private static final int MAX_HP = 100;

    /// <summary>
    /// 현재 내구도
    /// </summary>
    private int hp;

    /// <summary>
    /// 최대 내구도로 바리케이드 생성
    /// </summary>
    public Barricade() {
        this.hp = MAX_HP;
    }

    /// <summary>
    /// 현재 내구도 반환
    /// </summary>
    public synchronized int getHp() {
        return hp;
    }

    /// <summary>
    /// 최대 내구도 반환
    /// </summary>
    public synchronized int getMaxHp() {
        return MAX_HP;
    }

    /// <summary>
    /// 바리케이드가 파괴되었는지 확인
    /// </summary>
    public synchronized boolean isDestroyed() {
        return hp <= 0;
    }

    /// <summary>
    /// 피해를 받아 내구도 감소
    /// </summary>
    public synchronized void takeDamage(int damage) {
        hp = Math.max(hp - damage, 0);
    }

    /// <summary>
    /// 수리하여 내구도 회복 (최대치 초과 방지)
    /// </summary>
    public synchronized void repair(int amount) {
        hp = Math.min(hp + amount, MAX_HP);
    }
}
