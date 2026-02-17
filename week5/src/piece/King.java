package piece;

import cell.Cell;

/// <summary>
/// 킹 기물
/// 전방향 1칸 이동 (8방향)
/// 캐슬링은 Phase 6에서 추가 예정
/// </summary>
public class King extends Piece {

    // ========== 이동 방향 ==========

    // 8방향 (상하좌우 + 대각선) 각 1칸
    private static final int[][] OFFSETS = {
        {-1, -1}, {-1, 0}, {-1, 1},   // 좌상, 상, 우상
        {0, -1},           {0, 1},     // 좌,       우
        {1, -1},  {1, 0},  {1, 1}     // 좌하, 하, 우하
    };

    // ========== 생성자 ==========

    public King(int color, int row, int col) {
        super(color, row, col);
        this.name = "킹";
        this.symbol = (color == RED) ? "K" : "k";
        this.value = 0;
    }

    // ========== 이동 규칙 ==========

    /// <summary>
    /// 킹의 이동 규칙
    /// 8방향 각 1칸 중 보드 안이고 아군이 없는 칸
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

            // 빈 칸이거나 적군이면 이동 가능
            if (board[r][c].isEmpty() || isEnemy(board[r][c].getPiece())) {
                addMove(r, c);
            }
        }
    }
}
