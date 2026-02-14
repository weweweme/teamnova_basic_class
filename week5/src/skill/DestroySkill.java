package skill;

import java.util.ArrayList;
import core.Board;
import piece.Piece;

/// <summary>
/// 파괴 스킬
/// 상대 기물 하나를 즉시 보드에서 제거 (킹 제외)
/// 사용 횟수: 1회
/// </summary>
public class DestroySkill extends Skill {

    // ========== 생성자 ==========

    public DestroySkill() {
        super("파괴", "상대 기물 하나를 즉시 제거 (킹 제외)", 1);
    }

    // ========== 스킬 로직 ==========

    /// <summary>
    /// 사용 가능 여부: 상대 기물(킹 제외)이 보드에 하나라도 있으면 사용 가능
    /// </summary>
    @Override
    public boolean canUse(Board board, int color) {
        if (!hasUses()) {
            return false;
        }

        // 상대 색상
        int opponentColor = (color == Piece.RED) ? Piece.BLUE : Piece.RED;

        // 상대 기물(킹 제외)이 하나라도 있는지 확인
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece != null && piece.color == opponentColor && !(piece instanceof King)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// <summary>
    /// 대상: 보드 위의 상대 기물 위치 (킹 제외)
    /// </summary>
    @Override
    public int[][] getTargets(Board board, int color) {
        int opponentColor = (color == Piece.RED) ? Piece.BLUE : Piece.RED;
        ArrayList<int[]> targets = new ArrayList<>();

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece != null && piece.color == opponentColor && !(piece instanceof King)) {
                    targets.add(new int[]{r, c});
                }
            }
        }
        return targets.toArray(new int[0][]);
    }

    /// <summary>
    /// 효과: 지정한 칸의 상대 기물을 제거하고 잡힌 기물 목록에 추가
    /// </summary>
    @Override
    public void execute(Board board, int targetRow, int targetCol, int color) {
        board.removePiece(targetRow, targetCol);
        useCharge();
    }
}
