package player;

import core.*;
import piece.Piece;
import skill.Skill;
import item.Item;

/// <summary>
/// 사람 플레이어
/// 화살표 키로 커서를 이동하고 Enter로 기물을 선택/이동
/// 매 입력마다 화면을 지우고 보드를 다시 그림
/// </summary>
public class HumanPlayer extends Player {

    // ========== 생성자 ==========

    public HumanPlayer(int color, String name) {
        super(color, name);
    }

    // ========== 수 선택 ==========

    /// <summary>
    /// 화살표 키 조작으로 수를 선택
    /// 1단계: 보드에서 자기 기물을 선택 (탐색 모드)
    /// 2단계: 선택한 기물의 이동 가능한 칸 중 도착지 선택 (이동 모드)
    /// q 입력 시 null 반환 (게임 종료)
    /// </summary>
    @Override
    public Move chooseMove(Board board) {
        // 커서 시작 위치 (자기 팀 킹 위치)
        int[] kingPos = board.findKing(color);
        int cursorRow = kingPos[0];
        int cursorCol = kingPos[1];

        // 1단계: 기물 선택 (탐색 모드)
        while (true) {
            Util.clearScreen();
            board.print(cursorRow, cursorCol);
            System.out.println();
            System.out.println(name + "의 차례 (" + getColorName() + ")");
            // 체크 상태 경고
            if (board.isInCheck(color)) {
                System.out.println(">> 체크! 킹을 보호하세요!");
            }
            System.out.println("방향키: 이동 | Enter: 선택 | q: 종료");

            int key = Util.readKey();

            // 커서 이동
            int[] moved = moveCursor(cursorRow, cursorCol, key);
            if (moved != null) {
                cursorRow = moved[0];
                cursorCol = moved[1];
                continue;
            }

            // 게임 종료
            if (key == Util.KEY_QUIT) {
                return null;
            }

            // Enter → 기물 선택 시도
            if (key == Util.KEY_ENTER) {
                Piece piece = board.getPiece(cursorRow, cursorCol);

                // 자기 기물이 있는 칸인지 확인
                if (piece == null || piece.color != color) {
                    continue;
                }

                // 이동 가능한 수가 있는지 확인
                int[][] validMoves = board.getFilteredMoves(cursorRow, cursorCol);
                if (validMoves.length == 0) {
                    continue;
                }

                // 2단계: 도착지 선택 (이동 모드)
                int selectedRow = cursorRow;
                int selectedCol = cursorCol;
                Move result = chooseDest(board, selectedRow, selectedCol, validMoves);

                if (result != null) {
                    // 이동 확정
                    return result;
                }
                // null이면 선택 취소 → 1단계로 돌아감
            }
        }
    }

    // ========== 도착지 선택 ==========

    /// <summary>
    /// 선택한 기물의 이동 가능한 칸 중 도착지를 화살표 키로 선택
    /// q 입력 시 null 반환 (선택 취소, 1단계로 돌아감)
    /// </summary>
    private Move chooseDest(Board board, int selectedRow, int selectedCol, int[][] validMoves) {
        // 커서를 첫 번째 이동 가능한 칸으로 이동
        int cursorRow = validMoves[0][0];
        int cursorCol = validMoves[0][1];

        while (true) {
            Util.clearScreen();
            board.print(cursorRow, cursorCol, selectedRow, selectedCol, validMoves);
            System.out.println();
            Piece piece = board.getPiece(selectedRow, selectedCol);
            System.out.println(piece.name + " 선택됨 (" + Util.toNotation(selectedRow, selectedCol) + ")");
            System.out.println("방향키: 이동 | Enter: 확정 | q: 취소");

            int key = Util.readKey();

            // 커서 이동
            int[] moved = moveCursor(cursorRow, cursorCol, key);
            if (moved != null) {
                cursorRow = moved[0];
                cursorCol = moved[1];
                continue;
            }

            // 선택 취소
            if (key == Util.KEY_QUIT) {
                return null;
            }

            // Enter → 도착지 확정 시도
            if (key == Util.KEY_ENTER) {
                // 이동 가능한 칸인지 확인
                if (board.isInArray(cursorRow, cursorCol, validMoves)) {
                    return new Move(selectedRow, selectedCol, cursorRow, cursorCol);
                }
                // 이동 불가능한 칸이면 무시
            }
        }
    }

