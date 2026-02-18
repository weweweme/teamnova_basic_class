package game;

import core.Util;
import piece.*;
import player.*;

/// <summary>
/// 공식 모드 시연 게임
/// ClassicBoard가 SimpleBoard를 상속받아 추가한 특수 규칙을 시연
/// 턴마다 스크립트가 바뀌며 캐슬링 → 앙파상 → 프로모션 순서로 안내
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
        + "\n"
        + "캐슬링: 킹이 2칸 옆으로 이동하면 룩이 자동으로 따라옵니다.\n"
        + "ClassicBoard.addSpecialMoves()가 캐슬링 이동을 추가합니다.\n"
        + "→ g1(킹사이드) 또는 c1(퀸사이드)로 이동하세요",

        // 턴 2 (파란팀) - 앙파상 준비
        "[2/3] ▶ 파란 폰(c7)을 c5로 2칸 전진하세요\n"
        + "\n"
        + "앙파상 준비: 상대 폰이 첫 이동으로 2칸 전진하면\n"
        + "옆에 있는 아군 폰이 앙파상으로 잡을 수 있습니다.",

        // 턴 3 (빨간팀) - 앙파상
        "[2/3] ▶ 빨간 폰(d5)을 선택하세요\n"
        + "\n"
        + "앙파상: 파란 폰이 방금 2칸 전진했으므로\n"
        + "c6으로 대각선 이동하여 잡을 수 있습니다 (· 확인)\n"
        + "ClassicBoard.addEnPassantMoves()가 이 이동을 추가합니다.",

        // 턴 4 (파란팀)
        "[3/3] 파란팀 차례 - 아무 기물이나 이동하세요\n"
        + "\n"
        + "(다음 턴에 프로모션을 시연합니다)",

        // 턴 5 (빨간팀) - 프로모션
        "[3/3] ▶ 빨간 폰(g7)을 g8로 전진하세요\n"
        + "\n"
        + "프로모션: 폰이 상대 끝줄에 도달하면 승격합니다.\n"
        + "퀸/룩/비숍/나이트 중 하나를 선택할 수 있습니다.\n"
        + "ClassicBoard.isPromotion()이 이를 감지합니다."
    };

    // 모든 스크립트 완료 후 표시할 메시지
    private static final String COMPLETE_MESSAGE =
        "공식 모드 시연 완료!\n"
        + "ClassicBoard는 SimpleBoard를 상속받아\n"
        + "캐슬링, 앙파상, 프로모션 규칙을 추가합니다.\n"
        + "q를 눌러 다음 시연으로 넘어가세요.";

    // ========== 생성자 ==========

    public DemoClassicGame() {
        super(
            new ClassicHumanPlayer(Piece.RED, "빨간팀"),
            new ClassicHumanPlayer(Piece.BLUE, "파란팀")
        );

        // 표준 배치 제거 후 시연용 커스텀 배치
        board.clearAllPieces();

        // === 캐슬링 시연 ===
        board.placePiece(PieceType.KING, Piece.RED, Util.ROW_1, Util.COL_E);     // e1 - 킹 (캐슬링 가능)
        board.placePiece(PieceType.ROOK, Piece.RED, Util.ROW_1, Util.COL_H);     // h1 - 룩 (킹사이드)
        board.placePiece(PieceType.ROOK, Piece.RED, Util.ROW_1, Util.COL_A);     // a1 - 룩 (퀸사이드)

        // === 프로모션 시연 ===
        board.placePiece(PieceType.PAWN, Piece.RED, Util.ROW_7, Util.COL_G);     // g7 - 폰 (한 칸 전진하면 프로모션)

        // === 앙파상 시연 ===
        board.placePiece(PieceType.PAWN, Piece.RED, Util.ROW_5, Util.COL_D);     // d5 - 폰 (앙파상 대기)

        // 파란팀
        board.placePiece(PieceType.KING, Piece.BLUE, Util.ROW_8, Util.COL_E);    // e8 - 킹
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_7, Util.COL_C);    // c7 - 폰 (2칸 전진 → 앙파상 트리거)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_7, Util.COL_F);    // f7 - 폰
    }

    // ========== 턴 처리 (스크립트 갱신) ==========

    /// <summary>
    /// 매 턴 시작 시 현재 턴에 맞는 스크립트를 보드 하단에 표시
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
