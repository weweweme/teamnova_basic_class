package game;

import core.Util;
import piece.*;
import player.*;

/// <summary>
/// 기본 모드 시연 게임
/// Piece 상속 구조를 보여주기 위한 커스텀 보드 배치
/// 턴마다 스크립트가 바뀌며 각 기물의 고유한 이동 규칙을 안내
/// </summary>
public class DemoSimpleGame extends SimpleGame {

    // ========== 시연 스크립트 ==========

    /// <summary>
    /// 턴별 안내 메시지 (turnCount - 1 인덱스로 접근)
    /// 빨간팀(홀수 턴)이 각 기물을 시연, 파란팀(짝수 턴)은 자유 이동
    /// </summary>
    private static final String[] SCRIPTS = {
        // 턴 1 (빨간팀) - 나이트
        "[1/6] ▶ 빨간 나이트(b3)를 선택하세요\n"
        + "\n"
        + "Knight는 Piece를 상속받아 L자(2+1칸) 이동을 구현합니다.\n"
        + "이동 가능한 칸(·)이 L자 모양인 것을 확인하세요.",

        // 턴 2 (파란팀)
        "[1/6] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 3 (빨간팀) - 비숍
        "[2/6] ▶ 빨간 비숍(c1)을 선택하세요\n"
        + "\n"
        + "Bishop은 대각선 4방향으로 무제한 이동합니다.\n"
        + "Piece의 slideMoves() 헬퍼로 장거리 이동을 구현합니다.",

        // 턴 4 (파란팀)
        "[2/6] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 5 (빨간팀) - 룩
        "[3/6] ▶ 빨간 룩(a1)을 선택하세요\n"
        + "\n"
        + "Rook은 상하좌우 직선 4방향으로 무제한 이동합니다.\n"
        + "Bishop과 같은 slideMoves()로 방향만 다르게 구현합니다.",

        // 턴 6 (파란팀)
        "[3/6] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 7 (빨간팀) - 퀸
        "[4/6] ▶ 빨간 퀸(d4)을 선택하세요\n"
        + "\n"
        + "Queen은 직선 + 대각선 8방향으로 무제한 이동합니다.\n"
        + "Rook의 이동 + Bishop의 이동을 합친 것입니다.",

        // 턴 8 (파란팀)
        "[4/6] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 9 (빨간팀) - 폰
        "[5/6] ▶ 빨간 폰(e2)을 선택하세요\n"
        + "\n"
        + "Pawn은 전진 1칸(첫 이동 시 2칸), 대각선 잡기가 가능합니다.\n"
        + "다른 기물과 달리 전진과 잡기 방향이 다릅니다.",

        // 턴 10 (파란팀)
        "[5/6] 파란팀 차례 - 아무 기물이나 이동하세요",

        // 턴 11 (빨간팀) - 킹
        "[6/6] ▶ 빨간 킹(e1)을 선택하세요\n"
        + "\n"
        + "King은 전방향 1칸 이동이 가능합니다.\n"
        + "자기 킹이 위험해지는 수는 자동으로 제외됩니다."
    };

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

        // 파란팀: 최소 배치 (킹 + 자유 이동용)
        board.placePiece(PieceType.KING, Piece.BLUE, Util.ROW_8, Util.COL_E);    // e8 - 킹
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_5, Util.COL_E);    // e5 - 폰
        board.placePiece(PieceType.ROOK, Piece.BLUE, Util.ROW_8, Util.COL_A);    // a8 - 룩
    }

    // ========== 턴 처리 (스크립트 갱신) ==========

    /// <summary>
    /// 매 턴 시작 시 현재 턴에 맞는 스크립트를 보드 하단에 표시
    /// turnCount는 Game에서 1부터 시작하여 switchTurn()마다 증가
    /// </summary>
    @Override
    protected boolean processTurn() {
        int scriptIndex = turnCount - 1;
        if (scriptIndex < SCRIPTS.length) {
            board.setFooterMessage(SCRIPTS[scriptIndex]);
        } else {
            board.setFooterMessage(COMPLETE_MESSAGE);
        }
        return super.processTurn();
    }
}