    // ========== 커서 이동 ==========

    /// <summary>
    /// 화살표 키 입력에 따라 커서를 한 칸 이동
    /// 보드 범위(0~7)를 벗어나지 않도록 제한
    /// 이동했으면 새 좌표 반환, 화살표 키가 아니면 null 반환
    /// </summary>
    private int[] moveCursor(int row, int col, int key) {
        int newRow = row;
        int newCol = col;

        switch (key) {
            case Util.KEY_UP:
                newRow = row - 1;  // 위로 (행 감소)
                break;
            case Util.KEY_DOWN:
                newRow = row + 1;  // 아래로 (행 증가)
                break;
            case Util.KEY_LEFT:
                newCol = col - 1;  // 왼쪽 (열 감소)
                break;
            case Util.KEY_RIGHT:
                newCol = col + 1;  // 오른쪽 (열 증가)
                break;
            default:
                return null;  // 화살표 키가 아닌 입력
        }

        // 보드 범위 확인
        if (newRow < 0 || newRow >= Board.SIZE || newCol < 0 || newCol >= Board.SIZE) {
            return null;  // 범위 밖이면 이동하지 않음
        }

        return new int[]{newRow, newCol};
    }

    // ========== 프로모션 ==========

    /// <summary>
    /// 폰 프로모션 시 승격할 기물을 선택
    /// 숫자 키(1~4)로 선택
    /// </summary>
    @Override
    public int choosePromotion(Board board) {
        Util.clearScreen();
        board.print();
        System.out.println();
        System.out.println("프로모션! 승격할 기물을 선택하세요:");
        System.out.println("[1] 퀸  [2] 룩  [3] 비숍  [4] 나이트");

        // 유효한 키가 입력될 때까지 반복
        while (true) {
            int key = Util.readInt();
            if (key >= 1 && key <= 4) {
                return key;
            }
        }
    }

    // ========== 스킬/아이템 선택 (스킬 모드 전용) ==========

    /// <summary>
    /// 행동 선택 메뉴 (1: 이동, 2: 스킬, 3: 아이템)
    /// 사용 가능한 스킬/아이템이 있을 때만 해당 옵션 표시
    /// 이동만 가능하면 바로 0 반환
    /// </summary>
    @Override
    public int chooseAction(Board board, Skill[] skills, Item[] items) {
        // 사용 가능한 스킬이 있는지 확인
        boolean hasSkill = false;
        for (Skill skill : skills) {
            if (skill.hasUses() && skill.canUse(board.grid, color)) {
                hasSkill = true;
                break;
            }
        }

        // 사용 가능한 아이템이 있는지 확인
        boolean hasItem = false;
        for (Item item : items) {
            if (item.hasUses()) {
                hasItem = true;
                break;
            }
        }

        // 이동만 가능하면 바로 반환
        if (!hasSkill && !hasItem) {
            return 0;
        }

        SkillBoard skillBoard = (SkillBoard) board;
        Util.clearScreen();
        skillBoard.print(-1, -1, -1, -1, null, color);
        System.out.println();
        System.out.println(name + "의 차례 (" + getColorName() + ")");
        if (board.isInCheck(color)) {
            System.out.println(">> 체크! 킹을 보호하세요!");
        }
        System.out.println("[1] 기물 이동");
        if (hasSkill) {
            System.out.println("[2] 스킬 사용");
        }
        if (hasItem) {
            System.out.println("[3] 아이템 설치");
        }

        while (true) {
            int key = Util.readInt();
            if (key == 1) {
                return 0;
            }
            if (key == 2 && hasSkill) {
                return 1;
            }
            if (key == 3 && hasItem) {
                return 2;
            }
        }
    }

