package board;

import core.Move;
import core.Util;
import piece.*;

/// <summary>
/// 공식 체스 보드
/// 기본 이동 규칙(SimpleBoard)에 캐슬링, 앙파상, 프로모션을 추가
/// </summary>
public class ClassicBoard extends SimpleBoard {

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

    // 마지막으로 실행된 수 (앙파상 판정에 사용)
    private Move lastMove;

    // ========== 생성자 ==========

    public ClassicBoard() {
        super();
    }

    // ========== 이동 실행 (특수 규칙 포함) ==========

    /// <summary>
    /// 이동 실행 (캐슬링, 앙파상 등 특수 이동도 자동 감지하여 처리)
    /// 기본 이동은 부모 클래스(SimpleBoard)에 위임
    /// </summary>
    @Override
    public void executeMove(Move move) {
        Piece piece = grid[move.fromRow][move.fromCol].getPiece();

        // 캐슬링 감지 (킹이 2칸 옆으로 이동)
        boolean isKing = piece.type == PieceType.KING;                     // 이동한 기물이 킹인지
        boolean movedTwoColumns = Math.abs(move.toCol - move.fromCol) == 2;  // 2칸 옆으로 이동했는지
        boolean isCastling = isKing && movedTwoColumns;

        if (isCastling) {
            executeCastling(move);
            saveLastMove(move);
            return;
        }

        // 앙파상 감지 (폰이 대각선으로 빈 칸에 이동)
        boolean isPawn = piece.type == PieceType.PAWN;                    // 이동한 기물이 폰인지
        boolean movedDiagonally = move.fromCol != move.toCol;           // 열이 바뀌었는지 (대각선 이동)
        boolean destEmpty = grid[move.toRow][move.toCol].isEmpty();     // 도착 칸이 비어있는지
        boolean isEnPassant = isPawn && movedDiagonally && destEmpty;

        if (isEnPassant) {
            // 잡힌 폰 기록 및 제거
            capturedPieces.add(grid[move.fromRow][move.toCol].getPiece());
            grid[move.fromRow][move.toCol].removePiece();
        }

        // 기본 이동 (잡기 기록 + 기물 이동 + 위치 갱신)
        super.executeMove(move);

        saveLastMove(move);
    }

