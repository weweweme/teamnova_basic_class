package system;

/// <summary>
/// 휴식 상태 — 제자리에서 피로를 회복
/// 틱마다 피로를 감소시키고, 피로가 0이 되면 대기 상태로 전환
/// </summary>
public class RestingState extends ColonistState {

    /// <summary>
    /// 틱당 피로 회복량
    /// </summary>
    private static final int RECOVERY_PER_TICK = 2;

    @Override
    public void enter(Colonist colonist) {
        // 휴식 시작 시 별도 초기화 없음
    }

    @Override
    public void update(Colonist colonist) {
        colonist.reduceFatigue(RECOVERY_PER_TICK);

        // 피로가 0이 되면 대기 상태로 복귀
        if (colonist.getFatigue() <= 0) {
            colonist.changeState(new IdleState());
        }
    }

    @Override
    public void exit(Colonist colonist) {
        // 휴식 종료 시 별도 정리 없음
    }

    @Override
    public String getDisplayName() {
        return "휴식중";
    }
}
