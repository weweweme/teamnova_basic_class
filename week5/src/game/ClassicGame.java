package game;

import board.*;
import core.*;
import player.Player;
import player.Promotable;

/// <summary>
/// 공식 체스 게임
/// 공식 체스 규칙으로 진행 (이동 → 프로모션 → 승패 확인)
/// </summary>
public class ClassicGame extends Game {

    // ========== 필드 ==========

    // 공식 체스 보드 (프로모션 등 공식 규칙 메서드 호출용)
    private ClassicBoard classicBoard;

    // ========== 생성자 ==========

    public ClassicGame(Player redPlayer, Player bluePlayer) {
        super(redPlayer, bluePlayer);
    }

    // ========== 보드 생성 ==========

    /// <summary>
    /// 공식 체스용 ClassicBoard 생성
    /// </summary>
    @Override
    protected SimpleBoard createBoard() {
        classicBoard = new ClassicBoard();
        return classicBoard;
    }

    // ========== 턴 처리 ==========

    /// <summary>
    /// 공식 체스 턴 처리
    /// 수 선택 → 이동 실행 → 프로모션 확인
    /// true 반환 시 게임 종료 요청
    /// </summary>
    @Override
    protected boolean processTurn() {
        // 현재 플레이어가 수를 선택
        Move move = currentPlayer.chooseMove(board);

        // null이면 게임 종료 요청
        if (move == null) {
            Util.clearScreen();
            board.print();
            System.out.println("\n게임을 종료합니다.");
            return true;
        }

        // 이동 실행
        board.executeMove(move);

        // 프로모션 확인 (폰이 끝 줄에 도착하면 승격)
        if (classicBoard.isPromotion(move)) {
            int choice = ((Promotable) currentPlayer).choosePromotion(board);
            classicBoard.promote(move.toRow, move.toCol, choice);
        }

        return false;
    }
}