    /// <summary>
    /// 마지막 이동 정보를 저장 (앙파상 판정에 사용)
    /// 첫 이동 시 1회만 Move 객체를 생성하고, 이후에는 값만 덮어씀
    /// </summary>
    private void saveLastMove(Move move) {
        if (lastMove == null) {
            lastMove = new Move(move.fromRow, move.fromCol, move.toRow, move.toCol);
        } else {
            lastMove.set(move.fromRow, move.fromCol, move.toRow, move.toCol);
        }
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

        Piece rook;
        if (move.toCol > move.fromCol) {
            // 킹사이드: 룩 h열 → f열
            rook = grid[move.fromRow][KINGSIDE_ROOK_COL].getPiece();
            grid[move.fromRow][KINGSIDE_ROOK_DEST].setPiece(rook);
            grid[move.fromRow][KINGSIDE_ROOK_COL].removePiece();
            rook.row = move.fromRow;
            rook.col = KINGSIDE_ROOK_DEST;
        } else {
            // 퀸사이드: 룩 a열 → d열
            rook = grid[move.fromRow][QUEENSIDE_ROOK_COL].getPiece();
            grid[move.fromRow][QUEENSIDE_ROOK_DEST].setPiece(rook);
            grid[move.fromRow][QUEENSIDE_ROOK_COL].removePiece();
            rook.row = move.fromRow;
            rook.col = QUEENSIDE_ROOK_DEST;
        }
        rook.hasMoved = true;
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// 마지막 이동이 프로모션 대상인지 확인
    /// 폰이 상대편 끝 줄에 도착하면 프로모션
    /// </summary>
    public boolean isPromotion(Move move) {
        Piece piece = grid[move.toRow][move.toCol].getPiece();
        if (piece.type != PieceType.PAWN) {
            return false;
        }
        // 빨간팀은 0행(8번 줄), 파란팀은 7행(1번 줄)이 끝
        boolean isRedAtEnd = piece.color == Piece.RED && move.toRow == 0;
        boolean isBlueAtEnd = piece.color == Piece.BLUE && move.toRow == 7;
        return isRedAtEnd || isBlueAtEnd;
    }

    /// <summary>
    /// 폰을 선택한 기물로 승격
    /// 1: 퀸, 2: 룩, 3: 비숍, 4: 나이트
    /// </summary>
    public void promote(int row, int col, int choice) {
        Piece piece = grid[row][col].getPiece();
        switch (choice) {
            case Util.PROMOTE_QUEEN:
                PieceFactory.configure(piece, PieceType.QUEEN);
                break;
            case Util.PROMOTE_ROOK:
                PieceFactory.configure(piece, PieceType.ROOK);
                break;
            case Util.PROMOTE_BISHOP:
                PieceFactory.configure(piece, PieceType.BISHOP);
                break;
            case Util.PROMOTE_KNIGHT:
                PieceFactory.configure(piece, PieceType.KNIGHT);
                break;
        }
    }

    // ========== 특수 규칙 이동 추가 (훅 메서드 오버라이드) ==========

    /// <summary>
    /// 캐슬링과 앙파상 이동을 추가
    /// SimpleBoard의 getFilteredMoves()에서 기본 이동 필터링 후 호출됨
    /// </summary>
    @Override
    protected void addSpecialMoves(Piece piece, int row, int col) {
        // 캐슬링 (킹이 아직 움직이지 않았을 때만)
        if (piece.type == PieceType.KING && !piece.hasMoved) {
            addCastlingMoves(piece);
        }

        // 앙파상 (폰일 때만)
        if (piece.type == PieceType.PAWN) {
            addEnPassantMoves(piece, row, col);
        }
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
        boolean kingsideRookReady = kingsideRook != null && kingsideRook.type == PieceType.ROOK && !kingsideRook.hasMoved;
        // 킹과 룩 사이 칸(f열, g열)이 비어있는지
        boolean kingsidePathClear = grid[row][KINGSIDE_ROOK_DEST].isEmpty() && grid[row][KINGSIDE_KING_DEST].isEmpty();
        // 킹이 지나가는 칸(f열, g열)이 상대에게 공격받지 않는지
        boolean kingsidePathSafe = isSquareSafe(row, KINGSIDE_ROOK_DEST, opponentColor) && isSquareSafe(row, KINGSIDE_KING_DEST, opponentColor);

        if (kingsideRookReady && kingsidePathClear && kingsidePathSafe) {
            filteredBuffer[filteredCount][ROW] = row;
            filteredBuffer[filteredCount][COL] = KINGSIDE_KING_DEST;
            filteredCount++;
        }

        // 퀸사이드 캐슬링 (킹: e→c, 룩: a→d)
        Piece queensideRook = grid[row][QUEENSIDE_ROOK_COL].getPiece();

        // a열에 룩이 있고 한 번도 움직이지 않았는지
        boolean queensideRookReady = queensideRook != null && queensideRook.type == PieceType.ROOK && !queensideRook.hasMoved;
        // 킹과 룩 사이 칸(b열, c열, d열)이 비어있는지
        boolean queensidePathClear = grid[row][QUEENSIDE_PATH_COL].isEmpty() && grid[row][QUEENSIDE_KING_DEST].isEmpty() && grid[row][QUEENSIDE_ROOK_DEST].isEmpty();
        // 킹이 지나가는 칸(c열, d열)이 상대에게 공격받지 않는지
        boolean queensidePathSafe = isSquareSafe(row, QUEENSIDE_KING_DEST, opponentColor) && isSquareSafe(row, QUEENSIDE_ROOK_DEST, opponentColor);

        if (queensideRookReady && queensidePathClear && queensidePathSafe) {
            filteredBuffer[filteredCount][ROW] = row;
            filteredBuffer[filteredCount][COL] = QUEENSIDE_KING_DEST;
            filteredCount++;
        }
    }

    /// <summary>
    /// 특정 칸이 상대 기물에게 공격받지 않는 안전한 칸인지 확인
    /// 캐슬링 경유 칸 검증에 사용
    /// </summary>
    private boolean isSquareSafe(int row, int col, int attackerColor) {
        for (int r = 0; r < Util.BOARD_SIZE; r++) {
            for (int c = 0; c < Util.BOARD_SIZE; c++) {
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
                    if (piece.moveBuffer[i][ROW] == row && piece.moveBuffer[i][COL] == col) {
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
        if (lastPiece == null || lastPiece.type != PieceType.PAWN) {
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

        // 잡기가 차단된 폰은 앙파상으로 잡을 수 없음 (방패 등)
        if (isCaptureBlocked(lastPiece)) {
            return;
        }

        // 앙파상 도착 칸 (상대 폰이 지나온 빈 칸)
        int direction = (pawn.color == Piece.RED) ? Piece.RED_DIRECTION : Piece.BLUE_DIRECTION;
        int enPassantRow = row + direction;
        int enPassantCol = lastMove.toCol;

        // 자기 킹이 위험해지지 않는지 확인 (앙파상은 잡히는 위치가 다르므로 별도 확인)
        tempMove.set(row, col, enPassantRow, enPassantCol);
        if (!wouldBeInCheckEnPassant(tempMove, pawn.color, lastMove.toRow, lastMove.toCol)) {
            filteredBuffer[filteredCount][ROW] = enPassantRow;
            filteredBuffer[filteredCount][COL] = enPassantCol;
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
}
