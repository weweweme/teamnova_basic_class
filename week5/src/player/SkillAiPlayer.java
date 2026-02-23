package player;

import board.*;
import core.*;
import core.Chess;
import piece.*;
import skill.Skill;
import item.Item;

/// <summary>
/// AI 플레이어 (스킬 모드)
/// AI 전략(AiInput)과 프로모션, 스킬/아이템 전략을 함께 제공
/// </summary>
public class SkillAiPlayer extends SkillPlayer {

    // ========== AI 전략 상수 ==========

    // 부활 대상으로 삼을 최소 기물 가치
    private static final int REVIVE_VALUE_THRESHOLD = 5;

    // 방패 스킬 발동 확률 분모 (1/5 = 20%)
    private static final int SHIELD_CHANCE = 5;

    // 아이템 설치 확률 (3/10 = 30%)
    private static final int ITEM_CHANCE_RANGE = 10;
    private static final int ITEM_CHANCE_THRESHOLD = 3;

    // 아이템 설치 우선 탐색 영역 (중앙 4x4)
    private static final int CENTER_MIN = 2;
    private static final int CENTER_MAX = 5;

    // ========== 필드 ==========

    /// <summary>
    /// AI 전략 처리
    /// </summary>
    private final AiInput aiInput;

    // ========== 생성자 ==========

    public SkillAiPlayer(int color, String name) {
        super(color, name);
        this.aiInput = new AiInput();
    }

    // ========== 수 선택 ==========

    /// <summary>
    /// AI가 수를 선택
    /// AiInput에 위임
    /// </summary>
    @Override
    public Move chooseMove(SimpleBoard board) {
        return aiInput.chooseMove(board, color, name);
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// AI는 항상 퀸으로 승격 (가장 강한 기물)
    /// </summary>
    @Override
    public int choosePromotion(SimpleBoard board) {
        return Chess.PROMOTE_QUEEN;
    }

    // ========== 스킬/아이템 AI 전략 ==========

    /// <summary>
    /// AI 행동 선택 전략 (턴 내에서 스킬/아이템 사용 후 다시 호출됨)
    /// 1순위: 파괴 스킬로 상대 퀸 제거 가능하면 스킬 사용
    /// 2순위: 가치 5 이상인 잡힌 기물이 있고 부활 가능하면 스킬 사용
    /// 3순위: 20% 확률로 방패 스킬 사용
    /// 4순위: 30% 확률로 아이템 설치
    /// 5순위: 일반 이동
    /// </summary>
    @Override
    public int chooseAction(SimpleBoard board, Skill[] skills, Item[] items, boolean skillUsed, boolean itemUsed) {
        int opponentColor = (color == Chess.RED) ? Chess.BLUE : Chess.RED;

        // 스킬 확인 (이번 턴 미사용일 때만)
        if (!skillUsed) {
            // 파괴 스킬 확인: 상대 퀸이 있으면 파괴 우선
            if (skills[Chess.SKILL_DESTROY].hasUses() && skills[Chess.SKILL_DESTROY].canUse(board.grid, color)) {
                for (int r = 0; r < Chess.BOARD_SIZE; r++) {
                    for (int c = 0; c < Chess.BOARD_SIZE; c++) {
                        if (board.grid[r][c].isEmpty()) {
                            continue;
                        }
                        Piece piece = board.grid[r][c].getPiece();
                        if (piece.type == PieceType.QUEEN && piece.color == opponentColor) {
                            return Chess.ACTION_SKILL;
                        }
                    }
                }
            }

            // 부활 스킬 확인: 가치 높은 잡힌 기물이 있으면 부활
            if (skills[Chess.SKILL_REVIVE].hasUses() && skills[Chess.SKILL_REVIVE].canUse(board.grid, color)) {
                SkillBoard skillBoard = (SkillBoard) board;
                Piece[] captured = skillBoard.getCapturedPieces(color);
                for (Piece p : captured) {
                    if (p.value >= REVIVE_VALUE_THRESHOLD) {
                        return Chess.ACTION_SKILL;
                    }
                }
            }

            // 방패 스킬 확인: 20% 확률로 사용
            boolean shieldAvailable = skills[Chess.SKILL_SHIELD].hasUses() && skills[Chess.SKILL_SHIELD].canUse(board.grid, color);
            boolean randomTrigger = Util.rand(SHIELD_CHANCE) == 0;
            if (shieldAvailable && randomTrigger) {
                return Chess.ACTION_SKILL;
            }
        }

        // 아이템 확인 (이번 턴 미사용일 때만)
        if (!itemUsed) {
            boolean hasItem = false;
            for (Item item : items) {
                if (item.hasUses()) {
                    hasItem = true;
                    break;
                }
            }
            if (hasItem && Util.rand(ITEM_CHANCE_RANGE) < ITEM_CHANCE_THRESHOLD) {
                return Chess.ACTION_ITEM;
            }
        }

        return Chess.ACTION_MOVE;
    }

    /// <summary>
    /// AI 스킬 선택
    /// 우선순위: 파괴(퀸 대상) > 부활(가치 높은 기물) > 방패(가치 높은 기물)
    /// </summary>
    @Override
    public int chooseSkill(SimpleBoard board, Skill[] skills) {
        // 파괴 스킬
        if (skills[Chess.SKILL_DESTROY].hasUses() && skills[Chess.SKILL_DESTROY].canUse(board.grid, color)) {
            return Chess.SKILL_DESTROY;
        }

        // 부활 스킬
        if (skills[Chess.SKILL_REVIVE].hasUses() && skills[Chess.SKILL_REVIVE].canUse(board.grid, color)) {
            return Chess.SKILL_REVIVE;
        }

        // 방패 스킬
        if (skills[Chess.SKILL_SHIELD].hasUses() && skills[Chess.SKILL_SHIELD].canUse(board.grid, color)) {
            return Chess.SKILL_SHIELD;
        }

        return Util.NONE;
    }

    /// <summary>
    /// AI 스킬 대상 선택 (가장 가치 높은 기물이 있는 칸 우선)
    /// 기물이 없는 대상(빈 칸)이면 랜덤 선택
    /// </summary>
    @Override
    public int[] chooseSkillTarget(SimpleBoard board, int[][] targets, int targetCount) {
        if (targetCount == 0) {
            return null;
        }

        // 가치가 가장 높은 기물이 있는 칸 선택
        int bestIndex = 0;
        int bestValue = 0;

        for (int i = 0; i < targetCount; i++) {
            if (board.grid[targets[i][0]][targets[i][1]].isEmpty()) {
                continue;
            }
            Piece piece = board.grid[targets[i][0]][targets[i][1]].getPiece();
            if (piece.value > bestValue) {
                bestValue = piece.value;
                bestIndex = i;
            }
        }

        // 기물이 없는 대상(빈 칸, 부활 위치 등)이면 랜덤 선택
        if (bestValue == 0) {
            bestIndex = Util.rand(targetCount);
        }

        return targets[bestIndex];
    }

    /// <summary>
    /// AI 아이템 종류 선택 (사용 가능한 것 중 랜덤)
    /// </summary>
    @Override
    public int chooseItemType(SimpleBoard board, Item[] items) {
        java.util.ArrayList<Integer> available = new java.util.ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            if (items[i].hasUses()) {
                available.add(i);
            }
        }
        if (available.isEmpty()) {
            return Util.NONE;
        }
        return available.get(Util.rand(available.size()));
    }

