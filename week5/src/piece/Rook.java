package piece;

import cell.Cell;

/// <summary>
/// 룩 기물
/// 상하좌우 직선으로 무제한 이동 (가로막히면 멈춤)
/// slideMoves를 4방향으로 호출
/// </summary>
public class Rook extends Piece {

    // ========== 생성자 ==========

    public Rook(int color, int row, int col) {
        super(color, row, col);
        this.name = "룩";
        // 빨간팀은 대문자, 파란팀은 소문자
        this.symbol = (color == RED) ? "R" : "r";
        this.value = 5;
    }

    // ========== 이동 규칙 ==========

    /// <summary>
    /// 룩의 이동 규칙
    /// 상하좌우 4방향으로 직선 이동
    /// </summary>
    @Override
    protected void calculateMoves(Cell[][] board) {
        slideMoves(board, -1, 0);  // 위
        slideMoves(board, 1, 0);   // 아래
        slideMoves(board, 0, -1);  // 왼쪽
        slideMoves(board, 0, 1);   // 오른쪽
    }
}
