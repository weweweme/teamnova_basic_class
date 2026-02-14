package game;

import core.*;
import piece.Piece;
import player.Player;
import skill.*;
import item.*;

/// <summary>
/// 스킬 모드 게임
/// 일반 체스에 스킬과 아이템 시스템을 추가
/// 각 플레이어가 매 턴마다 이동/스킬/아이템 중 하나를 선택
/// </summary>
public class SkillGame extends Game {

    // ========== 필드 ==========

    // 빨간팀 스킬 (파괴, 방패, 부활)
    private Skill[] redSkills;

    // 파란팀 스킬
    private Skill[] blueSkills;

    // 빨간팀 아이템 (폭탄, 함정)
    private Item[] redItems;

    // 파란팀 아이템
    private Item[] blueItems;

    // ========== 생성자 ==========

    public SkillGame(Player redPlayer, Player bluePlayer) {
        super(redPlayer, bluePlayer);

        // 각 팀에 스킬 3개씩 지급
        redSkills = new Skill[]{new DestroySkill(), new ShieldSkill(), new ReviveSkill()};
        blueSkills = new Skill[]{new DestroySkill(), new ShieldSkill(), new ReviveSkill()};

        // 각 팀에 아이템 2개씩 지급
        redItems = new Item[]{new BombItem(), new TrapItem()};
        blueItems = new Item[]{new BombItem(), new TrapItem()};
    }

    // ========== 게임 루프 오버라이드 ==========

