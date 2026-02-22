package game;

import board.*;
import core.*;
import player.Player;

/// <summary>
/// 게임 클래스
/// 게임 루프(run)와 턴 처리 템플릿(processTurn)을 제공
/// 하위 클래스는 훅 메서드(beforeAction, doAction, afterAction)를 오버라이드하여 확장
/// Game → ClassicGame → SkillGame (직렬 상속 체인)
/// </summary>
public class Game {

    // ========== 필드 ==========

    // 체스판
    protected SimpleBoard board;

    // 빨간팀 플레이어 (선공)
    protected Player redPlayer;

    // 파란팀 플레이어 (후공)
    protected Player bluePlayer;

    // 현재 턴의 플레이어
    protected Player currentPlayer;

    // 턴 수
    protected int turnCount;

    /// <summary>
    /// 마지막으로 실행된 이동 (afterAction에서 프로모션 확인용)
    /// doAction에서 저장하고, afterAction에서 참조
    /// 스킬/아이템 사용 턴에는 null
    /// </summary>
    protected Move lastMove;

    // ========== 생성자 ==========

    /// <summary>
    /// 게임 생성
    /// 보드와 두 플레이어를 받아 초기화
    /// </summary>
    public Game(Player redPlayer, Player bluePlayer) {
        this.board = createBoard();
        this.redPlayer = redPlayer;
        this.bluePlayer = bluePlayer;
        // 빨간팀(RED)이 선공
        this.currentPlayer = redPlayer;
        this.turnCount = 1;
    }

    // ========== 게임 루프 ==========

    /// <summary>
    /// 게임 시작 (메인 루프)
    /// 턴 처리 → 체크메이트/스테일메이트 확인 → 턴 교대를 반복
    /// </summary>
    public void run() {
        while (true) {
            // 턴 처리 (true 반환 시 게임 종료 요청)
            boolean quit = processTurn();
            if (quit) {
                break;
            }

            // 상대 색상
            int opponentColor = getOpponentColor();

            // 체크메이트 확인
            if (board.isCheckmate(opponentColor)) {
                showCheckmate();
                break;
            }

            // 스테일메이트 확인 (무승부)
            if (board.isStalemate(opponentColor)) {
                showStalemate();
                break;
            }

            // 턴 교대
            switchTurn();
        }
    }

    // ========== 보드 생성 ==========

    /// <summary>
    /// 게임에 맞는 보드를 생성하여 반환
    /// ClassicGame: ClassicBoard, SkillGame: SkillBoard
    /// </summary>
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
    /// SkillGame이 오버라이드하여 행동 루프 (스킬/아이템 + 이동) 수행
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

    // ========== 공통 메서드 ==========

    /// <summary>
    /// 상대 팀의 색상 반환
    /// </summary>
    protected int getOpponentColor() {
        if (currentPlayer.color == Chess.RED) {
            return Chess.BLUE;
        }
        return Chess.RED;
    }

    /// <summary>
    /// 턴 교대 (빨간팀 ↔ 파란팀)
    /// </summary>
    protected void switchTurn() {
        if (currentPlayer == redPlayer) {
            currentPlayer = bluePlayer;
        } else {
            currentPlayer = redPlayer;
        }
        turnCount++;
    }

    /// <summary>
    /// 체크메이트 결과 출력
    /// </summary>
    protected void showCheckmate() {
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println("========================================");
        System.out.println("  체크메이트! " + currentPlayer.name + " 승리!");
        System.out.println("========================================");
        System.out.println();
        System.out.println("아무 키나 누르면 계속합니다...");
        Util.readKey();
    }

    /// <summary>
    /// 스테일메이트 결과 출력
    /// </summary>
    protected void showStalemate() {
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println("========================================");
        System.out.println("  스테일메이트! 무승부!");
        System.out.println("========================================");
        System.out.println();
        System.out.println("아무 키나 누르면 계속합니다...");
        Util.readKey();
    }
}
