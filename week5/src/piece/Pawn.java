package piece;

import cell.Cell;

/// <summary>
/// 폰 기물
/// 전진 1칸, 첫 이동 시 전진 2칸, 대각선으로 적군 잡기
/// 빨간팀은 위로(행 감소), 파란팀은 아래로(행 증가) 이동
/// 프로모션, 앙파상은 Phase 6에서 추가 예정
/// </summary>
public class Pawn extends Piece {

    // ========== 생성자 ==========

    public Pawn(int color, int row, int col) {
        super(color, row, col);
        this.name = "폰";
        this.symbol = (color == RED) ? "P" : "p";
        this.value = 1;
    }

    // ========== 이동 규칙 ==========

    /// <summary>
    /// 폰의 이동 규칙
    /// 전진 1칸(빈 칸만), 첫 이동 시 전진 2칸, 대각선 잡기(적군만)
    /// </summary>
    @Override
    protected void calculateMoves(Cell[][] board) {
        // 빨간팀은 위로, 파란팀은 아래로 전진
        int direction = (color == RED) ? RED_DIRECTION : BLUE_DIRECTION;

        // 1칸 전진 (빈 칸일 때만)
        int oneStep = row + direction;
        boolean oneStepInBounds = oneStep >= 0 && oneStep < 8;   // 1칸 앞이 보드 안인지
        boolean oneStepEmpty = oneStepInBounds && board[oneStep][col].isEmpty();  // 앞 칸이 비어있는지

        if (oneStepEmpty) {
            addMove(oneStep, col);

            // 2칸 전진 (첫 이동이고, 앞 2칸 모두 빈 칸일 때만)
            int twoStep = row + direction * 2;
            boolean isFirstMove = !hasMoved;                         // 아직 한 번도 움직이지 않았는지
            boolean twoStepInBounds = twoStep >= 0 && twoStep < 8;  // 2칸 앞이 보드 안인지
            boolean twoStepEmpty = twoStepInBounds && board[twoStep][col].isEmpty();  // 2칸 앞이 비어있는지

            if (isFirstMove && twoStepEmpty) {
                addMove(twoStep, col);
            }
        }

        // 대각선 잡기 (적군이 있을 때만)
        int[] captureCols = {col - 1, col + 1};  // 왼쪽 대각선, 오른쪽 대각선
        for (int captureCol : captureCols) {
            // 보드 범위 확인
            boolean rowOutOfBounds = oneStep < 0 || oneStep >= 8;       // 행이 보드 범위를 넘는지
            boolean colOutOfBounds = captureCol < 0 || captureCol >= 8; // 열이 보드 범위를 넘는지
            if (rowOutOfBounds || colOutOfBounds) {
                continue;
            }

            // 적군이 있을 때만 대각선 이동 가능
            if (board[oneStep][captureCol].hasPiece() && isEnemy(board[oneStep][captureCol].getPiece())) {
                addMove(oneStep, captureCol);
            }
        }
    }
}
