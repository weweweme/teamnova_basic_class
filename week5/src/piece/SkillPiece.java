package piece;

/// <summary>
/// 스킬 모드 전용 기물
/// 기본 기물(Piece)에 방패/동결 상태를 추가
/// SkillBoard에서만 생성되며, 스킬/아이템 효과의 대상이 됨
/// </summary>
public class SkillPiece extends Piece {

    // 방패 상태 (true이면 상대가 이 기물을 잡을 수 없음)
    public boolean shielded;

    // 동결 상태 (true이면 이번 턴에 이동할 수 없음)
    public boolean frozen;

    // ========== 생성자 ==========

    /// <summary>
    /// 스킬 모드 기물 생성
    /// 방패/동결 상태를 기본값(false)으로 초기화
    /// </summary>
    public SkillPiece(PieceType type, int color, int row, int col) {
        super(type, color, row, col);
        this.shielded = false;
        this.frozen = false;
    }
}
