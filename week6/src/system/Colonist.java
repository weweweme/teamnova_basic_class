package system;

/// <summary>
/// 정착민 한 명을 나타내는 클래스
/// 맵 위에서 자율적으로 행동하며, 플레이어의 명령을 수행
/// </summary>
public class Colonist {

    /// <summary>
    /// 최대 체력
    /// </summary>
    private static final int MAX_HP = 100;

    /// <summary>
    /// 최대 피로도 (이 값에 도달하면 쓰러짐)
    /// </summary>
    private static final int MAX_FATIGUE = 100;

    /// <summary>
    /// 정착민 이름
    /// </summary>
    private final String name;

    /// <summary>
    /// 이름의 첫 글자 (맵에 표시할 이니셜)
    /// </summary>
    private final char initial;

    /// <summary>
    /// 맵 위의 위치
    /// </summary>
    private final Position position;

    /// <summary>
    /// 현재 체력
    /// </summary>
    private int hp;

    /// <summary>
    /// 현재 피로도 (높을수록 지친 상태)
    /// </summary>
    private int fatigue;

    /// <summary>
    /// 지정한 이름과 위치로 정착민 생성
    /// 체력은 최대, 피로도는 0으로 시작
    /// </summary>
    public Colonist(String name, Position position) {
        this.name = name;
        this.initial = name.charAt(0);
        this.position = position;
        this.hp = MAX_HP;
        this.fatigue = 0;
    }

    /// <summary>
    /// 이름 반환
    /// </summary>
    public String getName() {
        return name;
    }

    /// <summary>
    /// 이니셜 반환
    /// </summary>
    public char getInitial() {
        return initial;
    }

    /// <summary>
    /// 위치 반환
    /// </summary>
    public Position getPosition() {
        return position;
    }

    /// <summary>
    /// 현재 체력 반환
    /// </summary>
    public int getHp() {
        return hp;
    }

    /// <summary>
    /// 최대 체력 반환
    /// </summary>
    public int getMaxHp() {
        return MAX_HP;
    }

    /// <summary>
    /// 현재 피로도 반환
    /// </summary>
    public int getFatigue() {
        return fatigue;
    }

    /// <summary>
    /// 최대 피로도 반환
    /// </summary>
    public int getMaxFatigue() {
        return MAX_FATIGUE;
    }

    /// <summary>
    /// 살아있는지 확인
    /// </summary>
    public boolean isAlive() {
        return hp > 0;
    }
}
