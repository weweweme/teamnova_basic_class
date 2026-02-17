package piece;

import core.Cell;

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
        // 빨간팀은 위로(-1), 파란팀은 아래로(+1) 전진
        int direction = (color == RED) ? -1 : 1;

        // 1칸 전진 (빈 칸일 때만)
        int oneStep = row + direction;
        if (oneStep >= 0 && oneStep < 8 && board[oneStep][col].isEmpty()) {
            moves.add(new int[]{oneStep, col});

            // 2칸 전진 (첫 이동이고, 앞 2칸 모두 빈 칸일 때만)
            int twoStep = row + direction * 2;
            if (!hasMoved && twoStep >= 0 && twoStep < 8 && board[twoStep][col].isEmpty()) {
                moves.add(new int[]{twoStep, col});
            }
        }

        // 대각선 잡기 (적군이 있을 때만)
        int[] captureCols = {col - 1, col + 1};  // 왼쪽 대각선, 오른쪽 대각선
        for (int captureCol : captureCols) {
            // 보드 범위 확인
            if (oneStep < 0 || oneStep >= 8 || captureCol < 0 || captureCol >= 8) {
                continue;
            }

            Piece target = board[oneStep][captureCol].getPiece();

            // 적군이 있을 때만 대각선 이동 가능
            if (isEnemy(target)) {
                moves.add(new int[]{oneStep, captureCol});
            }
        }
    }
}
