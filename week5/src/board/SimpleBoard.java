package board;

import cell.Cell;
import core.Move;
import core.Chess;
import core.Util;
import java.util.ArrayList;
import piece.*;

/// <summary>
/// 체스판 추상 클래스
/// 8x8 격자, 기물 배치, 기본 이동, 보드 출력, 체크/체크메이트 판정
/// SimpleBoard: 기본 체스, ClassicBoard: 공식 체스, SkillBoard: 스킬+아이템 모드
/// </summary>
public class SimpleBoard {

    // ========== 상수 ==========

    /// <summary>
    /// 좌표 배열에서 행(row) 값의 위치 (예: move[ROW])
    /// </summary>
    protected static final int ROW = 0;

    /// <summary>
    /// 좌표 배열에서 열(col) 값의 위치 (예: move[COL])
    /// </summary>
    protected static final int COL = 1;

    /// <summary>
    /// 이동 가능한 칸이 없음을 나타내는 빈 배열 (null 대신 사용)
    /// 한 번만 생성되어 재사용
    /// </summary>
    public static final int[][] EMPTY_MOVES = new int[0][Chess.COORD_SIZE];

    /// <summary>
    /// 필터링 후 한 기물의 최대 이동 가능 칸 수
    /// 기본 이동 최대 27칸 + 특수 규칙 여유분 = 30
    /// </summary>
    public static final int MAX_FILTERED_MOVES = 30;

    // ========== 필드 ==========

    /// <summary>
    /// 8x8 격자 (각 칸은 Cell 객체, 기물이 없으면 빈 Cell)
    /// </summary>
    public final Cell[][] grid;

    /// <summary>
    /// 잡힌 기물 목록 (잡은 기물 표시에 사용, 하위 클래스에서 접근 가능)
    /// </summary>
    protected final ArrayList<Piece> capturedPieces;

    // 빨간팀 킹 참조 (체크 판정, 커서 초기 위치 등에 사용)
    private Piece redKing;

    // 파란팀 킹 참조
    private Piece blueKing;

    // 현재 print에서 사용 중인 유효 이동/대상 칸 수
    protected int validMoveCount;

    /// <summary>
    /// 필터링된 이동 가능 칸 버퍼 (매번 새로 만들지 않고 재사용)
    /// 하위 클래스에서 특수 규칙 이동을 추가할 때 접근 필요
    /// </summary>
    protected final int[][] filteredBuffer = new int[MAX_FILTERED_MOVES][Chess.COORD_SIZE];

    /// <summary>
    /// 현재 유효한 필터링된 이동 칸 수
    /// 하위 클래스에서 특수 규칙 이동을 추가할 때 접근 필요
    /// </summary>
    protected int filteredCount;

    /// <summary>
    /// 특정 색상의 모든 합법적인 수를 모아두는 목록
    /// getAllValidMoves() 호출 시 매번 새로 만들지 않고 재사용
    /// </summary>
    private final ArrayList<Move> allMoves = new ArrayList<>();

    /// <summary>
    /// 이동 시뮬레이션용 임시 Move (체크 판정 시 매번 새로 만들지 않고 재사용)
    /// 하위 클래스에서 특수 규칙 체크 판정에 접근 필요
    /// </summary>
    protected final Move tempMove = new Move(0, 0, 0, 0);

    /// <summary>
    /// 잡힌 기물을 가치순으로 정렬하여 표시할 때 사용하는 목록
    /// capturedPieces의 내용을 복사해서 정렬 (원본 순서를 바꾸지 않기 위해)
    /// 매번 새로 만들지 않고 재사용
    /// </summary>
    private final ArrayList<Piece> sortedCapturedPieces = new ArrayList<>();

    // 잡힌 기물 표시용 문자열 조립기 (매번 새로 만들지 않고 재사용)
    private final StringBuilder redCaptures = new StringBuilder();
    private final StringBuilder blueCaptures = new StringBuilder();

    // 보드 아래에 표시할 추가 메시지 (시연 모드 등에서 사용)
    protected String footerMessage = "";

    // ========== 생성자 ==========

