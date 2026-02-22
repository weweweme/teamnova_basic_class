package game;

import core.Move;
import core.Chess;
import core.Util;
import piece.*;
import player.*;
import skill.*;
import item.*;

/// <summary>
/// 스킬 모드 튜토리얼 게임
/// 턴마다 스크립트가 바뀌며 파괴 → 방패 → 함정 → 폭탄 → 부활 순서로 안내
/// 모든 행동의 대상과 위치가 지정되어 있으며, 다른 선택을 하면 경고 후 다시 선택
/// </summary>
public class DemoSkillGame extends SkillGame {

    // ========== 시연 스크립트 ==========

    // 아이템 인덱스 (items 배열에서의 위치)
    private static final int ITEM_BOMB = 0;
    private static final int ITEM_TRAP = 1;

    /// <summary>
    /// 턴별 안내 메시지
    /// 파괴(턴1) → 방패(턴2) → 함정 설치(턴3) → 함정 발동(턴4)
    /// → 폭탄 설치(턴5) → 동결 체험+폭탄 발동(턴6) → 이동(턴7) → 부활(턴8)
    /// </summary>
    private static final String[] SCRIPTS = {
        // 턴 1 (빨간팀) - 파괴 스킬
        "[1/4] ▶ [2]스킬 → [1]파괴 → 파란 비숍(c8)을 선택하세요\n"
        + "파괴: 상대 기물 하나를 즉시 제거합니다.\n"
        + "각 스킬은 게임당 1회만 사용 가능합니다.",

        // 턴 2 (파란팀) - 방패 스킬
        "[1/4] ▶ [2]스킬 → [2]방패 → 파란 룩(h8)을 선택하세요\n"
        + "방패: 아군 기물에 방패(!)를 씌웁니다.\n"
        + "다음 턴까지 해당 기물은 잡히지 않습니다.",

        // 턴 3 (빨간팀) - 함정 설치
        "[2/4] ▶ [3]아이템 → [2]함정 → h6에 설치하세요\n"
        + "함정: 밟은 기물이 1턴간 움직일 수 없습니다.\n"
        + "설치한 아이템은 상대에게 보이지 않습니다.",

        // 턴 4 (파란팀) - 이동 (함정 발동)
        "[2/4] ▶ 파란 폰(h7)을 h6으로 이동하세요\n"
        + "함정이 설치된 칸을 밟으면 동결됩니다.",

        // 턴 5 (빨간팀) - 폭탄 설치
        "[3/4] ▶ [3]아이템 → [1]폭탄 → f6에 설치하세요\n"
        + "폭탄: 밟은 기물이 제거됩니다. (킹 제외)\n"
        + "설치한 아이템은 상대에게 보이지 않습니다.",

        // 턴 6 (파란팀) - 이동 (동결 체험 + 폭탄 발동)
        "[3/4] ▶ 파란 폰(f7)을 f6으로 이동하세요\n"
        + "폰(h6)이 동결(~)되어 선택할 수 없습니다!\n"
        + "동결되지 않은 다른 기물을 이동하세요.",

        // 턴 7 (빨간팀) - 이동
        "[3/4] ▶ 빨간 폰(d4)을 d5로 이동하세요\n"
        + "동결된 기물은 다음 턴에 자동으로 해제됩니다.",

        // 턴 8 (파란팀) - 부활 스킬
        "[4/4] ▶ [2]스킬 → [3]부활을 선택하세요\n"
        + "부활: 잡힌 아군 기물을 빈 칸에 되살립니다.\n"
        + "기물과 위치를 자유롭게 선택하세요."
    };

    /// <summary>
    /// 턴별 기대하는 행동 (범위 밖이면 자유 플레이)
    /// </summary>
    private static final int[] EXPECTED_ACTIONS = {
        Chess.ACTION_SKILL,   // 턴 1: 스킬 (파괴)
        Chess.ACTION_SKILL,   // 턴 2: 스킬 (방패)
        Chess.ACTION_ITEM,    // 턴 3: 아이템 (함정)
        Chess.ACTION_MOVE,    // 턴 4: 이동 (함정 발동)
        Chess.ACTION_ITEM,    // 턴 5: 아이템 (폭탄)
        Chess.ACTION_MOVE,    // 턴 6: 이동 (동결 체험 + 폭탄 발동)
        Chess.ACTION_MOVE,    // 턴 7: 이동
        Chess.ACTION_SKILL,   // 턴 8: 스킬 (부활)
    };

