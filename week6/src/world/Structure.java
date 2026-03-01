package world;

/// <summary>
/// 맵 위에 설치되는 건물의 공통 상위 클래스
/// 내구도를 가지며, 피해를 받고 수리할 수 있음
/// 여러 스레드에서 동시 접근 가능하므로 동기화 처리
/// </summary>
public abstract class Structure {

    /// <summary>
    /// 건물이 위치한 열
    /// </summary>
    private final int column;

    /// <summary>
    /// 최대 내구도 (업그레이드로 변경 가능)
    /// </summary>
    private int maxHp;

    /// <summary>
    /// 현재 내구도
    /// </summary>
    private int hp;

    /// <summary>
    /// 지정한 열과 최대 내구도로 건물 생성
    /// </summary>
    protected Structure(int column, int maxHp) {
        this.column = column;
        this.maxHp = maxHp;
        this.hp = maxHp;
    }

    /// <summary>
    /// 건물이 위치한 열 반환
    /// </summary>
    public int getColumn() {
        return column;
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
        return maxHp;
    }

    /// <summary>
    /// 건물이 파괴되었는지 확인
    /// </summary>
    public synchronized boolean isDestroyed() {
        return hp <= 0;
    }

    /// <summary>
    /// 최대 내구도 변경 (업그레이드용)
    /// </summary>
    protected synchronized void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    /// <summary>
    /// 현재 내구도를 직접 설정 (업그레이드 시 풀 회복용)
    /// </summary>
    protected synchronized void setHp(int hp) {
        this.hp = hp;
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
        hp = Math.min(hp + amount, maxHp);
    }
}