    /// <summary>
    /// 체스판 생성 및 기물 초기 배치
    /// </summary>
    public SimpleBoard() {
        grid = new Cell[Chess.BOARD_SIZE][Chess.BOARD_SIZE];

        // 각 칸을 빈 Cell로 초기화 (하위 클래스가 createCell()을 오버라이드하여 다른 종류의 칸 생성 가능)
        for (int r = 0; r < Chess.BOARD_SIZE; r++) {
            for (int c = 0; c < Chess.BOARD_SIZE; c++) {
                grid[r][c] = createCell();
            }
        }

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

    /// <summary>
    /// 기물 객체를 생성하는 팩토리 메서드
    /// 하위 클래스에서 오버라이드하여 확장된 기물(SkillPiece 등)을 생성 가능
    /// </summary>
    protected Piece createPiece(PieceType type, int color, int row, int col) {
        return new Piece(type, color, row, col);
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
        grid[BLUE_BACK_ROW][ROOK_LEFT].setPiece(createPiece(PieceType.ROOK, Chess.BLUE, BLUE_BACK_ROW, ROOK_LEFT));
        grid[BLUE_BACK_ROW][KNIGHT_LEFT].setPiece(createPiece(PieceType.KNIGHT, Chess.BLUE, BLUE_BACK_ROW, KNIGHT_LEFT));
        grid[BLUE_BACK_ROW][BISHOP_LEFT].setPiece(createPiece(PieceType.BISHOP, Chess.BLUE, BLUE_BACK_ROW, BISHOP_LEFT));
        grid[BLUE_BACK_ROW][QUEEN_COL].setPiece(createPiece(PieceType.QUEEN, Chess.BLUE, BLUE_BACK_ROW, QUEEN_COL));
        blueKing = createPiece(PieceType.KING, Chess.BLUE, BLUE_BACK_ROW, KING_COL);
        grid[BLUE_BACK_ROW][KING_COL].setPiece(blueKing);
        grid[BLUE_BACK_ROW][BISHOP_RIGHT].setPiece(createPiece(PieceType.BISHOP, Chess.BLUE, BLUE_BACK_ROW, BISHOP_RIGHT));
        grid[BLUE_BACK_ROW][KNIGHT_RIGHT].setPiece(createPiece(PieceType.KNIGHT, Chess.BLUE, BLUE_BACK_ROW, KNIGHT_RIGHT));
        grid[BLUE_BACK_ROW][ROOK_RIGHT].setPiece(createPiece(PieceType.ROOK, Chess.BLUE, BLUE_BACK_ROW, ROOK_RIGHT));

        // 파란팀 폰
        for (int c = 0; c < Chess.BOARD_SIZE; c++) {
            grid[BLUE_PAWN_ROW][c].setPiece(createPiece(PieceType.PAWN, Chess.BLUE, BLUE_PAWN_ROW, c));
        }

        // 빨간팀 폰
        for (int c = 0; c < Chess.BOARD_SIZE; c++) {
            grid[RED_PAWN_ROW][c].setPiece(createPiece(PieceType.PAWN, Chess.RED, RED_PAWN_ROW, c));
        }

        // 빨간팀 주요 기물
        grid[RED_BACK_ROW][ROOK_LEFT].setPiece(createPiece(PieceType.ROOK, Chess.RED, RED_BACK_ROW, ROOK_LEFT));
        grid[RED_BACK_ROW][KNIGHT_LEFT].setPiece(createPiece(PieceType.KNIGHT, Chess.RED, RED_BACK_ROW, KNIGHT_LEFT));
        grid[RED_BACK_ROW][BISHOP_LEFT].setPiece(createPiece(PieceType.BISHOP, Chess.RED, RED_BACK_ROW, BISHOP_LEFT));
        grid[RED_BACK_ROW][QUEEN_COL].setPiece(createPiece(PieceType.QUEEN, Chess.RED, RED_BACK_ROW, QUEEN_COL));
        redKing = createPiece(PieceType.KING, Chess.RED, RED_BACK_ROW, KING_COL);
        grid[RED_BACK_ROW][KING_COL].setPiece(redKing);
        grid[RED_BACK_ROW][BISHOP_RIGHT].setPiece(createPiece(PieceType.BISHOP, Chess.RED, RED_BACK_ROW, BISHOP_RIGHT));
        grid[RED_BACK_ROW][KNIGHT_RIGHT].setPiece(createPiece(PieceType.KNIGHT, Chess.RED, RED_BACK_ROW, KNIGHT_RIGHT));
        grid[RED_BACK_ROW][ROOK_RIGHT].setPiece(createPiece(PieceType.ROOK, Chess.RED, RED_BACK_ROW, ROOK_RIGHT));
    }

    // ========== 보드 커스터마이즈 (시연 모드 등) ==========

    /// <summary>
    /// 보드 아래에 표시할 추가 메시지를 설정
    /// 빈 문자열이면 표시하지 않음
    /// </summary>
    public void setFooterMessage(String message) {
        this.footerMessage = message;
    }

    /// <summary>
    /// 보드의 모든 기물을 제거
    /// 커스텀 배치를 위해 빈 보드를 만들 때 사용
    /// </summary>
    public void clearAllPieces() {
        for (int r = 0; r < Chess.BOARD_SIZE; r++) {
            for (int c = 0; c < Chess.BOARD_SIZE; c++) {
                if (grid[r][c].hasPiece()) {
                    grid[r][c].removePiece();
                }
            }
        }
        capturedPieces.clear();
        redKing = null;
        blueKing = null;
    }

    /// <summary>
    /// 특정 위치에 기물을 배치
    /// 킹은 자동으로 내부 참조에 등록됨 (체크 판정에 필요)
    /// createPiece()를 사용하므로 하위 클래스의 기물 타입에 맞게 생성됨
    /// </summary>
    public void placePiece(PieceType type, int color, int row, int col) {
        Piece piece = createPiece(type, color, row, col);
        grid[row][col].setPiece(piece);

        // 킹이면 내부 참조 등록 (체크/체크메이트 판정에 사용)
        if (type == PieceType.KING) {
            if (color == Chess.RED) {
                redKing = piece;
            } else {
                blueKing = piece;
            }
        }
    }

    // ========== 보드 출력 ==========

    /// <summary>
    /// 기본 보드 출력 (커서, 하이라이트 없음)
    /// </summary>
    public void print() {
        print(Util.NONE, Util.NONE, Util.NONE, Util.NONE, EMPTY_MOVES, 0);
    }

    /// <summary>
    /// 커서만 표시하며 보드 출력 (기물 탐색 모드)
    /// </summary>
    public void print(int cursorRow, int cursorCol) {
        print(cursorRow, cursorCol, Util.NONE, Util.NONE, EMPTY_MOVES, 0);
    }

    /// <summary>
    /// 보드 출력 (커서 + 선택된 기물 + 이동 가능한 칸 표시)
    /// cursorRow/Col: 현재 커서 위치 (Util.NONE이면 커서 없음)
    /// selectedRow/Col: 선택된 기물 위치 (Util.NONE이면 선택 없음)
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
        for (int r = 0; r < Chess.BOARD_SIZE; r++) {
            // 내부 행 번호(0~7)를 체스 줄 번호(8~1)로 변환
            int rank = Chess.BOARD_SIZE - r;

            // 한 행을 문자열로 조립 (예: " 8 | r | n | b | q | k | b | n | r | 8")
            StringBuilder line = new StringBuilder();
            line.append(String.format(" %d |", rank));

            for (int c = 0; c < Chess.BOARD_SIZE; c++) {
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

        // 추가 메시지 표시 (시연 모드 등)
        if (!footerMessage.isEmpty()) {
            System.out.println();
            System.out.println(footerMessage);
        }

    }

    /// <summary>
    /// 잡힌 기물을 팀별로 표시
    /// 가치 높은 순으로 정렬하여 보여줌
    /// </summary>
    protected void printCapturedPieces() {
        if (capturedPieces.isEmpty()) {
            return;
        }

        // 정렬용 목록에 복사 후 가치가 높은 기물이 앞에 오도록 정렬
        sortedCapturedPieces.clear();
        sortedCapturedPieces.addAll(capturedPieces);
        sortedCapturedPieces.sort((a, b) -> b.value - a.value);

        // 문자열 조립기 초기화
        redCaptures.setLength(0);
        blueCaptures.setLength(0);

        for (Piece p : sortedCapturedPieces) {
            if (p.color == Chess.BLUE) {
                // 파란 기물이 잡힘 → 빨간팀이 잡은 것
                redCaptures.append(Util.BLUE).append(p.symbol).append(Util.RESET).append(" ");
            } else {
                // 빨간 기물이 잡힘 → 파란팀이 잡은 것
                blueCaptures.append(Util.RED).append(p.symbol).append(Util.RESET).append(" ");
            }
        }

        System.out.println();
        if (!redCaptures.isEmpty()) {
            // trim()은 ESC(\033, 코드 27)도 제거하므로 마지막 공백만 삭제
            redCaptures.setLength(redCaptures.length() - 1);
            System.out.println("  " + Util.RED + "빨간팀" + Util.RESET + " 획득: " + redCaptures);
        }
        if (!blueCaptures.isEmpty()) {
            blueCaptures.setLength(blueCaptures.length() - 1);
            System.out.println("  " + Util.BLUE + "파란팀" + Util.RESET + " 획득: " + blueCaptures);
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
                String colorCode = (piece.color == Chess.RED) ? Util.RED : Util.BLUE;
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
            String colorCode = (piece.color == Chess.RED) ? Util.RED : Util.BLUE;
            return " " + colorCode + piece.symbol + Util.RESET + " ";
        }
        return "   ";
    }

    /// <summary>
    /// 커서 위치의 기물/아이템 설명 반환
    /// 빈 칸이면 null 반환
    /// SkillBoard가 오버라이드하여 아이템/효과 설명 추가
    /// </summary>
    public String getCellDescription(int row, int col) {
        if (grid[row][col].hasPiece()) {
            Piece piece = grid[row][col].getPiece();
            String colorCode = (piece.color == Chess.RED) ? Util.RED : Util.BLUE;
            return colorCode + piece.name + Util.RESET + " - " + piece.description;
        }
        return null;
    }

    /// <summary>
    /// 특정 좌표가 배열의 처음 count개 항목에 포함되어 있는지 확인
    /// 버퍼 배열에서 유효한 범위만 검사할 때 사용
    /// </summary>
    public boolean isInArray(int row, int col, int[][] array, int count) {
        for (int i = 0; i < count; i++) {
            if (array[i][ROW] == row && array[i][COL] == col) {
                return true;
            }
        }
        return false;
    }

    // ========== 기물 조회 ==========

    /// <summary>
    /// 특정 색상의 킹 반환
    /// </summary>
    public Piece getKing(int color) {
        return (color == Chess.RED) ? redKing : blueKing;
    }

    // ========== 이동 실행 ==========

    /// <summary>
    /// 기본 이동 실행 (기물 옮기기 + 잡기 처리)
    /// 캐슬링/앙파상 등 특수 이동은 하위 클래스(ClassicBoard)에서 오버라이드
    /// </summary>
    public void executeMove(Move move) {
        Piece piece = grid[move.fromRow][move.fromCol].getPiece();

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
    }

    // ========== 이동 유효성 ==========

    /// <summary>
    /// 특정 기물의 합법적인 이동 가능 칸 목록 반환
    /// 기본 이동 규칙 적용 후 자기 킹이 위험해지는 수는 제외
    /// 특수 규칙(캐슬링, 앙파상)은 하위 클래스의 addSpecialMoves()에서 추가
    /// </summary>
    public int getFilteredMoves(int row, int col) {
        filteredCount = 0;

        if (grid[row][col].isEmpty()) {
            return 0;
        }
        Piece piece = grid[row][col].getPiece();

        // 이동이 차단된 기물은 이동 불가 (SkillBoard에서 동결 상태 확인)
        if (isMovementBlocked(piece)) {
            return 0;
        }

        int rawCount = piece.getValidMoves(grid);

        for (int i = 0; i < rawCount; i++) {
            int destRow = piece.moveBuffer[i][ROW];
            int destCol = piece.moveBuffer[i][COL];

            // 잡기가 차단된 기물은 잡을 수 없음 (SkillBoard에서 방패 상태 확인)
            if (grid[destRow][destCol].hasPiece()) {
                Piece target = grid[destRow][destCol].getPiece();
                if (isCaptureBlocked(target)) {
                    continue;
                }
            }

            tempMove.set(row, col, destRow, destCol);
            // 이 수를 두면 자기 킹이 체크되는지 확인
            if (!wouldBeInCheck(tempMove, piece.color)) {
                filteredBuffer[filteredCount][ROW] = destRow;
                filteredBuffer[filteredCount][COL] = destCol;
                filteredCount++;
            }
        }

        // 특수 규칙 (하위 클래스가 오버라이드하여 캐슬링/앙파상 등 추가)
        addSpecialMoves(piece, row, col);

        return filteredCount;
    }

    /// <summary>
    /// 특수 규칙 이동을 추가하는 훅 메서드
    /// 기본 Board에서는 아무것도 추가하지 않음
    /// ClassicBoard가 오버라이드하여 캐슬링/앙파상 추가
    /// </summary>
    protected void addSpecialMoves(Piece piece, int row, int col) {
        // 기본: 특수 규칙 없음
    }

    /// <summary>
    /// 기물의 이동이 차단되었는지 확인하는 훅 메서드
    /// 기본 Board에서는 항상 이동 가능
    /// SkillBoard가 오버라이드하여 동결 상태 확인
    /// </summary>
    protected boolean isMovementBlocked(Piece piece) {
        return false;
    }

    /// <summary>
    /// 기물의 잡기가 차단되었는지 확인하는 훅 메서드
    /// 기본 Board에서는 항상 잡기 가능
    /// SkillBoard가 오버라이드하여 방패 상태 확인
    /// </summary>
    protected boolean isCaptureBlocked(Piece target) {
        return false;
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
    /// </summary>
    public ArrayList<Move> getAllValidMoves(int color) {
        allMoves.clear();

        for (int r = 0; r < Chess.BOARD_SIZE; r++) {
            for (int c = 0; c < Chess.BOARD_SIZE; c++) {
                if (grid[r][c].isEmpty()) {
                    continue;
                }
                Piece piece = grid[r][c].getPiece();
                if (piece.color != color) {
                    continue;
                }

                // getFilteredMoves가 addSpecialMoves 훅을 통해 특수 규칙도 포함
                int moveCount = getFilteredMoves(r, c);
                for (int i = 0; i < moveCount; i++) {
                    allMoves.add(new Move(r, c, filteredBuffer[i][ROW], filteredBuffer[i][COL]));
                }
            }
        }

        return allMoves;
    }

    /// <summary>
    /// 이 수를 두면 자기 킹이 체크 상태가 되는지 시뮬레이션
    /// 임시로 이동했다가 되돌리는 방식으로 확인
    /// </summary>
    protected boolean wouldBeInCheck(Move move, int color) {
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

    // ========== 체크 / 체크메이트 / 스테일메이트 ==========

    /// <summary>
    /// 특정 색상의 킹이 체크 상태인지 확인
    /// 상대 기물 중 하나라도 킹의 위치를 공격할 수 있으면 체크
    /// </summary>
    public boolean isInCheck(int color) {
        Piece king = getKing(color);
        int kingRow = king.row;
        int kingCol = king.col;

        // 모든 상대 기물을 순회하며 킹을 공격할 수 있는지 확인
        for (int r = 0; r < Chess.BOARD_SIZE; r++) {
            for (int c = 0; c < Chess.BOARD_SIZE; c++) {
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
    /// 합법적인 수가 하나라도 발견되면 즉시 반환 (전체를 모을 필요 없음)
    /// </summary>
    private boolean hasNoValidMoves(int color) {
        for (int r = 0; r < Chess.BOARD_SIZE; r++) {
            for (int c = 0; c < Chess.BOARD_SIZE; c++) {
                if (grid[r][c].isEmpty()) {
                    continue;
                }
                Piece piece = grid[r][c].getPiece();
                if (piece.color != color) {
                    continue;
                }
                if (getFilteredMoves(r, c) > 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