    /// <summary>
    /// 스킬 모드 게임 루프
    /// 체크메이트/스테일메이트 판정 전에 상대 동결을 해제
    /// 동결은 일시적이므로 게임 종료 판정에 영향을 주면 안 됨
    /// </summary>
    @Override
    public void run() {
        while (true) {
            boolean quit = processTurn();
            if (quit) {
                break;
            }

            int opponentColor = getOpponentColor();

            // 체크메이트/스테일메이트 판정 전에 상대 동결 해제
            board.clearFreezes(opponentColor);

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

    // ========== 턴 처리 ==========

    /// <summary>
    /// 스킬 모드 턴 처리
    /// 1. 지난 턴에 건 방패 해제
    /// 2. 지난 턴에 걸린 동결 해제
    /// 3. 행동 선택 (이동/스킬/아이템)
    /// 4. 이동 시 아이템 트리거 확인
    /// </summary>
    @Override
    protected boolean processTurn() {
        // 현재 플레이어의 스킬/아이템 가져오기
        Skill[] skills = (currentPlayer.color == Piece.RED) ? redSkills : blueSkills;
        Item[] items = (currentPlayer.color == Piece.RED) ? redItems : blueItems;

        // 1단계: 지난 턴에 건 방패 해제
        board.clearShields(currentPlayer.color);

        // 2단계: 지난 턴에 걸린 동결 해제
        board.clearFreezes(currentPlayer.color);

        // 모든 기물이 동결되어 있으면 턴 스킵
        if (!board.hasUnfrozenPieces(currentPlayer.color)) {
            Util.clearScreen();
            board.print(-1, -1, -1, -1, null, currentPlayer.color);
            System.out.println();
            System.out.println(currentPlayer.name + "의 모든 기물이 동결되어 턴을 넘깁니다.");
            Util.delay(2000);
            return false;
        }

        // 3단계: 행동 선택
        int action = currentPlayer.chooseAction(board, skills, items);

        switch (action) {
            case 1:
                // 스킬 사용
                boolean skillUsed = handleSkill(skills);
                if (!skillUsed) {
                    // 스킬 취소 → 턴 다시 시작
                    return processTurn();
                }
                break;

            case 2:
                // 아이템 설치
                boolean itemPlaced = handleItem(items);
                if (!itemPlaced) {
                    // 아이템 취소 → 턴 다시 시작
                    return processTurn();
                }
                break;

            default:
                // 이동
                boolean moved = handleMove();
                if (!moved) {
                    // null 반환 → 게임 종료
                    return true;
                }
                break;
        }

        return false;
    }

    // ========== 행동 처리 ==========

    /// <summary>
    /// 이동 처리 (기존 체스 이동 + 아이템 트리거)
    /// 반환: true면 이동 완료, false면 게임 종료 요청
    /// </summary>
    private boolean handleMove() {
        Move move = currentPlayer.chooseMove(board);

        if (move == null) {
            Util.clearScreen();
            board.print();
            System.out.println("\n게임을 종료합니다.");
            return false;
        }

        board.executeMove(move);

        // 아이템 트리거 확인 (이동한 칸에 상대 아이템이 있으면 발동)
        String triggeredItem = board.triggerItem(move.toRow, move.toCol);
        if (triggeredItem != null) {
            Util.clearScreen();
            board.print(-1, -1, -1, -1, null, currentPlayer.color);
            System.out.println();
            System.out.println(triggeredItem + "에 걸렸습니다!");
            Util.delay(2000);
        }

        // 프로모션 확인
        if (board.isPromotion(move)) {
            int choice = currentPlayer.choosePromotion(board);
            board.promote(move.toRow, move.toCol, choice);
        }

        return true;
    }

    /// <summary>
    /// 스킬 사용 처리
    /// 반환: true면 스킬 사용 완료, false면 취소됨
    /// </summary>
    private boolean handleSkill(Skill[] skills) {
        // 스킬 선택
        int skillIndex = currentPlayer.chooseSkill(board, skills);
        if (skillIndex == -1) {
            return false;
        }

        Skill skill = skills[skillIndex];

        // 부활 스킬은 별도 처리 (기물 선택 + 위치 선택 2단계)
        if (skill instanceof ReviveSkill) {
            return handleRevive(skill);
        }

        // 대상 선택
        int[][] targets = skill.getTargets(board, currentPlayer.color);
        if (targets.length == 0) {
            return false;
        }

        int[] target = currentPlayer.chooseSkillTarget(board, targets);
        if (target == null) {
            return false;
        }

        // 스킬 실행
        skill.execute(board, target[0], target[1], currentPlayer.color);

        // 결과 표시
        Util.clearScreen();
        board.print(-1, -1, -1, -1, null, currentPlayer.color);
        System.out.println();
        System.out.println(skill.name + " 스킬 사용! (" + Util.toNotation(target[0], target[1]) + ")");
        Util.delay(1500);

        return true;
    }

    /// <summary>
    /// 부활 스킬 처리 (잡힌 기물 선택 → 배치 위치 선택)
    /// 반환: true면 부활 완료, false면 취소됨
    /// </summary>
    private boolean handleRevive(Skill skill) {
        // 잡힌 아군 기물 목록
        Piece[] captured = board.getCapturedPieces(currentPlayer.color);
        if (captured.length == 0) {
            return false;
        }

        // 부활할 기물 선택
        int pieceIndex = currentPlayer.chooseReviveTarget(board, captured);
        if (pieceIndex == -1) {
            return false;
        }

        // 배치 위치 선택 (뒷줄의 빈 칸)
        int[][] targets = skill.getTargets(board, currentPlayer.color);
        if (targets.length == 0) {
            return false;
        }

        int[] target = currentPlayer.chooseSkillTarget(board, targets);
        if (target == null) {
            return false;
        }

        // 부활 실행
        Piece revived = captured[pieceIndex];
        board.revivePiece(revived, target[0], target[1]);
        skill.useCharge();

        // 결과 표시
        Util.clearScreen();
        board.print(-1, -1, -1, -1, null, currentPlayer.color);
        System.out.println();
        System.out.println(revived.name + " 부활! (" + Util.toNotation(target[0], target[1]) + ")");
        Util.delay(1500);

        return true;
    }

    /// <summary>
    /// 아이템 설치 처리
    /// 반환: true면 설치 완료, false면 취소됨
    /// </summary>
    private boolean handleItem(Item[] items) {
        // 아이템 종류 선택
        int itemIndex = currentPlayer.chooseItemType(board, items);
        if (itemIndex == -1) {
            return false;
        }

        Item item = items[itemIndex];

        // 설치 위치 선택
        int[] target = currentPlayer.chooseItemTarget(board);
        if (target == null) {
            return false;
        }

        // 아이템 복사본을 만들어 보드에 설치
        Item placed;
        if (item instanceof BombItem) {
            placed = new BombItem(currentPlayer.color, target[0], target[1]);
        } else {
            placed = new TrapItem(currentPlayer.color, target[0], target[1]);
        }
        board.placeItem(placed);
        item.useCharge();

        // 결과 표시
        Util.clearScreen();
        board.print(-1, -1, -1, -1, null, currentPlayer.color);
        System.out.println();
        System.out.println(item.name + " 설치 완료! (" + Util.toNotation(target[0], target[1]) + ")");
        Util.delay(1500);

        return true;
    }
}
