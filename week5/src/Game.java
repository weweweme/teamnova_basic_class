/// <summary>
/// 게임 클래스
/// 게임 루프, 턴 관리, 체크/체크메이트 판정
/// </summary>
public class Game {

    // ========== 필드 ==========

    // 체스판
    private Board board;

    // 빨간팀 플레이어 (선공)
    private Player redPlayer;

    // 파란팀 플레이어 (후공)
    private Player bluePlayer;

    // 현재 턴의 플레이어
    private Player currentPlayer;

    // 턴 수
    private int turnCount;

    // ========== 생성자 ==========

    /// <summary>
    /// 게임 생성
    /// 보드와 두 플레이어를 받아 초기화
    /// </summary>
    public Game(Player redPlayer, Player bluePlayer) {
        this.board = new Board();
        this.redPlayer = redPlayer;
        this.bluePlayer = bluePlayer;
        // 빨간팀(RED)이 선공
        this.currentPlayer = redPlayer;
        this.turnCount = 1;
    }

    // ========== 게임 루프 ==========

    /// <summary>
    /// 게임 시작 (메인 루프)
    /// 보드 출력 → 수 선택 → 이동 실행 → 승패 확인 → 턴 교대
    /// </summary>
    public void run() {
        while (true) {
            // 현재 플레이어가 수를 선택
            Move move = currentPlayer.chooseMove(board);

            // null이면 게임 종료 요청
            if (move == null) {
                Util.clearScreen();
                board.print();
                System.out.println("\n게임을 종료합니다.");
                break;
            }

            // 이동 실행
            board.executeMove(move);

            // 프로모션 확인 (폰이 끝 줄에 도착하면 승격)
            if (board.isPromotion(move)) {
                int choice = currentPlayer.choosePromotion(board);
                board.promote(move.toRow, move.toCol, choice);
            }

            // 상대 색상
            int opponentColor = (currentPlayer.color == Piece.RED) ? Piece.BLUE : Piece.RED;

            // 체크메이트 확인
            if (board.isCheckmate(opponentColor)) {
                Util.clearScreen();
                board.print();
                System.out.println();
                System.out.println("========================================");
                System.out.println("  체크메이트! " + currentPlayer.name + " 승리!");
                System.out.println("========================================");
                break;
            }

            // 스테일메이트 확인 (무승부)
            if (board.isStalemate(opponentColor)) {
                Util.clearScreen();
                board.print();
                System.out.println();
                System.out.println("========================================");
                System.out.println("  스테일메이트! 무승부!");
                System.out.println("========================================");
                break;
            }

            // 턴 교대
            switchTurn();
        }
    }

    // ========== 턴 관리 ==========

    /// <summary>
    /// 턴 교대 (빨간팀 ↔ 파란팀)
    /// </summary>
    private void switchTurn() {
        if (currentPlayer == redPlayer) {
            currentPlayer = bluePlayer;
        } else {
            currentPlayer = redPlayer;
        }
        turnCount++;
    }
}
