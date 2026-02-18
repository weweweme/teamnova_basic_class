package game;

import core.Util;
import piece.*;
import player.*;
import skill.*;
import item.*;

/// <summary>
/// 스킬 모드 튜토리얼 게임
/// 턴마다 스크립트가 바뀌며 파괴 → 방패 → 아이템 설치 → 아이템 발동 순서로 안내
/// 기대하는 행동과 다른 선택을 하면 경고 후 다시 선택
/// </summary>
public class DemoSkillGame extends SkillGame {

    // ========== 시연 스크립트 ==========

    /// <summary>
    /// 턴별 안내 메시지
    /// 파괴(턴1) → 방패(턴2) → 아이템 설치(턴3) → 아이템 발동(턴4) → 효과 확인(턴5~6) → 완료
    /// </summary>
    private static final String[] SCRIPTS = {
        // 턴 1 (빨간팀) - 파괴 스킬
        "[1/3] ▶ [2]스킬 → [1]파괴를 선택하세요\n"
        + "파괴: 상대 기물 하나를 즉시 제거합니다.\n"
        + "각 스킬은 게임당 1회만 사용 가능합니다.",

        // 턴 2 (파란팀) - 방패 스킬
        "[2/3] ▶ [2]스킬 → [2]방패를 선택하세요\n"
        + "방패: 아군 기물에 방패(!)를 씌웁니다.\n"
        + "다음 턴까지 해당 기물은 잡히지 않습니다.",

        // 턴 3 (빨간팀) - 아이템 설치
        "[3/3] ▶ [3]아이템 → 종류 선택 → 빈 칸에 설치\n"
        + "[1]폭탄: 밟은 기물이 제거됩니다. (킹 제외)\n"
        + "[2]함정: 밟은 기물이 1턴간 움직일 수 없습니다.\n"
        + "설치한 아이템은 상대에게 보이지 않습니다.",

        // 턴 4 (파란팀) - 이동 (아이템 발동 확인)
        "[3/3] ▶ [1]이동으로 기물을 이동하세요\n"
        + "아이템이 설치된 칸을 밟으면 효과가 발동됩니다.\n"
        + "아이템 위치를 기억하고 밟아보세요!",

        // 턴 5 (빨간팀) - 아이템 효과 확인
        "[완료] 아이템 효과를 확인하세요\n"
        + "함정: 기물 옆 ~는 동결 표시이며, 1턴간 이동 불가합니다.\n"
        + "폭탄: 밟은 기물이 제거됩니다. 단, 킹은 폭탄으로 제거할 수 없습니다.\n"
        + "q를 눌러 메뉴로 돌아가세요.",

        // 턴 6 (파란팀) - 동결 해제 확인
        "[완료] 파란팀 차례\n"
        + "동결된 기물이 있었다면 이번 턴에 해제됩니다.\n"
        + "q를 눌러 메뉴로 돌아가세요."
    };

    /// <summary>
    /// 턴별 기대하는 행동 (범위 밖이면 이동만 허용)
    /// </summary>
    private static final int[] EXPECTED_ACTIONS = {
        Util.ACTION_SKILL,   // 턴 1: 스킬 (파괴)
        Util.ACTION_SKILL,   // 턴 2: 스킬 (방패)
        Util.ACTION_ITEM,    // 턴 3: 아이템 설치
        Util.ACTION_MOVE,    // 턴 4: 이동 (아이템 발동)
    };

    // 모든 스크립트 완료 후 표시할 메시지
    private static final String COMPLETE_MESSAGE =
        "스킬 모드 튜토리얼 완료!\n"
        + "q를 눌러 메뉴로 돌아가세요.";

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
        board.placePiece(PieceType.PAWN, Piece.RED, Util.ROW_4, Util.COL_D);     // d4 - 폰
        board.placePiece(PieceType.KNIGHT, Piece.RED, Util.ROW_1, Util.COL_B);   // b1 - 나이트

