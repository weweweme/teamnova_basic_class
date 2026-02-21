package game;

import core.Move;
import core.Chess;
import core.Util;
import piece.*;
import player.*;

/// <summary>
/// 공식 모드 시연 게임
/// ClassicBoard가 SimpleBoard를 상속받아 추가한 특수 규칙을 시연
/// 턴마다 스크립트가 바뀌며 캐슬링 → 앙파상 → 프로모션 순서로 안내
/// 스크립트와 다른 수를 두면 이동을 실행하지 않고 다시 선택
/// </summary>
public class DemoClassicGame extends ClassicGame {

    // ========== 시연 스크립트 ==========

    /// <summary>
    /// 턴별 안내 메시지
    /// 캐슬링(턴1) → 앙파상 준비(턴2) → 앙파상(턴3) → 자유(턴4) → 프로모션(턴5)
    /// </summary>
    private static final String[] SCRIPTS = {
        // 턴 1 (빨간팀) - 캐슬링
        "[1/3] ▶ 빨간 킹(e1)을 선택하세요\n"
        + "캐슬링: 킹이 룩 쪽으로 2칸 이동하면 룩이 자동으로 따라옵니다.\n"
        + "킹과 룩 사이에 기물이 없어야 하며, 한 번도 움직이지 않았을 때만 가능합니다.\n"
        + "→ g1(킹사이드) 또는 c1(퀸사이드)로 이동하세요",

        // 턴 2 (파란팀) - 앙파상 준비
        "[2/3] ▶ 파란 폰(c7)을 c5로 2칸 전진하세요\n"
        + "폰의 첫 이동에서만 2칸 전진이 가능합니다.",

        // 턴 3 (빨간팀) - 앙파상
        "[2/3] ▶ 빨간 폰(d5)을 선택 → c6으로 이동\n"
        + "앙파상: 상대 폰이 바로 직전에 2칸 전진했다면\n"
        + "옆에 있는 아군 폰이 대각선으로 잡을 수 있습니다.",

        // 턴 4 (파란팀)
        "[3/3] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 5 (빨간팀) - 프로모션
        "[3/3] ▶ 빨간 폰(g7)을 g8로 전진하세요\n"
        + "프로모션: 폰이 상대 끝줄에 도달하면\n"
        + "퀸/룩/비숍/나이트 중 하나로 승격합니다."
    };

    /// <summary>
    /// 턴별 기대하는 출발 위치 (null이면 자유 이동)
    /// </summary>
    private static final int[][] EXPECTED_FROM = {
        {Chess.ROW_1, Chess.COL_E},   // 턴 1: 킹 e1
        {Chess.ROW_7, Chess.COL_C},   // 턴 2: 폰 c7
        {Chess.ROW_5, Chess.COL_D},   // 턴 3: 폰 d5
        null,                         // 턴 4: 자유
        {Chess.ROW_7, Chess.COL_G},   // 턴 5: 폰 g7
    };

    /// <summary>
    /// 턴별 기대하는 도착 위치 (null이면 도착 검증 안 함)
    /// 앙파상(c6), 프로모션(g8) 등 정확한 도착이 필요한 경우에만 지정
    /// </summary>
    private static final int[][] EXPECTED_TO = {
        null,                         // 턴 1: 킹사이드/퀸사이드 어느 쪽이든
        {Chess.ROW_5, Chess.COL_C},   // 턴 2: c5 (2칸 전진)
        {Chess.ROW_6, Chess.COL_C},   // 턴 3: c6 (앙파상)
        null,                         // 턴 4: 자유
        {Chess.ROW_8, Chess.COL_G},   // 턴 5: g8 (프로모션)
    };

    // 모든 스크립트 완료 후 표시할 메시지
    private static final String COMPLETE_MESSAGE =
        "공식 체스 튜토리얼 완료!\n"
        + "q를 눌러 메뉴로 돌아가세요.";

    // ========== 생성자 ==========

    public DemoClassicGame() {
        super(
            new ClassicHumanPlayer(Piece.RED, "빨간팀"),
            new ClassicHumanPlayer(Piece.BLUE, "파란팀")
        );

        // 표준 배치 제거 후 시연용 커스텀 배치
        board.clearAllPieces();

        // === 캐슬링 시연 ===
        board.placePiece(PieceType.KING, Piece.RED, Chess.ROW_1, Chess.COL_E);     // e1 - 킹 (캐슬링 가능)
        board.placePiece(PieceType.ROOK, Piece.RED, Chess.ROW_1, Chess.COL_H);     // h1 - 룩 (킹사이드)
        board.placePiece(PieceType.ROOK, Piece.RED, Chess.ROW_1, Chess.COL_A);     // a1 - 룩 (퀸사이드)

        // === 프로모션 시연 ===
        board.placePiece(PieceType.PAWN, Piece.RED, Chess.ROW_7, Chess.COL_G);     // g7 - 폰 (한 칸 전진하면 프로모션)

        // === 앙파상 시연 ===
        board.placePiece(PieceType.PAWN, Piece.RED, Chess.ROW_5, Chess.COL_D);     // d5 - 폰 (앙파상 대기)

        // 파란팀
        board.placePiece(PieceType.KING, Piece.BLUE, Chess.ROW_7, Chess.COL_E);    // e7 - 킹 (g8 프로모션 후 체크 방지)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Chess.ROW_7, Chess.COL_C);    // c7 - 폰 (2칸 전진 → 앙파상 트리거)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Chess.ROW_7, Chess.COL_F);    // f7 - 폰
    }

    // ========== 턴 처리 (스크립트 검증) ==========

    /// <summary>
    /// 매 턴 시작 시 스크립트를 표시하고, 플레이어의 수를 검증
    /// 출발/도착 위치가 스크립트와 다르면 이동을 실행하지 않고 다시 선택
    /// 프로모션도 직접 처리 (ClassicGame.processTurn 대체)
    /// </summary>
    @Override
    protected boolean processTurn() {
        int scriptIndex = turnCount - 1;
        String script = (scriptIndex < SCRIPTS.length) ? SCRIPTS[scriptIndex] : COMPLETE_MESSAGE;
        int[] expectedFrom = (scriptIndex < EXPECTED_FROM.length) ? EXPECTED_FROM[scriptIndex] : null;
        int[] expectedTo = (scriptIndex < EXPECTED_TO.length) ? EXPECTED_TO[scriptIndex] : null;

        board.setFooterMessage(script);

        while (true) {
            Move move = currentPlayer.chooseMove(board);

            // q → 게임 종료
            if (move == null) {
                Util.clearScreen();
                board.print();
                System.out.println("\n게임을 종료합니다.");
                return true;
            }

            // 출발 위치 검증
            boolean fromOk = (expectedFrom == null) || (move.fromRow == expectedFrom[0] && move.fromCol == expectedFrom[1]);
            // 도착 위치 검증
            boolean toOk = (expectedTo == null) || (move.toRow == expectedTo[0] && move.toCol == expectedTo[1]);

            if (!fromOk || !toOk) {
                board.setFooterMessage(">> 안내에 따라 진행해주세요!\n\n" + script);
                continue;
            }

            // 검증 통과 → 이동 실행
            board.executeMove(move);

            // 프로모션 확인 (ClassicGame의 afterMove 훅에서 처리)
            afterMove(move);

            return false;
        }
    }
}
