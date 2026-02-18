package game;

import core.Util;
import piece.*;
import player.*;

/// <summary>
/// 스킬 모드 시연 게임
/// SkillBoard가 ClassicBoard를 상속받아 추가한 스킬/아이템 시스템을 시연
/// 턴마다 스크립트가 바뀌며 파괴 → 방패 → 아이템 설치 → 아이템 발동 순서로 안내
/// </summary>
public class DemoSkillGame extends SkillGame {

    // ========== 시연 스크립트 ==========

    /// <summary>
    /// 턴별 안내 메시지
    /// 파괴(턴1) → 방패(턴2) → 아이템 설치(턴3) → 아이템 발동(턴4) → 자유(턴5~)
    /// </summary>
    private static final String[] SCRIPTS = {
        // 턴 1 (빨간팀) - 파괴 스킬
        "[1/3] ▶ [2]스킬 → [1]파괴를 선택하세요\n"
        + "\n"
        + "파괴 스킬: 상대 기물 하나를 즉시 제거합니다.\n"
        + "대상(·)이 표시된 상대 기물 중 하나를 선택하세요.\n"
        + "SkillBoard.removePiece()로 기물을 제거합니다.",

        // 턴 2 (파란팀) - 방패 스킬
        "[2/3] ▶ [2]스킬 → [2]방패를 선택하세요\n"
        + "\n"
        + "방패 스킬: 아군 기물에 방패(!)를 씌웁니다.\n"
        + "다음 턴까지 해당 기물은 잡히지 않습니다.\n"
        + "보호할 아군 기물을 선택하세요.",

        // 턴 3 (빨간팀) - 아이템 설치
        "[3/3] ▶ [3]아이템 → 종류 선택 → 빈 칸에 설치\n"
        + "\n"
        + "아이템: 빈 칸에 폭탄 또는 함정을 설치합니다.\n"
        + "상대에게는 보이지 않으며, 밟으면 효과가 발동됩니다.\n"
        + "[1]폭탄(주변 폭발) 또는 [2]함정(동결) 중 선택하세요.",

        // 턴 4 (파란팀) - 이동 (아이템 발동 확인)
        "[3/3] ▶ [1]이동으로 기물을 이동하세요\n"
        + "\n"
        + "아이템이 설치된 칸을 밟으면 효과가 발동됩니다.\n"
        + "(상대가 설치한 아이템은 보이지 않습니다)\n"
        + "아이템 위치를 기억하고 밟아보세요!",

        // 턴 5 (빨간팀) - 자유 플레이
        "[완료] 자유롭게 플레이하세요\n"
        + "\n"
        + "SkillBoard는 ClassicBoard를 상속받아\n"
        + "스킬(파괴/방패/부활)과 아이템(폭탄/함정)을 추가합니다.\n"
        + "남은 스킬/아이템을 자유롭게 사용해보세요.",

        // 턴 6 (파란팀) - 자유 플레이
        "[완료] 파란팀 차례 - 자유롭게 플레이하세요"
    };

    // 모든 스크립트 완료 후 표시할 메시지
    private static final String COMPLETE_MESSAGE =
        "스킬 모드 시연 완료!\n"
        + "SkillBoard는 ClassicBoard를 상속받아\n"
        + "스킬/아이템 시스템을 추가한 확장 보드입니다.\n"
        + "q를 눌러 시연을 종료하세요.";

    // ========== 생성자 ==========

    public DemoSkillGame() {
        super(
            new SkillHumanPlayer(Piece.RED, "빨간팀"),
            new SkillHumanPlayer(Piece.BLUE, "파란팀")
        );

        // 표준 배치 제거 후 시연용 커스텀 배치
        board.clearAllPieces();

        // 빨간팀
        board.placePiece(PieceType.KING, Piece.RED, Util.ROW_1, Util.COL_E);     // e1 - 킹
        board.placePiece(PieceType.ROOK, Piece.RED, Util.ROW_1, Util.COL_A);     // a1 - 룩
        board.placePiece(PieceType.PAWN, Piece.RED, Util.ROW_4, Util.COL_D);     // d4 - 폰 (이동 시연)
        board.placePiece(PieceType.KNIGHT, Piece.RED, Util.ROW_1, Util.COL_B);   // b1 - 나이트

        // 파란팀
        board.placePiece(PieceType.KING, Piece.BLUE, Util.ROW_8, Util.COL_E);    // e8 - 킹
        board.placePiece(PieceType.ROOK, Piece.BLUE, Util.ROW_8, Util.COL_A);    // a8 - 룩 (파괴/방패 대상)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_5, Util.COL_E);    // e5 - 폰 (이동 시연)
        board.placePiece(PieceType.BISHOP, Piece.BLUE, Util.ROW_8, Util.COL_C);  // c8 - 비숍
    }

    // ========== 턴 처리 (스크립트 갱신) ==========

    /// <summary>
    /// 매 턴 시작 시 현재 턴에 맞는 스크립트를 보드 하단에 표시
    /// SkillGame.processTurn()의 재귀 호출에도 안전 (turnCount 기반)
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
