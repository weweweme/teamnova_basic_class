package piece;

import cell.Cell;

/// <summary>
/// 나이트 기물
/// L자 모양으로 이동 (다른 기물을 뛰어넘을 수 있음)
/// slideMoves를 사용하지 않는 유일한 기물
/// </summary>
public class Knight extends Piece {

    // ========== 이동 방향 ==========

    // L자 이동: 한 방향 2칸 + 수직 방향 1칸 = 8가지 조합
    private static final int[][] OFFSETS = {
        {-2, -1}, {-2, 1},   // 위로 2칸 + 좌/우 1칸
        {-1, -2}, {-1, 2},   // 위로 1칸 + 좌/우 2칸
        {1, -2},  {1, 2},    // 아래로 1칸 + 좌/우 2칸
        {2, -1},  {2, 1}     // 아래로 2칸 + 좌/우 1칸
    };

    // ========== 생성자 ==========

    public Knight(int color, int row, int col) {
        super(color, row, col);
        this.name = "나이트";
        this.symbol = (color == RED) ? "N" : "n";
        this.value = 3;
    }

    // ========== 이동 규칙 ==========

    /// <summary>
    /// 나이트의 이동 규칙
    /// L자 모양 8칸 중 보드 안이고 아군이 없는 칸
    /// </summary>
    @Override
    protected void calculateMoves(Cell[][] board) {
        for (int[] offset : OFFSETS) {
            int r = row + offset[0];
            int c = col + offset[1];

            // 보드 범위 확인
            if (r < 0 || r >= 8 || c < 0 || c >= 8) {
                continue;
            }

            Piece target = board[r][c].getPiece();

            // 빈 칸이거나 적군이면 이동 가능
            if (target == null || isEnemy(target)) {
                moves.add(new int[]{r, c});
            }
        }
    }
}