        // 파란팀 (빨간 기물과 겹치지 않는 위치에 배치)
        board.placePiece(PieceType.KING, Piece.BLUE, Util.ROW_8, Util.COL_E);    // e8 - 킹
        board.placePiece(PieceType.ROOK, Piece.BLUE, Util.ROW_8, Util.COL_H);    // h8 - 룩 (파괴/방패 대상)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Util.ROW_7, Util.COL_H);    // h7 - 폰
        board.placePiece(PieceType.BISHOP, Piece.BLUE, Util.ROW_8, Util.COL_C);  // c8 - 비숍
    }

    // ========== 턴 처리 (행동 검증) ==========

    /// <summary>
    /// 매 턴 시작 시 스크립트를 표시하고, 플레이어의 행동을 검증
    /// 기대하는 행동(스킬/아이템/이동)과 다른 선택을 하면 경고 후 다시 선택
    /// 완료 턴(턴5~)은 행동 메뉴 없이 바로 이동 (q로 종료 가능)
    /// </summary>
    @Override
    protected boolean processTurn() {
        int scriptIndex = turnCount - 1;
        String script = (scriptIndex < SCRIPTS.length) ? SCRIPTS[scriptIndex] : COMPLETE_MESSAGE;
        int expectedAction = (scriptIndex < EXPECTED_ACTIONS.length) ? EXPECTED_ACTIONS[scriptIndex] : Util.NONE;

        board.setFooterMessage(script);

        // ===== 완료 턴: 행동 메뉴 없이 바로 이동 (q로 종료 가능) =====

        if (expectedAction == Util.NONE) {
            // 지난 턴 효과 정리
            skillBoard.clearShields(currentPlayer.color);
            skillBoard.clearFreezes(currentPlayer.color);

            // 모든 기물이 동결되어 있으면 턴 스킵
            if (!skillBoard.hasUnfrozenPieces(currentPlayer.color)) {
                Util.clearScreen();
                skillBoard.print(currentPlayer.color);
                System.out.println();
                System.out.println(currentPlayer.name + "의 모든 기물이 동결되어 턴을 넘깁니다.");
                Util.delay(2000);
                return false;
            }

            // 바로 이동 (행동 메뉴 건너뜀 → q로 종료 가능)
            boolean moved = handleMove();
            if (!moved) {
                return true;
            }
            return false;
        }

        // ===== 스크립트 턴: SkillGame.processTurn()의 설정 로직 =====

        Skill[] skills = (currentPlayer.color == Piece.RED) ? redSkills : blueSkills;
        Item[] items = (currentPlayer.color == Piece.RED) ? redItems : blueItems;

        // 지난 턴에 건 방패 해제
        skillBoard.clearShields(currentPlayer.color);

        // 지난 턴에 걸린 동결 해제
        skillBoard.clearFreezes(currentPlayer.color);

        // 모든 기물이 동결되어 있으면 턴 스킵
        if (!skillBoard.hasUnfrozenPieces(currentPlayer.color)) {
            Util.clearScreen();
            skillBoard.print(currentPlayer.color);
            System.out.println();
            System.out.println(currentPlayer.name + "의 모든 기물이 동결되어 턴을 넘깁니다.");
            Util.delay(2000);
            return false;
        }

        // 부활 스킬의 잡힌 기물 수 갱신
        ((ReviveSkill) skills[Util.SKILL_REVIVE]).setCapturedCount(
            skillBoard.getCapturedCount(currentPlayer.color)
        );

        // ===== 행동 선택 + 검증 =====

        int action = skillCapable().chooseAction(board, skills, items);

        // 기대하는 행동이 아니면 경고 후 재시도
        if (action != expectedAction) {
            board.setFooterMessage(">> 안내에 따라 진행해주세요!\n\n" + script);
            return processTurn();
        }

        // ===== 행동 처리 =====

        switch (action) {
            case Util.ACTION_SKILL:
                boolean skillUsed = handleSkill(skills);
                if (!skillUsed) {
                    // 스킬 취소 → 턴 다시 시작
                    return processTurn();
                }
                break;

            case Util.ACTION_ITEM:
                boolean itemPlaced = handleItem(items);
                if (!itemPlaced) {
                    // 아이템 취소 → 턴 다시 시작
                    return processTurn();
                }
                break;

            default:
                boolean moved = handleMove();
                if (!moved) {
                    // null 반환 → 게임 종료
                    return true;
                }
                break;
        }

        return false;
    }
}
