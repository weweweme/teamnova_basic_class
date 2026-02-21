package game;

import board.*;
import core.*;
import player.ClassicPlayer;
import player.Player;

/// <summary>
/// 공식 체스 게임
/// SimpleGame의 기본 이동에 프로모션 규칙을 추가
/// afterMove 훅을 오버라이드하여 폰이 끝 줄 도달 시 승격 처리
/// </summary>
public class ClassicGame extends SimpleGame {

    // ========== 필드 ==========

    /// <summary>
    /// 공식 체스 보드 (프로모션 등 공식 규칙 메서드 호출용)
    /// </summary>
    private ClassicBoard classicBoard;

    // 빨간팀 플레이어 (ClassicPlayer 타입으로 프로모션 메서드 호출용)
    private ClassicPlayer classicRedPlayer;

    // 파란팀 플레이어
    private ClassicPlayer classicBluePlayer;

    // ========== 생성자 ==========

    public ClassicGame(ClassicPlayer redPlayer, ClassicPlayer bluePlayer) {
        super(redPlayer, bluePlayer);
        this.classicRedPlayer = redPlayer;
        this.classicBluePlayer = bluePlayer;
    }

    // ========== 헬퍼 ==========

    /// <summary>
    /// 현재 플레이어를 ClassicPlayer 타입으로 반환
    /// </summary>
    protected ClassicPlayer currentClassicPlayer() {
        if (currentPlayer == redPlayer) {
            return classicRedPlayer;
        }
        return classicBluePlayer;
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

    // ========== 훅 메서드 오버라이드 ==========

    /// <summary>
    /// 이동 후 프로모션 확인
    /// 폰이 상대 끝 줄에 도착하면 승격할 기물을 선택
    /// </summary>
    @Override
    protected void afterMove(Move move) {
        if (classicBoard.isPromotion(move)) {
            int choice = currentClassicPlayer().choosePromotion(board);
            classicBoard.promote(move.toRow, move.toCol, choice);
        }
    }
}
