package game;

import piece.*;
import player.*;

/// <summary>
/// 기본 모드 시연 게임
/// Piece 상속 구조를 보여주기 위한 커스텀 보드 배치
/// 각 기물(King, Queen, Rook, Bishop, Knight, Pawn)의 고유한 이동 규칙을 시연
/// </summary>
public class DemoSimpleGame extends SimpleGame {

    // ========== 생성자 ==========

    public DemoSimpleGame() {
        super(
            new HumanPlayer(Piece.RED, "빨간팀"),
            new HumanPlayer(Piece.BLUE, "파란팀")
        );

        // 표준 배치 제거 후 시연용 커스텀 배치
        board.clearAllPieces();

        // 빨간팀: 각 기물의 이동 규칙을 보여줄 수 있는 위치에 배치
        board.placePiece(PieceType.KING, Piece.RED, 7, 4);     // e1 - 킹 (1칸 전방향)
        board.placePiece(PieceType.QUEEN, Piece.RED, 4, 3);    // d4 - 퀸 (8방향 무제한)
        board.placePiece(PieceType.ROOK, Piece.RED, 7, 0);     // a1 - 룩 (직선 4방향)
        board.placePiece(PieceType.BISHOP, Piece.RED, 7, 2);   // c1 - 비숍 (대각선 4방향)
        board.placePiece(PieceType.KNIGHT, Piece.RED, 5, 1);   // b3 - 나이트 (L자 이동)
        board.placePiece(PieceType.PAWN, Piece.RED, 6, 4);     // e2 - 폰 (전진, 첫 이동 2칸)

        // 파란팀: 최소 배치 (킹 + 잡기 시연용)
        board.placePiece(PieceType.KING, Piece.BLUE, 0, 4);    // e8 - 킹
        board.placePiece(PieceType.PAWN, Piece.BLUE, 3, 4);    // e5 - 폰 (잡기 대상)
        board.placePiece(PieceType.ROOK, Piece.BLUE, 0, 0);    // a8 - 룩

        // 시연 설명 텍스트
        board.setFooterMessage(
            "[시연] 기본 모드 - Piece 상속 구조\n"
            + "각 기물은 Piece를 상속받아 고유한 이동 규칙(getValidMoves)을 구현합니다.\n"
            + "기물을 선택하여 이동 가능한 칸(·)을 확인해보세요.\n"
            + "q: 다음 시연"
        );
    }
}
