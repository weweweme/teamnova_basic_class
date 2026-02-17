package cell;

import item.Item;

/// <summary>
/// 스킬 모드용 칸 클래스
/// 기본 Cell에 아이템 관리 기능을 추가
/// 새로운 스킬 모드 전용 칸 속성이 필요하면 이 클래스에 필드를 추가하면 됨
/// </summary>
public class SkillCell extends Cell {

    // ========== 필드 ==========

    // 이 칸에 설치된 아이템 (없으면 null)
    private Item item;

    // ========== 생성자 ==========

    /// <summary>
    /// 빈 스킬 칸 생성
    /// </summary>
    public SkillCell() {
        super();
        this.item = null;
    }

    // ========== 아이템 관련 ==========

    /// <summary>
    /// 이 칸의 아이템 반환 (없으면 null)
    /// </summary>
    public Item getItem() {
        return item;
    }

    /// <summary>
    /// 이 칸에 아이템 설치
    /// </summary>
    public void setItem(Item item) {
        this.item = item;
    }

    /// <summary>
    /// 이 칸의 아이템 제거
    /// </summary>
    public void removeItem() {
        this.item = null;
    }

    /// <summary>
    /// 이 칸에 아이템이 있는지 확인
    /// </summary>
    public boolean hasItem() {
        return item != null;
    }
}
