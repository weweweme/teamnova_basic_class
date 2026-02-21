package game;

import board.*;
import core.*;
import piece.Piece;
import player.Player;
import player.SkillPlayer;
import skill.*;
import item.*;

/// <summary>
/// 스킬 모드 게임
/// ClassicGame의 공식 규칙에 스킬과 아이템 시스템을 추가
/// 각 플레이어가 매 턴마다 이동/스킬/아이템 중 하나를 선택
/// </summary>
public class SkillGame extends ClassicGame {

    // ========== 필드 ==========

    // 스킬 보드 (스킬 전용 메서드 호출용)
    protected SkillBoard skillBoard;

    // 빨간팀 스킬 (파괴, 방패, 부활)
    protected Skill[] redSkills;

    // 파란팀 스킬
    protected Skill[] blueSkills;

    // 빨간팀 아이템 (폭탄, 함정)
    protected Item[] redItems;

    // 파란팀 아이템
    protected Item[] blueItems;

    // 빨간팀 플레이어 (SkillPlayer 타입으로 스킬 메서드 호출용)
    private SkillPlayer skillRedPlayer;

    // 파란팀 플레이어
    private SkillPlayer skillBluePlayer;

    // ========== 생성자 ==========

    public SkillGame(SkillPlayer redPlayer, SkillPlayer bluePlayer) {
        super(redPlayer, bluePlayer);
        this.skillRedPlayer = redPlayer;
        this.skillBluePlayer = bluePlayer;

        // 각 팀에 스킬 3개씩 지급
        redSkills = new Skill[]{new DestroySkill(), new ShieldSkill(), new ReviveSkill()};
        blueSkills = new Skill[]{new DestroySkill(), new ShieldSkill(), new ReviveSkill()};

        // 각 팀에 아이템 2개씩 지급
        redItems = new Item[]{new BombItem(), new TrapItem()};
        blueItems = new Item[]{new BombItem(), new TrapItem()};
    }

    // ========== 헬퍼 ==========

    /// <summary>
    /// 현재 플레이어를 SkillPlayer 타입으로 반환
    /// </summary>
    protected SkillPlayer currentSkillPlayer() {
        if (currentPlayer == redPlayer) {
            return skillRedPlayer;
        }
        return skillBluePlayer;
    }

    // ========== 보드 생성 ==========

    /// <summary>
    /// 스킬 모드용 SkillBoard 생성
    /// </summary>
    @Override
    protected SimpleBoard createBoard() {
        skillBoard = new SkillBoard();
        return skillBoard;
    }

    // ========== 훅 메서드 오버라이드 ==========

