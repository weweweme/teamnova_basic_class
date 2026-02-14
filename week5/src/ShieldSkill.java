import java.util.ArrayList;

/// <summary>
/// 방패 스킬
/// 아군 기물 하나를 1턴 동안 보호 (상대가 잡을 수 없음, 킹 제외)
/// 사용 횟수: 2회
/// </summary>
public class ShieldSkill extends Skill {

    // ========== 생성자 ==========

    public ShieldSkill() {
        super("방패", "아군 기물 하나를 1턴 보호 (킹 제외)", 2);
    }

    // ========== 스킬 로직 ==========

    /// <summary>
    /// 사용 가능 여부: 아군 기물(킹 제외)이 보드에 하나라도 있으면 사용 가능
    /// </summary>
    @Override
    public boolean canUse(Board board, int color) {
        if (!hasUses()) {
            return false;
        }

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece != null && piece.color == color && !(piece instanceof King)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// <summary>
    /// 대상: 보드 위의 아군 기물 위치 (킹 제외)
    /// </summary>
    @Override
    public int[][] getTargets(Board board, int color) {
        ArrayList<int[]> targets = new ArrayList<>();

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece != null && piece.color == color && !(piece instanceof King)) {
                    targets.add(new int[]{r, c});
                }
            }
        }
        return targets.toArray(new int[0][]);
    }

    /// <summary>
    /// 효과: 지정한 기물에 방패 상태 부여
    /// </summary>
    @Override
    public void execute(Board board, int targetRow, int targetCol, int color) {
        Piece piece = board.getPiece(targetRow, targetCol);
        if (piece != null) {
            piece.shielded = true;
        }
        useCharge();
    }
}