    /// <summary>
    /// 턴별 기대하는 스킬/아이템 인덱스 (이동 턴이면 NONE)
    /// </summary>
    private static final int[] EXPECTED_CHOICES = {
        Chess.SKILL_DESTROY,  // 턴 1: 파괴
        Chess.SKILL_SHIELD,   // 턴 2: 방패
        ITEM_TRAP,           // 턴 3: 함정
        Util.NONE,           // 턴 4: 이동
        ITEM_BOMB,           // 턴 5: 폭탄
        Util.NONE,           // 턴 6: 이동
        Util.NONE,           // 턴 7: 이동
        Chess.SKILL_REVIVE,   // 턴 8: 부활
    };

    /// <summary>
    /// 턴별 기대하는 대상 위치 (스킬 대상, 아이템 설치 위치, 이동 도착)
    /// null이면 검증 안 함
    /// </summary>
    private static final int[][] EXPECTED_TARGETS = {
        {Chess.ROW_8, Chess.COL_C},   // 턴 1: 파괴 대상 - c8 비숍
        {Chess.ROW_8, Chess.COL_H},   // 턴 2: 방패 대상 - h8 룩
        {Chess.ROW_6, Chess.COL_H},   // 턴 3: 함정 설치 - h6
        {Chess.ROW_6, Chess.COL_H},   // 턴 4: 이동 도착 - h6
        {Chess.ROW_6, Chess.COL_F},   // 턴 5: 폭탄 설치 - f6
        {Chess.ROW_6, Chess.COL_F},   // 턴 6: 이동 도착 - f6
        {Chess.ROW_5, Chess.COL_D},   // 턴 7: 이동 도착 - d5
        null,                         // 턴 8: 부활 (위치 자유)
    };

    /// <summary>
    /// 턴별 기대하는 출발 위치 (이동 턴에서만 사용, null이면 검증 안 함)
    /// </summary>
    private static final int[][] EXPECTED_FROM = {
        null,                         // 턴 1: 스킬
        null,                         // 턴 2: 스킬
        null,                         // 턴 3: 아이템
        {Chess.ROW_7, Chess.COL_H},   // 턴 4: 폰 h7에서 출발
        null,                         // 턴 5: 아이템
        {Chess.ROW_7, Chess.COL_F},   // 턴 6: 폰 f7에서 출발
        {Chess.ROW_4, Chess.COL_D},   // 턴 7: 폰 d4에서 출발
        null,                         // 턴 8: 스킬
    };

    // 동결 효과 시연을 위한 턴 인덱스
    // 이 턴(빨간 폭탄 설치) 이후 run()에서 상대 동결 해제를 건너뜀
    private static final int FREEZE_SKIP_RUN = 4;

    // 이 턴(파란 동결 체험) 시작 시 processTurn()에서 자기 동결 해제를 건너뜀
    private static final int FREEZE_SKIP_TURN = 5;

    // 모든 스크립트 완료 후 표시할 메시지
    private static final String COMPLETE_MESSAGE =
        "스킬 모드 튜토리얼 완료!\n"
        + "남은 스킬/아이템을 자유롭게 사용해보세요.\n"
        + "스킬과 아이템을 사용한 후 이동으로 턴을 마무리합니다.\n"
        + "이동에서 q를 눌러 메뉴로 돌아갈 수 있습니다.";

    // ========== 생성자 ==========

