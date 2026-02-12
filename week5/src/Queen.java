/// <summary>
/// 퀸 기물
/// 직선 + 대각선 8방향으로 무제한 이동 (룩 + 비숍의 결합)
/// slideMoves를 8방향으로 호출
/// </summary>
public class Queen extends Piece {

    // ========== 생성자 ==========

    public Queen(int color, int row, int col) {
        super(color, row, col);
        this.name = "퀸";
        this.symbol = (color == RED) ? "Q" : "q";
        this.value = 9;
    }

    // ========== 이동 규칙 ==========

    /// <summary>
    /// 퀸의 이동 규칙
    /// 직선 4방향 + 대각선 4방향 = 8방향 직선 이동
    /// </summary>
    @Override
    protected void calculateMoves(Piece[][] board) {
        // 직선 4방향 (룩과 동일)
        slideMoves(board, -1, 0, moves);   // 위
        slideMoves(board, 1, 0, moves);    // 아래
        slideMoves(board, 0, -1, moves);   // 왼쪽
        slideMoves(board, 0, 1, moves);    // 오른쪽

        // 대각선 4방향 (비숍과 동일)
        slideMoves(board, -1, -1, moves);  // 좌상
        slideMoves(board, -1, 1, moves);   // 우상
        slideMoves(board, 1, -1, moves);   // 좌하
        slideMoves(board, 1, 1, moves);    // 우하
    }
}