    /// <summary>
    /// 사용할 스킬 선택 (번호 입력)
    /// 사용 불가 스킬은 표시만 하고 선택 불가
    /// </summary>
    @Override
    public int chooseSkill(Board board, Skill[] skills) {
        SkillBoard skillBoard = (SkillBoard) board;
        Util.clearScreen();
        skillBoard.print(-1, -1, -1, -1, null, color);
        System.out.println();
        System.out.println("스킬을 선택하세요:");

        for (int i = 0; i < skills.length; i++) {
            Skill skill = skills[i];
            String available = (skill.hasUses() && skill.canUse(board.grid, color)) ? "" : " (사용 불가)";
            System.out.println("[" + (i + 1) + "] " + skill.name + " - " + skill.description
                    + " (남은 " + skill.remainingUses + "회)" + available);
        }
        System.out.println("[0] 취소");

        while (true) {
            int key = Util.readInt();
            if (key == 0) {
                return -1;
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
    public int[] chooseSkillTarget(Board board, int[][] targets, int targetCount) {
        SkillBoard skillBoard = (SkillBoard) board;
        // 첫 번째 대상으로 커서 초기화
        int cursorRow = targets[0][0];
        int cursorCol = targets[0][1];

        while (true) {
            Util.clearScreen();
            skillBoard.print(cursorRow, cursorCol, -1, -1, targets, targetCount, color);
            System.out.println();
            System.out.println("대상을 선택하세요 (· 표시된 칸)");
            System.out.println("방향키: 이동 | Enter: 확정 | q: 취소");

            int key = Util.readKey();

            int[] moved = moveCursor(cursorRow, cursorCol, key);
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
    public int chooseItemType(Board board, Item[] items) {
        SkillBoard skillBoard = (SkillBoard) board;
        Util.clearScreen();
        skillBoard.print(-1, -1, -1, -1, null, color);
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
                return -1;
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
    public int[] chooseItemTarget(Board board) {
        SkillBoard skillBoard = (SkillBoard) board;
        // 보드 중앙 근처에서 커서 시작
        int cursorRow = 3;
        int cursorCol = 3;

        while (true) {
            Util.clearScreen();
            skillBoard.print(cursorRow, cursorCol, -1, -1, null, color);
            System.out.println();
            System.out.println("아이템을 설치할 빈 칸을 선택하세요");
            System.out.println("방향키: 이동 | Enter: 확정 | q: 취소");

            int key = Util.readKey();

            int[] moved = moveCursor(cursorRow, cursorCol, key);
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
                if (board.getPiece(cursorRow, cursorCol) == null && skillBoard.getItem(cursorRow, cursorCol) == null) {
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
    public int chooseReviveTarget(Board board, Piece[] captured) {
        SkillBoard skillBoard = (SkillBoard) board;
        Util.clearScreen();
        skillBoard.print(-1, -1, -1, -1, null, color);
        System.out.println();
        System.out.println("부활할 기물을 선택하세요:");

        for (int i = 0; i < captured.length; i++) {
            String colorCode = (captured[i].color == Piece.RED) ? Util.RED : Util.BLUE;
            System.out.println("[" + (i + 1) + "] " + colorCode + captured[i].name + Util.RESET
                    + " (가치: " + captured[i].value + ")");
        }
        System.out.println("[0] 취소");

        while (true) {
            int key = Util.readInt();
            if (key == 0) {
                return -1;
            }
            if (key >= 1 && key <= captured.length) {
                return key - 1;
            }
        }
    }

    // ========== 유틸 ==========

    /// <summary>
    /// 색상 이름 반환 ("빨간팀" 또는 "파란팀")
    /// </summary>
    private String getColorName() {
        if (color == Piece.RED) {
            return Util.RED + "빨간팀" + Util.RESET;
        }
        return Util.BLUE + "파란팀" + Util.RESET;
    }
}
