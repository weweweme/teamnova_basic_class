package game;

import board.*;
import core.*;
import piece.Piece;
import player.Player;

/// <summary>
/// 게임 추상 클래스 (템플릿 메서드 패턴)
/// 게임 루프의 전체 흐름을 정의하고, 턴 처리 방식은 하위 클래스가 구현
/// Game → SimpleGame → ClassicGame → SkillGame (직렬 상속 체인)
/// </summary>
public abstract class Game {

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

    // ========== 게임 루프 (템플릿 메서드) ==========

    /// <summary>
    /// 게임 시작 (메인 루프)
    /// 전체 흐름을 정의하고, 턴 처리는 하위 클래스의 processTurn()에 위임
    /// </summary>
    public void run() {
        while (true) {
            // 턴 처리 (하위 클래스마다 다른 방식)
            // true 반환 시 게임 종료 요청
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

    // ========== 추상 메서드 ==========

    /// <summary>
    /// 게임에 맞는 보드를 생성하여 반환
    /// SimpleGame: SimpleBoard, ClassicGame: ClassicBoard, SkillGame: SkillBoard
    /// </summary>
    protected abstract SimpleBoard createBoard();

    /// <summary>
    /// 한 턴의 처리를 수행
    /// SimpleGame이 템플릿으로 구현: beforeAction → doAction → afterAction
    /// 하위 클래스는 각 훅 메서드를 오버라이드하여 턴 진행 방식을 확장
    /// true 반환 시 게임 종료 요청
    /// </summary>
    protected abstract boolean processTurn();

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
