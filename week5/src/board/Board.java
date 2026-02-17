package board;

import cell.Cell;
import core.Move;
import core.Util;
import java.util.ArrayList;
import piece.*;

/// <summary>
/// 체스판 추상 클래스
/// 8x8 격자, 기물 배치, 이동 실행, 보드 출력, 체크/체크메이트 판정
/// StandardBoard: 일반 체스, SkillBoard: 스킬+아이템 모드
/// </summary>
public abstract class Board {

    // ========== 상수 ==========

    /// <summary>
    /// 체스판 한 변의 칸 수 (8x8 정사각형 격자)
    /// </summary>
    public static final int SIZE = 8;

    /// <summary>
    /// 한 팀이 가질 수 있는 최대 기물 수 (킹1 + 퀸1 + 룩2 + 비숍2 + 나이트2 + 폰8 = 16)
    /// 스킬 대상 버퍼 배열 크기에 사용
    /// </summary>
    public static final int MAX_PIECES_PER_SIDE = 16;

    /// <summary>
    /// 하나의 좌표를 이루는 값의 수 (행, 열 = 2개)
    /// 스킬 대상 버퍼 배열의 내부 크기에 사용
    /// </summary>
    public static final int COORD_SIZE = 2;

    /// <summary>
    /// 위치가 지정되지 않았음을 나타내는 값
    /// 커서 없음, 선택 없음, 미설정 등에 사용
    /// </summary>
    public static final int NONE = -1;

    /// <summary>
    /// 좌표 배열에서 행(row) 값의 위치 (예: move[ROW])
    /// </summary>
    private static final int ROW = 0;

    /// <summary>
    /// 좌표 배열에서 열(col) 값의 위치 (예: move[COL])
    /// </summary>
    private static final int COL = 1;

    /// <summary>
    /// 이동 가능한 칸이 없음을 나타내는 빈 배열 (null 대신 사용)
    /// 한 번만 생성되어 재사용
    /// </summary>
    public static final int[][] EMPTY_MOVES = new int[0][COORD_SIZE];

    /// <summary>
    /// 필터링 후 한 기물의 최대 이동 가능 칸 수
    /// 기본 이동 최대 27칸 + 캐슬링 최대 2칸 + 앙파상 최대 1칸 = 30
    /// </summary>
    public static final int MAX_FILTERED_MOVES = 30;

    // ========== 캐슬링 열 위치 ==========

    // 킹사이드 (킹: e→g, 룩: h→f)
    private static final int KINGSIDE_ROOK_COL = 7;   // h열 - 룩 초기 위치
    private static final int KINGSIDE_ROOK_DEST = 5;  // f열 - 룩 도착 위치
    private static final int KINGSIDE_KING_DEST = 6;  // g열 - 킹 도착 위치

    // 퀸사이드 (킹: e→c, 룩: a→d)
    private static final int QUEENSIDE_ROOK_COL = 0;  // a열 - 룩 초기 위치
    private static final int QUEENSIDE_ROOK_DEST = 3; // d열 - 룩 도착 위치
    private static final int QUEENSIDE_KING_DEST = 2; // c열 - 킹 도착 위치
    private static final int QUEENSIDE_PATH_COL = 1;  // b열 - 퀸사이드 경유 칸

    // ========== 필드 ==========

    // 8x8 격자 (각 칸은 Cell 객체, 기물이 없으면 빈 Cell)
    public final Cell[][] grid;

    // 마지막으로 실행된 수 (앙파상 판정에 사용)
    private Move lastMove;

    // 잡힌 기물 목록 (잡은 기물 표시에 사용, 하위 클래스에서 접근 가능)
    protected final ArrayList<Piece> capturedPieces;

    // 현재 print에서 사용 중인 유효 이동/대상 칸 수
    protected int validMoveCount;

    // 필터링된 이동 가능 칸 버퍼 (매번 새로 만들지 않고 재사용)
    private final int[][] filteredBuffer = new int[MAX_FILTERED_MOVES][COORD_SIZE];

    // 현재 유효한 필터링된 이동 칸 수
    private int filteredCount;

    // ========== 생성자 ==========