    /// <summary>
    /// AI 아이템 설치 위치 선택 (중앙 근처의 빈 칸에 랜덤 설치)
    /// 중앙에 빈 칸이 없으면 전체에서 탐색
    /// </summary>
    @Override
    public int[] chooseItemTarget(SimpleBoard board) {
        SkillBoard skillBoard = (SkillBoard) board;
        // 보드 중앙 4x4 영역(2~5행, 2~5열)에서 빈 칸 탐색
        java.util.ArrayList<int[]> candidates = new java.util.ArrayList<>();
        for (int r = CENTER_MIN; r <= CENTER_MAX; r++) {
            for (int c = CENTER_MIN; c <= CENTER_MAX; c++) {
                if (board.grid[r][c].isEmpty() && skillBoard.getItem(r, c) == null) {
                    candidates.add(new int[]{r, c});
                }
            }
        }

        // 중앙에 빈 칸이 없으면 전체에서 탐색
        if (candidates.isEmpty()) {
            for (int r = 0; r < Chess.BOARD_SIZE; r++) {
                for (int c = 0; c < Chess.BOARD_SIZE; c++) {
                    if (board.grid[r][c].isEmpty() && skillBoard.getItem(r, c) == null) {
                        candidates.add(new int[]{r, c});
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(Util.rand(candidates.size()));
    }

    /// <summary>
    /// AI 부활 대상 선택 (가치가 가장 높은 기물 우선)
    /// </summary>
    @Override
    public int chooseReviveTarget(SimpleBoard board, Piece[] captured) {
        if (captured.length == 0) {
            return Util.NONE;
        }

        int bestIndex = 0;
        int bestValue = 0;

        for (int i = 0; i < captured.length; i++) {
            if (captured[i].value > bestValue) {
                bestValue = captured[i].value;
                bestIndex = i;
            }
        }

        return bestIndex;
    }
}
