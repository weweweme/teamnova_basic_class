package player;

import board.*;
import core.*;
import core.Chess;
import piece.Piece;
import skill.Skill;
import item.Item;

/// <summary>
/// 사람 플레이어 (스킬 모드)
/// 기본 조작(HumanInput)과 프로모션, 스킬/아이템 선택 기능을 함께 제공
/// </summary>
public class SkillHumanPlayer extends SkillPlayer {

    // ========== 필드 ==========

    /// <summary>
    /// 키보드 입력 처리
    /// </summary>
    private final HumanInput input = new HumanInput();

    // ========== 생성자 ==========

    public SkillHumanPlayer(int color, String name) {
        super(color, name);
    }

    // ========== 수 선택 ==========

    /// <summary>
    /// 화살표 키 조작으로 수를 선택
    /// HumanInput에 위임
    /// </summary>
    @Override
    public Move chooseMove(SimpleBoard board) {
        return input.chooseMove(board, color, name);
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// 폰 프로모션 시 승격할 기물을 선택
    /// HumanInput에 위임
    /// </summary>
    @Override
    public int choosePromotion(SimpleBoard board) {
        return input.choosePromotion(board);
    }

    // ========== 스킬/아이템 선택 ==========

    /// <summary>
    /// 행동 선택 메뉴 (1: 이동, 2: 스킬, 3: 아이템)
    /// 턴 내에서 스킬/아이템 사용 후 다시 호출됨
    /// 이미 사용한 행동은 "(이미 사용!)" 표시, 사용 불가 행동은 숨김
    /// 선택 가능한 스킬/아이템이 없으면 바로 이동 반환
    /// </summary>
    @Override
    public int chooseAction(SimpleBoard board, Skill[] skills, Item[] items, boolean skillUsed, boolean itemUsed) {
        // 스킬 사용 가능 여부 (이번 턴 미사용 AND 잔여 횟수 AND 사용 조건 충족)
        boolean hasSkill = false;
        if (!skillUsed) {
            for (Skill skill : skills) {
                if (skill.hasUses() && skill.canUse(board.grid, color)) {
                    hasSkill = true;
                    break;
                }
            }
        }

        // 아이템 사용 가능 여부 (이번 턴 미사용 AND 잔여 횟수)
        boolean hasItem = false;
        if (!itemUsed) {
            for (Item item : items) {
                if (item.hasUses()) {
                    hasItem = true;
                    break;
                }
            }
        }

        // 스킬/아이템 모두 사용 불가하고 피드백도 필요 없으면 바로 이동
        if (!hasSkill && !hasItem && !skillUsed && !itemUsed) {
            return Chess.ACTION_MOVE;
        }

        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println(name + "의 차례 (" + input.getColorName(color) + ")");
        if (board.isInCheck(color)) {
            System.out.println(">> 체크! 킹을 보호하세요!");
        }
        final int KEY_MOVE = 1;
        final int KEY_SKILL = 2;
        final int KEY_ITEM = 3;

        System.out.println("[" + KEY_MOVE + "] 기물 이동");

        // 스킬: 이미 사용 → 피드백, 사용 가능 → 표시, 잔여 없음 → 숨김
        if (skillUsed) {
            System.out.println("[" + KEY_SKILL + "] 스킬 사용 (이미 사용!)");
        } else if (hasSkill) {
            System.out.println("[" + KEY_SKILL + "] 스킬 사용");
        }

        // 아이템: 동일 패턴
        if (itemUsed) {
            System.out.println("[" + KEY_ITEM + "] 아이템 설치 (이미 사용!)");
        } else if (hasItem) {
            System.out.println("[" + KEY_ITEM + "] 아이템 설치");
        }

        while (true) {
            int key = Util.readInt();
            switch (key) {
                case KEY_MOVE:
                    return Chess.ACTION_MOVE;
                case KEY_SKILL:
                    if (hasSkill) {
                        return Chess.ACTION_SKILL;
                    }
                    break;
                case KEY_ITEM:
                    if (hasItem) {
                        return Chess.ACTION_ITEM;
                    }
                    break;
            }
        }
    }

    /// <summary>
    /// 사용할 스킬 선택 (번호 입력)
    /// 사용 불가 스킬은 표시만 하고 선택 불가
    /// </summary>
    @Override
    public int chooseSkill(SimpleBoard board, Skill[] skills) {
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println("스킬을 선택하세요:");

        for (int i = 0; i < skills.length; i++) {
            Skill skill = skills[i];
            String available = (skill.hasUses() && skill.canUse(board.grid, color)) ? "" : " (사용 불가)";
            System.out.println("[" + (i + 1) + "] " + skill.name + " - " + skill.description + " (남은 " + skill.remainingUses + "회)" + available);
        }
        System.out.println("[0] 취소");

        while (true) {
            int key = Util.readInt();
            if (key == 0) {
                return Util.NONE;
            }
            if (key >= 1 && key <= skills.length) {
                int index = key - 1;
                if (skills[index].hasUses() && skills[index].canUse(board.grid, color)) {
                    return index;
                }
            }
        }
    }

    /// <summary>
    /// 스킬 대상 선택 (커서로 보드에서 대상 칸 선택)
    /// targets 배열에 포함된 칸만 선택 가능 (· 표시)
    /// </summary>
    @Override
    public int[] chooseSkillTarget(SimpleBoard board, int[][] targets, int targetCount) {
        // 첫 번째 대상으로 커서 초기화
        int cursorRow = targets[0][0];
        int cursorCol = targets[0][1];

        while (true) {
            Util.clearScreen();
            board.print(cursorRow, cursorCol, Util.NONE, Util.NONE, targets, targetCount);
            System.out.println();
            System.out.println("대상을 선택하세요 (· 표시된 칸)");
            System.out.println("방향키: 이동 | Enter: 확정 | q: 취소");

            int key = Util.readKey();

            int[] moved = input.moveCursor(cursorRow, cursorCol, key);
            if (moved != null) {
                cursorRow = moved[0];
                cursorCol = moved[1];
                continue;
            }

            if (key == Util.KEY_QUIT) {
                return null;
            }

            if (key == Util.KEY_ENTER) {
                if (board.isInArray(cursorRow, cursorCol, targets, targetCount)) {
                    return new int[]{cursorRow, cursorCol};
                }
            }
        }
    }

    /// <summary>
    /// 사용할 아이템 종류 선택 (번호 입력)
    /// </summary>
    @Override
    public int chooseItemType(SimpleBoard board, Item[] items) {
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println("아이템을 선택하세요:");

        for (int i = 0; i < items.length; i++) {
            String available = items[i].hasUses() ? "" : " (사용 불가)";
            System.out.println("[" + (i + 1) + "] " + items[i].name + " - " + items[i].description
                    + " (남은 " + items[i].remainingUses + "회)" + available);
        }
        System.out.println("[0] 취소");

        while (true) {
            int key = Util.readInt();
            if (key == 0) {
                return Util.NONE;
            }
            if (key >= 1 && key <= items.length) {
                int index = key - 1;
                if (items[index].hasUses()) {
                    return index;
                }
            }
        }
    }

    /// <summary>
    /// 아이템 설치 칸 선택 (빈 칸 중에서 커서로 선택)
    /// 기물이 없고 아이템도 없는 빈 칸만 선택 가능
    /// </summary>
    @Override
    public int[] chooseItemTarget(SimpleBoard board) {
        SkillBoard skillBoard = (SkillBoard) board;
        // 보드 중앙 근처에서 커서 시작
        int cursorRow = 3;
        int cursorCol = 3;

        while (true) {
            Util.clearScreen();
            board.print(cursorRow, cursorCol);
            System.out.println();
            System.out.println("아이템을 설치할 빈 칸을 선택하세요");
            System.out.println("방향키: 이동 | Enter: 확정 | q: 취소");

            int key = Util.readKey();

            int[] moved = input.moveCursor(cursorRow, cursorCol, key);
            if (moved != null) {
                cursorRow = moved[0];
                cursorCol = moved[1];
                continue;
            }

            if (key == Util.KEY_QUIT) {
                return null;
            }

            if (key == Util.KEY_ENTER) {
                // 빈 칸이고 아이템이 없는 칸만 선택 가능
                if (board.grid[cursorRow][cursorCol].isEmpty() && skillBoard.getItem(cursorRow, cursorCol) == null) {
                    return new int[]{cursorRow, cursorCol};
                }
            }
        }
    }

    /// <summary>
    /// 부활할 기물 선택 (숫자 키로 목록에서 선택)
    /// 잡힌 아군 기물 목록을 보여주고 번호로 선택
    /// </summary>
    @Override
    public int chooseReviveTarget(SimpleBoard board, Piece[] captured) {
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println("부활할 기물을 선택하세요:");

        for (int i = 0; i < captured.length; i++) {
            String colorCode = (captured[i].color == Chess.RED) ? Util.RED : Util.BLUE;
            System.out.println("[" + (i + 1) + "] " + colorCode + captured[i].name + Util.RESET
                    + " (가치: " + captured[i].value + ")");
        }
        System.out.println("[0] 취소");

        while (true) {
            int key = Util.readInt();
            if (key == 0) {
                return Util.NONE;
            }
            if (key >= 1 && key <= captured.length) {
                return key - 1;
            }
        }
    }
}
