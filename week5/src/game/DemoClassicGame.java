package game;

import piece.*;
import player.*;

/// <summary>
/// 공식 모드 시연 게임
/// ClassicBoard가 SimpleBoard를 상속받아 추가한 특수 규칙을 시연
/// 캐슬링, 프로모션, 앙파상을 한 판에서 모두 보여줄 수 있는 배치
/// </summary>
public class DemoClassicGame extends ClassicGame {

    // ========== 생성자 ==========

    public DemoClassicGame() {
        super(
            new ClassicHumanPlayer(Piece.RED, "빨간팀"),
            new ClassicHumanPlayer(Piece.BLUE, "파란팀")
        );

        // 표준 배치 제거 후 시연용 커스텀 배치
        board.clearAllPieces();

        // === 캐슬링 시연 ===
        // 빨간 킹과 룩이 초기 위치에 있고 사이가 비어있으면 캐슬링 가능
        board.placePiece(PieceType.KING, Piece.RED, 7, 4);     // e1 - 킹 (캐슬링 가능)
        board.placePiece(PieceType.ROOK, Piece.RED, 7, 7);     // h1 - 룩 (킹사이드 캐슬링)
        board.placePiece(PieceType.ROOK, Piece.RED, 7, 0);     // a1 - 룩 (퀸사이드 캐슬링)

        // === 프로모션 시연 ===
        // 빨간 폰이 상대 끝줄(8번 줄)에 도달하면 승격
        board.placePiece(PieceType.PAWN, Piece.RED, 1, 6);     // g7 - 폰 (한 칸 전진하면 프로모션)

        // === 앙파상 시연 ===
        // 빨간 폰이 5번 줄에 있고, 파란 폰이 7번 줄에서 2칸 전진하면 앙파상 가능
        board.placePiece(PieceType.PAWN, Piece.RED, 3, 3);     // d5 - 폰 (앙파상 대기)

        // 파란팀
        board.placePiece(PieceType.KING, Piece.BLUE, 0, 4);    // e8 - 킹
        board.placePiece(PieceType.PAWN, Piece.BLUE, 1, 2);    // c7 - 폰 (2칸 전진 → 앙파상 트리거)
        board.placePiece(PieceType.PAWN, Piece.BLUE, 1, 5);    // f7 - 폰 (프로모션 경로 방어)

        // 시연 설명 텍스트
        board.setFooterMessage(
            "[시연] 공식 모드 - ClassicBoard가 SimpleBoard를 상속\n"
            + "addSpecialMoves()를 오버라이드하여 특수 규칙을 추가합니다.\n"
            + "1) 빨간 킹 선택 → 캐슬링 (g1 또는 c1로 2칸 이동)\n"
            + "2) 파란 폰 c7→c5 후 빨간 폰 d5로 앙파상\n"
            + "3) 빨간 폰 g7→g8로 프로모션\n"
            + "q: 다음 시연"
        );
    }
}
