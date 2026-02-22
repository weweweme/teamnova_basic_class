package player;

import board.*;
import core.*;
import piece.*;

import java.util.ArrayList;

/// <summary>
/// AI 플레이어용 수 선택 전략
/// 난이도에 따라 다른 전략으로 수를 선택
/// </summary>
public class AiInput {

    // ========== 필드 ==========

    // AI 난이도
    private final int difficulty;

    // ========== 생성자 ==========

    public AiInput(int difficulty) {
        this.difficulty = difficulty;
    }

    // ========== 수 선택 ==========

    /// <summary>
    /// AI가 수를 선택
    /// 난이도에 따라 쉬움(랜덤) 또는 보통(우선순위 전략) 사용
    /// </summary>
    public Move chooseMove(SimpleBoard board, int color, String name) {
        // AI 턴에도 현재 보드를 보여줌
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println(name + "이(가) 생각 중...");

        // 생각하는 시간 연출
        Util.delay(1000);

        // 모든 합법적인 수 가져오기
        ArrayList<Move> allMoves = board.getAllValidMoves(color);

        // 수가 없으면 null (체크메이트 또는 스테일메이트 상태)
        if (allMoves.isEmpty()) {
            return null;
        }

        // 난이도에 따라 전략 선택
        if (difficulty == AiPlayer.EASY) {
            return chooseEasy(allMoves);
        }

        return chooseNormal(board, allMoves, color);
    }

    // ========== 난이도별 전략 ==========

    /// <summary>
    /// 쉬움: 합법적인 수 중 랜덤으로 선택
    /// </summary>
    private Move chooseEasy(ArrayList<Move> allMoves) {
        return allMoves.get(Util.rand(allMoves.size()));
    }

    /// <summary>
    /// 보통: 우선순위 기반 전략
    /// 체크메이트 > 기물 잡기(가치 높은 순) > 체크 > 랜덤
    /// </summary>
    private Move chooseNormal(SimpleBoard board, ArrayList<Move> allMoves, int color) {
        // 상대 색상
        int opponentColor = (color == Chess.RED) ? Chess.BLUE : Chess.RED;

        // 1순위: 체크메이트 가능한 수
        for (Move move : allMoves) {
            if (wouldCheckmate(board, move, opponentColor)) {
                return move;
            }
        }

        // 2순위: 기물 잡기 (가치가 가장 높은 기물 우선)
        Move bestCapture = null;
        int bestValue = 0;

        for (Move move : allMoves) {
            if (board.grid[move.toRow][move.toCol].isEmpty()) {
                continue;
            }
            Piece target = board.grid[move.toRow][move.toCol].getPiece();
            if (target.color != color && target.value > bestValue) {
                bestValue = target.value;
                bestCapture = move;
            }
        }

        if (bestCapture != null) {
            return bestCapture;
        }

        // 3순위: 체크를 거는 수
        for (Move move : allMoves) {
            if (wouldCheck(board, move, opponentColor)) {
                return move;
            }
        }

        // 4순위: 랜덤 선택
        return allMoves.get(Util.rand(allMoves.size()));
    }

    // ========== 시뮬레이션 ==========

    /// <summary>
    /// 이 수를 두면 상대가 체크메이트가 되는지 시뮬레이션
    /// 임시로 이동했다가 되돌리는 방식
    /// </summary>
    private boolean wouldCheckmate(SimpleBoard board, Move move, int opponentColor) {
        // 원래 상태 저장
        Piece movingPiece = board.grid[move.fromRow][move.fromCol].getPiece();
        Piece capturedPiece = board.grid[move.toRow][move.toCol].getPiece();
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;
        boolean origHasMoved = movingPiece.hasMoved;

        // 임시로 이동 실행
        board.grid[move.toRow][move.toCol].setPiece(movingPiece);
        board.grid[move.fromRow][move.fromCol].removePiece();
        movingPiece.row = move.toRow;
        movingPiece.col = move.toCol;
        movingPiece.hasMoved = true;

        // 체크메이트 확인
        boolean checkmate = board.isCheckmate(opponentColor);

        // 원래 상태로 복원
        board.grid[move.fromRow][move.fromCol].setPiece(movingPiece);
        board.grid[move.toRow][move.toCol].setPiece(capturedPiece);
        movingPiece.row = origRow;
        movingPiece.col = origCol;
        movingPiece.hasMoved = origHasMoved;

        return checkmate;
    }

    /// <summary>
    /// 이 수를 두면 상대가 체크 상태가 되는지 시뮬레이션
    /// 임시로 이동했다가 되돌리는 방식
    /// </summary>
    private boolean wouldCheck(SimpleBoard board, Move move, int opponentColor) {
        // 원래 상태 저장
        Piece movingPiece = board.grid[move.fromRow][move.fromCol].getPiece();
        Piece capturedPiece = board.grid[move.toRow][move.toCol].getPiece();
        int origRow = movingPiece.row;
        int origCol = movingPiece.col;

        // 임시로 이동 실행
        board.grid[move.toRow][move.toCol].setPiece(movingPiece);
        board.grid[move.fromRow][move.fromCol].removePiece();
        movingPiece.row = move.toRow;
        movingPiece.col = move.toCol;

        // 체크 확인
        boolean check = board.isInCheck(opponentColor);

        // 원래 상태로 복원
        board.grid[move.fromRow][move.fromCol].setPiece(movingPiece);
        board.grid[move.toRow][move.toCol].setPiece(capturedPiece);
        movingPiece.row = origRow;
        movingPiece.col = origCol;

        return check;
    }
}
