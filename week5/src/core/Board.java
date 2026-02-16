package core;

import java.util.ArrayList;
import piece.*;

/// <summary>
/// 체스판 추상 클래스
/// 8x8 격자, 기물 배치, 이동 실행, 보드 출력, 체크/체크메이트 판정
/// StandardBoard: 일반 체스, SkillBoard: 스킬+아이템 모드
/// </summary>
public abstract class Board {

    // ========== 상수 ==========

    // 체스판 크기
    public static final int SIZE = 8;

    // 한 팀의 최대 기물 수
    public static final int MAX_PIECES_PER_SIDE = 16;

    // 좌표 구성 요소 수 (행, 열)
    public static final int COORD_SIZE = 2;

    // ========== 필드 ==========

    // 8x8 격자 (빈 칸은 null)
    public final Piece[][] grid;

    // 마지막으로 실행된 수 (앙파상 판정에 사용)
    private Move lastMove;

    // 잡힌 기물 목록 (잡은 기물 표시에 사용, 하위 클래스에서 접근 가능)
    protected final ArrayList<Piece> capturedPieces;

    // 현재 print에서 사용 중인 유효 이동/대상 칸 수 (-1이면 미설정)
    // 스킬 대상 버퍼처럼 배열 일부만 유효한 경우, 외부에서 먼저 설정
    protected int validMoveCount = -1;

    // ========== 생성자 ==========

    /// <summary>
    /// 체스판 생성 및 기물 초기 배치
    /// </summary>
    public Board() {
        grid = new Piece[SIZE][SIZE];
        lastMove = null;
        capturedPieces = new ArrayList<>();
        initPieces();
    }

    // ========== 초기 배치 ==========

    /// <summary>
    /// 기물을 표준 체스 시작 위치에 배치
    /// 파란팀(상단 0~1행), 빨간팀(하단 6~7행)
    /// </summary>
    private void initPieces() {
        // 행 위치 상수
        final int BLUE_BACK_ROW = 0;   // 파란팀 주요 기물 행 (8번 줄)
        final int BLUE_PAWN_ROW = 1;   // 파란팀 폰 행 (7번 줄)
        final int RED_PAWN_ROW = 6;    // 빨간팀 폰 행 (2번 줄)
        final int RED_BACK_ROW = 7;    // 빨간팀 주요 기물 행 (1번 줄)

        // 열 위치 상수 (체스 표준 배치 순서)
        final int ROOK_LEFT = 0;       // a열 - 룩
        final int KNIGHT_LEFT = 1;     // b열 - 나이트
        final int BISHOP_LEFT = 2;     // c열 - 비숍
        final int QUEEN_COL = 3;       // d열 - 퀸
        final int KING_COL = 4;        // e열 - 킹
        final int BISHOP_RIGHT = 5;    // f열 - 비숍
        final int KNIGHT_RIGHT = 6;    // g열 - 나이트
        final int ROOK_RIGHT = 7;      // h열 - 룩

        // 파란팀 주요 기물
        grid[BLUE_BACK_ROW][ROOK_LEFT] = new Rook(Piece.BLUE, BLUE_BACK_ROW, ROOK_LEFT);
        grid[BLUE_BACK_ROW][KNIGHT_LEFT] = new Knight(Piece.BLUE, BLUE_BACK_ROW, KNIGHT_LEFT);
        grid[BLUE_BACK_ROW][BISHOP_LEFT] = new Bishop(Piece.BLUE, BLUE_BACK_ROW, BISHOP_LEFT);
        grid[BLUE_BACK_ROW][QUEEN_COL] = new Queen(Piece.BLUE, BLUE_BACK_ROW, QUEEN_COL);
        grid[BLUE_BACK_ROW][KING_COL] = new King(Piece.BLUE, BLUE_BACK_ROW, KING_COL);
        grid[BLUE_BACK_ROW][BISHOP_RIGHT] = new Bishop(Piece.BLUE, BLUE_BACK_ROW, BISHOP_RIGHT);
        grid[BLUE_BACK_ROW][KNIGHT_RIGHT] = new Knight(Piece.BLUE, BLUE_BACK_ROW, KNIGHT_RIGHT);
        grid[BLUE_BACK_ROW][ROOK_RIGHT] = new Rook(Piece.BLUE, BLUE_BACK_ROW, ROOK_RIGHT);

        // 파란팀 폰
        for (int c = 0; c < SIZE; c++) {
            grid[BLUE_PAWN_ROW][c] = new Pawn(Piece.BLUE, BLUE_PAWN_ROW, c);
        }

        // 빨간팀 폰
        for (int c = 0; c < SIZE; c++) {
            grid[RED_PAWN_ROW][c] = new Pawn(Piece.RED, RED_PAWN_ROW, c);
        }

        // 빨간팀 주요 기물
        grid[RED_BACK_ROW][ROOK_LEFT] = new Rook(Piece.RED, RED_BACK_ROW, ROOK_LEFT);
        grid[RED_BACK_ROW][KNIGHT_LEFT] = new Knight(Piece.RED, RED_BACK_ROW, KNIGHT_LEFT);
        grid[RED_BACK_ROW][BISHOP_LEFT] = new Bishop(Piece.RED, RED_BACK_ROW, BISHOP_LEFT);
        grid[RED_BACK_ROW][QUEEN_COL] = new Queen(Piece.RED, RED_BACK_ROW, QUEEN_COL);
        grid[RED_BACK_ROW][KING_COL] = new King(Piece.RED, RED_BACK_ROW, KING_COL);
        grid[RED_BACK_ROW][BISHOP_RIGHT] = new Bishop(Piece.RED, RED_BACK_ROW, BISHOP_RIGHT);
        grid[RED_BACK_ROW][KNIGHT_RIGHT] = new Knight(Piece.RED, RED_BACK_ROW, KNIGHT_RIGHT);
        grid[RED_BACK_ROW][ROOK_RIGHT] = new Rook(Piece.RED, RED_BACK_ROW, ROOK_RIGHT);
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
        // 외부에서 validMoveCount가 설정되지 않았으면 배열 길이 사용
        if (validMoveCount == -1) {
            validMoveCount = (validMoves != null) ? validMoves.length : 0;
        }

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
            System.out.println(line);
            System.out.println("   +---+---+---+---+---+---+---+---+");
        }

