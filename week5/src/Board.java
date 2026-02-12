import java.util.ArrayList;

/// <summary>
/// 체스판 클래스
/// 8x8 격자, 기물 배치, 이동 실행, 보드 출력, 체크/체크메이트 판정
/// </summary>
public class Board {

    // ========== 상수 ==========

    // 체스판 크기
    public static final int SIZE = 8;

    // ========== 필드 ==========

    // 8x8 격자 (빈 칸은 null)
    public Piece[][] grid;

    // ========== 생성자 ==========

    /// <summary>
    /// 체스판 생성 및 기물 초기 배치
    /// </summary>
    public Board() {
        grid = new Piece[SIZE][SIZE];
        initPieces();
    }

    // ========== 초기 배치 ==========

    /// <summary>
    /// 기물을 표준 체스 시작 위치에 배치
    /// 파란팀(상단 0~1행), 빨간팀(하단 6~7행)
    /// </summary>
    private void initPieces() {
        // 파란팀 주요 기물 (0행 = 8번 줄)
        grid[0][0] = new Rook(Piece.BLUE, 0, 0);
        grid[0][1] = new Knight(Piece.BLUE, 0, 1);
        grid[0][2] = new Bishop(Piece.BLUE, 0, 2);
        grid[0][3] = new Queen(Piece.BLUE, 0, 3);
        grid[0][4] = new King(Piece.BLUE, 0, 4);
        grid[0][5] = new Bishop(Piece.BLUE, 0, 5);
        grid[0][6] = new Knight(Piece.BLUE, 0, 6);
        grid[0][7] = new Rook(Piece.BLUE, 0, 7);

        // 파란팀 폰 (1행 = 7번 줄)
        for (int c = 0; c < SIZE; c++) {
            grid[1][c] = new Pawn(Piece.BLUE, 1, c);
        }

        // 빨간팀 폰 (6행 = 2번 줄)
        for (int c = 0; c < SIZE; c++) {
            grid[6][c] = new Pawn(Piece.RED, 6, c);
        }

        // 빨간팀 주요 기물 (7행 = 1번 줄)
        grid[7][0] = new Rook(Piece.RED, 7, 0);
        grid[7][1] = new Knight(Piece.RED, 7, 1);
        grid[7][2] = new Bishop(Piece.RED, 7, 2);
        grid[7][3] = new Queen(Piece.RED, 7, 3);
        grid[7][4] = new King(Piece.RED, 7, 4);
        grid[7][5] = new Bishop(Piece.RED, 7, 5);
        grid[7][6] = new Knight(Piece.RED, 7, 6);
        grid[7][7] = new Rook(Piece.RED, 7, 7);
    }

    // ========== 보드 출력 ==========

    /// <summary>
    /// 기본 보드 출력 (커서, 하이라이트 없음)
    /// </summary>
    public void print() {
        print(-1, -1, -1, -1, null);
    }

    /// <summary>
    /// 커서만 표시하며 보드 출력 (기물 탐색 모드)
    /// </summary>
    public void print(int cursorRow, int cursorCol) {
        print(cursorRow, cursorCol, -1, -1, null);
    }

    /// <summary>
    /// 보드 출력 (커서 + 선택된 기물 + 이동 가능한 칸 표시)
    /// cursorRow/Col: 현재 커서 위치 (-1이면 커서 없음)
    /// selectedRow/Col: 선택된 기물 위치 (-1이면 선택 없음)
    /// validMoves: 이동 가능한 칸 목록 (null이면 표시 안 함)
    /// </summary>
    public void print(int cursorRow, int cursorCol, int selectedRow, int selectedCol, int[][] validMoves) {
        // 상단 열 표시
        System.out.println("     a   b   c   d   e   f   g   h");
        System.out.println("   +---+---+---+---+---+---+---+---+");

        for (int r = 0; r < SIZE; r++) {
            int rank = 8 - r;
            StringBuilder line = new StringBuilder();
            line.append(String.format(" %d |", rank));

            for (int c = 0; c < SIZE; c++) {
                String cell = renderCell(r, c, cursorRow, cursorCol, selectedRow, selectedCol, validMoves);
                line.append(cell).append("|");
            }

            line.append(String.format(" %d", rank));
            System.out.println(line.toString());
            System.out.println("   +---+---+---+---+---+---+---+---+");
        }

        // 하단 열 표시
        System.out.println("     a   b   c   d   e   f   g   h");
    }

    /// <summary>
    /// 한 칸의 표시 문자열을 결정
    /// 우선순위: 커서/선택 > 이동 가능 칸 > 일반
    /// </summary>
    private String renderCell(int r, int c, int cursorRow, int cursorCol, int selectedRow, int selectedCol, int[][] validMoves) {
        Piece piece = grid[r][c];
        boolean isCursor = (r == cursorRow && c == cursorCol);
        boolean isSelected = (r == selectedRow && c == selectedCol);
        boolean isValidMove = isInArray(r, c, validMoves);

        // 1순위: 커서 또는 선택된 기물 → 대괄호로 감싸기
        if (isCursor || isSelected) {
            if (piece != null) {
                String colorCode = (piece.color == Piece.RED) ? Util.RED : Util.BLUE;
                return "[" + colorCode + piece.symbol + Util.RESET + "]";
            }
            return "[ ]";
        }

        // 2순위: 이동 가능한 칸 → · 표시
        if (isValidMove) {
            return " · ";
        }

        // 3순위: 일반 칸
        if (piece != null) {
            String colorCode = (piece.color == Piece.RED) ? Util.RED : Util.BLUE;
            return " " + colorCode + piece.symbol + Util.RESET + " ";
        }
        return "   ";
    }