    public DemoSkillGame() {
        super(
            new SkillHumanPlayer(Piece.RED, "빨간팀"),
            new SkillHumanPlayer(Piece.BLUE, "파란팀")
        );

        // 표준 배치 제거 후 시연용 커스텀 배치
        board.clearAllPieces();

        // 빨간팀
        board.placePiece(PieceType.KING, Piece.RED, Chess.ROW_1, Chess.COL_E);     // e1 - 킹
        board.placePiece(PieceType.ROOK, Piece.RED, Chess.ROW_1, Chess.COL_A);     // a1 - 룩
        board.placePiece(PieceType.PAWN, Piece.RED, Chess.ROW_4, Chess.COL_D);     // d4 - 폰 (턴7 이동용)
        board.placePiece(PieceType.KNIGHT, Piece.RED, Chess.ROW_1, Chess.COL_B);   // b1 - 나이트

        // 파란팀
        board.placePiece(PieceType.KING, Piece.BLUE, Chess.ROW_8, Chess.COL_E);    // e8 - 킹
        board.placePiece(PieceType.ROOK, Piece.BLUE, Chess.ROW_8, Chess.COL_H);    // h8 - 룩 (턴2 방패 대상)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Chess.ROW_7, Chess.COL_H);    // h7 - 폰 (턴4 함정 발동용)
        board.placePiece(PieceType.BISHOP, Piece.BLUE, Chess.ROW_8, Chess.COL_C);  // c8 - 비숍 (턴1 파괴 대상)
        board.placePiece(PieceType.PAWN, Piece.BLUE, Chess.ROW_7, Chess.COL_F);    // f7 - 폰 (턴6 폭탄 발동용)
    }

    // ========== 게임 루프 오버라이드 ==========

    /// <summary>
    /// 동결 체험 시연을 위해 run() 오버라이드
    /// 함정 발동 후 바로 다음 턴(빨간 폭탄 설치)에서는 상대 동결 해제를 건너뛰어
    /// 그 다음 턴(파란팀)에서 동결된 기물을 직접 체험할 수 있게 함
    /// </summary>
    @Override
    public void run() {
        while (true) {
            boolean quit = processTurn();
            if (quit) {
                break;
            }

            int opponentColor = getOpponentColor();

            // 동결 체험 시연: 폭탄 설치 턴(턴5) 이후에는 상대(파란) 동결 해제를 건너뜀
            // 다음 턴(턴6)에서 파란팀이 동결 효과를 직접 체험
            int scriptIndex = turnCount - 1;
            if (scriptIndex != FREEZE_SKIP_RUN) {
                skillBoard.clearFreezes(opponentColor);
            }

            if (board.isCheckmate(opponentColor)) {
                showCheckmate();
                break;
            }

            if (board.isStalemate(opponentColor)) {
                showStalemate();
                break;
            }

            switchTurn();
        }
    }

    // ========== 턴 처리 (단계별 검증) ==========

    /// <summary>
    /// 매 턴 시작 시 스크립트를 표시하고, 플레이어의 행동을 단계별로 검증
    /// 행동 종류, 스킬/아이템 선택, 대상 위치를 모두 검증하며
    /// 기대와 다른 선택을 하면 경고 후 다시 선택
    /// 완료 턴(턴9~)은 자유 플레이 (남은 스킬/아이템 사용 가능)
    /// </summary>
    @Override
    protected boolean processTurn() {
        int scriptIndex = turnCount - 1;
        String script = (scriptIndex < SCRIPTS.length) ? SCRIPTS[scriptIndex] : COMPLETE_MESSAGE;
        int expectedAction = (scriptIndex < EXPECTED_ACTIONS.length) ? EXPECTED_ACTIONS[scriptIndex] : Util.NONE;

        board.setFooterMessage(script);

        // ===== 완료 턴: 자유 플레이 (남은 스킬/아이템 사용 가능) =====

        if (expectedAction == Util.NONE) {
            return super.processTurn();
        }

        // ===== 스크립트 턴: 설정 =====

        Skill[] skills = (currentPlayer.color == Piece.RED) ? redSkills : blueSkills;
        Item[] items = (currentPlayer.color == Piece.RED) ? redItems : blueItems;

        // 지난 턴에 건 방패 해제
        skillBoard.clearShields(currentPlayer.color);

        // 지난 턴에 걸린 동결 해제 (동결 체험 턴에서는 건너뜀)
        if (scriptIndex != FREEZE_SKIP_TURN) {
            skillBoard.clearFreezes(currentPlayer.color);
        }

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
        ((ReviveSkill) skills[Chess.SKILL_REVIVE]).setCapturedCount(
            skillBoard.getCapturedCount(currentPlayer.color)
        );

        // ===== 행동 선택 + 검증 =====

        int action = currentSkillPlayer().chooseAction(board, skills, items, false, false);

        // 기대하는 행동이 아니면 경고 후 재시도
        if (action != expectedAction) {
            board.setFooterMessage(">> 안내에 따라 진행해주세요!\n\n" + script);
            return processTurn();
        }

        // ===== 행동 처리 (단계별 검증 포함) =====

        switch (action) {
            case Chess.ACTION_SKILL:
                return handleDemoSkill(skills, scriptIndex);
            case Chess.ACTION_ITEM:
                return handleDemoItem(items, scriptIndex);
            default:
                return handleDemoMove(scriptIndex);
        }
    }

