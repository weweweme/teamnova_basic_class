package player;

import board.*;
import core.*;
import piece.*;
import skill.Skill;
import item.Item;

/// <summary>
/// AI 플레이어 (스킬 모드)
/// 공식 AI(ClassicAiPlayer)에 스킬/아이템 전략을 추가
/// </summary>
public class SkillAiPlayer extends ClassicAiPlayer implements SkillCapable {

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

    // ========== 생성자 ==========

    public SkillAiPlayer(int color, String name, int difficulty) {
        super(color, name, difficulty);
    }

    // ========== 스킬/아이템 AI 전략 ==========

    /// <summary>
    /// AI 행동 선택 전략
    /// 1순위: 파괴 스킬로 상대 퀸 제거 가능하면 스킬 사용
    /// 2순위: 가치 5 이상인 잡힌 기물이 있고 부활 가능하면 스킬 사용
    /// 3순위: 20% 확률로 방패 스킬 사용
    /// 4순위: 30% 확률로 아이템 설치
    /// 5순위: 일반 이동
    /// </summary>
    @Override
    public int chooseAction(SimpleBoard board, Skill[] skills, Item[] items) {
        int opponentColor = (color == Piece.RED) ? Piece.BLUE : Piece.RED;

        // 파괴 스킬 확인: 상대 퀸이 있으면 파괴 우선
        if (skills[Util.SKILL_DESTROY].hasUses() && skills[Util.SKILL_DESTROY].canUse(board.grid, color)) {
            for (int r = 0; r < Util.BOARD_SIZE; r++) {
                for (int c = 0; c < Util.BOARD_SIZE; c++) {
                    if (board.grid[r][c].isEmpty()) {
                        continue;
                    }
                    Piece piece = board.grid[r][c].getPiece();
                    if (piece.type == PieceType.QUEEN && piece.color == opponentColor) {
                        return Util.ACTION_SKILL;
                    }
                }
            }
        }

        // 부활 스킬 확인: 가치 높은 잡힌 기물이 있으면 부활
        if (skills[Util.SKILL_REVIVE].hasUses() && skills[Util.SKILL_REVIVE].canUse(board.grid, color)) {
            SkillBoard skillBoard = (SkillBoard) board;
            Piece[] captured = skillBoard.getCapturedPieces(color);
            for (Piece p : captured) {
                if (p.value >= REVIVE_VALUE_THRESHOLD) {
                    return Util.ACTION_SKILL;
                }
            }
        }

        // 방패 스킬 확인: 20% 확률로 사용
        boolean shieldAvailable = skills[Util.SKILL_SHIELD].hasUses() && skills[Util.SKILL_SHIELD].canUse(board.grid, color);
        boolean randomTrigger = Util.rand(SHIELD_CHANCE) == 0;
        if (shieldAvailable && randomTrigger) {
            return Util.ACTION_SKILL;
        }

        // 아이템 설치: 30% 확률로 사용
        boolean hasItem = false;
        for (Item item : items) {
            if (item.hasUses()) {
                hasItem = true;
                break;
            }
        }
        if (hasItem && Util.rand(ITEM_CHANCE_RANGE) < ITEM_CHANCE_THRESHOLD) {
            return Util.ACTION_ITEM;
        }

        return Util.ACTION_MOVE;
    }

    /// <summary>
    /// AI 스킬 선택
    /// 우선순위: 파괴(퀸 대상) > 부활(가치 높은 기물) > 방패(가치 높은 기물)
    /// </summary>
    @Override
    public int chooseSkill(SimpleBoard board, Skill[] skills) {
        // 파괴 스킬
        if (skills[Util.SKILL_DESTROY].hasUses() && skills[Util.SKILL_DESTROY].canUse(board.grid, color)) {
            return Util.SKILL_DESTROY;
        }

        // 부활 스킬
        if (skills[Util.SKILL_REVIVE].hasUses() && skills[Util.SKILL_REVIVE].canUse(board.grid, color)) {
            return Util.SKILL_REVIVE;
        }

        // 방패 스킬
        if (skills[Util.SKILL_SHIELD].hasUses() && skills[Util.SKILL_SHIELD].canUse(board.grid, color)) {
            return Util.SKILL_SHIELD;
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
            for (int r = 0; r < Util.BOARD_SIZE; r++) {
                for (int c = 0; c < Util.BOARD_SIZE; c++) {
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
