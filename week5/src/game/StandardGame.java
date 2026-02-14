package game;

import core.*;
import player.Player;

/// <summary>
/// 일반 체스 게임
/// 기존 체스 규칙 그대로 진행 (이동 → 프로모션 → 승패 확인)
/// </summary>
public class StandardGame extends Game {

    // ========== 생성자 ==========

    public StandardGame(Player redPlayer, Player bluePlayer) {
        super(redPlayer, bluePlayer);
    }

    // ========== 턴 처리 ==========

    /// <summary>
    /// 일반 체스 턴 처리
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
        if (board.isPromotion(move)) {
            int choice = currentPlayer.choosePromotion(board);
            board.promote(move.toRow, move.toCol, choice);
        }

        return false;
    }
}
