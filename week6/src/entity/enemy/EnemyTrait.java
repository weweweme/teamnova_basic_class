package entity.enemy;

/// <summary>
/// 적의 행동 특성을 정의하는 열거형
/// 종류마다 하나의 특성을 가지며, 전투 중 행동이 달라짐
/// </summary>
public enum EnemyTrait {

    /// <summary>
    /// 기본 — 일정 속도로 이동하여 공격
    /// </summary>
    STANDARD("기본"),

    /// <summary>
    /// 돌진 — 바리케이드 근처에서 2칸씩 이동
    /// </summary>
    CHARGER("돌진"),

    /// <summary>
    /// 방어 — 받는 피해 50% 감소
    /// </summary>
    ARMORED("방어"),

    /// <summary>
    /// 재생 — 3틱마다 체력 1 회복
    /// </summary>
    REGENERATING("재생");

    /// <summary>
    /// 화면에 표시할 특성 이름
    /// </summary>
    private final String displayName;

    EnemyTrait(String displayName) {
        this.displayName = displayName;
    }

    /// <summary>
    /// 특성 이름 반환
    /// </summary>
    public String getDisplayName() {
        return displayName;
    }
}
