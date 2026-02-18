package game;

import core.Move;
import core.Util;
import piece.*;
import player.*;

/// <summary>
/// 기본 모드 시연 게임
/// Piece 상속 구조를 보여주기 위한 커스텀 보드 배치
/// 턴마다 스크립트가 바뀌며 각 기물의 고유한 이동 규칙을 안내
/// 스크립트와 다른 기물을 선택하면 이동을 실행하지 않고 다시 선택
/// </summary>
public class DemoSimpleGame extends SimpleGame {

    // ========== 시연 스크립트 ==========

    /// <summary>
    /// 턴별 안내 메시지 (turnCount - 1 인덱스로 접근)
    /// 빨간팀(홀수 턴)이 각 기물을 시연, 파란팀(짝수 턴)은 자유 이동
    /// </summary>
    private static final String[] SCRIPTS = {
        // 턴 1 (빨간팀) - 나이트
        "[1/8] ▶ 빨간 나이트(b3)를 선택하세요\n"
        + "\n"
        + "Knight는 Piece를 상속받아 L자(2+1칸) 이동을 구현합니다.\n"
        + "이동 가능한 칸(·)이 L자 모양인 것을 확인하세요.",

        // 턴 2 (파란팀)
        "[1/8] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 3 (빨간팀) - 비숍
        "[2/8] ▶ 빨간 비숍(c1)을 선택하세요\n"
        + "\n"
        + "Bishop은 대각선 4방향으로 무제한 이동합니다.\n"
        + "Piece의 slideMoves() 헬퍼로 장거리 이동을 구현합니다.",

        // 턴 4 (파란팀)
        "[2/8] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 5 (빨간팀) - 룩
        "[3/8] ▶ 빨간 룩(a1)을 선택하세요\n"
        + "\n"
        + "Rook은 상하좌우 직선 4방향으로 무제한 이동합니다.\n"
        + "Bishop과 같은 slideMoves()로 방향만 다르게 구현합니다.",

        // 턴 6 (파란팀)
        "[3/8] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 7 (빨간팀) - 퀸
        "[4/8] ▶ 빨간 퀸(d4)을 선택하세요\n"
        + "\n"
        + "Queen은 직선 + 대각선 8방향으로 무제한 이동합니다.\n"
        + "Rook의 이동 + Bishop의 이동을 합친 것입니다.",

        // 턴 8 (파란팀)
        "[4/8] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 9 (빨간팀) - 폰
        "[5/8] ▶ 빨간 폰(e2)을 선택하세요\n"
        + "\n"
        + "Pawn은 전진 1칸(첫 이동 시 2칸), 대각선 잡기가 가능합니다.\n"
        + "다른 기물과 달리 전진과 잡기 방향이 다릅니다.",

        // 턴 10 (파란팀)
        "[5/8] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 11 (빨간팀) - 킹
        "[6/8] ▶ 빨간 킹(e1)을 선택하세요\n"
        + "\n"
        + "King은 전방향 1칸 이동이 가능합니다.\n"
        + "자기 킹이 위험해지는 수는 자동으로 제외됩니다.",

        // 턴 12 (파란팀) - 체크 시연
        "[7/8] 체크! 파란 킹(e8)이 빨간 룩에게 공격받고 있습니다\n"
        + "\n"
        + "체크: 킹이 위험하면 반드시 벗어나야 합니다.\n"
        + "다른 기물은 선택할 수 없고, 체크를 피하는 수만 가능합니다.\n"
        + "킹을 d7 또는 e7로 이동하세요.",

        // 턴 13 (빨간팀) - 체크 후 자유
        "[7/8] 빨간팀 차례 - 아무 기물이나 이동하세요",

        // 턴 14 (파란팀) - 체크메이트 준비
        "[8/8] 파란팀 차례 - 아무 기물이나 이동하세요\n"
        + "\n"
        + "(다음 턴에 체크메이트를 시연합니다)",

        // 턴 15 (빨간팀) - 체크메이트
        "[8/8] ▶ 빨간 룩(a1)을 a8로 이동하세요\n"
        + "\n"
        + "체크메이트: 상대 킹이 도망갈 곳이 없으면 게임 종료!\n"
        + "백 랭크 메이트: f7/g7/h7 폰이 자기 킹의 퇴로를 막습니다.\n"
        + "board.isCheckmate()가 이를 감지합니다."
    };

