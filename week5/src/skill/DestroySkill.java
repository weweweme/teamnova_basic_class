package skill;

import core.Board;
import core.SkillBoard;
import piece.King;
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
    /// 사용 가능 여부: 상대 기물(킹 제외)이 격자에 하나라도 있으면 사용 가능
    /// 격자를 직접 순회하며 조기 반환
    /// </summary>
    @Override
    public boolean canUse(Piece[][] grid, int color) {
        if (!hasUses()) {
            return false;
        }

        int opponentColor = (color == Piece.RED) ? Piece.BLUE : Piece.RED;

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece piece = grid[r][c];
                if (piece != null && piece.color == opponentColor && !(piece instanceof King)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// <summary>
    /// 대상 찾기: 격자 위의 상대 기물 위치 (킹 제외)
    /// 결과를 버퍼(targets, targetCount)에 저장하여 메모리 재사용
    /// </summary>
    @Override
    public void findTargets(Piece[][] grid, int color) {
        int opponentColor = (color == Piece.RED) ? Piece.BLUE : Piece.RED;
        targetCount = 0;

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Piece piece = grid[r][c];
                if (piece != null && piece.color == opponentColor && !(piece instanceof King)) {
                    targets[targetCount][0] = r;
                    targets[targetCount][1] = c;
                    targetCount++;
                }
            }
        }
    }

    /// <summary>
    /// 효과: 지정한 칸의 상대 기물을 제거하고 잡힌 기물 목록에 추가
    /// </summary>
    @Override
    public void execute(Board board, int targetRow, int targetCol, int color) {
        ((SkillBoard) board).removePiece(targetRow, targetCol);
        useCharge();
    }
}
