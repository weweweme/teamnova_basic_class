package game;

/// <summary>
/// 총알 명중 시 화면에 잠시 표시되는 이펙트
/// </summary>
public class HitEffect {

    /// <summary>
    /// 이펙트 표시 행
    /// </summary>
    private final int row;

    /// <summary>
    /// 이펙트 표시 열
    /// </summary>
    private final int col;

    /// <summary>
    /// 이펙트 생성 시각 (밀리초)
    /// </summary>
    private final long createdTime;

    /// <summary>
    /// 이펙트 표시 문자 (무기마다 다름)
    /// </summary>
    private final char effectChar;

    /// <summary>
    /// 이펙트 ANSI 색상 코드 (무기마다 다름)
    /// </summary>
    private final int effectColor;

    public HitEffect(int row, int col, long createdTime, char effectChar, int effectColor) {
        this.row = row;
        this.col = col;
        this.createdTime = createdTime;
        this.effectChar = effectChar;
        this.effectColor = effectColor;
    }

    /// <summary>
    /// 이펙트 행 반환
    /// </summary>
    public int getRow() {
        return row;
    }

    /// <summary>
    /// 이펙트 열 반환
    /// </summary>
    public int getCol() {
        return col;
    }

    /// <summary>
    /// 이펙트 생성 시각 반환
    /// </summary>
    public long getCreatedTime() {
        return createdTime;
    }

    /// <summary>
    /// 이펙트 표시 문자 반환
    /// </summary>
    public char getEffectChar() {
        return effectChar;
    }

    /// <summary>
    /// 이펙트 색상 코드 반환
    /// </summary>
    public int getEffectColor() {
        return effectColor;
    }
}