    /// <summary>
    /// 턴별 기대하는 출발 위치 (null이면 검증 없이 자유 이동)
    /// </summary>
    private static final int[][] EXPECTED_FROM = {
        {Util.ROW_3, Util.COL_B},   // 턴 1: 나이트 b3
        null,                         // 턴 2: 자유
        {Util.ROW_1, Util.COL_C},   // 턴 3: 비숍 c1
        null,                         // 턴 4: 자유
        {Util.ROW_1, Util.COL_A},   // 턴 5: 룩 a1
        null,                         // 턴 6: 자유
        {Util.ROW_4, Util.COL_D},   // 턴 7: 퀸 d4
        null,                         // 턴 8: 자유
        {Util.ROW_2, Util.COL_E},   // 턴 9: 폰 e2
        null,                         // 턴 10: 자유
        {Util.ROW_1, Util.COL_E},   // 턴 11: 킹 e1
        null,                         // 턴 12: 체크 탈출 (킹만 이동 가능)
        null,                         // 턴 13: 자유
        null,                         // 턴 14: 자유
        {Util.ROW_1, Util.COL_A},   // 턴 15: 룩 a1
    };

    /// <summary>
    /// 턴별 기대하는 도착 위치 (null이면 도착 검증 안 함)
    /// 체크메이트 시연에서 정확한 도착이 필요한 경우에만 지정
    /// </summary>
    private static final int[][] EXPECTED_TO = {
        null, null, null, null, null,   // 턴 1~5
        null, null, null, null, null,   // 턴 6~10
        null,                           // 턴 11: 킹 자유 이동
        null,                           // 턴 12: 체크 탈출
        null,                           // 턴 13: 자유
        null,                           // 턴 14: 자유
        {Util.ROW_8, Util.COL_A},     // 턴 15: a8 (체크메이트)
    };

    // 체크 시연에서 보드를 재배치하는 스크립트 인덱스 (턴 12)
    private static final int CHECK_STEP = 11;

    // 체크메이트 시연에서 보드를 재배치하는 스크립트 인덱스 (턴 15)
    private static final int CHECKMATE_STEP = 14;

    // 모든 스크립트 완료 후 표시할 메시지
    private static final String COMPLETE_MESSAGE =
        "기본 모드 시연 완료!\n"
        + "각 기물은 Piece를 상속받아 getValidMoves()를 오버라이딩합니다.\n"
        + "q를 눌러 다음 시연으로 넘어가세요.";

    // ========== 생성자 ==========