    // ========== 시연 전용 행동 처리 ==========

    /// <summary>
    /// 스킬 사용 처리 (스킬 선택 + 대상 위치 검증)
    /// 기대하는 스킬이 아니거나 대상 위치가 다르면 경고 후 턴 재시작
    /// 부활 스킬은 대상 위치를 검증하지 않음 (자유 선택)
    /// </summary>
    private boolean handleDemoSkill(Skill[] skills, int scriptIndex) {
        int expectedChoice = EXPECTED_CHOICES[scriptIndex];
        int[] expectedTarget = EXPECTED_TARGETS[scriptIndex];
        String script = SCRIPTS[scriptIndex];

        // 스킬 선택 + 검증
        int skillIndex = currentSkillPlayer().chooseSkill(board, skills);
        if (skillIndex == Util.NONE) {
            return processTurn();
        }
        if (skillIndex != expectedChoice) {
            board.setFooterMessage(">> 안내에 따라 진행해주세요!\n\n" + script);
            return processTurn();
        }

        Skill skill = skills[skillIndex];

        // 부활 스킬은 별도 처리 (기물 선택 + 위치 선택, 검증 없음)
        if (skill instanceof ReviveSkill) {
            return handleDemoRevive(skill);
        }

        // 대상 찾기
        skill.findTargets(board.grid, currentPlayer.color);
        if (skill.targetCount == 0) {
            return processTurn();
        }

        // 대상 선택 + 위치 검증
        int[] target = currentSkillPlayer().chooseSkillTarget(board, skill.targets, skill.targetCount);
        if (target == null) {
            return processTurn();
        }
        if (expectedTarget != null && (target[0] != expectedTarget[0] || target[1] != expectedTarget[1])) {
            board.setFooterMessage(">> 안내에 따라 진행해주세요!\n\n" + script);
            return processTurn();
        }

        // 스킬 실행
        skill.execute(skillBoard, target[0], target[1], currentPlayer.color);

        // 결과 표시
        Util.clearScreen();
        skillBoard.print(currentPlayer.color);
        System.out.println();
        System.out.println(skill.name + " 스킬 사용! (" + Chess.toNotation(target[0], target[1]) + ")");
        Util.delay(1500);

        return false;
    }

    /// <summary>
    /// 부활 스킬 처리 (잡힌 기물 선택 → 배치 위치 선택)
    /// 기물과 위치는 자유 선택 (검증 없음)
    /// </summary>
    private boolean handleDemoRevive(Skill skill) {
        // 잡힌 아군 기물 목록
        Piece[] captured = skillBoard.getCapturedPieces(currentPlayer.color);
        if (captured.length == 0) {
            return processTurn();
        }

        // 부활할 기물 선택
        int pieceIndex = currentSkillPlayer().chooseReviveTarget(board, captured);
        if (pieceIndex == Util.NONE) {
            return processTurn();
        }

        // 배치 위치 찾기
        skill.findTargets(board.grid, currentPlayer.color);
        if (skill.targetCount == 0) {
            return processTurn();
        }

        int[] target = currentSkillPlayer().chooseSkillTarget(board, skill.targets, skill.targetCount);
        if (target == null) {
            return processTurn();
        }

        // 부활 실행
        Piece revived = captured[pieceIndex];
        skillBoard.revivePiece(revived, target[0], target[1]);
        skill.useCharge();

        // 결과 표시
        Util.clearScreen();
        skillBoard.print(currentPlayer.color);
        System.out.println();
        System.out.println(revived.name + " 부활! (" + Chess.toNotation(target[0], target[1]) + ")");
        Util.delay(1500);

        return false;
    }

