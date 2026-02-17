package skill;

import board.Board;
import cell.Cell;
import piece.King;
import piece.Piece;

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
    /// 사용 가능 여부: 아군 기물(킹 제외)이 격자에 하나라도 있으면 사용 가능
    /// 격자를 직접 순회하며 조기 반환
    /// </summary>
    @Override
    public boolean canUse(Cell[][] grid, int color) {
        if (!hasUses()) {
            return false;
        }

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (grid[r][c].hasPiece()) {
                    Piece piece = grid[r][c].getPiece();
                    if (piece.color == color && !(piece instanceof King)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /// <summary>
    /// 대상 찾기: 격자 위의 아군 기물 위치 (킹 제외)
    /// 결과를 버퍼(targets, targetCount)에 저장하여 메모리 재사용
    /// </summary>
    @Override
    public void findTargets(Cell[][] grid, int color) {
        targetCount = 0;

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (grid[r][c].hasPiece()) {
                    Piece piece = grid[r][c].getPiece();
                    if (piece.color == color && !(piece instanceof King)) {
                        targets[targetCount][0] = r;
                        targets[targetCount][1] = c;
                        targetCount++;
                    }
                }
            }
        }
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