    /// <summary>
    /// 체스판 생성 및 기물 초기 배치
    /// </summary>
    public Board() {
        grid = new Cell[SIZE][SIZE];

        // 각 칸을 빈 Cell로 초기화 (하위 클래스가 createCell()을 오버라이드하여 다른 종류의 칸 생성 가능)
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = createCell();
            }
        }

        lastMove = null;
        capturedPieces = new ArrayList<>();
        initPieces();
    }

    /// <summary>
    /// 칸 객체를 생성하는 팩토리 메서드
    /// 하위 클래스에서 오버라이드하여 확장된 칸(SkillCell 등)을 생성 가능
    /// </summary>
    protected Cell createCell() {
        return new Cell();
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
        grid[BLUE_BACK_ROW][ROOK_LEFT].setPiece(new Rook(Piece.BLUE, BLUE_BACK_ROW, ROOK_LEFT));
        grid[BLUE_BACK_ROW][KNIGHT_LEFT].setPiece(new Knight(Piece.BLUE, BLUE_BACK_ROW, KNIGHT_LEFT));
        grid[BLUE_BACK_ROW][BISHOP_LEFT].setPiece(new Bishop(Piece.BLUE, BLUE_BACK_ROW, BISHOP_LEFT));
        grid[BLUE_BACK_ROW][QUEEN_COL].setPiece(new Queen(Piece.BLUE, BLUE_BACK_ROW, QUEEN_COL));
        grid[BLUE_BACK_ROW][KING_COL].setPiece(new King(Piece.BLUE, BLUE_BACK_ROW, KING_COL));
        grid[BLUE_BACK_ROW][BISHOP_RIGHT].setPiece(new Bishop(Piece.BLUE, BLUE_BACK_ROW, BISHOP_RIGHT));
        grid[BLUE_BACK_ROW][KNIGHT_RIGHT].setPiece(new Knight(Piece.BLUE, BLUE_BACK_ROW, KNIGHT_RIGHT));
        grid[BLUE_BACK_ROW][ROOK_RIGHT].setPiece(new Rook(Piece.BLUE, BLUE_BACK_ROW, ROOK_RIGHT));

        // 파란팀 폰
        for (int c = 0; c < SIZE; c++) {
            grid[BLUE_PAWN_ROW][c].setPiece(new Pawn(Piece.BLUE, BLUE_PAWN_ROW, c));
        }

        // 빨간팀 폰
        for (int c = 0; c < SIZE; c++) {
            grid[RED_PAWN_ROW][c].setPiece(new Pawn(Piece.RED, RED_PAWN_ROW, c));
        }

        // 빨간팀 주요 기물
        grid[RED_BACK_ROW][ROOK_LEFT].setPiece(new Rook(Piece.RED, RED_BACK_ROW, ROOK_LEFT));
        grid[RED_BACK_ROW][KNIGHT_LEFT].setPiece(new Knight(Piece.RED, RED_BACK_ROW, KNIGHT_LEFT));
        grid[RED_BACK_ROW][BISHOP_LEFT].setPiece(new Bishop(Piece.RED, RED_BACK_ROW, BISHOP_LEFT));
        grid[RED_BACK_ROW][QUEEN_COL].setPiece(new Queen(Piece.RED, RED_BACK_ROW, QUEEN_COL));
        grid[RED_BACK_ROW][KING_COL].setPiece(new King(Piece.RED, RED_BACK_ROW, KING_COL));
        grid[RED_BACK_ROW][BISHOP_RIGHT].setPiece(new Bishop(Piece.RED, RED_BACK_ROW, BISHOP_RIGHT));
        grid[RED_BACK_ROW][KNIGHT_RIGHT].setPiece(new Knight(Piece.RED, RED_BACK_ROW, KNIGHT_RIGHT));
        grid[RED_BACK_ROW][ROOK_RIGHT].setPiece(new Rook(Piece.RED, RED_BACK_ROW, ROOK_RIGHT));
    }

    // ========== 보드 출력 ==========

    /// <summary>
    /// 기본 보드 출력 (커서, 하이라이트 없음)
    /// </summary>
    public void print() {
        print(NONE, NONE, NONE, NONE, EMPTY_MOVES, 0);
    }

    /// <summary>
    /// 커서만 표시하며 보드 출력 (기물 탐색 모드)
    /// </summary>
    public void print(int cursorRow, int cursorCol) {
        print(cursorRow, cursorCol, NONE, NONE, EMPTY_MOVES, 0);
    }

    /// <summary>
    /// 보드 출력 (커서 + 선택된 기물 + 이동 가능한 칸 표시)
    /// cursorRow/Col: 현재 커서 위치 (NONE이면 커서 없음)
    /// selectedRow/Col: 선택된 기물 위치 (NONE이면 선택 없음)
    /// validMoves: 이동 가능한 칸 버퍼
    /// validMoveCount: 버퍼에서 유효한 칸 수
    /// </summary>
    public void print(int cursorRow, int cursorCol, int selectedRow, int selectedCol, int[][] validMoves, int validMoveCount) {
        this.validMoveCount = validMoveCount;

        // === 보드 출력 시작 ===

        // 상단 열 레이블 (a~h)
        System.out.println("     a   b   c   d   e   f   g   h");
        System.out.println("   +---+---+---+---+---+---+---+---+");

        // 8x8 격자 한 행씩 출력
        for (int r = 0; r < SIZE; r++) {
            // 내부 행 번호(0~7)를 체스 줄 번호(8~1)로 변환
            int rank = SIZE - r;

            // 한 행을 문자열로 조립 (예: " 8 | r | n | b | q | k | b | n | r | 8")
            StringBuilder line = new StringBuilder();
            line.append(String.format(" %d |", rank));

            for (int c = 0; c < SIZE; c++) {
                // 각 칸의 표시 문자열 결정 (기물, 커서, 이동 가능 표시 등)
                String cell = renderCell(r, c, cursorRow, cursorCol, selectedRow, selectedCol, validMoves);
                line.append(cell).append("|");
            }

            line.append(String.format(" %d", rank));
            System.out.println(line);
            System.out.println("   +---+---+---+---+---+---+---+---+");
        }

        // 하단 열 레이블 (a~h)
        System.out.println("     a   b   c   d   e   f   g   h");

        // 잡은 기물 표시
        printCapturedPieces();

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
        boolean hasPiece = grid[r][c].hasPiece();
        boolean isCursor = (r == cursorRow && c == cursorCol);
        boolean isSelected = (r == selectedRow && c == selectedCol);
        boolean isValidMove = isInArray(r, c, validMoves, validMoveCount);

        // 1순위: 커서 또는 선택된 기물 → 대괄호로 감싸기
        if (isCursor || isSelected) {
            if (hasPiece) {
                Piece piece = grid[r][c].getPiece();
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
        if (hasPiece) {
            Piece piece = grid[r][c].getPiece();
            String colorCode = (piece.color == Piece.RED) ? Util.RED : Util.BLUE;
            return " " + colorCode + piece.symbol + Util.RESET + " ";
        }
        return "   ";
    }

    /// <summary>
    /// 특정 좌표가 배열의 처음 count개 항목에 포함되어 있는지 확인
    /// 버퍼 배열에서 유효한 범위만 검사할 때 사용
    /// </summary>
    public boolean isInArray(int row, int col, int[][] array, int count) {
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
        return grid[row][col].getPiece();
    }

    /// <summary>
    /// 특정 색상의 킹 위치를 {행, 열} 배열로 반환
    /// </summary>
    public int[] findKing(int color) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Piece piece = grid[r][c].getPiece();
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
        Piece piece = grid[move.fromRow][move.fromCol].getPiece();

        // 캐슬링 감지 (킹이 2칸 이동)
        if (piece instanceof King && Math.abs(move.toCol - move.fromCol) == 2) {
            executeCastling(move);
            lastMove = move;
            return;
        }

        // 앙파상 감지 (폰이 대각선으로 빈 칸에 이동)
        if (piece instanceof Pawn && move.fromCol != move.toCol && grid[move.toRow][move.toCol].isEmpty()) {
            // 잡힌 폰 기록 및 제거
            capturedPieces.add(grid[move.fromRow][move.toCol].getPiece());
            grid[move.fromRow][move.toCol].removePiece();
        }

        // 일반 잡기 기록
        if (grid[move.toRow][move.toCol].hasPiece()) {
            capturedPieces.add(grid[move.toRow][move.toCol].getPiece());
        }

        // 도착 칸에 기물 배치
        grid[move.toRow][move.toCol].setPiece(piece);
        grid[move.fromRow][move.fromCol].removePiece();

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
        Piece king = grid[move.fromRow][move.fromCol].getPiece();

        // 킹 이동
        grid[move.toRow][move.toCol].setPiece(king);
        grid[move.fromRow][move.fromCol].removePiece();
        king.row = move.toRow;
        king.col = move.toCol;
        king.hasMoved = true;

        if (move.toCol > move.fromCol) {
            // 킹사이드: 룩 h열 → f열
            Piece rook = grid[move.fromRow][KINGSIDE_ROOK_COL].getPiece();
            grid[move.fromRow][KINGSIDE_ROOK_DEST].setPiece(rook);
            grid[move.fromRow][KINGSIDE_ROOK_COL].removePiece();
            rook.row = move.fromRow;
            rook.col = KINGSIDE_ROOK_DEST;
            rook.hasMoved = true;
        } else {
            // 퀸사이드: 룩 a열 → d열
            Piece rook = grid[move.fromRow][QUEENSIDE_ROOK_COL].getPiece();
            grid[move.fromRow][QUEENSIDE_ROOK_DEST].setPiece(rook);
            grid[move.fromRow][QUEENSIDE_ROOK_COL].removePiece();
            rook.row = move.fromRow;
            rook.col = QUEENSIDE_ROOK_DEST;
            rook.hasMoved = true;
        }
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// 마지막 이동이 프로모션 대상인지 확인
    /// 폰이 상대편 끝 줄에 도착하면 프로모션
    /// </summary>
    public boolean isPromotion(Move move) {
        Piece piece = grid[move.toRow][move.toCol].getPiece();
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
        int color = grid[row][col].getPiece().color;
        switch (choice) {
            case 1:
                grid[row][col].setPiece(new Queen(color, row, col));
                break;
            case 2:
                grid[row][col].setPiece(new Rook(color, row, col));
                break;
            case 3:
                grid[row][col].setPiece(new Bishop(color, row, col));
                break;
            case 4:
                grid[row][col].setPiece(new Knight(color, row, col));
                break;
        }
        // 승격된 기물은 이미 이동한 상태
        grid[row][col].getPiece().hasMoved = true;
    }

    // ========== 이동 유효성 ==========

    /// <summary>
    /// 특정 기물의 합법적인 이동 가능 칸 목록 반환
    /// 기본 이동 규칙 + 특수 규칙(캐슬링, 앙파상) 포함
    /// 자기 킹이 위험해지는 수는 제외
    /// </summary>
    public int getFilteredMoves(int row, int col) {
        filteredCount = 0;

        if (grid[row][col].isEmpty()) {
            return 0;
        }
        Piece piece = grid[row][col].getPiece();

        // 동결된 기물은 이동 불가
        if (piece.frozen) {
            return 0;
        }

        int rawCount = piece.getValidMoves(grid);

        for (int i = 0; i < rawCount; i++) {
            int destRow = piece.moveBuffer[i][0];
            int destCol = piece.moveBuffer[i][1];

            // 방패가 걸린 상대 기물은 잡을 수 없음
            if (grid[destRow][destCol].hasPiece()) {
                Piece target = grid[destRow][destCol].getPiece();
                if (target.shielded && target.color != piece.color) {
                    continue;
                }
            }

            Move move = new Move(row, col, destRow, destCol);
            // 이 수를 두면 자기 킹이 체크되는지 확인
            if (!wouldBeInCheck(move, piece.color)) {
                filteredBuffer[filteredCount][0] = destRow;
                filteredBuffer[filteredCount][1] = destCol;
                filteredCount++;
            }
        }

        // 캐슬링 (킹이 아직 움직이지 않았을 때만)
        if (piece instanceof King && !piece.hasMoved) {
            addCastlingMoves(piece);
        }

        // 앙파상 (폰일 때만)
        if (piece instanceof Pawn) {
            addEnPassantMoves(piece, row, col);
        }

        return filteredCount;
    }

    /// <summary>
    /// 필터링된 이동 가능 칸 버퍼 반환 (getFilteredMoves와 함께 사용)
    /// getFilteredMoves의 반환값이 유효한 칸 수이고, 이 버퍼에서 그 수만큼만 유효
    /// </summary>
    public int[][] getFilteredBuffer() {
        return filteredBuffer;
    }

    /// <summary>
    /// 특정 색상의 모든 합법적인 수 목록 반환
    /// 캐슬링, 앙파상 등 특수 규칙도 포함
    /// </summary>
    public Move[] getAllValidMoves(int color) {
        ArrayList<Move> allMoves = new ArrayList<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].isEmpty()) {
                    continue;
                }
                Piece piece = grid[r][c].getPiece();
                if (piece.color != color) {
                    continue;
                }

                // getFilteredMoves가 캐슬링, 앙파상도 포함하여 반환
                int moveCount = getFilteredMoves(r, c);
                for (int i = 0; i < moveCount; i++) {
                    allMoves.add(new Move(r, c, filteredBuffer[i][0], filteredBuffer[i][1]));
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
        Piece movingPiece = grid[move.fromRow][move.fromCol].getPiece();
        Piece capturedPiece = grid[move.toRow][move.toCol].getPiece();
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;

        // 임시로 이동 실행
        grid[move.toRow][move.toCol].setPiece(movingPiece);
        grid[move.fromRow][move.fromCol].removePiece();
        movingPiece.row = move.toRow;
        movingPiece.col = move.toCol;

        // 체크 상태 확인
        boolean inCheck = isInCheck(color);

        // 원래 상태로 복원
        grid[move.fromRow][move.fromCol].setPiece(movingPiece);
        grid[move.toRow][move.toCol].setPiece(capturedPiece);
        movingPiece.row = origRow;
        movingPiece.col = origCol;

        return inCheck;
    }

    // ========== 캐슬링 ==========

    /// <summary>
    /// 캐슬링 가능한 수를 목록에 추가
    /// 조건: 킹/룩 미이동, 사이에 기물 없음, 체크 아님, 경유 칸 공격 안 받음
    /// </summary>
    private void addCastlingMoves(Piece king) {
        int row = king.row;
        int opponentColor = (king.color == Piece.RED) ? Piece.BLUE : Piece.RED;

        // 현재 체크 상태면 캐슬링 불가
        if (isInCheck(king.color)) {
            return;
        }

        // 킹사이드 캐슬링 (킹: e→g, 룩: h→f)
        Piece kingsideRook = grid[row][KINGSIDE_ROOK_COL].getPiece();

        // h열에 룩이 있고 한 번도 움직이지 않았는지
        boolean kingsideRookReady = kingsideRook instanceof Rook && !kingsideRook.hasMoved;
        // 킹과 룩 사이 칸(f열, g열)이 비어있는지
        boolean kingsidePathClear = grid[row][KINGSIDE_ROOK_DEST].isEmpty() && grid[row][KINGSIDE_KING_DEST].isEmpty();
        // 킹이 지나가는 칸(f열, g열)이 상대에게 공격받지 않는지
        boolean kingsidePathSafe = isSquareSafe(row, KINGSIDE_ROOK_DEST, opponentColor) && isSquareSafe(row, KINGSIDE_KING_DEST, opponentColor);

        if (kingsideRookReady && kingsidePathClear && kingsidePathSafe) {
            filteredBuffer[filteredCount][0] = row;
            filteredBuffer[filteredCount][1] = KINGSIDE_KING_DEST;
            filteredCount++;
        }

        // 퀸사이드 캐슬링 (킹: e→c, 룩: a→d)
        Piece queensideRook = grid[row][QUEENSIDE_ROOK_COL].getPiece();

        // a열에 룩이 있고 한 번도 움직이지 않았는지
        boolean queensideRookReady = queensideRook instanceof Rook && !queensideRook.hasMoved;
        // 킹과 룩 사이 칸(b열, c열, d열)이 비어있는지
        boolean queensidePathClear = grid[row][QUEENSIDE_PATH_COL].isEmpty() && grid[row][QUEENSIDE_KING_DEST].isEmpty() && grid[row][QUEENSIDE_ROOK_DEST].isEmpty();
        // 킹이 지나가는 칸(c열, d열)이 상대에게 공격받지 않는지
        boolean queensidePathSafe = isSquareSafe(row, QUEENSIDE_KING_DEST, opponentColor) && isSquareSafe(row, QUEENSIDE_ROOK_DEST, opponentColor);

        if (queensideRookReady && queensidePathClear && queensidePathSafe) {
            filteredBuffer[filteredCount][0] = row;
            filteredBuffer[filteredCount][1] = QUEENSIDE_KING_DEST;
            filteredCount++;
        }
    }

    /// <summary>
    /// 특정 칸이 상대 기물에게 공격받지 않는 안전한 칸인지 확인
    /// 캐슬링 경유 칸 검증에 사용
    /// </summary>
    private boolean isSquareSafe(int row, int col, int attackerColor) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].isEmpty()) {
                    continue;
                }
                Piece piece = grid[r][c].getPiece();
                if (piece.color != attackerColor) {
                    continue;
                }

                // 해당 기물의 이동 가능한 칸에 목표 칸이 포함되면 안전하지 않음
                int moveCount = piece.getValidMoves(grid);
                for (int i = 0; i < moveCount; i++) {
                    if (piece.moveBuffer[i][0] == row && piece.moveBuffer[i][1] == col) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // ========== 앙파상 ==========

    /// <summary>
    /// 앙파상 가능한 수를 목록에 추가
    /// 상대 폰이 바로 직전에 2칸 전진했고, 이 폰이 옆에 있으면 앙파상 가능
    /// </summary>
    private void addEnPassantMoves(Piece pawn, int row, int col) {
        // 폰이 첫 이동 시 전진하는 행 수
        final int PAWN_DOUBLE_STEP = 2;
        // 인접한 열 거리 (바로 옆 1칸)
        final int ADJACENT_COL_DISTANCE = 1;

        if (lastMove == null) {
            return;
        }

        // 마지막으로 이동한 기물이 폰인지 확인
        Piece lastPiece = grid[lastMove.toRow][lastMove.toCol].getPiece();
        if (!(lastPiece instanceof Pawn)) {
            return;
        }

        // 상대 폰이 2칸 전진했는지 확인
        if (Math.abs(lastMove.toRow - lastMove.fromRow) != PAWN_DOUBLE_STEP) {
            return;
        }

        // 같은 행에 있고 바로 옆 열에 있는지 확인
        if (lastMove.toRow != row) {
            return;
        }
        if (Math.abs(lastMove.toCol - col) != ADJACENT_COL_DISTANCE) {
            return;
        }

        // 방패가 걸린 폰은 앙파상으로 잡을 수 없음
        if (lastPiece.shielded) {
            return;
        }

        // 앙파상 도착 칸 (상대 폰이 지나온 빈 칸)
        int direction = (pawn.color == Piece.RED) ? Piece.RED_DIRECTION : Piece.BLUE_DIRECTION;
        int enPassantRow = row + direction;
        int enPassantCol = lastMove.toCol;

        // 자기 킹이 위험해지지 않는지 확인 (앙파상은 잡히는 위치가 다르므로 별도 확인)
        Move enPassantMove = new Move(row, col, enPassantRow, enPassantCol);
        if (!wouldBeInCheckEnPassant(enPassantMove, pawn.color, lastMove.toRow, lastMove.toCol)) {
            filteredBuffer[filteredCount][0] = enPassantRow;
            filteredBuffer[filteredCount][1] = enPassantCol;
            filteredCount++;
        }
    }

    /// <summary>
    /// 앙파상 수를 두면 자기 킹이 체크 상태가 되는지 시뮬레이션
    /// 일반 이동과 다르게 잡히는 폰이 도착 칸이 아닌 옆 칸에 있으므로 별도 처리
    /// </summary>
    private boolean wouldBeInCheckEnPassant(Move move, int color, int capturedRow, int capturedCol) {
        // 원래 상태 저장
        Piece movingPiece = grid[move.fromRow][move.fromCol].getPiece();
        Piece capturedPiece = grid[capturedRow][capturedCol].getPiece();
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;

        // 임시로 이동 + 잡힌 폰 제거
        grid[move.toRow][move.toCol].setPiece(movingPiece);
        grid[move.fromRow][move.fromCol].removePiece();
        grid[capturedRow][capturedCol].removePiece();
        movingPiece.row = move.toRow;
        movingPiece.col = move.toCol;

        // 체크 상태 확인
        boolean inCheck = isInCheck(color);

        // 원래 상태로 복원
        grid[move.fromRow][move.fromCol].setPiece(movingPiece);
        grid[move.toRow][move.toCol].removePiece();
        grid[capturedRow][capturedCol].setPiece(capturedPiece);
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

        int kingRow = kingPos[ROW];
        int kingCol = kingPos[COL];

        // 모든 상대 기물을 순회하며 킹을 공격할 수 있는지 확인
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].isEmpty()) {
                    continue;
                }
                Piece piece = grid[r][c].getPiece();
                if (piece.color == color) {
                    continue;
                }

                // 상대 기물의 이동 가능한 칸에 킹 위치가 포함되면 체크
                int moveCount = piece.getValidMoves(grid);
                for (int i = 0; i < moveCount; i++) {
                    if (piece.moveBuffer[i][ROW] == kingRow && piece.moveBuffer[i][COL] == kingCol) {
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
        return hasNoValidMoves(color);
    }

    /// <summary>
    /// 특정 색상이 스테일메이트(무승부) 상태인지 확인
    /// 체크가 아닌데 합법적인 수가 하나도 없으면 스테일메이트
    /// </summary>
    public boolean isStalemate(int color) {
        if (isInCheck(color)) {
            return false;
        }
        return hasNoValidMoves(color);
    }

    /// <summary>
    /// 특정 색상에 합법적인 수가 하나도 없는지 확인
    /// </summary>
    private boolean hasNoValidMoves(int color) {
        return getAllValidMoves(color).length == 0;
    }
}
