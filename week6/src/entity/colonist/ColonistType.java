package entity.colonist;

/// <summary>
/// 정착민 유형을 정의하는 열거형
/// 유형마다 최대 체력이 다름
/// 전투 능력은 장착한 무기(Gun)가 결정
/// </summary>
public enum ColonistType {

    /// <summary>
    /// 사격수 — 균형형 (체력 100, 기본 무기: 피스톨)
    /// </summary>
    GUNNER("사격수", 100),

    /// <summary>
    /// 저격수 — 낮은 체력 (체력 80, 기본 무기: 라이플)
    /// </summary>
    SNIPER("저격수", 80),

    /// <summary>
    /// 돌격수 — 높은 체력 (체력 120, 기본 무기: 샷건)
    /// </summary>
    ASSAULT("돌격수", 120);

    /// <summary>
    /// 화면에 표시할 유형 이름
    /// </summary>
    private final String displayName;

    /// <summary>
    /// 최대 체력
    /// </summary>
    private final int maxHp;

    ColonistType(String displayName, int maxHp) {
        this.displayName = displayName;
        this.maxHp = maxHp;
    }

    /// <summary>
    /// 유형 이름 반환
    /// </summary>
    public String getDisplayName() {
        return displayName;
    }

    /// <summary>
    /// 최대 체력 반환
    /// </summary>
    public int getMaxHp() {
        return maxHp;
    }
}
