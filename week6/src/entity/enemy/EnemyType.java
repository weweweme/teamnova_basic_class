package entity.enemy;

/// <summary>
/// 적의 종류를 나타내는 열거형
/// 실제 속성 데이터는 EnemyFactory에서 EnemySpec으로 제공
/// </summary>
public enum EnemyType {

    // ── 일반 ──
    WOLF,
    SPIDER,
    SKELETON,
    ZOMBIE,
    RAT,
    SLIME,

    // ── 강한 ──
    BEAR,
    BANDIT,
    SCORPION,
    ORC,

    // ── 보스 ──
    DRAGON,
    GOLEM
}
