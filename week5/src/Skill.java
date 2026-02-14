import java.util.ArrayList;

/// <summary>
/// 스킬 추상 클래스
/// 모든 스킬의 공통 필드와 메서드를 정의
/// 각 하위 클래스(DestroySkill, ShieldSkill, ReviveSkill)가 자기만의 스킬 효과를 구현
/// </summary>
public abstract class Skill {

    // ========== 필드 ==========

    // 스킬 이름 ("파괴", "방패", "부활")
    protected String name;

    // 스킬 설명 (메뉴에 표시)
    protected String description;

    // 최대 사용 횟수
    protected int maxUses;

    // 남은 사용 횟수
    protected int remainingUses;

    // ========== 생성자 ==========

    /// <summary>
    /// 스킬 생성
    /// 이름, 설명, 최대 사용 횟수를 설정
    /// </summary>
    public Skill(String name, String description, int maxUses) {
        this.name = name;
        this.description = description;
        this.maxUses = maxUses;
        this.remainingUses = maxUses;
    }

    // ========== 공통 메서드 ==========

    /// <summary>
    /// 스킬을 사용할 수 있는지 확인 (남은 횟수가 있는지)
    /// </summary>
    public boolean hasUses() {
        return remainingUses > 0;
    }

    /// <summary>
    /// 사용 횟수 1회 차감
    /// </summary>
    public void useCharge() {
        remainingUses--;
    }

    // ========== 추상 메서드 ==========

    /// <summary>
    /// 현재 보드에서 이 스킬을 사용할 수 있는지 판단
    /// 남은 횟수와 별개로, 유효한 대상이 있는지 확인
    /// 각 하위 클래스가 자기만의 조건으로 구현 (메서드 오버라이딩)
    /// </summary>
    public abstract boolean canUse(Board board, int color);

    /// <summary>
    /// 스킬 대상이 될 수 있는 칸들의 좌표 목록 반환
    /// 플레이어가 이 중 하나를 선택하여 스킬을 사용
    /// 각 하위 클래스가 자기만의 대상 조건으로 구현 (메서드 오버라이딩)
    /// </summary>
    public abstract int[][] getTargets(Board board, int color);

    /// <summary>
    /// 선택한 대상에게 스킬 효과를 적용
    /// 각 하위 클래스가 자기만의 효과로 구현 (메서드 오버라이딩)
    /// </summary>
    public abstract void execute(Board board, int targetRow, int targetCol, int color);
}