        // 하단 열 표시
        System.out.println("     a   b   c   d   e   f   g   h");

        // 잡은 기물 표시
        printCapturedPieces();

        // 렌더링 완료 후 초기화
        validMoveCount = -1;
    }

    /// <summary>
    /// 잡힌 기물을 팀별로 표시
    /// 가치 높은 순으로 정렬하여 보여줌
    /// </summary>
    protected void printCapturedPieces() {
        if (capturedPieces.isEmpty()) {
            return;
        }

        // 복사본을 만들어 가치 높은 순으로 정렬
        ArrayList<Piece> sorted = new ArrayList<>(capturedPieces);
        sorted.sort((a, b) -> b.value - a.value);

        StringBuilder redCaptures = new StringBuilder();   // 빨간팀이 잡은 기물 (파란색)
        StringBuilder blueCaptures = new StringBuilder();  // 파란팀이 잡은 기물 (빨간색)

        for (Piece p : sorted) {
            if (p.color == Piece.BLUE) {
                // 파란 기물이 잡힘 → 빨간팀이 잡은 것
                redCaptures.append(Util.BLUE).append(p.symbol).append(Util.RESET).append(" ");
            } else {
                // 빨간 기물이 잡힘 → 파란팀이 잡은 것
                blueCaptures.append(Util.RED).append(p.symbol).append(Util.RESET).append(" ");
            }
        }

        System.out.println();
        if (!redCaptures.isEmpty()) {
            System.out.println("  " + Util.RED + "빨간팀" + Util.RESET + " 획득: " + redCaptures.toString().trim());
        }
        if (!blueCaptures.isEmpty()) {
            System.out.println("  " + Util.BLUE + "파란팀" + Util.RESET + " 획득: " + blueCaptures.toString().trim());
        }
    }

    /// <summary>
    /// 한 칸의 표시 문자열을 결정
    /// 우선순위: 커서/선택 > 이동 가능 칸 > 일반
    /// 하위 클래스에서 오버라이드하여 추가 표시 가능
    /// </summary>
    protected String renderCell(int r, int c, int cursorRow, int cursorCol, int selectedRow, int selectedCol, int[][] validMoves) {
        Piece piece = grid[r][c];
        boolean isCursor = (r == cursorRow && c == cursorCol);
        boolean isSelected = (r == selectedRow && c == selectedCol);
        boolean isValidMove = isInArray(r, c, validMoves, validMoveCount);

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
    /// 배열 전체를 검사
    /// </summary>
    public boolean isInArray(int row, int col, int[][] array) {
        return isInArray(row, col, array, (array != null) ? array.length : 0);
    }

    /// <summary>
    /// 특정 좌표가 배열의 처음 count개 항목에 포함되어 있는지 확인
    /// 버퍼 배열에서 유효한 범위만 검사할 때 사용
    /// </summary>
    public boolean isInArray(int row, int col, int[][] array, int count) {
        if (array == null) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (array[i][0] == row && array[i][1] == col) {
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
    /// 캐슬링, 앙파상 등 특수 이동도 자동 감지하여 처리
    /// </summary>
    public void executeMove(Move move) {
        Piece piece = grid[move.fromRow][move.fromCol];

        // 캐슬링 감지 (킹이 2칸 이동)
        if (piece instanceof King && Math.abs(move.toCol - move.fromCol) == 2) {
            executeCastling(move);
            lastMove = move;
            return;
        }

        // 앙파상 감지 (폰이 대각선으로 빈 칸에 이동)
        if (piece instanceof Pawn && move.fromCol != move.toCol && grid[move.toRow][move.toCol] == null) {
            // 잡힌 폰 기록 및 제거
            capturedPieces.add(grid[move.fromRow][move.toCol]);
            grid[move.fromRow][move.toCol] = null;
        }

        // 일반 잡기 기록
        Piece captured = grid[move.toRow][move.toCol];
        if (captured != null) {
            capturedPieces.add(captured);
        }

        // 도착 칸에 기물 배치
        grid[move.toRow][move.toCol] = piece;
        grid[move.fromRow][move.fromCol] = null;

        // 기물의 위치 정보 갱신
        piece.row = move.toRow;
        piece.col = move.toCol;
        piece.hasMoved = true;

        lastMove = move;
    }

    /// <summary>
    /// 캐슬링 실행 (킹과 룩을 동시에 이동)
    /// 킹사이드: 킹 e→g, 룩 h→f
    /// 퀸사이드: 킹 e→c, 룩 a→d
    /// </summary>
    private void executeCastling(Move move) {
        Piece king = grid[move.fromRow][move.fromCol];

        // 킹 이동
        grid[move.toRow][move.toCol] = king;
        grid[move.fromRow][move.fromCol] = null;
        king.row = move.toRow;
        king.col = move.toCol;
        king.hasMoved = true;

        if (move.toCol > move.fromCol) {
            // 킹사이드: 룩 h열(7) → f열(5)
            Piece rook = grid[move.fromRow][7];
            grid[move.fromRow][5] = rook;
            grid[move.fromRow][7] = null;
            rook.row = move.fromRow;
            rook.col = 5;
            rook.hasMoved = true;
        } else {
            // 퀸사이드: 룩 a열(0) → d열(3)
            Piece rook = grid[move.fromRow][0];
            grid[move.fromRow][3] = rook;
            grid[move.fromRow][0] = null;
            rook.row = move.fromRow;
            rook.col = 3;
            rook.hasMoved = true;
        }
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// 마지막 이동이 프로모션 대상인지 확인
    /// 폰이 상대편 끝 줄에 도착하면 프로모션
    /// </summary>
    public boolean isPromotion(Move move) {
        Piece piece = grid[move.toRow][move.toCol];
        if (!(piece instanceof Pawn)) {
            return false;
        }
        // 빨간팀은 0행(8번 줄), 파란팀은 7행(1번 줄)이 끝
        return (piece.color == Piece.RED && move.toRow == 0) ||
               (piece.color == Piece.BLUE && move.toRow == 7);
    }

    /// <summary>
    /// 폰을 선택한 기물로 승격
    /// 1: 퀸, 2: 룩, 3: 비숍, 4: 나이트
    /// </summary>
    public void promote(int row, int col, int choice) {
        int color = grid[row][col].color;
        switch (choice) {
            case 1:
                grid[row][col] = new Queen(color, row, col);
                break;
            case 2:
                grid[row][col] = new Rook(color, row, col);
                break;
            case 3:
                grid[row][col] = new Bishop(color, row, col);
                break;
            case 4:
                grid[row][col] = new Knight(color, row, col);
                break;
        }
        // 승격된 기물은 이미 이동한 상태
        grid[row][col].hasMoved = true;
    }

    // ========== 이동 유효성 ==========

    /// <summary>
    /// 특정 기물의 합법적인 이동 가능 칸 목록 반환
    /// 기본 이동 규칙 + 특수 규칙(캐슬링, 앙파상) 포함
    /// 자기 킹이 위험해지는 수는 제외
    /// </summary>
    public int[][] getFilteredMoves(int row, int col) {
        Piece piece = grid[row][col];
        if (piece == null) {
            return new int[0][];
        }

        // 동결된 기물은 이동 불가
        if (piece.frozen) {
            return new int[0][];
        }

        int[][] rawMoves = piece.getValidMoves(grid);
        ArrayList<int[]> filtered = new ArrayList<>();

        for (int[] dest : rawMoves) {
            // 방패가 걸린 상대 기물은 잡을 수 없음
            Piece target = grid[dest[0]][dest[1]];
            if (target != null && target.shielded && target.color != piece.color) {
                continue;
            }

            Move move = new Move(row, col, dest[0], dest[1]);
            // 이 수를 두면 자기 킹이 체크되는지 확인
            if (!wouldBeInCheck(move, piece.color)) {
                filtered.add(dest);
            }
        }

        // 캐슬링 (킹이 아직 움직이지 않았을 때만)
        if (piece instanceof King && !piece.hasMoved) {
            addCastlingMoves(piece, filtered);
        }

        // 앙파상 (폰일 때만)
        if (piece instanceof Pawn) {
            addEnPassantMoves(piece, row, col, filtered);
        }

        return filtered.toArray(new int[0][]);
    }

    /// <summary>
    /// 특정 색상의 모든 합법적인 수 목록 반환
    /// 캐슬링, 앙파상 등 특수 규칙도 포함
    /// </summary>
    public Move[] getAllValidMoves(int color) {
        ArrayList<Move> allMoves = new ArrayList<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece piece = grid[r][c];
                if (piece == null || piece.color != color) {
                    continue;
                }

                // getFilteredMoves가 캐슬링, 앙파상도 포함하여 반환
                int[][] moves = getFilteredMoves(r, c);
                for (int[] dest : moves) {
                    allMoves.add(new Move(r, c, dest[0], dest[1]));
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

    // ========== 캐슬링 ==========

    /// <summary>
    /// 캐슬링 가능한 수를 목록에 추가
    /// 조건: 킹/룩 미이동, 사이에 기물 없음, 체크 아님, 경유 칸 공격 안 받음
    /// </summary>
    private void addCastlingMoves(Piece king, ArrayList<int[]> moves) {
        int row = king.row;
        int opponentColor = (king.color == Piece.RED) ? Piece.BLUE : Piece.RED;

        // 현재 체크 상태면 캐슬링 불가
        if (isInCheck(king.color)) {
            return;
        }

        // 킹사이드 캐슬링 (킹: e→g, 룩: h→f)
        Piece kingsideRook = grid[row][7];
        if (kingsideRook instanceof Rook && !kingsideRook.hasMoved) {
            // f열, g열이 비어있는지 확인
            if (grid[row][5] == null && grid[row][6] == null) {
                // f열, g열이 공격받지 않는지 확인
                if (!isSquareAttacked(row, 5, opponentColor) &&
                    !isSquareAttacked(row, 6, opponentColor)) {
                    moves.add(new int[]{row, 6});
                }
            }
        }

        // 퀸사이드 캐슬링 (킹: e→c, 룩: a→d)
        Piece queensideRook = grid[row][0];
        if (queensideRook instanceof Rook && !queensideRook.hasMoved) {
            // b열, c열, d열이 비어있는지 확인
            if (grid[row][1] == null && grid[row][2] == null && grid[row][3] == null) {
                // c열, d열이 공격받지 않는지 확인
                if (!isSquareAttacked(row, 2, opponentColor) &&
                    !isSquareAttacked(row, 3, opponentColor)) {
                    moves.add(new int[]{row, 2});
                }
            }
        }
    }

    /// <summary>
    /// 특정 칸이 상대 기물에게 공격받고 있는지 확인
    /// 캐슬링 경유 칸 검증에 사용
    /// </summary>
    private boolean isSquareAttacked(int row, int col, int attackerColor) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece piece = grid[r][c];
                if (piece == null || piece.color != attackerColor) {
                    continue;
                }

                // 해당 기물의 이동 가능한 칸에 목표 칸이 포함되면 공격받는 것
                int[][] pieceMoves = piece.getValidMoves(grid);
                for (int[] move : pieceMoves) {
                    if (move[0] == row && move[1] == col) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ========== 앙파상 ==========

    /// <summary>
    /// 앙파상 가능한 수를 목록에 추가
    /// 상대 폰이 바로 직전에 2칸 전진했고, 이 폰이 옆에 있으면 앙파상 가능
    /// </summary>
    private void addEnPassantMoves(Piece pawn, int row, int col, ArrayList<int[]> moves) {
        if (lastMove == null) {
            return;
        }

        // 마지막으로 이동한 기물이 폰인지 확인
        Piece lastPiece = grid[lastMove.toRow][lastMove.toCol];
        if (!(lastPiece instanceof Pawn)) {
            return;
        }

        // 상대 폰이 2칸 전진했는지 확인
        if (Math.abs(lastMove.toRow - lastMove.fromRow) != 2) {
            return;
        }

        // 같은 행에 있고 인접한 열에 있는지 확인
        if (lastMove.toRow != row) {
            return;
        }
        if (Math.abs(lastMove.toCol - col) != 1) {
            return;
        }

        // 방패가 걸린 폰은 앙파상으로 잡을 수 없음
        if (lastPiece.shielded) {
            return;
        }

        // 앙파상 도착 칸 (상대 폰이 지나온 빈 칸)
        int direction = (pawn.color == Piece.RED) ? -1 : 1;
        int enPassantRow = row + direction;
        int enPassantCol = lastMove.toCol;

        // 자기 킹이 위험해지지 않는지 확인 (앙파상은 잡히는 위치가 다르므로 별도 확인)
        Move enPassantMove = new Move(row, col, enPassantRow, enPassantCol);
        if (!wouldBeInCheckEnPassant(enPassantMove, pawn.color, lastMove.toRow, lastMove.toCol)) {
            moves.add(new int[]{enPassantRow, enPassantCol});
        }
    }

    /// <summary>
    /// 앙파상 수를 두면 자기 킹이 체크 상태가 되는지 시뮬레이션
    /// 일반 이동과 다르게 잡히는 폰이 도착 칸이 아닌 옆 칸에 있으므로 별도 처리
    /// </summary>
    private boolean wouldBeInCheckEnPassant(Move move, int color, int capturedRow, int capturedCol) {
        // 원래 상태 저장
        Piece movingPiece = grid[move.fromRow][move.fromCol];
        Piece capturedPiece = grid[capturedRow][capturedCol];
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;

        // 임시로 이동 + 잡힌 폰 제거
        grid[move.toRow][move.toCol] = movingPiece;
        grid[move.fromRow][move.fromCol] = null;
        grid[capturedRow][capturedCol] = null;
        movingPiece.row = move.toRow;
        movingPiece.col = move.toCol;

        // 체크 상태 확인
        boolean inCheck = isInCheck(color);

        // 원래 상태로 복원
        grid[move.fromRow][move.fromCol] = movingPiece;
        grid[move.toRow][move.toCol] = null;
        grid[capturedRow][capturedCol] = capturedPiece;
        movingPiece.row = origRow;
        movingPiece.col = origCol;

        return inCheck;
    }

    // ========== 체크 / 체크메이트 / 스테일메이트 ==========

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

    /// <summary>
    /// 특정 색상이 스테일메이트(무승부) 상태인지 확인
    /// 체크가 아닌데 합법적인 수가 하나도 없으면 스테일메이트
    /// </summary>
    public boolean isStalemate(int color) {
        if (isInCheck(color)) {
            return false;
        }
        return getAllValidMoves(color).length == 0;
    }
}
