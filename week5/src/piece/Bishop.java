package piece;

import core.Cell;

/// <summary>
/// 비숍 기물
/// 대각선 4방향으로 무제한 이동 (가로막히면 멈춤)
/// slideMoves를 4방향으로 호출
/// </summary>
public class Bishop extends Piece {

    // ========== 생성자 ==========

    public Bishop(int color, int row, int col) {
        super(color, row, col);
        this.name = "비숍";
        this.symbol = (color == RED) ? "B" : "b";
        this.value = 3;
    }

    // ========== 이동 규칙 ==========

    /// <summary>
    /// 비숍의 이동 규칙
    /// 대각선 4방향으로 직선 이동
    /// </summary>
    @Override
    protected void calculateMoves(Cell[][] board) {
        slideMoves(board, -1, -1, moves);  // 좌상
        slideMoves(board, -1, 1, moves);   // 우상
        slideMoves(board, 1, -1, moves);   // 좌하
        slideMoves(board, 1, 1, moves);    // 우하
    }
}