    public DemoSimpleGame() {
        super(
            new HumanPlayer(Piece.RED, "빨간팀"),
            new HumanPlayer(Piece.BLUE, "파란팀")
        );

        // 표준 배치 제거 후 시연용 커스텀 배치
        board.clearAllPieces();

        // 빨간팀: 각 기물의 이동 규칙을 보여줄 수 있는 위치에 배치
        board.placePiece(PieceType.KING, Piece.RED, Util.ROW_1, Util.COL_E);     // e1 - 킹 (1칸 전방향)
        board.placePiece(PieceType.QUEEN, Piece.RED, Util.ROW_4, Util.COL_D);    // d4 - 퀸 (8방향 무제한)
        board.placePiece(PieceType.ROOK, Piece.RED, Util.ROW_1, Util.COL_A);     // a1 - 룩 (직선 4방향)
        board.placePiece(PieceType.BISHOP, Piece.RED, Util.ROW_1, Util.COL_C);   // c1 - 비숍 (대각선 4방향)
        board.placePiece(PieceType.KNIGHT, Piece.RED, Util.ROW_3, Util.COL_B);   // b3 - 나이트 (L자 이동)
        board.placePiece(PieceType.PAWN, Piece.RED, Util.ROW_2, Util.COL_E);     // e2 - 폰 (전진, 첫 이동 2칸)

        // 파란팀: 빨간 기물과 겹치지 않는 g/h 열에 배치 (시연 기물 보호)
        board.placePiece(PieceType.KING, Piece.BLUE, Util.ROW_8, Util.COL_E);    // e8 - 킹
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_7, Util.COL_G);    // g7 - 폰
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_7, Util.COL_H);    // h7 - 폰
    }

    // ========== 체크/체크메이트 시연 배치 ==========

    /// <summary>
    /// 체크 시연 배치로 보드를 재배치
    /// 빨간 룩(a8)이 파란 킹(e8)을 체크하는 상태
    /// 파란팀은 킹을 d7 또는 e7로 이동하여 체크를 벗어나야 함
    /// (폰은 체크를 해소하지 못하므로 이동 불가)
    /// </summary>
    private void setupCheckBoard() {
        board.clearAllPieces();

        // 빨간팀
        board.placePiece(PieceType.KING, Piece.RED, Util.ROW_1, Util.COL_G);    // g1 - 킹
        board.placePiece(PieceType.ROOK, Piece.RED, Util.ROW_8, Util.COL_A);    // a8 - 룩 (체크 중)

        // 파란팀 (킹이 체크 상태)
        board.placePiece(PieceType.KING, Piece.BLUE, Util.ROW_8, Util.COL_E);   // e8 - 킹 (체크!)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_7, Util.COL_F);   // f7 - 폰 (이동 불가)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_7, Util.COL_G);   // g7 - 폰 (이동 불가)
    }

    /// <summary>
    /// 백 랭크 메이트 배치로 보드를 재배치
    /// 룩(a1)이 a8로 이동하면 킹(g8)이 f7/g7/h7 폰에 막혀 체크메이트
    /// </summary>
    private void setupCheckmateBoard() {
        board.clearAllPieces();

        // 빨간팀
        board.placePiece(PieceType.KING, Piece.RED, Util.ROW_1, Util.COL_G);    // g1 - 킹
        board.placePiece(PieceType.ROOK, Piece.RED, Util.ROW_1, Util.COL_A);    // a1 - 룩 (체크메이트 실행)

        // 파란팀 (킹이 자기 폰에 막힌 상태)
        board.placePiece(PieceType.KING, Piece.BLUE, Util.ROW_8, Util.COL_G);   // g8 - 킹
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_7, Util.COL_F);   // f7 - 폰 (퇴로 차단)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_7, Util.COL_G);   // g7 - 폰 (퇴로 차단)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_7, Util.COL_H);   // h7 - 폰 (퇴로 차단)
    }

    // ========== 턴 처리 (스크립트 검증) ==========

    /// <summary>
    /// 매 턴 시작 시 스크립트를 표시하고, 플레이어의 수를 검증
    /// 스크립트에 지정된 기물이 아닌 다른 기물을 선택하면
    /// 이동을 실행하지 않고 경고 후 다시 선택하게 함
    /// </summary>
    @Override
    protected boolean processTurn() {
        int scriptIndex = turnCount - 1;
        String script = (scriptIndex < SCRIPTS.length) ? SCRIPTS[scriptIndex] : COMPLETE_MESSAGE;
        int[] expectedFrom = (scriptIndex < EXPECTED_FROM.length) ? EXPECTED_FROM[scriptIndex] : null;
        int[] expectedTo = (scriptIndex < EXPECTED_TO.length) ? EXPECTED_TO[scriptIndex] : null;

        // 체크 시연 턴이면 보드를 체크 배치로 전환
        if (scriptIndex == CHECK_STEP) {
            setupCheckBoard();
        }

        // 체크메이트 시연 턴이면 보드를 백 랭크 메이트 배치로 전환
        if (scriptIndex == CHECKMATE_STEP) {
            setupCheckmateBoard();
        }

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
            boolean fromOk = (expectedFrom == null)
                    || (move.fromRow == expectedFrom[0] && move.fromCol == expectedFrom[1]);
            // 도착 위치 검증
            boolean toOk = (expectedTo == null)
                    || (move.toRow == expectedTo[0] && move.toCol == expectedTo[1]);

            if (!fromOk || !toOk) {
                board.setFooterMessage(">> 안내에 따라 진행해주세요!\n\n" + script);
                continue;
            }

            // 검증 통과 → 이동 실행
            board.executeMove(move);
            return false;
        }
    }
}
