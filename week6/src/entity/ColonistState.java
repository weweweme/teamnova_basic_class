package entity;

/// <summary>
/// 정착민의 행동 상태를 나타내는 추상 클래스
/// 각 상태는 진입/실행/퇴장 단계를 구현해야 함
/// </summary>
public abstract class ColonistState {

    /// <summary>
    /// 상태에 진입할 때 1회 호출
    /// 초기화 작업 (목표 설정, 타이머 리셋 등)
    /// </summary>
    public abstract void enter(Colonist colonist);

    /// <summary>
    /// 매 틱마다 호출되는 상태의 핵심 로직
    /// 행동 수행 및 상태 전환 판단
    /// </summary>
    public abstract void update(Colonist colonist);

    /// <summary>
    /// 상태에서 빠져나올 때 1회 호출
    /// 정리 작업 (리소스 해제 등)
    /// </summary>
    public abstract void exit(Colonist colonist);

    /// <summary>
    /// 패널에 표시할 상태 이름 반환
    /// </summary>
    public abstract String getDisplayName();
}