    /// <summary>
    /// 이동 후 프로모션 확인
    /// ClassicGame의 afterMove와 동일하지만 skillBoard를 사용
    /// </summary>
    @Override
    protected void afterMove(Move move) {
        if (skillBoard.isPromotion(move)) {
            int choice = currentClassicPlayer().choosePromotion(board);
            skillBoard.promote(move.toRow, move.toCol, choice);
        }
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
            skillBoard.clearFreezes(opponentColor);

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
    /// 3. 부활 스킬의 잡힌 기물 수 갱신
    /// 4. 행동 선택 (이동/스킬/아이템)
    /// </summary>
    @Override
    protected boolean processTurn() {
        // 현재 플레이어의 스킬/아이템 가져오기
        Skill[] skills = (currentPlayer.color == Piece.RED) ? redSkills : blueSkills;
        Item[] items = (currentPlayer.color == Piece.RED) ? redItems : blueItems;

        // 1단계: 지난 턴에 건 방패 해제
        skillBoard.clearShields(currentPlayer.color);

        // 2단계: 지난 턴에 걸린 동결 해제
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

        // 3단계: 부활 스킬의 잡힌 기물 수 갱신 (격자에 없는 정보)
        ((ReviveSkill) skills[Chess.SKILL_REVIVE]).setCapturedCount(
            skillBoard.getCapturedCount(currentPlayer.color)
        );

        // 4단계: 행동 선택
        int action = currentSkillPlayer().chooseAction(board, skills, items);

        switch (action) {
            case Chess.ACTION_SKILL:
                // 스킬 사용
                boolean skillUsed = handleSkill(skills);
                if (!skillUsed) {
                    // 스킬 취소 → 턴 다시 시작
                    return processTurn();
                }
                break;

            case Chess.ACTION_ITEM:
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
    protected boolean handleMove() {
        Move move = currentPlayer.chooseMove(board);

        if (move == null) {
            Util.clearScreen();
            board.print();
            System.out.println("\n게임을 종료합니다.");
            return false;
        }

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

        // 프로모션 확인 (ClassicGame의 afterMove 훅에서 처리)
        afterMove(move);

        return true;
    }

    /// <summary>
    /// 스킬 사용 처리
    /// 반환: true면 스킬 사용 완료, false면 취소됨
    /// </summary>
    protected boolean handleSkill(Skill[] skills) {
        // 스킬 선택
        int skillIndex = currentSkillPlayer().chooseSkill(board, skills);
        if (skillIndex == Util.NONE) {
            return false;
        }

        Skill skill = skills[skillIndex];

        // 부활 스킬은 별도 처리 (기물 선택 + 위치 선택 2단계)
        if (skill instanceof ReviveSkill) {
            return handleRevive(skill);
        }

        // 대상 찾기 (버퍼에 저장)
        skill.findTargets(board.grid, currentPlayer.color);
        if (skill.targetCount == 0) {
            return false;
        }

        int[] target = currentSkillPlayer().chooseSkillTarget(board, skill.targets, skill.targetCount);
        if (target == null) {
            return false;
        }

        // 스킬 실행
        skill.execute(skillBoard, target[0], target[1], currentPlayer.color);

        // 결과 표시
        Util.clearScreen();
        skillBoard.print(currentPlayer.color);
        System.out.println();
        System.out.println(skill.name + " 스킬 사용! (" + Chess.toNotation(target[0], target[1]) + ")");
        Util.delay(1500);

        return true;
    }

    /// <summary>
    /// 부활 스킬 처리 (잡힌 기물 선택 → 배치 위치 선택)
    /// 반환: true면 부활 완료, false면 취소됨
    /// </summary>
    private boolean handleRevive(Skill skill) {
        // 잡힌 아군 기물 목록
        Piece[] captured = skillBoard.getCapturedPieces(currentPlayer.color);
        if (captured.length == 0) {
            return false;
        }

        // 부활할 기물 선택
        int pieceIndex = currentSkillPlayer().chooseReviveTarget(board, captured);
        if (pieceIndex == Util.NONE) {
            return false;
        }

        // 배치 위치 찾기 (버퍼에 저장)
        skill.findTargets(board.grid, currentPlayer.color);
        if (skill.targetCount == 0) {
            return false;
        }

        int[] target = currentSkillPlayer().chooseSkillTarget(board, skill.targets, skill.targetCount);
        if (target == null) {
            return false;
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

        return true;
    }

    /// <summary>
    /// 아이템 설치 처리
    /// 반환: true면 설치 완료, false면 취소됨
    /// </summary>
    protected boolean handleItem(Item[] items) {
        // 아이템 종류 선택
        int itemIndex = currentSkillPlayer().chooseItemType(board, items);
        if (itemIndex == Util.NONE) {
            return false;
        }

        Item item = items[itemIndex];

        // 설치 위치 선택
        int[] target = currentSkillPlayer().chooseItemTarget(board);
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
        skillBoard.placeItem(placed);
        item.useCharge();

        // 결과 표시
        Util.clearScreen();
        skillBoard.print(currentPlayer.color);
        System.out.println();
        System.out.println(item.name + " 설치 완료! (" + Chess.toNotation(target[0], target[1]) + ")");
        Util.delay(1500);

        return true;
    }
}
