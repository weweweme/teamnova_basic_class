package world;

/// <summary>
/// 낮마다 랜덤으로 발생하는 이벤트
/// 이벤트마다 다른 보너스 효과가 있음
/// </summary>
public enum DayEvent {

    /// <summary>
    /// 보급품 발견 — 보급품 +15
    /// </summary>
    SUPPLY_DROP("보급품 발견!", 15),

    /// <summary>
    /// 떠돌이 합류 — 랜덤 정착민 HP 30 회복
    /// </summary>
    WANDERER("떠돌이 합류!", 30),

    /// <summary>
    /// 폭풍 경고 — 다음 밤 적 수 증가 (효과는 DayNightCycle에서 처리)
    /// </summary>
    STORM_WARNING("폭풍 경고!", 0),

    /// <summary>
    /// 평온한 하루 — 특별한 효과 없음
    /// </summary>
    CALM_DAY("평온한 하루", 0);

    /// <summary>
    /// 이벤트 메시지
    /// </summary>
    private final String message;

    /// <summary>
    /// 이벤트 수치 (보급품 양 또는 회복량)
    /// </summary>
    private final int value;

    DayEvent(String message, int value) {
        this.message = message;
        this.value = value;
    }

    /// <summary>
    /// 이벤트 메시지 반환
    /// </summary>
    public String getMessage() {
        return message;
    }

    /// <summary>
    /// 이벤트 수치 반환
    /// </summary>
    public int getValue() {
        return value;
    }
}
