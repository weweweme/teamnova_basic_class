package skill;

import board.SkillBoard;
import cell.Cell;
import core.Util;
import core.Chess;

/// <summary>
/// 스킬 추상 클래스
/// 모든 스킬의 공통 필드와 메서드를 정의
/// 각 하위 클래스(DestroySkill, ShieldSkill, ReviveSkill)가 자기만의 스킬 효과를 구현
/// </summary>
public abstract class Skill {

    // ========== 필드 ==========

    // 스킬 이름 ("파괴", "방패", "부활")
    public String name;

    // 스킬 설명 (메뉴에 표시)
    public String description;

    // 최대 사용 횟수
    protected int maxUses;

    // 남은 사용 횟수
    public int remainingUses;

    // 대상 좌표 버퍼 (매번 새로 만들지 않고 재사용)
    public final int[][] targets = new int[Chess.MAX_PIECES_PER_SIDE][Chess.COORD_SIZE];

    // 현재 유효한 대상 수 (targets 배열에서 이 수만큼만 유효)
    public int targetCount = 0;

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
    /// 현재 격자에서 이 스킬을 사용할 수 있는지 판단
    /// 남은 횟수와 별개로, 유효한 대상이 있는지 확인
    /// Board가 아닌 격자 배열만 받아 최소한의 정보로 판단
    /// 각 하위 클래스가 자기만의 조건으로 구현 (메서드 오버라이딩)
    /// </summary>
    public abstract boolean canUse(Cell[][] grid, int color);

    /// <summary>
    /// 스킬 대상이 될 수 있는 칸들을 찾아 버퍼에 저장
    /// 결과는 targets 배열과 targetCount 필드에 기록
    /// 매번 새 배열을 만들지 않고 기존 버퍼를 재사용
    /// 각 하위 클래스가 자기만의 대상 조건으로 구현 (메서드 오버라이딩)
    /// </summary>
    public abstract void findTargets(Cell[][] grid, int color);

    /// <summary>
    /// 선택한 대상에게 스킬 효과를 적용
    /// 기물 제거/부활 등 보드 상태 변경이 필요하므로 Board를 받음
    /// 각 하위 클래스가 자기만의 효과로 구현 (메서드 오버라이딩)
    /// </summary>
    public abstract void execute(SkillBoard board, int targetRow, int targetCol, int color);
}
