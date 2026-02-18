package game;

import piece.*;
import player.*;

/// <summary>
/// 스킬 모드 시연 게임
/// SkillBoard가 ClassicBoard를 상속받아 추가한 스킬/아이템 시스템을 시연
/// 파괴, 방패, 부활 스킬과 폭탄, 함정 아이템을 보여줄 수 있는 배치
/// </summary>
public class DemoSkillGame extends SkillGame {

    // ========== 생성자 ==========

    public DemoSkillGame() {
        super(
            new SkillHumanPlayer(Piece.RED, "빨간팀"),
            new SkillHumanPlayer(Piece.BLUE, "파란팀")
        );

        // 표준 배치 제거 후 시연용 커스텀 배치
        board.clearAllPieces();

        // 빨간팀
        board.placePiece(PieceType.KING, Piece.RED, 7, 4);     // e1 - 킹
        board.placePiece(PieceType.ROOK, Piece.RED, 7, 0);     // a1 - 룩
        board.placePiece(PieceType.PAWN, Piece.RED, 5, 4);     // e3 - 폰 (이동 시연)
        board.placePiece(PieceType.KNIGHT, Piece.RED, 7, 1);   // b1 - 나이트

        // 파란팀
        board.placePiece(PieceType.KING, Piece.BLUE, 0, 4);    // e8 - 킹
        board.placePiece(PieceType.ROOK, Piece.BLUE, 0, 0);    // a8 - 룩 (파괴/방패 대상)
        board.placePiece(PieceType.PAWN, Piece.BLUE, 2, 4);    // e6 - 폰 (이동 시연)
        board.placePiece(PieceType.BISHOP, Piece.BLUE, 0, 2);  // c8 - 비숍

        // 시연 설명 텍스트
        board.setFooterMessage(
            "[시연] 스킬 모드 - SkillBoard가 ClassicBoard를 상속\n"
            + "스킬(파괴/방패/부활)과 아이템(폭탄/함정) 시스템을 추가합니다.\n"
            + "턴 시작 시 [1]이동 [2]스킬 [3]아이템 중 선택할 수 있습니다.\n"
            + "q: 시연 종료"
        );
    }
}
