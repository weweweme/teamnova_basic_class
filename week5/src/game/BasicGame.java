package game;

import board.*;
import core.*;
import player.Player;

/// <summary>
/// 기본 체스 게임
/// 기물의 기본 이동만 사용 (캐슬링, 앙파상, 프로모션 없음)
/// </summary>
public class BasicGame extends Game {

    // ========== 생성자 ==========

    public BasicGame(Player redPlayer, Player bluePlayer) {
        super(redPlayer, bluePlayer);
    }

    // ========== 보드 생성 ==========

    /// <summary>
    /// 기본 체스용 SimpleBoard 생성
    /// </summary>
    @Override
    protected SimpleBoard createBoard() {
        return new SimpleBoard();
    }

    // ========== 턴 처리 ==========

    /// <summary>
    /// 기본 체스 턴 처리
    /// 수 선택 → 이동 실행 (프로모션 없음)
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

        // 이동 실행 (기본 이동만, 캐슬링/앙파상 없음)
        board.executeMove(move);

        return false;
    }
}
