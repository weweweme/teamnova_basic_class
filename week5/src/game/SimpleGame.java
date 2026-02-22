package game;

import board.*;
import core.*;
import player.Player;

/// <summary>
/// 기본 체스 게임
/// 기물의 기본 이동만 사용 (캐슬링, 앙파상, 프로모션 없음)
/// processTurn이 템플릿 메서드로 3단계 훅을 순서대로 호출
/// </summary>
public class SimpleGame extends Game {

    // ========== 필드 ==========

    /// <summary>
    /// 마지막으로 실행된 이동 (afterAction에서 프로모션 확인용)
    /// doAction에서 저장하고, afterAction에서 참조
    /// 스킬/아이템 사용 턴에는 null
    /// </summary>
    protected Move lastMove;

    // ========== 생성자 ==========

    public SimpleGame(Player redPlayer, Player bluePlayer) {
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

    // ========== 턴 처리 (템플릿) ==========

    /// <summary>
    /// 턴 처리 템플릿
    /// 사전처리 → 액션 수행 → 사후처리 순서로 훅 메서드를 호출
    /// 하위 클래스는 processTurn 대신 각 훅을 오버라이드
    /// true 반환 시 게임 종료 요청
    /// </summary>
    @Override
    protected boolean processTurn() {
        // 사전처리
        beforeAction();

        // 액션 수행 (true 반환 시 게임 종료)
        if (doAction()) {
            return true;
        }

        // 사후처리
        afterAction();
        return false;
    }

    // ========== 훅 메서드 ==========

    /// <summary>
    /// 턴 사전처리 (훅 메서드)
    /// SkillGame이 오버라이드하여 방패/동결 해제
    /// </summary>
    protected void beforeAction() {
        // 기본 모드: 사전처리 없음
    }

    /// <summary>
    /// 액션 수행 (훅 메서드)
    /// 기본 모드: 수 선택 → 이동 실행
    /// SkillGame이 오버라이드하여 이동/스킬/아이템 중 하나를 선택
    /// true 반환 시 게임 종료 요청
    /// </summary>
    protected boolean doAction() {
        // 현재 플레이어가 수를 선택
        lastMove = currentPlayer.chooseMove(board);

        // 종료 요청
        if (lastMove == null) {
            Util.clearScreen();
            board.print();
            System.out.println("\n게임을 종료합니다.");
            return true;
        }

        // 이동 실행
        board.executeMove(lastMove);
        return false;
    }

    /// <summary>
    /// 턴 사후처리 (훅 메서드)
    /// ClassicGame이 오버라이드하여 프로모션 확인
    /// </summary>
    protected void afterAction() {
        // 기본 모드: 사후처리 없음
    }
}