    /// <summary>
    /// 아이템 설치 처리 (아이템 선택 + 설치 위치 검증)
    /// 기대하는 아이템이 아니거나 설치 위치가 다르면 경고 후 턴 재시작
    /// </summary>
    private boolean handleDemoItem(Item[] items, int scriptIndex) {
        int expectedChoice = EXPECTED_CHOICES[scriptIndex];
        int[] expectedTarget = EXPECTED_TARGETS[scriptIndex];
        String script = SCRIPTS[scriptIndex];

        // 아이템 종류 선택 + 검증
        int itemIndex = currentSkillPlayer().chooseItemType(board, items);
        if (itemIndex == Util.NONE) {
            return processTurn();
        }
        if (itemIndex != expectedChoice) {
            board.setFooterMessage(">> 안내에 따라 진행해주세요!\n\n" + script);
            return processTurn();
        }

        Item item = items[itemIndex];

        // 설치 위치 선택 + 검증
        int[] target = currentSkillPlayer().chooseItemTarget(board);
        if (target == null) {
            return processTurn();
        }
        if (expectedTarget != null && (target[0] != expectedTarget[0] || target[1] != expectedTarget[1])) {
            board.setFooterMessage(">> 안내에 따라 진행해주세요!\n\n" + script);
            return processTurn();
        }

        // 아이템 복사본을 만들어 보드에 설치
        Item placed;
        if (item instanceof BombItem) {
            placed = new BombItem(currentPlayer.color, target[0], target[1]);
        } else {
            placed = new TrapItem(currentPlayer.color, target[0], target[1]);
        }
        skillBoard.placeItem(placed);
        item.useCharge();

        // 결과 표시
        Util.clearScreen();
        skillBoard.print(currentPlayer.color);
        System.out.println();
        System.out.println(item.name + " 설치 완료! (" + Chess.toNotation(target[0], target[1]) + ")");
        Util.delay(1500);

        return false;
    }

    /// <summary>
    /// 이동 처리 (출발/도착 위치 검증 + 아이템 트리거)
    /// 기대하는 출발/도착 위치와 다르면 경고 후 재선택
    /// </summary>
    private boolean handleDemoMove(int scriptIndex) {
        int[] expectedFrom = EXPECTED_FROM[scriptIndex];
        int[] expectedTarget = EXPECTED_TARGETS[scriptIndex];
        String script = SCRIPTS[scriptIndex];

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
            boolean toOk = (expectedTarget == null)
                || (move.toRow == expectedTarget[0] && move.toCol == expectedTarget[1]);

            if (!fromOk || !toOk) {
                board.setFooterMessage(">> 안내에 따라 진행해주세요!\n\n" + script);
                continue;
            }

            // 검증 통과 → 이동 실행
            board.executeMove(move);

            // 아이템 트리거 확인 (이동한 칸에 상대 아이템이 있으면 발동)
            String triggeredItem = skillBoard.triggerItem(move.toRow, move.toCol);
            if (!triggeredItem.isEmpty()) {
                Util.clearScreen();
                skillBoard.print(currentPlayer.color);
                System.out.println();

                // 폭탄인데 기물이 살아있으면 킹이 면역된 것
                boolean bombImmune = triggeredItem.equals("폭탄") && board.grid[move.toRow][move.toCol].hasPiece();
                if (bombImmune) {
                    System.out.println("폭탄에 걸렸지만 킹은 폭탄으로 제거할 수 없습니다!");
                } else {
                    System.out.println(triggeredItem + "에 걸렸습니다!");
                }
                Util.delay(2000);
            }

            return false;
        }
    }
}