    /// <summary>
    /// 특정 좌표가 배열에 포함되어 있는지 확인
    /// </summary>
    public boolean isInArray(int row, int col, int[][] array) {
        if (array == null) {
            return false;
        }
        for (int[] pos : array) {
            if (pos[0] == row && pos[1] == col) {
                return true;
            }
        }
        return false;
    }

    // ========== 기물 조회 ==========

    /// <summary>
    /// 지정한 칸의 기물 반환 (빈 칸이면 null)
    /// </summary>
    public Piece getPiece(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return null;
        }
        return grid[row][col];
    }

    /// <summary>
    /// 특정 색상의 킹 위치를 {행, 열} 배열로 반환
    /// </summary>
    public int[] findKing(int color) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece piece = grid[r][c];
                if (piece instanceof King && piece.color == color) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    // ========== 이동 실행 ==========

    /// <summary>
    /// 이동 실행 (기물 옮기기 + 잡기 처리)
    /// 기물의 위치 정보와 hasMoved 상태도 갱신
    /// </summary>
    public void executeMove(Move move) {
        Piece piece = grid[move.fromRow][move.fromCol];

        // 도착 칸에 기물 배치 (적군이 있으면 잡기)
        grid[move.toRow][move.toCol] = piece;
        grid[move.fromRow][move.fromCol] = null;

        // 기물의 위치 정보 갱신
        piece.row = move.toRow;
        piece.col = move.toCol;
        piece.hasMoved = true;
    }

    // ========== 이동 유효성 ==========

    /// <summary>
    /// 특정 기물의 이동 가능한 칸 중 자기 킹이 위험해지지 않는 수만 반환
    /// 실제로 플레이어가 선택할 수 있는 합법적인 수 목록
    /// </summary>
    public int[][] getFilteredMoves(int row, int col) {
        Piece piece = grid[row][col];
        if (piece == null) {
            return new int[0][];
        }

        int[][] rawMoves = piece.getValidMoves(grid);
        ArrayList<int[]> filtered = new ArrayList<>();

        for (int[] dest : rawMoves) {
            Move move = new Move(row, col, dest[0], dest[1]);
            // 이 수를 두면 자기 킹이 체크되는지 확인
            if (!wouldBeInCheck(move, piece.color)) {
                filtered.add(dest);
            }
        }

        return filtered.toArray(new int[0][]);
    }

    /// <summary>
    /// 특정 색상의 모든 합법적인 수 목록 반환
    /// 각 기물의 이동 가능한 수 중 자기 킹이 위험해지지 않는 수만 포함
    /// </summary>
    public Move[] getAllValidMoves(int color) {
        ArrayList<Move> allMoves = new ArrayList<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece piece = grid[r][c];
                if (piece == null || piece.color != color) {
                    continue;
                }

                int[][] pieceMoves = piece.getValidMoves(grid);
                for (int[] dest : pieceMoves) {
                    Move move = new Move(r, c, dest[0], dest[1]);
                    if (!wouldBeInCheck(move, color)) {
                        allMoves.add(move);
                    }
                }
            }
        }

        return allMoves.toArray(new Move[0]);
    }

    /// <summary>
    /// 이 수를 두면 자기 킹이 체크 상태가 되는지 시뮬레이션
    /// 임시로 이동했다가 되돌리는 방식으로 확인
    /// </summary>
    private boolean wouldBeInCheck(Move move, int color) {
        // 원래 상태 저장
        Piece movingPiece = grid[move.fromRow][move.fromCol];
        Piece capturedPiece = grid[move.toRow][move.toCol];
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;

        // 임시로 이동 실행
        grid[move.toRow][move.toCol] = movingPiece;
        grid[move.fromRow][move.fromCol] = null;
        movingPiece.row = move.toRow;
        movingPiece.col = move.toCol;

        // 체크 상태 확인
        boolean inCheck = isInCheck(color);

        // 원래 상태로 복원
        grid[move.fromRow][move.fromCol] = movingPiece;
        grid[move.toRow][move.toCol] = capturedPiece;
        movingPiece.row = origRow;
        movingPiece.col = origCol;

        return inCheck;
    }

    // ========== 체크 / 체크메이트 ==========

    /// <summary>
    /// 특정 색상의 킹이 체크 상태인지 확인
    /// 상대 기물 중 하나라도 킹의 위치를 공격할 수 있으면 체크
    /// </summary>
    public boolean isInCheck(int color) {
        int[] kingPos = findKing(color);
        if (kingPos == null) {
            return false;
        }

        int kingRow = kingPos[0];
        int kingCol = kingPos[1];

        // 모든 상대 기물을 순회하며 킹을 공격할 수 있는지 확인
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece piece = grid[r][c];
                if (piece == null || piece.color == color) {
                    continue;
                }

                // 상대 기물의 이동 가능한 칸에 킹 위치가 포함되면 체크
                int[][] moves = piece.getValidMoves(grid);
                for (int[] move : moves) {
                    if (move[0] == kingRow && move[1] == kingCol) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /// <summary>
    /// 특정 색상이 체크메이트 상태인지 확인
    /// 체크 상태이면서 합법적인 수가 하나도 없으면 체크메이트
    /// </summary>
    public boolean isCheckmate(int color) {
        if (!isInCheck(color)) {
            return false;
        }
        return getAllValidMoves(color).length == 0;
    }
}
